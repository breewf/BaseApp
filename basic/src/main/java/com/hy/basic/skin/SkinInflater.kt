package com.hy.basic.skin

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.CompoundButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.TextView
import androidx.annotation.AnyRes
import androidx.appcompat.app.AppCompatViewInflater
import androidx.cardview.widget.CardView

/** 可换的属性:资源 id + 处理方式,日夜是同一个 id */
internal class SkinSlot(
    /** 资源 id,代码切态时更新 */
    @param:AnyRes var resId: Int,
    /** 处理方式 */
    val kind: SkinPart,
)

/** android 命名空间 */
private const val ANDROID_NS = "http://schemas.android.com/apk/res/android"

/** app 命名空间 */
private const val AUTO_NS = "http://schemas.android.com/apk/res-auto"

/**
 * 布局接管:包一层宿主已有的 Factory2,View 创建后记下可换的属性。
 *
 * 包装而非替换:AppCompat 靠自己的 Factory2 生成 AppCompat 控件。
 * 只认布局里直接写的引用,style/theme 里的颜色接不到,需要 [IDarkMode] 兜底。
 * 代码里动态改过颜色或图片的控件,切肤会回到 XML 的值;
 * 需要保持形态的用 [SkinManager.setSkinRes] 写回槽位。
 */
internal object SkinInflater {

    /** 包装 inflater 的 Factory2,重复调用不会叠加 */
    fun install(inflater: LayoutInflater) {
        val origin = inflater.factory2
        if (origin is SkinFactory2) return
        inflater.factory2 = SkinFactory2(inflater, origin)
    }

    /** 解析可换属性,没有则返回 null */
    fun parse(view: View, attrs: AttributeSet): MutableList<SkinSlot>? {
        var slots: MutableList<SkinSlot>? = null

        // 命中一个资源引用就记一条
        fun collect(ns: String, name: String, kind: SkinPart) {
            val id = attrs.getAttributeResourceValue(ns, name, 0)
            if (id == 0) return
            val list = slots ?: mutableListOf<SkinSlot>().also { slots = it }
            list.add(SkinSlot(id, kind))
        }

        collect(ANDROID_NS, "background", SkinPart.BACKGROUND)
        collect(ANDROID_NS, "foreground", SkinPart.FOREGROUND)
        collect(ANDROID_NS, "backgroundTint", SkinPart.BACKGROUND_TINT)
        collect(AUTO_NS, "backgroundTint", SkinPart.BACKGROUND_TINT)
        if (view is TextView) {
            collect(ANDROID_NS, "textColor", SkinPart.TEXT_COLOR)
            collect(ANDROID_NS, "textColorHint", SkinPart.TEXT_HINT)
            collect(ANDROID_NS, "textColorLink", SkinPart.TEXT_COLOR_LINK)
            collect(ANDROID_NS, "drawableStart", SkinPart.COMPOUND_DRAWABLE_START)
            collect(ANDROID_NS, "drawableTop", SkinPart.COMPOUND_DRAWABLE_TOP)
            collect(ANDROID_NS, "drawableEnd", SkinPart.COMPOUND_DRAWABLE_END)
            collect(ANDROID_NS, "drawableBottom", SkinPart.COMPOUND_DRAWABLE_BOTTOM)
        }
        if (view is ImageView) {
            collect(ANDROID_NS, "src", SkinPart.SRC)
            collect(ANDROID_NS, "tint", SkinPart.IMAGE_TINT)
            collect(AUTO_NS, "tint", SkinPart.IMAGE_TINT)
        }
        if (view is ProgressBar) {
            collect(ANDROID_NS, "progressTint", SkinPart.PROGRESS_TINT)
            collect(ANDROID_NS, "progressBackgroundTint", SkinPart.PROGRESS_BACKGROUND_TINT)
            collect(ANDROID_NS, "secondaryProgressTint", SkinPart.SECONDARY_PROGRESS_TINT)
            collect(ANDROID_NS, "indeterminateTint", SkinPart.INDETERMINATE_TINT)
        }
        if (view is SeekBar) {
            collect(ANDROID_NS, "thumbTint", SkinPart.THUMB_TINT)
            collect(ANDROID_NS, "tickMarkTint", SkinPart.TICK_MARK_TINT)
        }
        if (view is CompoundButton) {
            collect(ANDROID_NS, "buttonTint", SkinPart.BUTTON_TINT)
            collect(AUTO_NS, "buttonTint", SkinPart.BUTTON_TINT)
        }
        if (view is LinearLayout) {
            collect(ANDROID_NS, "divider", SkinPart.DIVIDER)
        }
        if (view is CardView) {
            collect(AUTO_NS, "cardBackgroundColor", SkinPart.CARD_BG)
        }
        return slots
    }

    /** 先交给宿主创建,保住 AppCompat 控件,再记槽位 */
    private class SkinFactory2(
        /** 用于兜底创建 View */
        private val inflater: LayoutInflater,
        /** 宿主原本的 Factory2,没有则为 null */
        private val origin: LayoutInflater.Factory2?,
    ) : LayoutInflater.Factory2 {

        /** 宿主没有 factory 时用它生成 AppCompat 控件 */
        private val appCompatInflater = AppCompatViewInflater()

        /** 宿主优先,其次 AppCompat 控件,最后系统创建 */
        override fun onCreateView(
            parent: View?,
            name: String,
            context: Context,
            attrs: AttributeSet,
        ): View? {
            val view = origin?.onCreateView(parent, name, context, attrs)
                // 参数顺序:inheritContext, readAndroidTheme, readAppTheme, wrapContext
                ?: appCompatInflater.createView(parent, name, context, attrs, false, true, true, true)
                ?: runCatching { inflater.createView(name, null, attrs) }.getOrNull()
                ?: return null
            SkinManager.attach(view, attrs)
            return view
        }

        /** Factory 单参入口,转给 Factory2 */
        override fun onCreateView(name: String, context: Context, attrs: AttributeSet): View? =
            onCreateView(null, name, context, attrs)
    }
}
