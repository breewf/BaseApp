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
            tvEntryDesc.text = entry.desc
            root.setOnClickListener { onClick(entry) }
        }
    }

    private companion object {
        const val TYPE_HEADER = 0
        const val TYPE_ENTRY = 1
    }
}
