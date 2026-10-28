package com.hy.basic.hint

import android.content.Context
import android.content.res.Resources
import androidx.annotation.ColorInt
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import com.blankj.utilcode.util.ToastUtils

/**
 * desc: Toast 统一入口。底层复用 ToastUtils(踩平了 7.1 BadTokenException 等问题)。
 *
 * 注意:Toast 是“最新优先”,不是队列。同一帧连弹多条用户只看到最后一条。
 * 要合并信息就拼成一条文案;要“能反悔”的提示用 SnackbarKit。
 *
 * Android 11+ 系统限制自定义 Toast 视图,位置/底色可能被降级成系统默认样式。
 *
 * ```
 * toast("保存成功")
 * toastLong("后台导出已开始")
 * ToastKit.cancel()
 * ToastKit.style(gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL, yOffsetDp = 80)
 * ```
 */
object ToastKit {

    /** 弹一条 Toast。空 / 空白内容直接忽略,不弹没意义的空气泡 */
    fun show(message: CharSequence?, long: Boolean = false) {
        if (message.isNullOrBlank()) return
        if (long) ToastUtils.showLong(message) else ToastUtils.showShort(message)
    }

    /** 资源版:内部按占位符格式化 */
    fun show(@StringRes resId: Int, long: Boolean = false, vararg formatArgs: Any) {
        if (long) ToastUtils.showLong(resId, *formatArgs) else ToastUtils.showShort(resId, *formatArgs)
    }

    /** 撤掉当前正在显示的 Toast */
    fun cancel() {
        ToastUtils.cancel()
    }

    /** 全局样式配置,没传的项保持原样。gravity 位置, textColor/bgColor 传 0 不改, textSizeSp 传 0 不改 */
    fun style(
        gravity: Int? = null,
        xOffsetDp: Int = 0,
        yOffsetDp: Int = 64,
        @ColorInt textColor: Int = 0,
        @ColorInt bgColor: Int = 0,
        textSizeSp: Int = 0,
    ) {
        val maker = ToastUtils.getDefaultMaker()
        gravity?.let { maker.setGravity(it, dp(xOffsetDp), dp(yOffsetDp)) }
        if (textColor != 0) maker.setTextColor(textColor)
        if (bgColor != 0) maker.setBgColor(bgColor)
        if (textSizeSp != 0) maker.setTextSize(textSizeSp)
    }

    private fun dp(value: Int): Int =
        (value * Resources.getSystem().displayMetrics.density + 0.5f).toInt()
}

// ==== 调用处扩展 ====

/** 短 Toast */
fun Context.toast(message: CharSequence?, long: Boolean = false) = ToastKit.show(message, long)

/** 长时长 Toast(约 3.5 秒),重要提示用 */
fun Context.toastLong(message: CharSequence?) = ToastKit.show(message, long = true)

/** 资源版 Toast */
fun Context.toast(@StringRes resId: Int, vararg formatArgs: Any) =
    ToastKit.show(resId, formatArgs = formatArgs)

/** [toast] 的 Fragment 版 */
fun Fragment.toast(message: CharSequence?, long: Boolean = false) = ToastKit.show(message, long)

/** [toastLong] 的 Fragment 版 */
fun Fragment.toastLong(message: CharSequence?) = ToastKit.show(message, long = true)
