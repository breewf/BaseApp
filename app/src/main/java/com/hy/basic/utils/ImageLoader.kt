package com.hy.basic.utils

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.hy.baseapp.common.iface.IImageLoadCompleteListener

/**
 * 图片加载工具类（基于 Glide）。
 *
 * 全静态方法调用，无需单例；已移除 Fresco 相关实现。
 * Created by hy on 2016/4/29.
 */
object ImageLoader {

    /**
     * 加载普通图片（支持 url 字符串或资源 id）。
     *
     * @param imageView 目标 ImageView
     * @param model     图片地址（String）或资源 id（Int）等
     */
    @JvmStatic
    fun loadImage(imageView: ImageView, model: Any?) {
        Glide.with(imageView).load(model).into(imageView)
    }

    /**
     * 加载图片并以 Bitmap 回调（回调中自行处理 Bitmap，可获取原图尺寸等）。
     *
     * @param imageView 目标 ImageView（用于绑定生命周期）
     * @param model     图片地址（String）或资源 id（Int）等
     * @param listener  加载完成回调
     */
    @JvmStatic
    fun loadImage(imageView: ImageView, model: Any?, listener: IImageLoadCompleteListener?) {
        Glide.with(imageView).asBitmap().load(model).into(object : CustomTarget<Bitmap>() {
            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                listener?.loadComplete(resource)
            }

            override fun onLoadCleared(placeholder: Drawable?) {
                // 请求被回收时无需处理，交由 Glide 管理
            }
        })
    }

    /**
     * 加载圆形图片。
     *
     * @param imageView 目标 ImageView
     * @param model     图片地址（String）或资源 id（Int）等
     */
    @JvmStatic
    fun loadCircleImage(imageView: ImageView, model: Any?) {
        Glide.with(imageView).load(model).circleCrop().into(imageView)
    }

    /**
     * 加载 Gif 图，自动播放。
     *
     * @param imageView 目标 ImageView
     * @param model     图片地址（String）或资源 id（Int）等
     */
    @JvmStatic
    fun loadGifImage(imageView: ImageView, model: Any?) {
        Glide.with(imageView).load(model).into(imageView)
    }

}
