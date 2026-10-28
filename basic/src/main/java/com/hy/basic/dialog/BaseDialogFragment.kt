package com.hy.basic.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.Gravity
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.activity.ComponentDialog
import androidx.activity.OnBackPressedCallback
import androidx.annotation.StyleRes
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.viewbinding.ViewBinding
import com.hy.basic.R

/**
 * @author hy
 * desc: 全屏/悬浮 DialogFragment 基类 —— 补函数式弹窗([DialogExt])覆盖不了的场景:
 * 内容复杂、要管生命周期、占满整屏或需要退场动画的弹框。
 *
 * 沿用 [com.hy.basic.BaseFragment] 模板方法:initView -> observeViewModel -> initData,
 * 子类只需关心内容。
 *
 * 能力:createBinding(ViewBinding) / isFullscreen+gravity+widthRatio+dimAmount(窗口配置)
 * / animStyleRes(进出场动画) / onBackPressed(返回键拦截) / edge-to-edge 系统栏适配
 * / showSafely(安全弹出+状态已保存兜底)。
 *
 * 数据传递:参数走 arguments;与宿主共享数据用 ViewModelProvider(requireActivity())。
 */
abstract class BaseDialogFragment<VB : ViewBinding> : DialogFragment() {

    private var _binding: VB? = null

    /** ViewBinding 实例,仅存在于弹框显示期间 */
    protected val binding: VB
        get() = requireNotNull(_binding) {
            "${javaClass.simpleName} binding 仅在弹框显示期间可用"
        }

    /**
     * 子类直接返回 `XxxBinding.inflate(inflater)`。
     * 容器可能是弹框窗口的 content,用单参 inflate 最稳(避免根 View 参数被忽略的告警)。
     */
    protected abstract fun createBinding(inflater: LayoutInflater, container: ViewGroup?): VB

    // ==== 皮肤与窗口 ====

    /**
     * 弹框主题。缺省按 [isFullscreen] 在两套自研皮肤间切换:
     * 全屏(背景由布局自己给) / 卡片(透明背景 + 蒙层,尺寸位置由 [widthRatio] 与 [gravity] 定)。
     * 两套皮肤都是**不悬浮**的窗口:悬浮窗口的内容会被主题的 windowMinWidthMajor/Minor 量到
     * 一个上限,match_parent 永远铺不到屏幕两边(卡片两侧那两条透明层就是这么来的)。
     *
     * 本值靠重写 [getTheme] 生效(见下面),光定义皮肤不接线是无效的。
     */
    @get:StyleRes
    protected open val themeRes: Int
        get() = if (isFullscreen) R.style.AbsDialogFullscreen else R.style.AbsDialogCard

    /** true = 占满整屏(协议页/引导页);false = 卡片 */
    protected open val isFullscreen: Boolean get() = false

    /** 卡片的位置(全屏时仅影响尺寸表现不明显,可忽略);BOTTOM = 贴底通栏卡片 */
    protected open val gravity: Int get() = Gravity.CENTER

    /** 宽度占屏比例;1f(缺省卡片以外常见取值)直接用 MATCH_PARENT,原因见 [applyWindowLayout] */
    protected open val widthRatio: Float get() = if (isFullscreen) 1f else 0.85f

    /** 背后蒙层浓度,全屏弹框通常 0f(自己画背景) */
    protected open val dimAmount: Float get() = if (isFullscreen) 0f else DEFAULT_DIM

    // ==== 动画 ====

    /**
     * 进出场动画样式(里面就是 `android:windowEnterAnimation` / `android:windowExitAnimation` 两项):
     * 缺省从底部滑入滑出 [R.style.AbsDialogAnimBottom],整屏淡入淡出用 [R.style.AbsDialogAnimFade],0 = 无动画。
     *
     * 只在窗口层做(android.view.Window.setWindowAnimations):动画若挂在内容根 View 上,窗口是“整块先出现、
     * 内容再动”的,入场会先闪一层窗口底色,退场会“内容滑走了、底色还挂在屏上”。
     */
    @get:StyleRes
    protected open val animStyleRes: Int get() = R.style.AbsDialogAnimBottom

    // ==== 行为 ====

    /** 能否被返回键/点外部关闭(强制阅读类弹框置 false) */
    protected open val cancelable: Boolean get() = true

    /** 点卡片外部能否关闭(卡片常用;全屏弹框无"外部"概念,不影响) */
    protected open val cancelOnTouchOutside: Boolean get() = true

    /**
     * 返回键拦截:返回 true 表示本次返回已由页面消费,弹框保持打开。
     * 默认 false = 不拦截,按 [cancelable] 决定是否关闭(关闭同样走 [dismissWithAnimation],有退场动画)。
     *
     * 注:输入法展开时第一次返回键会被输入法消费(系统行为),拦截点仍在弹框这边。
     */
    protected open fun onBackPressed(): Boolean = false

    /** 弹框已关闭回调(点按钮/点外部/返回键/页面销毁都会走到,适合埋点或收尾) */
    protected open fun onDialogDismiss() {}

    // ==== 模板方法(与 BaseFragment 同名同序) ====

    /** 初始化视图:控件、监听、参数回填 */
    protected open fun initView(savedInstanceState: Bundle?) {}

    /** 观察业务数据:Flow 用 viewLifecycleOwner.lifecycleScope + repeatOnLifecycle */
    protected open fun observeViewModel() {}

    /** 加载数据:发起请求、读取参数 */
    protected open fun initData() {}

    /**
     * 皮肤必须显式递给弹框:[DialogFragment.onCreateDialog] 里 new Dialog 用的就是本方法,
     * 不重写会拿到 `0` → 落到**宿主 Activity 的主题**上(不是 Dialog 主题:windowBackground 是
     * 实色、系统栏色取自 colorPrimaryDark),结果皮肤里的透明背景/无标题/蒙层/窗口动画一个都不生效,
     * 表现就是“弹框先闪一层白底、背面页面被刷白、宽度怎么都不铺满”。
     * 与 [BaseBottomSheetDialogFragment] 同一套路:只重写 getTheme,不改宿主主题、不用 setStyle。
     */
    override fun getTheme(): Int = themeRes

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val created = super.onCreateDialog(savedInstanceState)
        created.setCancelable(cancelable)
        created.setCanceledOnTouchOutside(cancelOnTouchOutside)
        // 进出场动画必须在窗口被加进 WindowManager **之前**指定:[DialogFragment.onStart] 才 show 弹框,
        // 而入场动画取的是加窗口那一刻的 attrs.windowAnimations。放到 onStart 之后设,入场那一次来不及,
        // 只有退场有效——表现就是“直接冒出来,关掉时却有动画”。
        created.window?.setWindowAnimations(animStyleRes)
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
        // 两套皮肤都要做系统栏适配:targetSdk 35+ 强制 edge-to-edge,不补 padding 内容会被
        // 状态栏/导航栏压住;补哪几条由 [applySystemBarInsets] 按 gravity 决定
        installSystemBarInsets(view)
        initView(savedInstanceState)
        observeViewModel()
        initData()
    }

    /** 尺寸/位置必须在 show 之后设置才生效(Dialog 窗口此时才存在) */
    override fun onStart() {
        super.onStart()
        applyWindowLayout()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDialogDismiss()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    /**
     * 关闭弹框。退场动画由**窗口层**播(见 [animStyleRes]),dismiss 后系统会把窗口移除动画放完,
     * 所以这里可以直接 dismiss,不需要“等 View 动画结束再关”(那种做法会让窗口底色先留在屏上)。
     * [animStyleRes] 为 0 时就是无动画直接关。
     */
    protected fun dismissWithAnimation() = dismissSafely()

    private fun applyWindowLayout() {
        val window = dialog?.window ?: return
        val screenWidth = resources.displayMetrics.widthPixels
        // 皮肤都是不悬浮的窗口,所以两种尺寸写法都可靠:
        // - 比例 1f 直接给 MATCH_PARENT(不用换算屏幕像素:横屏三键导航、刘海、分屏时
        //   displayMetrics.widthPixels 会比窗口可用宽度小,算出来的像素值就会在两侧留下空隙);
        // - 比例小于 1 才需要按像素换算(没有百分比写法可用)。
        // (如果回到悬浮窗口,match_parent 会被主题的 windowMinWidthMajor/Minor 限住,铺不满。)
        val width = when {
            isFullscreen || widthRatio >= 1f -> ViewGroup.LayoutParams.MATCH_PARENT
            else -> (screenWidth * widthRatio).toInt()
        }
        val height = if (isFullscreen) {
            ViewGroup.LayoutParams.MATCH_PARENT
        } else {
            ViewGroup.LayoutParams.WRAP_CONTENT
        }
        window.setLayout(width, height)
        window.setGravity(gravity)
        // 蒙层:全屏皮(窗口不悬浮)默认不带 DIM_BEHIND flag,要浓度就得自己补上
        val params = window.attributes
        params.dimAmount = dimAmount
        if (dimAmount > 0f) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        }
        window.attributes = params
    }

    /** 返回 true 表示这次返回已被消费(拦截或已发起关闭) */
    private fun handleBackKey(): Boolean {
        if (onBackPressed()) return true
        if (cancelable) dismissWithAnimation()
        return true
    }

    /** 全屏弹框的系统栏适配:targetSdk 35+ 强制 edge-to-edge,内容会被状态栏/导航栏压住 */
    private fun installSystemBarInsets(root: View) {
        contentBasePadding = intArrayOf(
            root.paddingLeft, root.paddingTop, root.paddingRight, root.paddingBottom,
        )
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout(),
            )
            applySystemBarInsets(view, bars)
            insets
        }
        root.post { ViewCompat.requestApplyInsets(root) }
    }

    private var contentBasePadding: IntArray? = null

    /**
     * 缺省实现:把系统栏/刘海高度**叠加**到根 View 原有 padding 上,但只补真会被压住的那几条:
     * 顶部只有全屏(或 top 对齐)才补,底部只有全屏(或 bottom 对齐)才补——中间居中的卡片
     * 跟导航栏不搭界,补了就是白给卡片垫高一段。
     * 想只让标题栏加高(背景铺进状态栏的经典观感)就重写本方法,自己分发 inset。
     */
    protected open fun applySystemBarInsets(root: View, bars: Insets) {
        val base = contentBasePadding ?: return
        val touchesTop = isFullscreen || (gravity and Gravity.TOP) == Gravity.TOP
        val touchesBottom = isFullscreen || (gravity and Gravity.BOTTOM) == Gravity.BOTTOM
        root.setPadding(
            base[0] + bars.left,
            base[1] + if (touchesTop) bars.top else 0,
            base[2] + bars.right,
            base[3] + if (touchesBottom) bars.bottom else 0,
        )
    }

    companion object {
        /** 悬浮卡片缺省蒙层浓度 */
        const val DEFAULT_DIM = 0.5f
    }
}

// ==== 调用侧便捷扩展 ====

/**
 * 返回键拦截装配(两个 DialogFragment 基类共用)。
 * ComponentDialog 走 onBackPressedDispatcher;其余走 setOnKeyListener。
 */
internal fun DialogFragment.wireBackInterception(handler: () -> Boolean) {
    val current = dialog ?: return
    if (current is ComponentDialog) {
        current.onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    handler()
                }
            },
        )
    } else {
        current.setOnKeyListener { _, keyCode, event ->
            keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP && handler()
        }
    }
}

/**
 * 安全弹出:
 * - 已经弹出过 / 同 tag 已存在 -> 直接跳过(避免同一弹框叠两层);
 * - 状态已保存(页面切后台时才收到"该弹个框"的回调) -> 降级为 commitAllowingStateLoss,
 *   不再抛经典的 IllegalStateException"Can not perform this action after onSaveInstanceState"。
 */
fun DialogFragment.showSafely(manager: FragmentManager, tag: String = javaClass.name) {
    if (isAdded || manager.findFragmentByTag(tag) != null) return
    if (manager.isStateSaved) {
        runCatching { manager.beginTransaction().add(this, tag).commitAllowingStateLoss() }
    } else {
        runCatching { show(manager, tag) }
    }
}

/** 挂在 Activity 上弹出(用 supportFragmentManager,弹框随 Activity 销毁) */
fun DialogFragment.showOn(activity: FragmentActivity, tag: String = javaClass.name) =
    showSafely(activity.supportFragmentManager, tag)

/** 挂在 Fragment 上弹出(用 childFragmentManager,宿主 Fragment 销毁时弹框跟着关) */
fun DialogFragment.showOn(fragment: Fragment, tag: String = javaClass.name) =
    showSafely(fragment.childFragmentManager, tag)

/** 安全关闭:没 attach / 已 dismiss 时是空操作,不会抛"not attached to FragmentManager" */
fun DialogFragment.dismissSafely() {
    if (!isAdded) return
    runCatching { dismissAllowingStateLoss() }
}
