package com.hy.basic.dialog

import android.content.DialogInterface
import android.view.Gravity
import android.view.View
import androidx.annotation.DrawableRes

/**
 * desc: 弹框配置 —— **一份配置,两种皮**。
 *
 * 系统样式([showDialog] 走 AppCompat AlertDialog)与自定义样式([showCustomDialog] 走自研圆角布局)
 * 读的是同一份配置:想换外观只改调用的函数名,参数一个字都不用动。
 *
 * 用法(DSL 里全是可选项,不写就走默认):
 * ```
 * showDialog {
 *     title = "删除这条记录?"
 *     message = "删除后不可恢复。"
 *     confirm("删除") { viewModel.delete() }
 *     cancel("再想想")
 * }
 * ```
 *
 * 只要一个按钮:`singleButton()`(或 `cancelText = null`);
 * 需要"点了不关"(校验失败留在弹框里)时置 [autoClose] = false,
 * 再在回调里自己调 [dismissDialog] —— 对话框实例由封装在 show 之后回填,页面不用持有。
 */
class DialogOptions internal constructor() {

    /** 标题,不写就没有标题行 */
    var title: CharSequence? = null

    /** 正文 */
    var message: CharSequence? = null

    /** 标题左侧图标,**仅系统样式生效**(自定义布局没留图标位) */
    @DrawableRes
    var iconRes: Int = 0

    /** 正文下面的自定义内容区(输入框、提示卡片等),两种样式都支持 */
    var customView: View? = null

    var confirmText: CharSequence = defaultConfirmText
    var cancelText: CharSequence? = defaultCancelText
    var onConfirm: (() -> Unit)? = null
    var onCancel: (() -> Unit)? = null

    /** 点按钮后是否自动关闭。false 时由回调里的 [dismissDialog] 决定何时关闭 */
    var autoClose: Boolean = true

    /** 能否用返回键关闭(强制更新/强制阅读场景置 false) */
    var cancelable: Boolean = true

    /** 点弹框外部能否关闭 */
    var cancelOnTouchOutside: Boolean = true

    /** 关闭回调:点按钮、点外部、返回键、页面销毁自动关闭都会走到,适合埋点或收尾 */
    var onDismiss: (() -> Unit)? = null

    // ==== 以下仅自定义样式([showCustomDialog])生效 ====

    /** 弹框宽度占屏幕宽的比例 */
    var widthRatio: Float = defaultWidthRatio

    /** 标题/正文对齐方式,默认居中;要"微信式左对齐"就传 [Gravity.START] */
    var contentGravity: Int = defaultContentGravity

    /** DSL 便捷写法:确定按钮文案(省略则用默认) + 点击动作 */
    fun confirm(text: CharSequence? = null, action: (() -> Unit)? = null) {
        if (text != null) confirmText = text
        onConfirm = action
    }

    /** DSL 便捷写法:取消按钮文案(省略则用默认) + 点击动作 */
    fun cancel(text: CharSequence? = null, action: (() -> Unit)? = null) {
        if (text != null) cancelText = text
        onCancel = action
    }

    /** 只保留一个按钮(提示类弹框常用) */
    fun singleButton() {
        cancelText = null
    }

    /** 当前弹框实例,由封装在 show 之后回填;[autoClose] = false 时用 [dismissDialog] 关它 */
    internal var dialogRef: DialogInterface? = null

    /** 主动关闭当前弹框(已关闭/还没弹出时调用是安全的空操作) */
    fun dismissDialog() {
        dialogRef.dismissSafely()
    }

    companion object {
        /** 全局默认文案:App 启动时改一次,全站弹框跟着变 */
        var defaultConfirmText: CharSequence = "确定"
        var defaultCancelText: CharSequence? = "取消"

        /** 全局默认宽度比例 */
        var defaultWidthRatio: Float = 0.82f

        /** 全局默认内容对齐 */
        var defaultContentGravity: Int = Gravity.CENTER
    }
}
