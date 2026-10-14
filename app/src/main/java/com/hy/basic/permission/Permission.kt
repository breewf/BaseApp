package com.hy.basic.permission

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * desc: 权限**只读**能力与常量集合,不弹窗、不依赖生命周期,任何地方都能调。
 *
 * 设计取向:
 * - [state] / [states] 把"能不能用"收敛成 4 个枚举值,页面不再自己拼
 *   `checkSelfPermission` + `shouldShowRequestPermissionRationale` 的语义;
 * - 内部用 [PermissionRecord] 记录了"这个权限有没有问过用户",从而能正确区分
 *   **首次拒绝(还能弹)** 与 **永久拒绝(只能去设置)** —— 这正是三方权限库的主要价值,
 *   纯靠系统 API 判断会把"刚装还没问过"误判成永久拒绝;
 * - [normalize] 做版本自适应(如 33+ 的媒体权限 vs 旧存储权限),业务只写语义组。
 */
object Permission {

    /** 相机 */
    val CAMERA: List<String> = listOf(Manifest.permission.CAMERA)

    /** 麦克风 */
    val MICROPHONE: List<String> = listOf(Manifest.permission.RECORD_AUDIO)

    /** 通讯录 */
    val CONTACTS: List<String> = listOf(Manifest.permission.READ_CONTACTS)

    /**
     * 相册/媒体:33+ 走分类媒体权限,更低版本回退到存储权限。
     * 只是"选几张图"建议用系统照片选择器(免权限),见 [PermissionResult] 用法示例页。
     */
    val MEDIA: List<String>
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            listOf(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            listOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

    /**
     * 通知:33 起才是运行时权限,更低版本由系统直接放行(无需申请)。
     */
    val NOTIFICATION: List<String>
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            listOf(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            emptyList()
        }

    /** 精确定位(31+ 需要与粗略定位一起申请,否则弹窗只显示"大致位置") */
    val LOCATION: List<String>
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            listOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        } else {
            listOf(Manifest.permission.ACCESS_FINE_LOCATION)
        }

    /** 便捷判断:一组权限是否已全部可用(空集合视为已满足) */
    fun isGranted(context: Context, vararg permissions: String): Boolean =
        states(context, permissions).values.all { it == PermissionState.GRANTED }

    /** 便捷判断:一组权限是否已全部可用 */
    fun isGranted(context: Context, permissions: Collection<String>): Boolean =
        states(context, permissions).values.all { it == PermissionState.GRANTED }

    /** 单个权限的当前状态(见 [PermissionState] 各分支含义) */
    fun state(context: Context, permission: String): PermissionState {
        val target = normalize(permission)
        // 版本回退映射到了另一个权限(如 33 以下请求媒体权限),按映射后的权限判断
        if (target != permission) return state(context, target)
        // 该版本没有对应运行时权限(如 33 以下的通知权限),视为无需申请
        if (target.isEmpty()) return PermissionState.GRANTED
        if (!isDeclared(context, target)) return PermissionState.UNDECLARED
        if (ContextCompat.checkSelfPermission(context, target) == PackageManager.PERMISSION_GRANTED) {
            return PermissionState.GRANTED
        }
        // 还没向用户发起过申请:系统弹窗一定会出现
        if (!PermissionRecord.hasRequested(context, target)) return PermissionState.DENIED
        // 曾在设置页里被用户手动关掉:重新弹窗依然有效
        if (PermissionRecord.wasGranted(context, target)) return PermissionState.DENIED
        val rationale = (context as? Activity)
            ?.let { ActivityCompat.shouldShowRequestPermissionRationale(it, target) } ?: false
        return if (rationale) PermissionState.DENIED else PermissionState.DENIED_FOREVER
    }

    /** 批量查询,保持传入顺序,便于直接渲染到界面 */
    fun states(context: Context, permissions: Array<out String>): Map<String, PermissionState> =
        states(context, permissions.asList())

    /** 批量查询(去重后保序);低版本上被归一/无需申请的权限会从结果里剔除 */
    fun states(context: Context, permissions: Collection<String>): Map<String, PermissionState> =
        permissions.effective().associateWith { state(context, it) }

    /**
     * 系统弹窗**真的能起作用**的权限(过滤掉已授予、无需申请、以及 manifest 未声明的)。
     * 传空给 launch 会让系统直接回调,所以申请前先过一遍这道筛子。
     */
    fun needRequest(context: Context, permissions: Collection<String>): List<String> =
        permissions.effective().filter { state(context, it) != PermissionState.GRANTED }

    /** 版本归一 + 去重 + 保序 + 去掉本版本不存在的权限 */
    internal fun Collection<String>.effective(): List<String> =
        map { normalize(it) }.filter { it.isNotEmpty() }.distinct()

    /**
     * 版本自适应归一:
     * - 高版本媒体权限在低版本上不存在 -> 归一到 `READ_EXTERNAL_STORAGE`;
     * - 低版本(33 以下)的通知权限 -> 归一为空(表示无需申请);
     * - 其余原样返回。
     */
    fun normalize(permission: String): String = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> permission
        permission == Manifest.permission.READ_MEDIA_IMAGES ->
            Manifest.permission.READ_EXTERNAL_STORAGE
        permission == Manifest.permission.READ_MEDIA_VIDEO ->
            Manifest.permission.READ_EXTERNAL_STORAGE
        permission == Manifest.permission.READ_MEDIA_AUDIO ->
            Manifest.permission.READ_EXTERNAL_STORAGE
        permission == Manifest.permission.POST_NOTIFICATIONS -> ""
        else -> permission
    }

    /** manifest 是否声明了该权限(未声明的申请必然被秒拒,是最难排查的一类"权限拿不到") */
    fun isDeclared(context: Context, permission: String): Boolean =
        declared(context).contains(permission)

    private var declaredCache: Set<String>? = null

    private fun declared(context: Context): Set<String> = declaredCache ?: runCatching {
        @Suppress("DEPRECATION")
        val info = context.packageManager
            .getPackageInfo(context.packageName, PackageManager.GET_PERMISSIONS)
        info.requestedPermissions?.toSet().orEmpty()
    }.getOrDefault(emptySet()).also { declaredCache = it }

    /** 权限的中文简称,用于界面展示("android.permission.CAMERA" 这类串没人看得懂) */
    fun label(permission: String): String = when (permission) {
        Manifest.permission.CAMERA -> "相机"
        Manifest.permission.RECORD_AUDIO -> "麦克风"
        Manifest.permission.READ_CONTACTS -> "通讯录"
        Manifest.permission.POST_NOTIFICATIONS -> "通知"
        Manifest.permission.ACCESS_FINE_LOCATION -> "精确定位"
        Manifest.permission.ACCESS_COARSE_LOCATION -> "大致定位"
        Manifest.permission.READ_MEDIA_IMAGES -> "照片"
        Manifest.permission.READ_MEDIA_VIDEO -> "视频"
        Manifest.permission.READ_MEDIA_AUDIO -> "音频文件"
        Manifest.permission.READ_EXTERNAL_STORAGE -> "存储(读)"
        Manifest.permission.WRITE_EXTERNAL_STORAGE -> "存储(写)"
        Manifest.permission.READ_PHONE_STATE -> "设备信息"
        else -> permission.substringAfterLast('.')
    }

    /**
     * 打开本应用的权限设置页。
     * 走 [Settings.ACTION_APPLICATION_DETAILS_SETTINGS] 是唯一稳妥的入口:
     * 各 ROM 的"权限管理"页面没有公开 Intent 常量,直达子页面容易闪退。
     */
    fun openAppSettings(context: Context) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            // 极少数定制 ROM 连这个 action 都没有,兜底进应用信息页
            context.startActivity(
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    .setData(Uri.fromParts("package", context.packageName, null))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }
}
