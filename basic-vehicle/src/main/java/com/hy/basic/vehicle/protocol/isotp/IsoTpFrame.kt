package com.hy.basic.vehicle.protocol.isotp

/**
 * ISO-TP 帧类型。
 *
 * 用法：
 * `IsoTpCodec.decode(frame)` 返回具体类型，通道层据此做重组。
 * 注意：payload 是 PCI 之后的有效数据，不包含填充字节。
 */
sealed interface IsoTpFrame {

    /** 单帧，负载已完整 */
    data class Single(
        /** 完整负载 */
        val payload: ByteArray,
    ) : IsoTpFrame

    /** 首帧，后面还有连续帧 */
    data class First(
        /** 本次消息总长度 */
        val totalLength: Int,
        /** 首帧携带的负载 */
        val payload: ByteArray,
    ) : IsoTpFrame

    /** 连续帧 */
    data class Consecutive(
        /** 序号 0..15，首帧之后从 1 开始 */
        val sequence: Int,
        /** 本帧携带的负载 */
        val payload: ByteArray,
    ) : IsoTpFrame

    /** 流控帧 */
    data class FlowControl(
        /** 继续发送/等待/溢出 */
        val status: IsoTpFlowStatus,
        /** 发送方还可以连续发多少帧，0 表示不限 */
        val blockSize: Int,
        /** 连续帧最小间隔，单位微秒 */
        val separationTimeUs: Long,
    ) : IsoTpFrame
}

/** 流控状态 */
enum class IsoTpFlowStatus {
    /** 继续发送 */
    CONTINUE,

    /** 暂停，等下一帧流控 */
    WAIT,

    /** 接收方缓冲区不足 */
    OVERFLOW,
}
