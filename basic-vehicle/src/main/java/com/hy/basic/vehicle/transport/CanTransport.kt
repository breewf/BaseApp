package com.hy.basic.vehicle.transport

import com.hy.basic.vehicle.protocol.can.CanFrame
import java.io.Closeable
import kotlinx.coroutines.flow.SharedFlow

/**
 * CAN/CAN FD 收发抽象，屏蔽 SocketCAN、厂商 SDK、车机服务的差异。
 *
 * 用法：
 * `transport.frames.collect { frame -> ... }` 订阅接收，`transport.send(frame)` 发送。
 * 注意：frames 是热流，只收订阅之后的数据；实现方负责在 [close] 时停止读线程。
 */
interface CanTransport : Closeable {

    /** 收到的原始帧，不做 ISO-TP 重组和信号解码 */
    val frames: SharedFlow<CanFrame>

    /** 发送一帧；通道关闭或驱动写失败时抛异常 */
    suspend fun send(frame: CanFrame)

    /** 关闭通道并释放底层资源 */
    override fun close()
}
