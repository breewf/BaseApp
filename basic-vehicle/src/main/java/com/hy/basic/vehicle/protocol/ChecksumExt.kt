@file:JvmName("VehicleChecksumExt")

package com.hy.basic.vehicle.protocol

import java.util.zip.CRC32

/**
 * desc: 报文校验常用的和校验、异或校验与 CRC。
 *
 * CRC 的参数(poly / init / xorOut)必须和协议文档完全一致,同名 CRC 在不同厂家
 * 的定义里结果可能不同,接新协议时先拿一组已知报文对一遍再上线。
 *
 * 校验范围默认是整个数组;如果校验字节本身也在数组里,调用时把 to 参数减 1,
 * 不要把校验位算进去。
 */

/** 累加和取低 8 位 */
fun ByteArray.checksumSum8(from: Int = 0, to: Int = size): Int {
    require(from in 0..size && to in from..size) { "校验范围非法: [$from, $to), 长度 $size" }
    var sum = 0
    for (i in from until to) sum += this[i].unsigned
    return sum and 0xFF
}

/** 逐字节异或 */
fun ByteArray.checksumXor8(from: Int = 0, to: Int = size): Int {
    require(from in 0..size && to in from..size) { "校验范围非法: [$from, $to), 长度 $size" }
    var value = 0
    for (i in from until to) value = value xor this[i].unsigned
    return value and 0xFF
}

/**
 * 非反射 CRC8。
 * 默认参数就是 SAE J1850(0x1D / 0xFF / 0xFF),AUTOSAR 的 H2F 用 0x2F / 0xFF / 0xFF。
 */
fun ByteArray.crc8(poly: Int = 0x1D, init: Int = 0xFF, xorOut: Int = 0xFF): Int {
    var crc = init and 0xFF
    for (byte in this) {
        crc = crc xor byte.unsigned
        repeat(8) {
            crc = if (crc and 0x80 != 0) {
                ((crc shl 1) xor poly) and 0xFF
            } else {
                (crc shl 1) and 0xFF
            }
        }
    }
    return (crc xor xorOut) and 0xFF
}

/** CRC8 SAE J1850:poly=0x1D, init=0xFF, xorOut=0xFF */
fun ByteArray.crc8SaeJ1850(): Int = crc8(0x1D, 0xFF, 0xFF)

/** CRC8 ATM:poly=0x07, init=0x00, xorOut=0x00 */
fun ByteArray.crc8Atm(): Int = crc8(0x07, 0x00, 0x00)

/** CRC16 CCITT(非反射):poly=0x1021,常用 init=0xFFFF */
fun ByteArray.crc16Ccitt(init: Int = 0xFFFF, xorOut: Int = 0): Int {
    var crc = init and 0xFFFF
    for (byte in this) {
        crc = crc xor (byte.unsigned shl 8)
        repeat(8) {
            crc = if (crc and 0x8000 != 0) {
                ((crc shl 1) xor 0x1021) and 0xFFFF
            } else {
                (crc shl 1) and 0xFFFF
            }
        }
    }
    return (crc xor xorOut) and 0xFFFF
}

/** CRC16 Modbus(反射):poly=0xA001, init=0xFFFF */
fun ByteArray.crc16Modbus(): Int {
    var crc = 0xFFFF
    for (byte in this) {
        crc = crc xor byte.unsigned
        repeat(8) {
            crc = if (crc and 1 != 0) (crc ushr 1) xor 0xA001 else crc ushr 1
        }
    }
    return crc and 0xFFFF
}

/** CRC32,标准 IEEE 多项式;返回 Long 方便和协议里的 8 位十六进制对齐 */
fun ByteArray.crc32(): Long {
    val crc = CRC32()
    crc.update(this)
    return crc.value
}

/**
 * 滚动计数器加一,到上限回到 0。E2E 保护里常和 CRC 一起用,
 * 上限一般是 0x0F 或 0xFF,按协议来。
 */
fun Int.nextRollingCounter(maxValue: Int = 0x0F): Int {
    require(maxValue > 0) { "滚动计数器上限必须大于 0: $maxValue" }
    return if (this >= maxValue) 0 else this + 1
}
