package com.hy.basic.hint

import android.app.Activity
import android.content.Context
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.IntDef
import androidx.core.content.ContextCompat
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.hy.myapp.R

/**
 * desc: Snackbar 统一入口 —— 贴在内容底部、**可以带一个操作**的提示。
 *
 * 与 Toast 的分工:
 * - Toast:说完就走、不打断操作(保存成功、请求失败);
 * - Snackbar:用户要"能立刻反悔"或"必须看一眼"的场景(删除后撤销、离线后重试)。
 *
 * ```
 * snackbar("已删除 1 条记录", "撤销") { restore() }                       // Activity / Fragment 里一行
 * binding.root.snackbar("网络已断开", "重试", duration = Snackbar.LENGTH_INDEFINITE) { reload() }
 * ```
 *
 * 为什么每次显示都显式上色:宿主主题是 `Theme.AppCompat`,里面没有 Material 的
 * `colorSurface` / `colorOnSurface` 这类属性,Snackbar 自带样式在这些主题下可能把文字解析成透明。
 * 这里统一按"覆盖值 -> 主题属性 -> 兜底色"取色,保证任何主题下都看得见、观感一致。
 * 要换全站风格,改 [backgroundColor] / [textColor] / [actionColor] 三个字段即可。
 */
object SnackbarKit {

    /** 底色;null = 主题 `colorSurface`,取不到用深灰兜底 */
    @ColorInt
    var backgroundColor: Int? = null

    /** 正文颜色;null = 白 */
    @ColorInt
    var textColor: Int? = null

    /** 操作按钮颜色;null = 主题 `colorAccent` */
    @ColorInt
    var actionColor: Int? = null

    /**
     * 弹一条 Snackbar。
     *
     * @param anchor 挂在哪个视图层级上(决定 Snackbar 贴在哪、能不能被滑动关闭)
     * @param actionText 操作按钮文案,为 null 就不带按钮
     * @param duration [Snackbar.LENGTH_SHORT] / [Snackbar.LENGTH_LONG] / [Snackbar.LENGTH_INDEFINITE];
     *                 用 INDEFINITE 时务必给 [actionText],否则用户没法关掉它
     * @param onAction 点操作按钮时回调,点完自动收起
     */
    fun show(
        anchor: View,
        message: CharSequence,
        actionText: CharSequence? = null,
        @Duration duration: Int = Snackbar.LENGTH_SHORT,
        onAction: (() -> Unit)? = null,
    ): Snackbar {
        val context = anchor.context
        val snackbar = Snackbar.make(anchor, message, duration)
            .setBackgroundTint(backgroundColor ?: context.themeColor(R.attr.colorSurface, darkGray(context)))
            .setTextColor(textColor ?: ContextCompat.getColor(context, R.color.white))
        if (actionText != null) {
            snackbar.setActionTextColor(
                actionColor ?: context.themeColor(R.attr.colorAccent, accent(context)),
            )
            snackbar.setAction(actionText) {
                onAction?.invoke()
                snackbar.dismiss()
            }
        }
        snackbar.show()
        return snackbar
    }

    private fun darkGray(context: Context): Int = ContextCompat.getColor(context, R.color.color_323232)

    private fun accent(context: Context): Int = ContextCompat.getColor(context, R.color.colorAccent)

    private fun Context.themeColor(@AttrRes attr: Int, @ColorInt fallback: Int): Int {
        val tv = TypedValue()
        return if (theme.resolveAttribute(attr, tv, true) && tv.type != TypedValue.TYPE_NULL) tv.data else fallback
    }

    /** [show] 的 duration 取值范围 */
    @IntDef(Snackbar.LENGTH_SHORT, Snackbar.LENGTH_LONG, Snackbar.LENGTH_INDEFINITE)
    @Retention(AnnotationRetention.SOURCE)
    annotation class Duration
}

// ==== 调用处直接可用的扩展 ====

/** 挂在这个 View 上弹 Snackbar */
fun View.snackbar(
    message: CharSequence,
    actionText: CharSequence? = null,
    @SnackbarKit.Duration duration: Int = Snackbar.LENGTH_SHORT,
    onAction: (() -> Unit)? = null,
): Snackbar = SnackbarKit.show(this, message, actionText, duration, onAction)

/**
 * 挂在页面上弹 Snackbar,不用自己找锚点:
 * 页面根是 [CoordinatorLayout] 时优先用它(有滑入滑出、能跟把手联动),否则退到内容容器。
 */
fun Activity.snackbar(
    message: CharSequence,
    actionText: CharSequence? = null,
    @SnackbarKit.Duration duration: Int = Snackbar.LENGTH_SHORT,
    onAction: (() -> Unit)? = null,
): Snackbar = SnackbarKit.show(snackbarAnchor(), message, actionText, duration, onAction)

/** [Activity.snackbar] 的锚点选取,页面想自己指定就改用 [View.snackbar] */
private fun Activity.snackbarAnchor(): View {
    val content = findViewById<ViewGroup>(android.R.id.content) ?: return window.decorView
    return (content.getChildAt(0) as? CoordinatorLayout) ?: content
}

/** [snackbar] 的 Fragment 版 */
fun Fragment.snackbar(
    message: CharSequence,
    actionText: CharSequence? = null,
    @SnackbarKit.Duration duration: Int = Snackbar.LENGTH_SHORT,
    onAction: (() -> Unit)? = null,
): Snackbar = requireActivity().snackbar(message, actionText, duration, onAction)
