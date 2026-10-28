package com.hy.basic.banner

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.hy.basic.R
import kotlin.math.abs

/**
 * desc: 轮播控件 —— ViewPager2 + RecyclerView 实现,不依赖任何三方 Banner 库。
 *
 * 最小用法(纯图片轮播):
 * ```
 * banner.setData(urls)
 * ```
 * 常用配置一行一个意图:
 * ```
 * banner.loop = true                       // 无限循环(数据不足 2 条自动退化)
 * banner.autoScroll = true                 // 自动轮播,离屏/不可见时自动停
 * banner.interval = 3000L                  // 轮播间隔
 * banner.scrollDuration = 600              // 翻页动画时长(0 = 用系统时长)
 * banner.transformer = BannerTransformer.CUBE   // 切换动画
 * banner.setPageMarginDp(24)               // 两侧露出相邻页(peek)
 * banner.indicatorStyle = BannerIndicator.Style.DOTS_SLIDER
 * banner.onPageClick = { pos, item -> open(item) }
 * banner.onPageSelected = { pos -> tv.text = "第 ${pos + 1} 页" }
 * ```
 * XML 里也可以把这些一次性写完(见 attrs.xml 的 `Banner`)。
 *
 * 想放自己的内容(图 + 标题、卡片、视频封面)就换一种写法:
 * ```
 * banner.setData(
 *     items = titles,                       // 数据可以是任意对象
 *     itemLayoutRes = R.layout.banner_item_title,
 * ) { page, item, position ->
 *     page.findViewById<TextView>(R.id.tvBannerTitle).text = item as? CharSequence
 * }
 * ```
 *
 * 三个内部约定,理解后基本不会踩坑:
 * 1. **循环靠"虚拟下标"**:页数 = 数据量 × 100,起始落在中间那一格,所以左右都能无限翻;
 *    对外的下标一律是折算后的**真实下标**,页面侧不用关心虚拟的存在。
 * 2. **自动轮播看状态自己起停**:只在 `autoScroll && 已挂载 && 可见 && 未在滑动 && 还有下一页`
 *    时才排队,页面切走、隐藏、用户正在拖都会自动暂停,回来自动恢复,不需要页面写生命周期代码。
 * 3. **[pager] 是敞开的**:VP2 本身的能力(自定义 PageChangeCallback、offscreenPageLimit、
 *    内部 RecyclerView 的细节)直接从 [pager] 进去拿,封装不做黑盒。
 */
class Banner @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : FrameLayout(context, attrs) {

    /**
     * 底层翻页控件。
     *
     * 封装没覆盖到的能力(自己注册 OnPageChangeCallback、调 [ViewPager2.setOrientation]、
     * 拿 `pager.getChildAt(0)` 当 RecyclerView 用)都从这里进去。
     * 注意:adapter 由 [Banner] 持有,不要对它调用 [ViewPager2.setAdapter]。
     */
    val pager: ViewPager2 = ViewPager2(context)

    /** 页码指示器;XML 没开启时为 null。颜色 / 半径 / 间距等细节直接在这个 View 上改 */
    var indicator: BannerIndicator? = null
        private set

    private val adapter = BannerAdapter()
    private val handler = Handler(Looper.getMainLooper())
    private val turnNextTask = Runnable { advance(1) }

    /** 当前是否处于滑动过程中(手势拖动或惯性滚动),期间不排队自动轮播 */
    private var isSettling = false

    /**
     * [ViewPager2] 的最新滚动状态,用来分开"滑过去"与"直接跳过去"。
     *
     * VP2 对程序化滚动是先 dispatchStateChanged(SETTLING)、再 dispatchSelected,
     * 所以 [onPageSelected] 里读到的一定是滚动已开始的状态 —— 靠这个才能不早落位。
     */
    private var scrollState = ViewPager2.SCROLL_STATE_IDLE

    /** [setCustomTransformer] 写入的自定义动画;非空时优先于 [transformer] */
    private var customBlock: ((page: View, position: Float) -> Unit)? = null

    // ==== 可配置项 ====

    /** 无限循环。数据不足 2 条时自动退化为不循环 */
    var loop: Boolean = true
        set(value) {
            if (field == value) return
            field = value
            adapter.loop = value
            restorePosition()
            updateAuto()
        }

    /** 是否自动轮播 */
    var autoScroll: Boolean = true
        set(value) {
            if (field == value) return
            field = value
            updateAuto()
        }

    /** 自动轮播间隔(ms) */
    var interval: Long = DEFAULT_INTERVAL
        set(value) {
            val v = value.coerceAtLeast(MIN_INTERVAL)
            if (field == v) return
            field = v
            updateAuto()
        }

    /**
     * 平滑翻页的动画时长(ms),自动轮播与 [next] / [prev] / [select] 都走它;0 = 不干预。
     *
     * ViewPager2 没开放这个开关:它内部走 LinearLayoutManager.smoothScrollToPosition,
     * 时长按「每英寸 25ms」算,翻一页只有一百多毫秒,自动轮播看着很急。
     * 这里的做法是:先让 [ViewPager2.setCurrentItem] 把记账与回调全部走完(顺序不变),
     * 紧接着用 [RecyclerView.LayoutManager.startSmoothScroll] 换上定时长的 scroller ——
     * 新 scroller 会把刚启动那个顶掉,而它连一帧都还没走,所以观感就是「同一页,慢一点滑过去」。
     */
    var scrollDuration: Int = DEFAULT_SCROLL_DURATION
        set(value) {
            val v = value.coerceAtLeast(0)
            if (field == v) return
            field = v
        }

    /**
     * 两侧各留出多少像素露出相邻页(peek 效果),0 = 铺满。
     *
     * 实现上不再给内部 RecyclerView 加 padding(ViewPager2 1.0.0 对 padding 是否参与页宽
     * 因版本而异,靠不住),而是两步一起：
     * 1. 每页内容左右各内缩 pageMargin(见 BannerAdapter.pagePadding),页本身仍是满宽;
     * 2. 每帧把页面按 position 往中心靠拢(见 shiftForPeek),相邻页自然从两边滑进露出来的那一段。
     * 这样不依赖 snap 算法(位移只是视觉),静止与拖动都不会错位;
     * 缩放类的动画(卡片流)会把相邻页推出屏幕,那一步里还额外补了缩放让出来的量。
     */
    var pageMargin: Int = 0
        set(value) {
            val v = value.coerceAtLeast(0)
            if (field == v) return
            field = v
            applyPeek()
        }

    /** [pageMargin] 的 dp 版,调用处少算一次密度 */
    fun setPageMarginDp(dp: Int) {
        pageMargin = dp(dp.toFloat()).toInt()
    }

    /** 图片模式下 ImageView 的缩放方式,默认 CENTER_CROP */
    var imageScaleType: ImageView.ScaleType = ImageView.ScaleType.CENTER_CROP
        set(value) {
            if (field == value) return
            field = value
            adapter.imageScaleType = value
            adapter.notifyDataSetChanged()
        }

    /** 是否允许用户手势滑动(引导页最后一步、纯展示位可以关掉) */
    var userScrollEnabled: Boolean = true
        set(value) {
            if (field == value) return
            field = value
            // ViewPager2 1.0.0 里这个开关叫 userInputEnabled(1.1.0 才改名 userScrollEnabled)
            pager.setUserInputEnabled(value)
        }

    /** 内置切换动画。赋值会清掉 [setCustomTransformer] 设置的自定义动画 */
    var transformer: BannerTransformer = BannerTransformer.NONE
        set(value) {
            if (field == value) return
            field = value
            customBlock = null
            applyTransformer()
        }

    /** 指示器样式;传 null 关闭指示器。开启时若尚未创建会自动创建 */
    var indicatorStyle: BannerIndicator.Style? = null
        set(value) {
            if (field == value) return
            field = value
            ensureIndicator()
        }

    /** 指示器在 Banner 内的位置([Gravity] 组合),默认底部居中 */
    var indicatorGravity: Int = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
        set(value) {
            if (field == value) return
            field = value
            indicator?.layoutParams = indicatorParams()
        }

    /** 指示器与 Banner 边缘的距离(px) */
    var indicatorMargin: Int = dp(10f).toInt()
        set(value) {
            if (field == value) return
            field = value
            indicator?.layoutParams = indicatorParams()
        }

    // ==== 回调 ====

    /** 点击某一页,[position] 是真实下标 */
    var onPageClick: ((position: Int, item: Any?) -> Unit)? = null
        set(value) {
            field = value
            adapter.onPageClick = value
        }

    /** 翻页完成,[position] 是真实下标 */
    var onPageSelected: ((position: Int) -> Unit)? = null

    /** 滑动过程中逐帧回调,[position] 真实下标、[offset] 该页滑出比例 */
    var onPageScrolled: ((position: Int, offset: Float) -> Unit)? = null

    /** [ViewPager2] 的滚动状态变化,需要精细控制(比如滑动时暂停轮播)时用 */
    var onPageScrollStateChanged: ((state: Int) -> Unit)? = null

    /** 数据条数 */
    val realItemCount: Int get() = adapter.realItemCount

    /**
     * 内部翻页监听:把虚拟下标折算成真实下标后再对外回调,并顺手驱动 [indicator]。
     *
     * 声明在 [init] 之前,因为 init 里就要 registerOnPageChangeCallback 用到它。
     */
    private val pageCallback = object : ViewPager2.OnPageChangeCallback() {

        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            val real = adapter.realPosition(position)
            // 指示器跟手:喂的是"页 + 比例",所以拖到一半指示器也就走一半。
            // 这里故意不钳到 [0, count-1]:循环轮播从末页滑向首页时 real 停在末页、比例跑到 1,
            // 游标自然越到 count 上,指示器据此做"滑出的一端同时滑入"的绕回动画
            indicator?.selection = real + positionOffset
            onPageScrolled?.invoke(real, positionOffset)
        }

        override fun onPageSelected(position: Int) {
            val real = adapter.realPosition(position)
            // 只在没滚动动画时直接落位。程序化滚动的 Selected 是在滚动「开始时」就分发的,
            // 那会儿把游标设成新页,目标点会先亮一下、再被后续 Scrolled 拉回去重新滑过去
            if (scrollState == ViewPager2.SCROLL_STATE_IDLE) indicator?.selection = real.toFloat()
            onPageSelected?.invoke(real)
        }

        override fun onPageScrollStateChanged(state: Int) {
            scrollState = state
            isSettling = state != ViewPager2.SCROLL_STATE_IDLE
            // 停下后把游标收到当前页的整数格,消除差一点到 1 的浮点尾巴(拖到一半松手回到原页也走这里)
            if (state == ViewPager2.SCROLL_STATE_IDLE) {
                indicator?.selection = adapter.realPosition(pager.currentItem).toFloat()
            }
            onPageScrollStateChanged?.invoke(state)
            updateAuto()
        }
    }

    init {
        addView(pager, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        adapter.loop = loop
        pager.adapter = adapter
        pager.registerOnPageChangeCallback(pageCallback)
        applyPeek()
        applyTransformer()
        attrs?.let { readAttrs(it) }
        ensureIndicator()
    }

    /**
     * 设置轮播数据。
     *
     * - [itemLayoutRes] = 0:内置图片模式,[items] 里放 url(String)或资源 id(@DrawableRes Int)即可;
     * - [itemLayoutRes] != 0:每页 inflate 这个布局,[onBind] 里按真实下标回填内容。
     */
    fun setData(
        items: List<Any?>,
        @LayoutRes itemLayoutRes: Int = 0,
        onBind: ((page: View, item: Any?, position: Int) -> Unit)? = null,
    ) {
        adapter.itemLayoutRes = itemLayoutRes
        adapter.onBind = if (itemLayoutRes == 0) null else onBind
        adapter.items = items
        indicator?.count = items.size
        restorePosition()
        updateAuto()
    }

    /** 当前真实页下标;赋值等价于 [select] 且不带动画 */
    var currentItem: Int
        get() = adapter.realPosition(pager.currentItem)
        set(value) = select(value, smooth = false)

    /** 跳到第 [position] 页(真实下标) */
    fun select(position: Int, smooth: Boolean = true) {
        if (adapter.realItemCount == 0) return
        val target = virtualFor(position)
        if (smooth) smoothScrollTo(target) else pager.setCurrentItem(target, false)
        updateAuto()
    }

    /** 翻到下一页 */
    fun next() = advance(1)

    /** 翻到上一页 */
    fun prev() = advance(-1)

    /** 立刻开始自动轮播(会重新计时) */
    fun start() = updateAuto()

    /** 停止自动轮播。之后任何配置变化都可能按 [autoScroll] 重新排上,想彻底关掉请用 [autoScroll] = false */
    fun stop() {
        handler.removeCallbacks(turnNextTask)
    }

    /** 自定义切换动画;传 null 回到 [transformer] 指定的内置动画 */
    fun setCustomTransformer(block: ((page: View, position: Float) -> Unit)?) {
        customBlock = block
        applyTransformer()
    }

    /** 便捷注册翻页回调,不想自己拼 object : OnPageChangeCallback 时用 */
    fun addPageCallback(block: (position: Int, offset: Float, dragging: Boolean) -> Unit) {
        pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrolled(position: Int, offset: Float, pixels: Int) {
                block(adapter.realPosition(position), offset, isSettling)
            }
        })
    }

    // ==== 内部实现 ====

    /** 只在"该自动翻"的时候排队,任何时候都先清空再决定,避免多个待执行任务叠加 */
    private fun updateAuto() {
        handler.removeCallbacks(turnNextTask)
        val hasNextPage = loop || pager.currentItem < adapter.itemCount - 1
        val shouldRun = autoScroll && isAttachedToWindow && isShown &&
            windowVisibility == VISIBLE && adapter.realItemCount > 1 && !isSettling && hasNextPage
        if (shouldRun) handler.postDelayed(turnNextTask, interval)
    }

    private fun advance(step: Int) {
        if (adapter.itemCount == 0) return
        val target = pager.currentItem + step
        if (!loop && (target < 0 || target >= adapter.itemCount)) return
        smoothScrollTo(target)
        updateAuto()
    }

    /**
     * 真实下标 -> 虚拟下标,取离当前位置最近的那个同余格,
     * 这样 `select(0)` 在循环中间也能翻到"下一个第 0 页",而不是横跨 5000 页跳过去。
     */
    private fun virtualFor(real: Int): Int {
        val count = adapter.realItemCount
        if (count == 0 || !loop) return real.coerceIn(0, maxOf(count - 1, 0))
        val current = pager.currentItem
        var target = (current - Math.floorMod(current, count)) + Math.floorMod(real, count)
        if (target - current > count / 2) target -= count
        if (current - target > count / 2) target += count
        return target.coerceIn(0, adapter.itemCount - 1)
    }

    /** 数据/循环变化后回到中间那一格,保证两侧都有得翻 */
    private fun restorePosition() {
        pager.setCurrentItem(adapter.startVirtualPosition(), false)
        indicator?.selection = 0f
    }

    /** ViewPager2 的内部第一层就是 RecyclerView(它负责真正的分页与复用) */
    private val recycler: RecyclerView? get() = pager.getChildAt(0) as? RecyclerView

    /**
     * 按 [scrollDuration] 平滑翻到第 [target] 页(虚拟下标)。
     *
     * 只接管近距离翻页:跳很多页时 ViewPager2 自已是「先瞬移到附近、下一帧再滑」,
     * 那个平滑滚动是 post 出来的,这里不去插手,否则会把它的补偿打断。
     */
    private fun smoothScrollTo(target: Int) {
        val jump = abs(target - pager.currentItem)
        pager.setCurrentItem(target, true)
        if (scrollDuration <= 0 || jump > MAX_SMOOTH_JUMP) return
        val layoutManager = recycler?.layoutManager ?: return
        val duration = scrollDuration
        val scroller = object : LinearSmoothScroller(context) {
            // 原本这两个时长都按距离算,固定成 duration 就是一页匀速滑过去
            override fun calculateTimeForScrolling(dx: Int): Int = duration
            override fun calculateTimeForDeceleration(dx: Int): Int = duration
        }
        scroller.targetPosition = target
        layoutManager.startSmoothScroll(scroller)
    }

    /** 露边:页内容内缩 + 保证相邻页已被创建(否则两边是空白) */
    private fun applyPeek() {
        adapter.pagePadding = pageMargin
        pager.offscreenPageLimit = if (pageMargin > 0) 1 else ViewPager2.OFFSCREEN_PAGE_LIMIT_DEFAULT
        // 已经 attach 的页直接改内缩,不用 notifyDataSetChanged(避开刷新时当前页跳回开头)
        val recycler = recycler ?: return
        for (i in 0 until recycler.childCount) recycler.getChildAt(i).setPadding(pageMargin, 0, pageMargin, 0)
    }

    /**
     * 把每帧流程一次挂到 ViewPager2 上:[resetForTransform] 复位 -> 效果 -> 露边靠拢。
     *
     * 三步顺序不能乱(旧 ABaseTransformer 也是这个结构):
     * 1. 复位旋转/缩放/轴心/透明度,避免复用页带脏状态;
     * 2. 视口外的页直接透明掉不再叠加效果,不然位移、缩放这类效果会把隔了一两张的页拉进画面;
     * 3. 开了露边再把页面按 position 靠拢,让左右相邻页露出来。
     */
    private fun applyTransformer() {
        val effect = customBlock ?: transformer.block
        // 自定义动画统一按「保留原生平移」处理(要原地播放,自己在 lambda 里写 translationX)
        val keepSlide = customBlock != null || transformer.keepSlide
        val layer = if (customBlock == null && transformer.needsLayer) {
            View.LAYER_TYPE_HARDWARE
        } else {
            View.LAYER_TYPE_NONE
        }
        val peek = pageMargin
        pager.setPageTransformer { page, position ->
            // setLayerType 参数与 getter 不对称,Kotlin 生成不了 layerType 的合成属性,只能显式调
            page.setLayerType(layer, null)
            page.resetForTransform(position, keepSlide)
            // 露边后相邻页更靠近中心,「还算不算视口内」的边界要按间距放宽
            val spacing = page.width - 2 * peek
            val edge = if (peek > 0 && spacing > 0) pager.width.toFloat() / spacing else 1f
            if (abs(position) >= edge) {
                page.hideFarPage()
            } else {
                effect(page, position)
                if (peek > 0) page.shiftForPeek(position, peek)
            }
        }
        // 换动画后先清一遍脏状态,不然残留的 alpha / scale 会跟着复用 View 走
        resetPages()
    }

    private fun resetPages() {
        val recycler = recycler ?: return
        for (i in 0 until recycler.childCount) recycler.resetChild(i)
    }

    private fun ensureIndicator() {
        val style = indicatorStyle
        if (style == null) {
            indicator?.let { removeView(it) }
            indicator = null
            return
        }
        val view = indicator ?: BannerIndicator(context).also { indicator = it }
        view.style = style
        view.count = adapter.realItemCount
        if (view.parent == null) addView(view, indicatorParams())
        else view.layoutParams = indicatorParams()
    }

    private fun indicatorParams(): LayoutParams = LayoutParams(
        LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, indicatorGravity,
    ).apply {
        leftMargin = indicatorMargin
        topMargin = indicatorMargin
        rightMargin = indicatorMargin
        bottomMargin = indicatorMargin
    }

    private fun readAttrs(attrs: AttributeSet) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.Banner)
        // 循环/自动轮播先落到字段上,init 里的 adapter、排队都依赖这两个值
        val loopValue = a.getBoolean(R.styleable.Banner_banner_loop, loop)
        val autoValue = a.getBoolean(R.styleable.Banner_banner_autoScroll, autoScroll)
        interval = a.getInteger(R.styleable.Banner_banner_interval, interval.toInt()).toLong()
        scrollDuration = a.getInteger(R.styleable.Banner_banner_scrollDuration, scrollDuration)
        pageMargin = a.getDimensionPixelSize(R.styleable.Banner_banner_pageMargin, pageMargin)
        transformer = BannerTransformer.fromAttrValue(
            a.getInt(R.styleable.Banner_banner_transformer, BannerTransformer.NONE.attrValue),
        )
        userScrollEnabled = a.getBoolean(R.styleable.Banner_banner_userScrollEnabled, userScrollEnabled)
        imageScaleType = when (a.getInt(R.styleable.Banner_banner_scaleType, 0)) {
            1 -> ImageView.ScaleType.FIT_XY
            2 -> ImageView.ScaleType.CENTER_INSIDE
            3 -> ImageView.ScaleType.FIT_CENTER
            else -> ImageView.ScaleType.CENTER_CROP
        }
        val indicatorValue = a.getInt(R.styleable.Banner_banner_indicator, -1)
        indicatorStyle = BannerIndicator.Style.entries.firstOrNull { it.attrValue == indicatorValue }
        indicatorGravity = when (a.getInt(R.styleable.Banner_banner_indicatorGravity, 0)) {
            1 -> Gravity.BOTTOM or Gravity.START
            2 -> Gravity.BOTTOM or Gravity.END
            3 -> Gravity.TOP or Gravity.CENTER_HORIZONTAL
            4 -> Gravity.TOP or Gravity.START
            5 -> Gravity.TOP or Gravity.END
            else -> Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
        }
        indicatorMargin = a.getDimensionPixelSize(R.styleable.Banner_banner_indicatorMargin, indicatorMargin)
        // 指示器外观在 BannerIndicator 自己的 styleable 里读,这里只透传颜色;没开启指示器时整段跳过
        indicator?.let { indicator ->
            indicator.dotColor = a.getColor(R.styleable.Banner_banner_indicatorColor, indicator.dotColor)
            indicator.selectedColor =
                a.getColor(R.styleable.Banner_banner_indicatorSelectedColor, indicator.selectedColor)
        }
        a.recycle()
        loop = loopValue
        autoScroll = autoValue
        updateAuto()
    }

    /**
     * 挂载/可见性变化都会影响"该不该轮播",这几个回调就是把它重新判一遍。
     * 页面切走时 windowVisibility 变 GONE、View 隐藏时 isShown 变 false,
     * 于是列表滚出屏幕的 Banner 不会在后台偷偷翻页。
     */
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        applyPeek()
        updateAuto()
    }

    override fun onDetachedFromWindow() {
        stop()
        super.onDetachedFromWindow()
    }

    override fun onWindowVisibilityChanged(visibility: Int) {
        super.onWindowVisibilityChanged(visibility)
        updateAuto()
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        updateAuto()
    }

    private fun dp(value: Float): Float = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, value, resources.displayMetrics,
    )

    private companion object {
        const val DEFAULT_INTERVAL = 3000L
        const val MIN_INTERVAL = 1000L

        /** 翻页动画默认时长(ms):比系统按距离算出来的一百多毫秒慢一倍,自动轮播不再显得急 */
        const val DEFAULT_SCROLL_DURATION = 500

        /** 超过这个页数差就不接管动画(交给 ViewPager2 自己分段处理) */
        const val MAX_SMOOTH_JUMP = 3
    }
}
