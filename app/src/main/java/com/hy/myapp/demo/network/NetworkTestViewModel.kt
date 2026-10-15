package com.hy.myapp.demo.network

import androidx.lifecycle.viewModelScope
import com.hy.basic.BaseViewModel
import com.hy.basic.network.ApiException
import com.hy.basic.network.ApiExceptionExt
import com.hy.basic.network.awaitRawRequest
import com.hy.basic.network.launchRawRequest
import com.hy.basic.network.launchRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * desc: 网络请求测试页 ViewModel
 * 原 demo/DemoActivity 中的 DemoViewModel 迁移至此并按测试项细分。
 *
 * 只依赖 [WeatherRepository] 接口,不关心数据来自线上还是本地 Mock
 * (切换见 [WeatherApi.USE_MOCK_DATA])。
 *
 * 三种请求形态的验证:
 * - 裸 JSON + 多域名 + 串行依赖:见 [loadWeather]
 * - 信封响应(code/msg/data)+ unwrap 解包:见 [loadEnvelopeData]
 * - 异常兜底(4xx/超时/无数据)统一走 [BaseViewModel.apiExceptionEvent]:见 [loadBrokenEndpoint]
 */
class NetworkTestViewModel : BaseViewModel() {

    private val repository: WeatherRepository = createWeatherRepository()

    private val _weather = MutableStateFlow<WeatherUiState?>(null)

    /** 天气展示数据(null 表示尚未请求成功,页面显示占位) */
    val weather = _weather.asStateFlow()

    private val _resultText = MutableStateFlow("")

    /** 信封接口/异常测试的结果文本 */
    val resultText = _resultText.asStateFlow()

    /**
     * 城市 -> 经纬度 -> 天气,两个域名的串行请求。
     *
     * 注意两步用了不同 API,这是本方法的关键示范:
     * - 第一步用 [launchRawRequest]:它的返回值是"请求是否成功",能把"请求失败"和"数据为空"分开。
     *   若用 [awaitRawRequest](失败也返回 null),断网时会先提示"网络连接失败"、
     *   紧接着再补一条"未找到城市「xx」",两条提示互相矛盾且都是重复弹窗。
     * - 第二步没有额外的业务判断,用 [awaitRawRequest] 更短:失败即中断,原因已由全局异常通道提示。
     *
     * @param showLoading false 为静默刷新(下拉刷新等场景),验证 loading 引用计数能力
     */
    fun loadWeather(cityName: String, showLoading: Boolean = true) = viewModelScope.launch {
        val city = cityName.trim()
        if (city.isEmpty()) {
            postApiException(ApiException(null, "请输入城市名称"))
            return@launch
        }
        var geoResponse: GeoResponse? = null
        val geoSucceeded = launchRawRequest(
            showLoading = showLoading,
            block = { repository.searchCity(city) },
        ) { geoResponse = it }
        if (!geoSucceeded) return@launch

        val target = geoResponse?.firstCity()
        if (target == null) {
            // 只有在"请求成功但没匹配到城市"时才是真的城市不存在
            postApiException(ApiException(ApiExceptionExt.CODE_EMPTY_DATA, "未找到城市「$city」"))
            return@launch
        }
        val forecast = awaitRawRequest(showLoading) {
            repository.forecast(target.latitude, target.longitude)
        } ?: return@launch
        _weather.value = forecast.toUiState(
            city = target.name ?: city,
            region = listOfNotNull(target.admin1, target.country).joinToString(" · "),
        )
    }

    /** 信封接口(data 字段):基类 request 负责 unwrap 解包与业务码校验 */
    fun loadEnvelopeData() = viewModelScope.launch {
        launchRequest(block = { getTestResult() }) { data: TestResultDataKt? ->
            _resultText.value =
                "信封接口(data 字段)返回成功\ncode = ${data?.from}\n${data?.name.orEmpty()}"
        }
    }

    /** 信封接口(content 字段):验证 unwrap 的 data -> result -> content 优先级 */
    fun loadContentData() = viewModelScope.launch {
        launchRequest(block = { getApiTest(16, "mixed", true) }) { content ->
            _resultText.value = "信封接口(content 字段)解包结果:\n${content?.string}"
        }
    }

    /** 异常兜底:异常 -> 归一化 ApiException -> 基类 onApiException 统一提示,不会崩溃、也不叠加其他提示 */
    fun loadBrokenEndpoint() = viewModelScope.launch {
        launchRawRequest(block = { repository.brokenEndpoint() }) {
            _resultText.value = "接口居然可用(预期失败并触发全局异常提示)"
        }
    }

    companion object {
        /** 默认城市,进入页面自动查询一次 */
        const val DEFAULT_CITY = "郑州"
    }
}
