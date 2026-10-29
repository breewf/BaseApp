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
 * 存储卷信息读取，包括 U 盘/SD 卡。
 *
 * 用法：
 * `context.externalVolumes()`，`context.hasRemovableVolume()`。
 * 注意：卷列表随插拔变化，调用方每次重新获取；API 23 用路径回退，API 24+ 用 StorageVolume 描述。
 * Android 10+ 访问外接盘受分区存储限制。
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
@Suppress("DEPRECATION")
fun Context.externalVolumes(): List<ExternalVolume> {
    val appContext = applicationContext
    val roots = ContextCompat.getExternalFilesDirs(appContext, null)
        .filterNotNull()
        .mapNotNull { it.volumeRootOrNull() }
        .distinctBy { it.absolutePath }

    val primaryRoot = runCatching { Environment.getExternalStorageDirectory().canonicalPath }
        .getOrNull()

    return roots.map { root ->
        val meta = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            volumeMeta(appContext, root)
        } else {
            VolumeMeta()
        }
        val isPrimary = runCatching { root.canonicalPath == primaryRoot }.getOrDefault(false)
        ExternalVolume(
            root = root,
            label = meta.label
                ?: if (isPrimary) "内部存储" else "外部存储",
            removable = meta.removable
                ?: runCatching { Environment.isExternalStorageRemovable(root) }
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

/** API 24+ 的 StorageVolume 信息；放在独立方法里，避免 API 23 加载缺失类 */
@RequiresApi(Build.VERSION_CODES.N)
private fun volumeMeta(context: Context, root: File): VolumeMeta = runCatching {
    val manager = context.getSystemService(Context.STORAGE_SERVICE) as? StorageManager
        ?: return@runCatching VolumeMeta()
    val volume = manager.getStorageVolume(root) ?: return@runCatching VolumeMeta()
    VolumeMeta(
        label = volume.getDescription(context),
        removable = volume.isRemovable,
    )
}.getOrDefault(VolumeMeta())

/** 存储卷展示信息 */
private data class VolumeMeta(
    /** 系统展示名称 */
    val label: String? = null,
    /** 是否可移动 */
    val removable: Boolean? = null,
)

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
fun Context.hasRemovableVolume(): Boolean = externalVolumes().any { it.removable }
