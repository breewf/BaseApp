package com.hy.myapp.demo.oilprice

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * desc: 油价查询接口地址与数据源开关(所有"换接口"的事只改这里)
 *
 * 接口来自公共开放平台 https://www.iamwawa.cn/oilprice,返回的是 {status, data} 结构,
 * **不是**本项目 [com.hy.basic.network.BaseHttpResponse](code/msg/data) 那套信封,
 * 所以归到"裸 JSON 第三方接口",ViewModel 里用 [com.hy.basic.network.launchRawRequest] 走,
 * 并手动校验 status —— 与天气 demo 里手动判断"未找到城市"是同一种示范。
 */
object OilPriceApi {

    /**
     * 数据源开关:
     * - true:[MockOilPriceRepository] 本地造数据(不依赖外网,依然走完整的 loading / 反序列化 / 解析 / 上屏流程)
     * - false:[RemoteOilPriceRepository] 真实请求 [BASE_URL]
     */
    const val USE_MOCK_DATA = false

    /** 油价接口域名(Retrofit baseUrl 需以 '/' 结尾,方法里写相对路径) */
    const val BASE_URL = "https://www.iamwawa.cn/"
}

/**
 * 油价接口。同一个路径 `oilprice/api` 靠 area 参数区分两种返回结构:
 * - area=all -> data 是"含 list 的对象"([OilPriceAllResponse])
 * - area=某地区 -> data 是"单地区对象"([OilPriceSingleResponse])
 * 两者结构不同,因此拆成两个方法、两个返回类型,便于演示"同域名不同结构"的建模方式。
 */
interface OilPriceService {

    /** 获取所有地区油价 */
    @GET("oilprice/api")
    suspend fun fetchAll(
        @Query("area") area: String = "all",
    ): OilPriceAllResponse

    /** 获取指定地区油价(area 传中文地区名,如"北京") */
    @GET("oilprice/api")
    suspend fun fetchArea(
        @Query("area") area: String,
    ): OilPriceSingleResponse
}
