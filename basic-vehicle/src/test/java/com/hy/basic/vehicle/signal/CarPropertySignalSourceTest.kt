package com.hy.basic.vehicle.signal

import java.io.Closeable
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** CarProperty 信号源测试：非 Double 类型和写入转发。 */
class CarPropertySignalSourceTest {

    /** 属性回调能转成通用信号 */
    @Test
    fun `CarProperty 回调转信号`() = runBlocking {
        val client = FakeCarPropertyClient()
        val source = CarPropertySignalSource(
            client = client,
            bindings = listOf(CarPropertyBinding("car:gear", 0x100)),
        )

        client.push(CarPropertyReading("D", timestampMs = monotonicMs()))
        val value = source.observe("car:gear").first()

        assertEquals("D", value.value)
        assertEquals(SignalQuality.VALID, value.quality)
        source.close()
    }

    /** 写入转发给 CarPropertyClient */
    @Test
    fun `CarProperty 写入转发`() = runBlocking {
        val client = FakeCarPropertyClient()
        val source = CarPropertySignalSource(
            client = client,
            bindings = listOf(CarPropertyBinding("car:gear", 0x100)),
        )

        assertTrue(source.write("car:gear", "P"))
        assertEquals("P", client.lastWritten)
        source.close()
    }

    /** 测试用 CarProperty 适配器 */
    private class FakeCarPropertyClient : CarPropertyClient {

        /** 当前订阅回调 */
        private var callback: ((CarPropertyReading) -> Unit)? = null

        /** 最近写入值 */
        var lastWritten: Any? = null

        override fun subscribe(
            propertyId: Int,
            areaId: Int,
            callback: (CarPropertyReading) -> Unit,
        ): Closeable {
            this.callback = callback
            return Closeable { this.callback = null }
        }

        override fun get(propertyId: Int, areaId: Int): CarPropertyReading? = null

        override fun set(propertyId: Int, areaId: Int, value: Any): Boolean {
            lastWritten = value
            return true
        }

        /** 模拟属性回调 */
        fun push(reading: CarPropertyReading) {
            callback?.invoke(reading)
        }
    }
}
