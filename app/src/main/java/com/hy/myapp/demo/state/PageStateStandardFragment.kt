package com.hy.myapp.demo.state

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.hy.basic.BaseFragment
import com.hy.myapp.databinding.FragmentPageStateStandardBinding
import kotlinx.coroutines.launch

/**
 * desc: ViewPager Tab1 —— Fragment 侧使用整页多状态的**最简范式**。
 *
 * 页面里状态相关代码只有 [enablePageState] / [isLazyLoad] 两行:
 * - [enablePageState] = true:BaseFragment 把 [binding] 根 View 包进 PageStateLayout,
 *   状态层只覆盖本 Fragment 的内容区,宿主 Toolbar/TabLayout 不受影响;
 * - [isLazyLoad] = true:ViewPager 预加载的离屏页不会提前发请求,首次可见才 [initData];
 * - 状态层「重试」走基类默认回调 → 重新执行 [initData]。
 */
class PageStateStandardFragment :
    BaseFragment<FragmentPageStateStandardBinding, PageStateTabViewModel>() {

    override val enablePageState: Boolean get() = true

    override val isLazyLoad: Boolean get() = true

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentPageStateStandardBinding.inflate(inflater, container, false)

    override fun initView(savedInstanceState: Bundle?) {
        binding.btnReload.setOnClickListener { viewModel.reload(TAG) }
    }

    override fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.content.collect { binding.tvContent.text = it }
            }
        }
    }

    override fun initData() {
        viewModel.reload(TAG)
    }

    private companion object {
        const val TAG = "Tab1"
    }
}
