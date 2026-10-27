package com.hy.basic.skin

import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes

/**
 * 皮肤资源来源。默认实现 [NightSkinSource] 读 values / values-night;
 * 节日皮肤实现本接口后交给 [SkinManager.setSource] 即可,刷新链路不用改。
 */
interface SkinSource {

    /** 当前皮肤是否深色 */
    val isDark: Boolean

    /** 明暗变化回调,实现方切换取色目标 */
    fun applyMode(isDark: Boolean)

    /** 配置变化时清缓存,默认空实现 */
    fun invalidate() {}

    /** 取色,不要用 context.resources(可能还是系统配置) */
    fun color(@ColorRes id: Int): Int

    /** 取 ColorStateList */
    fun colorStateList(@ColorRes id: Int): ColorStateList?

    /** 取 Drawable,颜色资源会返回 ColorDrawable */
    fun drawable(@DrawableRes id: Int): Drawable?
}
