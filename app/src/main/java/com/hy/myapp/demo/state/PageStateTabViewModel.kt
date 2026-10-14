package com.hy.myapp.demo.state

import androidx.lifecycle.viewModelScope
import com.hy.basic.BaseViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * desc: ViewPager 两个 Fragment 共用的状态演示 ViewModel(各自持有独立实例)。
 *
 * 演示重点:
 * - 整页状态仍只由 [BaseViewModel.pageState] 承载,Fragment 只调用语义方法,不碰任何 View;
 * - ViewModel 作用域是 Fragment,ViewPager 回滑重建 View 时状态与数据都不丢;
 * - [reload] 按 内容 -> 空 -> 错误 -> 无网络 轮播,[loadOnce] 只走一次成功加载,
 *   `showXxx` 系列用于手动指定单个状态。
 */
class PageStateTabViewModel : BaseViewModel() {

    private val _content = MutableStateFlow<CharSequence>("")

    /** 成功态要展示的业务数据(与整页状态分离) */
    val content = _content.asStateFlow()

    private val sequence = listOf(Outcome.DATA, Outcome.EMPTY, Outcome.ERROR, Outcome.NO_NETWORK)
    private var step = 0

    /** 模拟一次加载,并按顺序落到不同终态;[tag] 用于区分是哪个 Tab */
    fun reload(tag: CharSequence) = viewModelScope.launch {
        showPageLoading()
        delay(SIMULATE_DELAY_MS)
        when (sequence[step % sequence.size]) {
            Outcome.DATA -> showContent("[$tag] 第 ${step + 1} 次拉取成功 ✅\n滑到另一个 Tab 或点状态层「重试」继续体验。")
            Outcome.EMPTY -> showPageEmpty("[$tag] 这里一条数据都没有 📭")
            Outcome.ERROR -> showPageError("[$tag] 服务器开小差了，稍后再试 🛠", code = 5000)
            Outcome.NO_NETWORK -> showPageNoNetwork("[$tag] 网络不可用，检查连接后重试 📶")
        }
        step++
    }

    /** 一次纯成功的加载:Loading -> Content */
    fun loadOnce(tag: CharSequence) = viewModelScope.launch {
        showPageLoading()
        delay(SIMULATE_DELAY_MS)
        showContent("[$tag] 加载完成 ✅\n状态层已淡出,底下就是页面自己的内容区。")
    }

    /** 手动落到某个状态,便于逐个观察状态层外观 */
    fun showLoading() = showPageLoading()

    fun showContent(text: CharSequence) {
        _content.value = text
        showPageContent()
    }

    fun showEmpty(message: CharSequence) = showPageEmpty(message)

    fun showError(message: CharSequence) = showPageError(message, code = 5000)

    fun showNoNetwork(message: CharSequence) = showPageNoNetwork(message)

    private enum class Outcome { DATA, EMPTY, ERROR, NO_NETWORK }

    private companion object {
        const val SIMULATE_DELAY_MS = 700L
    }
}
