package com.hy.basic.transition

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.Drawable
import android.transition.ChangeBounds
import android.transition.ChangeImageTransform
import android.transition.ChangeTransform
import android.transition.TransitionSet
import android.view.View
import android.widget.ImageView
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import kotlin.reflect.KClass

/**
 * desc: 共享元素转场封装。源页 [start] 带共享元素启动,目标页 [bindContainerTarget] 一行接好
 * 容器形变 + Glide 首帧等待。核心坑点都收在这两个方法里,页面不用重复踩。
 *
 * MaterialComponents 主题默认给 windowEnter/ExitTransition 挂了一层 Fade,会在形变之外
 * 叠一整窗交叉淡变(就是转场前后“闪一下 + 淡入淡出”的根因);start/bindContainerTarget 里
 * 统一把进出场 transition 置 null,只留共享元素自己动。
 */
object SharedTransitionKit {

    /** 详情页入参约定 */
    const val EXTRA_URL = "share_url"
    const val EXTRA_URL_RES = "share_url_res"
    const val EXTRA_NAME = "share_name"

    /**
     * 源页面:用 [shareView] 作为共享元素启动 [target]。
     *
     * 坑:shareView 必须已设 transitionName 且已 attach/布局完成(点击回调里满足);
     * name 留空则取 shareView 自己的 transitionName,列表场景每个 item 传唯一名即可复用本方法。
     */
    fun start(
        source: Activity,
        shareView: View,
        target: KClass<out Activity>,
        name: String? = shareView.transitionName,
        url: Any?,
        extras: Intent.() -> Unit = {},
    ) {
        val transitionName = name
            ?: error("shareView 需要先设置 transitionName,或显式传 name")
        // 源页也关掉进出场淡变:否则切走/切回时源窗口会在共享元素之外整块闪一下
        killContentFade(source)
        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
            source,
            Pair(shareView, transitionName),
        )
        source.startActivity(
            Intent(source, target.java).apply {
                putExtra(EXTRA_NAME, transitionName)
                when (url) {
                    is String -> putExtra(EXTRA_URL, url)
                    is Int -> putExtra(EXTRA_URL_RES, url)
                }
                extras()
            },
            options.toBundle(),
        )
    }

    /**
     * 目标页:把 [imageView] 作为共享元素,套容器变换并等 Glide 首帧再放行转场。
     * 在 onCreate(基类 initView)里调用一次即可。
     */
    fun bindContainerTarget(
        activity: Activity,
        imageView: ImageView,
        transitionName: String?,
        model: Any?,
        durationMs: Long = 320L,
    ) {
        if (!transitionName.isNullOrEmpty()) imageView.transitionName = transitionName

        // overlay 关掉:让真正装了大图的 ImageView 参与形变。默认 true 会先动画一张快照,
        // 首帧没图时整段过渡就是一块空白 —— 图片共享元素最典型的坑
        activity.window.sharedElementsUseOverlay = false
        killContentFade(activity)
        activity.window.sharedElementEnterTransition = newSharedTransition(durationMs)
        activity.window.sharedElementReturnTransition = newSharedTransition(durationMs)

        // 先 hold 住入场,等大图 Glide 到位再 startPostponedEnterTransition
        activity.postponeEnterTransition()
        var resumed = false
        val resume = {
            if (!resumed) {
                resumed = true
                activity.startPostponedEnterTransition()
            }
        }
        Glide.with(imageView)
            .load(model)
            .dontAnimate() // 关掉 Glide 自带淡入,免得和容器变换叠成双重动画
            .override(Target.SIZE_ORIGINAL)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable?>,
                    isFirstResource: Boolean
                ): Boolean {
                    // 失败也必须放行,否则永久卡在推迟的转场、页面停在半透明
                    resume()
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable,
                    model: Any,
                    target: Target<Drawable?>?,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    resume()
                    return false
                }
            })
            .into(imageView)
    }

    /**
     * 去掉窗口的进出场内容动画(淡入淡出/闪一下的根源)。置 null 只影响非共享视图的淡变,
     * 共享元素形变由 sharedElement*Transition 独立接管,二者互不干扰。
     */
    private fun killContentFade(activity: Activity) = with(activity.window) {
        enterTransition = null
        exitTransition = null
        reenterTransition = null
        returnTransition = null
    }

    /**
     * Activity 窗口共享元素只能用 android.transition 系列。三条组合:
     * ChangeBounds 走位置尺寸、ChangeTransform 走缩放旋转、ChangeImageTransform 专门补
     * ImageView 图片尺寸变化时的平滑拉伸——缺最后这条,大图过渡会出现跳变。
     */
    private fun newSharedTransition(durationMs: Long) =
        TransitionSet().apply {
            ordering = TransitionSet.ORDERING_TOGETHER
            duration = durationMs
            addTransition(ChangeBounds())
            addTransition(ChangeTransform())
            addTransition(ChangeImageTransform())
        }
}
