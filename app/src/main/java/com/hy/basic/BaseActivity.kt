package com.hy.basic

import android.app.Activity
import android.content.Context
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
import com.hy.basic.font.FontScaleManager
import com.hy.basic.hint.ToastKit
import com.hy.basic.network.ApiException
import com.hy.basic.network.ApiExceptionExt
import com.hy.basic.page.PageStateConfig
import com.hy.basic.page.PageStateLayout
import com.hy.basic.page.UiEvent
import com.hy.myapp.databinding.AbsActivityToolbarBinding
import com.hy.myapp.R
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import kotlinx.coroutines.launch

/**
 * @author hy
 * desc: Activity 基类(ViewBinding + ViewModel + 协程/Flow)。
 *
 * 模板方法:initView -> observeViewModel -> initData。
 * 通用能力:ViewBinding / 泛型反射 ViewModel / UiEvent 统一事件分发(Toast/错误/导航/回传等)
 * / 可选 Toolbar / 可选整页多状态(PageStateLayout)。
 *
 * 泛型:VB = 布局 Binding;VM = BaseViewModel 子类。
 * 中间层二次转发泛型时重写 [createViewModel] 手动提供实例。
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

    /** 布局创建前的 inflater 钩子,在 super.onCreate 之前调用 */
    protected open fun onConfigureInflater(inflater: LayoutInflater) {}

    /** 本页是否跟随字号。默认取全局策略,单页重写 false 就整体退出 */
    protected open val followFontScale: Boolean get() = FontScaleManager.followByDefault

    /** 字号要在建 View 前包进 Context,放到 onCreate 就来不及了 */
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(if (followFontScale) FontScaleManager.wrap(newBase) else newBase)
    }

    /** 从其他页面回来时补一次检查 */
    override fun onResume() {
        super.onResume()
        syncFontScale()
    }

    /**
     * 档位变了但页面还在栈里,不会再走 onCreate,只能重建一次。
     * 放到可见时才重建,避免改档瞬间把栈里的页面一起刷了。
     */
    private fun syncFontScale() {
        if (!followFontScale || FontScaleManager.isApplied(this)) return
        window.decorView.post {
            if (!isFinishing && !isDestroyed) recreate()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        onConfigureInflater(layoutInflater)
        super.onCreate(savedInstanceState)
        binding = createBinding(layoutInflater)
        setupContentView()
        viewModel = createViewModel()
        observeGlobalEvents()
        initView(savedInstanceState)
        observeViewModel()
        initData()
    }

    /** 装配页面根布局:需要 Toolbar 或多状态时套壳布局,否则直接 setContentView */
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

    /** edge-to-edge 适配:Toolbar 时状态栏高度并入 Toolbar;否则四边 inset 补在壳根上 */
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
            WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars =
                false
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

    /** 创建 ViewModel,默认泛型反射;泛型无法解析时回退 BaseViewModel,子类可重写 */
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
     * 统一处理一次性 UI 事件:默认按类型分发到各 open 钩子。
     * 需要拦截全部事件(如埋点)时重写本方法,记得调 `super.onUiEvent(event)`。
     */
    protected open fun onUiEvent(event: UiEvent) {
        when (event) {
            is UiEvent.ShowToast -> onShowToast(event.message, event.long)
            is UiEvent.ShowError -> onShowError(event.exception)
            is UiEvent.Navigate -> onNavigate(event)
            UiEvent.GoBack -> onGoBack()
            is UiEvent.Finish -> finishActivity(event.resultCode, event.data)
            is UiEvent.ShowResult -> {
                // 不能再走 finishActivity —— 它内部会再 setResult(null) 把回传数据清空
                setResult(event.resultCode, resultIntentOf(event.values))
                if (!isFinishing) finish()
            }
            is UiEvent.Custom -> onCustomEvent(event.tag, event.data)
        }
    }

    /** Toast 钩子:默认走 [ToastKit] */
    protected open fun onShowToast(message: CharSequence?, long: Boolean) {
        ToastKit.show(message, long)
    }

    /**
     * 错误提示钩子(默认 Toast)。登录态失效等场景可重写:
     * `if (exception.code == ApiExceptionExt.CODE_AUTH_INVALID) { 跳登录 } else super.onShowError(exception)`
     */
    protected open fun onShowError(exception: ApiException) {
        showLongToast(exception.message)
    }

    /** 导航钩子:默认组装 Intent(显式 Class / 任意 Intent)后启动;可重写做转场动画等定制 */
    protected open fun onNavigate(event: UiEvent.Navigate) {
        val intent = when (val target = event.target) {
            is com.hy.basic.page.NavigationTarget.Explicit -> Intent(this, target.activityClass)
            is com.hy.basic.page.NavigationTarget.ByIntent -> Intent(target.intent)
        }
        event.extras?.let { intent.putExtras(it) }
        if (event.flags != 0) intent.flags = event.flags
        runCatching { startActivity(intent) }.onFailure { showLongToast("无法打开目标页面:${it.message}") }
    }

    /** 返回钩子:默认走返回分发器(popup 关闭/折叠头拦截等回调仍生效),无人接管时 finish */
    protected open fun onGoBack() {
        onBackPressedDispatcher.onBackPressed()
    }

    /** 自定义事件钩子([UiEvent.Custom]):业务专属动作,默认仅打日志 */
    protected open fun onCustomEvent(tag: String, data: Any?) {
        showLongToast("[$tag] 未处理的自定义事件")
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
                launch { viewModel.uiEvent.collect(::onUiEvent) }
                launch {
                    viewModel.pageState.collect {
                        pageStateLayout?.bind(it) { onPageRetry() }
                    }
                }
            }
        }
    }

    /** 轻提示:统一走 [ToastKit],要换实现/改样式只动那个入口 */
    protected fun showToast(msg: CharSequence?) {
        ToastKit.show(msg)
    }

    protected fun showLongToast(msg: CharSequence?) {
        ToastKit.show(msg, true)
    }

    /** 关闭当前页:抽成方法供 [onUiEvent] 的 Finish/ShowResult 分支复用 */
    private fun finishActivity(resultCode: Int, data: Intent? = null) {
        if (data != null) setResult(resultCode, data) else setResult(resultCode)
        finish()
    }

    /** 便捷启动目标 Activity:startActivity<TargetActivity> { putExtra(...) } */
    protected inline fun <reified A : Activity> startActivity(block: Intent.() -> Unit = {}) {
        startActivity(Intent(this, A::class.java).apply(block))
    }
}

/** Map 负载 -> Intent extras([UiEvent.ShowResult] 回传用,放在顶层供 Activity/Fragment 共用) */
internal fun resultIntentOf(values: Map<String, Any?>): Intent = Intent().apply {
    values.forEach { (key, value) ->
        when (value) {
            null -> putExtra(key, null as String?)
            is CharSequence -> putExtra(key, value)
            is Int -> putExtra(key, value)
            is Long -> putExtra(key, value)
            is Boolean -> putExtra(key, value)
            is Float -> putExtra(key, value)
            is Double -> putExtra(key, value)
            else -> putExtra(key, value.toString())
        }
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
