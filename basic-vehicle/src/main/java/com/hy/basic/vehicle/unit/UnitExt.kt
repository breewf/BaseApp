@file:JvmName("VehicleUnitExt")

package com.hy.basic.vehicle.unit

import java.util.Locale

/**
 * 车机信号常用量纲换算。
 *
 * 用法：
 * `605.tenthKphToKph()`、`240.0.kpaToBar()`、`35.0.celsiusToFahrenheit()`。
 * 注意：factor/offset 以协议为准，这里只做量纲换算。
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
