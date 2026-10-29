@file:JvmName("VehicleDisplayExt")

package com.hy.basic.vehicle.display

import android.content.Context
import android.hardware.display.DisplayManager
import android.util.DisplayMetrics
import android.view.Display

/**
 * 多屏信息读取与副屏 Context。
 *
 * 用法：
 * `context.displayInfoList()`，`context.displayContext(displayId)?.let { ... }`。
 * 注意：副屏 density 可能和主屏不同，复用主屏 metrics 会算错；虚拟屏也会出现在列表里。
 */

/** 单块显示屏的基本信息 */
data class DisplayInfo(
    /** Display ID */
    val id: Int,
    /** 系统里的显示屏名称 */
    val name: String,
    /** 宽度(px) */
    val widthPx: Int,
    /** 高度(px) */
    val heightPx: Int,
    /** 屏幕密度(dpi) */
    val densityDpi: Int,
    /** 是否主屏 */
    val isPrimary: Boolean,
)

/** 当前所有显示屏信息,取不到的屏直接跳过 */
fun Context.displayInfoList(): List<DisplayInfo> {
    val manager = getSystemService(Context.DISPLAY_SERVICE) as? DisplayManager ?: return emptyList()
    return manager.displays.mapNotNull { display ->
        runCatching {
            val metrics = DisplayMetrics()
            @Suppress("DEPRECATION")
            display.getRealMetrics(metrics)
            DisplayInfo(
                id = display.displayId,
                name = display.name ?: "display-${display.displayId}",
                widthPx = metrics.widthPixels,
                heightPx = metrics.heightPixels,
                densityDpi = metrics.densityDpi,
                isPrimary = display.displayId == Display.DEFAULT_DISPLAY,
            )
        }.getOrNull()
    }
}

/** 按 displayId 取单块屏的信息 */
fun Context.displayInfo(displayId: Int): DisplayInfo? =
    displayInfoList().firstOrNull { it.id == displayId }

/** 取副屏 Context;屏不存在或系统不允许时返回 null */
fun Context.displayContext(displayId: Int): Context? {
    val manager = getSystemService(Context.DISPLAY_SERVICE) as? DisplayManager ?: return null
    val display = manager.getDisplay(displayId) ?: return null
    return runCatching { createDisplayContext(display) }.getOrNull()
}

/** 当前是否多屏环境 */
fun Context.isMultiDisplay(): Boolean = displayInfoList().size > 1
