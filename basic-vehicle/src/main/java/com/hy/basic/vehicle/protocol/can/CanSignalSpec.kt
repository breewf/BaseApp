package com.hy.basic.vehicle.protocol.can

/**
 * CAN 信号定义，字段和 DBC 的 SIGNAL_* 对应。
 *
 * 用法：
 * `CanSignalSpec(name = "Speed", startBit = 0, length = 16, factor = 0.1, unit = "km/h")`。
 * 注意：invalidRaw 用于 0xFF/0xFFFF 这类无效值；min/max 是物理值范围。
 */
data class CanSignalSpec(
    /** 信号名,和 DBC 保持一致 */
    val name: String,
    /** 起始位,Intel 是最低位 / Motorola 是最高位 */
    val startBit: Int,
    /** 信号占用位数,1..64 */
    val length: Int,
    /** 字节序,默认 Intel */
    val byteOrder: CanByteOrder = CanByteOrder.INTEL,
    /** 是否有符号 */
    val signed: Boolean = false,
    /** 物理值 = raw * factor + offset */
    val factor: Double = 1.0,
    /** 物理值偏移,常见如温度 -40 */
    val offset: Double = 0.0,
    /** 物理单位,用于展示 */
    val unit: String = "",
    /** 物理值下限,不填不校验 */
    val min: Double = Double.NEGATIVE_INFINITY,
    /** 物理值上限,不填不校验 */
    val max: Double = Double.POSITIVE_INFINITY,
    /** 无效哨兵 raw 值,如 0xFFFF */
    val invalidRaw: Long? = null,
    /** 枚举值标签,如档位 3 -> D */
    val valueLabels: Map<Long, String> = emptyMap(),
) {

    init {
        require(name.isNotBlank()) { "信号名不能为空" }
        require(startBit >= 0) { "startBit 不能为负: $startBit" }
        require(length in 1..64) { "信号长度需在 1..64: $length" }
        require(factor != 0.0) { "factor 不能为 0,否则无法换算物理值" }
        require(min <= max) { "min($min) 不能大于 max($max)" }
    }

    /** Motorola 在解析时按大端走位 */
    val motorola: Boolean get() = byteOrder == CanByteOrder.MOTOROLA

    /** 枚举信号的展示文本,没有配置时返回 null */
    fun labelOf(raw: Long): String? = valueLabels[raw]
}
