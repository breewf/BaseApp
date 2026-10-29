package com.hy.basic.vehicle.signal

import com.hy.basic.vehicle.protocol.can.CanFrame
import com.hy.basic.vehicle.protocol.dbc.DbcParser
import com.hy.basic.vehicle.transport.CanTransport
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds

/** CAN 信号源与管理器联调测试。 */
class CanSignalSourceTest {

    /** 测试 DBC */
    private val dbc = """
        BO_ 256 Powertrain: 8 ECU
         SG_ Speed : 0|16@1+ (0.1,0) [0|250] "km/h" ECU
    """.trimIndent()

    /** 原始帧解码后能通过管理器订阅 */
    @Test
    fun `CAN 帧解码为通用信号`() = runBlocking {
        val transport = FakeCanTransport()
        val db = DbcParser.parse(dbc)
        val source = CanSignalSource.fromDatabase(transport = transport, database = db)
        val key = CanSignalKeys.of(0x100, "Speed")
        val spec = db.signal(0x100, "Speed")!!.spec
        val frame = CanFrame.standard(0x100, ByteArray(8))
            .write(spec, 60.5)
            .copy(timestampMs = monotonicMs())

        transport.receive(frame)
        val value = withTimeout(1_000.milliseconds) { source.observe(key).first() }

        assertEquals(60.5, value.value as Double, 0.001)
        assertEquals(SignalQuality.VALID, value.quality)
        source.close()
    }

    /** 管理器超时后补 STALE */
    @Test
    fun `管理器超时补 STALE`() = runBlocking {
        val transport = FakeCanTransport()
        val source = CanSignalSource.fromDatabase(transport = transport, database = DbcParser.parse(dbc))
        val manager = VehicleSignalManager()
        manager.register(source)
        val key = CanSignalKeys.of(0x100, "Speed")
        val spec = DbcParser.parse(dbc).signal(0x100, "Speed")!!.spec
        val frame = CanFrame.standard(0x100, ByteArray(8))
            .write(spec, 30.0)
            .copy(timestampMs = monotonicMs())

        transport.receive(frame)
        val valid = withTimeout(1_000.milliseconds) {
            manager.observe<Double>(key, staleTimeoutMs = 200).first()
        }
        assertTrue(valid.quality == SignalQuality.VALID)

        val stale = withTimeout(1_000.milliseconds) {
            manager.observe<Double>(key, staleTimeoutMs = 200)
                .first { it.quality == SignalQuality.STALE }
        }
        assertEquals(30.0, stale.value as Double, 0.001)
        manager.close()
    }

    /** 测试用 CAN 通道，保留最后一帧避免订阅时序影响 */
    private class FakeCanTransport : CanTransport {

        /** 收帧流，保留一帧供晚订阅者读取 */
        private val incoming = MutableSharedFlow<CanFrame>(replay = 1, extraBufferCapacity = 8)
        override val frames: SharedFlow<CanFrame> = incoming.asSharedFlow()

        override suspend fun send(frame: CanFrame) = Unit

        /** 喂一帧 */
        suspend fun receive(frame: CanFrame) {
            incoming.emit(frame)
        }

        override fun close() {}
    }
}
