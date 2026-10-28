package com.hy.basic.vehicle.protocol.can

/**
 * desc: DBC 里的字节序。
 *
 * Intel 小端:startBit 指向信号最低位,信号向高位序号延伸。
 * Motorola 大端:startBit 指向信号最高位,字节内往低位走,跨字节跳到下一字节 bit7。
 *
 * 这两种序不能只靠"大端/小端"四个字判断,接 DBC 时以 startBit 和字节序一起看。
 */
enum class CanByteOrder {
    /** 小端:startBit 是最低位 */
    INTEL,

    /** 大端:startBit 是最高位 */
    MOTOROLA,
}
