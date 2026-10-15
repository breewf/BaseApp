package com.hy.basic.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.hy.baseapp.common.iface.IImageLoadCompleteListener
import java.io.File
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * 图片加载工具类（基于 Glide）。
 *
 * 收敛成一个入口，页面只关心“加载成什么样子”，不重复写 Glide 链式调用。
 * 常见能力都覆盖到：普通加载 / 形状变换（圆形、圆角、四角独立圆角）/
 * 占位图与错误图 / 加载监听回调 / 缩略图过渡 / 预加载 /
 * 协程取 Bitmap / 下载原图 / 生命周期与缓存管理。
 *
 * 约定：所有 `model` 参数支持网络 url（String）、本地资源 id（Int）、File、Uri 等 Glide 能识别的类型。
 * 所有 `radius` / 圆角参数单位是 dp，内部按屏幕密度换算成 px。
 *
 * Created by hy on 2026/10/15.
 */
object ImageLoader {

    // ------------------------------------------------------------------
    // 一、基础加载
    // ------------------------------------------------------------------

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
     * 加载 Gif 图，自动播放。
     *
     * @param imageView 目标 ImageView
     * @param model     图片地址（String）或资源 id（Int）等
     */
    @JvmStatic
    fun loadGifImage(imageView: ImageView, model: Any?) {
        Glide.with(imageView).load(model).into(imageView)
    }

    /**
     * 通用入口：把任意 [RequestOptions] 透传给 Glide，满足上面没专门覆盖的个性化需求
     * （如跳过缓存、指定格式、组合变换等）。
     */
    @JvmStatic
    fun loadWithOptions(imageView: ImageView, model: Any?, options: RequestOptions?) {
        var request = Glide.with(imageView).load(model)
        if (options != null) request = request.apply(options)
        request.into(imageView)
    }

    // ------------------------------------------------------------------
    // 二、形状变换
    // ------------------------------------------------------------------

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
     * 加载圆角图片（四角统一半径）。
     *
     * @param radiusDp 圆角半径，单位 dp
     *
     * 先 [CenterCrop] 把图解码裁剪成 ImageView 尺寸，再 [RoundedCorners] 做圆角；
     * 否则 Glide 只按原比例降采样，View 的 centerCrop 会把已圆角的图非等比拉伸，圆角会变形/看似的消失。
     */
    @JvmStatic
    fun loadRoundImage(imageView: ImageView, model: Any?, radiusDp: Int) {
        val radiusPx = dpToPx(imageView, radiusDp)
        Glide.with(imageView).load(model)
            .apply(RequestOptions.bitmapTransform(MultiTransformation(CenterCrop(), RoundedCorners(radiusPx))))
            .into(imageView)
    }

    /**
     * 加载“特殊圆角”图片：四个角各自独立半径（Glide 内置 RoundedCorners 只能四角统一）。
     *
     * 例如“上方两个角大圆角、下方直角”的卡片头图：`loadCustomRoundImage(iv, url, 16, 16, 0, 0)`。
     *
     * @param topLeftDp     左上圆角，dp
     * @param topRightDp    右上圆角，dp
     * @param bottomLeftDp  左下圆角，dp
     * @param bottomRightDp 右下圆角，dp
     */
    @JvmStatic
    fun loadCustomRoundImage(
        imageView: ImageView,
        model: Any?,
        topLeftDp: Int,
        topRightDp: Int,
        bottomLeftDp: Int,
        bottomRightDp: Int,
    ) {
        val transform = CornerTransformation(
            topLeft = dpToPx(imageView, topLeftDp).toFloat(),
            topRight = dpToPx(imageView, topRightDp).toFloat(),
            bottomRight = dpToPx(imageView, bottomRightDp).toFloat(),
            bottomLeft = dpToPx(imageView, bottomLeftDp).toFloat(),
        )
        // 同 loadRoundImage：先 CenterCrop 裁满再圆角，避免拉伸变形
        Glide.with(imageView).load(model)
            .apply(RequestOptions.bitmapTransform(MultiTransformation(CenterCrop(), transform)))
            .into(imageView)
    }

    // ------------------------------------------------------------------
    // 三、占位图 / 错误图 & 缩放方式
    // ------------------------------------------------------------------

    /**
     * 带占位图、错误图的加载。加载中显示 [placeholder]，失败显示 [error]。
     *
     * @param placeholder 占位图（资源 id / url / Drawable / Uri / File），可为 null
     * @param error       加载失败时的兜底图，可为 null
     */
    @JvmStatic
    fun loadImage(
        imageView: ImageView,
        model: Any?,
        placeholder: Any? = null,
        error: Any? = null,
    ) {
        val options = RequestOptions()
        applyHolder(options, placeholder, isPlaceholder = true)
        applyHolder(options, error, isPlaceholder = false)
        Glide.with(imageView).load(model).apply(options).into(imageView)
    }

    /**
     * Glide 的 placeholder/error 只接受具体类型（这里支持最常见的资源 id 与 Drawable），null 则不设置该占位。
     */
    private fun applyHolder(options: RequestOptions, holder: Any?, isPlaceholder: Boolean) {
        when (holder) {
            is Int -> if (isPlaceholder) options.placeholder(holder) else options.error(holder)
            is Drawable -> if (isPlaceholder) options.placeholder(holder) else options.error(holder)
        }
    }

    /**
     * 固定尺寸加载：按 [widthPx] x [heightPx] 请求解码后的图片，节省内存（列表缩略图常用）。
     */
    @JvmStatic
    fun loadOverride(imageView: ImageView, model: Any?, widthPx: Int, heightPx: Int) {
        Glide.with(imageView).load(model).override(widthPx, heightPx).into(imageView)
    }

    // ------------------------------------------------------------------
    // 四、加载监听回调
    // ------------------------------------------------------------------

    /**
     * 带成功 / 失败监听的加载：图片仍会正常显示到 [imageView]，同时把结果回调出去
     * （常见于“加载完做入场动画 / 埋点 / 拿到真实尺寸 / 失败提示”）。
     *
     * @param onReady  加载成功，参数为最终显示的 Drawable 与解码后的宽高（px）
     * @param onFailed 加载失败，参数为异常（可为 null）
     */
    @JvmStatic
    fun loadWithCallback(
        imageView: ImageView,
        model: Any?,
        onReady: ((drawable: Drawable, width: Int, height: Int) -> Unit)? = null,
        onFailed: ((e: Exception?) -> Unit)? = null,
    ) {
        Glide.with(imageView)
            .load(model)
            .listener(object : RequestListener<Drawable> {
                override fun onResourceReady(
                    resource: Drawable,
                    model: Any,
                    target: Target<Drawable>?,
                    dataSource: DataSource,
                    isFirstResource: Boolean,
                ): Boolean {
                    onReady?.invoke(resource, resource.intrinsicWidth, resource.intrinsicHeight)
                    return false // 返回 false 让 Glide 正常把图 set 进 ImageView
                }

                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>,
                    isFirstResource: Boolean,
                ): Boolean {
                    onFailed?.invoke(e)
                    return false // 返回 false 让 Glide 显示 error 占位图
                }
            })
            .into(imageView)
    }

    // ------------------------------------------------------------------
    // 五、缩略图过渡
    // ------------------------------------------------------------------

    /**
     * 带缩略图的加载：先出低清/占位再过渡到原图，网络大图体验更好。
     *
     * @param thumbnail 缩略图模型：可为 url / 资源 id，或一个倍率 Float（如 `0.1f` 表示先加载 10% 尺寸）
     */
    @JvmStatic
    fun loadWithThumbnail(imageView: ImageView, model: Any?, thumbnail: Any?) {
        var request = Glide.with(imageView).load(model)
        when (thumbnail) {
            // 倍率缩略图：用 sizeMultiplierOf 构造一个缩小的 RequestBuilder（避免已废弃的 thumbnail(Float)）
            is Float -> request = request.thumbnail(
                Glide.with(imageView).load(model).apply(RequestOptions.sizeMultiplierOf(thumbnail)),
            )
            null -> Unit
            else -> request = request.thumbnail(Glide.with(imageView).load(thumbnail))
        }
        request.into(imageView)
    }

    // ------------------------------------------------------------------
    // 六、预加载 / 协程取图 / 下载原图
    // ------------------------------------------------------------------

    /**
     * 预加载：只把图片下载并写入缓存，不显示到任何 View。适合列表滑动前提前备好下一屏图。
     *
     * @param callback 是否成功（true 表示已在缓存中，后续加载可秒出）
     */
    @JvmStatic
    fun preload(context: Context, model: Any?, callback: ((succeed: Boolean) -> Unit)? = null) {
        Glide.with(context.applicationContext)
            .load(model)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(object : CustomTarget<Drawable>() {
                override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                    callback?.invoke(true)
                }

                override fun onLoadFailed(errorDrawable: Drawable?) {
                    callback?.invoke(false)
                }

                override fun onLoadCleared(placeholder: Drawable?) = Unit
            })
    }

    /**
     * 协程方式加载 Bitmap：挂起直到解码完成，失败返回 null。
     * 适合“拿到 Bitmap 再做二次处理”（如 Palette 取色、保存到相册）而不想写回调嵌套。
     */
    suspend fun loadBitmap(context: Context, model: Any?): Bitmap? =
        suspendCancellableCoroutine { cont ->
            val target = Glide.with(context.applicationContext)
                .asBitmap()
                .load(model)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        if (cont.isActive) cont.resume(resource)
                    }

                    override fun onLoadFailed(errorDrawable: Drawable?) {
                        if (cont.isActive) cont.resume(null)
                    }

                    override fun onLoadCleared(placeholder: Drawable?) = Unit
                })
            cont.invokeOnCancellation { Glide.with(context.applicationContext).clear(target) }
        }

    /**
     * 协程方式下载原图到磁盘缓存，返回文件；失败返回 null。适合“先存图再分享/上传”。
     */
    suspend fun downloadOnly(context: Context, model: Any?): File? =
        suspendCancellableCoroutine { cont ->
            Glide.with(context.applicationContext)
                .downloadOnly()
                .load(model)
                .into(object : CustomTarget<File>() {
                    override fun onResourceReady(resource: File, transition: Transition<in File>?) {
                        if (cont.isActive) cont.resume(resource)
                    }

                    override fun onLoadFailed(errorDrawable: Drawable?) {
                        if (cont.isActive) cont.resume(null)
                    }

                    override fun onLoadCleared(placeholder: Drawable?) = Unit
                })
        }

    // ------------------------------------------------------------------
    // 七、生命周期 / 缓存管理
    // ------------------------------------------------------------------

    /** 取消 [imageView] 上正在进行的加载（如列表 item 复用时清掉旧请求，防止错位）。 */
    @JvmStatic
    fun clear(imageView: ImageView) {
        Glide.with(imageView).clear(imageView)
    }

    /** 暂停所有加载（如界面不可见时省流量/性能）。 */
    @JvmStatic
    fun pauseRequests(context: Context) {
        Glide.with(context.applicationContext).pauseRequests()
    }

    /** 恢复加载。 */
    @JvmStatic
    fun resumeRequests(context: Context) {
        Glide.with(context.applicationContext).resumeRequests()
    }

    /** 清空内存图片缓存（如内存紧张 / 退出登录时调用）。 */
    @JvmStatic
    fun clearMemory(context: Context) {
        Glide.get(context.applicationContext).clearMemory()
    }

    // ------------------------------------------------------------------

    /** dp -> px，四舍五入。 */
    private fun dpToPx(imageView: ImageView, dp: Int): Int =
        (dp * imageView.resources.displayMetrics.density + 0.5f).toInt()
}
