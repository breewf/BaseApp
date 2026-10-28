package com.hy.basic.font

import android.util.TypedValue
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.widget.TextViewCompat
import com.hy.basic.R

/*
 * 控件级退出字号缩放。
 *
 * 资源级缩放没法让单个 View 逃逸，这里按页面当前的 fontScale 除回去，
 * 换回没放大时的像素。页面重建后 View 是新的，要在 initView 里重调。
 */

/** 单个 TextView 不跟随字号设置 */
fun TextView.ignoreFontScale(): TextView {
    // 自适应字号自己算大小，插手会打架
    if (TextViewCompat.getAutoSizeTextType(this) != TextViewCompat.AUTO_SIZE_TEXT_TYPE_NONE) {
        return this
    }
    val current = context.resources.configuration.fontScale.takeIf { it > 0f } ?: 1f
    // 基准只算一次，重复调用读 tag，避免来回切累积误差
    val base = (getTag(R.id.font_scale_base_tag) as? Float)
        ?: (textSize / current).also { setTag(R.id.font_scale_base_tag, it) }
    setTextSize(TypedValue.COMPLEX_UNIT_PX, base)
    return this
}

/** 整棵子树都不跟随，动态 inflate 的 View 要在 inflate 之后调 */
fun ViewGroup.ignoreFontScaleDeep() {
    for (i in 0 until childCount) {
        when (val child = getChildAt(i)) {
            is TextView -> child.ignoreFontScale()
            is ViewGroup -> child.ignoreFontScaleDeep()
        }
    }
}
