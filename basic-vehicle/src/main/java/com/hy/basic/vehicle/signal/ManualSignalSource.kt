package com.hy.basic.vehicle.signal

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.update

/**
 * 内存信号源，用于仿真和单测。
 *
 * 用法：
 * `val source = ManualSignalSource("sim")`，`source.set("speed", 60.0)` 后注册到管理器。
 * 注意：新订阅者会立即收到最近一次值；真实接车时替换成 CAN/广播 Source。
 */
class ManualSignalSource(
    /** 数据源名 */
    override val name: String = "manual",
) : SignalSource {

    /** 当前信号值 */
    private val values = MutableStateFlow<Map<String, SignalValue<Double>>>(emptyMap())

    /** 订阅 key 对应的值 */
    override fun observe(key: String): Flow<SignalValue<Double>> =
        values.mapNotNull { it[key] }.distinctUntilChanged()

    /** 最近一次值 */
    override fun latest(key: String): SignalValue<Double>? = values.value[key]

    /** 写入有效值 */
    override fun write(key: String, value: Double): Boolean {
        set(key, value)
        return true
    }

    /** 写入有效值,时间戳取当前单调时钟 */
    fun set(key: String, value: Double, raw: Long? = null) {
        values.update { current ->
            current + (key to SignalValue(value, SignalQuality.VALID, monotonicMs(), raw))
        }
    }

    /** 标记为无效,UI 应显示 "--" */
    fun setInvalid(key: String) {
        values.update { current ->
            current + (key to SignalValue(null, SignalQuality.INVALID, monotonicMs()))
        }
    }

    /** 移除一个信号 */
    fun remove(key: String) {
        values.update { it - key }
    }

    /** 清空所有仿真值 */
    fun clear() {
        values.value = emptyMap()
    }
}
