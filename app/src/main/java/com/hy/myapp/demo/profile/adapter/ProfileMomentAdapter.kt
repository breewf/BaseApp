package com.hy.myapp.demo.profile.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.chad.library.adapter4.BaseQuickAdapter
import com.chad.library.adapter4.viewholder.QuickViewHolder
import com.hy.basic.utils.ImageLoader
import com.hy.myapp.R

/**
 * desc: 动态时间线适配器(BRVAH4,同列表演示页写法)
 *
 * 配图是可选的:[Moment.imageModel] 为 null 时隐藏图片区(复用池里的 View 必须显式复位,
 * 否则上一个 item 的图会"残留"到无图 item 上 —— 列表复用的经典坑)。
 */
class ProfileMomentAdapter : BaseQuickAdapter<Moment, QuickViewHolder>() {

    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int) =
        QuickViewHolder(R.layout.item_profile_moment, parent)

    override fun onBindViewHolder(holder: QuickViewHolder, position: Int, item: Moment?) {
        if (item == null) return
        with(holder) {
            setText(R.id.tvTime, item.time)
            setText(R.id.tvContent, item.content)
            val iv = getView<ImageView>(R.id.ivImage)
            if (item.imageModel != null) {
                iv.visibility = View.VISIBLE
                ImageLoader.loadRoundImage(iv, item.imageModel, radiusDp = 8)
            } else {
                iv.visibility = View.GONE
            }
        }
    }
}

/** 一条动态:[imageModel] 支持网络 url 与本地资源 id,空则纯文字动态 */
data class Moment(
    val time: String,
    val content: String,
    val imageModel: Any? = null,
)
