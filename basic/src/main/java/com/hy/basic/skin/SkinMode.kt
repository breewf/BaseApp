package com.hy.basic.skin

import androidx.appcompat.app.AppCompatDelegate

/** 皮肤明暗模式,持久化在 MMKV */
enum class SkinMode {

    /** 跟随系统(默认) */
    FOLLOW_SYSTEM,

    /** 强制浅色 */
    LIGHT,

    /** 强制深色 */
    DARK;

    /** 对应的 AppCompatDelegate 常量 */
    internal val compatValue: Int
        get() = when (this) {
            FOLLOW_SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            DARK -> AppCompatDelegate.MODE_NIGHT_YES
        }

    /** 中文名 */
    val label: String
        get() = when (this) {
            FOLLOW_SYSTEM -> "跟随系统"
            LIGHT -> "浅色"
            DARK -> "深色"
        }
}
