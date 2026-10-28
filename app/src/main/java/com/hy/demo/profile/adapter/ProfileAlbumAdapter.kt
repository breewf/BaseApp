package com.hy.demo.profile.adapter

import android.content.Context
import android.view.ViewGroup
import android.widget.ImageView
import com.chad.library.adapter4.BaseQuickAdapter
import com.chad.library.adapter4.viewholder.QuickViewHolder
import com.hy.basic.utils.ImageLoader
import com.hy.myapp.R

/**
 * desc: 相册九宫格适配器 —— model 直接透传 [ImageLoader](网络 url 与本地 resId 混排)
 */
class ProfileAlbumAdapter : BaseQuickAdapter<Any, QuickViewHolder>() {

    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int) =
        QuickViewHolder(R.layout.item_profile_album, parent)

    override fun onBindViewHolder(holder: QuickViewHolder, position: Int, item: Any?) {
        if (item == null) return
        ImageLoader.loadRoundImage(holder.getView<ImageView>(R.id.ivAlbum), item, radiusDp = 4)
    }
}
