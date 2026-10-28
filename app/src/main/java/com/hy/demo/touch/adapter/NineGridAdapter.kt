package com.hy.demo.touch.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter4.BaseQuickAdapter
import com.chad.library.adapter4.viewholder.QuickViewHolder
import com.hy.basic.utils.ImageLoader
import com.hy.myapp.R
import com.hy.demo.touch.GridImage

/** 九宫格图片(BRVAH4)。末尾「＋」用同一布局换显隐,能不能拖由 Fragment 的 isLocked 决定。 */
class NineGridAdapter(private val imagePool: List<Any>) : BaseQuickAdapter<GridImage, QuickViewHolder>() {

    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int) =
        QuickViewHolder(R.layout.item_touch_grid_image, parent)

    override fun onBindViewHolder(holder: QuickViewHolder, position: Int, item: GridImage?) {
        if (item == null) return
        val image = holder.getView<ImageView>(R.id.ivImage)
        val add = holder.getView<TextView>(R.id.tvAdd)
        val delete = holder.getView<TextView>(R.id.tvDelete)

        val isAdd = item.isAdd
        add.visibility = if (isAdd) View.VISIBLE else View.GONE
        image.visibility = if (isAdd) View.GONE else View.VISIBLE
        delete.visibility = if (isAdd) View.GONE else View.VISIBLE
        if (isAdd) return

        ImageLoader.loadRoundImage(image, item.url, radiusDp = 6)
        // 角标自己消费点击,避免同时触发整格点击
        delete.setOnClickListener {
            val pos = holder.bindingAdapterPosition
            if (pos != RecyclerView.NO_POSITION) removeAt(pos)
        }
    }

    /** 在末尾「＋」之前插一张,id 取当前最大值 +1 */
    fun addImage() {
        val insertAt = (itemCount - 1).coerceAtLeast(0)
        val nextId = (items.maxOfOrNull { it.id } ?: 0) + 1
        add(insertAt, GridImage(id = nextId, url = imagePool[nextId % imagePool.size]))
    }
}
