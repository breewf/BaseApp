@file:JvmName("VehicleViewFocusExt")

package com.hy.basic.vehicle.input

import android.view.View
import android.view.ViewGroup

/**
 * desc: 车机 D-pad / 旋钮焦点操作的小封装。
 *
 * 车机大量用方向键和确认键,页面切换、Tab 切换后焦点容易丢。
 * 这里提供安全的请求焦点和焦点 id 记忆/恢复,复杂焦点链仍交给 XML 的 nextFocus* 配置。
 *
 * 注意:
 * - 隐藏页面不要抢焦点,所以 requestFocus 前先判 isShown;
 * - restoreFocus 找不到原 View 时会回退到第一个可聚焦子 View;
 * - 焦点高亮样式由主题和 selector 控制,这里只管焦点落在哪。
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
