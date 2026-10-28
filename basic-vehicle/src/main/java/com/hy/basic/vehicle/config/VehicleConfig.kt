package com.hy.basic.vehicle.config

import com.hy.basic.storage.MMKVStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

/**
 * desc: 车型 / 功能开关的持久化配置。
 *
 * 同一份 App 常要按车型裁功能,把开关集中到这里:MMKV 落盘、默认值兜底、
 * 写入后用 [changed] 通知同进程页面刷新,省得每个页面都去读一遍磁盘。
 *
 * 注意:
 * - key 自动加 `vehicle_cfg_` 前缀,避免和业务其它配置撞;
 * - 默认库是单进程模式,通信进程也要读配置时,得换成 MMKV 多进程实例单独封装;
 * - [changed] 只在本进程内通知,跨进程请走 EventBus 或广播;
 * - 配置是"功能开关"级别的东西,不要把实时信号值往这里塞。
 */
object VehicleConfig {

    /** key 统一前缀,避免和业务其它配置冲突 */
    private const val PREFIX = "vehicle_cfg_"

    /** 本进程内的变更通知源 */
    private val _changed = MutableSharedFlow<String>(extraBufferCapacity = 16)

    /** 变更通知,内容是去掉前缀后的业务 key */
    val changed: SharedFlow<String> = _changed.asSharedFlow()

    // ==== 读写 ====

    /** 写入布尔开关 */
    fun putBoolean(key: String, value: Boolean) {
        MMKVStore.putBoolean(realKey(key), value)
        notifyChanged(key)
    }

    /** 读取布尔开关 */
    fun boolean(key: String, def: Boolean = false): Boolean =
        MMKVStore.getBoolean(realKey(key), def)

    /** 写入整型配置 */
    fun putInt(key: String, value: Int) {
        MMKVStore.putInt(realKey(key), value)
        notifyChanged(key)
    }

    /** 读取整型配置 */
    fun int(key: String, def: Int = 0): Int = MMKVStore.getInt(realKey(key), def)

    /** 写入长整型配置 */
    fun putLong(key: String, value: Long) {
        MMKVStore.putLong(realKey(key), value)
        notifyChanged(key)
    }

    /** 读取长整型配置 */
    fun long(key: String, def: Long = 0L): Long = MMKVStore.getLong(realKey(key), def)

    /** 写入浮点配置 */
    fun putFloat(key: String, value: Float) {
        MMKVStore.putFloat(realKey(key), value)
        notifyChanged(key)
    }

    /** 读取浮点配置 */
    fun float(key: String, def: Float = 0f): Float = MMKVStore.getFloat(realKey(key), def)

    /** 写入字符串配置 */
    fun putString(key: String, value: String) {
        MMKVStore.putString(realKey(key), value)
        notifyChanged(key)
    }

    /** 读取字符串配置 */
    fun string(key: String, def: String = ""): String = MMKVStore.getString(realKey(key), def)

    // ==== 观察:先发当前值,之后 key 匹配才刷新 ====

    /** 观察布尔开关,订阅时先发当前值 */
    fun observeBoolean(key: String, def: Boolean = false): Flow<Boolean> =
        observeValue(key) { boolean(key, def) }

    /** 观察整型配置,订阅时先发当前值 */
    fun observeInt(key: String, def: Int = 0): Flow<Int> =
        observeValue(key) { int(key, def) }

    /** 观察长整型配置,订阅时先发当前值 */
    fun observeLong(key: String, def: Long = 0L): Flow<Long> =
        observeValue(key) { long(key, def) }

    /** 观察浮点配置,订阅时先发当前值 */
    fun observeFloat(key: String, def: Float = 0f): Flow<Float> =
        observeValue(key) { float(key, def) }

    /** 观察字符串配置,订阅时先发当前值 */
    fun observeString(key: String, def: String = ""): Flow<String> =
        observeValue(key) { string(key, def) }

    // ==== 维护 ====

    /** key 是否存在 */
    fun contains(key: String): Boolean = MMKVStore.contains(realKey(key))

    /** 删除单个配置并通知 */
    fun remove(key: String) {
        MMKVStore.remove(realKey(key))
        notifyChanged(key)
    }

    /** 当前已保存的业务 key(不含前缀) */
    fun keys(): List<String> =
        MMKVStore.allKeys()
            .filter { it.startsWith(PREFIX) }
            .map { it.removePrefix(PREFIX) }

    /** 只清车辆配置,不动 MMKV 里的其它业务数据 */
    fun clear() {
        keys().forEach { key ->
            MMKVStore.remove(realKey(key))
            notifyChanged(key)
        }
    }

    /** 订阅指定 key,变化后重新读当前值 */
    private fun <T> observeValue(key: String, read: () -> T): Flow<T> =
        changed.filter { it == key }
            .map { read() }
            .onStart { emit(read()) }
            .distinctUntilChanged()

    /** 拼上统一前缀 */
    private fun realKey(key: String): String = PREFIX + key

    /** 发送变更通知,缓冲满时丢弃 */
    private fun notifyChanged(key: String) {
        _changed.tryEmit(key)
    }
}
