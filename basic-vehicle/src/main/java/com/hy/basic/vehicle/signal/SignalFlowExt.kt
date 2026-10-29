@file:JvmName("VehicleSignalFlowExt")

package com.hy.basic.vehicle.signal

import java.util.concurrent.atomic.AtomicReference
import kotlin.math.abs
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

/**
 * 信号流加工：过滤无效值、按容差去重、超时置 STALE。
 *
 * 用法：
 * `flow.onlyValid()`、`flow.distinctByTolerance(0.1)`、`flow.withStaleTimeout(300)`。
 * 注意：超时只把 VALID 改成 STALE；容差只比较值和质量，不比较时间戳。
 */

/** 只保留质量可用的信号 */
fun <T> Flow<SignalValue<T>>.onlyValid(): Flow<SignalValue<T>> =
    filter { it.isValid }

/**
 * 数值变化小于 [tolerance] 时不上报。
 * 适合车速、温度这类带小抖动的信号;tolerance 为 0 时退化成严格去重。
 */
fun Flow<SignalValue<Double>>.distinctByTolerance(
    tolerance: Double = 0.0,
): Flow<SignalValue<Double>> {
    require(tolerance >= 0.0) { "tolerance 不能为负: $tolerance" }
    return distinctUntilChanged { old, new ->
        when {
            old.quality != new.quality -> false
            old.value == null || new.value == null -> old.value == new.value
            else -> abs(old.value - new.value) <= tolerance
        }
    }
}

/**
 * 超时未更新时补发一条 STALE。
 *
 * 源停止发送后,下游不会一直停在最后一个 VALID 值上;UI 可以据此显示"数据过期"。
 * [clock] 主要给单测替换时间源用,默认用单调时钟。
 */
fun <T> Flow<SignalValue<T>>.withStaleTimeout(
    timeoutMs: Long,
    clock: () -> Long = { monotonicMs() },
): Flow<SignalValue<T>> {
    require(timeoutMs > 0) { "timeoutMs 必须大于 0: $timeoutMs" }

    return channelFlow {
        val last = AtomicReference<SignalValue<T>?>(null)

        val watchdog = launch {
            while (isActive) {
                delay((timeoutMs / 2).coerceAtLeast(50L).milliseconds)
                val current = last.get() ?: continue
                if (current.quality == SignalQuality.VALID &&
                    clock() - current.timestampMs >= timeoutMs
                ) {
                    val stale = current.copy(quality = SignalQuality.STALE)
                    last.set(stale)
                    send(stale)
                }
            }
        }

        collect { value ->
            last.set(value)
            send(value)
        }
        watchdog.cancel()
    }
}
