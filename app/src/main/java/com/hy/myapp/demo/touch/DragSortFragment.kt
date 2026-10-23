package com.hy.myapp.demo.touch

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.hy.basic.BaseFragment
import com.hy.basic.BaseViewModel
import com.hy.basic.utils.ItemTouchConfig
import com.hy.basic.utils.attachAdapter
import com.hy.basic.utils.disableInnerScroll
import com.hy.basic.utils.resolvePagerConflict
import com.hy.basic.utils.setupDragSort
import com.hy.basic.utils.setupGrid
import com.hy.basic.utils.setupItemTouch
import com.hy.basic.utils.setupLinear
import com.hy.myapp.databinding.FragmentTouchDragBinding
import com.hy.myapp.demo.touch.adapter.DragTileAdapter
import com.hy.myapp.demo.touch.adapter.NineGridAdapter

/**
 * desc: 拖拽排序页:横向色块 + 九宫格图片。
 *
 * 拖拽方向由 LayoutManager 推断;九宫格末尾的「＋」用 [ItemTouchConfig.isLocked] 固定。
 */
class DragSortFragment : BaseFragment<FragmentTouchDragBinding, BaseViewModel>() {

    private val tileAdapter by lazy { DragTileAdapter() }

    private val gridAdapter by lazy { NineGridAdapter(TouchFakeData.imagePool) }

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentTouchDragBinding.inflate(inflater, container, false)

    override fun initView(savedInstanceState: Bundle?) {
        initTileList()
        initGrid()
    }

    /** 横向色块,拖拽方向由 LayoutManager 决定(左右) */
    private fun initTileList() {
        binding.rvTiles
            .setupLinear(orientation = LinearLayoutManager.HORIZONTAL)
            .resolvePagerConflict()
            .attachAdapter(tileAdapter)
        tileAdapter.setOnItemClickListener { _, _, position ->
            tileAdapter.items.getOrNull(position)?.let {
                showToast("点击了「${it.label}」，position:$position，长按可拖动")
            }
        }
        tileAdapter.submitList(TouchFakeData.tiles())
        binding.rvTiles.setupDragSort(tileAdapter)
    }

    /** 九宫格,末尾「＋」固定;网格在滚动容器里,要关掉内部滚动 */
    private fun initGrid() {
        binding.rvGrid
            .setupGrid(spanCount = 3)
            .disableInnerScroll()
            // 网格四向都能拖,横滑不能交给 pager 切页
            .resolvePagerConflict(blockPagerOnDown = true)
            .attachAdapter(gridAdapter)
        gridAdapter.submitList(TouchFakeData.gridImages())
        binding.rvGrid.setupItemTouch(
            adapter = gridAdapter,
            config = ItemTouchConfig(
                swipeEnabled = false,
                isLocked = { position -> gridAdapter.items.getOrNull(position)?.isAdd == true },
            ),
        )
        gridAdapter.setOnItemClickListener { _, _, position ->
            val item = gridAdapter.items.getOrNull(position)
            when {
                item == null -> Unit
                item.isAdd -> {
                    gridAdapter.addImage()
                    showToast("已添加一张,末尾「＋」保持固定")
                }
                else -> showToast("点击了第 $position 张,长按可拖动排序")
            }
        }
    }
}
