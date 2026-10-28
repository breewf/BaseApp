@file:JvmName("AppExt")

package com.hy.basic.utils

import android.content.Context
import android.graphics.drawable.Drawable
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.DeviceUtils

/**
 * desc: 设备 / 应用信息扩展 —— 基于 blankj [DeviceUtils] 与 [AppUtils] 的安全封装
 *
 * 以 Context 扩展暴露(调用点 `context.deviceModel()` / `context.appVersionName()` 等),
 * 统一吞掉底层异常并回退到安全默认值,省去各处 try/catch 样板。
 * [Context] 形参只为调用顺手,底层实际用的是 application context。
 *
 * 分组:
 * - 机型 / 系统:[deviceModel] / [deviceManufacturer] / [sdkVersionName] / [sdkVersionCode] /
 *   [isTabletDevice] / [isEmulatorDevice] / [isDeviceRooted] / [androidID] / [uniqueDeviceId];
 * - 当前 App:[appName] / [appPackageName] / [appVersionName] / [appVersionCode] / [appIcon] /
 *   [isAppDebugBuild] / [isAppInForeground];
 * - 已安装应用:[isAppInstalled] / [installedApps] / [launchInstalledApp] / [openCurrentAppSettings] /
 *   [installAppFrom] / [uninstallAppBy]。
 *
 * 提醒:Android 11(API 30)起读取"已安装应用列表"([installedApps])需要在 Manifest 声明
 * `QUERY_ALL_PACKAGES` 权限或 `<queries>`,否则拿到的结果会被系统过滤。
 */

/** 吞异常的安全执行:底层设备/应用 API 在部分机型/权限缺失时会抛异常,统一回退默认值 */
private inline fun <T> safeApp(default: T, block: () -> T): T = try {
    block()
} catch (e: Exception) {
    default
}

// ==== 机型 / 系统 ====

/** 机型名称(如 "Pixel 7");拿不到时返回空串 */
fun Context.deviceModel(): String =
    safeApp("") { DeviceUtils.getModel() ?: "" }

/** 制造商(如 "Google");拿不到时返回空串 */
fun Context.deviceManufacturer(): String =
    safeApp("") { DeviceUtils.getManufacturer() ?: "" }

/** Android 版本名(如 "13");拿不到时返回空串 */
fun Context.sdkVersionName(): String =
    safeApp("") { DeviceUtils.getSDKVersionName() ?: "" }

/** API 等级(如 33);拿不到时返回 0 */
fun Context.sdkVersionCode(): Int =
    safeApp(0) { DeviceUtils.getSDKVersionCode() }

/** 是否为平板 */
fun Context.isTabletDevice(): Boolean =
    safeApp(false) { DeviceUtils.isTablet() }

/** 是否为模拟器 */
fun Context.isEmulatorDevice(): Boolean =
    safeApp(false) { DeviceUtils.isEmulator() }

/** 设备是否已 root */
fun Context.isDeviceRooted(): Boolean =
    safeApp(false) { DeviceUtils.isDeviceRooted() }

/** Android 安全 ID;拿不到时返回空串 */
fun Context.androidID(): String =
    safeApp("") { DeviceUtils.getAndroidID() ?: "" }

/** 设备唯一标识(自生成并持久化,重装可能变化);拿不到时返回空串 */
fun Context.uniqueDeviceId(): String =
    safeApp("") { DeviceUtils.getUniqueDeviceId() ?: "" }

// ==== 当前 App ====

/** 当前应用名(清单 label);拿不到时返回空串 */
fun Context.appName(): String =
    safeApp("") { AppUtils.getAppName() ?: "" }

/** 当前应用包名;拿不到时返回空串 */
fun Context.appPackageName(): String =
    safeApp("") { AppUtils.getAppPackageName() ?: "" }

/** 当前应用版本名(versionName,如 "1.2.0");拿不到时返回空串 */
fun Context.appVersionName(): String =
    safeApp("") { AppUtils.getAppVersionName() ?: "" }

/** 当前应用版本号(versionCode);拿不到时返回 0 */
fun Context.appVersionCode(): Int =
    safeApp(0) { AppUtils.getAppVersionCode() }

/** 当前应用图标;拿不到时返回 null */
fun Context.appIcon(): Drawable? =
    safeApp(null) { AppUtils.getAppIcon() }

/** 当前应用是否为可调试包(debuggable) */
fun Context.isAppDebugBuild(): Boolean =
    safeApp(false) { AppUtils.isAppDebug() }

/** 当前应用是否处于前台 */
fun Context.isAppInForeground(): Boolean =
    safeApp(false) { AppUtils.isAppForeground() }

// ==== 已安装应用 ====

/** 指定包名([pkg])的应用是否已安装 */
fun Context.isAppInstalled(pkg: String): Boolean =
    safeApp(false) { AppUtils.isAppInstalled(pkg) }

/**
 * 已安装应用列表。
 * @param includeSystem 是否包含系统应用;默认 false 只返回第三方应用。
 * 每项为 [AppUtils.AppInfo](含包名/应用名/图标/版本等)。需要存储/查询包权限,拿不到时返回空列表。
 */
fun Context.installedApps(includeSystem: Boolean = false): List<AppUtils.AppInfo> =
    safeApp(emptyList()) { AppUtils.getAppsInfo().filter { includeSystem || !it.isSystem } }

/** 启动指定包名([pkg])的已安装应用 */
fun Context.launchInstalledApp(pkg: String) {
    safeApp(Unit) { AppUtils.launchApp(pkg); Unit }
}

/** 跳转到当前应用的"应用详情"设置页(供引导用户授权等) */
fun Context.openCurrentAppSettings() {
    safeApp(Unit) { AppUtils.launchAppDetailsSettings(); Unit }
}

/** 从 apk 文件路径([apkPath])安装应用(需相应权限) */
fun Context.installAppFrom(apkPath: String) {
    safeApp(Unit) { AppUtils.installApp(apkPath); Unit }
}

/** 卸载指定包名([pkg])的应用(系统应用/无权限时不生效) */
fun Context.uninstallAppBy(pkg: String) {
    safeApp(Unit) { AppUtils.uninstallApp(pkg); Unit }
}
