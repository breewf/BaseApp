package com.hy.myapp.demo.network

import com.google.gson.Gson
import com.hy.basic.network.Api
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlinx.coroutines.delay

/**
 * desc: 天气数据源抽象层
 *
 * 为什么要这一层:ViewModel 只依赖 [WeatherRepository] 接口,
 * 于是"真实接口""本地 Mock""缓存优先""换域名/换供应商"都是实现细节,
 * 页面代码、loading/异常通道、数据解析链路全都不用动 —— 这也是后续加 Repository + 缓存的标准姿势。
 *
 * 两个实现返回完全相同的 [GeoResponse] / [WeatherResponse],
 * 因此 Mock 模式下依然会跑一遍 Gson 反序列化 + toUiState 映射,流程与线上一致。
 */
interface WeatherRepository {

    /** 城市名 -> 候选城市列表(裸 JSON,无 code/msg/data 信封) */
    suspend fun searchCity(name: String): GeoResponse

    /** 经纬度 -> 当前天气 + 逐日预报 */
    suspend fun forecast(latitude: Double, longitude: Double): WeatherResponse

    /** 异常兜底验证:线上返回 4xx,本地抛出 IO 异常,最终都被归一化为 ApiException */
    suspend fun brokenEndpoint(): WeatherResponse
}

/** 按 [WeatherApi.USE_MOCK_DATA] 决定用哪套数据源,调用方无需感知 */
fun createWeatherRepository(): WeatherRepository =
    if (WeatherApi.USE_MOCK_DATA) MockWeatherRepository() else RemoteWeatherRepository()

// ---------------------------------------------------------------- 线上实现

/**
 * 真实请求:两个域名各自一个 Retrofit,共用 [com.hy.basic.network.Api.okHttpClient]。
 * lazy 保证首次请求时才创建,进程内只创建一次。
 */
class RemoteWeatherRepository : WeatherRepository {

    private val geoService by lazy { ApiCreator.geo }
    private val weatherService by lazy { ApiCreator.weather }

    override suspend fun searchCity(name: String) = geoService.searchCity(name)

    override suspend fun forecast(latitude: Double, longitude: Double) =
        weatherService.forecast(latitude, longitude)

    override suspend fun brokenEndpoint() = weatherService.brokenEndpoint()
}

/** 接口实例的统一持有处(多域名共用一份 OkHttp,避免重复创建连接池) */
private object ApiCreator {

    val geo: GeoService by lazy { Api.create<GeoService>(WeatherApi.GEO_BASE_URL) }

    val weather: WeatherService by lazy {
        Api.create<WeatherService>(WeatherApi.FORECAST_BASE_URL)
    }
}

// ---------------------------------------------------------------- 本地 Mock 实现

/**
 * 离线数据源:写死城市表 + 动态生成"今天起 7 天"的 JSON,再交给 Gson 解析。
 *
 * - [delay] 模拟网络耗时,用来观察 loading 引用计数(两个串行请求期间 loading 不会提前消失)
 * - 用经纬度派生稳定偏移量,让不同城市的数据不同,便于确认 UI 真的在刷新
 * - [brokenEndpoint] 抛 [IOException],验证异常归一化 + 全局提示链路
 */
class MockWeatherRepository : WeatherRepository {

    private val gson = Gson()

    override suspend fun searchCity(name: String): GeoResponse {
        delay(MOCK_DELAY_MS)
        val keyword = name.trim()
        // 支持"郑州市"这类带后缀的输入;匹配不到返回空列表,由 ViewModel 走"未找到城市"业务分支
        val city = MOCK_CITIES.entries
            .firstOrNull { (key, _) -> key == keyword || keyword.startsWith(key) || keyword.endsWith(key) }
            ?.value
        return GeoResponse(results = if (city == null) emptyList() else listOf(city))
    }

    override suspend fun forecast(latitude: Double, longitude: Double): WeatherResponse {
        delay(MOCK_DELAY_MS)
        return gson.fromJson(mockJson(latitude, longitude), WeatherResponse::class.java)
    }

    /**
     * 想验证不同异常分支,把下面这行换成:
     * - `throw SocketTimeoutException()` -> "请求超时,请稍后再试"
     * - `throw UnknownHostException()`  -> "网络连接失败,请稍后再试"
     * - `throw JsonParseException()`     -> "数据解析错误,请稍后再试"
     */
    override suspend fun brokenEndpoint(): WeatherResponse =
        throw IOException("模拟服务端返回 404 Not Found")

    /** 生成与 Open-Meteo 结构一致的响应文本(日期基于当天推导,所以预报永远是"未来几天") */
    private fun mockJson(latitude: Double, longitude: Double): String {
        val offset = kotlin.math.abs((latitude + longitude).toInt()) % 5
        val dayFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timeFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault())
        val now = Calendar.getInstance()
        val days = (0 until MOCK_FORECAST_DAYS).map { index ->
            val calendar = Calendar.getInstance().apply {
                time = now.time
                add(Calendar.DAY_OF_MONTH, index)
            }
            dayFormat.format(calendar.time)
        }
        // WMO 代码是离散枚举(0/1/2/3/45/51/61/80/95…),没有 4/64/98 这类值,
        // 因此不能对代码做加法偏移,只能按 offset 在合法列表内轮转取值
        val codes = days.indices.map { index ->
            MOCK_WEATHER_CODES[(index + offset) % MOCK_WEATHER_CODES.size]
        }
        val maxTemperatures = days.indices.map { 22 + offset + it % 3 }
        val minTemperatures = days.indices.map { 13 + offset + it % 3 }
        return """
            {
              "current": {
                "time": "${timeFormat.format(now.time)}",
                "temperature_2m": ${24 + offset}.5,
                "apparent_temperature": ${25 + offset}.2,
                "relative_humidity_2m": ${50 + offset * 3},
                "weather_code": ${codes.first()},
                "wind_speed_10m": 8.5
              },
              "daily": {
                "time": ${days.toQuoteJsonArray()},
                "weather_code": ${codes.toJsonArray()},
                "temperature_2m_max": ${maxTemperatures.toJsonArray()},
                "temperature_2m_min": ${minTemperatures.toJsonArray()}
              }
            }
        """.trimIndent()
    }

    private fun List<Number>.toJsonArray() = joinToString(prefix = "[", postfix = "]")

    private fun List<String>.toQuoteJsonArray() = joinToString(prefix = "[", postfix = "]") { "\"$it\"" }

    private companion object {

        /** 模拟网络耗时(ms),方便看到 loading 转圈 */
        const val MOCK_DELAY_MS = 700L

        const val MOCK_FORECAST_DAYS = 7

        /** 逐日天气代码(均为合法 WMO 值):晴 → 大部晴朗 → 局部多云 → 阴 → 降雨 → 阵雨 → 雷暴 */
        val MOCK_WEATHER_CODES = listOf(0, 1, 2, 3, 61, 80, 95)

        /** 写死的城市表(默认郑州);不在表内的城市会走"未找到城市"分支 */
        val MOCK_CITIES: Map<String, GeoCity> = linkedMapOf(
            "郑州" to GeoCity("郑州", 34.7572, 113.6204, "河南省", "中国"),
            "北京" to GeoCity("北京", 39.9042, 116.4074, "北京市", "中国"),
            "上海" to GeoCity("上海", 31.2304, 121.4737, "上海市", "中国"),
            "西安" to GeoCity("西安", 34.3416, 108.9398, "陕西省", "中国"),
            "洛阳" to GeoCity("洛阳", 34.6197, 112.4540, "河南省", "中国"),
        )
    }
}
