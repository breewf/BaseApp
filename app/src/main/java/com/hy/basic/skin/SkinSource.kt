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

    /** 取色;当前皮肤没有就回落默认配置,都没有回 0 */
    fun color(@ColorRes id: Int): Int

    /** 取 ColorStateList;取不到返回 null,调用方应保持原值 */
    fun colorStateList(@ColorRes id: Int): ColorStateList?

    /** 取 Drawable;取不到返回 null,调用方应保持原值 */
    fun drawable(@DrawableRes id: Int): Drawable?
}
