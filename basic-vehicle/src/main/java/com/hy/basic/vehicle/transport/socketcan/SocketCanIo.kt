package com.hy.basic.vehicle.transport.socketcan

import android.system.Os
import java.io.Closeable
import java.io.FileDescriptor
import java.io.IOException

/**
 * SocketCAN 的原始读写接口，便于替换成 JNI、测试桩或厂商通道。
 *
 * 用法：
 * 实现 [read] / [write] 后交给 [SocketCanTransport]；Android 可直接用 [FileDescriptorSocketCanIo]。
 * 注意：read 是阻塞调用，close 需要能让阻塞中的 read 返回或抛异常。
 */
interface SocketCanIo : Closeable {

    /** 读一条 SocketCAN 报文，返回实际字节数；返回 0 表示暂时没有数据 */
    fun read(buffer: ByteArray): Int

    /** 写完整一条 SocketCAN 报文，不能只写一部分 */
    fun write(buffer: ByteArray)

    /** 关闭底层 socket */
    override fun close()
}

/**
 * 基于 [FileDescriptor] 的 SocketCAN IO。
 *
 * 用法：
 * `SocketCanTransport(FileDescriptorSocketCanIo(fd))`；fd 由 JNI 或厂商服务打开并 bind 到 canX。
 * 注意：类库不负责 socket(AF_CAN) 和 bind；CAN FD 还需宿主开启 CAN_RAW_FD_FRAMES。
 */
class FileDescriptorSocketCanIo(
    /** 已绑定到 CAN 接口的 socket fd */
    private val fd: FileDescriptor,
) : SocketCanIo {

    /** 阻塞读取一条报文 */
    override fun read(buffer: ByteArray): Int = Os.read(fd, buffer, 0, buffer.size)

    /** 循环写出，处理驱动只写一部分的情况 */
    override fun write(buffer: ByteArray) {
        var offset = 0
        while (offset < buffer.size) {
            val written = Os.write(fd, buffer, offset, buffer.size - offset)
            if (written <= 0) throw IOException("SocketCAN 写入失败: offset=$offset")
            offset += written
        }
    }

    /** 关闭 fd，阻塞中的 read 会随之返回错误 */
    override fun close() {
        Os.close(fd)
    }
}
