package com.hy.demo.list.adapter

import android.content.Context
import android.view.ViewGroup
import android.view.View
import com.chad.library.adapter4.BaseQuickAdapter
import com.chad.library.adapter4.viewholder.QuickViewHolder
import com.hy.myapp.R
import com.hy.demo.list.CardItem

/**
 * desc: 瀑布流卡片适配器(BRVAH4)
 *
 * 错落高度靠两点自然形成:正文 [CardItem.content] 行数不同 + 顶部色块高度按数据微调,
 * 交给 [androidx.recyclerview.widget.StaggeredGridLayoutManager] 摆放,适配器只管绑定。
 */
class WaterfallAdapter : BaseQuickAdapter<CardItem, QuickViewHolder>() {

    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int) =
        QuickViewHolder(R.layout.item_list_card, parent)

    override fun onBindViewHolder(holder: QuickViewHolder, position: Int, item: CardItem?) {
        if (item == null) return
        val density = holder.itemView.resources.displayMetrics.density
        val blockHeightPx = ((56 + (item.id % 4) * 24) * density).toInt()
        with(holder) {
            setText(R.id.tvCardTitle, item.title)
            setText(R.id.tvCardContent, item.content)
            getView<View>(R.id.vBlock).apply {
                layoutParams.height = blockHeightPx
                setBackgroundColor(item.colorArgb)
                requestLayout()
            }
        }
    }
}
