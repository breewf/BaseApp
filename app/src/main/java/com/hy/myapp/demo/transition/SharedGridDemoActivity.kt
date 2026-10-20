package com.hy.myapp.demo.transition

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hy.basic.BaseActivity
import com.hy.basic.BaseViewModel
import com.hy.basic.transition.SharedTransitionKit
import com.hy.basic.utils.ImageLoader
import com.hy.myapp.databinding.ActivitySharedGridDemoBinding
import com.hy.myapp.databinding.ItemSharedGridBinding

/**
 * desc: 列表 → 详情的共享元素转场。两列网格,点哪格用哪格的唯一 transitionName 飞进大图页。
 */
class SharedGridDemoActivity : BaseActivity<ActivitySharedGridDemoBinding, BaseViewModel>() {

    override val showToolBar: Boolean get() = true
    override val toolbarTitle: CharSequence get() = "共享元素 · 列表"

    private val urls = listOf(
        "https://www.gstatic.com/webp/gallery/1.jpg",
        "https://www.gstatic.com/webp/gallery/2.jpg",
        "https://www.gstatic.com/webp/gallery/3.jpg",
        "https://www.gstatic.com/webp/gallery/4.jpg",
        "https://www.gstatic.com/webp/gallery/5.jpg",
        "https://www.gstatic.com/webp/gallery/1.webp",
    )

    override fun createBinding(inflater: LayoutInflater) =
        ActivitySharedGridDemoBinding.inflate(inflater)

    override fun initView(savedInstanceState: Bundle?) {
        binding.rvGrid.layoutManager = GridLayoutManager(this, 2)
        binding.rvGrid.adapter = GridAdapter(urls) { thumb, url ->
            // thumb 已带唯一 transitionName,直接用视图自身的名字配对
            SharedTransitionKit.start(this, thumb, SharedImageDetailActivity::class, url = url)
        }
    }

    private class GridAdapter(
        private val items: List<String>,
        private val onClick: (ImageView, String) -> Unit,
    ) : RecyclerView.Adapter<GridAdapter.VH>() {

        class VH(val binding: ItemSharedGridBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            VH(ItemSharedGridBinding.inflate(LayoutInflater.from(parent.context), parent, false))

        override fun getItemCount(): Int = items.size

        override fun onBindViewHolder(holder: VH, position: Int) {
            val url = items[position]
            val thumb = holder.binding.ivThumb
            // 复用视图必须每次重设唯一名:全部用同名会让共享元素配对到别的格子
            thumb.transitionName = "grid_${url.hashCode()}"
            ImageLoader.loadImage(thumb, url)
            thumb.setOnClickListener { onClick(thumb, url) }
        }
    }
}
