package com.hy.basic.vehicle.protocol.can

import com.hy.basic.vehicle.signal.SignalQuality
import com.hy.basic.vehicle.signal.SignalValue
import java.util.Locale
import kotlin.math.roundToLong

/**
 * 信号解码结果：raw、物理值、质量、时间戳。
 *
 * 用法：
 * `value.display()` 生成上屏文本，`value.toSignalValue()` 转给 VehicleSignalManager。
 * 注意：quality 不是 VALID 时 UI 显示 "--"。
 */
data class CanSignalValue(
    /** 对应的信号定义 */
    val spec: CanSignalSpec,
    /** 报文里的原始码值 */
    val raw: Long,
    /** 乘过 factor、加过 offset 的物理值 */
    val physical: Double,
    /** 信号质量,决定是否能上屏 */
    val quality: SignalQuality,
    /** 解码时间戳(毫秒)，用单调时钟 */
    val timestampMs: Long = 0L,
) {

    /** 信号名 */
    val name: String get() = spec.name

    /** 物理单位 */
    val unit: String get() = spec.unit

    /** 是否可以直接展示 */
    val isValid: Boolean get() = quality.isUsable

    /** 枚举信号的标签,比如档位 3 -> "D" */
    val label: String? get() = spec.labelOf(raw)

    /** 上屏文本；无效值显示 "--" */
    fun display(decimals: Int = 1): String {
        if (!isValid) return "$name: --"

        label?.let { return "$name: $it" }

        val number = if (decimals <= 0) {
            physical.roundToLong().toString()
        } else {
            String.format(Locale.US, "%.${decimals}f", physical)
        }
        val suffix = if (unit.isBlank()) "" else " $unit"
        return "$name: $number$suffix"
    }
}

/** 转成通用信号模型 */
fun CanSignalValue.toSignalValue(): SignalValue<Double> =
    SignalValue(
        value = if (isValid) physical else null,
        quality = quality,
        timestampMs = timestampMs,
        raw = raw,
    )
