package com.hy.basic.vehicle.protocol.isotp

import com.hy.basic.vehicle.protocol.can.CanFrame
import com.hy.basic.vehicle.signal.monotonicMs

/**
 * ISO-TP 帧编解码。
 *
 * 用法：
 * `decode(frame)` 拆 PCI，`encodeSingle/encodeFirst/encodeConsecutive/encodeFlowControl` 组帧。
 * 注意：首帧长度使用 12 位格式，负载上限 4095；超过该长度的 FD 长报文暂不支持。
 */
object IsoTpCodec {

    /** 单帧 PCI 类型 */
    private const val TYPE_SINGLE = 0x0

    /** 首帧 PCI 类型 */
    private const val TYPE_FIRST = 0x1

    /** 连续帧 PCI 类型 */
    private const val TYPE_CONSECUTIVE = 0x2

    /** 流控帧 PCI 类型 */
    private const val TYPE_FLOW_CONTROL = 0x3

    /** 解析一帧 ISO-TP 数据；数据不足或类型非法时返回 null */
    fun decode(frame: CanFrame): IsoTpFrame? {
        val data = frame.data
        if (data.isEmpty()) return null

        val type = (data[0].toInt() and 0xFF) ushr 4
        return when (type) {
            TYPE_SINGLE -> decodeSingle(data, frame.fd)
            TYPE_FIRST -> decodeFirst(data)
            TYPE_CONSECUTIVE -> decodeConsecutive(data)
            TYPE_FLOW_CONTROL -> decodeFlowControl(data)
            else -> null
        }
    }

    /** 组单帧；CAN FD 超过 7 字节时使用转义长度格式 */
    fun encodeSingle(payload: ByteArray, config: IsoTpConfig): CanFrame {
        require(payload.isNotEmpty()) { "单帧负载不能为空" }
        require(payload.size <= config.singleFrameCapacity) {
            "单帧负载过长: ${payload.size} > ${config.singleFrameCapacity}"
        }
        val escaped = config.fd && payload.size > 7
        val data = newData(config, if (escaped) 2 + payload.size else 1 + payload.size)
        if (escaped) {
            data[0] = TYPE_SINGLE.toByte()
            data[1] = payload.size.toByte()
            payload.copyInto(data, 2)
        } else {
            data[0] = (TYPE_SINGLE shl 4 or payload.size).toByte()
            payload.copyInto(data, 1)
        }
        return newFrame(config.requestId, data, config)
    }

    /** 组首帧；负载长度必须在 8..4095 之间 */
    fun encodeFirst(payload: ByteArray, config: IsoTpConfig): CanFrame {
        require(payload.size in (config.singleFrameCapacity + 1)..config.maxPayloadLength) {
            "首帧负载长度超出范围: ${payload.size}"
        }
        val data = newData(config, 2 + config.firstFrameCapacity)
        data[0] = (TYPE_FIRST shl 4 or (payload.size ushr 8)).toByte()
        data[1] = payload.size.toByte()
        payload.copyInto(data, 2, 0, config.firstFrameCapacity)
        return newFrame(config.requestId, data, config)
    }

    /** 组连续帧；[sequence] 按 0..15 循环 */
    fun encodeConsecutive(
        payload: ByteArray,
        sequence: Int,
        config: IsoTpConfig,
    ): CanFrame {
        require(payload.size <= config.consecutiveFrameCapacity) {
            "连续帧负载过长: ${payload.size}"
        }
        val data = newData(config, 1 + payload.size)
        data[0] = (TYPE_CONSECUTIVE shl 4 or (sequence and 0x0F)).toByte()
        payload.copyInto(data, 1)
        return newFrame(config.requestId, data, config)
    }

    /** 组流控帧；流控始终发往对端请求 ID，即本端的 [IsoTpConfig.requestId] */
    fun encodeFlowControl(
        status: IsoTpFlowStatus,
        blockSize: Int,
        separationTimeMs: Int,
        config: IsoTpConfig,
    ): CanFrame {
        require(blockSize in 0..0xFF) { "blockSize 必须在 0..255: $blockSize" }
        require(separationTimeMs in 0..127) {
            "separationTimeMs 必须在 0..127: $separationTimeMs"
        }
        val data = newData(config, 8)
        data[0] = (TYPE_FLOW_CONTROL shl 4 or status.ordinal).toByte()
        data[1] = blockSize.toByte()
        data[2] = separationTimeMs.toByte()
        return newFrame(config.requestId, data, config)
    }

    /** STmin 字节转微秒，0xF1..0xF9 表示 100..900 微秒 */
    fun separationTimeUs(byte: Int): Long = when {
        byte in 0..0x7F -> byte * 1_000L
        byte in 0xF1..0xF9 -> (byte - 0xF0) * 100L
        else -> 0L
    }

    /** 解析单帧；CAN FD 长度 0 表示转义格式，第二字节才是长度 */
    private fun decodeSingle(data: ByteArray, fd: Boolean): IsoTpFrame? {
        val length = data[0].toInt() and 0x0F
        if (length == 0) {
            if (!fd || data.size < 2) return null
            val escapedLength = data[1].toInt() and 0xFF
            if (escapedLength !in 1..62 || data.size < 2 + escapedLength) return null
            return IsoTpFrame.Single(data.copyOfRange(2, 2 + escapedLength))
        }
        if (data.size < 1 + length) return null
        return IsoTpFrame.Single(data.copyOfRange(1, 1 + length))
    }

    /** 解析首帧，12 位长度格式 */
    private fun decodeFirst(data: ByteArray): IsoTpFrame? {
        if (data.size < 2) return null
        val totalLength = ((data[0].toInt() and 0x0F) shl 8) or
            (data[1].toInt() and 0xFF)
        if (totalLength == 0) return null
        val payloadLength = minOf(totalLength, data.size - 2)
        return IsoTpFrame.First(
            totalLength = totalLength,
            payload = data.copyOfRange(2, 2 + payloadLength),
        )
    }

    /** 解析连续帧 */
    private fun decodeConsecutive(data: ByteArray): IsoTpFrame? {
        if (data.size < 2) return null
        return IsoTpFrame.Consecutive(
            sequence = data[0].toInt() and 0x0F,
            payload = data.copyOfRange(1, data.size),
        )
    }

    /** 解析流控帧 */
    private fun decodeFlowControl(data: ByteArray): IsoTpFrame? {
        if (data.size < 3) return null
        val status = when (data[0].toInt() and 0x0F) {
            0 -> IsoTpFlowStatus.CONTINUE
            1 -> IsoTpFlowStatus.WAIT
            2 -> IsoTpFlowStatus.OVERFLOW
            else -> return null
        }
        return IsoTpFrame.FlowControl(
            status = status,
            blockSize = data[1].toInt() and 0xFF,
            separationTimeUs = separationTimeUs(data[2].toInt() and 0xFF),
        )
    }

    /** 按配置创建目标长度的数据区并填入填充字节 */
    private fun newData(config: IsoTpConfig, naturalSize: Int): ByteArray {
        val size = when {
            config.padding != null -> if (config.fd) 64 else 8
            !config.fd -> 8
            else -> naturalSize
        }
        val data = ByteArray(size)
        config.padding?.let { padding ->
            data.fill(padding.toByte())
        }
        return data
    }

    /** 创建带时间戳的 CAN 帧 */
    private fun newFrame(id: Int, data: ByteArray, config: IsoTpConfig): CanFrame =
        CanFrame(
            id = id,
            data = data,
            extended = config.extended,
            fd = config.fd,
            timestampMs = monotonicMs(),
            bitRateSwitch = config.bitRateSwitch,
        )
}
