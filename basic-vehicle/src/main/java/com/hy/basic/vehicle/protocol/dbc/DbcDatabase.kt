package com.hy.basic.vehicle.protocol.dbc

/**
 * desc: DBC 解析结果。
 *
 * [issues] 记录解析时跳过或纠正的行,正式接入前先看一眼,能把 DBC 里的坑提前暴露。
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
