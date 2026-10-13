package com.hy.basic

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewbinding.ViewBinding
import com.blankj.utilcode.util.ToastUtils
import com.hy.basic.network.ApiException
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = createBinding(layoutInflater)
        setContentView(binding.root)
        viewModel = createViewModel()
        observeGlobalEvents()
        initView(savedInstanceState)
        observeViewModel()
        initData()
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

    private fun observeGlobalEvents() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.isLoading.collect(::showLoading) }
                launch { viewModel.apiExceptionEvent.collect(::onApiException) }
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
