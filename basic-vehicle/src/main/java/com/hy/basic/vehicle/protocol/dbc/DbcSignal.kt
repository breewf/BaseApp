package com.hy.basic.vehicle.protocol.dbc

import com.hy.basic.vehicle.protocol.can.CanFrame
import com.hy.basic.vehicle.protocol.can.CanSignalCodec
import com.hy.basic.vehicle.protocol.can.CanSignalSpec
import com.hy.basic.vehicle.protocol.can.CanSignalValue

/**
 * desc: DBC 里的一条信号。
 *
 * [spec] 直接复用 CAN 信号定义,解析和处理流程和手写 [CanSignalSpec] 完全一致。
 * 多路复用、接收节点、注释这些 DBC 专有信息单独保留,不影响解码。
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
