package com.hy.myapp.demo.oilprice

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.DividerItemDecoration
import com.hy.basic.BaseActivity
import com.hy.myapp.databinding.ActivityOilPriceBinding
import com.hy.myapp.demo.oilprice.adapter.OilPriceAdapter
import kotlinx.coroutines.launch

/**
 * desc: 油价查询页(第三方裸 JSON 接口 + status 手动校验示例)
 *
 * 与 [com.hy.myapp.demo.network.NetworkTestActivity] 同源结构,用来对照学习:
 * - 同一个网络框架如何适配"没有 code/msg/data 信封"的公共接口
 * - Repository 抽象(Mock/Remote 切换见 [OilPriceApi.USE_MOCK_DATA])
 * - loading、异常全部由 [BaseActivity] 基类兜底,页面只写渲染
 */
class OilPriceActivity : BaseActivity<ActivityOilPriceBinding, OilPriceViewModel>() {

    override val showToolBar: Boolean get() = true

    override val toolbarTitle: CharSequence get() = "油价查询"

    override val enablePageState: Boolean get() = false

    private val priceAdapter = OilPriceAdapter()

    override fun createBinding(inflater: LayoutInflater) = ActivityOilPriceBinding.inflate(inflater)

    override fun initView(savedInstanceState: Bundle?) {
        binding.rvOil.adapter = priceAdapter
        binding.rvOil.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        binding.btnQuery.setOnClickListener {
            viewModel.loadArea(binding.etArea.text?.toString().orEmpty())
        }
        binding.btnAll.setOnClickListener { viewModel.loadAll() }

        // 输入为空时禁用查询按钮
        binding.etArea.addTextChangedListener { text ->
            binding.btnQuery.isEnabled = !text.isNullOrBlank()
        }
    }

    override fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.uiState.collect(::render) }
            }
        }
    }

    /**
     * 转屏后 ViewModel 仍存活,StateFlow 会重新分发上一次结果;
     * 仅在从未成功请求过时才自动拉一次全部,避免重复网络开销。
     */
    override fun initData() {
        if (viewModel.uiState.value == null) {
            viewModel.loadAll()
        }
    }

    private fun render(state: OilPriceUiState?) {
        if (state == null) {
            binding.tvScope.visibility = View.GONE
            binding.tvUpdatedAt.visibility = View.GONE
            binding.rvOil.visibility = View.GONE
            binding.tvEmpty.visibility = View.VISIBLE
            return
        }
        with(binding) {
            tvScope.visibility = View.VISIBLE
            tvScope.text = state.scope
            tvUpdatedAt.visibility = View.VISIBLE
            tvUpdatedAt.text = buildString {
                append("数据日期:").append(state.date.ifBlank { "--" })
                if (state.nextUpdateTime.isNotBlank()) {
                    append("  |  下次更新:").append(state.nextUpdateTime)
                }
            }
            priceAdapter.submitList(state.rows)
            val isEmpty = state.rows.isEmpty()
            rvOil.visibility = if (isEmpty) View.GONE else View.VISIBLE
            tvEmpty.visibility = if (isEmpty) View.VISIBLE else View.GONE
        }
    }

    override fun showLoading(show: Boolean) {
        binding.pbLoading.visibility = if (show) View.VISIBLE else View.GONE
    }
}
