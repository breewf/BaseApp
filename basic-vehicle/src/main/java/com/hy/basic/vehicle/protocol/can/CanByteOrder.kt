package com.hy.basic.vehicle.protocol.can

/**
 * DBC 字节序。
 *
 * 用法：构造 [CanSignalSpec] 时指定 byteOrder。
 * 注意：Intel 的 startBit 是最低位，Motorola 是最高位。
 */
enum class CanByteOrder {
    /** 小端:startBit 是最低位 */
    INTEL,

    /** 大端:startBit 是最高位 */
    MOTOROLA,
}
