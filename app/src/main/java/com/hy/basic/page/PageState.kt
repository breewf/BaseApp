package com.hy.basic.page

import com.hy.basic.network.ApiException
import com.hy.basic.network.ApiExceptionHandler

/**
 * desc: 全页面级加载状态。
 *
 * 与业务数据流(StateFlow<T>)**正交、互不耦合**:
 * - 数据流负责"内容是什么";
 * - [PageState] 只负责"内容区当前该显示哪一层"(加载中 / 内容 / 空 / 错误 / 无网络)。
 *
 * 因此把整页状态放进 ViewModel 并不会和数据耦合:它是独立的一条 UI 关注点流,
 * 由 [com.hy.basic.page.PageStateLayout] 收集后驱动叠加层,页面 XML 完全无感。
 */
sealed class PageState {

    /** 整页骨架 loading(首次进入 / 全屏刷新) */
    data object Loading : PageState()

    /** 正常内容 */
    data object Content : PageState()

    /** 请求成功但无数据 */
    data class Empty(val message: CharSequence? = null) : PageState()

    /** 网络不可用(断网 / 超时) */
    data class NoNetwork(val message: CharSequence? = null) : PageState()

    /** 请求或业务失败 */
    data class Error(val message: CharSequence? = null, val code: Int = 0) : PageState()

    companion object {

        /** 被视为"无网络"的错误码集合,可按后端约定调整 */
        private val NO_NETWORK_CODES = setOf(
            ApiExceptionHandler.CODE_NET_ERROR,
            ApiExceptionHandler.CODE_TIMEOUT,
        )

        /**
         * 由归一化后的 [ApiException] 推导页面状态:
         * 连接类错误 -> [NoNetwork],其余 -> [Error](保留 code 便于后续定制)。
         */
        fun from(exception: ApiException): PageState {
            val code = exception.code ?: 0
            return if (code in NO_NETWORK_CODES) NoNetwork(exception.message) else Error(exception.message, code)
        }
    }
}
