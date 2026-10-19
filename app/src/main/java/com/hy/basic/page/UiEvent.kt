package com.hy.basic.page

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.hy.basic.network.ApiException

/**
 * desc: ViewModel → 页面的一次性事件通道载荷。
 * ViewModel 发事件只负责“发”,基类页面统一收集后分发到各 open 钩子,页面按需重写单个钩子即可。
 *
 * 与 PageState 的分工:pageState/isLoading 是状态(StateFlow,可重放),
 * uiEvent 是事件(SharedFlow,错过不补)。状态混进事件会被重放(转屏重复弹 Toast 的根因),
 * 事件混进状态会丢。
 */
sealed class UiEvent {

    /** 轻提示;[long]=true 走长时长 */
    data class ShowToast(val message: CharSequence?, val long: Boolean = false) : UiEvent()

    /** 错误提示,保留 code 便于 401 等分支定制 */
    data class ShowError(val exception: ApiException) : UiEvent()

    /** 页面导航,目标可以是 App 内 Activity Class 或任意 Intent(隐式跳转) */
    data class Navigate(
        val target: NavigationTarget,
        val flags: Int = 0,
        val extras: Bundle? = null,
    ) : UiEvent() {
        companion object {
            /** 跳转到 App 内页面:goto(LoginActivity::class.java) { putExtra(...) } */
            fun goto(
                activityClass: Class<out Activity>,
                flags: Int = 0,
                block: Intent.() -> Unit = {},
            ): Navigate = Navigate(NavigationTarget.Explicit(activityClass), flags, Intent().apply(block).extras)

            /** 跳转到任意 Intent(action/data/component 自备,隐式跳转走系统解析) */
            fun by(intent: Intent, flags: Int = 0): Navigate =
                Navigate(NavigationTarget.ByIntent(intent), flags)
        }
    }

    /** 返回上一页:走返回分发器,弹窗/折叠头拦截等 onBackPressedDispatcher 回调仍然生效 */
    data object GoBack : UiEvent()

    /** 直接关闭当前页面(带结果时发 [ShowResult],由被拉起方的 awaitResult 收到) */
    data class Finish(val resultCode: Int = Activity.RESULT_CANCELED, val data: Intent? = null) : UiEvent()

    /** 向拉起方回传数据,内部自动 setResult + finish,与 awaitResult 配套 */
    data class ShowResult(
        val values: Map<String, Any?>,
        val resultCode: Int = Activity.RESULT_OK,
    ) : UiEvent()

    /** 自定义事件:业务专属动作用 tag 区分,[data] 携带任意负载 */
    data class Custom(val tag: String, val data: Any? = null) : UiEvent()
}

/** [UiEvent.Navigate] 的目标描述 */
sealed class NavigationTarget {
    /** App 内显式跳转 */
    data class Explicit(val activityClass: Class<out Activity>) : NavigationTarget()

    /** 任意已组装好的 Intent(隐式跳转/外部应用) */
    data class ByIntent(val intent: Intent) : NavigationTarget()
}
