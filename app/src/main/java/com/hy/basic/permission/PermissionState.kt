package com.hy.basic.permission

import android.content.Context

/**
 * desc: 单个运行时权限的**只读**状态(不弹窗,随时可查)。
 *
 * 与 [PermissionResult] 的分工:
 * - [PermissionState] 用于"做事之前先判断",决定要不要弹说明、要不要直接跳设置页;
 * - [PermissionResult] 用于"弹窗之后拿结果",驱动后续业务分支。
 */
enum class PermissionState {

    /** 已授予,可直接使用 */
    GRANTED,

    /** 未授予,且系统弹窗还会正常出现(首次申请 / 普通过期 / 用户主动撤销) */
    DENIED,

    /**
     * 未授予且**系统不会再弹窗**:用户勾了"禁止后不再询问",或 Android 11+ 连续两次拒绝。
     * 唯一出路是引导用户去应用权限设置页手动开启,`requestPermissions` 对它只会秒回 false。
     */
    DENIED_FOREVER,

    /** AndroidManifest 里根本没有 `uses-permission` 声明 —— 申请必被系统秒拒,属开发期配置错误 */
    UNDECLARED,
    ;

    /** 是否已经拿到权限(可直接执行业务) */
    val isGranted: Boolean get() = this == GRANTED

    /** 是否还有救:能弹窗问用户(区别于 [DENIED_FOREVER] 与 [UNDECLARED]) */
    val canRequestAgain: Boolean get() = this == GRANTED || this == DENIED
}

/**
 * desc: 一次权限申请的完整结果。
 *
 * 三个集合互斥且覆盖全部申请项:`requested == granted + denied + deniedForever`。
 * 组合场景(一次问多个权限)下能准确区分"哪些拿到了""哪些还能再问""哪些只能去设置页",
 * 页面只需要 `when` 三个派生属性即可写完全部 UI 分支。
 */
data class PermissionResult(
    val granted: Set<String>,
    val denied: Set<String>,
    val deniedForever: Set<String>,
) {

    /** 全部授予:业务可以放心往下走 */
    val allGranted: Boolean get() = denied.isEmpty() && deniedForever.isEmpty()

    /** 部分授予:功能可用但降级,通常提示一次即可 */
    val partiallyGranted: Boolean get() = granted.isNotEmpty() && !allGranted

    /** 有权限被"永久拒绝":需要弹窗引导去设置页,重复申请没有意义 */
    val needsSettings: Boolean get() = deniedForever.isNotEmpty()

    /** 有权限只是普通拒绝:下次进入还能再弹窗申请,不必跳设置 */
    val canAskAgain: Boolean get() = denied.isNotEmpty()

    /** 一个都没拿到 */
    val allDenied: Boolean get() = granted.isEmpty()

    /** 未授予的权限(普通拒绝 + 永久拒绝),用于日志与文案 */
    val deniedAll: Set<String> get() = denied + deniedForever

    /** 一行式结论,直接可用于 Toast / 日志 */
    fun summary(): String = when {
        allGranted -> "全部已授予"
        allDenied && needsSettings -> "全部被永久拒绝,需前往设置开启"
        allDenied -> "已拒绝,可再次申请"
        needsSettings -> "部分授予,${deniedAll.size} 项需去设置开启"
        else -> "部分授予,${deniedAll.size} 项被拒绝"
    }

    companion object {

        /** 拿不到宿主时的安全兼底值(相当于"一项都没申请到") */
        val empty = PermissionResult(emptySet(), emptySet(), emptySet())

        /** 未发起申请即按当前状态回填(如已在设置页授予后的复查) */
        fun of(states: Map<String, PermissionState>): PermissionResult {
            val granted = states.filterValues { it == PermissionState.GRANTED }.keys
            val forever = states.filterValues {
                it == PermissionState.DENIED_FOREVER || it == PermissionState.UNDECLARED
            }.keys
            val denied = states.filterValues { it == PermissionState.DENIED }.keys
            return PermissionResult(granted.toSet(), denied.toSet(), forever.toSet())
        }

        /** 不弹窗,直接按当前实际状态生成结果 */
        fun current(context: Context, permissions: Collection<String>): PermissionResult =
            of(Permission.states(context, permissions))
    }
}
