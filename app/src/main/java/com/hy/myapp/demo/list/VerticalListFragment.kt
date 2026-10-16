package com.hy.myapp.demo.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.hy.basic.BaseFragment
import com.hy.basic.utils.addDivider
import com.hy.basic.utils.applyDefaults
import com.hy.basic.utils.attachAdapter
import com.hy.basic.utils.bindPaging
import com.hy.basic.utils.loadMoreDone
import com.hy.basic.utils.refreshDone
import com.hy.basic.utils.refreshFailed
import com.hy.basic.utils.resolvePagerConflict
import com.hy.basic.utils.setupLinear
import com.hy.myapp.databinding.FragmentListVerticalBinding
import com.hy.myapp.demo.list.adapter.FeedAdapter
import kotlinx.coroutines.launch

/**
 * desc: 竖向列表页 —— SmartRefreshLayout 下拉/上拉 + 整页多状态(pageState)的完整参考流程。
 *
 * 装配全部走 com.hy.basic.utils 的扩展,页面代码只表达"意图":
 * - 列表:`rv.setupLinear().addDivider().attachAdapter(adapter)`;
 * - 刷新:`srl.applyDefaults().bindPaging(...)`,手势回调里 launch ViewModel 挂起方法,拿到结果再收尾动画;
 * - 多状态:`enablePageState = true` 后,整页 Loading/Empty/Error/NoNetwork 由基类 PageStateLayout 消费
 *   [com.hy.basic.BaseViewModel.pageState] 驱动,本页 XML 无任何状态视图;状态层「重试」默认回调 → [initData]。
 *
 * 顶部控制条按钮用于主动演示各终态(点空态/错误后,点状态层「重试」回到内容,即可继续下拉/上拉)。
 */
class VerticalListFragment :
    BaseFragment<FragmentListVerticalBinding, ListFeedViewModel>() {

    override val enablePageState: Boolean get() = true

    override val isLazyLoad: Boolean get() = true

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentListVerticalBinding.inflate(inflater, container, false)

    private val adapter by lazy { FeedAdapter() }

    override fun initView(savedInstanceState: Bundle?) {
        binding.rvFeed.setupLinear().addDivider().resolvePagerConflict().attachAdapter(adapter)

        // BRVAH4 自带 item 点击/长按监听(回调给适配器+position),无需在 ViewHolder 里手写 setOnClickListener
        adapter.setOnItemClickListener { _, _, position ->
            adapter.items.getOrNull(position)?.let { item ->
                showToast("点击了第 ${position + 1} 条:${item.title}")
            }
        }
        adapter.setOnItemLongClickListener { _, _, position ->
            adapter.items.getOrNull(position)?.let { item ->
                showToast("长按了:${item.tag} · ${item.author}")
            }
            true
        }

        binding.srlFeed.applyDefaults().bindPaging(
            onRefresh = {
                viewLifecycleOwner.lifecycleScope.launch {
                    if (viewModel.pullRefresh()) binding.srlFeed.refreshDone(viewModel.hasMore)
                    else binding.srlFeed.refreshFailed()
                }
            },
            onLoadMore = {
                viewLifecycleOwner.lifecycleScope.launch {
                    binding.srlFeed.loadMoreDone(viewModel.loadMore())
                }
            },
        )

        // Loading 状态无"重试"语义(见 PageStateLayout),故用一次完整加载演示:短暂 Loading 后自动落回 Content
        binding.btnStateLoading.setOnClickListener { viewModel.loadFirst() }
        binding.btnStateEmpty.setOnClickListener { viewModel.demoEmpty() }
        binding.btnStateError.setOnClickListener { viewModel.demoError() }
        binding.btnStateNoNetwork.setOnClickListener { viewModel.demoNoNetwork() }
        binding.btnRefreshFail.setOnClickListener {
            viewModel.armRefreshFailOnce()
            showToast("已预埋,下一次下拉刷新将失败,请下拉体验")
        }
    }

    override fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.items.collect { adapter.submitList(it) }
            }
        }
    }

    override fun initData() {
        viewModel.loadFirst()
    }
}
