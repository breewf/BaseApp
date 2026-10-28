package com.hy.myapp.demo.skin

import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.hy.basic.BaseViewModel
import com.hy.basic.skin.BaseSkinActivity
import com.hy.basic.skin.SkinConfig
import com.hy.basic.skin.SkinEngine
import com.hy.basic.skin.SkinManager
import com.hy.basic.skin.SkinMode
import com.hy.myapp.R
import com.hy.myapp.databinding.ActivitySkinDemoBinding
import com.hy.myapp.demo.adapter.DemoMenuAdapter
import com.hy.myapp.demo.model.DemoEntry
import com.hy.myapp.demo.model.DemoRow
import kotlinx.coroutines.launch

/** 换肤演示:明暗/引擎切换、色板、按压 Drawable、Canvas 自绘、列表复用 */
class SkinDemoActivity : BaseSkinActivity<ActivitySkinDemoBinding, BaseViewModel>() {

    /** 列表复用验证用的适配器 */
    private val previewAdapter = DemoMenuAdapter { showToast(it.title) }

    /** 加载布局 */
    override fun createBinding(inflater: LayoutInflater) = ActivitySkinDemoBinding.inflate(inflater)

    /** 绑定列表和按钮,订阅皮肤变化 */
    override fun initView(savedInstanceState: Bundle?) {
        binding.rvPreview.adapter = previewAdapter
        previewAdapter.submitList(previewRows())

        binding.tvFollow.setOnClickListener { SkinManager.switchTo(SkinMode.FOLLOW_SYSTEM) }
        binding.tvLight.setOnClickListener { SkinManager.switchTo(SkinMode.LIGHT) }
        binding.tvDark.setOnClickListener { SkinManager.switchTo(SkinMode.DARK) }
        binding.tvEngine.setOnClickListener { toggleEngine() }
        // 按下反馈来自 drawable selector,切肤时整张图会重新取一次
        binding.tvPressable.setOnClickListener { showToast("按压态由 drawable 提供") }
        // Tab + Fragment 验证放在这里,总入口对应位置换成了字号设置
        binding.tvFragmentDemo.setOnClickListener {
            startActivity(Intent(this, SkinFragmentDemoActivity::class.java))
        }

        // 皮肤变化后重绘文案和色板,系统栏由基类处理
        lifecycleScope.launch {
            SkinManager.isDarkFlow.collect {
                renderMode()
                renderSwatches()
            }
        }
    }

    /** 自研 / 官方来回切 */
    private fun toggleEngine() {
        val next = if (SkinConfig.engine == SkinEngine.SELF) SkinEngine.COMPAT else SkinEngine.SELF
        SkinManager.setEngine(next)
        renderMode()
    }

    /** 刷新模式和引擎文案 */
    private fun renderMode() = with(binding) {
        tvCurrentMode.text =
            "当前：${SkinManager.mode.label} · ${if (SkinManager.isDark) "深色" else "浅色"}"
        tvEngine.text = if (SkinConfig.engine == SkinEngine.SELF) {
            "引擎：自研（不重启）"
        } else {
            "引擎：官方（重建）"
        }
        tvEngineTip.text = if (SkinConfig.engine == SkinEngine.SELF) {
            "点一下切到官方实现：走 AppCompatDelegate + recreate，页面会重建一次；再点回到自研。"
        } else {
            "当前是官方实现，切回自研后不再重建。两种引擎共用同一套 values-night 资源。"
        }
    }

    /** 重建色板,颜色按资源 id 取 */
    private fun renderSwatches() {
        val container = binding.llSwatches
        container.removeAllViews()
        SWATCHES.forEach { (name, colorRes) ->
            val item = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f,
                )
            }
            item.addView(View(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dp(36),
                )
                background = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = dp(8).toFloat()
                    setColor(SkinManager.color(colorRes))
                    setStroke(dp(1), SkinManager.color(R.color.skin_divider))
                }
            })
            item.addView(TextView(this).apply {
                text = name
                textSize = 10f
                gravity = Gravity.CENTER
                setTextColor(SkinManager.color(R.color.skin_text_secondary))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                ).apply { topMargin = dp(4) }
            })
            container.addView(item)
        }
    }

    /** 列表数据 */
    private fun previewRows(): List<DemoRow> =
        listOf(DemoRow.Header("列表复用验证")) +
            (1..20).map { DemoRow.Entry(DemoEntry("条目 $it", "切肤后滚动列表，颜色应始终保持当前皮肤")) }

    /** dp 转 px */
    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()

    private companion object {
        /** 色板项:名字 + 颜色资源 */
        val SWATCHES = listOf(
            "page" to R.color.skin_bg_page,
            "card" to R.color.skin_bg_card,
            "text" to R.color.skin_text_primary,
            "accent" to R.color.skin_accent,
        )
    }
}
