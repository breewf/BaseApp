package com.hy.myapp.demo.list.adapter

import android.content.Context
import android.view.ViewGroup
import android.widget.ImageView
import com.chad.library.adapter4.BaseQuickAdapter
import com.chad.library.adapter4.viewholder.QuickViewHolder
import com.hy.basic.utils.ImageLoader
import com.hy.myapp.R
import com.hy.myapp.demo.list.FeedItem

/**
 * desc: 竖向列表适配器(BRVAH4 BaseQuickAdapter + QuickViewHolder)
 *
 * 4.x 写法要点(与 3.x 差异,已按源码核实):
 * - 泛型是 <数据类, ViewHolder类>,不再传 layoutResId 到构造;
 * - onCreate 里用 [QuickViewHolder] 直接绑定 item 布局(它自带 setText/setImageResource/getView 便捷方法);
 * - onBind 三参 (holder, position, item),item 可空需判空。
 * 封面用 [ImageLoader] 加载(网络 url 与本地 resId 同走 model),圆角统一。
 */
class FeedAdapter : BaseQuickAdapter<FeedItem, QuickViewHolder>() {

    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int) =
        QuickViewHolder(R.layout.item_list_feed, parent)

    override fun onBindViewHolder(holder: QuickViewHolder, position: Int, item: FeedItem?) {
        if (item == null) return
        with(holder) {
            setText(R.id.tvTitle, item.title)
            setText(R.id.tvSummary, item.summary)
            setText(R.id.tvTag, item.tag)
            setText(R.id.tvAuthor, "By ${item.author}")
            ImageLoader.loadRoundImage(getView<ImageView>(R.id.ivCover), item.imageModel, radiusDp = 8)
        }
    }
}
