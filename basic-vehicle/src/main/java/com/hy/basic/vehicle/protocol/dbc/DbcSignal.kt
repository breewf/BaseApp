package com.hy.basic.vehicle.protocol.dbc

import com.hy.basic.vehicle.protocol.can.CanFrame
import com.hy.basic.vehicle.protocol.can.CanSignalCodec
import com.hy.basic.vehicle.protocol.can.CanSignalSpec
import com.hy.basic.vehicle.protocol.can.CanSignalValue

/**
 * DBC 信号，复用 [CanSignalSpec] 做解码。
 *
 * 用法：
 * `signal.decode(frame)`；DBC 专有信息通过 multiplexer/receivers/comment 读取。
 * 注意：多路复用信号需要业务按选择子自行筛选。
 */
data class DbcSignal(
    /** 解码/编码用信号定义 */
    val spec: CanSignalSpec,
    /** 多路复用标识:M / m0 / m0M;普通信号为 null */
    val multiplexer: String? = null,
    /** 接收节点,空表示未指定 */
    val receivers: List<String> = emptyList(),
    /** 信号注释 */
    val comment: String? = null,
    /** 起始 raw 值,来自 GenSigStartValue */
    val startValueRaw: Long? = null,
) {

    /** 信号名 */
    val name: String get() = spec.name

    /** 按当前信号定义解码一帧 */
    fun decode(frame: CanFrame): CanSignalValue = CanSignalCodec.decode(frame, spec)
}
