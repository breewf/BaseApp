package com.hy.basic.vehicle.protocol.can

import com.hy.basic.vehicle.signal.SignalQuality
import java.util.Locale
import kotlin.math.roundToLong

/**
 * desc: 一条信号解析后的结果。
 *
 * raw 保留原始码值,方便对照报文;physical 是乘过 factor / 加过 offset 的物理值。
 * 只有 [quality] 为 VALID 时才建议上屏,其余情况统一显示 "--"。
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
    /** 解码时间戳(毫秒) */
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

    /** 上屏文本;无效值不要显示 0,统一 "--" */
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
