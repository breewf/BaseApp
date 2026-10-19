package com.hy.basic.permission

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

/**
 * desc: 记录“某个权限有没有向用户发起过申请、上一次是什么结果”。
 * 系统只给两个信号,单靠系统 API 无法区分“还能弹”和“只能去设置”,
 * 这里用 SharedPreferences 补齐这段状态记录。
 */
internal object PermissionRecord {

    private const val SP = "hy_basic_permission_record"
    private const val KEY_REQUESTED_PREFIX = "requested_"
    private const val KEY_GRANTED_PREFIX = "granted_"

    /** 是否曾向用户发起过该权限的申请 */
    fun hasRequested(context: Context, permission: String): Boolean =
        sp(context).getBoolean(KEY_REQUESTED_PREFIX + permission, false)

    /** 上一次申请/查询时是否为"已授予"(用于识别"在设置页里被手动关掉"的情形) */
    fun wasGranted(context: Context, permission: String): Boolean =
        sp(context).getBoolean(KEY_GRANTED_PREFIX + permission, false)

    /**
     * 申请回调后落库。传入的 [results] 为系统返回的 权限 -> 是否授予。
     * 同步把当前真实状态也写一次,保证后续 [Permission.state] 的判断与磁盘记录一致。
     */
    fun save(context: Context, results: Map<String, Boolean>) {
        results.forEach { (permission, granted) ->
            sp(context).edit()
                .putBoolean(KEY_REQUESTED_PREFIX + permission, true)
                .putBoolean(KEY_GRANTED_PREFIX + permission, granted)
                .apply()
        }
    }

    /** 主动刷新一次当前状态(如从设置页返回后),不产生"已申请"标记 */
    fun sync(context: Context, permission: String) {
        val granted = ContextCompat.checkSelfPermission(
            context, permission,
        ) == PackageManager.PERMISSION_GRANTED
        sp(context).edit().putBoolean(KEY_GRANTED_PREFIX + permission, granted).apply()
    }

    private fun sp(context: Context) =
        context.applicationContext.getSharedPreferences(SP, Context.MODE_PRIVATE)
}
