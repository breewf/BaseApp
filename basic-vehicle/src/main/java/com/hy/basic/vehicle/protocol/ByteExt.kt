@file:JvmName("VehicleByteExt")

package com.hy.basic.vehicle.protocol

import java.util.Locale

/**
 * 协议解析常用字节/位操作：hex、大小端、DBC 位域、BCD。
 *
 * 用法：
 * `data.extractBits(startBit, length, motorola)`、`data.setBit(bit, true)`、`data.toHexString()`。
 * 注意：位序号按 DBC 约定；Motorola 的 startBit 是信号最高位，Intel 是最低位。
 */

/** 无符号字节值(0..255)。负数字节直接 toInt 会得到负数 */
val Byte.unsigned: Int get() = toInt() and 0xFF

/** 按字节下标取无符号值,越界抛 [IllegalArgumentException] */
fun ByteArray.u8At(index: Int): Int {
    require(index in indices) { "字节下标越界: $index, 数据长度 ${size}" }
    return this[index].unsigned
}

/** 按字节下标取有符号值(-128..127) */
fun ByteArray.i8At(index: Int): Int {
    require(index in indices) { "字节下标越界: $index, 数据长度 ${size}" }
    return this[index].toInt()
}

/** 取 u16;默认小端,即低字节在前 */
fun ByteArray.u16At(index: Int, littleEndian: Boolean = true): Int {
    require(index >= 0 && index + 2 <= size) { "读取 u16 越界: index=$index, 数据长度 $size" }
    return if (littleEndian) {
        u8At(index) or (u8At(index + 1) shl 8)
    } else {
        (u8At(index) shl 8) or u8At(index + 1)
    }
}

/** 取 i16,符号位按 16 位补 */
fun ByteArray.i16At(index: Int, littleEndian: Boolean = true): Int {
    val raw = u16At(index, littleEndian)
    return if (raw and 0x8000 != 0) raw - 0x10000 else raw
}

/** 取 u32。返回值用 Long 装,避免最高位被当成符号位 */
fun ByteArray.u32At(index: Int, littleEndian: Boolean = true): Long {
    require(index >= 0 && index + 4 <= size) { "读取 u32 越界: index=$index, 数据长度 $size" }
    return if (littleEndian) {
        u8At(index).toLong() or
            (u8At(index + 1).toLong() shl 8) or
            (u8At(index + 2).toLong() shl 16) or
            (u8At(index + 3).toLong() shl 24)
    } else {
        (u8At(index).toLong() shl 24) or
            (u8At(index + 1).toLong() shl 16) or
            (u8At(index + 2).toLong() shl 8) or
            u8At(index + 3).toLong()
    }
}

/** 取某一位。bitIndex 从 0 开始,0 是第一个字节的最低位 */
fun ByteArray.bitAt(bitIndex: Int): Boolean {
    require(bitIndex >= 0) { "位下标不能为负: $bitIndex" }
    val byteIndex = bitIndex / 8
    require(byteIndex < size) { "位下标越界: $bitIndex, 数据长度 ${size * 8} 位" }
    return (this[byteIndex].toInt() ushr (bitIndex % 8)) and 1 == 1
}

/** 写某一位,只改这一位,其余位保持原样 */
fun ByteArray.setBit(bitIndex: Int, value: Boolean) {
    require(bitIndex >= 0) { "位下标不能为负: $bitIndex" }
    val byteIndex = bitIndex / 8
    require(byteIndex < size) { "位下标越界: $bitIndex, 数据长度 ${size * 8} 位" }
    val mask = 1 shl (bitIndex % 8)
    this[byteIndex] = if (value) {
        (this[byteIndex].toInt() or mask).toByte()
    } else {
        (this[byteIndex].toInt() and mask.inv()).toByte()
    }
}

/**
 * 按 DBC 规则提取位域。
 *
 * Intel(小端):startBit 是信号最低位,往后按位序号递增取 length 位。
 * Motorola(大端):startBit 是信号最高位,字节内往低位走,跨字节时跳到下一字节的 bit7。
 */
fun ByteArray.extractBits(startBit: Int, length: Int, motorola: Boolean = false): Long {
    require(startBit >= 0) { "startBit 不能为负: $startBit" }
    require(length in 1..64) { "信号长度需在 1..64: $length" }

    if (!motorola) {
        var value = 0L
        for (i in 0 until length) {
            if (bitAt(startBit + i)) value = value or (1L shl i)
        }
        return value
    }

    var value = 0L
    var bit = startBit
    repeat(length) {
        value = (value shl 1) or (if (bitAt(bit)) 1L else 0L)
        // 字节内走到最低位后,下一字节从 bit7 继续
        bit += if (bit % 8 == 0) 15 else -1
    }
    return value
}

/** 按 DBC 规则提取有符号位域,长度 64 时原样返回(已经是补码) */
fun ByteArray.extractSignedBits(startBit: Int, length: Int, motorola: Boolean = false): Long {
    val raw = extractBits(startBit, length, motorola)
    if (length >= 64) return raw
    val signBit = 1L shl (length - 1)
    return if (raw and signBit != 0L) raw - (1L shl length) else raw
}

/** 按小端写入 size 个字节,超出部分截断 */
fun Long.toBytesLe(size: Int): ByteArray {
    require(size in 1..8) { "字节数需在 1..8: $size" }
    return ByteArray(size) { i -> (this ushr (8 * i)).toByte() }
}

/** 按大端写入 size 个字节,超出部分截断 */
fun Long.toBytesBe(size: Int): ByteArray {
    require(size in 1..8) { "字节数需在 1..8: $size" }
    return ByteArray(size) { i -> (this ushr (8 * (size - 1 - i))).toByte() }
}

/** 字节数组转十六进制文本,默认空格分隔、大写 */
fun ByteArray.toHexString(separator: String = " ", upperCase: Boolean = true): String =
    joinToString(separator) { byte ->
        val text = byte.unsigned.toString(16).padStart(2, '0')
        if (upperCase) text.uppercase(Locale.US) else text
    }

/**
 * 十六进制文本转字节数组。
 * 允许 "0x12 34", "12,34", "1234" 这几种写法,但总长度必须成对。
 */
fun String.hexToByteArray(): ByteArray {
    val clean = replace("0x", "", ignoreCase = true)
        .replace(" ", "")
        .replace(",", "")
        .replace("-", "")
        .replace("\n", "")
        .replace("\t", "")

    require(clean.length % 2 == 0) { "十六进制长度必须是偶数: ${clean.length}" }
    require(clean.all { it in '0'..'9' || it.lowercaseChar() in 'a'..'f' }) {
        "包含非法十六进制字符: $this"
    }
    return ByteArray(clean.length / 2) { i ->
        clean.substring(i * 2, i * 2 + 2).toInt(16).toByte()
    }
}

/** 拼接多个字节数组,顺序为 当前 + 参数依次 */
fun ByteArray.concat(vararg others: ByteArray): ByteArray {
    val result = ByteArray(size + others.sumOf { it.size })
    copyInto(result)
    var offset = size
    others.forEach { other ->
        other.copyInto(result, offset)
        offset += other.size
    }
    return result
}

/** 0..99 转成单字节 BCD,例如 23 -> 0x23 */
fun Int.toBcdByte(): Byte {
    require(this in 0..99) { "BCD 单字节只支持 0..99: $this" }
    return (((this / 10) shl 4) or (this % 10)).toByte()
}

/**
 * BCD 转十进制。例如 `0x23 0x59` -> 2359。
 * 任意 nibble 超过 9 视为非法 BCD，返回 null。
 */
fun ByteArray.bcdToIntOrNull(): Int? {
    if (isEmpty()) return null
    var value = 0
    for (byte in this) {
        val high = (byte.toInt() ushr 4) and 0x0F
        val low = byte.toInt() and 0x0F
        if (high > 9 || low > 9) return null
        value = value * 100 + high * 10 + low
    }
    return value
}
