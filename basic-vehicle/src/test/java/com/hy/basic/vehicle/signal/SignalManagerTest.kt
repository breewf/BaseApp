package com.hy.basic.vehicle.signal

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds

/**
 * desc: 信号管理回归测试。
 *
 * 覆盖多源合并、最近值、写入和超时置 STALE,全部用仿真源,不依赖车机系统。
 */
class SignalManagerTest {

    @Test
    fun `同名数据源只注册一个`() {
        val manager = VehicleSignalManager()

        assertTrue(manager.register(ManualSignalSource("can")))
        assertTrue(!manager.register(ManualSignalSource("can")))
        assertEquals(1, manager.sources().size)
    }

    @Test
    fun `手动源写入后能订阅到`() = runBlocking {
        val source = ManualSignalSource("sim")
        val manager = VehicleSignalManager()
        manager.register(source)

        source.set("speed", 60.0)
        val value = withTimeout(1_000.milliseconds) { manager.observe("speed").first() }

        assertEquals(60.0, value.value)
        assertTrue(value.isValid)
    }

    @Test
    fun `超时未更新会补 STALE`() = runBlocking {
        val source = ManualSignalSource("sim")
        val manager = VehicleSignalManager()
        manager.register(source)
        source.set("speed", 60.0)

        val stale = withTimeout(2_000.milliseconds) {
            manager.observe("speed", staleTimeoutMs = 100)
                .first { it.quality == SignalQuality.STALE }
        }

        assertEquals(60.0, stale.value)
    }

    @Test
    fun `小抖动按容差去重`() = runBlocking {
        val values = flowOf(
            SignalValue(60.0, SignalQuality.VALID, 0L),
            SignalValue(60.04, SignalQuality.VALID, 1L),
            SignalValue(61.0, SignalQuality.VALID, 2L),
        ).distinctByTolerance(0.1).toList()

        assertEquals(2, values.size)
        assertEquals(60.0, values[0].value)
        assertEquals(61.0, values[1].value)
    }
}
