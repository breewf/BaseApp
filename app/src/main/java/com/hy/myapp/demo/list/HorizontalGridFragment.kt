package com.hy.myapp.demo.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.hy.basic.BaseFragment
import com.hy.basic.BaseViewModel
import com.hy.basic.utils.attachAdapter
import com.hy.basic.utils.resolvePagerConflict
import com.hy.basic.utils.setupGrid
import com.hy.basic.utils.setupLinear
import com.hy.myapp.databinding.FragmentListHgridBinding
import com.hy.myapp.demo.list.adapter.CellAdapter

/**
 * desc: 横向列表 + 网格列表页 —— 同一份数据、同一适配器,换 LayoutManager 即变换排布。
 *
 * 演示"排布由 LayoutManager 决定,与适配器解耦"这一 RecyclerView 核心设计:
 * - 上半区 [binding.rvHorizontal]:`setupLinear(HORIZONTAL)`,单行横向滑动;
 * - 下半区 [binding.rvGrid]:`setupGrid(4)`,竖向四列网格,自身可滚动。
 *
 * 数据为本地假数据,无网络/刷新需求,ViewModel 直接用 [BaseViewModel]。
 */
class HorizontalGridFragment :
    BaseFragment<FragmentListHgridBinding, BaseViewModel>() {

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentListHgridBinding.inflate(inflater, container, false)

    override fun initView(savedInstanceState: Bundle?) {
        val cells = ListFakeData.cells()
        binding.rvHorizontal
            .setupLinear(orientation = LinearLayoutManager.HORIZONTAL)
            .resolvePagerConflict()
            .attachAdapter(CellAdapter().apply { submitList(cells) })
        binding.rvGrid
            .setupGrid(spanCount = 4)
            .resolvePagerConflict()
            .attachAdapter(CellAdapter().apply { submitList(cells) })
    }
}
