package com.hy.basic.vehicle.signal

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.update

/**
 * desc: 内存信号源,用于仿真、单测和没有真实车辆信号的场景。
 *
 * 写进去的值会保存在 StateFlow 里,新订阅者能立即拿到最近一次值。
 * 真实接车时用 CAN / 广播 / CarProperty 各自的 Source 替换即可。
 */
class ManualSignalSource(
    override val name: String = "manual",
) : SignalSource {

    private val values = MutableStateFlow<Map<String, SignalValue<Double>>>(emptyMap())

    override fun observe(key: String): Flow<SignalValue<Double>> =
        values.mapNotNull { it[key] }.distinctUntilChanged()

    override fun latest(key: String): SignalValue<Double>? = values.value[key]

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
