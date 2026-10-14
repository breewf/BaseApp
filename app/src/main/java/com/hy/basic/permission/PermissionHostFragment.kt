package com.hy.basic.permission

import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * desc: 无 UI 的权限宿主 Fragment —— 把 `registerForActivityResult` 的注册时机问题一次性解决。
 *
 * 为什么走宿主 Fragment 而不是直接给 Activity 加扩展:
 * `registerForActivityResult` **必须在宿主进入 STARTED 之前调用**,而业务发起申请几乎都在点击
 * 回调里(那时 Activity 早就 STARTED 了),懒注册会直接抛 IllegalStateException。
 * 本类在自己的字段初始化里完成注册(官方推荐的写法),由 [permissionHost] 按需挂到宿主的
 * FragmentManager 上,于是:
 * - 页面代码一行注册都不用写,点击里直接 `awaitPermissions(...)`;
 * - 不要求页面继承任何基类,老的 Java Activity 也能用;
 * - 用协程把"弹窗 -> 回调"拉平成顺序代码,免掉层层嵌套的 callback。
 */
internal class PermissionHostFragment : Fragment() {

    /** 串行化并发申请:系统权限弹窗本身一次只能有一个 */
    private val mutex = Mutex()

    /** 当前等待结果的挂起点;有 mutex 保证同一时刻最多一个 */
    private var pending: CancellableContinuation<PermissionResult>? = null

    private val launcher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { results -> dispatch(results) }

    /**
     * 申请权限挂起等待结果。
     * 已授予的权限不会触发弹窗,只在结果里以"已授予"回填;宿主状态已保存(切后台/销毁中)时
     * 不打扰用户,直接按当前状态返回。
     */
    suspend fun request(permissions: Collection<String>): PermissionResult = mutex.withLock {
        val context = context ?: return@withLock PermissionResult.empty
        val current = Permission.states(context, permissions)
        val need = Permission.needRequest(context, permissions)
        if (need.isEmpty() || parentFragmentManager.isStateSaved) {
            return@withLock PermissionResult.of(current)
        }
        val outcome = suspendCancellableCoroutine { cont ->
            pending = cont
            cont.invokeOnCancellation { if (pending === cont) pending = null }
            launcher.launch(need.toTypedArray())
        }
        // 申请前就已授予的那部分补回 granted,让结果始终覆盖完整的申请集合
        val preGranted = current.filterValues { it == PermissionState.GRANTED }.keys
        outcome.copy(granted = outcome.granted + preGranted)
    }

    private fun dispatch(results: Map<String, Boolean>) {
        val cont = pending
        pending = null
        val ctx = context ?: return
        val act = activity
        PermissionRecord.save(ctx, results)
        val granted = results.filterValues { it }.keys.toSet()
        val notGranted = results.filterValues { !it }.keys
        // 拒绝后系统不再展示弹窗 => 用户勾了"不再询问"(或 11+ 二次拒绝),只能引导去设置页
        val forever = notGranted.filter { act == null || !ActivityCompat.shouldShowRequestPermissionRationale(act, it) }
        cont?.takeIf { it.isActive }?.resume(
            PermissionResult(
                granted = granted,
                denied = (notGranted - forever.toSet()).toSet(),
                deniedForever = forever.toSet(),
            )
        )
    }
}

private const val HOST_TAG = "com.hy.basic.permission.PermissionHostFragment"

/** 取出(或首次挂载)宿主的权限宿主;宿主状态已保存时返回 null,调用方按当前状态兜底 */
internal fun FragmentActivity.permissionHost(): PermissionHostFragment? =
    supportFragmentManager.attachPermissionHost()

/** Fragment 版:挂在 childFragmentManager 上,随该 Fragment 一起销毁,互不串扰 */
internal fun Fragment.permissionHost(): PermissionHostFragment? =
    takeIf { isAdded }?.childFragmentManager?.attachPermissionHost()

private fun FragmentManager.attachPermissionHost(): PermissionHostFragment? {
    (findFragmentByTag(HOST_TAG) as? PermissionHostFragment)?.let { return it }
    if (isStateSaved) return null
    return PermissionHostFragment().also {
        beginTransaction().add(it, HOST_TAG).commitNow()
    }
}
