package com.hy.myapp.demo.network

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * desc: 网络测试页的接口地址与数据源开关配置(所有"换接口"的事都只改这里)
 *
 * 线上模式刻意使用两个不同域名,用于验证 [com.hy.basic.network.Api.create] 的多域名能力:
 * 两个接口实例共用同一份 OkHttpClient(连接池/线程池只一份)。
 */
object WeatherApi {

    /**
     * 数据源开关:
     * - true:[MockWeatherRepository] 本地造数据(不依赖外网,依然走完整的 loading / 反序列化 / 解析 / 上屏流程)
     * - false:[RemoteWeatherRepository] 真实请求下面两个域名
     *
     * 换成国内接口只需三步,ViewModel 与 Activity 一行都不用改:
     * 1. 把本开关改为 false
     * 2. 改 [GEO_BASE_URL] / [FORECAST_BASE_URL] 为国内域名(如和风天气 https://devapi.qweather.com/)
     * 3. 按新接口文档调整 [GeoService] / [WeatherService] 的路径、参数与返回 bean
     *    (若新接口带 code/msg/data 信封,把 ViewModel 里的 launchRawRequest 换成 launchRequest 即可)
     */
    const val USE_MOCK_DATA = true

    /** 城市检索域名(Open-Meteo:免费、无需注册与 key) */
    const val GEO_BASE_URL = "https://geocoding-api.open-meteo.com/"

    /** 天气数据域名 */
    const val FORECAST_BASE_URL = "https://api.open-meteo.com/"
}

/** 城市名 -> 经纬度 */
interface GeoService {

    @GET("v1/search")
    suspend fun searchCity(
        @Query("name") name: String,
        @Query("count") count: Int = 1,
        @Query("language") language: String = "zh",
    ): GeoResponse
}

/** 当前天气 + 逐日预报 */
interface WeatherService {

    @GET("v1/forecast")
    suspend fun forecast(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String =
            "temperature_2m,relative_humidity_2m,apparent_temperature,weather_code,wind_speed_10m",
        @Query("daily") daily: String = "time,weather_code,temperature_2m_max,temperature_2m_min",
        @Query("timezone") timezone: String = "auto",
    ): WeatherResponse

    /**
     * 异常兜底验证:不存在的接口路径,服务端返回 4xx,
     * 由 [com.hy.basic.network.ApiExceptionExt] 归一化为 ApiException 并走全局提示。
     */
    @GET("v1/endpoint-does-not-exist")
    suspend fun brokenEndpoint(): WeatherResponse
}
