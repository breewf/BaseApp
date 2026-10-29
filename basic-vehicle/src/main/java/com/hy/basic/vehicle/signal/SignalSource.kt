package com.hy.basic.vehicle.signal

import kotlinx.coroutines.flow.Flow

/**
 * 信号数据源，CAN/广播/CarProperty/仿真统一实现这个接口。
 *
 * 用法：
 * `source.observe("can:0x18F:Speed").collect { ... }`，再交给 VehicleSignalManager 统一订阅。
 * 注意：key 由业务定义，建议带来源前缀；不支持时 observe 返回 emptyFlow。
 */
interface SignalSource {

    /** 数据源名,用于日志和去重 */
    val name: String

    /** 订阅指定信号;不支持该 key 时返回 emptyFlow */
    fun observe(key: String): Flow<SignalValue<Double>>

    /** 最近一次值;没有返回 null */
    fun latest(key: String): SignalValue<Double>?

    /** 下发物理值;只读数据源返回 false */
    fun write(key: String, value: Double): Boolean = false
}
