package com.hy.basic.utils

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.viewpager2.widget.ViewPager2
import kotlin.math.abs

/**
 * desc: RecyclerView 装配扩展 —— 四种布局形态一行搞定
 *
 * 列表页样板代码(LayoutManager 构造 + adapter 挂载 + 分割线 + 嵌套滚动开关)收口在这里,
 * 页面只声明"我要什么形态的列表"。全部返回自身,支持链式:
 * ```
 * rv.setupLinear().addDivider().attachAdapter(adapter)
 * ```
 */

/** 线性布局:默认竖向,[orientation] 传 [LinearLayoutManager.HORIZONTAL] 即横向列表 */
@SuppressLint("WrongConstant")
fun RecyclerView.setupLinear(
    orientation: Int = LinearLayoutManager.VERTICAL,
    reverseLayout: Boolean = false,
): RecyclerView = apply {
    layoutManager = LinearLayoutManager(context, orientation, reverseLayout)
}

/** 网格布局:[spanCount] 列(横向滚动时是 [spanCount] 行) */
@SuppressLint("WrongConstant")
fun RecyclerView.setupGrid(
    spanCount: Int,
    orientation: Int = GridLayoutManager.VERTICAL,
): RecyclerView = apply {
    layoutManager = GridLayoutManager(context, spanCount, orientation, false)
}

/** 瀑布流布局:[spanCount] 列,item 高度由内容/代码决定,天然错落 */
fun RecyclerView.setupWaterfall(
    spanCount: Int,
    orientation: Int = StaggeredGridLayoutManager.VERTICAL,
    gapStrategy: Int = StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS,
): RecyclerView = apply {
    layoutManager = StaggeredGridLayoutManager(spanCount, orientation).apply {
        this.gapStrategy = gapStrategy
    }
}

/** 挂载适配器并返回它(方便持有引用后续 submitList) */
fun <A : RecyclerView.Adapter<*>> RecyclerView.attachAdapter(adapter: A): A {
    this.adapter = adapter
    return adapter
}

/** 加分割线:竖向列表配 [DividerItemDecoration.VERTICAL](默认),横向配 HORIZONTAL */
fun RecyclerView.addDivider(orientation: Int = DividerItemDecoration.VERTICAL): RecyclerView = apply {
    addItemDecoration(DividerItemDecoration(context, orientation))
}

/**
 * 关闭自身嵌套滚动:网格/瀑布流嵌在 ScrollView / NestedScrollView 里时必须调用,
 * 否则外层滚动抢事件、内层高度计算也异常(此时 item 高度需 wrap 全量铺开)。
 */
fun RecyclerView.disableInnerScroll(): RecyclerView = apply {
    isNestedScrollingEnabled = false
}

/**
 * 解决列表与外层 [ViewPager2] 的滑动冲突(Tab 页内列表常遇):
 *
 * ViewPager2 内部的 RecyclerView 覆写了 requestDisallowInterceptTouchEvent 强制允许拦截,
 * 所以"子 View 请求父容器不要拦截"的老办法对它无效;可行的办法是在手指按住列表期间临时关掉
 * pager 的手势输入(isUserInputEnabled),抬起再恢复。
 * - 横向列表(仅横向可滚,与 pager 同轴):手指一落列表就禁用 pager,避免横滑被抢去切页;
 * - 竖向列表/网格/瀑布流:方向感知——首个越界移动若竖向为主(dy>=dx,含大角度斜滑)则禁用 pager,
 *   纯横向滑动仍交给 pager 切页,不误伤"左右滑切 Tab"。
 *
 * pager 在每次触摸时沿父链查找,不依赖 View 是否已挂到 pager 上;找不到 pager 则不做任何处理。
 * 需在 `setupXxx()` 设好 LayoutManager 之后调用(方向在触摸时才读,故调用时机不敏感)。
 *
 * @param blockPagerOnDown true = 手指一落到列表就关掉 pager 手势。列表里有横向侧滑删除、
 *   或网格支持四向拖拽时必须打开:这两种手势和 pager 同轴,等方向判定来不及,pager 会先抢走。
 */
@SuppressLint("ClickableViewAccessibility")
fun RecyclerView.resolvePagerConflict(blockPagerOnDown: Boolean = false): RecyclerView = apply {
    val slop = ViewConfiguration.get(context).scaledTouchSlop
    var downX = 0f
    var downY = 0f
    var decided = false

    fun handle(e: MotionEvent) {
        when (e.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downX = e.rawX
                downY = e.rawY
                decided = false
                if (blockPagerOnDown || isHorizontalOnly()) {
                    findParentPager()?.isUserInputEnabled = false
                }
            }

            MotionEvent.ACTION_MOVE -> {
                if (!blockPagerOnDown && !decided) {
                    val dx = abs(e.rawX - downX)
                    val dy = abs(e.rawY - downY)
                    if (dx > slop || dy > slop) {
                        decided = true
                        // 竖向为主(含斜滑)才禁用;纯横向保留 pager 切页
                        if (!isHorizontalOnly() && dy >= dx) {
                            findParentPager()?.isUserInputEnabled = false
                        }
                    }
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL ->
                findParentPager()?.isUserInputEnabled = true
        }
    }

    // 拦截链路:item 可点击时也能收到 DOWN,起手就禁用 pager
    addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
        override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
            handle(e)
            // 只读不消费,让列表/子 View 正常滚动和点击
            return false
        }

        override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) = Unit

        override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) = Unit
    })

    // 收尾通道:ItemTouchHelper 接管后拦截链路收不到 UP,由这里恢复 pager
    setOnTouchListener { _, e ->
        handle(e)
        false
    }
}

/** 是否仅横向可滚(横向列表),用于区分与 pager 同轴的冲突场景 */
private fun RecyclerView.isHorizontalOnly(): Boolean {
    val lm = layoutManager ?: return false
    return lm.canScrollHorizontally() && !lm.canScrollVertically()
}

/** 沿父链查找承载本 View 的 [ViewPager2](Fragment 根视图到 pager 之间层级不固定) */
private fun View.findParentPager(): ViewPager2? {
    var p = parent
    while (p != null) {
        if (p is ViewPager2) return p
        p = p.parent
    }
    return null
}
