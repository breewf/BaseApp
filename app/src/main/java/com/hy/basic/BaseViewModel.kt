package com.hy.basic

import androidx.lifecycle.ViewModel
import com.hy.basic.network.ApiException
import com.hy.basic.page.PageState
import com.hy.basic.page.UiEvent
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
 * - [uiEvent]:ViewModel -> 页面的一次性事件通道([UiEvent]:Toast/错误/导航/返回/关页/回传结果/自定义)。
 *   使用 SharedFlow 而非 LiveData,避免转屏/重订阅时旧事件被重放导致重复 Toast/重复跳转;
 *   旧的 apiExceptionEvent 已并入本通道(ShowError 分支),异常仍保留 code 供 401 等定制
 * - [isLoading]:加载态,内部引用计数,支持并发请求(最后一个请求结束才隐藏 loading)
 *   —— 用于页面内局部转圈(如列表顶部进度条),与整页状态无关
 * - [pageState]:整页多状态(加载/内容/空/错误/无网络),由 [com.hy.basic.page.PageStateLayout]
 *   收集驱动。与业务数据流正交,不与数据耦合
 *
 * 状态与事件的分工:pageState/isLoading 是**状态**(可重放,恢复后仍是最新值),
 * uiEvent 是**事件**(错过不补);Toast/导航混进状态会被重放,页面状态混进事件会丢。
 *
 * 注:ViewModel 不再实现 LifecycleObserver(该机制已废弃),
 * UI 相关生命周期逻辑一律放在 Activity/Fragment 侧。
 */
open class BaseViewModel : ViewModel() {

    private val _uiEvent = MutableSharedFlow<UiEvent>(extraBufferCapacity = 16)

    /**
     * 一次性 UI 事件流,由基类页面统一收集并分发到 onShowToast / onShowError /
     * onNavigate 等钩子(见 [com.hy.basic.BaseActivity.onUiEvent])。
     */
    val uiEvent: SharedFlow<UiEvent> = _uiEvent.asSharedFlow()

    private val _isLoading = MutableStateFlow(false)

    /** 加载态,由 [com.hy.basic.network.launchRequest] 自动维护,页面基类收集后驱动 UI */
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val loadingCounter = AtomicInteger(0)

    private val _pageState = MutableStateFlow<PageState>(PageState.Content)

    /**
     * 整页多状态。默认 [PageState.Content](直接展示内容,行为与未启用状态页的旧页面一致)。
     * 仅当页面基类开启 `enablePageState` 时才会被 [com.hy.basic.page.PageStateLayout] 收集消费。
     */
    val pageState: StateFlow<PageState> = _pageState.asStateFlow()

    /**
     * 发送一次性 UI 事件(Toast/导航/回传结果等)。
     * [tryEmit] 不要求协程上下文,缓冲区满(短时间堆积 >16 条)时丢弃并返回 false。
     */
    fun emitUiEvent(event: UiEvent): Boolean = _uiEvent.tryEmit(event)

    /**
     * 上报网络异常(请求封装内部自动调用,业务侧也可手动上报)。
     * 归一到统一事件通道的 [UiEvent.ShowError] 分支,保留 code 供页面定制。
     */
    fun postApiException(exception: ApiException) {
        emitUiEvent(UiEvent.ShowError(exception))
    }

    // ==== 整页状态便捷方法:业务只声明"现在是什么状态",不碰任何 View ====

    /** 进入整页加载 */
    protected fun showPageLoading() {
        _pageState.update { PageState.Loading }
    }

    /** 展示正常内容 */
    protected fun showPageContent() {
        _pageState.update { PageState.Content }
    }

    /** 请求成功但无数据 */
    protected fun showPageEmpty(message: CharSequence? = null) {
        _pageState.update { PageState.Empty(message) }
    }

    /** 网络不可用 */
    protected fun showPageNoNetwork(message: CharSequence? = null) {
        _pageState.update { PageState.NoNetwork(message) }
    }

    /** 请求/业务失败,可由归一化后的 [ApiException] 直接推导状态 */
    protected fun showPageError(exception: ApiException) {
        _pageState.update { PageState.from(exception) }
    }

    /** 请求/业务失败,自定义文案与错误码 */
    protected fun showPageError(message: CharSequence?, code: Int = 0) {
        _pageState.update { PageState.Error(message, code) }
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
