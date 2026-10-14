package com.hy.basic.hint

import android.content.Context
import android.content.res.Resources
import androidx.annotation.ColorInt
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import com.blankj.utilcode.util.ToastUtils

/**
 * desc: Toast 统一入口。调用处一行,样式与默认行为只在这里改一次。
 *
 * 底层复用 [ToastUtils](项目已有依赖):它替我们踩平了 Android 7.1 的 `BadTokenException`、
 * Toast 实例复用与快速连弹时的取消时机等问题。再包一层的意义是:业务侧只认 [toast] 这一个动词,
 * 以后换实现(比如加全局曝光埋点、敏感文案统一脱敏)只改本对象,不用去几十个页面里翻。
 *
 * ```
 * toast("保存成功")                       // Activity / Fragment / View 的 context 里都能直接调
 * toast("已同步 ${count} 条数据")
 * toastLong("后台导出已开始,完成后会通知你")
 * ToastKit.cancel()                       // 进度型提示:先撤掉上一条再弹,做覆盖式更新
 * ```
 *
 * **注意:Toast 是"最新优先",不是队列。** [ToastUtils] 每次 show 前都会先 `cancel()` 掉当前那条,
 * 所以同一帧里连弹 3 条,用户只会看到最后一条 —— 这是库有意的"覆盖式"设计(防止提示堆积刷屏)。
 * 要一次性把多条信息说清楚,就**合并成一条文案**;要"能立刻反悔"的提示用 [SnackbarKit]。
 *
 * 想改外观(位置 / 颜色 / 字号)在 App 启动时调一次 [style] 即可全站生效:
 * ```
 * ToastKit.style(gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL, yOffsetDp = 80)
 * ```
 * 注意:Android 11(API 30)之后系统限制自定义 Toast 视图,位置/底色在新系统上可能被降级成
 * 系统默认样式 —— 这是系统限制,不是封装失效。需要"贴在内容上方 + 能带一个操作"的提示用
 * [SnackbarKit]。
 */
object ToastKit {

    /** 弹一条 Toast。空 / 空白内容直接忽略,不弹没意义的空气泡 */
    fun show(message: CharSequence?, long: Boolean = false) {
        if (message.isNullOrBlank()) return
        if (long) ToastUtils.showLong(message) else ToastUtils.showShort(message)
    }

    /** 资源版:内部按 `%1$s` 这类占位符格式化,[formatArgs] 为空时等于直接取文案 */
    fun show(@StringRes resId: Int, long: Boolean = false, vararg formatArgs: Any) {
        if (long) ToastUtils.showLong(resId, *formatArgs) else ToastUtils.showShort(resId, *formatArgs)
    }

    /** 撤掉当前正在显示的 Toast */
    fun cancel() {
        ToastUtils.cancel()
    }

    /**
     * 全局样式配置,没传的项保持原样。
     *
     * - [gravity] 位置,横向/纵向偏移单位 dp
     * - [textColor] / [bgColor] 文字色 / 底色,传 0 表示不改
     * - [textSizeSp] 字号(sp),传 0 表示不改
     */
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

// ==== 调用处直接可用的扩展 ====

/** 短 Toast。[Context] 只是取个顺手入口,内容与时长之外的样式都在 [ToastKit] 里统一配 */
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
