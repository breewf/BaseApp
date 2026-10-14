package com.hy.myapp.demo.permission

import android.content.Context
import com.hy.basic.BaseViewModel
import com.hy.basic.permission.Permission
import com.hy.basic.permission.PermissionResult
import com.hy.basic.permission.PermissionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * desc: 权限演示页的 ViewModel —— 只负责**展示状态与结果**,申请动作留在页面里。
 *
 * 边界说明:发起申请必须绑到一个 `ActivityResultCaller`(Activity/Fragment)上,VM 拿不到也不该拿;
 * 而"读状态"只依赖 Context,是无副作用查询,放 VM 里没有任何生命周期风险。
 * 所以这里只做状态渲染,VM 里不出现任何请求逻辑,与项目现有 MVVM 分层一致。
 */
class PermissionDemoViewModel : BaseViewModel() {

    /** 本页关注的权限(顺序即展示顺序);媒体/通知组会自动按系统版本归一 */
    private val watchList by lazy {
        Permission.CAMERA + Permission.MEDIA + Permission.MICROPHONE + Permission.NOTIFICATION
    }

    private val _statusText = MutableStateFlow("")

    /** 权限总览(每个权限当前是什么状态) */
    val statusText = _statusText.asStateFlow()

    private val _logText = MutableStateFlow(DEFAULT_LOG)

    /** 最近若干次申请结果 + 下一步建议 */
    val logText = _logText.asStateFlow()

    private val logs = ArrayDeque<String>()

    /** 刷新权限总览(进入页面、从设置页返回时各调一次即可) */
    fun refreshStatus(context: Context) {
        _statusText.value = Permission.states(context, watchList)
            .entries
            .joinToString("\n") { (permission, state) ->
                "· ${Permission.label(permission)}：${describe(state)}"
            }
    }

    /** 记录一次申请结果,并把结论翻译成"下一步该干什么" */
    fun logRequest(title: String, result: PermissionResult) {
        appendLog("【$title】${result.summary()}\n${advice(result)}")
    }

    /** 记录一条与申请无关的说明(如免权限选图、拍照结果) */
    fun logMessage(message: String) = appendLog(message)

    /** 由结果直接给出行动建议,页面文案不必自己拼 when 分支 */
    private fun advice(result: PermissionResult): String = when {
        result.allGranted -> "→ 全部可用，业务直接往下走"
        result.needsSettings -> "→ ${names(result.deniedForever)} 已勾选不再询问，系统不会再弹窗，只能去设置里开"
        else -> "→ ${names(result.denied)} 属于本次拒绝，下次进入还能再弹窗申请"
    }

    private fun names(permissions: Set<String>) =
        permissions.joinToString("、") { Permission.label(it) }.ifEmpty { "无" }

    private fun describe(state: PermissionState) = when (state) {
        PermissionState.GRANTED -> "已授予"
        PermissionState.DENIED -> "未授予（弹窗还会出现，可再次申请）"
        PermissionState.DENIED_FOREVER -> "已永久拒绝（不会再弹窗，需去设置）"
        PermissionState.UNDECLARED -> "manifest 未声明（申请必被秒拒，先补 uses-permission）"
    }

    private fun appendLog(line: String) {
        logs.addFirst(line)
        while (logs.size > MAX_LOG) logs.removeLast()
        _logText.value = logs.joinToString("\n\n")
    }

    private companion object {
        const val MAX_LOG = 10
        const val DEFAULT_LOG = "结果日志：点下面的按钮申请权限，这里会打印结果与下一步建议。"
    }
}
