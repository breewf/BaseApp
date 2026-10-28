@file:JvmName("VehicleUnitExt")

package com.hy.basic.vehicle.unit

import java.util.Locale

/**
 * desc: 车机信号常见量纲换算。
 *
 * 车机报文里大量用整数 + 精度传值,比如车速 605 表示 60.5 km/h、温度 350 表示 35.0℃。
 * 这些换算集中在这里,业务里就不要到处写 *0.1、/1000 这种魔法数字了。
 *
 * 注意:
 * - 精度和 offset 最终以 DBC/协议文档为准,这里的函数只负责量纲换算;
 * - 胎压有的车厂用 kPa、有的用 bar、有的用 psi,展示前确认单位,别混着用;
 * - 角度归一化统一到 [0, 360),需要 -180..180 的场合再单独转。
 */

// ==== 车速 ====

/** km/h 转 m/s */
fun Double.kphToMps(): Double = this / 3.6

/** m/s 转 km/h */
fun Double.mpsToKph(): Double = this * 3.6

/** km/h 转 mph */
fun Double.kphToMph(): Double = this / 1.609344

/** mph 转 km/h */
fun Double.mphToKph(): Double = this * 1.609344

/** 0.1 km/h 精度的原始值转 km/h */
fun Int.tenthKphToKph(): Double = this / 10.0

// ==== 温度 ====

/** 0.1℃ 精度的原始值转摄氏度 */
fun Int.tenthCelsiusToCelsius(): Double = this / 10.0

/** 摄氏度转华氏度 */
fun Double.celsiusToFahrenheit(): Double = this * 9.0 / 5.0 + 32.0

/** 华氏度转摄氏度 */
fun Double.fahrenheitToCelsius(): Double = (this - 32.0) * 5.0 / 9.0

// ==== 压力 ====

/** kPa 转 bar */
fun Double.kpaToBar(): Double = this / 100.0

/** bar 转 kPa */
fun Double.barToKpa(): Double = this * 100.0

/** kPa 转 psi */
fun Double.kpaToPsi(): Double = this / 6.894757

/** psi 转 kPa */
fun Double.psiToKpa(): Double = this * 6.894757

// ==== 电量 / 电压 ====

/** Wh 转 kWh */
fun Int.whToKwh(): Double = this / 1000.0

/** 0.1% 精度的原始值转百分比 */
fun Int.tenthPercentToPercent(): Double = this / 10.0

/** 毫伏转伏 */
fun Int.millivoltToVolt(): Double = this / 1000.0

// ==== 距离 / 角度 ====

/** 米转公里 */
fun Double.metersToKm(): Double = this / 1000.0

/** 公里转米 */
fun Double.kmToMeters(): Double = this * 1000.0

/** 角度归一化到 [0, 360) */
fun Double.normalizeDegrees(): Double {
    var value = this % 360.0
    if (value < 0) value += 360.0
    return value
}

/** 角度归一化到 [-180, 180),适合展示车头朝向差值 */
fun Double.toSignedDegrees(): Double {
    val value = normalizeDegrees()
    return if (value >= 180.0) value - 360.0 else value
}

/** 数值 + 单位的上屏格式,固定用 Locale.US 避免小数点变成逗号 */
fun Double.formatWithUnit(unit: String = "", decimals: Int = 1): String {
    val text = String.format(Locale.US, "%.${decimals}f", this)
    return if (unit.isBlank()) text else "$text $unit"
}
