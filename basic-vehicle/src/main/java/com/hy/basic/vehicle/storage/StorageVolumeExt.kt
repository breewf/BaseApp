@file:JvmName("VehicleStorageVolumeExt")

package com.hy.basic.vehicle.storage

import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.storage.StorageManager
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import java.io.File
import java.util.Locale

/**
 * desc: 车机外接存储(U 盘 / SD 卡)信息读取。
 *
 * 车里插 U 盘听歌、导日志是常见需求。这里用 getExternalFilesDirs 拿到各存储卷上
 * 的应用目录,再反推卷根路径,避开 StorageVolume.getDirectory() 在低版本不可用的问题。
 *
 * 注意:
 * - Android 10 起应用不能随意遍历整个 U 盘,只能访问自己的目录或通过 SAF 授权;
 * - 卷的挂载状态会变,插拔 U 盘后重新调用一次,不要缓存 [ExternalVolume] 列表;
 * - 容量按 1024 进位显示,和系统设置里按 1000 进位的数字会有差异,别拿来对账。
 */

/** 一个已挂载或可用的存储卷 */
data class ExternalVolume(
    /** 卷根目录 */
    val root: File,
    /** 系统展示名称,如“内部存储” */
    val label: String,
    /** 是否可移动(U 盘 / SD 卡) */
    val removable: Boolean,
    /** 是否主存储 */
    val primary: Boolean,
    /** 当前是否已挂载 */
    val mounted: Boolean,
    /** 总容量(字节) */
    val totalBytes: Long,
    /** 可用容量(字节) */
    val freeBytes: Long,
)

/** 当前可用的存储卷(含内置存储),取不到时返回空列表 */
@RequiresApi(Build.VERSION_CODES.N)
@Suppress("DEPRECATION")
fun Context.externalVolumes(): List<ExternalVolume> {
    val appContext = applicationContext
    val roots = ContextCompat.getExternalFilesDirs(appContext, null)
        .filterNotNull()
        .mapNotNull { it.volumeRootOrNull() }
        .distinctBy { it.absolutePath }

    val storageManager = appContext.getSystemService(Context.STORAGE_SERVICE) as? StorageManager
    val primaryRoot = runCatching { Environment.getExternalStorageDirectory().canonicalPath }
        .getOrNull()

    return roots.map { root ->
        val volume = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            runCatching { storageManager?.getStorageVolume(root) }.getOrNull()
        } else {
            null
        }
        val isPrimary = runCatching { root.canonicalPath == primaryRoot }.getOrDefault(false)
        ExternalVolume(
            root = root,
            label = runCatching { volume?.getDescription(appContext) }.getOrNull()
                ?: if (isPrimary) "内部存储" else "外部存储",
            removable = runCatching { Environment.isExternalStorageRemovable(root) }
                .getOrDefault(!isPrimary),
            primary = isPrimary,
            mounted = runCatching {
                Environment.getExternalStorageState(root) == Environment.MEDIA_MOUNTED
            }.getOrDefault(false),
            totalBytes = runCatching { root.totalSpace }.getOrDefault(0L),
            freeBytes = runCatching { root.usableSpace }.getOrDefault(0L),
        )
    }
}

/** 从应用目录 `<root>/Android/data/<pkg>/files` 往上四级拿到卷根 */
private fun File.volumeRootOrNull(): File? {
    val root = parentFile?.parentFile?.parentFile?.parentFile ?: return null
    return root.takeIf { it.exists() }
}

/** 字节数转可读容量,按 1024 进位 */
fun Long.toReadableSize(decimals: Int = 1): String {
    if (this < 1024L) return "$this B"

    val units = arrayOf("KB", "MB", "GB", "TB")
    var value = toDouble()
    var unitIndex = -1
    while (value >= 1024.0 && unitIndex < units.lastIndex) {
        value /= 1024.0
        unitIndex++
    }
    return String.format(Locale.US, "%.${decimals}f %s", value, units[unitIndex])
}

/** 是否存在可移动存储(U 盘/SD 卡) */
@RequiresApi(Build.VERSION_CODES.N)
fun Context.hasRemovableVolume(): Boolean = externalVolumes().any { it.removable }
