@file:JvmName("VehiclePowerState")

package com.hy.basic.vehicle.power

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.PowerManager
import androidx.core.content.ContextCompat
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * desc: 亮灭屏 / 休眠唤醒监听。
 *
 * AOSP 里能稳定拿到的是 SCREEN_ON / SCREEN_OFF,车机熄火休眠通常也走这两个广播;
 * ACC、READY、充电这些状态得看厂商广播或车辆信号,这里不猜。
 *
 * 注意:
 * - 休眠时进程会被冻结,恢复后不要只依赖广播,onResume 里主动读一次 [isScreenOn];
 * - 亮屏不等于前台可见,页面该用 repeatOnLifecycle 收集,别在后台一直刷 UI;
 * - 厂商如果改过电源管理,广播可能延迟,关键业务还是要结合车辆电源信号判断。
 */
object ScreenStateMonitor {

    /** 当前是否亮屏/可交互 */
    fun isScreenOn(context: Context): Boolean =
        (context.getSystemService(Context.POWER_SERVICE) as? PowerManager)?.isInteractive ?: true

    /**
     * 监听亮灭屏。订阅时先发一次当前状态,之后跟随广播更新;
     * 收集方停止时自动注销广播。
     */
    fun observe(context: Context): Flow<Boolean> = callbackFlow {
        val appContext = context.applicationContext
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    Intent.ACTION_SCREEN_ON -> trySend(true)
                    Intent.ACTION_SCREEN_OFF -> trySend(false)
                }
            }
        }
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
        }
        ContextCompat.registerReceiver(
            appContext,
            receiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED,
        )
        trySend(isScreenOn(appContext))
        awaitClose { appContext.unregisterReceiver(receiver) }
    }.distinctUntilChanged()
}

/** 便捷写法:context.observeScreenState() */
fun Context.observeScreenState(): Flow<Boolean> = ScreenStateMonitor.observe(this)

/** 便捷写法:context.isScreenOn() */
fun Context.isScreenOn(): Boolean = ScreenStateMonitor.isScreenOn(this)
