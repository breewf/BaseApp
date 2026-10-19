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
 * desc: Snackbar 统一入口,贴在内容底部、可带操作的提示。
 *
 * 与 Toast 分工:Toast 说完就走;Snackbar 用于“能反悔”或“必须看一眼”的场景。
 * 每次显式上色:宿主 AppCompat 主题没有 Material 的 colorSurface/colorOnSurface,
 * 不显式设置文字可能透明。
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

    /** 弹一条 Snackbar。INDEFINITE 时务必给 actionText,否则用户关不掉 */
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
