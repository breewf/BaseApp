package com.hy.demo.list.adapter

import android.content.Context
import android.view.ViewGroup
import android.widget.ImageView
import com.chad.library.adapter4.BaseQuickAdapter
import com.chad.library.adapter4.viewholder.QuickViewHolder
import com.hy.basic.utils.ImageLoader
import com.hy.myapp.R
import com.hy.demo.list.CellItem

/**
 * desc: 横向 & 网格共用适配器(BRVAH4)
 *
 * 同一份数据、同一个 item 布局,[com.hy.demo.list.HorizontalGridFragment]
 * 里给两个 RecyclerView 各挂一个实例,分别设 LinearManager(横向) / GridManager(3 列),
 * 演示"排布由 LayoutManager 决定,与适配器解耦"。
 */
class CellAdapter : BaseQuickAdapter<CellItem, QuickViewHolder>() {

    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int) =
        QuickViewHolder(R.layout.item_list_cell, parent)

    override fun onBindViewHolder(holder: QuickViewHolder, position: Int, item: CellItem?) {
        if (item == null) return
        with(holder) {
            setText(R.id.tvLabel, item.label)
            ImageLoader.loadRoundImage(getView<ImageView>(R.id.ivCell), item.imageModel, radiusDp = 10)
        }
    }
}
