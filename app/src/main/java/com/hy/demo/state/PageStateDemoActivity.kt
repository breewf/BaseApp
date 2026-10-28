package com.hy.demo.state

import android.os.Bundle
import android.view.LayoutInflater
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.hy.basic.BaseActivity
import com.hy.myapp.databinding.ActivityPageStateDemoBinding
import kotlinx.coroutines.launch

/**
 * desc: 整页多状态用法示例。
 *
 * 全页面只做了四件事,状态相关代码为零:
 * 1. [showToolBar] = true —— 基类壳布局提供统一 Toolbar(标题/返回键);
 * 2. [enablePageState] = true —— 基类自动把本页根 View 包裹进 PageStateLayout(在 Toolbar 之下,
 *    loading/error 不会遮住标题栏);
 * 3. 观察业务数据流 [PageStateDemoViewModel.content] 渲染内容;
 * 4. 触发 ViewModel 的状态切换(showPageLoading/…由 ViewModel 内部完成)。
 *
 * 加载 / 空 / 错误 / 无网络 叠加层全部由基类收集 [com.hy.basic.BaseViewModel.pageState] 后驱动,
 * 页面 XML 不写任何状态视图,页面代码也不手动切换 visibility。
 */
class PageStateDemoActivity : BaseActivity<ActivityPageStateDemoBinding, PageStateDemoViewModel>() {

    override val showToolBar: Boolean get() = true

    override val toolbarTitle: CharSequence get() = "多状态页面 Demo"

    override val enablePageState: Boolean get() = true

    override fun createBinding(inflater: LayoutInflater) =
        ActivityPageStateDemoBinding.inflate(inflater)

    override fun initView(savedInstanceState: Bundle?) {
        binding.btnReload.setOnClickListener { viewModel.reload() }
    }

    override fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.content.collect { binding.tvContent.text = it }
            }
        }
    }

    override fun initData() {
        viewModel.reload()
    }
}
