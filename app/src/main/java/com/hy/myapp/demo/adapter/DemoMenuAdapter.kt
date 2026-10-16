package com.hy.myapp.demo.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hy.myapp.databinding.ItemDemoEntryBinding
import com.hy.myapp.databinding.ItemDemoHeaderBinding
import com.hy.myapp.demo.model.DemoEntry
import com.hy.myapp.demo.model.DemoRow

/**
 * desc: 测试总入口列表(分组标题 + 入口卡片,两种 ItemType)
 *
 * 列表挂在两列 GridLayoutManager 上,跨列规则由 [isHeaderAt] 提供:
 * 标题行跨满整行,入口卡片各占一列。卡片高度对齐靠描述固定两行
 * (不足补空白、超长截断),同排卡片视觉等高。
 */
class DemoMenuAdapter(
    private val onEntryClick: (DemoEntry) -> Unit,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val rows = mutableListOf<DemoRow>()

    fun submitList(list: List<DemoRow>) {
        rows.clear()
        rows += list
        notifyDataSetChanged()
    }

    /** 供 GridLayoutManager.SpanSizeLookup 判断该行是否跨满整行 */
    fun isHeaderAt(position: Int) = rows.getOrNull(position) is DemoRow.Header

    override fun getItemViewType(position: Int) = when (rows[position]) {
        is DemoRow.Header -> TYPE_HEADER
        is DemoRow.Entry -> TYPE_ENTRY
    }

    override fun getItemCount(): Int = rows.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_HEADER -> HeaderViewHolder(ItemDemoHeaderBinding.inflate(inflater, parent, false))
            else -> EntryViewHolder(ItemDemoEntryBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val row = rows[position]) {
            is DemoRow.Header -> (holder as HeaderViewHolder).bind(row.title)
            is DemoRow.Entry -> (holder as EntryViewHolder).bind(row.entry, onEntryClick)
        }
    }

    class HeaderViewHolder(private val binding: ItemDemoHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(title: String) {
            binding.tvGroupTitle.text = title
        }
    }

    class EntryViewHolder(private val binding: ItemDemoEntryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(entry: DemoEntry, onClick: (DemoEntry) -> Unit) = with(binding) {
            tvEntryTitle.text = entry.title
            // 先按实际内容收行数,再抬到最低两行:单行描述补白、两行描述占满,
            // 保证同排两张卡片等高;超过两行由 XML 的 maxLines=2 截断
            tvEntryDesc.minLines = 1
            tvEntryDesc.text = entry.desc
            tvEntryDesc.minLines = 2
            root.setOnClickListener { onClick(entry) }
        }
    }

    private companion object {
        const val TYPE_HEADER = 0
        const val TYPE_ENTRY = 1
    }
}
