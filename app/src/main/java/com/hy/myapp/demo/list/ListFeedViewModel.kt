package com.hy.myapp.demo.list

import androidx.lifecycle.viewModelScope
import com.hy.basic.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * desc: 竖向列表页 ViewModel。整页多状态 + 下拉/上拉分页的完整状态机。
 * 三条关注点正交:业务数据流 items / 整页状态 pageState / 下拉上拉 pullRefresh+loadMore。
 * 数据来自 ListFakeData(本地假数据 + 模拟延迟)。
 */
class ListFeedViewModel : BaseViewModel() {

    private val _items = MutableStateFlow<List<FeedItem>>(emptyList())

    /** 列表数据,Fragment collect 后 submitList 给适配器 */
    val items = _items.asStateFlow()

    /** 当前已加载到的页码(从 1 开始);0 表示尚未加载 */
    private var page = 0

    /** 是否还有下一页:供下拉刷新收尾时判断 hasMore */
    val hasMore: Boolean get() = page in 1 until ListFakeData.TOTAL_PAGE

    /** 演示标记:置为 true 后,下一次 [pullRefresh] 故意返回 false(展示刷新失败动画) */
    private var armRefreshFail = false

    /** 首次进入 / 整页重试:Loading → (Content | Empty | Error)。假数据总是有内容,这里稳定落到 Content */
    fun loadFirst() = viewModelScope.launch {
        showPageLoading()
        val data = ListFakeData.feedPage(PAGE_FIRST)
        page = PAGE_FIRST
        _items.value = data
        if (data.isEmpty()) showPageEmpty(EMPTY_TIP) else showPageContent()
    }

    /**
     * 下拉刷新(挂起):重置到第 1 页。
     * @return true=刷新成功(数据已替换);false=失败([armRefreshFailOnce] 后首次必失败,演示失败态)
     */
    suspend fun pullRefresh(): Boolean {
        if (armRefreshFail) {
            armRefreshFail = false
            return false
        }
        val data = ListFakeData.feedPage(PAGE_FIRST)
        page = PAGE_FIRST
        _items.value = data
        return true
    }

    /** 上拉加载更多(挂起):追加下一页。到达最后一页返回 false */
    suspend fun loadMore(): Boolean {
        if (page >= ListFakeData.TOTAL_PAGE) return false
        val next = ListFakeData.feedPage(page + 1, latencyMs = LOAD_MORE_LATENCY)
        _items.value += next
        page += 1
        return page < ListFakeData.TOTAL_PAGE
    }

    // ==== 整页状态演示:点一下就落到对应终态,便于快速回看各状态长什么样 ====

    /** 让下一次下拉刷新失败一次(配合 Fragment 提示"请下拉") */
    fun armRefreshFailOnce() {
        armRefreshFail = true
    }

    /** 演示空态 */
    fun demoEmpty() = showPageEmpty(EMPTY_TIP)

    /** 演示错误态 */
    fun demoError() = showPageError(ERROR_TIP, code = 5000)

    /** 演示无网络态 */
    fun demoNoNetwork() = showPageNoNetwork()

    private companion object {
        const val PAGE_FIRST = 1

        /** 自动预加载的模拟延迟:取 0 代表"网络快→直接插数据不闪加载态";调大即可演示"慢→只显示转圈" */
        const val LOAD_MORE_LATENCY = 0L
        const val EMPTY_TIP = "这里一条数据都没有 📭"
        const val ERROR_TIP = "服务器开小差了,稍后再试 🛠"
    }
}
