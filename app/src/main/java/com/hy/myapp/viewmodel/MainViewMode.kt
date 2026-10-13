package com.hy.myapp.viewmodel

import androidx.lifecycle.viewModelScope
import com.hy.basic.BaseViewModel
import com.hy.basic.network.launchRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * desc: Demo ViewModel(ViewBinding + 协程版)
 * 原 ObservableField(DataBinding 可观察字段)已替换为 StateFlow,
 * 异常/loading 由 BaseViewModel + 页面基类统一处理
 */
class MainViewMode : BaseViewModel() {

    private val _responseText = MutableStateFlow("Kotlin ViewMode")
    val responseText = _responseText.asStateFlow()

    fun requestData() = viewModelScope.launch {
        launchRequest(block = { getApiTest("") }) { content ->
            _responseText.value = content.orEmpty()
        }
    }
}
