package com.hy.basic.vehicle.protocol.dbc

/**
 * DBC 解析结果，按 ID/名称索引报文。
 *
 * 用法：
 * `db.message(0x100)?.signal("Speed")`、`db.message("Powertrain")`。
 * 注意：issues 记录跳过或纠正的行，接入前先看一遍。
 */
data class DbcDatabase(
    /** 解析出的报文,保持 DBC 里的顺序 */
    val messages: List<DbcMessage> = emptyList(),
    /** 解析问题清单 */
    val issues: List<String> = emptyList(),
) {

    /** ID + 扩展帧标志 -> 报文 */
    private val byId = messages.associateBy { it.id to it.extended }

    /** 报文名 -> 报文 */
    private val byName = messages.associateBy { it.name }

    /** 按 ID + 扩展帧标志查报文 */
    fun message(id: Int, extended: Boolean = false): DbcMessage? = byId[id to extended]

    /** 按报文名查 */
    fun message(name: String): DbcMessage? = byName[name]

    /** 按报文 + 信号名查 */
    fun signal(messageId: Int, signalName: String, extended: Boolean = false): DbcSignal? =
        message(messageId, extended)?.signal(signalName)

    /** 是否没有解析出任何报文 */
    val isEmpty: Boolean get() = messages.isEmpty()
}
