package com.hy.basic.vehicle.protocol.isotp

import com.hy.basic.vehicle.protocol.can.CanFrame
import com.hy.basic.vehicle.transport.CanTransport
import java.io.Closeable
import java.io.IOException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration.Companion.microseconds
import kotlin.time.Duration.Companion.milliseconds

/**
 * ISO-TP 请求/响应通道，负责单帧、多帧拆分与流控。
 *
 * 用法：
 * `IsoTpChannel(transport, IsoTpConfig(requestId = 0x7E0, responseId = 0x7E8))`，
 * 然后调用 `request(payload)` 完成一次完整交换。
 * 注意：同一实例的请求串行执行；响应超时返回 null，帧序号错误抛 [IsoTpException]。
 */
class IsoTpChannel(
    /** 原始 CAN 收发 */
    private val transport: CanTransport,
    /** 通道参数 */
    private val config: IsoTpConfig,
    /** 收集响应的作用域 */
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
    /** 是否由本实例取消 [scope] */
    private val ownsScope: Boolean = true,
) : Closeable {

    /** 请求串行锁，避免诊断请求互相抢响应 */
    private val mutex = Mutex()

    /** 是否已关闭 */
    @Volatile
    private var closed = false

    /**
     * 发送请求并等待完整响应。
     *
     * [keepWaiting] 返回 true 时继续等下一包，用于 UDS 的 0x78 ResponsePending。
     * 超时返回 null；传输异常直接抛出。
     */
    @OptIn(ExperimentalCoroutinesApi::class, InternalCoroutinesApi::class)
    suspend fun request(
        payload: ByteArray,
        timeoutMs: Long = config.timeoutMs,
        keepWaiting: (ByteArray) -> Boolean = { false },
    ): ByteArray? {
        require(payload.isNotEmpty()) { "ISO-TP 负载不能为空" }
        require(payload.size <= config.maxPayloadLength) {
            "ISO-TP 负载过长: ${payload.size} > ${config.maxPayloadLength}"
        }
        require(timeoutMs > 0) { "timeoutMs 必须大于 0: $timeoutMs" }
        check(!closed) { "ISO-TP 通道已关闭" }

        return mutex.withLock {
            if (closed) return@withLock null

            withTimeoutOrNull(timeoutMs.milliseconds) {
                // UNDISPATCHED 保证 produce 在当前线程完成订阅后才返回，避免丢第一帧
                val inbound = scope.produce<CanFrame>(
                    capacity = Channel.BUFFERED,
                    start = CoroutineStart.UNDISPATCHED,
                ) {
                    transport.frames.collect { frame ->
                        if (frame.id == config.responseId &&
                            frame.extended == config.extended
                        ) {
                            send(frame)
                        }
                    }
                }

                try {
                    sendRequest(payload, inbound)
                    receiveResponse(inbound, keepWaiting)
                } catch (e: ClosedReceiveChannelException) {
                    null
                } finally {
                    inbound.cancel()
                }
            }
        }
    }

    /** 关闭通道；外部传入的 scope 不会被取消 */
    override fun close() {
        if (closed) return
        closed = true
        if (ownsScope) scope.cancel()
    }

    /** 按负载长度选择单帧或首帧+连续帧发送 */
    private suspend fun sendRequest(
        payload: ByteArray,
        inbound: ReceiveChannel<CanFrame>,
    ) {
        if (payload.size <= config.singleFrameCapacity) {
            transport.send(IsoTpCodec.encodeSingle(payload, config))
            return
        }

        transport.send(IsoTpCodec.encodeFirst(payload, config))
        var offset = config.firstFrameCapacity
        var sequence = 1

        while (offset < payload.size) {
            val flowControl = awaitFlowControl(inbound)
            when (flowControl.status) {
                IsoTpFlowStatus.WAIT -> continue
                IsoTpFlowStatus.OVERFLOW -> throw IsoTpException("接收方流控溢出")
                IsoTpFlowStatus.CONTINUE -> Unit
            }

            var sentInBlock = 0
            while (offset < payload.size &&
                (flowControl.blockSize == 0 || sentInBlock < flowControl.blockSize)
            ) {
                val end = minOf(offset + config.consecutiveFrameCapacity, payload.size)
                val chunk = payload.copyOfRange(offset, end)
                transport.send(IsoTpCodec.encodeConsecutive(chunk, sequence, config))

                offset = end
                sequence = (sequence + 1) and 0x0F
                sentInBlock++
                if (offset < payload.size) {
                    delay(flowControl.separationTimeUs.microseconds)
                }
            }
        }
    }

    /** 从接收流里取下一帧流控，数据帧暂时忽略 */
    private suspend fun awaitFlowControl(
        inbound: ReceiveChannel<CanFrame>,
    ): IsoTpFrame.FlowControl {
        while (true) {
            when (val frame = IsoTpCodec.decode(inbound.receive())) {
                is IsoTpFrame.FlowControl -> return frame
                else -> Unit
            }
        }
    }

    /** 从接收流里取出完整响应，多个 pending 响应由 [keepWaiting] 决定是否继续 */
    private suspend fun receiveResponse(
        inbound: ReceiveChannel<CanFrame>,
        keepWaiting: (ByteArray) -> Boolean,
    ): ByteArray {
        while (true) {
            val response = receiveMessage(inbound)
            if (!keepWaiting(response)) return response
        }
    }

    /** 读取一包完整 ISO-TP 消息 */
    private suspend fun receiveMessage(
        inbound: ReceiveChannel<CanFrame>,
    ): ByteArray {
        while (true) {
            when (val frame = IsoTpCodec.decode(inbound.receive())) {
                is IsoTpFrame.Single -> return frame.payload
                is IsoTpFrame.First -> return receiveMultiFrame(frame, inbound)
                is IsoTpFrame.Consecutive -> Unit
                is IsoTpFrame.FlowControl -> Unit
                null -> Unit
            }
        }
    }

    /** 接收首帧后的连续帧，并按 blockSize 回送流控 */
    private suspend fun receiveMultiFrame(
        first: IsoTpFrame.First,
        inbound: ReceiveChannel<CanFrame>,
    ): ByteArray {
        if (first.totalLength > config.maxPayloadLength) {
            transport.send(
                IsoTpCodec.encodeFlowControl(
                    status = IsoTpFlowStatus.OVERFLOW,
                    blockSize = 0,
                    separationTimeMs = config.separationTimeMs,
                    config = config,
                )
            )
            throw IsoTpException("响应超过最大长度: ${first.totalLength}")
        }

        val result = ByteArray(first.totalLength)
        first.payload.copyInto(result)
        var offset = first.payload.size
        if (offset >= result.size) return result

        var expectedSequence = 1
        var receivedInBlock = 0

        // 告诉 ECU 可以开始发连续帧
        sendFlowControl()

        while (offset < result.size) {
            when (val frame = IsoTpCodec.decode(inbound.receive())) {
                is IsoTpFrame.Consecutive -> {
                    if (frame.sequence != expectedSequence) {
                        throw IsoTpException(
                            "连续帧序号错误: 期望 $expectedSequence, 实际 ${frame.sequence}"
                        )
                    }
                    val take = minOf(frame.payload.size, result.size - offset)
                    if (take <= 0) throw IsoTpException("连续帧没有有效数据")
                    frame.payload.copyInto(result, offset, 0, take)
                    offset += take
                    expectedSequence = (expectedSequence + 1) and 0x0F
                    receivedInBlock++

                    if (config.blockSize in 1..receivedInBlock &&
                        offset < result.size
                    ) {
                        sendFlowControl()
                        receivedInBlock = 0
                    }
                }

                is IsoTpFrame.FlowControl -> {
                    if (frame.status == IsoTpFlowStatus.OVERFLOW) {
                        throw IsoTpException("接收方流控溢出")
                    }
                }

                else -> throw IsoTpException("多帧响应里出现非法帧")
            }
        }
        return result
    }

    /** 按当前配置回送 CTS 流控 */
    private suspend fun sendFlowControl() {
        transport.send(
            IsoTpCodec.encodeFlowControl(
                status = IsoTpFlowStatus.CONTINUE,
                blockSize = config.blockSize,
                separationTimeMs = config.separationTimeMs,
                config = config,
            )
        )
    }
}

/** ISO-TP 拆包或流控错误 */
class IsoTpException(message: String) : IOException(message)
