package com.hy.demo.banner

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.google.android.flexbox.FlexboxLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.hy.basic.BaseActivity
import com.hy.basic.banner.Banner
import com.hy.basic.banner.BannerIndicator
import com.hy.basic.banner.BannerTransformer
import com.hy.basic.hint.toast
import com.hy.basic.utils.ImageLoader
import com.hy.myapp.R
import com.hy.myapp.databinding.ActivityBannerDemoBinding
import com.hy.demo.model.DemoLogViewModel
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

/**
 * desc: Banner 轮播用法示例。两种写法都走一遍:
 * 1. 纯图片轮播:样式全在 XML,代码只有一句 setData(urls)
 * 2. 自定义页轮播:data 是任意对象,配合 pageMargin 露边 + FLOW 动画就是卡片流
 */
class BannerDemoActivity : BaseActivity<ActivityBannerDemoBinding, DemoLogViewModel>() {

    /** 一页自定义内容:图 + 标题 —— 传给 [Banner.setData] 的数据不限于 url,任意对象都行 */
    private data class CardPage(@DrawableRes val image: Int, val title: String)

    override val showToolBar: Boolean get() = true

    override val toolbarTitle: CharSequence get() = "Banner 轮播"

    /** 网络图:探测过可访问 */
    private val networkImages = listOf(
        "https://www.gstatic.com/webp/gallery/1.jpg",
        "https://www.gstatic.com/webp/gallery/2.jpg",
        "https://www.gstatic.com/webp/gallery/3.jpg",
        "https://www.gstatic.com/webp/gallery/4.jpg",
        "https://www.gstatic.com/webp/gallery/5.jpg",
    )

    /** 本地图:[Banner] 的数据里放 @DrawableRes 资源 id 一样能加载,不用先转 url */
    private val localImages = listOf(
        R.mipmap.bg_home_create,
        R.mipmap.bg_home_daily,
        R.mipmap.bg_home_fav,
        R.mipmap.bg_home_like,
    )

    private val cards = listOf(
        CardPage(R.mipmap.bg_home_create, "创造 · 把手上的想法落成能跑的东西"),
        CardPage(R.mipmap.bg_home_daily, "日常 · 每天顺手记一点"),
        CardPage(R.mipmap.bg_home_fav, "收藏 · 以后还会回来看"),
        CardPage(R.mipmap.bg_home_like, "喜欢 · 看过一次就记住"),
    )

    /** 指示器样式轮播顺序,最后一个 null = 关掉指示器 */
    private val indicatorStyles = listOf<BannerIndicator.Style?>(
        BannerIndicator.Style.DOTS,
        BannerIndicator.Style.DOTS_SLIDER,
        BannerIndicator.Style.BAR,
        BannerIndicator.Style.TEXT,
        null,
    )

    private var usingNetwork = true
    private var indicatorIndex = 1

    override fun createBinding(inflater: LayoutInflater) =
        ActivityBannerDemoBinding.inflate(inflater)

    override fun initView(savedInstanceState: Bundle?) {
        viewModel.header = "操作日志：记「改了什么配置、点了哪一页」，控件行为直接在上方 Banner 上看效果。"
        setupMainBanner()
        setupCardBanner()
        buildTransformerButtons()
        bindSwitches()
        updateMainInfo()
    }

    override fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.logText.collect { binding.tvLog.text = it }
            }
        }
    }

    /** 1. 最简用法:XML 已配好循环 / 自动轮播 / 动画 / 指示器,这里只喂数据 + 挂回调 */
    private fun setupMainBanner() = with(binding.bannerMain) {
        setData(currentImages())
        onPageSelected = { position ->
            updateMainInfo()
            viewModel.log("bannerMain 翻到第 ${position + 1} 页")
        }
        onPageClick = { position, item ->
            toast("点击了第 ${position + 1} 页")
            viewModel.log("点击 bannerMain 第 ${position + 1} 页 → $item")
        }
        // 逐帧回调:想自己接管指示器、做视差、按滑动比例渐变背景时用这个
        addPageCallback { position, offset, _ ->
            if (offset.roundToInt() == 0 && position != currentItem) updateMainInfo()
        }
    }

    /** 2. 自定义页:每页 inflate banner_item_title,onBind 里按真实下标回填内容与图 */
    private fun setupCardBanner() = with(binding.bannerCard) {
        setData(
            items = cards,
            itemLayoutRes = R.layout.banner_item_title,
        ) { page, item, _ ->
            val card = item as? CardPage ?: return@setData
            ImageLoader.loadImage(page.findViewById(R.id.ivBannerImage), card.image)
            page.findViewById<TextView>(R.id.tvBannerTitle).text = card.title
        }
        onPageClick = { position, _ ->
            toast("卡片：${cards[position].title.take(6)}")
            viewModel.log("点击 bannerCard 第 ${position + 1} 页")
        }
    }

    /** 动画按钮按枚举生成:以后 BannerTransformer 加一种,这里不用改 */
    private fun buildTransformerButtons() {
        BannerTransformer.entries.forEach { effect ->
            binding.flTransformers.addView(
                transformerButton(effect.label.toString()) {
                    // 换内置动画会顺带清掉 setCustomTransformer 设置的自定义动画
                    binding.bannerMain.transformer = effect
                    viewModel.log("bannerMain.transformer = ${effect.name}（${effect.label}）")
                    updateMainInfo()
                },
            )
        }
        // 自定义动画:内置效果不够用时,直接给一段"每页每帧"的 lambda
        binding.flTransformers.addView(
            transformerButton("自定义") {
                binding.bannerMain.setCustomTransformer { page, position ->
                    page.rotation = position * 12f
                    page.alpha = 1f - abs(position).coerceAtMost(1f) * 0.6f
                    page.scaleX = 1f - abs(position).coerceAtMost(1f) * 0.1f
                    page.scaleY = page.scaleX
                }
                viewModel.log("setCustomTransformer { 旋转 + 淡出 + 缩小 }")
                updateMainInfo("自定义动画")
            },
        )
    }

    private fun transformerButton(text: CharSequence, onClick: () -> Unit): Button =
        Button(this).apply {
            isAllCaps = false
            minWidth = dp(64)
            setPadding(dp(4), 0, dp(4), 0)
            this.text = text
            textSize = 12f
            setTextColor(ContextCompat.getColor(this@BannerDemoActivity, R.color.colorPrimary))
            setBackgroundResource(R.drawable.demo_bg_button_outline)
            setOnClickListener { onClick() }
            // FlexboxLayout 的 LayoutParams:宽度自适应 + 自动换行,按钮多了也不会藏到屏幕外
            layoutParams = FlexboxLayout.LayoutParams(
                FlexboxLayout.LayoutParams.WRAP_CONTENT, dp(40),
            ).apply {
                marginEnd = dp(6)
                topMargin = dp(6)
            }
        }

    /** 配置开关:改的都是 Banner 上的公开属性,和写在 XML 里完全等价 */
    private fun bindSwitches() {
        binding.btnPrev.setOnClickListener {
            binding.bannerMain.prev()
            viewModel.log("bannerMain.prev()")
        }
        binding.btnNext.setOnClickListener {
            binding.bannerMain.next()
            viewModel.log("bannerMain.next()")
        }
        binding.btnToggleAuto.setOnClickListener {
            val banner = binding.bannerMain
            banner.autoScroll = !banner.autoScroll
            binding.btnToggleAuto.text = labelAuto(banner.autoScroll)
            viewModel.log("bannerMain.autoScroll = ${banner.autoScroll}（另有 start()/stop() 可临时插队）")
        }
        binding.btnJump.setOnClickListener {
            binding.bannerMain.select(position = 2, smooth = true)
            viewModel.log("bannerMain.select(2, smooth = true) —— 循环下走最近的那一格")
        }
        binding.btnToggleLoop.setOnClickListener {
            val banner = binding.bannerMain
            banner.loop = !banner.loop
            binding.btnToggleLoop.text = if (banner.loop) "循环：开" else "循环：关"
            viewModel.log("bannerMain.loop = ${banner.loop}（关掉后到底就不再往前翻）")
        }
        binding.btnToggleIndicator.setOnClickListener {
            indicatorIndex = (indicatorIndex + 1) % indicatorStyles.size
            val style = indicatorStyles[indicatorIndex]
            binding.bannerMain.indicatorStyle = style
            binding.btnToggleIndicator.text = "指示器：${styleName(style)}"
            // 位置 / 边距也是 Banner 的属性,单独动一下演示"改了立刻生效"
            binding.bannerMain.indicatorGravity = if (style == BannerIndicator.Style.TEXT) {
                Gravity.TOP or Gravity.END
            } else {
                Gravity.BOTTOM or Gravity.END
            }
            viewModel.log("bannerMain.indicatorStyle = ${style?.name ?: "null"}")
        }
        binding.btnToggleData.setOnClickListener {
            usingNetwork = !usingNetwork
            binding.bannerMain.setData(currentImages())
            binding.btnToggleData.text = if (usingNetwork) "换本地图" else "换网络图"
            updateMainInfo()
            viewModel.log(if (usingNetwork) "数据换成网络 url 列表" else "数据换成本地资源 id 列表")
        }
        binding.btnClearLog.setOnClickListener { viewModel.clear() }
    }

    private fun labelAuto(autoScroll: Boolean): String = if (autoScroll) "暂停轮播" else "开始轮播"

    private fun currentImages(): List<Any> = if (usingNetwork) networkImages else localImages

    private fun updateMainInfo(effectName: CharSequence? = null) {
        val banner = binding.bannerMain
        val name = effectName ?: banner.transformer.label
        binding.tvBannerInfo.text = buildString {
            append("第 ${banner.currentItem + 1}/${banner.realItemCount} 页 · 动画：$name")
            append(" · 循环：${if (banner.loop) "开" else "关"}")
            append(" · 自动：${if (banner.autoScroll) banner.interval.toString() + "ms" else "停"}")
            append(" · 翻页动画：${banner.scrollDuration}ms")
        }
    }

    private fun styleName(style: BannerIndicator.Style?) = when (style) {
        null -> "无"
        BannerIndicator.Style.DOTS -> "圆点"
        BannerIndicator.Style.DOTS_SLIDER -> "滑块"
        BannerIndicator.Style.BAR -> "进度条"
        BannerIndicator.Style.TEXT -> "文字"
    }

    private fun dp(value: Int): Int = (resources.displayMetrics.density * value).roundToInt()
}
