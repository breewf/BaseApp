package com.hy.basic.skin

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.drawable.Drawable
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.res.ResourcesCompat

/**
 * 默认来源:同一个资源 id 按 uiMode 从 values / values-night 取。
 *
 * 两套 Resources 都强制指定 uiMode,不读当前 Context。页面不重建时
 * context.resources 可能还是系统配置,直接取会拿错日间/夜间值。
 */
internal class NightSkinSource(context: Context) : SkinSource {

    /** 应用 Context,用于创建独立配置的 Resources */
    private val appContext = context.applicationContext

    /** 浅色 Resources,懒加载 */
    private var lightRes: Resources? = null

    /** 深色 Resources,懒加载 */
    private var darkRes: Resources? = null

    /** 当前是否深色,由 applyMode 更新 */
    override var isDark: Boolean = false
        private set

    /** 切换取色目标 */
    override fun applyMode(isDark: Boolean) {
        this.isDark = isDark
    }

    /** 丢弃缓存,下次重新创建 */
    override fun invalidate() {
        lightRes = null
        darkRes = null
    }

    /** 取色 */
    override fun color(@ColorRes id: Int): Int =
        ResourcesCompat.getColor(currentResources(), id, appContext.theme)

    /** 取 ColorStateList,资源不存在时返回 null */
    override fun colorStateList(@ColorRes id: Int): ColorStateList? =
        runCatching { ResourcesCompat.getColorStateList(currentResources(), id, appContext.theme) }
            .getOrNull()

    /** 取 Drawable,资源不存在时返回 null */
    override fun drawable(@DrawableRes id: Int): Drawable? =
        runCatching { ResourcesCompat.getDrawable(currentResources(), id, appContext.theme) }
            .getOrNull()

    /** 当前皮肤对应的 Resources */
    private fun currentResources(): Resources = if (isDark) {
        darkRes ?: create(dark = true).also { darkRes = it }
    } else {
        lightRes ?: create(dark = false).also { lightRes = it }
    }

    /** 复制当前配置,只改夜间位,其余跟随系统 */
    private fun create(dark: Boolean): Resources {
        val config = Configuration(appContext.resources.configuration)
        config.uiMode = (config.uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or
            if (dark) Configuration.UI_MODE_NIGHT_YES else Configuration.UI_MODE_NIGHT_NO
        return appContext.createConfigurationContext(config).resources
    }
}
