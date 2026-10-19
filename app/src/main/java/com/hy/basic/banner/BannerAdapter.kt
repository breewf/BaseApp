package com.hy.basic.banner

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import com.hy.basic.utils.ImageLoader

/**
 * desc: Banner 页适配器。循环做法:itemCount = 数据量 × LOOP_SPAN,初始落在中间,
 * 左右都能无限翻,对外统一用 realPosition 折算回真实下标。
 *
 * 两种页内容模式(Banner 内部按是否传布局资源自动选择):
 * - 图片模式:每页一个 ImageView,数据是 url 或资源 id
 * - 自定义模式:每页 inflate 一个 itemLayoutRes,在 onBind 里回填
 */
internal class BannerAdapter internal constructor() : RecyclerView.Adapter<BannerAdapter.PageHolder>() {

    /** 每页数据,url(String) 或资源 id(Int),自定义模式下可以是任意对象 */
    var items: List<Any?> = emptyList()
        set(value) {
            val changed = field !== value
            field = value
            if (changed) notifyDataSetChanged()
        }

    /** 是否无限循环。数据量 <= 1 时自动退化为不循环 */
    var loop: Boolean = true
        set(value) {
            if (field == value) return
            field = value
            notifyDataSetChanged()
        }

    /** 自定义页布局;0 表示走内置图片模式 */
    @LayoutRes
    var itemLayoutRes: Int = 0
        set(value) {
            if (field == value) return
            field = value
            notifyDataSetChanged()
        }

    /** 回填自定义页内容。[position] 已是折算后的真实下标 */
    var onBind: ((page: View, item: Any?, position: Int) -> Unit)? = null

    /** 点击某一页。[position] 是真实下标 */
    var onPageClick: ((position: Int, item: Any?) -> Unit)? = null

    /** 图片模式下 ImageView 的缩放方式 */
    var imageScaleType: ImageView.ScaleType = ImageView.ScaleType.CENTER_CROP

    /**
     * 每页内容左右内缩(px)。页本身仍是满宽(保证 snap 对齐不变),
     * 露出来的那一段由相邻页平移填上,得到“两边也能看到卡片”的效果。
     */
    var pagePadding: Int = 0

    /** 数据量 */
    val realItemCount: Int get() = items.size

    /** 是否真的在循环(数据太少时循环没意义) */
    private val looping get() = loop && items.size > 1

    /** 虚拟页总数 */
    override fun getItemCount(): Int = if (looping) items.size * LOOP_SPAN else items.size

    /** 虚拟下标 -> 真实下标 */
    fun realPosition(virtualPosition: Int): Int =
        if (items.isEmpty()) 0 else virtualPosition % items.size

    /** 起始虚拟下标:取正中间那一格,保证两侧都有足够页可翻 */
    fun startVirtualPosition(): Int = if (looping) items.size * (LOOP_SPAN / 2) else 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageHolder {
        // ViewPager2 要求每页铺满,这里直接用 MATCH_PARENT 的容器兜住两种模式
        val container = FrameLayout(parent.context).apply {
            layoutParams = RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT,
            )
            // 露边靠这层内缩,不改页宽,所以 snap / 手势都不受影响
            setPadding(pagePadding, 0, pagePadding, 0)
            isClickable = true
        }
        val image = if (itemLayoutRes == 0) {
            ImageView(parent.context).apply {
                layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT,
                )
                scaleType = imageScaleType
            }.also { container.addView(it) }
        } else {
            LayoutInflater.from(parent.context).inflate(itemLayoutRes, container, true)
            null
        }
        return PageHolder(container, image)
    }

    override fun onBindViewHolder(holder: PageHolder, position: Int) {
        val real = realPosition(position)
        val item = items.getOrNull(real)
        if (holder.imageView != null) {
            holder.imageView.scaleType = imageScaleType
            ImageLoader.loadImage(holder.imageView, item)
        }
        onBind?.invoke(holder.itemView, item, real)
        holder.itemView.setOnClickListener { onPageClick?.invoke(real, item) }
    }

    /** 页容器。imageView 只在图片模式下非空,bind 阶段只换图不重建 */
    class PageHolder internal constructor(
        container: FrameLayout,
        val imageView: ImageView?,
    ) : RecyclerView.ViewHolder(container)

    private companion object {
        /** 循环倍数:够大到用户翻不完,又不至于让 int 下标溢出 */
        const val LOOP_SPAN = 100
    }
}
