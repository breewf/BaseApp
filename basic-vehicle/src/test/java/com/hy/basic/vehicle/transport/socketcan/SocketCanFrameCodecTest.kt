package com.hy.basic.vehicle.transport.socketcan

import com.hy.basic.vehicle.protocol.can.CanFrame
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** SocketCAN 帧编解码测试。 */
class SocketCanFrameCodecTest {

    /** 经典帧 ID、数据和时间戳不变 */
    @Test
    fun `经典帧往返`() {
        val frame = CanFrame.standard(0x18F, byteArrayOf(0x01, 0x02, 0x03))
        val encoded = SocketCanFrameCodec.encode(frame)
        val decoded = SocketCanFrameCodec.decode(encoded, SocketCanFrameCodec.CLASSIC_FRAME_SIZE)

        assertEquals(frame, decoded)
    }

    /** CAN FD 的 BRS 标志能保留 */
    @Test
    fun `CAN FD 帧往返`() {
        val frame = CanFrame(
            id = 0x18DAF110,
            data = ByteArray(12) { it.toByte() },
            extended = true,
            fd = true,
            bitRateSwitch = true,
        )
        val encoded = SocketCanFrameCodec.encode(frame)
        val decoded = SocketCanFrameCodec.decode(encoded, SocketCanFrameCodec.FD_FRAME_SIZE)

        assertTrue(decoded != null)
        assertEquals(frame.id, decoded!!.id)
        assertTrue(decoded.extended)
        assertTrue(decoded.fd)
        assertTrue(decoded.bitRateSwitch)
        assertArrayEquals(frame.data, decoded.data)
    }

    /** 非标准长度直接丢弃 */
    @Test
    fun `非法长度返回 null`() {
        assertEquals(null, SocketCanFrameCodec.decode(ByteArray(8), 8))
    }
}
