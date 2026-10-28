package com.hy.demo.model

import android.app.Activity

/**
 * desc: 测试总入口的菜单模型
 *
 * 新增测试页只需在 [com.hy.demo.model.DemoMenu] 里加一行,无需改布局与 Activity。
 */

/** 单个测试入口:[action] 优先(页内动作),否则跳 [target] 页面 */
data class DemoEntry(
    val title: String,
    val desc: String,
    val target: Class<out Activity>? = null,
    val action: (() -> Unit)? = null,
)

/** 测试分类 */
data class DemoGroup(val title: String, val entries: List<DemoEntry>)

/** 列表行:分组标题 / 测试入口 */
sealed class DemoRow {

    data class Header(val title: String) : DemoRow()

    data class Entry(val entry: DemoEntry) : DemoRow()
}
