package com.hy.myapp.demo.oilprice.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hy.myapp.databinding.ItemOilPriceBinding
import com.hy.myapp.demo.oilprice.OilPriceRowUi

/**
 * desc: 油价列表(ViewBinding 版,不依赖 BRVAH)
 * 数据一次性设置,直接 notifyDataSetChanged;如后续有大量增量更新再换 DiffUtil。
 */
class OilPriceAdapter : RecyclerView.Adapter<OilPriceAdapter.PriceViewHolder>() {

    private val items = mutableListOf<OilPriceRowUi>()

    fun submitList(list: List<OilPriceRowUi>) {
        items.clear()
        items += list
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        PriceViewHolder(ItemOilPriceBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: PriceViewHolder, position: Int) = holder.bind(items[position])

    class PriceViewHolder(private val binding: ItemOilPriceBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: OilPriceRowUi) = with(binding) {
            tvName.text = item.name
            tvP92.text = item.p92
            tvP95.text = item.p95
            tvP98.text = item.p98
            tvP0.text = item.p0
        }
    }
}
