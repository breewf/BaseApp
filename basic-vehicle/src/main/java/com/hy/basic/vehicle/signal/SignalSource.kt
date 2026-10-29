package com.hy.basic.vehicle.signal

import java.io.Closeable
import kotlinx.coroutines.flow.Flow

/**
 * 信号数据源，CAN/广播/CarProperty/仿真统一实现这个接口。
 *
 * 用法：
 * `source.observe("can:0x18F:Speed").collect { ... }`，再交给 VehicleSignalManager 统一订阅。
 * 注意：值类型由具体源决定，管理器按调用方指定的 T 取用；不支持时 observe 返回 emptyFlow。
 */
interface SignalSource : Closeable {

    /** 数据源名,用于日志和去重 */
    val name: String

    /** 订阅指定信号;不支持该 key 时返回 emptyFlow */
    fun observe(key: String): Flow<SignalValue<*>>

    /** 最近一次值;没有返回 null */
    fun latest(key: String): SignalValue<*>?

    /** 下发值;只读数据源返回 false */
    suspend fun write(key: String, value: Any): Boolean = false

    /** 释放数据源占用的监听/协程;默认无资源可释放 */
    override fun close() {}
}
