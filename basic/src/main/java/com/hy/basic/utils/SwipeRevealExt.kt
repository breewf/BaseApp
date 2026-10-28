package com.hy.basic.utils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs

/** 单个操作按钮宽度,操作区总宽 = 按钮数 × 该值 */
private const val ACTION_WIDTH_DP = 80

/** 最多支持的操作项数 */
private const val MAX_ACTION_COUNT = 3

/**
 * desc: 一个侧滑操作项:文字、背景色和点击回调写在一起。
 *
 * @param label 按钮文字
 * @param backgroundColor 按钮背景色(8 位 ARGB)
 * @param onAction 点击回调,参数是这一行的位置;执行时收起动画已经播完
 */
class SwipeAction(
    val label: CharSequence,
    @param:ColorInt val backgroundColor: Int,
    val onAction: (position: Int) -> Unit,
)

/**
 * desc: 让列表项左滑露出操作区,松手后停住,不会直接删除;可以和长按拖拽一起用。
 *
 * item 布局要分两层:操作区贴着右边(宽度就是滑开的距离),前景盖在上面,只平移前景。
 * 操作按钮由 [setupSwipeReveal] 动态创建,点了先收起再执行回调,这里不关心具体业务。
 *
 * 和拖拽互相让路:拖拽期间页面把 [dragInProgress] 置 true,侧滑就不接手势;
 * 反过来侧滑展开或正在滑时,[isDragLocked] 会返回 true,页面据此关掉长按拖拽。
 */
class SwipeRevealController internal constructor(
    private val recyclerView: RecyclerView,
    private val foregroundId: Int,
    private val actionsId: Int,
    private val actions: List<SwipeAction>,
    private val direction: Int,
    private val onItemClick: ((position: Int) -> Unit)?,
) {

    /** 侧滑开关,关掉时收起已展开项 */
    var enabled: Boolean = true
        set(value) {
            if (field == value) return
            field = value
            if (value) return
            // 正在跟手的那行先归零,已经展开的行则走收起动画
            gesture?.takeIf { it.swiping }?.let { animateTo(it.foreground, 0f) }
            gesture = null
            close()
        }

    /** 拖拽期间置 true,侧滑让路;正在跟手的手势会先落定 */
    var dragInProgress: Boolean = false
        set(value) {
            if (field == value) return
            field = value
            if (!value) return
            gesture?.takeIf { it.swiping }?.let(::settle)
            gesture = null
        }

    private val slop = ViewConfiguration.get(recyclerView.context).scaledTouchSlop

    private val actionWidth = (ACTION_WIDTH_DP * recyclerView.resources.displayMetrics.density).toInt()

    private var openHolder: RecyclerView.ViewHolder? = null

    /** 当前跟手的手势;落手时创建,抬手或被打断时置空 */
    private var gesture: SwipeGesture? = null

    private val touchListener = object : RecyclerView.OnItemTouchListener {
        override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
            if (!enabled || dragInProgress) return false
            when (e.actionMasked) {
                MotionEvent.ACTION_DOWN -> gesture = createGesture(rv, e)

                MotionEvent.ACTION_MOVE -> {
                    val g = gesture ?: return false
                    if (g.decided) return false
                    val dx = e.x - g.downX
                    val dy = e.y - g.downY
                    // 竖向为主就让给列表滚动,这次手势不再判定
                    if (abs(dx) <= abs(dy)) {
                        if (abs(dy) > slop) g.decided = true
                        return false
                    }
                    if (abs(dx) <= slop) return false
                    g.startSwipe(e.x)
                    // 让 ItemTouchHelper 放手:侧滑接管后它收不到 UP,会一直卡在拖拽态
                    rv.requestDisallowInterceptTouchEvent(true)
                    g.moveTo(e.x)
                    // 吃掉这次手势,子 View 收 CANCEL,避免滑动结束再触发 item 点击
                    return true
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> gesture = null
            }
            return false
        }

        override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {
            val g = gesture
            when (e.actionMasked) {
                MotionEvent.ACTION_MOVE -> if (g != null && g.swiping) g.moveTo(e.x)
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    if (g != null && g.swiping) settle(g)
                    gesture = null
                }
            }
        }

        override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) = Unit
    }

    private val dataObserver = object : RecyclerView.AdapterDataObserver() {
        override fun onChanged() = close(animate = false)

        override fun onItemRangeChanged(positionStart: Int, itemCount: Int) = close(animate = false)

        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) = close(animate = false)

        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) = close(animate = false)

        override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) =
            close(animate = false)
    }

    private val childAttachListener = object : RecyclerView.OnChildAttachStateChangeListener {
        override fun onChildViewAttachedToWindow(view: View) = bindItem(view)

        override fun onChildViewDetachedFromWindow(view: View) {
            // 行被回收时把平移归零,否则复用到别的数据上会带着旧偏移
            view.findViewById<View>(foregroundId)?.apply {
                animate().cancel()
                translationX = 0f
            }
            if (openHolder?.itemView === view) openHolder = null
        }
    }

    private val scrollListener = object : RecyclerView.OnScrollListener() {
        // 滚动时收起展开行,否则长按拖拽会一直被挡着
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            if (newState == RecyclerView.SCROLL_STATE_DRAGGING) close()
        }
    }

    internal fun attach() {
        recyclerView.addOnItemTouchListener(touchListener)
        recyclerView.addOnChildAttachStateChangeListener(childAttachListener)
        recyclerView.addOnScrollListener(scrollListener)
        recyclerView.adapter?.registerAdapterDataObserver(dataObserver)
        for (i in 0 until recyclerView.childCount) bindItem(recyclerView.getChildAt(i))
    }

    fun detach() {
        recyclerView.removeOnItemTouchListener(touchListener)
        recyclerView.removeOnChildAttachStateChangeListener(childAttachListener)
        recyclerView.removeOnScrollListener(scrollListener)
        recyclerView.adapter?.unregisterAdapterDataObserver(dataObserver)
        close(animate = false)
        gesture = null
    }

    /**
     * 长按拖拽要不要让路:有展开行,或侧滑正在跟手时返回 true。
     *
     * 侧滑接管后,后续事件只发给侧滑监听,ItemTouchHelper 收不到 UP 会一直停在选中态,
     * 所以页面要在长按时用 ItemTouchConfig.isLocked 把它拦掉。
     */
    fun isDragLocked(): Boolean = openHolder != null || gesture?.swiping == true

    /**
     * 收起当前展开项。
     *
     * @param animate 要不要播收起动画
     * @param onEnd 收起后的回调,删除这类操作放这里
     */
    fun close(animate: Boolean = true, onEnd: (() -> Unit)? = null) {
        val holder = openHolder
        openHolder = null
        val foreground = holder?.itemView?.findViewById<View>(foregroundId)
        if (!animate || foreground == null) {
            foreground?.animate()?.cancel()
            foreground?.translationX = 0f
            onEnd?.invoke()
            return
        }
        animateTo(foreground, 0f, onEnd)
    }

    /**
     * 落手时记下按到的行,展开行也记 —— 按住它往回滑就是收起。
     * 没滑动时点击仍由前景自己处理,这里不吞事件。
     */
    private fun createGesture(rv: RecyclerView, e: MotionEvent): SwipeGesture? {
        val child = rv.findChildViewUnder(e.x, e.y) ?: return null
        val foreground = child.findViewById<View>(foregroundId) ?: return null
        val actionsView = child.findViewById<View>(actionsId) ?: return null
        if (actionsView.width <= 0) return null
        // 落在别的行上:先把上一行收起,再跟这一行
        if (openHolder?.itemView !== child) close()
        return SwipeGesture(rv.getChildViewHolder(child), foreground, actionsView.width, e.x, e.y)
    }

    private fun settle(g: SwipeGesture) {
        val width = g.width.toFloat()
        val open = abs(g.foreground.translationX) > width / 2f
        openHolder = if (open) g.holder else null
        // 松手后补一段动画:滑过一半就展开,否则弹回
        animateTo(g.foreground, if (open) openOffset(width) else 0f)
    }

    /** 完全展开时的位移:左滑是负数,右滑是正数 */
    private fun openOffset(width: Float): Float =
        if (direction == ItemTouchHelper.LEFT) -width else width

    private fun animateTo(view: View, target: Float, onEnd: (() -> Unit)? = null) {
        view.animate().cancel()
        view.animate()
            .translationX(target)
            .setDuration(180L)
            .withEndAction { onEnd?.invoke() }
            .start()
    }

    /** 一次侧滑的跟手状态;落手时创建,抬手或被打断时丢掉 */
    private inner class SwipeGesture(
        val holder: RecyclerView.ViewHolder,
        val foreground: View,
        val width: Int,
        val downX: Float,
        val downY: Float,
    ) {
        private var startX = downX
        private var startTranslation = foreground.translationX
        var decided = false
        var swiping = false

        /** 截获手势:停掉进行中的动画,以当前位移为起点,跟手不跳位 */
        fun startSwipe(x: Float) {
            decided = true
            swiping = true
            foreground.animate().cancel()
            startX = x
            startTranslation = foreground.translationX
        }

        /** 跟手平移,限制在 0 ~ 展开宽度之间 */
        fun moveTo(x: Float) {
            val raw = startTranslation + (x - startX)
            val limit = width.toFloat()
            foreground.translationX = if (direction == ItemTouchHelper.LEFT) {
                raw.coerceIn(-limit, 0f)
            } else {
                raw.coerceIn(0f, limit)
            }
        }
    }

    /** 给 item 填操作按钮、绑前景点击;每次挂到窗口都会走一遍 */
    private fun bindItem(itemView: View) {
        val foreground = itemView.findViewById<View>(foregroundId) ?: return
        val actionsView = itemView.findViewById<LinearLayout>(actionsId) ?: return
        if (actionsView.childCount == 0) {
            actions.forEach { actionsView.addView(createActionButton(actionsView, it)) }
        }
        foreground.setOnClickListener {
            val position = recyclerView.getChildAdapterPosition(itemView)
            if (position == RecyclerView.NO_POSITION) return@setOnClickListener
            when {
                openHolder?.itemView === itemView -> close()
                // 收起动画被打断时会停在半路,先归位,不触发点击
                foreground.translationX != 0f -> animateTo(foreground, 0f)
                else -> onItemClick?.invoke(position)
            }
        }
    }

    /** 按配置生成一个操作按钮 */
    private fun createActionButton(parent: ViewGroup, action: SwipeAction): TextView =
        TextView(parent.context).apply {
            layoutParams = LinearLayout.LayoutParams(actionWidth, ViewGroup.LayoutParams.MATCH_PARENT)
            text = action.label
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            textSize = 14f
            setTypeface(typeface, Typeface.BOLD)
            setBackgroundColor(action.backgroundColor)
            isClickable = true
            isFocusable = true
            foreground = resolveSelectableItemBackground(parent.context)
            setOnClickListener {
                val itemView = recyclerView.findContainingItemView(this) ?: return@setOnClickListener
                close {
                    val position = recyclerView.getChildAdapterPosition(itemView)
                    if (position != RecyclerView.NO_POSITION) action.onAction(position)
                }
            }
        }

    /** 取主题里的水波纹背景,给按钮做按下反馈 */
    @SuppressLint("UseCompatLoadingForDrawables")
    private fun resolveSelectableItemBackground(context: Context) = TypedValue().let { value ->
        if (context.theme.resolveAttribute(android.R.attr.selectableItemBackground, value, true)) {
            context.getDrawable(value.resourceId)
        } else {
            null
        }
    }
}

/**
 * 给列表挂上"侧滑露出操作区"的手势,返回的控制器用来开关和主动收起。
 *
 * 按钮按 [actions] 动态生成;点击后先播收起动画,结束再执行 [SwipeAction.onAction],
 * 业务不用自己处理收起。
 *
 * @param foregroundId 会被平移的前景 View,必须是 item 根布局的直接子 View
 * @param actionsId 操作区容器,按钮由本方法填进去,它的宽度就是滑开的距离
 * @param actions 操作项,1~3 项
 * @param direction 展开方向,默认左滑
 * @param onItemClick 未展开时点击整行的回调
 */
fun RecyclerView.setupSwipeReveal(
    foregroundId: Int,
    actionsId: Int,
    actions: List<SwipeAction>,
    direction: Int = ItemTouchHelper.LEFT,
    onItemClick: ((position: Int) -> Unit)? = null,
): SwipeRevealController {
    require(actions.size in 1..MAX_ACTION_COUNT) {
        "操作项需为 1~$MAX_ACTION_COUNT 项,当前 ${actions.size}"
    }
    return SwipeRevealController(
        recyclerView = this,
        foregroundId = foregroundId,
        actionsId = actionsId,
        actions = actions,
        direction = direction,
        onItemClick = onItemClick,
    ).apply { attach() }
}
