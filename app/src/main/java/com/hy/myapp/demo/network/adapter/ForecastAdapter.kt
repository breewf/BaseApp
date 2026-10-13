package com.hy.myapp.demo.network.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hy.myapp.databinding.ItemWeatherForecastBinding
import com.hy.myapp.demo.network.DailyUi

/**
 * desc: 逐日预报列表(ViewBinding 版,不依赖 BRVAH)
 * 数据一次性设置,直接 notifyDataSetChanged;如后续有大量增量更新再换 DiffUtil。
 */
class ForecastAdapter : RecyclerView.Adapter<ForecastAdapter.DayViewHolder>() {

    private val items = mutableListOf<DailyUi>()

    fun submitList(list: List<DailyUi>) {
        items.clear()
        items += list
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        DayViewHolder(ItemWeatherForecastBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) = holder.bind(items[position])

    class DayViewHolder(private val binding: ItemWeatherForecastBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: DailyUi) = with(binding) {
            tvDate.text = item.date
            tvWeekday.text = item.weekday
            tvIcon.text = item.icon
            tvDescription.text = item.description
            tvRange.text = item.range
        }
    }
}
