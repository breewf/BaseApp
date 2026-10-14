package com.hy.basic

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewbinding.ViewBinding
import com.blankj.utilcode.util.ToastUtils
import com.hy.basic.network.ApiException
import com.hy.basic.page.PageStateConfig
import com.hy.basic.page.PageStateLayout
import com.hy.myapp.databinding.AbsActivityToolbarBinding
import com.hy.myapp.R
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import kotlinx.coroutines.launch

/**
 * @author hy
 * desc: Activity 基类(ViewBinding + ViewModel + 协程/Flow)
 *
 * 通用能力:
 * - [binding]:类型安全的 ViewBinding,由子类 [createBinding] 提供,无反射、无 BR、布局无需 <layout> 根
 * - [viewModel]:泛型反射自动创建,作用域为当前页面,配置变更后复用
 * - 模板方法固定初始化顺序:[initView] -> [observeViewModel] -> [initData]
 * - 全局收集 ViewModel 事件:
 *   [BaseViewModel.apiExceptionEvent] -> [onApiException](默认 Toast,401 等可重写处理),
 *   [BaseViewModel.isLoading] -> [showLoading]/[hideLoading]
 * - 整页多状态(可选):[enablePageState] 为 true 时,基类会把 [binding] 根 View 程序化包裹进
 *   [PageStateLayout],并收集 [BaseViewModel.pageState] 驱动 加载/内容/空/错误/无网络 切换,
 *   页面 XML 无需写任何状态视图,也无需手动切换 visibility;
 *   状态层只覆盖内容区(在 [toolbar] 之下),不会遮住标题栏
 * - 通用 Toolbar(可选):[showToolBar] 为 true 时,基类用壳布局(Toolbar + 内容容器)装配页面,
 *   标题取 [toolbarTitle]、返回键行为取 [showToolBarBack],菜单/自定义视图在 [onToolBarInitialized] 里加
 *
 * 泛型:
 * - VB:布局生成的 Binding 类;VM:[BaseViewModel] 子类。
 * 若 VM 泛型在中间层被二次转发(子类仍是泛型类),请重写 [createViewModel] 手动提供实例。
 *
 * 用法:
 * ```
 * class DemoActivity : BaseActivity<ActivityDemoBinding, DemoViewModel>() {
 *     override fun createBinding(inflater: LayoutInflater) = ActivityDemoBinding.inflate(inflater)
 *     override fun initData() { viewModel.loadData() }
 * }
 * ```
 */
abstract class BaseActivity<VB : ViewBinding, VM : BaseViewModel> : AppCompatActivity() {

    /** ViewBinding 实例,onCreate 后可用 */
    protected lateinit var binding: VB
        private set

    /** 页面 ViewModel,onCreate 后可用 */
    protected lateinit var viewModel: VM
        private set

    /** 子类直接返回 XxxBinding.inflate(layoutInflater) */
    protected abstract fun createBinding(inflater: LayoutInflater): VB

    /**
     * 是否启用整页多状态。为 true 时页面根 View 会被包裹进 [PageStateLayout],
     * 整页状态由 [BaseViewModel.pageState] 驱动。默认 false(行为与旧页面一致)。
     */
    protected open val enablePageState: Boolean get() = false

    /** 是否显示基类统一 Toolbar。默认 false(页面保留自己的头部布局方式)。 */
    protected open val showToolBar: Boolean get() = false

    /** Toolbar 标题,默认取 Activity 的 label(manifest android:label),可重写 */
    protected open val toolbarTitle: CharSequence? get() = title

    /** Toolbar 是否显示返回键(点击走返回分发,无上级时 finish),默认显示 */
    protected open val showToolBarBack: Boolean get() = true

    /** 状态层外观/布局配置,页面重写返回自定义 [PageStateConfig](如整体替换某个状态的 XML) */
    protected open fun createPageStateConfig(): PageStateConfig = PageStateConfig()

    /** Toolbar 初始化完成后回调:在此 inflateMenu / 加自定义视图;未启用 Toolbar 时不触发 */
    protected open fun onToolBarInitialized(toolbar: Toolbar) {}

    private var pageStateLayout: PageStateLayout? = null

    /** 基类 Toolbar;未启用(showToolBar/enablePageState 均为 false)时为 null */
    protected var toolbar: Toolbar? = null
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = createBinding(layoutInflater)
        setupContentView()
        viewModel = createViewModel()
        observeGlobalEvents()
        initView(savedInstanceState)
        observeViewModel()
        initData()
    }

    /**
     * 装配页面根布局:
     * - 需要 Toolbar 或多状态时,套用壳布局(Toolbar 在上,内容在下),
     *   [PageStateLayout] 只包住内容区,状态层不覆盖 Toolbar;
     * - 两者都不需要时保持旧行为,直接 setContentView(binding.root)。
     */
    private fun setupContentView() {
        if (!showToolBar && !enablePageState) {
            setContentView(binding.root)
            return
        }
        val shell = AbsActivityToolbarBinding.inflate(layoutInflater)
        toolbar = shell.toolbar
        setupToolBar(shell.toolbar)
        val content: View = if (enablePageState) {
            PageStateLayout(this, createPageStateConfig()).apply {
                attachContent(binding.root)
                pageStateLayout = this
            }
        } else {
            binding.root
        }
        shell.flPageContent.addView(content)
        setContentView(shell.root)
        applyEdgeToEdgeInsets(shell)
    }

    /**
     * edge-to-edge 适配(targetSdk 35+ 在 Android 15+ 上强制内容侵透系统栏,被动等待不行,
     * 低版本也主动 opt-in 保证行为一致):
     * - 显示 Toolbar 时,状态栏高度并入 Toolbar 顶部 padding 并加高自身 —— 背景色延伸到
     *   状态栏底下(经典彩色状态栏观感),标题/导航图标落在状态栏之下;
     * - 未显示 Toolbar 时,顶部 inset 由内容容器根兼 bottom/left/right 一并 padding 兑现;
     * - bottom/left/right(导航栏/刘海)始终补在壳根上,页面内容不会被遮挡。
     */
    private fun applyEdgeToEdgeInsets(shell: AbsActivityToolbarBinding) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val toolbarContentHeight = shell.toolbar.layoutParams.height
        ViewCompat.setOnApplyWindowInsetsListener(shell.root) { root, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout(),
            )
            if (showToolBar) {
                shell.toolbar.apply {
                    setPadding(0, bars.top, 0, 0)
                    if (layoutParams.height != toolbarContentHeight + bars.top) {
                        layoutParams.height = toolbarContentHeight + bars.top
                        requestLayout()
                    }
                }
                root.setPadding(bars.left, 0, bars.right, bars.bottom)
            } else {
                root.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            }
            insets
        }
        if (showToolBar) {
            // Toolbar 背景为深色 colorPrimary,状态栏图标用浅色(白)
            WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false
        }
    }

    private fun setupToolBar(toolbar: Toolbar) {
        if (!showToolBar) return
        toolbar.visibility = View.VISIBLE
        toolbar.title = toolbarTitle
        if (showToolBarBack) {
            toolbar.setNavigationIcon(R.drawable.abs_ic_arrow_back)
            toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
        }
        onToolBarInitialized(toolbar)
    }

    /**
     * 创建 ViewModel,默认通过泛型反射解析 VM 类型并挂载到 Activity。
     * 泛型无法解析时回退 [BaseViewModel],子类可重写本方法。
     */
    @Suppress("UNCHECKED_CAST")
    protected open fun createViewModel(): VM {
        val vmClass = resolveViewModelClass(javaClass) as Class<VM>
        return ViewModelProvider(this)[vmClass]
    }

    /** 初始化视图:控件、适配器等初始化 */
    protected open fun initView(savedInstanceState: Bundle?) {}

    /** 观察业务数据:LiveData 直接 observe;Flow 用 lifecycleScope + repeatOnLifecycle */
    protected open fun observeViewModel() {}

    /** 加载数据:发起网络请求(launchRequest)、读取缓存等 */
    protected open fun initData() {}

    /**
     * 统一处理网络异常,默认 Toast 提示。
     * 登录态失效等场景可重写:
     * `if (exception.code == ApiExceptionHandler.CODE_AUTH_INVALID) { 跳登录 } else super.onApiException(exception)`
     */
    protected open fun onApiException(exception: ApiException) {
        showToast(exception.message)
    }

    /** 加载态钩子:子类实现(显示/隐藏 ProgressBar、骨架屏等) */
    protected open fun showLoading(show: Boolean) {}

    /** 整页状态下的重试回调,默认重新走一遍 [initData] */
    protected open fun onPageRetry() {
        initData()
    }

    private fun observeGlobalEvents() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.isLoading.collect(::showLoading) }
                launch { viewModel.apiExceptionEvent.collect(::onApiException) }
                launch {
                    viewModel.pageState.collect {
                        pageStateLayout?.bind(it) { onPageRetry() }
                    }
                }
            }
        }
    }

    /** 轻提示 */
    protected fun showToast(msg: CharSequence?) {
        if (!msg.isNullOrBlank()) ToastUtils.showShort(msg)
    }

    /** 便捷启动目标 Activity:startActivity<TargetActivity> { putExtra(...) } */
    protected inline fun <reified A : Activity> startActivity(block: Intent.() -> Unit = {}) {
        startActivity(Intent(this, A::class.java).apply(block))
    }
}

/**
 * 沿继承链解析 [BaseActivity]/[BaseFragment] 第 2 个泛型参数对应的 ViewModel 类型,
 * 无法解析时回退 [BaseViewModel]。
 */
internal fun resolveViewModelClass(clazz: Class<*>): Class<*> {
    var type: Type? = clazz.genericSuperclass
    while (type != null) {
        if (type is ParameterizedType) {
            when (val arg = type.actualTypeArguments.getOrNull(1)) {
                is Class<*> -> return arg
                is ParameterizedType -> return arg.rawType as Class<*>
            }
        }
        type = (type as? Class<*>)?.genericSuperclass
    }
    return BaseViewModel::class.java
}
