package com.hy.basic.vehicle.protocol.uds

import java.util.ArrayDeque
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** UDS 客户端回归测试：用假传输固定请求和响应。 */
class UdsClientTest {

    /** 假传输，按队列返回响应 */
    private class FakeTransport(
        /** 预置响应队列 */
        private val responses: ArrayDeque<ByteArray?>,
    ) : UdsTransport {

        /** 最近一次请求的原始字节 */
        var lastPayload: ByteArray? = null

        /** 最近一次请求的超时 */
        var lastTimeoutMs: Long = 0L

        override suspend fun request(payload: ByteArray, timeoutMs: Long): ByteArray? {
            lastPayload = payload
            lastTimeoutMs = timeoutMs
            return responses.pollFirst()
        }
    }

    /** 按顺序构造响应队列,传 null 表示超时 */
    private fun queue(vararg responses: ByteArray?): ArrayDeque<ByteArray?> {
        val queue = ArrayDeque<ByteArray?>()
        responses.forEach { queue.addLast(it) }
        return queue
    }

    /** 10 服务请求字节正确 */
    @Test
    fun `会话切换请求字节正确`() = runBlocking {
        val transport = FakeTransport(queue(byteArrayOf(0x50, 0x03)))
        val client = UdsClient(transport)

        val result = client.sessionControl(0x03, timeoutMs = 500)

        assertTrue(result is UdsResult.Success)
        assertArrayEquals(byteArrayOf(0x10, 0x03), transport.lastPayload!!)
        assertEquals(500L, transport.lastTimeoutMs)
    }

    /** 否定响应拆出 NRC */
    @Test
    fun `否定响应拆出 NRC`() = runBlocking {
        val transport = FakeTransport(queue(byteArrayOf(0x7F, 0x22, 0x31)))
        val client = UdsClient(transport)

        val result = client.readDataByIdentifier(0xF190)

        assertTrue(result is UdsResult.Negative)
        val negative = result as UdsResult.Negative
        assertEquals(0x22, negative.serviceId)
        assertEquals(0x31, negative.nrc)
        assertArrayEquals(byteArrayOf(0x22, 0xF1.toByte(), 0x90.toByte()), transport.lastPayload!!)
    }

    /** 超时返回 Timeout */
    @Test
    fun `超时返回 Timeout`() = runBlocking {
        val transport = FakeTransport(queue(null))
        val client = UdsClient(transport)

        val result = client.testerPresent()

        assertTrue(result is UdsResult.Timeout)
    }

    /** 安全访问用 seed 算 key */
    @Test
    fun `安全访问用 seed 算 key`() = runBlocking {
        val transport = FakeTransport(
            queue(
                byteArrayOf(0x67, 0x01, 0x12, 0x34),
                byteArrayOf(0x67, 0x02),
            )
        )
        val client = UdsClient(transport)

        val result = client.securityAccess(0x01) { seed ->
            assertArrayEquals(byteArrayOf(0x12, 0x34), seed)
            byteArrayOf(0x56, 0x78)
        }

        assertTrue(result is UdsResult.Success)
        assertArrayEquals(
            byteArrayOf(0x27, 0x02, 0x56, 0x78),
            transport.lastPayload!!,
        )
    }

    /** 解析 19 02 响应 */
    @Test
    fun `DTC 响应解析`() {
        val response = byteArrayOf(
            0x59,
            0x02,
            0xFF.toByte(),
            0x03,
            0x01,
            0x00,
            UdsDtcStatus.CONFIRMED.toByte(),
        )

        val dtc = parseDtcByStatusMask(response)

        assertEquals(1, dtc.size)
        assertEquals("P0301", dtc.first().codeText())
        assertEquals(UdsDtcStatus.CONFIRMED, dtc.first().status)
    }
}
