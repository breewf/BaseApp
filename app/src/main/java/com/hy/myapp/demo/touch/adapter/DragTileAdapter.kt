package com.hy.myapp.demo.touch.adapter

import android.content.Context
import android.content.res.ColorStateList
import android.view.ViewGroup
import android.widget.TextView
import com.chad.library.adapter4.BaseQuickAdapter
import com.chad.library.adapter4.viewholder.QuickViewHolder
import com.hy.myapp.R
import com.hy.myapp.demo.touch.DragTile

/** 横向色块(BRVAH4)。圆角底在 XML,这里只换颜色;拖动时整行由 ItemTouchHelper 平移。 */
class DragTileAdapter : BaseQuickAdapter<DragTile, QuickViewHolder>() {

    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int) =
        QuickViewHolder(R.layout.item_touch_tile, parent)

    override fun onBindViewHolder(holder: QuickViewHolder, position: Int, item: DragTile?) {
        if (item == null) return
        val tile = holder.getView<TextView>(R.id.tvTile)
        tile.text = item.label
        tile.backgroundTintList = ColorStateList.valueOf(item.colorArgb)
    }
}
