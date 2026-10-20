package com.hy.myapp.demo.transition

import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.addCallback
import com.hy.basic.BaseActivity
import com.hy.basic.BaseViewModel
import com.hy.basic.transition.SharedTransitionKit
import com.hy.myapp.databinding.ActivitySharedImageDetailBinding

/**
 * desc: 共享元素大图页。由单图页 / 网格页带 transitionName + url 拉起,一行 bindContainerTarget
 * 接好容器变换与 Glide 首帧等待;返回走 finishAfterTransition 让反向动画跑完。
 */
class SharedImageDetailActivity :
    BaseActivity<ActivitySharedImageDetailBinding, BaseViewModel>() {

    override val showToolBar: Boolean get() = true
    override val toolbarTitle: CharSequence get() = "大图预览"

    override fun createBinding(inflater: LayoutInflater) =
        ActivitySharedImageDetailBinding.inflate(inflater)

    override fun initView(savedInstanceState: Bundle?) {
        val url = intent.getStringExtra(SharedTransitionKit.EXTRA_URL)
        val resId = intent.getIntExtra(SharedTransitionKit.EXTRA_URL_RES, 0)
        val model: Any? = url ?: resId.takeIf { it != 0 }

        SharedTransitionKit.bindContainerTarget(
            activity = this,
            imageView = binding.ivBigImage,
            transitionName = intent.getStringExtra(SharedTransitionKit.EXTRA_NAME),
            model = model,
        )

        binding.ivBigImage.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        // 关键:共享元素返场必须 finishAfterTransition,直接 finish() 会让反向动画被截断
        onBackPressedDispatcher.addCallback(this) { finishAfterTransition() }
    }
}
