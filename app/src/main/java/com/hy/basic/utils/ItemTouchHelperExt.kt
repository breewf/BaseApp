package com.hy.basic.utils

import android.graphics.Canvas
import android.graphics.Paint
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter4.BaseQuickAdapter

/**
 * desc: RecyclerView 拖拽排序 / 侧滑删除的封装,配合 BRVAH4 使用。
 *
 * 拖拽:换位交给适配器的 move,数据和界面一起更新;
 * 侧滑:先删数据再回调页面,回调里的 position 是删除前的位置,可以直接插回原位。
 *
 * 拖拽方向跟着 LayoutManager 走:横列表左右、竖列表上下、网格四向;侧滑方向单独配置。
 * 拖拽要长按才触发,侧滑不用等长按,所以两者同向也能共存。
 */

/**
 * 拖拽 / 侧滑配置。
 *
 * @param dragEnabled 是否允许长按拖拽
 * @param swipeEnabled 是否允许侧滑
 * @param swipeDirs 侧滑方向,默认左滑,可以组合 LEFT / RIGHT / UP / DOWN
 * @param swipeThreshold 滑多远才算删除,0~1,越大越难触发
 * @param swipeBackColor 侧滑时露出来的底色,0 表示不画,交给 item 布局自己处理
 * @param isLocked 返回 true 的项不能拖也不能滑,比如九宫格末尾的「＋」;
 *   页面想动态开关拖拽也可以用它:关掉时直接返回 true,不用重建 helper
 * @param onDragStateChanged 拖拽开始/结束的回调,页面还有别的手势时用它让路
 */
class ItemTouchConfig(
    val dragEnabled: Boolean = true,
    val swipeEnabled: Boolean = true,
    val swipeDirs: Int = ItemTouchHelper.LEFT,
    val swipeThreshold: Float = 0.5f,
    val swipeBackColor: Int = 0,
    val isLocked: (position: Int) -> Boolean = { false },
    val onDragStateChanged: ((dragging: Boolean) -> Unit)? = null,
)

/**
 * 给列表挂上拖拽 / 侧滑,返回 helper;需要主动发起拖拽时调它的 startDrag。
 *
 * 注意:非 Diff 模式下 BRVAH4 的 move 直接改 items,所以 submitList 传进来的集合必须是可变的,
 * 否则拖拽时会抛 UnsupportedOperationException。
 *
 * @param onMoved 每次换位后回调(from, to)
 * @param onSwiped 侧滑删除后回调,这时 item 已经从适配器里移除
 */
fun <T : Any> RecyclerView.setupItemTouch(
    adapter: BaseQuickAdapter<T, *>,
    config: ItemTouchConfig = ItemTouchConfig(),
    onMoved: ((from: Int, to: Int) -> Unit)? = null,
    onSwiped: ((item: T, position: Int, direction: Int) -> Unit)? = null,
): ItemTouchHelper {
    val helper = ItemTouchHelper(ItemTouchCallback(adapter, config, onMoved, onSwiped))
    helper.attachToRecyclerView(this)
    return helper
}

/** 只要拖拽时的简写 */
fun <T : Any> RecyclerView.setupDragSort(
    adapter: BaseQuickAdapter<T, *>,
    onMoved: ((from: Int, to: Int) -> Unit)? = null,
): ItemTouchHelper =
    setupItemTouch(adapter, ItemTouchConfig(swipeEnabled = false), onMoved)

private class ItemTouchCallback<T : Any>(
    private val adapter: BaseQuickAdapter<T, *>,
    private val config: ItemTouchConfig,
    private val onMoved: ((from: Int, to: Int) -> Unit)?,
    private val onSwiped: ((item: T, position: Int, direction: Int) -> Unit)?,
) : ItemTouchHelper.Callback() {

    private val backPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var dragging = false

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
    ): Int {
        val position = viewHolder.bindingAdapterPosition
        if (position == RecyclerView.NO_POSITION || config.isLocked(position)) return 0
        val dragFlags = if (config.dragEnabled) dragFlagsOf(recyclerView.layoutManager) else 0
        val swipeFlags = if (config.swipeEnabled) config.swipeDirs else 0
        return makeMovementFlags(dragFlags, swipeFlags)
    }

    override fun isLongPressDragEnabled(): Boolean = config.dragEnabled

    override fun isItemViewSwipeEnabled(): Boolean = config.swipeEnabled

    override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float =
        config.swipeThreshold

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder,
    ): Boolean {
        val from = viewHolder.bindingAdapterPosition
        val to = target.bindingAdapterPosition
        if (from == RecyclerView.NO_POSITION || to == RecyclerView.NO_POSITION) return false
        if (from == to || config.isLocked(to)) return false
        // BRVAH4 的 move 内部已换位并 notifyItemMoved,这里不能再补一次通知
        adapter.move(from, to)
        onMoved?.invoke(from, to)
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.bindingAdapterPosition
        if (position == RecyclerView.NO_POSITION) return
        val item = adapter.items.getOrNull(position) ?: return
        // 先删数据再回调:页面拿到的 position 是删除前的位置,正好用来插回原位
        adapter.removeAt(position)
        onSwiped?.invoke(item, position, direction)
    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
            dragging = true
            config.onDragStateChanged?.invoke(true)
        }
        super.onSelectedChanged(viewHolder, actionState)
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)
        if (dragging) {
            dragging = false
            config.onDragStateChanged?.invoke(false)
        }
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean,
    ) {
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            drawSwipeBack(c, viewHolder.itemView, dX)
        }
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }

    /**
     * 背板画在 item 让出来的那块空位上。
     * ItemTouchHelper 只平移 itemView,item 布局里垫的底色会跟着一起滑走,所以只能在这里画。
     */
    private fun drawSwipeBack(c: Canvas, itemView: View, dX: Float) {
        if (config.swipeBackColor == 0 || dX == 0f) return
        val left = itemView.left.toFloat()
        val top = itemView.top.toFloat()
        val right = itemView.right.toFloat()
        val bottom = itemView.bottom.toFloat()
        // 左滑让出右侧、右滑让出左侧,只在这条区域里画
        val coverLeft = if (dX < 0) (right + dX).coerceAtLeast(left) else left
        val coverRight = if (dX < 0) right else (left + dX).coerceAtMost(right)

        c.save()
        c.clipRect(coverLeft, top, coverRight, bottom)
        backPaint.color = config.swipeBackColor
        c.drawRect(left, top, right, bottom, backPaint)
        c.restore()
    }

    private fun dragFlagsOf(layoutManager: RecyclerView.LayoutManager?): Int =
        when (layoutManager) {
            is GridLayoutManager -> ItemTouchHelper.UP or ItemTouchHelper.DOWN or
                ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
            // 横向列表左右拖,竖向列表上下拖,和滚动同向才不打架
            is LinearLayoutManager ->
                if (layoutManager.orientation == LinearLayoutManager.HORIZONTAL) {
                    ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
                } else {
                    ItemTouchHelper.UP or ItemTouchHelper.DOWN
                }
            // 瀑布流等其它布局不支持拖拽:跨列换位会乱
            else -> 0
        }
}
