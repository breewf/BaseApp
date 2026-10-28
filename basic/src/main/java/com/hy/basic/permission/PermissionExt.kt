package com.hy.basic.permission

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

/**
 * desc: 权限申请对外 API。把注册时机、并发、版本差异全部吃掉,页面只写结果分支。
 *
 * 两种用法:
 * ```
 * // 协程式(推荐)
 * val result = awaitPermissions(Manifest.permission.CAMERA)
 * when {
 *     result.allGranted -> openCamera()
 *     result.needsSettings -> showGoSettingsDialog()
 *     else -> showToast("已拒绝")
 * }
 *
 * // 回调式
 * requestPermissions(*Permission.MEDIA) { result -> if (result.allGranted) pickImage() }
 * ```
 */

/**
 * 挂起申请一组权限,返回覆盖全部申请项的 [PermissionResult]。
 * 已授予的权限不会弹窗;宿主状态已保存(切后台/销毁中)时不打扰用户,直接回填当前状态。
 */
suspend fun FragmentActivity.awaitPermissions(vararg permissions: String): PermissionResult =
    permissionHost()?.request(permissions.asList())
        ?: PermissionResult.current(this, permissions.asList())

/** [awaitPermissions] 的集合版,直接传 [Permission.MEDIA] 这类版本自适应权限组 */
suspend fun FragmentActivity.awaitPermissions(permissions: Collection<String>): PermissionResult =
    permissionHost()?.request(permissions)
        ?: PermissionResult.current(this, permissions)

/** [awaitPermissions] 的 Fragment 版,宿主挂在自身 childFragmentManager 上,与其他页面互不干扰 */
suspend fun Fragment.awaitPermissions(vararg permissions: String): PermissionResult =
    permissionHost()?.request(permissions.asList())
        ?: PermissionResult.current(requireContext(), permissions.asList())

/** [awaitPermissions] 的集合版(Fragment) */
suspend fun Fragment.awaitPermissions(permissions: Collection<String>): PermissionResult =
    permissionHost()?.request(permissions)
        ?: PermissionResult.current(requireContext(), permissions)

/** 回调式申请:内部用宿主 lifecycleScope 发起,页面销毁时自动停止 */
fun FragmentActivity.requestPermissions(
    vararg permissions: String,
    onResult: (PermissionResult) -> Unit,
) = requestPermissions(permissions.asList(), onResult)

/** [requestPermissions] 的集合版,直接传权限组 */
fun FragmentActivity.requestPermissions(
    permissions: Collection<String>,
    onResult: (PermissionResult) -> Unit,
) = lifecycleScope.launch {
    onResult(awaitPermissions(permissions))
}

/** [requestPermissions] 的 Fragment 版 */
fun Fragment.requestPermissions(
    vararg permissions: String,
    onResult: (PermissionResult) -> Unit,
) = requestPermissions(permissions.asList(), onResult)

/** [requestPermissions] 的集合版(Fragment) */
fun Fragment.requestPermissions(
    permissions: Collection<String>,
    onResult: (PermissionResult) -> Unit,
) = lifecycleScope.launch {
    onResult(awaitPermissions(permissions))
}

/** 便捷只读判断:一组权限是否都已可用 */
fun Context.hasPermission(vararg permissions: String): Boolean =
    Permission.isGranted(this, *permissions)

/** 便捷只读判断:单个权限处于哪种状态(做事之前的分支判断用它,不需要等弹窗回调) */
fun Context.permissionState(permission: String): PermissionState = Permission.state(this, permission)

/** 权限中文简称,直接拼进文案(如"没有相机权限,无法扫码") */
fun Context.permissionLabel(permission: String): String = Permission.label(permission)
