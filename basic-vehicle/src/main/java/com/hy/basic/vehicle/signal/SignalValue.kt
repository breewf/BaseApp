package com.hy.basic.vehicle.signal

/**
 * desc: 通用信号值。
 *
 * CAN、厂商广播、仿真源最后都落到这个模型:值 + 质量 + 时间戳。
 * 上层只认它,不关心数据从哪来。
 */
data class SignalValue<T>(
    /** 解码后的值,无效时为 null */
    val value: T?,
    /** 数据质量 */
    val quality: SignalQuality,
    /** 采样时间(毫秒,单调时钟) */
    val timestampMs: Long,
    /** 原始码值,没有就为 null */
    val raw: Long? = null,
) {

    /** 是否可以直接使用 */
    val isValid: Boolean get() = quality.isUsable

    /** 值转换,保留质量和时间戳 */
    fun <R> map(transform: (T) -> R): SignalValue<R> =
        SignalValue(value?.let(transform), quality, timestampMs, raw)
}

/** 单调时钟毫秒,避免系统校时导致超时判断错乱 */
fun monotonicMs(): Long = System.nanoTime() / 1_000_000L
