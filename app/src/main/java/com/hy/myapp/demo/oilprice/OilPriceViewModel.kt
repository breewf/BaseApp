package com.hy.myapp.demo.oilprice

import androidx.lifecycle.viewModelScope
import com.hy.basic.BaseViewModel
import com.hy.basic.network.ApiException
import com.hy.basic.network.launchRawRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * desc: 油价查询页 ViewModel
 *
 * 只依赖 [OilPriceRepository] 接口,不关心数据来自线上还是本地 Mock(切换见 [OilPriceApi.USE_MOCK_DATA])。
 *
 * 关键示范:油价接口返回 {status, data} 裸 JSON,不是本项目的 BaseHttpResponse 信封,
 * 因此用 [launchRawRequest] 发请求,并在成功回调里**手动校验 status**——
 * 网络层只负责"请求成不成",业务码(status!=1)是否成功要业务自己判断,
 * 这与天气 demo 里手动判断"未找到城市"是同一种写法。
 */
class OilPriceViewModel : BaseViewModel() {

    private val repository: OilPriceRepository = createOilPriceRepository()

    private val _uiState = MutableStateFlow<OilPriceUiState?>(null)

    /** 展示数据(null 表示尚未请求成功,页面显示占位) */
    val uiState = _uiState.asStateFlow()

    private val _lastQueryArea = MutableStateFlow("")

    /** 最近一次查询的地区,转屏后用于恢复输入框 */
    val lastQueryArea = _lastQueryArea.asStateFlow()

    /** 拉取全部地区油价 */
    fun loadAll() = viewModelScope.launch {
        launchRawRequest(block = { repository.fetchAll() }) { response ->
            val data = response.data
            if (response.status != STATUS_SUCCESS || data == null) {
                postApiException(
                    ApiException(response.status, response.message ?: "油价数据获取失败"),
                )
                return@launchRawRequest
            }
            _lastQueryArea.value = ""
            _uiState.value = data.toAllUiState()
        }
    }

    /** 查询指定地区油价 */
    fun loadArea(areaInput: String) = viewModelScope.launch {
        val area = areaInput.trim()
        if (area.isEmpty()) {
            postApiException(ApiException(null, "请输入地区名称"))
            return@launch
        }
        launchRawRequest(block = { repository.fetchArea(area) }) { response ->
            val data = response.data
            if (response.status != STATUS_SUCCESS || data == null) {
                postApiException(
                    ApiException(response.status, response.message ?: "未查询到「$area」的油价"),
                )
                return@launchRawRequest
            }
            _lastQueryArea.value = area
            _uiState.value = data.toSingleUiState(area)
        }
    }

    private companion object {

        /** 接口约定的业务成功码 */
        const val STATUS_SUCCESS = 1
    }
}
