package com.hy.myapp.demo.state

import androidx.lifecycle.viewModelScope
import com.hy.basic.BaseViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * desc: 整页多状态用法示例 ViewModel。
 *
 * 演示重点:
 * - 页面状态由 [BaseViewModel.pageState] 单独承载,与业务数据流 [content] 正交,互不耦合;
 * - 业务代码只调用 showPageLoading/showPageContent/showPageEmpty/showPageError/showPageNoNetwork,
 *   完全不碰任何 View,页面 XML 里也没有 loading/empty/error 视图。
 */
class PageStateDemoViewModel : BaseViewModel() {

    private val _content = MutableStateFlow("")

    /** 成功态要展示的业务数据(与整页状态分离) */
    val content = _content.asStateFlow()

    /** 演示轮播的终态序列:内容 -> 空 -> 错误 -> 无网络,循环 */
    private val sequence = listOf(Outcome.DATA, Outcome.EMPTY, Outcome.ERROR, Outcome.NO_NETWORK)
    private var step = 0

    /** 模拟一次"进入页面/下拉刷新/点击重试"的加载,并按顺序落到不同终态 */
    fun reload() = viewModelScope.launch {
        showPageLoading()
        delay(SIMULATE_DELAY_MS)
        when (sequence[step % sequence.size]) {
            Outcome.DATA -> {
                _content.value = "加载成功 ✅\n第 ${step + 1} 次拉取到内容数据。\n" +
                        "点击下方按钮或整页状态里的「重试」可循环体验 空 / 错误 / 无网络 状态。"
                showPageContent()
            }

            Outcome.EMPTY -> showPageEmpty("这里一条数据都没有 📭")
            Outcome.ERROR -> showPageError("服务器开小差了，稍后再试 🛠", code = 5000)
            Outcome.NO_NETWORK -> showPageNoNetwork()
        }
        step++
    }

    private enum class Outcome { DATA, EMPTY, ERROR, NO_NETWORK }

    private companion object {
        const val SIMULATE_DELAY_MS = 700L
    }
}
