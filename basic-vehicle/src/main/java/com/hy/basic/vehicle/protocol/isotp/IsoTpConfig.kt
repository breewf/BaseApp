package com.hy.basic.vehicle.protocol.isotp

/**
 * ISO-TP(ISO 15765-2) 通道参数。
 *
 * 用法：
 * `IsoTpConfig(requestId = 0x7E0, responseId = 0x7E8)`，CAN FD 诊断把 [fd] 设为 true。
 * 注意：当前实现支持最大 4095 字节；[padding] 非空时经典帧补到 8 字节、FD 补到 64 字节。
 */
data class IsoTpConfig(
    /** 诊断请求 CAN ID */
    val requestId: Int,
    /** 诊断响应 CAN ID */
    val responseId: Int,
    /** 是否使用 29 位扩展帧 */
    val extended: Boolean = false,
    /** 是否使用 CAN FD */
    val fd: Boolean = false,
    /** CAN FD 是否开启 BRS */
    val bitRateSwitch: Boolean = false,
    /** 填充字节；null 表示不额外填充 */
    val padding: Int? = null,
    /** 本端接收多帧时，每隔多少帧发一次流控；0 表示只在开始时发一次 */
    val blockSize: Int = 0,
    /** 本端流控帧里的最小间隔，单位 ms，范围 0..127 */
    val separationTimeMs: Int = 0,
    /** 默认请求超时 */
    val timeoutMs: Long = 1_000L,
) {

    init {
        val maxId = if (extended) 0x1FFFFFFF else 0x7FF
        require(requestId in 0..maxId) { "requestId 超出范围: 0x${requestId.toString(16)}" }
        require(responseId in 0..maxId) { "responseId 超出范围: 0x${responseId.toString(16)}" }
        require(!bitRateSwitch || fd) { "BRS 只适用于 CAN FD" }
        require(padding == null || padding in 0..0xFF) { "padding 必须在 0..255: $padding" }
        require(blockSize in 0..0xFF) { "blockSize 必须在 0..255: $blockSize" }
        require(separationTimeMs in 0..127) { "separationTimeMs 必须在 0..127: $separationTimeMs" }
        require(timeoutMs > 0) { "timeoutMs 必须大于 0: $timeoutMs" }
    }

    /** 单帧能携带的最大字节数，经典 CAN 为 7，CAN FD 为 62 */
    val singleFrameCapacity: Int get() = if (fd) 62 else 7

    /** 首帧里除 PCI 外可携带的字节数 */
    val firstFrameCapacity: Int get() = if (fd) 62 else 6

    /** 连续帧里除 PCI 外可携带的字节数 */
    val consecutiveFrameCapacity: Int get() = if (fd) 63 else 7

    /** 当前实现支持的 UDS 负载上限 */
    val maxPayloadLength: Int get() = 4_095
}
