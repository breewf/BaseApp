package com.hy.basic.font

import android.content.Context
import android.content.res.Configuration
import com.hy.basic.storage.MMKVStore
import kotlin.math.abs

/**
 * 字号缩放入口。改的是 Configuration.fontScale，不逐个 View 改 textSize。
 *
 * 覆盖而不是相乘：系统字号和 App 档位叠起来会得到不可控的极值，
 * 所以接入的页面只认这里的档位，没接入的继续用系统值。
 *
 * 改完要重建一次页面。sp 在 inflate 时就换算成 px 存在 View 里，
 * 已有的 View 不会自己重算；新页面在 attachBaseContext 里就是目标值。
 *
 * 例：FontScaleManager.level = FontScaleLevel.LARGE
 */
object FontScaleManager {

    /** 档位存储键 */
    private const val KEY_LEVEL = "font.scale.level"

    /** 配置回读有精度损失，浮点比较留容差 */
    private const val EPSILON = 0.01f

    /** 新页面默认是否跟随。老项目逐页接入时保持 false，全站铺开后再打开 */
    @Volatile
    var followByDefault: Boolean = false

    /** 当前档位，脏值回落到标准 */
    var level: FontScaleLevel
        get() = FontScaleLevel.entries.getOrElse(
            runCatching { MMKVStore.getInt(KEY_LEVEL, FontScaleLevel.NORMAL.ordinal) }
                .getOrDefault(FontScaleLevel.NORMAL.ordinal)
        ) { FontScaleLevel.NORMAL }
        set(value) {
            runCatching { MMKVStore.putInt(KEY_LEVEL, value.ordinal) }
        }

    /** 当前倍数 */
    val scale: Float get() = level.scale

    /** 给页面包一层带目标 fontScale 的 Context，只能在 attachBaseContext 调 */
    fun wrap(base: Context): Context {
        val config = Configuration(base.resources.configuration)
        config.fontScale = scale
        return base.createConfigurationContext(config)
    }

    /** 页面当前配置是否已经是目标档位 */
    fun isApplied(context: Context): Boolean =
        abs(context.resources.configuration.fontScale - scale) < EPSILON
}
