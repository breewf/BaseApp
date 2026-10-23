package com.hy.myapp.demo.touch.adapter

import android.content.Context
import android.content.res.ColorStateList
import android.view.ViewGroup
import android.widget.TextView
import com.chad.library.adapter4.BaseQuickAdapter
import com.chad.library.adapter4.viewholder.QuickViewHolder
import com.hy.myapp.R
import com.hy.myapp.demo.touch.TaskItem

/** 侧滑行(BRVAH4)。操作区和前景分两层,控制器只平移前景。 */
class TaskAdapter : BaseQuickAdapter<TaskItem, QuickViewHolder>() {

    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int) =
        QuickViewHolder(R.layout.item_touch_swipe, parent)

    override fun onBindViewHolder(holder: QuickViewHolder, position: Int, item: TaskItem?) {
        if (item == null) return
        with(holder) {
            setText(R.id.tvBadge, item.id.toString().padStart(2, '0'))
            setText(R.id.tvTitle, item.title)
            setText(R.id.tvDesc, item.desc)
            getView<TextView>(R.id.tvBadge).backgroundTintList =
                ColorStateList.valueOf(item.colorArgb)
        }
    }
}
