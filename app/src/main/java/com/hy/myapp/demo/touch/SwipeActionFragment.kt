package com.hy.myapp.demo.touch

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import com.google.android.material.snackbar.Snackbar
import com.hy.basic.BaseFragment
import com.hy.basic.BaseViewModel
import com.hy.basic.hint.snackbar
import com.hy.basic.utils.ItemTouchConfig
import com.hy.basic.utils.SwipeAction
import com.hy.basic.utils.SwipeRevealController
import com.hy.basic.utils.addDivider
import com.hy.basic.utils.attachAdapter
import com.hy.basic.utils.resolvePagerConflict
import com.hy.basic.utils.setupItemTouch
import com.hy.basic.utils.setupLinear
import com.hy.basic.utils.setupSwipeReveal
import com.hy.myapp.R
import com.hy.myapp.databinding.FragmentTouchSwipeBinding
import com.hy.myapp.demo.touch.adapter.TaskAdapter

/**
 * desc: 侧滑操作页:长按拖拽 + 左滑露出操作项 + 点击。
 *
 * 操作项的文字、颜色和点击都在 [SwipeAction] 里配置;收起动画由 [SwipeRevealController] 负责。
 */
class SwipeActionFragment : BaseFragment<FragmentTouchSwipeBinding, BaseViewModel>() {

    private val adapter by lazy { TaskAdapter() }

    private var touchHelper: ItemTouchHelper? = null

    private var swipeController: SwipeRevealController? = null

    private var dragEnabled = true

    private var swipeEnabled = true

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentTouchSwipeBinding.inflate(inflater, container, false)

    override fun initView(savedInstanceState: Bundle?) {
        setupList()
        setupButtons()
    }

    /** 列表:拖拽先挂,侧滑随后;操作项只给文案、颜色和点击 */
    private fun setupList() {
        binding.rvTasks
            .setupLinear()
            .addDivider()
            // 侧滑和 pager 同向,按下就禁掉 pager,否则滑不出操作区
            .resolvePagerConflict(blockPagerOnDown = true)
            .attachAdapter(adapter)
        // 拖拽要先挂:手势检测排在侧滑前面,才收得到 MOVE 并取消长按
        touchHelper = binding.rvTasks.setupItemTouch(
            adapter = adapter,
            config = ItemTouchConfig(
                swipeEnabled = false,
                // 长按时现查:拖拽开关关了,或侧滑展开/跟手中,都不允许拖
                isLocked = { !dragEnabled || swipeController?.isDragLocked() == true },
                onDragStateChanged = { swipeController?.dragInProgress = it },
            ),
        )
        swipeController = binding.rvTasks.setupSwipeReveal(
            foregroundId = R.id.vSwipeForeground,
            actionsId = R.id.vSwipeActions,
            actions = listOf(
                SwipeAction(
                    label = getString(R.string.demo_touch_swipe_note),
                    backgroundColor = ContextCompat.getColor(requireContext(), R.color.colorPrimary),
                ) { position ->
                    adapter.items.getOrNull(position)?.let { showToast("备注「${it.title}」：仅做演示") }
                },
                SwipeAction(
                    label = getString(R.string.demo_touch_swipe_delete),
                    backgroundColor = ContextCompat.getColor(requireContext(), R.color.demo_swipe_delete_bg),
                ) { position ->
                    adapter.items.getOrNull(position)?.let { removeTask(it) }
                },
            ),
            onItemClick = { position ->
                adapter.items.getOrNull(position)?.let { showToast("点击了「${it.title}」") }
            },
        )
        adapter.submitList(TouchFakeData.tasks())
    }

    /** 顶部两个开关 + 恢复默认 */
    private fun setupButtons() {
        binding.btnDrag.setOnClickListener {
            dragEnabled = !dragEnabled
            applyToggles()
        }
        binding.btnSwipe.setOnClickListener {
            swipeEnabled = !swipeEnabled
            applyToggles()
        }
        binding.btnReset.setOnClickListener {
            swipeController?.close(animate = false)
            adapter.submitList(TouchFakeData.tasks())
            showToast("已恢复默认数据")
        }
        applyToggles()
    }

    /** 按 id 找回位置再删;收起动画期间 position 可能已经变了 */
    private fun removeTask(item: TaskItem) {
        val index = adapter.items.indexOfFirst { it.id == item.id }
        if (index < 0) return
        adapter.removeAt(index)
        snackbar("已删除「${item.title}」", "撤销", Snackbar.LENGTH_LONG) {
            adapter.add(index.coerceAtMost(adapter.itemCount), item)
        }
    }

    /** 侧滑开关写控制器;拖拽开关由 isLocked 现查,这里只刷新按钮 */
    private fun applyToggles() {
        swipeController?.enabled = swipeEnabled
        binding.btnDrag.applyToggle(dragEnabled, "拖拽")
        binding.btnSwipe.applyToggle(swipeEnabled, "侧滑")
    }

    /** 开:实底白字;关:描边,按钮本身就是状态灯 */
    private fun TextView.applyToggle(on: Boolean, label: String) {
        text = if (on) "$label：开" else "$label：关"
        setBackgroundResource(
            if (on) R.drawable.demo_bg_button else R.drawable.demo_bg_button_outline,
        )
        setTextColor(ContextCompat.getColor(context, if (on) R.color.white else R.color.colorPrimary))
    }

    override fun onDestroyView() {
        // 不解绑会把旧 RecyclerView 和它的 Context 留到页面销毁之后
        swipeController?.detach()
        swipeController = null
        touchHelper?.attachToRecyclerView(null)
        touchHelper = null
        super.onDestroyView()
    }
}
