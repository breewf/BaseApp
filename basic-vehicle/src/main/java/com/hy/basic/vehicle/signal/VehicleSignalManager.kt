package com.hy.basic.vehicle.signal

import java.util.concurrent.CopyOnWriteArrayList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.merge

/**
 * 车辆信号统一入口，按 key 聚合多个 [SignalSource]。
 *
 * 用法：
 * `manager.register(canSource)`，`manager.observe("can:0x18F:Speed", staleTimeoutMs = 300)`。
 * 注意：先注册再订阅；observe 使用注册时的源快照；同名源只保留一个。
 */
class VehicleSignalManager {

    /** 已注册的数据源 */
    private val sources = CopyOnWriteArrayList<SignalSource>()

    /** 注册数据源;同名已存在时返回 false */
    fun register(source: SignalSource): Boolean {
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
     */
    fun observe(key: String, staleTimeoutMs: Long? = null): Flow<SignalValue<Double>> {
        val flows = sources.map { it.observe(key) }
        if (flows.isEmpty()) return emptyFlow()

        val merged = merge(*flows.toTypedArray())
        return if (staleTimeoutMs != null) {
            merged.withStaleTimeout(staleTimeoutMs)
        } else {
            merged
        }
    }

    /** 最近一次值,后注册的源优先 */
    fun latest(key: String): SignalValue<Double>? =
        sources.asReversed().firstNotNullOfOrNull { it.latest(key) }

    /** 下发物理值,后注册的源优先;所有源都不支持时返回 false */
    fun write(key: String, value: Double): Boolean =
        sources.asReversed().any { it.write(key, value) }

    /** 清空数据源,主要给测试用 */
    fun clear() {
        sources.clear()
    }

    companion object {

        /** 进程内默认实例 */
        val default: VehicleSignalManager by lazy { VehicleSignalManager() }
    }
}
