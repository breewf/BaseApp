package com.hy.basic.skin

import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes

/**
 * 节日皮肤来源:在默认来源外再按 [SkinPack] 做一层资源映射。
 * 包里没有列出的资源继续走默认来源,未适配的页面不受影响。
 */
internal class FestivalSkinSource(
    /** 默认来源,负责明暗切换与兜底 */
    private val base: SkinSource,
    /** 当前节日包 */
    private val pack: SkinPack,
) : SkinSource {

    /** 明暗仍由默认来源决定 */
    override val isDark: Boolean get() = base.isDark

    /** 明暗切换交给默认来源 */
    override fun applyMode(isDark: Boolean) = base.applyMode(isDark)

    /** 配置变化时一起清缓存 */
    override fun invalidate() = base.invalidate()

    /** 有映射取节日色,没有映射取默认色 */
    override fun color(@ColorRes id: Int): Int = base.color(pack.colors[id] ?: id)

    /** 颜色选择器同样按映射走 */
    override fun colorStateList(@ColorRes id: Int): ColorStateList? =
        base.colorStateList(pack.colors[id] ?: id)

    /** 图片按映射走 */
    override fun drawable(@DrawableRes id: Int): Drawable? =
        base.drawable(pack.drawables[id] ?: id)
}
