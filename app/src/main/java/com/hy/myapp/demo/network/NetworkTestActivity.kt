package com.hy.myapp.demo.network

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.DividerItemDecoration
import com.hy.basic.BaseActivity
import com.hy.myapp.R
import com.hy.myapp.databinding.ActivityNetworkTestBinding
import com.hy.myapp.demo.network.adapter.ForecastAdapter
import kotlinx.coroutines.launch

/**
 * desc: 网络请求测试页(MVVM 基类能力验证)
 *
 * 覆盖能力:
 * - [createBinding]:ViewBinding 绑定入口
 * - 基类泛型反射自动创建 [NetworkTestViewModel],无需手动 new
 * - 模板方法顺序:initView -> observeViewModel -> initData
 * - [showLoading]:由 BaseViewModel.isLoading 引用计数驱动,业务侧不写 loading 逻辑
 * - 全局异常:失败请求由基类 [onApiException] 统一提示,页面不崩溃
 */
class NetworkTestActivity : BaseActivity<ActivityNetworkTestBinding, NetworkTestViewModel>() {

    private val forecastAdapter = ForecastAdapter()

    override fun createBinding(inflater: LayoutInflater) = ActivityNetworkTestBinding.inflate(inflater)

    override fun initView(savedInstanceState: Bundle?) {
        binding.rvForecast.adapter = forecastAdapter
        binding.rvForecast.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        binding.btnSearch.setOnClickListener {
            viewModel.loadWeather(binding.etCity.text?.toString().orEmpty())
        }
        binding.btnEnvelope.setOnClickListener { viewModel.loadEnvelopeData() }
        binding.btnContent.setOnClickListener { viewModel.loadContentData() }
        binding.btnFault.setOnClickListener { viewModel.loadBrokenEndpoint() }

        // 输入为空时禁用查询,顺带验证控件状态管理
        binding.etCity.addTextChangedListener { text ->
            binding.btnSearch.isEnabled = !text.isNullOrBlank()
        }

        applyDataSourceHints()
    }

    /** Mock 模式下调整文案,避免"公共接口 / 404"这类只在线上成立的描述误导 */
    private fun applyDataSourceHints() {
        if (!WeatherApi.USE_MOCK_DATA) return
        binding.tvSubtitle.setText(R.string.demo_network_subtitle_mock)
        binding.btnFault.setText(R.string.demo_network_btn_fault_mock)
    }

    override fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.weather.collect(::renderWeather) }
                launch {
                    viewModel.resultText.collect { text ->
                        binding.tvResult.text =
                            text.ifBlank { getString(R.string.demo_network_result_default) }
                    }
                }
            }
        }
    }

    /**
     * 转屏后 ViewModel 仍存活,StateFlow 保留上一次结果且会被重新收集,
     * 因此只在无数据时发起请求,避免重复的网络开销。
     */
    override fun initData() {
        if (viewModel.weather.value == null) {
            viewModel.loadWeather(NetworkTestViewModel.DEFAULT_CITY)
        }
    }

    private fun renderWeather(state: WeatherUiState?) {
        if (state == null) return
        with(binding) {
            tvLocation.text = "${state.city} · ${state.region}"
            tvIcon.text = state.icon
            tvTemperature.text = state.temperature
            tvDescription.text = state.description
            tvDetail.text = state.detail
            tvUpdatedAt.text = state.updatedAt
            forecastAdapter.submitList(state.daily)
            rvForecast.visibility = if (state.daily.isEmpty()) View.GONE else View.VISIBLE
            tvDailyEmpty.visibility = if (state.daily.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    /**
     * loading 由基类收集 isLoading 后回调:
     * 引用计数保证"并发请求中最后一个结束才隐藏",业务无需判断。
     */
    override fun showLoading(show: Boolean) {
        binding.pbLoading.visibility = if (show) View.VISIBLE else View.GONE
    }
}
