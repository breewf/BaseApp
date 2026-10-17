package com.hy.basic

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewbinding.ViewBinding
import com.hy.basic.hint.ToastKit
import com.hy.basic.network.ApiException
import com.hy.basic.page.PageStateConfig
import com.hy.basic.page.PageStateLayout
import com.hy.basic.page.UiEvent
import kotlinx.coroutines.launch

/**
 * @author hy
 * desc: Fragment 基类(ViewBinding + ViewModel + 协程/Flow)
 *
 * 通用能力:
 * - [binding]:类型安全的 ViewBinding,由子类 [createBinding] 提供;
 *   在 onDestroyView 时自动置空,杜绝 View 泄漏
 * - [viewModel]:泛型反射自动创建;作用域为 Fragment,View 重建(如 ViewPager2 回滑)时数据不丢
 * - 模板方法固定初始化顺序:[initView] -> [observeViewModel] -> [initData]
 * - 懒加载:[isLazyLoad] 为 true 时,首次对用户可见才执行 [initData]
 * - 全局收集 ViewModel 事件(基于 viewLifecycleOwner,随 View 销毁自动停止):
 *   [BaseViewModel.uiEvent] 统一事件通道 -> [onUiEvent] 分发到 onShowToast/onShowError/
 *   onNavigate/onGoBack 等钩子(与 BaseActivity 同一套语义),
 *   [BaseViewModel.isLoading] -> [showLoading]
 * - 整页多状态(可选):[enablePageState] 为 true 时,[binding] 根 View 被包裹进 [PageStateLayout],
 *   整页状态由 [BaseViewModel.pageState] 驱动,页面 XML 零状态视图
 * - ActivityResult:推荐直接在点击里用 [awaitResult]/[awaitResultBundle]
 *   (无 UI 宿主挂在 childFragmentManager 上代持注册,见 LauncherSpec)
 *
 * 泛型:
 * - VB:布局生成的 Binding 类;VM:[BaseViewModel] 子类。
 * 若 VM 泛型在中间层被二次转发,请重写 [createViewModel] 手动提供实例。
 */
abstract class BaseFragment<VB : ViewBinding, VM : BaseViewModel> : Fragment() {

    private var _binding: VB? = null

    /** ViewBinding 实例,仅存在于 onCreateView ~ onDestroyView 之间 */
    protected val binding: VB
        get() = requireNotNull(_binding) {
            "${javaClass.simpleName} binding 仅在 View 存活期间(onCreateView ~ onDestroyView)可用"
        }

    /** 页面 ViewModel,onCreate 后可用,View 重建后仍复用同一实例 */
    protected lateinit var viewModel: VM
        private set

    /** 子类直接返回 XxxBinding.inflate(inflater, container, false) */
    protected abstract fun createBinding(inflater: LayoutInflater, container: ViewGroup?): VB

    /** true:首次对用户可见时才执行 [initData](ViewPager/ViewPager2 场景的懒加载) */
    open val isLazyLoad: Boolean get() = false

    /**
     * 是否启用整页多状态。为 true 时 [binding] 根 View 会被包裹进 [PageStateLayout],
     * 整页状态由 [BaseViewModel.pageState] 驱动。默认 false。
     * Toolbar 由宿主 Activity 统一提供,状态层只覆盖 Fragment 自身内容。
     */
    protected open val enablePageState: Boolean get() = false

    /** 状态层外观/布局配置,页面重写返回自定义 [PageStateConfig](如整体替换某个状态的 XML) */
    protected open fun createPageStateConfig(): PageStateConfig = PageStateConfig()

    private var pageStateLayout: PageStateLayout? = null

    private var isDataInit = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = createViewModel()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val vbd: VB = createBinding(inflater, container)
        _binding = vbd
        return if (enablePageState) {
            PageStateLayout(requireContext(), createPageStateConfig()).apply {
                attachContent(vbd.root)
                pageStateLayout = this
            }
        } else {
            vbd.root
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeGlobalEvents()
        initView(savedInstanceState)
        observeViewModel()
        if (isLazyLoad) {
            initDataIfNeeded()
        } else {
            isDataInit = true
            initData()
        }
    }

    /**
     * 创建 ViewModel,默认通过泛型反射解析 VM 类型并挂载到 Fragment,
     * 需要与 Activity 或其他 Fragment 共享时重写本方法,使用 `requireActivity()` 作为 owner。
     */
    @Suppress("UNCHECKED_CAST")
    protected open fun createViewModel(): VM {
        val vmClass = resolveViewModelClass(javaClass) as Class<VM>
        return ViewModelProvider(this)[vmClass]
    }

    /** 初始化视图:控件、适配器等初始化 */
    protected open fun initView(savedInstanceState: Bundle?) {}

    /** 观察业务数据:LiveData 观察者务必使用 [viewLifecycleOwner] */
    protected open fun observeViewModel() {}

    /** 加载数据:发起网络请求(launchRequest)、读取缓存等 */
    protected open fun initData() {}

    /**
     * 统一处理一次性 UI 事件:默认按类型分发到各 open 钩子(与 [BaseActivity] 同一套语义)。
     * 导航/关页作用于宿主 Activity;回传结果([UiEvent.ShowResult])仅对
     * 以 ActivityResult 方式被拉起的场景有意义。
     */
    protected open fun onUiEvent(event: UiEvent) {
        when (event) {
            is UiEvent.ShowToast -> onShowToast(event.message, event.long)
            is UiEvent.ShowError -> onShowError(event.exception)
            is UiEvent.Navigate -> onNavigate(event)
            UiEvent.GoBack -> onGoBack()
            is UiEvent.Finish -> requireActivity().finish(event.resultCode, event.data)
            is UiEvent.ShowResult -> {
                val host = requireActivity()
                host.setResult(event.resultCode, resultIntentOf(event.values))
                host.finish()
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

    /** 导航钩子:默认组装 Intent(显式 Class / 任意 Intent)后启动 */
    protected open fun onNavigate(event: UiEvent.Navigate) {
        val intent = when (val target = event.target) {
            is com.hy.basic.page.NavigationTarget.Explicit -> Intent(requireContext(), target.activityClass)
            is com.hy.basic.page.NavigationTarget.ByIntent -> Intent(target.intent)
        }
        event.extras?.let { intent.putExtras(it) }
        if (event.flags != 0) intent.flags = event.flags
        runCatching { startActivity(intent) }
            .onFailure { showLongToast("无法打开目标页面:${it.message}") }
    }

    /** 返回钩子:默认交还宿主 Activity 的返回分发器 */
    protected open fun onGoBack() {
        requireActivity().onBackPressedDispatcher.onBackPressed()
    }

    /** 自定义事件钩子([UiEvent.Custom]):业务专属动作,默认仅提示未处理 */
    protected open fun onCustomEvent(tag: String, data: Any?) {
        showLongToast("[$tag] 未处理的自定义事件")
    }

    /** 加载态钩子:子类实现,内部只能使用 [viewLifecycleOwner] */
    protected open fun showLoading(show: Boolean) {}

    /** 整页状态下的重试回调,默认重新走一遍 [initData] */
    protected open fun onPageRetry() {
        initData()
    }

    private fun observeGlobalEvents() = viewLifecycleOwner.lifecycleScope.launch {
        viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            launch { viewModel.isLoading.collect(::showLoading) }
            launch { viewModel.uiEvent.collect(::onUiEvent) }
            launch {
                viewModel.pageState.collect {
                    pageStateLayout?.bind(it) { onPageRetry() }
                }
            }
        }
    }

    /** 懒加载:Fragment resumed 且未隐藏时触发一次 initData */
    private fun initDataIfNeeded() {
        if (isLazyLoad && !isDataInit && isResumed && !isHidden) {
            isDataInit = true
            initData()
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        initDataIfNeeded()
    }

    override fun onResume() {
        super.onResume()
        initDataIfNeeded()
    }

    /** View 销毁时必须置空 binding,否则异步回调会泄漏已销毁的 View */
    override fun onDestroyView() {
        _binding = null
        pageStateLayout = null
        super.onDestroyView()
    }

    /** 轻提示:统一走 [ToastKit],要换实现/改样式只动那个入口 */
    protected fun showToast(msg: CharSequence?) {
        ToastKit.show(msg)
    }

    protected fun showLongToast(msg: CharSequence?) {
        ToastKit.show(msg, true)
    }

    /** 便捷启动目标 Activity:startActivity<TargetActivity> { putExtra(...) } */
    protected inline fun <reified A : Activity> startActivity(block: Intent.() -> Unit = {}) {
        requireActivity().startActivity(Intent(requireContext(), A::class.java).apply(block))
    }
}

/** Activity.finish(resultCode, data) 的中转(minSdk 23 上 Activity#finish(Intent) 仅 API 30+,改用 setResult+finish 等价实现) */
private fun Activity.finish(resultCode: Int, data: android.content.Intent?) {
    if (data != null) setResult(resultCode, data) else setResult(resultCode)
    finish()
}
