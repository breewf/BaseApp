package com.hy.myapp.demo.oilprice

import com.google.gson.annotations.SerializedName

/**
 * desc: 油价接口响应体 + 页面展示模型
 *
 * 字段全部声明为可空并有默认值:Gson 反序列化绕过构造函数,缺字段时不会赋值,
 * 非空类型会出现"看似非空实为 null"的坑,因此统一按可空处理。
 */

// ---------------- 接口响应模型 ----------------

/** area=all:data 是"日期 + 列表"结构 */
data class OilPriceAllResponse(
    @SerializedName("status") val status: Int? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: OilPriceAllData? = null,
)

data class OilPriceAllData(
    @SerializedName("date") val date: String? = null,
    @SerializedName("next_update_time") val nextUpdateTime: String? = null,
    @SerializedName("list") val list: List<OilPriceItem?>? = null,
)

/** 单地区油价(92/95/98/0 号柴油,接口以字符串返回) */
data class OilPriceItem(
    @SerializedName("name") val name: String? = null,
    @SerializedName("p92") val p92: String? = null,
    @SerializedName("p95") val p95: String? = null,
    @SerializedName("p98") val p98: String? = null,
    @SerializedName("p0") val p0: String? = null,
)

/** area=某地区:data 直接是"单地区对象" */
data class OilPriceSingleResponse(
    @SerializedName("status") val status: Int? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: OilPriceSingleData? = null,
)

data class OilPriceSingleData(
    @SerializedName("date") val date: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("p92") val p92: String? = null,
    @SerializedName("p95") val p95: String? = null,
    @SerializedName("p98") val p98: String? = null,
    @SerializedName("p0") val p0: String? = null,
    @SerializedName("next_update_time") val nextUpdateTime: String? = null,
)

// ---------------- 页面展示模型 ----------------

/** 页面只面向展示模型,不感知接口结构(换接口不影响 UI 代码) */
data class OilPriceUiState(
    /** 当前查询范围标题,如"全部地区(31 个)"或"北京" */
    val scope: String,
    /** 数据日期 */
    val date: String,
    /** 下次更新时间 */
    val nextUpdateTime: String,
    /** 列表行(all 时多行,单地区时一行) */
    val rows: List<OilPriceRowUi>,
)

data class OilPriceRowUi(
    val name: String,
    val p92: String,
    val p95: String,
    val p98: String,
    val p0: String,
)

// ---------------- 响应 -> 展示模型 ----------------

fun OilPriceAllData.toAllUiState(): OilPriceUiState {
    val rows = list?.filterNotNull()?.map { it.toRow() } ?: emptyList()
    return OilPriceUiState(
        scope = "全部地区(${rows.size} 个)",
        date = date.orEmpty(),
        nextUpdateTime = nextUpdateTime.orEmpty(),
        rows = rows,
    )
}

fun OilPriceSingleData.toSingleUiState(fallbackName: String): OilPriceUiState {
    val name = name ?: fallbackName
    return OilPriceUiState(
        scope = name,
        date = date.orEmpty(),
        nextUpdateTime = nextUpdateTime.orEmpty(),
        rows = listOf(toRow()),
    )
}

private fun OilPriceItem.toRow() = OilPriceRowUi(
    name = name.orEmpty(),
    p92 = p92.toPrice(),
    p95 = p95.toPrice(),
    p98 = p98.toPrice(),
    p0 = p0.toPrice(),
)

private fun OilPriceSingleData.toRow() = OilPriceRowUi(
    name = name.orEmpty(),
    p92 = p92.toPrice(),
    p95 = p95.toPrice(),
    p98 = p98.toPrice(),
    p0 = p0.toPrice(),
)

/** 价格缺失时统一显示占位,避免空字符串让表格错位 */
private fun String?.toPrice(): String = if (isNullOrBlank()) "--" else this
