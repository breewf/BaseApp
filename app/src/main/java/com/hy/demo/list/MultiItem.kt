package com.hy.demo.list

/**
 * desc: 多类型列表的数据模型 —— 一个密封类承载三种 item。
 *
 * 演示 BRVAH4 多类型:不再靠旧版 MultiItemEntity 接口,而是
 * - 靠密封类子类型给 [com.chad.library.adapter4.BaseMultiItemAdapter.onItemViewType] 返回视图类型(见 [typeOf]);
 * - 每种类型各自注册 onCreate/onBind(见 [com.hy.demo.list.adapter.MultiTypeAdapter])。
 *
 * 三种类型刻意做成"标题 / 图文卡片 / 纯文本便签"差异明显的样式,便于对照不同布局的创建与绑定。
 */
sealed class MultiItem {

    /** 分组标题(整行) */
    data class Header(val title: String) : MultiItem()

    /** 图文卡片(标题 + 摘要 + 封面) */
    data class Article(val title: String, val summary: String, val imageModel: Any) : MultiItem()

    /** 纯文本便签(一段话) */
    data class Note(val content: String) : MultiItem()

    /** 视图类型常量,供 onItemViewType / addItemType 使用 */
    companion object {
        const val TYPE_HEADER = 0
        const val TYPE_ARTICLE = 1
        const val TYPE_NOTE = 2

        /** 由密封类子类型推导视图类型,交给 BaseMultiItemAdapter.onItemViewType 使用 */
        fun typeOf(item: MultiItem?): Int = when (item) {
            is Header -> TYPE_HEADER
            is Article -> TYPE_ARTICLE
            is Note -> TYPE_NOTE
            null -> TYPE_NOTE
        }
    }
}
