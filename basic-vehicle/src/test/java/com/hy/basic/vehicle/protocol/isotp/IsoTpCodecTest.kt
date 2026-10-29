package com.hy.basic.vehicle.protocol.isotp

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** ISO-TP 帧编解码测试，重点覆盖 CAN FD 的长度转义。 */
class IsoTpCodecTest {

    /** CAN FD 单帧超过 7 字节时使用转义长度 */
    @Test
    fun `CAN FD 单帧转义长度`() {
        val config = IsoTpConfig(requestId = 0x7E0, responseId = 0x7E8, fd = true)
        val payload = ByteArray(20) { it.toByte() }

        val frame = IsoTpCodec.encodeSingle(payload, config)
        val decoded = IsoTpCodec.decode(frame)

        assertEquals(0, frame.data[0].toInt())
        assertEquals(20, frame.data[1].toInt())
        assertTrue(decoded is IsoTpFrame.Single)
        assertArrayEquals(payload, (decoded as IsoTpFrame.Single).payload)
    }

    /** 经典 CAN 首帧+连续帧的总长度字段能解回 */
    @Test
    fun `经典多帧长度字段`() {
        val config = IsoTpConfig(requestId = 0x7E0, responseId = 0x7E8)
        val payload = ByteArray(20) { (it + 1).toByte() }

        val first = IsoTpCodec.encodeFirst(payload, config)
        val decoded = IsoTpCodec.decode(first)

        assertTrue(decoded is IsoTpFrame.First)
        assertEquals(20, (decoded as IsoTpFrame.First).totalLength)
        assertArrayEquals(payload.copyOfRange(0, 6), decoded.payload)
    }

    /** STmin 的毫秒和微秒编码 */
    @Test
    fun `STmin 编码`() {
        assertEquals(50_000L, IsoTpCodec.separationTimeUs(0x32))
        assertEquals(200L, IsoTpCodec.separationTimeUs(0xF2))
    }
}
