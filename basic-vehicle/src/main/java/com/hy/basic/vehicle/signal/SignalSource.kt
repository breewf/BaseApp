package com.hy.basic.vehicle.signal

import kotlinx.coroutines.flow.Flow

/**
 * desc: 信号数据源。
 *
 * 车辆信号可能来自 CAN、厂商广播、CarProperty,甚至本地仿真。
 * 统一成这个接口后,上层只订阅 key,不关心底层通道。
 *
 * key 由业务自己定,建议带来源前缀,例如 `can:0x18F:Speed`、`sim:vehicleSpeed`。
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
