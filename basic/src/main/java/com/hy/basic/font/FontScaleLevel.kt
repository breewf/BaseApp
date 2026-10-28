package com.hy.basic.font

/**
 * 字号档位。加一档只改这里，页面和控件不用动。
 *
 * [scale] 直接写进 Configuration.fontScale，不是在系统字号上再乘一次，
 * 否则两边都调大时布局容易撑爆。
 */
enum class FontScaleLevel(val label: String, val scale: Float) {
    SMALL("小", 0.85f),
    NORMAL("标准", 1.0f),
    LARGE("大", 1.15f),
    XLARGE("特大", 1.3f),
}
