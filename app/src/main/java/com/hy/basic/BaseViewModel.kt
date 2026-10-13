package com.hy.basic

import androidx.lifecycle.ViewModel
import com.hy.basic.network.ApiException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.concurrent.atomic.AtomicInteger

/**
 * @author hy
 * desc: ViewModel 基类
 *
 * 通用能力:
 * - [apiExceptionEvent]:全局网络异常一次性事件。使用 SharedFlow 而非 LiveData,
 *   避免转屏/重订阅时旧异常事件被重放导致重复 Toast
 * - [isLoading]:加载态,内部引用计数,支持并发请求(最后一个请求结束才隐藏 loading)
 *
 * 注:ViewModel 不再实现 LifecycleObserver(该机制已废弃),
 * UI 相关生命周期逻辑一律放在 Activity/Fragment 侧。
 */
open class BaseViewModel : ViewModel() {

    private val _apiExceptionEvent = MutableSharedFlow<ApiException>(extraBufferCapacity = 16)

    /** 全局网络异常事件,由基类页面统一收集并回调 onApiException */
    val apiExceptionEvent: SharedFlow<ApiException> = _apiExceptionEvent.asSharedFlow()

    private val _isLoading = MutableStateFlow(false)

    /** 加载态,由 [com.hy.basic.network.launchRequest] 自动维护,页面基类收集后驱动 UI */
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val loadingCounter = AtomicInteger(0)

    /** 上报网络异常(请求封装内部自动调用,业务侧也可手动上报) */
    fun postApiException(exception: ApiException) {
        _apiExceptionEvent.tryEmit(exception)
    }

    /** 开始加载:引用计数 +1 */
    fun startLoading() {
        loadingCounter.incrementAndGet()
        _isLoading.update { true }
    }

    /** 结束加载:引用计数 -1,归零后才置为未加载 */
    fun stopLoading() {
        if (loadingCounter.decrementAndGet() <= 0) {
            loadingCounter.set(0)
            _isLoading.update { false }
        }
    }
}
