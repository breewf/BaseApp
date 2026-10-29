package com.hy.basic.vehicle.transport.socketcan

import com.hy.basic.vehicle.protocol.can.CanFrame
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * SocketCAN `struct can_frame` / `struct canfd_frame` 与 [CanFrame] 互转。
 *
 * 用法：
 * `SocketCanFrameCodec.decode(buffer, readSize)` 解析驱动读到的字节，`encode(frame)` 生成写缓冲。
 * 注意：结构体使用主机字节序，Android 设备基本都是小端；不解码 RTR/错误帧。
 */
object SocketCanFrameCodec {

    /** 经典 CAN 帧结构体长度 */
    const val CLASSIC_FRAME_SIZE = 16

    /** CAN FD 帧结构体长度 */
    const val FD_FRAME_SIZE = 72

    /** 扩展帧标志 */
    private const val CAN_EFF_FLAG = Int.MIN_VALUE

    /** 远程帧标志，当前模型不承载 */
    private const val CAN_RTR_FLAG = 0x40000000

    /** 错误帧标志，当前模型不承载 */
    private const val CAN_ERR_FLAG = 0x20000000

    /** CAN FD 可变速率标志 */
    private const val CANFD_BRS = 0x01

    /**
     * 解析一帧 SocketCAN 数据。
     *
     * [size] 为实际读取长度，16 按经典帧处理，72 按 CAN FD 处理，其它长度返回 null。
     */
    fun decode(buffer: ByteArray, size: Int): CanFrame? {
        if (size != CLASSIC_FRAME_SIZE && size != FD_FRAME_SIZE) return null
        if (buffer.size < size) return null

        val rawId = ByteBuffer.wrap(buffer, 0, 4)
            .order(ByteOrder.nativeOrder())
            .int
        if ((rawId and CAN_RTR_FLAG) != 0 || (rawId and CAN_ERR_FLAG) != 0) return null

        val extended = (rawId and CAN_EFF_FLAG) != 0
        val id = rawId and CAN_EFF_FLAG.inv()

        return if (size == CLASSIC_FRAME_SIZE) {
            val length = (buffer[4].toInt() and 0xFF).coerceAtMost(8)
            CanFrame(
                id = id,
                data = buffer.copyOfRange(8, 8 + length),
                extended = extended,
                fd = false,
            )
        } else {
            val length = buffer[4].toInt() and 0xFF
            if (length > 64) return null
            CanFrame(
                id = id,
                data = buffer.copyOfRange(8, 8 + length),
                extended = extended,
                fd = true,
                bitRateSwitch = (buffer[5].toInt() and CANFD_BRS) != 0,
            )
        }
    }

    /** 把一帧编码成 SocketCAN 写缓冲 */
    fun encode(frame: CanFrame): ByteArray {
        val size = if (frame.fd) FD_FRAME_SIZE else CLASSIC_FRAME_SIZE
        val buffer = ByteArray(size)
        val rawId = frame.id or if (frame.extended) CAN_EFF_FLAG else 0

        ByteBuffer.wrap(buffer, 0, 4)
            .order(ByteOrder.nativeOrder())
            .putInt(rawId)

        buffer[4] = frame.data.size.toByte()
        if (frame.fd) {
            buffer[5] = if (frame.bitRateSwitch) CANFD_BRS.toByte() else 0
        }
        frame.data.copyInto(buffer, destinationOffset = 8)
        return buffer
    }
}
