package com.hy.basic.vehicle.protocol.uds

import com.hy.basic.vehicle.protocol.isotp.IsoTpChannel

/**
 * 把 [IsoTpChannel] 接到 UDS 客户端，并吞掉 0x78 ResponsePending。
 *
 * 用法：
 * `val client = UdsClient(IsoTpUdsTransport(channel))`，或直接用 `channel.udsClient()`。
 * 注意：收到 0x78 后会继续等最终响应，整体仍受本次请求的 timeoutMs 限制。
 */
class IsoTpUdsTransport(
    /** 已完成 ISO-TP 分帧/重组和流控的通道 */
    private val channel: IsoTpChannel,
) : UdsTransport {

    /** 发请求并等最终响应；0x78 不作为失败返回 */
    override suspend fun request(payload: ByteArray, timeoutMs: Long): ByteArray? =
        channel.request(payload, timeoutMs) { response ->
            isResponsePending(response)
        }

    companion object {

        /** 判断是否为 7F SID 78 的“还在处理”响应 */
        fun isResponsePending(response: ByteArray): Boolean =
            response.size >= 3 &&
                (response[0].toInt() and 0xFF) == 0x7F &&
                (response[2].toInt() and 0xFF) == UdsNrc.RESPONSE_PENDING
    }
}

/** 用当前 ISO-TP 通道创建 UDS 客户端 */
fun IsoTpChannel.udsClient(defaultTimeoutMs: Long = 1_000L): UdsClient =
    UdsClient(IsoTpUdsTransport(this), defaultTimeoutMs)

/** 把当前通道包装成 UDS 传输层 */
fun IsoTpChannel.asUdsTransport(): UdsTransport = IsoTpUdsTransport(this)
