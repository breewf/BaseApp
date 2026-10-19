package com.hy.basic.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewParent
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.annotation.StyleRes
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewbinding.ViewBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import com.hy.myapp.R

/**
 * @author hy
 * desc: Material 底部弹窗基类 —— 收口 BottomSheetDialogFragment 的三件必做琐事:
 * **主题、behavior 配置、拖到底自动关闭**。
 *
 * - 主题:本项目 AppCompat 主题下 Material 底部弹窗必须显式给 Material 皮,重写 getTheme() 即可,
 *   宿主页面不用改;
 * - behavior 配置:peekHeight/draggable/hideable/initialState 等在 onStart 里统一设置
 *   (顺序敏感:先 hideable 再 state);
 * - 系统栏留白与铺满由皮肤属性处理(enableEdgeToEdge + padding*SystemWindowInsets),
 *   基类不再自己补 padding;
 * - 面板高度上限:behavior.setMaxHeight 夹成可见区高,防止内容溢出;
 * - 面板根 View 必须是 NestedScrollView/RecyclerView 等嵌套滑动容器。
 *
 * 与 [BaseDialogFragment] 分工:整页/返回键拦截/转场动画用后者;选项叠层/半屏面板用本基类。
 * ViewBinding 与模板方法(initView -> observeViewModel -> initData)同一套写法。
 */
abstract class BaseBottomSheetDialogFragment<VB : ViewBinding> : BottomSheetDialogFragment() {

    private var _binding: VB? = null

    /** ViewBinding 实例,仅存在于弹框显示期间 */
    protected val binding: VB
        get() = requireNotNull(_binding) {
            "${javaClass.simpleName} binding 仅在弹框显示期间可用"
        }

    /** 子类直接返回 `XxxBinding.inflate(inflater)` */
    protected abstract fun createBinding(inflater: LayoutInflater, container: ViewGroup?): VB

    // ==== 皮肤 ====

    /**
     * 底部弹窗皮肤(整套 Material 主题,不是 overlay)。
     * 想换圆角/背景色就重写本方法给自己的样式,或重写 [cornerRadiiPx] 只改圆角。
     */
    @get:StyleRes
    protected open val themeRes: Int get() = R.style.AbsBottomSheetDialog

    /** 顶部左右圆角半径(px),缺省 -1 表示交给皮肤决定 */
    protected open val cornerRadiiPx: Int get() = -1

    /** 背后蒙层浓度 */
    protected open val dimAmount: Float get() = BaseDialogFragment.DEFAULT_DIM

    // ==== BottomSheetBehavior 配置 ====

    /**
     * 收起态露出高度(px)。缺省 [PEEK_UNSET] = 不设置,由 fitToContents/内容决定;
     * 想固定半屏就传 `screenHeight * 0.5`,或直接给 dp 换算值。
     */
    protected open val peekHeightPx: Int get() = PEEK_UNSET

    /** 初始状态,缺省收起(只露 peekHeight) */
    protected open val initialState: Int get() = BottomSheetBehavior.STATE_COLLAPSED

    /** 能否拖拽(置 false 后面板钉死,只能靠按钮关) */
    protected open val draggable: Boolean get() = true

    /** 能否被下滑隐藏。**true 时拖到底自动关闭弹框**;false 时下滑只回到收起态 */
    protected open val hideable: Boolean get() = true

    /** 拖拽时跳过中间态(COLLAPSED -> 直接 EXPANDED) */
    protected open val skipCollapsed: Boolean get() = false

    /**
     * true(缺省):展开高度按内容收敛;false:展开到满屏且内容可继续滚动
     * (内部放 RecyclerView/ScrollView 做"半屏起、可拉满"的面板时置 false)。
     */
    protected open val fitToContents: Boolean get() = true

    /**
     * 展开态离顶部的距离(px),想让背后页面露一点就设它。
     * 注意 material 1.13 里 `getExpandedOffset()` 在非 fitToContents 时返回的是
     * `max(本值, 状态栏高度)`,所以传 0 也只会贴到状态栏下沿(这是保住顶部圆角的前提)。
     */
    protected open val expandedOffsetPx: Int get() = 0

    /**
     * true = 面板**铺满可见区**(内容比可见区矮也把白底铺到屏幕底),false = 随内容高。
     *
     * 面板容器(design_bottom_sheet)在 Material 的布局里是 `wrap_content`,内容多矮就只画多高:
     * 展开态顶边被钉在状态栏下方时,底下那一段就是透明的(露出页面)。所以"钉住满屏"的面板
     * 要把内容根的高度钉成"可见区高 - 容器自身 padding":容器量出来刚好等于上限,
     * 底边贴屏幕底,内容又不被导航栏压住(导航栏那一条是容器的 padding)。
     */
    protected open val fillSheetHeight: Boolean get() = false

    /**
     * 面板最大高度(px),<=0 表示用基类算出来的上限(屏幕高 - 状态栏高)。
     *
     * 必须有这个上限:容器是 wrap_content,内容超高时会被量到整屏高,而展开位又被往下推了
     * 一条状态栏的距离,结果面板底边(以及放在末尾的按钮)溢出到屏幕外面去。
     */
    protected open val sheetMaxHeightPx: Int get() = 0

    /** 中间态(HALF_EXPANDED)高度比例,<0 表示不启用半展开态 */
    protected open val halfExpandedRatio: Float get() = -1f

    /**
     * 面板接近屏幕顶部时是否把顶部圆角动画成 0。
     *
     * material 1.13 新增了这套动效(皮肤 attr `shouldRemoveExpandedCorners`,缺省 **true**),
     * 拉满时顶角会变方——看着像“圆角丢了”。但光关掉这个开关不够:
     * 库里的判定是 `state == EXPANDED && (shouldRemoveExpandedCorners || 面板顶边 y == 0)`,
     * 面板拉到屏幕最上沿时圆角照样被抽掉。所以缺省置 false(皮肤里也关了一遍),
     * 配套把皮肤的 `paddingTopSystemWindowInsets` 关成 false,让展开态停在状态栏下方(y 不为 0),
     * 圆角才真的留得住。想要官方那种“拉满变方”的效果就置 true。
     */
    protected open val removeExpandedCorners: Boolean get() = false

    /** true = 交给 Material 播下滑退场动画([dismissWithAnimation] 靠它) */
    protected open val animateOnDismiss: Boolean get() = true

    /** 能否用返回键/点外部关闭 */
    protected open val cancelable: Boolean get() = true

    /**
     * 返回键拦截:返回 true 表示本次返回被页面消费,面板不关。
     * 默认 false = 按 [cancelable] 决定(关闭走下滑退场)。
     */
    protected open fun onBackPressed(): Boolean = false

    // ==== 拖拽状态回调 ====

    /** 状态变化(收起/展开/拖拽中/ settles),用于埋点或联动 UI */
    protected open fun onSheetStateChanged(state: Int) {}

    /** 滑动进度:0 = 完全收起只露 peekHeight,1 = 完全展开 */
    protected open fun onSheetSlide(slideOffset: Float) {}

    // ==== 模板方法 ====

    /** 初始化视图:控件、监听、参数回填 */
    protected open fun initView(savedInstanceState: Bundle?) {}

    /** 观察业务数据 */
    protected open fun observeViewModel() {}

    /** 加载数据 */
    protected open fun initData() {}

    /** 弹框已关闭回调(埋点/收尾) */
    protected open fun onDialogDismiss() {}

    /**
     * 底部弹窗必须显式给 Material 皮肤:基类在 [themeRes] 里给,
     * 覆盖掉 `setStyle`/宿主主题那条路,避免 AppCompat 主题下直接崩。
     */
    override fun getTheme(): Int = themeRes

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val created = super.onCreateDialog(savedInstanceState)
        created.setCancelable(cancelable)
        created.setCanceledOnTouchOutside(cancelable)
        (created as? BottomSheetDialog)?.setDismissWithAnimation(animateOnDismiss)
        return created
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val vbd = createBinding(inflater, container)
        _binding = vbd
        return vbd.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        wireBackInterception(::handleBackKey)
        initView(savedInstanceState)
        observeViewModel()
        initData()
    }

    /** behavior 的参数要在窗口就绪后设置才不会被后续布局覆盖,统一放 onStart */
    override fun onStart() {
        super.onStart()
        applyWindowDim()
        applyBehavior()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDialogDismiss()
    }

    override fun onDestroyView() {
        removeSheetSizeWatcher()
        _binding = null
        super.onDestroyView()
    }

    /** 当前面板的 behavior;弹框还没起来时为 null */
    protected val sheetBehavior: BottomSheetBehavior<FrameLayout>?
        get() = (dialog as? BottomSheetDialog)?.behavior

    /** Material 面板容器(design_bottom_sheet),按 id 往上找,少依赖实现细节 */
    protected val sheetContainer: View?
        get() {
            var current: ViewParent? = _binding?.root?.parent
            while (current is View) {
                if (current.id == com.google.android.material.R.id.design_bottom_sheet) return current
                current = current.parent
            }
            return _binding?.root?.parent as? View
        }

    /**
     * 关闭面板。走 cancel() 而非 dismiss():cancel 才会触发下滑退场动画。
     * hideable 为 false 时 setState(HIDDEN) 会被忽略,只能直接 dismiss。
     */
    protected fun dismissWithAnimation() {
        val sheetDialog = dialog as? BottomSheetDialog
        if (sheetDialog == null || !animateOnDismiss || !hideable) {
            dismissSafely()
            return
        }
        sheetDialog.setDismissWithAnimation(true)
        sheetDialog.cancel()
    }

    private fun handleBackKey(): Boolean {
        if (onBackPressed()) return true
        if (cancelable) dismissWithAnimation()
        return true
    }

    private fun applyWindowDim() {
        val window = dialog?.window ?: return
        val params = window.attributes
        params.dimAmount = dimAmount
        if (dimAmount > 0f) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        }
        window.attributes = params
    }

    /**
     * 装配顺序敏感:先 hideable(会重置状态)再 state;状态监听只挂一次。
     * 用显式 setter 而非属性语法:BottomSheetBehavior 有同名私有字段,Kotlin 会绑错。
     */
    private fun applyBehavior() {
        val sheet = sheetBehavior ?: return
        sheet.setFitToContents(fitToContents)
        sheet.setDraggable(draggable)
        sheet.setSkipCollapsed(skipCollapsed)
        sheet.setExpandedOffset(expandedOffsetPx)
        if (peekHeightPx != PEEK_UNSET) sheet.setPeekHeight(peekHeightPx)
        if (halfExpandedRatio >= 0f) {
            sheet.setHalfExpandedRatio(halfExpandedRatio)
        }
        sheet.setHideable(hideable)
        // 拉满时保留顶部圆角:必须在 setState 之前关掉。圆角是按 slideOffset 插值动画掉的,
        // 一上来就是 EXPANDED 时这一下已经跑完了,事后才置 false 只会看到“圆角还是没了”。
        sheet.setShouldRemoveExpandedCorners(removeExpandedCorners)
        sheet.setState(initialState)
        applyCornerRadii()
        // 面板高度上限要用 coordinator 的真实高度 + 状态栏 inset 算,onStart 时窗口还没量过,
        // 所以挂一个布局监听反复校正(内容高度改了、转屏了都能重算一次)
        installSheetSizeWatcher()
        if (!sheetCallbackAttached) {
            sheet.addBottomSheetCallback(sheetCallback)
            sheetCallbackAttached = true
        }
    }

    private var sheetCallbackAttached = false

    private val sheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {
        override fun onStateChanged(sheet: View, newState: Int) {
            // 拖到底 = 关闭:Material 只给状态,关不关由基类决定(hideable 才有这个状态)
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                dismissSafely()
                return
            }
            onSheetStateChanged(newState)
        }

        override fun onSlide(sheet: View, slideOffset: Float) {
            onSheetSlide(slideOffset)
        }
    }

    /** 给面板夹一个不溢出屏幕的高度上限,并按需铺满。setMaxHeight 只赋值不 requestLayout。 */
    private fun applySheetSize() {
        val sheet = sheetContainer ?: return
        val coordinator = sheet.parent as? View ?: return
        val behavior = sheetBehavior ?: return
        if (coordinator.height <= 0) return
        val bars = ViewCompat.getRootWindowInsets(coordinator)?.getInsets(
            WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout(),
        )
        val visibleHeight = coordinator.height - (bars?.top ?: 0)
        val cap = if (sheetMaxHeightPx > 0) minOf(sheetMaxHeightPx, visibleHeight) else visibleHeight
        if (cap > 0 && cap != appliedSheetMaxHeight) {
            appliedSheetMaxHeight = cap
            // 不用属性语法:maxHeight 有个同名私有字段,和 draggable 那批一样会被绑错
            behavior.setMaxHeight(cap)
            sheet.requestLayout()
        }
        if (fillSheetHeight) {
            // 容器是 wrap_content,子 View 给 MATCH_PARENT 会退化成 AT_MOST 铺不满;
            // 钉成 EXACTLY 才能把容器顶到 cap 高。要减掉容器自身的 padding。
            val root = _binding?.root ?: return
            val target = cap - sheet.paddingTop - sheet.paddingBottom
            val rootLp = root.layoutParams ?: return
            if (target > 0 && rootLp.height != target) {
                rootLp.height = target
                root.layoutParams = rootLp
            }
        }
    }

    private var appliedSheetMaxHeight = 0

    /** 面板尺寸在窗口真量过之后才能算,转屏/内容高度变化后重算。值没变不动手,不会循环布局。 */
    private fun installSheetSizeWatcher() {
        val coordinator = sheetContainer?.parent as? View ?: return
        if (sheetSizeWatched === coordinator) return
        removeSheetSizeWatcher()
        val listener = View.OnLayoutChangeListener { _, _, _, _, _, _, _, _, _ -> applySheetSize() }
        coordinator.addOnLayoutChangeListener(listener)
        sheetSizeWatched = coordinator
        sheetSizeListener = listener
        applySheetSize()
    }

    private fun removeSheetSizeWatcher() {
        val listener = sheetSizeListener ?: return
        (sheetSizeWatched ?: return).removeOnLayoutChangeListener(listener)
        sheetSizeListener = null
        sheetSizeWatched = null
    }

    private var sheetSizeWatched: View? = null
    private var sheetSizeListener: View.OnLayoutChangeListener? = null

    /** 皮肤里的顶部圆角覆盖(想按页面改圆角时用,不想要圆角就传 0) */
    private fun applyCornerRadii() {
        val radii = cornerRadiiPx
        if (radii < 0) return
        // 面板容器的背景就是 Material 画圆角用的 MaterialShapeDrawable
        val sheetView = sheetContainer ?: return
        val shape = sheetView.background as? MaterialShapeDrawable ?: return
        shape.shapeAppearanceModel = ShapeAppearanceModel.builder()
            .setTopLeftCornerSize(radii.toFloat())
            .setTopRightCornerSize(radii.toFloat())
            .setBottomLeftCornerSize(0f)
            .setBottomRightCornerSize(0f)
            .build()
    }

    companion object {
        /** [peekHeightPx] 的"不设置"哨兵(BottomSheetBehavior 用负数表示 AUTO,不能混用) */
        const val PEEK_UNSET = Int.MIN_VALUE
    }
}

/** 状态码 -> 可读名称(排查拖拽问题时直接打这个,别对着一串 int 猜) */
fun bottomSheetStateName(state: Int): String = when (state) {
    BottomSheetBehavior.STATE_DRAGGING -> "拖拽中"
    BottomSheetBehavior.STATE_SETTLING -> "松手回弹中"
    BottomSheetBehavior.STATE_EXPANDED -> "完全展开"
    BottomSheetBehavior.STATE_COLLAPSED -> "收起(露 peekHeight)"
    BottomSheetBehavior.STATE_HIDDEN -> "已隐藏"
    BottomSheetBehavior.STATE_HALF_EXPANDED -> "半展开"
    else -> "未知状态($state)"
}
