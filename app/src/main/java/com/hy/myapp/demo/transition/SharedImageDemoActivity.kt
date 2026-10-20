package com.hy.myapp.demo.transition

import android.os.Bundle
import android.view.LayoutInflater
import com.hy.basic.BaseActivity
import com.hy.basic.BaseViewModel
import com.hy.basic.transition.SharedTransitionKit
import com.hy.basic.utils.ImageLoader
import com.hy.myapp.databinding.ActivitySharedImageDemoBinding

/**
 * desc: 单图共享元素转场入口。图片本身或按钮都能触发,共享的是 ivCover(transitionName 已在布局固定)。
 */
class SharedImageDemoActivity : BaseActivity<ActivitySharedImageDemoBinding, BaseViewModel>() {

    override val showToolBar: Boolean get() = true
    override val toolbarTitle: CharSequence get() = "共享元素转场"

    private val cover = "https://www.gstatic.com/webp/gallery/1.jpg"

    override fun createBinding(inflater: LayoutInflater) =
        ActivitySharedImageDemoBinding.inflate(inflater)

    override fun initView(savedInstanceState: Bundle?) {
        ImageLoader.loadImage(binding.ivCover, cover)

        val openBig = {
            SharedTransitionKit.start(
                source = this,
                shareView = binding.ivCover,
                target = SharedImageDetailActivity::class,
                url = cover,
            )
        }
        binding.ivCover.setOnClickListener { openBig() }
        binding.btnViewBig.setOnClickListener { openBig() }
        binding.btnGoGrid.setOnClickListener { startActivity<SharedGridDemoActivity>() }
    }
}
