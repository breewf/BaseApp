package com.hy.basic.page

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.hy.basic.network.ApiException

/**
 * desc: ViewModel -> 页面的一次性事件通道载荷。
 *
 * 为什么需要它:业务里 Toast/跳转/返回/关页这类"发生一次就该消费一次"的动作没有一个统一出口,
 * 只能散落在页面里各写各的。[UiEvent] 把它们收成一个 sealed class:
 * - ViewModel 侧 [com.hy.basic.BaseViewModel.emitUiEvent] 只负责"发",不碰任何 View;
 * - 基类页面统一收集后分发到 onShowToast / onShowError / onNavigate 等 open 钩子,
 *   页面按需重写单个钩子即可(如 401 跳登录重写 [onShowError] 的分支),零样板。
 *
 * 与 [PageState] 的分工([com.hy.basic.BaseViewModel.pageState] 保留不合并):
 * - pageState/isLoading 是**状态**(StateFlow,可重放、转屏恢复后仍是最新值),
 *   适合"整页现在该显示哪一层";
 * - uiEvent 是**事件**(SharedFlow,错过不补),适合 Toast/导航等一次性动作。
 *   把状态混进事件会丢,把事件混进状态会被重放(转屏重复弹 Toast 的经典根因)。
 */
sealed class UiEvent {

    /** 轻提示;[long]=true 走长时长 */
    data class ShowToast(val message: CharSequence?, val long: Boolean = false) : UiEvent()

    /**
     * 错误提示:网络异常归一化后走这里(保留 code 便于 401 等分支定制),
     * 业务校验失败也可手动发。
     */
    data class ShowError(val exception: ApiException) : UiEvent()

    /**
     * 页面导航:基类组装 Intent 后 startActivity。
     * 目标既可以是 App 内 [Activity] Class,也可以是 action/data 齐备的 [Intent]
     * (隐式跳转/打开外部应用)。
     */
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

    /**
     * 向拉起方回传数据:内部自动 setResult 再 finish,
     * 与 [com.hy.basic.awaitResult] 配套使用(表单页/选择页提交时发)。
     */
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
