package com.hy.basic.page

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.LayoutRes
import androidx.core.content.ContextCompat
import com.hy.myapp.R

/**
 * desc: 全页面多状态容器 —— 在页面内容之上叠加 加载/空/错误/无网络 状态层。
 *
 * 解耦点(零外部依赖、零页面 XML 侵入):
 * - 状态层布局来自**独立 XML**(page_state_loading/empty/error/no_network.xml),
 *   业务页面 XML 里不需要写任何 loading/empty/error 视图;
 *   基类在 setContentView 时把页面根 View 作为内容 child 塞进本容器,
 *   且容器只包裹 Toolbar 之下的内容区 —— 状态层不会覆盖 Toolbar。
 * - 只有一个入口 [bind]:状态由 ViewModel 的 `Flow<PageState>` 驱动,页面不手写 visibility 切换。
 * - 想完全自定义某个状态:在 [PageStateConfig] 里换成自己的 layoutRes 即可。
 *   运行时按约定 id 回填数据,布局里没有对应 id 的 view 就跳过(不崩溃):
 *   - `ivPageStateIcon`    提示图标([PageStateConfig.iconResOverride] 非 0 时覆盖 src)
 *   - `tvPageStateMessage` 文案(状态携带 message 或配置文案非空时覆盖)
 *   - `btnPageStateRetry`  重试入口(绑定点击回调;Loading 状态隐藏)
 * - [attachContent] 会**同步预置 Loading 叠加层**,保证首帧就是"加载中"而不是
 *   未回填数据的裸内容,消除进页/切页时内容先闪一下再被覆盖的问题。
 *   切入状态层时**瞬时不透明覆盖**(不做淡入),避免半透明期间透出底部内容造成闪烁;
 *   仅回到 [PageState.Content] 时让状态层淡出揭示内容。
 */
class PageStateLayout(
    context: Context,
    private val config: PageStateConfig = PageStateConfig(),
) : FrameLayout(context) {

    private var stateView: View? = null

    /** 淡出中的旧状态层,新状态进来时会被立即移除 */
    private var fadingOutView: View? = null

    private var currentState: PageState? = null

    /** 把页面真正的内容 View 作为最底层放入容器(仅一次),并预置 Loading 防首帧闪烁 */
    fun attachContent(content: View) {
        if (content.parent === this) return
        addView(content, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        bind(PageState.Loading)
    }

    /**
     * 切换到目标状态。[onRetry] 在 空/错误/无网络 状态下被点击时回调(通常重新发起请求)。
     * 同状态重复调用直接忽略,避免切换动画抖动;回到 [PageState.Content] 时状态层淡出。
     */
    fun bind(state: PageState, onRetry: (() -> Unit)? = null) {
        if (state == currentState) return
        currentState = state
        when (state) {
            PageState.Content -> fadeOutStateView()
            else -> showState(state, onRetry)
        }
    }

    private fun showState(state: PageState, onRetry: (() -> Unit)?) {
        removeImmediate()
        // 不做淡入:状态层带不透明背景,直接盖住内容;若从 alpha 0 淡入会透出底层内容闪一下
        val view = createStateView(state, onRetry)
        addView(view, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        stateView = view
    }

    private fun fadeOutStateView() {
        removeFadingOut()
        val view = stateView ?: return
        stateView = null
        fadingOutView = view
        view.animate().alpha(0f).setDuration(STATE_ANIM_MS)
            .withEndAction {
                if (fadingOutView === view) {
                    fadingOutView = null
                    removeView(view)
                }
            }
            .start()
    }

    /** 立即移除当前与淡出中的状态层(切到新状态时避免叠层) */
    private fun removeImmediate() {
        removeFadingOut()
        stateView?.let {
            it.animate().cancel()
            removeView(it)
        }
        stateView = null
    }

    private fun removeFadingOut() {
        fadingOutView?.let {
            it.animate().cancel()
            removeView(it)
        }
        fadingOutView = null
    }

    private fun createStateView(state: PageState, onRetry: (() -> Unit)?): View {
        val view = LayoutInflater.from(context).inflate(layoutResFor(state), this, false)

        if (config.backgroundRes != 0) view.setBackgroundColor(color(config.backgroundRes))

        view.findViewById<TextView>(R.id.tvPageStateMessage)?.apply {
            messageFor(state)?.let { text = it }
            if (config.textRes != 0) setTextColor(color(config.textRes))
        }

        if (config.iconResOverride != 0) {
            view.findViewById<ImageView>(R.id.ivPageStateIcon)
                ?.setImageResource(config.iconResOverride)
        }

        view.findViewById<View>(R.id.btnPageStateRetry)?.apply {
            // loading 没有重试语义,布局里若带重试入口则隐藏
            visibility = if (state is PageState.Loading) GONE else VISIBLE
            if (state !is PageState.Loading) {
                (this as? TextView)?.let { tv ->
                    if (config.retryText.isNotEmpty()) tv.text = config.retryText
                    if (config.retryTextRes != 0) tv.setTextColor(color(config.retryTextRes))
                }
                setOnClickListener { onRetry?.invoke() }
            }
        }
        return view
    }

    @LayoutRes
    private fun layoutResFor(state: PageState): Int = when (state) {
        PageState.Loading -> config.loadingLayoutRes
        is PageState.Empty -> config.emptyLayoutRes
        is PageState.NoNetwork -> config.noNetworkLayoutRes
        is PageState.Error -> config.errorLayoutRes
        PageState.Content -> config.errorLayoutRes // Content 不会走到这里
    }

    /** 返回 null 表示不覆盖布局里写死的默认文案 */
    private fun messageFor(state: PageState): CharSequence? = when (state) {
        is PageState.Loading -> config.loadingText.takeIf { it.isNotEmpty() }
        is PageState.Empty -> state.message ?: config.emptyText.takeIf { it.isNotEmpty() }
        is PageState.NoNetwork -> state.message ?: config.noNetworkText.takeIf { it.isNotEmpty() }
        is PageState.Error -> state.message ?: config.errorText.takeIf { it.isNotEmpty() }
        PageState.Content -> null
    }

    private fun color(@ColorRes res: Int): Int = ContextCompat.getColor(context, res)

    private companion object {
        const val STATE_ANIM_MS = 180L
    }
}

/**
 * 状态层外观配置:纯数据,全部以资源 id / 文案提供,解析在 [PageStateLayout] 侧完成,
 * 因此配置对象可跨页面/跨 Context 复用。
 *
 * - 换某个状态的整套布局:改对应 `xxxLayoutRes`(页面重写
 *   `createPageStateConfig()` 传自己的即可);
 * - 只改文案/颜色/图标:动对应字段,0 / 空字符串表示"沿用布局里的默认值"。
 */
data class PageStateConfig(
    var loadingText: CharSequence = "",
    var emptyText: CharSequence = "",
    var errorText: CharSequence = "",
    var noNetworkText: CharSequence = "",
    var retryText: CharSequence = "",
    @param:LayoutRes var loadingLayoutRes: Int = R.layout.page_state_loading,
    @param:LayoutRes var emptyLayoutRes: Int = R.layout.page_state_empty,
    @param:LayoutRes var errorLayoutRes: Int = R.layout.page_state_error,
    @param:LayoutRes var noNetworkLayoutRes: Int = R.layout.page_state_no_network,
    /** 非 0 时覆盖布局里 ImageView 的默认图标 */
    @param:DrawableRes var iconResOverride: Int = 0,
    /** 非 0 时覆盖布局里的文案颜色 */
    @param:ColorRes var backgroundRes: Int = 0,
    @param:ColorRes var textRes: Int = 0,
    @param:ColorRes var retryTextRes: Int = 0,
)
