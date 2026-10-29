package com.hy.basic.vehicle.protocol.isotp

import com.hy.basic.vehicle.protocol.can.CanFrame
import com.hy.basic.vehicle.protocol.uds.IsoTpUdsTransport
import com.hy.basic.vehicle.protocol.uds.UdsClient
import com.hy.basic.vehicle.protocol.uds.UdsResult
import com.hy.basic.vehicle.transport.CanTransport
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** ISO-TP 回归测试：单帧、多帧、流控和 UDS 0x78。 */
class IsoTpChannelTest {

    /** 请求 ID */
    private val requestId = 0x7E0

    /** 响应 ID */
    private val responseId = 0x7E8

    /** 单帧请求能拿到单帧响应 */
    @Test
    fun `单帧请求响应`() = runBlocking {
        val transport = FakeCanTransport()
        val channel = IsoTpChannel(
            transport = transport,
            config = IsoTpConfig(requestId, responseId),
        )
        transport.onSend = { frame ->
            if (frame.id == requestId &&
                (frame.data[0].toInt() ushr 4) == 0
            ) {
                transport.receive(responseFrame(byteArrayOf(0x62, 0xF1.toByte(), 0x90.toByte())))
            }
        }

        val response = channel.request(byteArrayOf(0x22, 0xF1.toByte(), 0x90.toByte()), 1_000)

        assertTrue(response != null)
        assertArrayEquals(byteArrayOf(0x62, 0xF1.toByte(), 0x90.toByte()), response!!)
        channel.close()
    }

    /** 响应多帧能按首帧+连续帧重组 */
    @Test
    fun `响应多帧重组`() = runBlocking {
        val transport = FakeCanTransport()
        val channel = IsoTpChannel(
            transport = transport,
            config = IsoTpConfig(requestId, responseId),
        )
        val payload = ByteArray(20) { (it + 1).toByte() }
        transport.onSend = { frame ->
            if (frame.id == requestId &&
                (frame.data[0].toInt() ushr 4) == 0
            ) {
                transport.receive(CanFrame(responseId, firstFrame(payload)))
                transport.receive(CanFrame(responseId, consecutiveFrame(1, payload, 6)))
                transport.receive(CanFrame(responseId, consecutiveFrame(2, payload, 13)))
            }
        }

        val response = channel.request(byteArrayOf(0x22), 1_000)

        assertTrue(response != null)
        assertArrayEquals(payload, response!!)
        assertTrue(transport.sent.any {
            it.id == requestId && (it.data[0].toInt() ushr 4) == 3
        })
        channel.close()
    }

    /** 多帧请求会先等流控，再按块发连续帧 */
    @Test
    fun `多帧请求等待流控`() = runBlocking {
        val transport = FakeCanTransport()
        val channel = IsoTpChannel(
            transport = transport,
            config = IsoTpConfig(requestId, responseId),
        )
        val payload = ByteArray(20) { (it + 1).toByte() }
        var sentConsecutive = 0

        transport.onSend = { frame ->
            when (frame.data[0].toInt() ushr 4) {
                1 -> transport.receive(
                    CanFrame(responseId, byteArrayOf(0x30, 0, 0, 0, 0, 0, 0, 0))
                )

                2 -> {
                    sentConsecutive++
                    if (sentConsecutive == 2) {
                        transport.receive(responseFrame(byteArrayOf(0x62, 0x01)))
                    }
                }
            }
        }

        val response = channel.request(payload, 1_000)

        assertTrue(response != null)
        assertArrayEquals(byteArrayOf(0x62, 0x01), response!!)
        assertEquals(2, sentConsecutive)
        channel.close()
    }

    /** 0x78 后继续等最终 UDS 响应 */
    @Test
    fun `UDS 响应挂起后继续接收`() = runBlocking {
        val transport = FakeCanTransport()
        val channel = IsoTpChannel(
            transport = transport,
            config = IsoTpConfig(requestId, responseId),
        )
        transport.onSend = { frame ->
            if (frame.id == requestId &&
                (frame.data[0].toInt() ushr 4) == 0
            ) {
                transport.receive(responseFrame(byteArrayOf(0x7F, 0x22, 0x78)))
                transport.receive(responseFrame(byteArrayOf(0x62, 0xF1.toByte(), 0x90.toByte(), 0x31)))
            }
        }
        val client = UdsClient(IsoTpUdsTransport(channel))

        val result = client.readDataByIdentifier(0xF190)

        assertTrue(result is UdsResult.Success)
        assertEquals(0x62, (result as UdsResult.Success).response[0].toInt() and 0xFF)
        channel.close()
    }

    /** 请求帧的 PCI 是单帧，响应帧同理 */
    private fun responseFrame(payload: ByteArray): CanFrame {
        val data = ByteArray(8)
        data[0] = payload.size.toByte()
        payload.copyInto(data, 1)
        return CanFrame(responseId, data)
    }

    /** 构造经典 CAN 首帧 */
    private fun firstFrame(payload: ByteArray): ByteArray {
        val data = ByteArray(8)
        data[0] = (0x10 or (payload.size ushr 8)).toByte()
        data[1] = payload.size.toByte()
        payload.copyInto(data, 2, 0, 6)
        return data
    }

    /** 构造经典 CAN 连续帧 */
    private fun consecutiveFrame(sequence: Int, payload: ByteArray, offset: Int): ByteArray {
        val data = ByteArray(8)
        data[0] = (0x20 or sequence).toByte()
        payload.copyInto(data, 1, offset, minOf(offset + 7, payload.size))
        return data
    }

    /** 测试用 CAN 通道 */
    private class FakeCanTransport : CanTransport {

        /** 测试注入的发送响应 */
        var onSend: (suspend (CanFrame) -> Unit)? = null

        /** 收帧流 */
        private val incoming = MutableSharedFlow<CanFrame>(extraBufferCapacity = 32)
        override val frames: SharedFlow<CanFrame> = incoming.asSharedFlow()

        /** 记录发出的帧 */
        val sent = mutableListOf<CanFrame>()

        override suspend fun send(frame: CanFrame) {
            sent += frame
            yield()
            onSend?.invoke(frame)
        }

        /** 喂一帧响应 */
        suspend fun receive(frame: CanFrame) {
            incoming.emit(frame)
        }

        override fun close() {}
    }
}
