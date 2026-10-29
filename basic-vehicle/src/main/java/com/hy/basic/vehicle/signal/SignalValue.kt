package com.hy.basic.vehicle.signal

/**
 * 通用信号值：值、质量、时间戳。
 *
 * 用法：
 * `SignalValue(60.0, SignalQuality.VALID, monotonicMs())`，`value.map { ... }` 做类型转换。
 * 注意：timestamp 用单调时钟，便于判断 STALE。
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
