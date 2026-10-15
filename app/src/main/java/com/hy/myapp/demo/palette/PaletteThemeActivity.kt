package com.hy.myapp.demo.palette

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.hy.basic.BaseActivity
import com.hy.basic.utils.ImageLoader
import com.hy.basic.utils.ImagePalette
import com.hy.basic.utils.applyThemeColor
import com.hy.basic.utils.setBackgroundColorTransition
import com.hy.basic.utils.darken
import com.hy.basic.utils.setGradientBackgroundTransition
import com.hy.basic.utils.toHexString
import com.hy.myapp.R
import com.hy.myapp.databinding.ActivityPaletteThemeBinding
import com.google.android.flexbox.FlexboxLayout
import kotlinx.coroutines.launch

/**
 * desc: 图片吸色(Palette)演示页 —— 现代主流写法
 *
 * 复刻"网易云专辑页"那种设计感交互:选一张封面 → 从封面吸取主题色 →
 * 顶部 Toolbar(吸顶色)、头部渐变背景、状态栏图标深浅、色板卡片全部随主题联动,
 * 切图时带颜色过渡动画。对照旧 [com.hy.myapp.activity.PaletteTestActivity](Java + ViewPager 回调式),
 * 这里示范当前推荐的用法:
 * - 取色封装在 [com.hy.basic.utils.ImagePalette](挂起、后台线程、带缓存),页面零 Palette 样板代码;
 * - 前景/背景颜色由主题色计算(可读色随 [ImagePalette.Result.onTheme] 下发,不写死);
 * - 状态:ViewModel StateFlow 驱动,loading/异常由基类兜底;
 * - 落地:Toolbar 换色用 [setBackgroundColorTransition] 纯色过渡,头部用 [setGradientBackgroundTransition] 渐变到渐变的平滑过渡。
 */
class PaletteThemeActivity : BaseActivity<ActivityPaletteThemeBinding, PaletteThemeViewModel>() {

    override val showToolBar: Boolean get() = true

    override val toolbarTitle: CharSequence get() = "图片吸色主题"

    /** 可选封面:url 走网络解码取色,resId 走本地图;颜色差异大,方便看主题联动 */
    private val covers: List<Pair<Any, String>> = listOf(
        "https://www.gstatic.com/webp/gallery/1.jpg" to "风景 · 绿调",
        "https://www.gstatic.com/webp/gallery/2.jpg" to "自然 · 杂色",
        "https://www.gstatic.com/webp/gallery/4.jpg" to "黄昏 · 暖调",
        "https://www.gstatic.com/webp/gallery/5.jpg" to "热烈 · 红调",
        R.mipmap.img_lab_view1 to "头像 · 红白",
        R.mipmap.img_lab_view2 to "头像 · 黄白",
        R.mipmap.cat_touxiang to "头像 · 灰调",
    )

    private val thumbViews = mutableListOf<ImageView>()

    /** 当前主题色,用于缩略图选中描边 */
    private var currentTheme = Color.parseColor("#FF5722")

    override fun createBinding(inflater: LayoutInflater) =
        ActivityPaletteThemeBinding.inflate(inflater)

    override fun initView(savedInstanceState: Bundle?) {
        buildThumbs()
    }

    override fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.theme.collect(::render) }
                launch { viewModel.selectedIndex.collect(::highlightThumb) }
            }
        }
    }

    /** 转屏后 ViewModel 存活,只在从未成功取过色时加载首图,避免重复解码 */
    override fun initData() {
        if (viewModel.theme.value == null) {
            selectCover(0)
        }
    }

    private fun selectCover(index: Int) {
        // 先把封面图本身显示到头部(与取色并行,取色较慢时图也不空)
        ImageLoader.loadImage(binding.ivCover, covers[index].first)
        viewModel.loadPalette(this, covers[index].first, index)
    }

    // ---------------- 缩略图选择区 ----------------

    private fun buildThumbs() {
        val size = dp(72)
        val gap = dp(10)
        covers.forEachIndexed { index, (model, _) ->
            val iv = ImageView(this).apply {
                layoutParams = flexParams(size, size, spacing = gap)
                scaleType = ImageView.ScaleType.CENTER_CROP
                ImageLoader.loadImage(this, model)
                background = roundedStroke(Color.TRANSPARENT)
                contentDescription = "封面${index + 1}"
                setOnClickListener { selectCover(index) }
            }
            thumbViews.add(iv)
            binding.llThumbs.addView(iv)
        }
    }

    private fun highlightThumb(selected: Int) {
        thumbViews.forEachIndexed { i, iv ->
            iv.background = roundedStroke(if (i == selected) currentTheme else Color.parseColor("#33000000"))
        }
    }

    // ---------------- 主题渲染 ----------------

    private fun render(result: ImagePalette.Result?) {
        if (result == null) return
        val theme = result.themeColor
        val onTheme = result.onTheme
        currentTheme = theme

        with(binding) {
            // 头部:主题色"渐变到渐变"平滑过渡(与 Toolbar 同为动画换色,不再瞬间跳变)
            flHeader.setGradientBackgroundTransition(theme, theme.darken(HEADER_DEPTH))
            tvTitle.setTextColor(onTheme)
            tvThemeHex.setTextColor(onTheme)
            tvTip.setTextColor(onTheme)
            tvThemeHex.text = "主题色 ${theme.toHexString()}"
            buildSwatches(result.swatches)
        }

        // Toolbar 吸顶色:带过渡动画换色,标题文字用可读色,状态栏图标深浅自适应
        toolbar?.let { tb ->
            tb.setBackgroundColorTransition(theme)
            tb.setTitleTextColor(onTheme)
            // 返回箭头随主题色着色(浅底换深色箭头/深底换浅色箭头),mutate 避免污染共享的 Drawable 缓存
            tb.navigationIcon?.mutate()?.let { icon ->
                icon.setTint(onTheme)
                tb.navigationIcon = icon
            }
            applyThemeColor(theme)
        }
    }

    private fun buildSwatches(swatches: List<ImagePalette.Swatch>) {
        val container = binding.llSwatches
        container.removeAllViews()
        val chipWidth = dp(64)
        swatches.forEach { swatch ->
            val chip = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = linearParams(chipWidth, LinearLayout.LayoutParams.WRAP_CONTENT, marginEnd = dp(10))
            }
            chip.addView(View(this).apply {
                layoutParams = linearParams(chipWidth, dp(40))
                background = GradientDrawable().apply {
                    setColor(swatch.color)
                    cornerRadius = dp(8).toFloat()
                }
            })
            chip.addView(labelText(swatch.name, 12, R.color.color_333333, bold = true))
            chip.addView(labelText(swatch.color.toHexString(), 11, R.color.color_999999, bold = false))
            container.addView(chip)
        }
    }

    private fun labelText(text: String, sizeSp: Int, colorRes: Int, bold: Boolean) =
        TextView(this).apply {
            this.text = text
            textSize = sizeSp.toFloat()
            setTextColor(resources.getColor(colorRes, theme))
            gravity = Gravity.CENTER
            if (bold) setTypeface(null, Typeface.BOLD)
        }

    /** 选中/未选中缩略图的圆角描边背景 */
    private fun roundedStroke(strokeColor: Int): GradientDrawable = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        cornerRadius = dp(10).toFloat()
        setColor(Color.TRANSPARENT)
        setStroke(dp(3), strokeColor)
    }

    override fun showLoading(show: Boolean) {
        binding.pbLoading.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun linearParams(
        width: Int,
        height: Int,
        marginEnd: Int = 0,
    ): LinearLayout.LayoutParams = LinearLayout.LayoutParams(width, height).also { it.marginEnd = marginEnd }

    /** 缩略图用 Flexbox 流式布局的参数(flexWrap=wrap 自动换行);同时给右/下间距,换行后行与行之间也有空隙 */
    private fun flexParams(
        width: Int,
        height: Int,
        spacing: Int = 0,
    ): FlexboxLayout.LayoutParams = FlexboxLayout.LayoutParams(width, height).also {
        it.marginEnd = spacing
        it.bottomMargin = spacing
    }

    private fun Context.dp(value: Int): Int =
        (value * resources.displayMetrics.density + 0.5f).toInt()

    private companion object {
        /** 头部渐变向下"沉"的深度(主题色 → 加深后色) */
        const val HEADER_DEPTH = 0.2f
    }
}
