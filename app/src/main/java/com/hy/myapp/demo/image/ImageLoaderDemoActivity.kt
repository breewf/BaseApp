package com.hy.myapp.demo.image

import android.os.Bundle
import android.view.LayoutInflater
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.hy.basic.BaseActivity
import com.hy.basic.hint.toast
import com.hy.basic.utils.ImageLoader
import com.hy.myapp.R
import com.hy.myapp.databinding.ActivityImageLoaderDemoBinding
import com.hy.myapp.demo.model.DemoLogViewModel
import kotlinx.coroutines.launch

/**
 * desc: 图片加载用法示例。一个页面把 ImageLoader 的常用姿势各走一遍:
 * 圆形/圆角/四角独立圆角/带回调/缩略图过渡/GIF/协程取 Bitmap/预加载。
 */
class ImageLoaderDemoActivity : BaseActivity<ActivityImageLoaderDemoBinding, DemoLogViewModel>() {

    override val showToolBar: Boolean get() = true

    override val toolbarTitle: CharSequence get() = "图片加载"

    private val img1 = "https://www.gstatic.com/webp/gallery/1.jpg"
    private val img2 = "https://www.gstatic.com/webp/gallery/2.jpg"
    private val img3 = "https://www.gstatic.com/webp/gallery/3.jpg"
    private val img4 = "https://www.gstatic.com/webp/gallery/4.jpg"

    /** 横图示例:用来看 centerCrop(裁两侧铺满)与 fitCenter(上下留白)的对比;用与其余格子同源的 gstatic 图以保证可访问 */
    private val imgWide = "https://www.gstatic.com/webp/gallery/1.jpg"

    /** 必然加载失败的地址,用来演示 error 占位图与失败回调 */
    private val badUrl = "https://image-loader-demo.invalid/definitely-not-here.jpg"

    override fun createBinding(inflater: LayoutInflater) =
        ActivityImageLoaderDemoBinding.inflate(inflater)

    override fun initView(savedInstanceState: Bundle?) {
        viewModel.header = "操作日志:记「点了哪个按钮、回调拿到了什么」,静态示例进入页面即加载。"
        loadStaticExamples()
        bindActions()
    }

    override fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.logText.collect { binding.tvLog.text = it }
            }
        }
    }

    /** 进入页面就展示的一批静态示例(不需要点按钮) */
    private fun loadStaticExamples() = with(binding) {
        // 一、形状变换
        ImageLoader.loadCircleImage(ivCircle, R.mipmap.cat_touxiang)
        ImageLoader.loadRoundImage(ivRound, img1, radiusDp = 12)
        // 上圆角大、下直角:卡片头图常见
        ImageLoader.loadCustomRoundImage(ivRoundTop, img2, topLeftDp = 24, topRightDp = 24, bottomLeftDp = 0, bottomRightDp = 0)
        // 只把左侧两个角做圆:分割线/半圆标签效果
        ImageLoader.loadCustomRoundImage(ivRoundFrame, img3, topLeftDp = 40, topRightDp = 0, bottomLeftDp = 40, bottomRightDp = 0)

        // 二、占位图 & 错误图
        ImageLoader.loadImage(ivHolderOk, img4, placeholder = R.drawable.img_placeholder, error = R.drawable.img_error)
        ImageLoader.loadImage(ivHolderError, badUrl, placeholder = R.drawable.img_placeholder, error = R.drawable.img_error)

        // 五、缩略图过渡(先出 10% 模糊图再刷清) + 本地 GIF
        ImageLoader.loadWithThumbnail(ivThumb, img1, thumbnail = 0.1f)
        ImageLoader.loadGifImage(ivGif, R.mipmap.xiaohuangren)
    }

    private fun bindActions() = with(binding) {
        btnCallbackOk.setOnClickListener { demoCallbackSuccess() }
        btnCallbackFail.setOnClickListener { demoCallbackFailure() }
        btnLoadBitmap.setOnClickListener { demoLoadBitmap() }
        btnPreload.setOnClickListener { demoPreload() }
        btnClearLog.setOnClickListener { viewModel.clear() }
    }

    /** 4. 带回调的加载:成功后拿解码尺寸,并给图片加个淡入 */
    private fun demoCallbackSuccess() {
        with(binding) {
            tvCallbackStatus.text = "加载中…"
            viewModel.log("loadWithCallback(正常地址)")
            ImageLoader.loadWithCallback(
                imageView = ivCallback,
                model = img2,
                onReady = { _, width, height ->
                    tvCallbackStatus.text = "✅ 成功:解码尺寸 ${width}×${height}px"
                    ivCallback.animate().alpha(1f).setDuration(300).start()
                    viewModel.log("回调 onReady → ${width}×$height")
                },
                onFailed = { e ->
                    tvCallbackStatus.text = "❌ 失败:${e?.message}"
                    viewModel.log("回调 onFailed → ${e?.javaClass?.simpleName}")
                },
            )
        }
    }

    /** 4. 带回调的加载:错误地址走 onFailed */
    private fun demoCallbackFailure() {
        with(binding) {
            tvCallbackStatus.text = "加载中…"
            ivCallback.alpha = 1f
            viewModel.log("loadWithCallback(错误地址) → 走失败分支")
            ImageLoader.loadWithCallback(
                imageView = ivCallback,
                model = badUrl,
                onReady = { _, w, h ->
                    tvCallbackStatus.text = "✅ 意外成功:${w}×$h"
                },
                onFailed = { e ->
                    tvCallbackStatus.text = "❌ 失败:${e?.javaClass?.simpleName}(可在此提示重试)"
                    viewModel.log("回调 onFailed → 加载失败,已捕获")
                },
            )
        }
    }

    /** 6. 协程取 Bitmap:挂起拿到 Bitmap 后展示并报告尺寸 */
    private fun demoLoadBitmap() {
        viewModel.log("loadBitmap( ) 挂起等待解码…")
        binding.tvBitmapInfo.text = "取 Bitmap 中…"
        lifecycleScope.launch {
            val bitmap = ImageLoader.loadBitmap(this@ImageLoaderDemoActivity, img4)
            if (bitmap != null) {
                binding.ivBitmap.setImageBitmap(bitmap)
                binding.tvBitmapInfo.text = "✅ Bitmap ${bitmap.width}×${bitmap.height}(可用于取色/保存等二次处理)"
                viewModel.log("loadBitmap 拿到 ${bitmap.width}×${bitmap.height}")
            } else {
                binding.tvBitmapInfo.text = "❌ 未取得 Bitmap"
                viewModel.log("loadBitmap 返回 null")
            }
        }
    }

    /** 6. 预加载:提前把图写进缓存,后续真正显示时秒出 */
    private fun demoPreload() {
        viewModel.log("preload( ) 开始写入缓存…")
        ImageLoader.preload(this, img4) { succeed ->
            val msg = if (succeed) "preload 成功,已缓存" else "preload 失败"
            toast(msg)
            viewModel.log(msg)
        }
    }
}
