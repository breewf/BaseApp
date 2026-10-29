package com.hy.basic.vehicle.signal

import java.io.Closeable
import java.util.concurrent.CopyOnWriteArrayList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge

/**
 * 车辆信号统一入口，按 key 聚合多个 [SignalSource]。
 *
 * 用法：
 * `manager.register(canSource)`，`manager.observe<Double>("can:0x18F:Speed", staleTimeoutMs = 300)`。
 * 注意：先注册再订阅；observe 使用注册时的源快照；同名源只保留一个。
 * 关闭后不能再注册、订阅或写入。
 *
 * @param defaultStaleTimeoutMs 默认超时；传 null 表示不自动补 STALE
 */
class VehicleSignalManager(
    /** 默认超时，子调用可覆盖 */
    private val defaultStaleTimeoutMs: Long? = null,
) : Closeable {

    /** 已注册的数据源 */
    private val sources = CopyOnWriteArrayList<SignalSource>()

    /** 是否已关闭 */
    @Volatile
    private var closed = false

    init {
        require(defaultStaleTimeoutMs == null || defaultStaleTimeoutMs > 0) {
            "defaultStaleTimeoutMs 必须大于 0: $defaultStaleTimeoutMs"
        }
    }

    /** 注册数据源;同名已存在或管理器已关闭时返回 false */
    fun register(source: SignalSource): Boolean {
        if (closed) return false
        if (sources.any { it.name == source.name }) return false
        sources += source
        return true
    }

    /** 按实例注销 */
    fun unregister(source: SignalSource) {
        sources.remove(source)
    }

    /** 按名称注销,返回是否有源被移除 */
    fun unregister(name: String): Boolean = sources.removeAll { it.name == name }

    /** 当前数据源快照 */
    fun sources(): List<SignalSource> = sources.toList()

    /**
     * 订阅信号。多个源会合并;[staleTimeoutMs] 非空时超时自动补 STALE。
     * 调用方需保证 T 和源实际类型一致，例如 CAN 信号用 Double。
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> observe(
        key: String,
        staleTimeoutMs: Long? = defaultStaleTimeoutMs,
    ): Flow<SignalValue<T>> {
        require(staleTimeoutMs == null || staleTimeoutMs > 0) {
            "staleTimeoutMs 必须大于 0: $staleTimeoutMs"
        }
        if (closed) return emptyFlow()
        val flows = sources.map { it.observe(key) }
        if (flows.isEmpty()) return emptyFlow()

        // 源按各自类型发射，合并后统一按 Any? 处理，调用方再用 T 取用
        val merged = merge(*flows.toTypedArray()) as Flow<SignalValue<Any?>>
        val timed = if (staleTimeoutMs != null) {
            merged.withStaleTimeout(staleTimeoutMs)
        } else {
            merged
        }
        return timed.map { it as SignalValue<T> }
    }

    /** 最近一次值,后注册的源优先 */
    @Suppress("UNCHECKED_CAST")
    fun <T> latest(key: String): SignalValue<T>? =
        if (closed) null
        else sources.asReversed().firstNotNullOfOrNull { it.latest(key) } as SignalValue<T>?

    /** 下发值,后注册的源优先;所有源都不支持时返回 false */
    suspend fun write(key: String, value: Any): Boolean {
        if (closed) return false
        for (source in sources.asReversed()) {
            if (source.write(key, value)) return true
        }
        return false
    }

    /** 注销并关闭全部数据源，关闭后不能继续注册或订阅 */
    override fun close() {
        if (closed) return
        closed = true
        sources.forEach { runCatching { it.close() } }
        sources.clear()
    }

    /** 清空数据源,不关闭监听;主要给测试用 */
    fun clear() {
        sources.clear()
    }

    companion object {

        /** 进程内默认实例 */
        val default: VehicleSignalManager by lazy { VehicleSignalManager() }
    }
}
