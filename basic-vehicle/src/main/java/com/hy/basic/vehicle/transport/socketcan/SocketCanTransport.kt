package com.hy.basic.vehicle.transport.socketcan

import com.hy.basic.vehicle.diagnostic.VehicleLog
import com.hy.basic.vehicle.protocol.can.CanFrame
import com.hy.basic.vehicle.signal.monotonicMs
import com.hy.basic.vehicle.transport.CanTransport
import java.io.FileDescriptor
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * 基于 SocketCAN 原始 IO 的 CAN/CAN FD 传输。
 *
 * 用法：
 * `SocketCanTransport.fromFileDescriptor(fd)`，或自己实现 [SocketCanIo] 后传给构造器。
 * 注意：fd 需由宿主/JNI 打开并 bind；CAN FD 需开启 CAN_RAW_FD_FRAMES；[close] 会关闭 IO。
 */
class SocketCanTransport(
    /** SocketCAN 原始读写 */
    private val io: SocketCanIo,
    /** 读循环所在调度器 */
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : CanTransport {

    /** 读循环作用域 */
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)

    /** 写串行化，避免多协程同时写一个 fd */
    private val writeMutex = Mutex()

    /** 收帧缓冲；诊断/信号采集通常消费得过来，极端拥塞时丢最旧帧 */
    private val _frames = MutableSharedFlow<CanFrame>(
        extraBufferCapacity = 256,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    /** 对外暴露的收帧流 */
    override val frames: SharedFlow<CanFrame> = _frames.asSharedFlow()

    /** 是否已关闭 */
    @Volatile
    private var closed = false

    init {
        scope.launch { readLoop() }
    }

    /** 编码后写入 SocketCAN */
    override suspend fun send(frame: CanFrame) {
        check(!closed) { "SocketCAN 已关闭" }
        val bytes = SocketCanFrameCodec.encode(frame)
        writeMutex.withLock { io.write(bytes) }
    }

    /** 关闭 fd 并取消读循环 */
    override fun close() {
        if (closed) return
        closed = true
        scope.cancel()
        runCatching { io.close() }
    }

    /** 持续读取原始报文并解码成 [CanFrame] */
    private suspend fun readLoop() {
        val buffer = ByteArray(SocketCanFrameCodec.FD_FRAME_SIZE)
        try {
            while (scope.isActive && !closed) {
                val size = io.read(buffer)
                if (size <= 0) break
                val frame = SocketCanFrameCodec.decode(buffer, size) ?: continue
                _frames.emit(frame.copy(timestampMs = monotonicMs()))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            if (!closed) VehicleLog.e(TAG, "SocketCAN 读循环退出", e)
        }
    }

    companion object {

        /** 日志 tag */
        private const val TAG = "SocketCAN"

        /**
         * 包装一个已经 bind 到 canX 的 fd。
         *
         * fd 的所有权转交给 transport，close 时会一起关闭。
         */
        fun fromFileDescriptor(fd: FileDescriptor): SocketCanTransport =
            SocketCanTransport(FileDescriptorSocketCanIo(fd))
    }
}
