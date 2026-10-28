package com.hy.demo.oilprice

import com.google.gson.Gson
import com.hy.basic.network.Api
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlinx.coroutines.delay

/**
 * desc: 油价数据源抽象层
 *
 * 为什么要这一层:ViewModel 只依赖 [OilPriceRepository] 接口,
 * "真实接口""本地 Mock"都是实现细节,页面代码、loading/异常通道、解析链路全不用动。
 * 两个实现返回完全相同的响应类型,Mock 模式依然会跑一遍 Gson 反序列化 + toUiState 映射。
 */
interface OilPriceRepository {

    /** 全部地区油价(area=all,data 为列表结构) */
    suspend fun fetchAll(): OilPriceAllResponse

    /** 指定地区油价(data 为单对象结构) */
    suspend fun fetchArea(area: String): OilPriceSingleResponse
}

/** 按 [OilPriceApi.USE_MOCK_DATA] 决定用哪套数据源,调用方无需感知 */
fun createOilPriceRepository(): OilPriceRepository =
    if (OilPriceApi.USE_MOCK_DATA) MockOilPriceRepository() else RemoteOilPriceRepository()

// ---------------------------------------------------------------- 线上实现

/**
 * 真实请求:复用 [Api.create] 建接口实例(共用全局 OkHttpClient 的连接池/线程池)。
 * lazy 保证首次请求时才创建,进程内只创建一次。
 */
class RemoteOilPriceRepository : OilPriceRepository {

    private val service by lazy { Api.create<OilPriceService>(OilPriceApi.BASE_URL) }

    override suspend fun fetchAll(): OilPriceAllResponse = service.fetchAll()

    override suspend fun fetchArea(area: String): OilPriceSingleResponse =
        service.fetchArea(area)
}

// ---------------------------------------------------------------- 本地 Mock 实现

/**
 * 离线数据源:造与线上结构一致的 JSON,再交给 Gson 解析,流程与线上一致。
 * - [delay] 模拟网络耗时,方便观察 loading 转圈
 * - 用地区名派生稳定偏移量,让不同地区价格不同,便于确认 UI 真的在刷新
 * - 查询不在表内的地区时返回 status=0,用于演示"业务码非成功"的分支
 */
class MockOilPriceRepository : OilPriceRepository {

    private val gson = Gson()

    override suspend fun fetchAll(): OilPriceAllResponse {
        delay(MOCK_DELAY_MS)
        val list = MOCK_AREAS.joinToString(",\n") { mockItemJson(it) }
        return gson.fromJson(
            """
            {
              "status": 1,
              "message": "成功",
              "data": {
                "date": "${mockDate(0)}",
                "next_update_time": "${mockDate(10)} 24:00",
                "list": [
                  $list
                ]
              }
            }
            """.trimIndent(),
            OilPriceAllResponse::class.java,
        )
    }

    override suspend fun fetchArea(area: String): OilPriceSingleResponse {
        delay(MOCK_DELAY_MS)
        val keyword = area.trim()
        val matched = MOCK_AREAS.firstOrNull { it == keyword || keyword.startsWith(it) }
        if (matched == null) {
            return gson.fromJson(
                """{"status": 0, "message": "未收录地区「$keyword」,请检查名称"}""",
                OilPriceSingleResponse::class.java,
            )
        }
        val offset = priceOffset(matched)
        return gson.fromJson(
            """
            {
              "status": 1,
              "message": "成功",
              "data": {
                "date": "${mockDate(0)}",
                "name": "$matched",
                "p92": "${9.00 + offset * 0.01}",
                "p95": "${9.55 + offset * 0.01}",
                "p98": "${10.60 + offset * 0.01}",
                "p0": "${8.30 + offset * 0.01}",
                "next_update_time": "${mockDate(10)} 24:00"
              }
            }
            """.trimIndent(),
            OilPriceSingleResponse::class.java,
        )
    }

    private fun mockItemJson(area: String): String {
        val offset = priceOffset(area)
        return """
            {
              "name": "$area",
              "p92": "${9.00 + offset * 0.01}",
              "p95": "${9.55 + offset * 0.01}",
              "p98": "${10.60 + offset * 0.01}",
              "p0": "${8.30 + offset * 0.01}"
            }
        """.trimIndent()
    }

    /** 由地区名派生 0..90 的稳定偏移,让每个地区价格略有差异 */
    private fun priceOffset(area: String): Int = (kotlin.math.abs(area.hashCode()) % 90)

    /** 相对今天的第 [plusDays] 天,格式 yyyy-MM-dd(保证 Mock 数据永远是"最新") */
    private fun mockDate(plusDays: Int): String {
        val calendar = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, plusDays) }
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
    }

    private companion object {

        /** 模拟网络耗时(ms) */
        const val MOCK_DELAY_MS = 700L

        /** 写死的地区表(与接口支持地区一致的一部分);不在表内的地区走 status=0 分支 */
        val MOCK_AREAS = listOf(
            "北京", "上海", "江苏", "天津", "重庆", "广东", "山东", "浙江",
            "河南", "四川", "湖北", "河北", "福建", "湖南", "安徽", "陕西",
        )
    }
}
