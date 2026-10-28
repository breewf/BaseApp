package com.hy.basic.dialog

import android.app.Dialog
import android.content.Context
import android.content.ContextWrapper
import android.content.DialogInterface
import android.text.InputFilter
import android.text.InputType
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.hy.basic.R

/**
 * desc: 弹框对外 API。核心思路是**调用处只写"要弹什么、点了干什么"**,
 * 主题选择、按钮回调拆包、WindowLeaked 防御这些琐事全部在这里吃掉。
 *
 * 两种皮共用一份 [DialogOptions]:
 * ```
 * // 系统样式
 * showDialog { title = "退出登录"; message = "确定要退出当前账号吗?"; confirm("退出") { logout() }; cancel() }
 * // 自定义样式(圆角白底 + 底部两个按钮),配置完全一样
 * showCustomDialog { title = "退出登录"; message = "确定要退出当前账号吗?"; confirm("退出") { logout() }; cancel() }
 * ```
 *
 * 常用场景还有更短的一行版:[showTip] 单按钮提示、[showConfirm] 二次确认、
 * [showItems] 列表选择、[showSingleChoice] 单选、[showMultiChoice] 多选
 *
 * 约束:必须从 Activity / Fragment(`requireContext()`)调用 —— 弹框要有窗口令牌,
 * 用 Application context 时拿不到 Activity,直接返回 null(不抛异常)。
 * 页面销毁时未关闭的弹框会被自动 dismiss,不会 WindowLeaked,也不会把回调打到已死的页面上。
 */

// ==== 一份配置,两种皮 ====

/** 系统样式 AlertDialog(链式 DSL),用法见文件注释 */
fun Context.showDialog(block: DialogOptions.() -> Unit): AlertDialog? =
    showDialog(DialogOptions().apply(block))

/** [showDialog] 的显式配置版,便于同一份配置复用给不同皮肤 */
fun Context.showDialog(options: DialogOptions): AlertDialog? {
    val host = findActivity() ?: return null
    if (host.isFinishing || host.isDestroyed) return null
    val builder = AlertDialog.Builder(host).setCancelable(options.cancelable)
    options.title?.let { builder.setTitle(it) }
    options.message?.let { builder.setMessage(it) }
    if (options.iconRes != 0) builder.setIcon(options.iconRes)
    options.customView?.let { builder.setView(it) }
    // AlertDialog 的按钮栏语义:不给 positive/negative 就不显示按钮
    builder.setPositiveButton(options.confirmText) { dialog, _ ->
        options.onConfirm?.invoke()
        if (options.autoClose) dialog.dismissSafely()
    }
    options.cancelText?.let { text ->
        builder.setNegativeButton(text) { dialog, _ ->
            options.onCancel?.invoke()
            if (options.autoClose) dialog.dismissSafely()
        }
    }
    val dialog = builder.create()
    dialog.setCanceledOnTouchOutside(options.cancelOnTouchOutside)
    options.onDismiss?.let { cb -> dialog.setOnDismissListener { cb() } }
    host.attachAndAutoDismiss(dialog)
    options.dialogRef = dialog
    dialog.show()
    if (!options.autoClose) {
        // AlertDialog 点按钮总会自己关掉,想"点了不关"(校验失败留在弹框里)只能在 show 后接管点击
        dialog.takeOverButton(DialogInterface.BUTTON_POSITIVE) { options.onConfirm?.invoke() }
        if (options.cancelText != null) {
            dialog.takeOverButton(DialogInterface.BUTTON_NEGATIVE) { options.onCancel?.invoke() }
        }
    }
    return dialog
}

/** 自定义样式弹框:圆角白底,标题(可选)+ 正文(可选)+ 内容插槽 + 两个按钮 */
fun Context.showCustomDialog(block: DialogOptions.() -> Unit): CustomDialog? =
    showCustomDialog(DialogOptions().apply(block))

/** [showCustomDialog] 的显式配置版 */
fun Context.showCustomDialog(options: DialogOptions): CustomDialog? {
    val host = findActivity() ?: return null
    if (host.isFinishing || host.isDestroyed) return null
    val dialog = CustomDialog(host, options)
    host.attachAndAutoDismiss(dialog)
    options.dialogRef = dialog
    dialog.show()
    return dialog
}

// ==== 常用一行版(系统样式) ====

/** 纯提示:只有一个按钮,点完就关 */
fun Context.showTip(
    message: CharSequence,
    title: CharSequence? = null,
    confirmText: CharSequence = DialogOptions.defaultConfirmText,
    onConfirm: (() -> Unit)? = null,
): AlertDialog? = showDialog {
    this.title = title
    this.message = message
    singleButton()
    confirm(confirmText, onConfirm)
}

/** 二次确认:确定走 [onConfirm],取消只负责关掉 */
fun Context.showConfirm(
    message: CharSequence,
    title: CharSequence? = null,
    confirmText: CharSequence = DialogOptions.defaultConfirmText,
    cancelText: CharSequence = DialogOptions.defaultCancelText ?: "取消",
    onConfirm: () -> Unit,
): AlertDialog? = showDialog {
    this.title = title
    this.message = message
    confirm(confirmText, onConfirm)
    cancel(cancelText)
}

/** 列表选择:点击某项即关闭并回调下标与内容 */
fun Context.showItems(
    title: CharSequence? = null,
    items: List<CharSequence>,
    onSelect: (index: Int, item: CharSequence) -> Unit,
): AlertDialog? {
    val host = findActivity() ?: return null
    return presentList(host) {
        setTitle(title)
        setItems(items.toTypedArray()) { dialog, which ->
            dialog.dismissSafely()
            onSelect(which, items[which])
        }
    }
}

/** 单选:点击即选中并关闭 */
fun Context.showSingleChoice(
    title: CharSequence? = null,
    items: List<CharSequence>,
    checkedIndex: Int = -1,
    onSelect: (index: Int, item: CharSequence) -> Unit,
): AlertDialog? {
    val host = findActivity() ?: return null
    return presentList(host) {
        setTitle(title)
        setSingleChoiceItems(items.toTypedArray(), checkedIndex) { dialog, which ->
            dialog.dismissSafely()
            onSelect(which, items[which])
        }
    }
}

/** 多选:点确定才关闭,[onConfirm] 给出选中下标与选中内容 */
fun Context.showMultiChoice(
    title: CharSequence? = null,
    items: List<CharSequence>,
    checked: Set<Int> = emptySet(),
    confirmText: CharSequence = DialogOptions.defaultConfirmText,
    cancelText: CharSequence = DialogOptions.defaultCancelText ?: "取消",
    onConfirm: (indices: IntArray, selected: List<CharSequence>) -> Unit,
): AlertDialog? {
    val host = findActivity() ?: return null
    val states = BooleanArray(items.size) { it in checked }
    return presentList(host) {
        setTitle(title)
        setMultiChoiceItems(items.toTypedArray(), states) { _, which, isChecked -> states[which] = isChecked }
        setNegativeButton(cancelText, null)
        setPositiveButton(confirmText) { dialog, _ ->
            val indices = states.indices.filter { states[it] }.toIntArray()
            dialog.dismissSafely()
            onConfirm(indices, indices.map { items[it] })
        }
    }
}

// ==== Fragment 侧同一套 API(窗口仍挂在宿主 Activity 上) ====

fun Fragment.showDialog(block: DialogOptions.() -> Unit): AlertDialog? =
    requireContext().showDialog(block)

fun Fragment.showCustomDialog(block: DialogOptions.() -> Unit): CustomDialog? =
    requireContext().showCustomDialog(block)

fun Fragment.showTip(
    message: CharSequence,
    title: CharSequence? = null,
    confirmText: CharSequence = DialogOptions.defaultConfirmText,
    onConfirm: (() -> Unit)? = null,
): AlertDialog? = requireContext().showTip(message, title, confirmText, onConfirm)

fun Fragment.showConfirm(
    message: CharSequence,
    title: CharSequence? = null,
    confirmText: CharSequence = DialogOptions.defaultConfirmText,
    cancelText: CharSequence = DialogOptions.defaultCancelText ?: "取消",
    onConfirm: () -> Unit,
): AlertDialog? = requireContext().showConfirm(message, title, confirmText, cancelText, onConfirm)

fun Fragment.showItems(
    title: CharSequence? = null,
    items: List<CharSequence>,
    onSelect: (index: Int, item: CharSequence) -> Unit,
): AlertDialog? = requireContext().showItems(title, items, onSelect)

// ==== 通用小工具 ====

/** 安全关闭:不是 Dialog、没在显示、ROM 抽风,统统吞掉 */
fun DialogInterface?.dismissSafely() {
    val dialog = this as? Dialog ?: return
    if (!dialog.isShowing) return
    runCatching { dialog.dismiss() }
}

/** 从任意 Context(Activity / Fragment 的 context / View 的 context)往上捞出宿主 Activity */
internal fun Context.findActivity(): FragmentActivity? {
    var ctx: Context = this
    while (ctx is ContextWrapper) {
        if (ctx is FragmentActivity) return ctx
        ctx = ctx.baseContext ?: return null
    }
    return null
}

/** 页面销毁时自动关闭弹框,避免 WindowLeaked 与"回调打在死页面上" */
private fun FragmentActivity.attachAndAutoDismiss(dialog: Dialog) {
    lifecycle.addObserver(LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_DESTROY) dialog.dismissSafely()
    })
}

/** 列表 / 单选 / 多选三兄弟的公共装配 */
private fun Context.presentList(
    host: FragmentActivity,
    configure: AlertDialog.Builder.() -> Unit,
): AlertDialog? {
    if (host.isFinishing || host.isDestroyed) return null
    val dialog = AlertDialog.Builder(host).apply(configure).create()
    host.attachAndAutoDismiss(dialog)
    dialog.show()
    return dialog
}

/** 接管 AlertDialog 按钮点击,把"要不要关"的决定权交回业务 */
private fun AlertDialog.takeOverButton(which: Int, action: () -> Unit) {
    getButton(which)?.setOnClickListener { action() }
}

/**
 * 输入法弹出时顶起弹框,并主动把键盘弹出来。
 * ADJUST_RESIZE 在 Activity 上因 edge-to-edge 被废弃,但 Dialog 是独立窗口,
 * 这里仍是各版本表现最一致、成本最低的写法。
 */
@Suppress("DEPRECATION")
private fun Dialog.showKeyboardAndResize() {
    window?.setSoftInputMode(
        WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE or
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE,
    )
}
