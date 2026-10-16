@file:JvmName("NetworkExt")

package com.hy.basic.utils

import android.content.Context
import com.blankj.utilcode.util.NetworkUtils
import com.blankj.utilcode.util.NetworkUtils.NetworkType

/**
 * desc: 网络状态判断扩展 —— 基于 blankj [NetworkUtils] 的安全封装
 *
 * 以 Context 扩展暴露(调用点 `context.isNetworkConnected` / `context.wifiSSID()` 等),
 * 统一吞掉底层异常并回退到安全默认值,省去各处 try/catch 样板。
 * [Context] 形参只为调用顺手,底层实际用的是 application context。
 *
 * 分组:
 * - 连接态:[isNetworkConnected] / [isNetworkAvailable] / [isNetworkAvailableByPing];
 * - 类型态:[isWifiConnected] / [isMobileData] / [is4G] / [is5G] / [networkType];
 * - 附加信息:[wifiSSID] / [networkOperatorName] / [ipAddress];
 * - 环境:[isUsingVPN] / [isBehindProxy] / [openNetworkSettings]。
 *
 * 提醒:"已连接"不等于"能上网",要判断真正连通性请用 [isNetworkAvailable] 系列。
 */

/** 吞异常的安全执行:底层网络 API 在部分机型/权限缺失时会抛异常,统一回退默认值 */
private inline fun <T> safeNetwork(default: T, block: () -> T): T = try {
    block()
} catch (e: Exception) {
    default
}

// ==== 连接态 ====

/** 网络是否已连接(仅表示连上了某个网络,不代表能访问外网) */
val Context.isNetworkConnected: Boolean
    get() = safeNetwork(false) { NetworkUtils.isConnected() }

/** 网络是否真正可用(系统判定该网络具备外网连通性) */
fun Context.isNetworkAvailable(): Boolean =
    safeNetwork(false) { NetworkUtils.isAvailable() }

/** 通过 ping 探测真实连通性(耗时,勿在主线程调用);[host] 为空时 ping 默认地址 */
fun Context.isNetworkAvailableByPing(host: String = ""): Boolean =
    safeNetwork(false) {
        if (host.isEmpty()) NetworkUtils.isAvailableByPing() else NetworkUtils.isAvailableByPing(host)
    }

// ==== 类型态 ====

/** Wi-Fi 当前是否连接 */
val Context.isWifiConnected: Boolean
    get() = safeNetwork(false) { NetworkUtils.isWifiConnected() }

/** Wi-Fi 是否可用(已连接且具备连通性) */
fun Context.isWifiAvailable(): Boolean =
    safeNetwork(false) { NetworkUtils.isWifiAvailable() }

/** Wi-Fi 开关是否打开(不代表已连接) */
fun Context.isWifiEnabled(): Boolean =
    safeNetwork(false) { NetworkUtils.getWifiEnabled() }

/** 移动数据(蜂窝网络)当前是否连接 */
val Context.isMobileData: Boolean
    get() = safeNetwork(false) { NetworkUtils.isMobileData() }

/** 是否 4G 网络 */
fun Context.is4G(): Boolean =
    safeNetwork(false) { NetworkUtils.is4G() }

/** 是否 5G 网络 */
fun Context.is5G(): Boolean =
    safeNetwork(false) { NetworkUtils.is5G() }

/** 当前网络类型枚举(ETHERNET/WIFI/5G/4G/3G/2G/UNKNOWN/NO);拿不到时返回 [NetworkType.NETWORK_UNKNOWN] */
fun Context.networkType(): NetworkType =
    safeNetwork(NetworkType.NETWORK_UNKNOWN) { NetworkUtils.getNetworkType() }

// ==== 附加信息 ====

/** 当前 Wi-Fi 的 SSID;非 Wi-Fi 或拿不到时返回空串 */
fun Context.wifiSSID(): String =
    safeNetwork("") { NetworkUtils.getSSID() ?: "" }

/** 运营商名称(如 "China Mobile");拿不到时返回空串 */
fun Context.networkOperatorName(): String =
    safeNetwork("") { NetworkUtils.getNetworkOperatorName() ?: "" }

/** 本机 IP 地址;[ipv4]=true 取 IPv4,否则 IPv6;拿不到时返回空串 */
fun Context.ipAddress(ipv4: Boolean = true): String =
    safeNetwork("") { NetworkUtils.getIPAddress(ipv4) ?: "" }

// ==== 环境 ====

/** 是否正在使用 VPN */
fun Context.isUsingVPN(): Boolean =
    safeNetwork(false) { NetworkUtils.isUsingVPN() }

/** 是否走了代理 */
fun Context.isBehindProxy(): Boolean =
    safeNetwork(false) { NetworkUtils.isBehindProxy() }

/** 跳转到系统"无线和网络"设置页(供引导用户手动开启网络) */
fun Context.openNetworkSettings() {
    safeNetwork(Unit) { NetworkUtils.openWirelessSettings(); Unit }
}
