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
         SG_ Gear : 16|4@1+ (1,0) [0|15] "" ECU
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

    /** DBC 多路复用只解当前选择器分支 */
    @Test
    fun `DBC 多路复用只解当前分支`() = runBlocking {
        val muxDbc = """
            BO_ 512 MuxMessage: 8 ECU
             SG_ Mux M : 0|4@1+ (1,0) [0|15] "" ECU
             SG_ MuxA m0 : 8|8@1+ (1,0) [0|255] "" ECU
             SG_ MuxB m1 : 8|8@1+ (1,0) [0|255] "" ECU
        """.trimIndent()
        val db = DbcParser.parse(muxDbc)
        val transport = FakeCanTransport()
        val source = CanSignalSource.fromDatabase(transport = transport, database = db)
        val mux = db.signal(0x200, "Mux")!!.spec
        val muxA = db.signal(0x200, "MuxA")!!.spec
        val muxB = db.signal(0x200, "MuxB")!!.spec

        val frameA = CanFrame.standard(0x200, ByteArray(8))
            .write(mux, 0.0)
            .write(muxA, 42.0)
            .copy(timestampMs = monotonicMs())
        transport.receive(frameA)
        val valueA = withTimeout(1_000.milliseconds) {
            source.observe("can:0x200:MuxA").first()
        }

        assertEquals(42.0, valueA.value as Double, 0.001)
        assertTrue(source.latest("can:0x200:MuxB") == null)

        val frameB = CanFrame.standard(0x200, ByteArray(8))
            .write(mux, 1.0)
            .write(muxB, 99.0)
            .copy(timestampMs = monotonicMs())
        transport.receive(frameB)
        val valueB = withTimeout(1_000.milliseconds) {
            source.observe("can:0x200:MuxB").first()
        }

        assertEquals(99.0, valueB.value as Double, 0.001)
        assertEquals(SignalQuality.INVALID, source.latest("can:0x200:MuxA")?.quality)
        source.close()
    }

    /** 连续写同一报文时要保留前一个信号的值 */
    @Test
    fun `连续写同一报文保留其它信号`() = runBlocking {
        val transport = FakeCanTransport()
        val source = CanSignalSource.fromDatabase(
            transport = transport,
            database = DbcParser.parse(dbc),
        )

        source.write(CanSignalKeys.of(0x100, "Speed"), 60.5)
        source.write(CanSignalKeys.of(0x100, "Gear"), 3.0)

        val sent = transport.sent.last()
        assertEquals(60.5, sent.decode(DbcParser.parse(dbc).signal(0x100, "Speed")!!.spec).physical, 0.001)
        assertEquals(3.0, sent.decode(DbcParser.parse(dbc).signal(0x100, "Gear")!!.spec).physical, 0.001)
        source.close()
    }

    /** 测试用 CAN 通道，保留最后一帧避免订阅时序影响 */
    private class FakeCanTransport : CanTransport {

        /** 收帧流，保留一帧供晚订阅者读取 */
        private val incoming = MutableSharedFlow<CanFrame>(replay = 1, extraBufferCapacity = 8)
        override val frames: SharedFlow<CanFrame> = incoming.asSharedFlow()

        /** 已发送帧 */
        val sent = mutableListOf<CanFrame>()

        override suspend fun send(frame: CanFrame) {
            sent += frame
        }

        /** 喂一帧 */
        suspend fun receive(frame: CanFrame) {
            incoming.emit(frame)
        }

        override fun close() {}
    }
}
