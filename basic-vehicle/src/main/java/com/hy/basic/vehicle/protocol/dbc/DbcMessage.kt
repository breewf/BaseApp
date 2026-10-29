package com.hy.basic.vehicle.protocol.dbc

import com.hy.basic.vehicle.protocol.can.CanFrame
import com.hy.basic.vehicle.protocol.can.CanSignalValue

/**
 * desc: DBC 里的一帧报文。
 *
 * 只保留解析和仿真常用字段,发送节点、周期、注释都带着,方便做信号矩阵展示。
 * 解码时仍按 [signals] 里的信号定义逐条取。
 */
data class DbcMessage(
    /** 规范化后的报文 ID,不含扩展帧标志位 */
    val id: Int,
    /** 报文名 */
    val name: String,
    /** 数据长度 */
    val dlc: Int,
    /** 发送节点 */
    val sender: String,
    /** 是否扩展帧 */
    val extended: Boolean,
    /** 周期(ms),来自 GenMsgCycleTime */
    val cycleTimeMs: Int? = null,
    /** 报文注释 */
    val comment: String? = null,
    /** 信号列表 */
    val signals: List<DbcSignal> = emptyList(),
) {

    /** 按名称找信号 */
    fun signal(name: String): DbcSignal? = signals.firstOrNull { it.name == name }

    /** 按当前报文信息创建一帧 */
    fun frame(data: ByteArray, fd: Boolean = false): CanFrame =
        CanFrame(id = id, data = data, extended = extended, fd = fd)

    /** 解一帧里的全部信号 */
    fun decode(frame: CanFrame): List<CanSignalValue> = signals.map { it.decode(frame) }
}
