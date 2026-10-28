package com.hy.demo.network

import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * desc: 天气接口响应体 + 页面展示模型
 *
 * 字段全部声明为可空并有默认值:Gson 反序列化绕过构造函数,缺字段时不会赋值,
 * 非空类型会出现"看似非空实为 null"的坑,因此这里统一按可空处理。
 */

// ---------------- 接口响应模型 ----------------

data class GeoResponse(
    @SerializedName("results") val results: List<GeoCity?>? = null,
) {
    /** 检索无结果时返回 null,由业务给出提示 */
    fun firstCity(): GeoCity? = results?.firstOrNull()
}

data class GeoCity(
    @SerializedName("name") val name: String? = null,
    @SerializedName("latitude") val latitude: Double = 0.0,
    @SerializedName("longitude") val longitude: Double = 0.0,
    @SerializedName("admin1") val admin1: String? = null,
    @SerializedName("country") val country: String? = null,
)

data class WeatherResponse(
    @SerializedName("current") val current: CurrentWeather? = null,
    @SerializedName("daily") val daily: DailyWeather? = null,
)

data class CurrentWeather(
    @SerializedName("time") val time: String? = null,
    @SerializedName("temperature_2m") val temperature: Double? = null,
    @SerializedName("apparent_temperature") val apparentTemperature: Double? = null,
    @SerializedName("relative_humidity_2m") val humidity: Int? = null,
    @SerializedName("weather_code") val weatherCode: Int? = null,
    @SerializedName("wind_speed_10m") val windSpeed: Double? = null,
)

/** 逐日数据为"平行数组"结构,取值时按下标对齐 */
data class DailyWeather(
    @SerializedName("time") val dates: List<String?>? = null,
    @SerializedName("weather_code") val codes: List<Int?>? = null,
    @SerializedName("temperature_2m_max") val maxTemperatures: List<Double?>? = null,
    @SerializedName("temperature_2m_min") val minTemperatures: List<Double?>? = null,
) {
    val size: Int get() = dates?.size ?: 0

    fun dateAt(index: Int): String? = dates?.getOrNull(index)

    fun codeAt(index: Int): Int? = codes?.getOrNull(index)

    fun maxAt(index: Int): Double? = maxTemperatures?.getOrNull(index)

    fun minAt(index: Int): Double? = minTemperatures?.getOrNull(index)
}

// ---------------- 页面展示模型 ----------------

/** 页面只面向展示模型,不感知接口结构(换接口不影响 UI 代码) */
data class WeatherUiState(
    val city: String,
    val region: String,
    val icon: String,
    val description: String,
    val temperature: String,
    val detail: String,
    val updatedAt: String,
    val daily: List<DailyUi>,
)

data class DailyUi(
    val date: String,
    val weekday: String,
    val icon: String,
    val description: String,
    val range: String,
)

/** 接口响应 -> 展示模型 */
fun WeatherResponse.toUiState(city: String, region: String): WeatherUiState {
    val now = current
    val currentCode = now?.weatherCode
    val (icon, description) = currentCode.toWmo()
    val forecast = daily
    return WeatherUiState(
        city = city,
        region = region,
        icon = icon,
        description = description,
        temperature = now?.temperature.toCelsius(),
        detail = "体感 ${now?.apparentTemperature.toCelsius()} ｜ 湿度 ${
            now?.humidity?.let { "$it%" } ?: "--"
        } ｜ 风力 ${now?.windSpeed.toNumber(" km/h")}",
        updatedAt = "更新时间 ${now?.time.toDisplayTime()}",
        daily = if (forecast == null) emptyList() else (0 until forecast.size).map { index ->
            val (dayIcon, dayDescription) = forecast.codeAt(index).toWmo()
            DailyUi(
                date = forecast.dateAt(index).toShortDate(),
                weekday = forecast.dateAt(index).toWeekday(),
                icon = dayIcon,
                description = dayDescription,
                range = "${forecast.maxAt(index).toCelsius()} / ${forecast.minAt(index).toCelsius()}",
            )
        },
    )
}

// ---------------- 格式化工具 ----------------

private val WEEK_LABELS = arrayOf("周日", "周一", "周二", "周三", "周四", "周五", "周六")

/**
 * WMO 天气代码 -> (图标, 中文描述)
 * 上面已覆盖 Open-Meteo 支持的全部代码;换国内接口(和风/高德等各自编码体系)时,
 * 未命中会显示"未知(原始值)",便于直接定位要补哪些分支。
 */
private fun Int?.toWmo(): Pair<String, String> = when (this) {
    0 -> "☀️" to "晴"
    1 -> "🌤️" to "大部晴朗"
    2 -> "⛅" to "局部多云"
    3 -> "☁️" to "阴"
    45, 48 -> "🌫️" to "雾"
    51, 53, 55 -> "🌦️" to "毛毛雨"
    56, 57 -> "🌧️" to "冻毛毛雨"
    61, 63 -> "🌧️" to "降雨"
    65 -> "🌧️" to "大雨"
    66, 67 -> "🌧️" to "冻雨"
    71, 73 -> "❄️" to "降雪"
    75, 77 -> "❄️" to "大雪"
    80, 81, 82 -> "🌦️" to "阵雨"
    85, 86 -> "🌨️" to "阵雪"
    95 -> "⛈️" to "雷暴"
    96, 99 -> "⛈️" to "雷暴伴冰雹"
    else -> "🌡️" to (this?.let { "未知($it)" } ?: "未知")
}

private fun Double?.toCelsius(): String = this?.let { "${it.toNumber("°")}C" } ?: "--"

private fun Double?.toNumber(suffix: String): String =
    this?.let { String.format(Locale.getDefault(), "%.1f%s", it, suffix) } ?: "--"

/** "2026-10-13T15:30" -> "10-13 15:30" */
private fun String?.toDisplayTime(): String = this
    ?.replace("T", " ")
    ?.let { if (it.length >= 16) it.substring(5, 16) else it }
    ?: "--"

/** "2026-10-13" -> "10-13" */
private fun String?.toShortDate(): String =
    if (this != null && this.length >= 10) this.substring(5) else this ?: "--"

/** "2026-10-13" -> "周三"(第一天由页面另标"今天",此处只算星期) */
private fun String?.toWeekday(): String {
    if (isNullOrBlank()) return ""
    return try {
        val parsed = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(this) ?: return ""
        WEEK_LABELS[Calendar.getInstance().apply { time = parsed }.get(Calendar.DAY_OF_WEEK) - 1]
    } catch (e: java.text.ParseException) {
        ""
    }
}
