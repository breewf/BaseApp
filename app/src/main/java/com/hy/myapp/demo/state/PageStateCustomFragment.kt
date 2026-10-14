package com.hy.myapp.demo.state

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.hy.basic.BaseFragment
import com.hy.basic.page.PageStateConfig
import com.hy.myapp.R
import com.hy.myapp.databinding.FragmentPageStateCustomBinding
import kotlinx.coroutines.launch

/**
 * desc: ViewPager Tab2 —— 自定义状态层外观 + 手动指定状态。
 *
 * 与 Tab1 的区别只有两点:
 * - [createPageStateConfig] 返回自己的配置:换整层底色/文案颜色/重试文案,
 *   需要连布局结构一起换时改 `xxxLayoutRes` 指向页面自己的 XML 即可;
 * - 用按钮逐个落到 Loading/空/错误/无网络,方便对比外观。
 *
 * 两个 Tab 各自持有独立的 ViewModel,互不干扰;切页/回滑后状态仍然保留。
 */
class PageStateCustomFragment :
    BaseFragment<FragmentPageStateCustomBinding, PageStateTabViewModel>() {

    override val enablePageState: Boolean get() = true

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentPageStateCustomBinding.inflate(inflater, container, false)

    override fun createPageStateConfig() = PageStateConfig(
        loadingText = "正在玩命加载…",
        emptyText = "一个结果都没有 🤔",
        errorText = "这页打不开了,重试一下",
        noNetworkText = "网线好像被拔了",
        retryText = "点这里重试",
        backgroundRes = R.color.colorPrimary,
        textRes = R.color.white,
        retryTextRes = R.color.white,
    )

    override fun initView(savedInstanceState: Bundle?) {
        binding.btnReload.setOnClickListener { viewModel.loadOnce(TAG) }
        binding.btnStateLoading.setOnClickListener { viewModel.showLoading() }
        binding.btnStateEmpty.setOnClickListener { viewModel.showEmpty("[$TAG] 空状态(自定义外观)") }
        binding.btnStateError.setOnClickListener { viewModel.showError("[$TAG] 错误状态(自定义外观)") }
        binding.btnStateNoNetwork.setOnClickListener {
            viewModel.showNoNetwork("[$TAG] 无网络状态(自定义外观)")
        }
    }

    override fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.content.collect { binding.tvContent.text = it }
            }
        }
    }

    override fun initData() {
        viewModel.loadOnce(TAG)
    }

    /** 状态层「重试」的默认实现是重跑 [initData],这里额外给一点反馈 */
    override fun onPageRetry() {
        showToast("Tab2 收到重试回调")
        super.onPageRetry()
    }

    private companion object {
        const val TAG = "Tab2"
    }
}
