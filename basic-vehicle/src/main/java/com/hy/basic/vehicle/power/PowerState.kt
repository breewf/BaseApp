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
 * 亮灭屏/休眠唤醒监听。
 *
 * 用法：
 * `context.observeScreenState().collect { on -> ... }`，或直接读 `context.isScreenOn()`。
 * 注意：只处理 AOSP 的 SCREEN_ON/OFF；ACC、READY 等状态需要厂商信号。
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
