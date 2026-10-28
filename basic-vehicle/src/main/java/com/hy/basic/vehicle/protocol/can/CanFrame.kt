package com.hy.basic.vehicle.protocol.can

import com.hy.basic.vehicle.protocol.extractBits
import com.hy.basic.vehicle.protocol.extractSignedBits
import com.hy.basic.vehicle.protocol.toHexString
import com.hy.basic.vehicle.protocol.u8At

/**
 * desc: 一帧 CAN / CAN FD 报文。
 *
 * 只做数据承载和取值,不做收发。收发由具体通道(SocketCAN、厂商 Service)负责,
 * 这样上层解析和单元测试都不依赖 Android。
 *
 * 注意:
 * - 标准帧 ID 最大 0x7FF,扩展帧最大 0x1FFFFFFF,构造时直接校验;
 * - 经典 CAN 数据最长 8 字节,CAN FD 最长 64 字节,别把 FD 帧丢给只支持 8 字节的通道;
 * - equals/hashCode 只比较 ID、标志位和数据,不比较时间戳,方便测试里直接对比帧内容。
 */
data class CanFrame(
    /** 报文 ID,标准帧 11 位 / 扩展帧 29 位 */
    val id: Int,
    /** 数据区,经典 CAN 最长 8 字节,CAN FD 最长 64 字节 */
    val data: ByteArray,
    /** 是否扩展帧 */
    val extended: Boolean = false,
    /** 是否 CAN FD 帧 */
    val fd: Boolean = false,
    /** 接收时间戳(毫秒),由通道层填写 */
    val timestampMs: Long = 0L,
) {

    init {
        require(id >= 0) { "CAN ID 不能为负: $id" }
        val maxId = if (extended) 0x1FFFFFFF else 0x7FF
        require(id <= maxId) { "CAN ID 超出范围: 0x${id.toString(16)}, 上限 0x${maxId.toString(16)}" }
        val maxLength = if (fd) 64 else 8
        require(data.size <= maxLength) { "数据长度 ${data.size} 超过上限 $maxLength(fd=$fd)" }
    }

    /** 实际数据字节数。CAN FD 的 DLC 编码(9~15)换算由驱动层处理 */
    val byteCount: Int get() = data.size

    /** 按字节下标取无符号值 */
    fun byteAt(index: Int): Int = data.u8At(index)

    /** 直接按位域取值,不经过信号描述 */
    fun bits(startBit: Int, length: Int, motorola: Boolean = false): Long =
        data.extractBits(startBit, length, motorola)

    /** 按位域取有符号值 */
    fun signedBits(startBit: Int, length: Int, motorola: Boolean = false): Long =
        data.extractSignedBits(startBit, length, motorola)

    /** 按信号描述解码 */
    fun decode(spec: CanSignalSpec): CanSignalValue = CanSignalCodec.decode(this, spec)

    /** 按信号描述写入物理值,返回新的帧(原帧不变) */
    fun write(spec: CanSignalSpec, physical: Double): CanFrame =
        CanSignalCodec.write(this, spec, physical)

    /** 按信号描述写入原始码值,主要用于构造无效值/仿真帧 */
    fun writeRaw(spec: CanSignalSpec, raw: Long): CanFrame =
        CanSignalCodec.writeRaw(this, spec, raw)

    /** 数据区转十六进制文本,便于日志排查 */
    fun hex(): String = data.toHexString()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CanFrame) return false
        return id == other.id &&
            extended == other.extended &&
            fd == other.fd &&
            data.contentEquals(other.data)
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + data.contentHashCode()
        result = 31 * result + extended.hashCode()
        result = 31 * result + fd.hashCode()
        return result
    }

    override fun toString(): String =
        "CanFrame(id=0x${id.toString(16).uppercase()}, len=$byteCount, data=${hex()})"

    companion object {

        /** 标准帧快捷构造 */
        fun standard(id: Int, data: ByteArray, timestampMs: Long = 0L): CanFrame =
            CanFrame(id = id, data = data, extended = false, fd = false, timestampMs = timestampMs)

        /** 扩展帧快捷构造 */
        fun extended(id: Int, data: ByteArray, timestampMs: Long = 0L): CanFrame =
            CanFrame(id = id, data = data, extended = true, fd = false, timestampMs = timestampMs)
    }
}
