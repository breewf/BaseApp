@file:JvmName("VehicleViewFocusExt")

package com.hy.basic.vehicle.input

import android.view.View
import android.view.ViewGroup

/**
 * 车机 D-pad/旋钮焦点操作：安全请求焦点、记录/恢复焦点 id。
 *
 * 用法：
 * `val id = container.focusedChildId()`，`container.restoreFocus(id)`。
 * 注意：只负责焦点落点；焦点高亮和 nextFocus* 链路由主题与 XML 控制。
 */

/** 可见且可聚焦时才请求焦点,避免隐藏页抢焦点 */
fun View.requestFocusSafely(): Boolean =
    isShown && isFocusable && requestFocus()

/** 记录当前子树里的焦点 View id,没有焦点返回 [View.NO_ID] */
fun ViewGroup.focusedChildId(): Int = findFocus()?.id ?: View.NO_ID

/**
 * 恢复焦点。优先按 [childId] 找原 View,找不到时回退到第一个可聚焦子 View。
 * 返回是否成功拿到焦点。
 */
fun ViewGroup.restoreFocus(childId: Int = View.NO_ID): Boolean {
    if (childId != View.NO_ID) {
        findViewById<View>(childId)?.let { view ->
            if (view.requestFocusSafely()) return true
        }
    }
    return requestFocusFirstChild()
}

/** 递归找第一个可见且可聚焦的子 View */
fun ViewGroup.requestFocusFirstChild(): Boolean {
    for (i in 0 until childCount) {
        val child = getChildAt(i)
        if (child.visibility != View.VISIBLE) continue
        if (child.requestFocusSafely()) return true
        if (child is ViewGroup && child.requestFocusFirstChild()) return true
    }
    return false
}
