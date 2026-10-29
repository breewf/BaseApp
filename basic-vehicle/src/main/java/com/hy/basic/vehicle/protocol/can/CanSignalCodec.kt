package com.hy.basic.vehicle.protocol.can

import com.hy.basic.vehicle.protocol.extractBits
import com.hy.basic.vehicle.protocol.extractSignedBits
import com.hy.basic.vehicle.protocol.setBit
import com.hy.basic.vehicle.signal.SignalQuality
import kotlin.math.roundToLong

/**
 * CAN 信号编解码：提位域、补码、factor/offset 换算、无效值判定。
 *
 * 用法：
 * `frame.decode(spec)` 解一条信号，`frame.write(spec, 60.5)` 写物理值并返回新帧。
 * 注意：写物理值会按信号长度限幅；64 位无符号信号不能直接用 Long 物理值换算。
 */
object CanSignalCodec {

    /** 解码一条信号 */
    fun decode(frame: CanFrame, spec: CanSignalSpec): CanSignalValue {
        val raw = if (spec.signed) {
            frame.data.extractSignedBits(spec.startBit, spec.length, spec.motorola)
        } else {
            frame.data.extractBits(spec.startBit, spec.length, spec.motorola)
        }
        val physical = raw * spec.factor + spec.offset

        val quality = when {
            matchesInvalid(raw, spec) -> SignalQuality.INVALID
            physical < spec.min || physical > spec.max -> SignalQuality.INVALID
            else -> SignalQuality.VALID
        }
        return CanSignalValue(
            spec = spec,
            raw = raw,
            physical = physical,
            quality = quality,
            timestampMs = frame.timestampMs,
        )
    }

    /** 物理值转 raw,超出信号长度范围时做限幅 */
    fun encodeRaw(spec: CanSignalSpec, physical: Double): Long {
        val raw = ((physical - spec.offset) / spec.factor).roundToLong()
        return raw.coerceIn(rawRange(spec))
    }

    /** 写入物理值,返回新的帧 */
    fun write(frame: CanFrame, spec: CanSignalSpec, physical: Double): CanFrame {
        val data = frame.data.copyOf()
        writeBits(data, spec, encodeRaw(spec, physical))
        return frame.copy(data = data)
    }

    /** 写入原始码值,主要用于构造无效值或仿真报文 */
    fun writeRaw(frame: CanFrame, spec: CanSignalSpec, raw: Long): CanFrame {
        val data = frame.data.copyOf()
        writeBits(data, spec, raw)
        return frame.copy(data = data)
    }

    /** 直接往字节数组写 raw,目标数组会被修改 */
    fun writeBits(data: ByteArray, spec: CanSignalSpec, raw: Long) {
        if (!spec.motorola) {
            for (i in 0 until spec.length) {
                data.setBit(spec.startBit + i, ((raw ushr i) and 1L) != 0L)
            }
            return
        }

        var bit = spec.startBit
        for (i in 0 until spec.length) {
            val shift = spec.length - 1 - i
            data.setBit(bit, ((raw ushr shift) and 1L) != 0L)
            bit += if (bit % 8 == 0) 15 else -1
        }
    }

    /** 信号长度能表示的 raw 范围 */
    private fun rawRange(spec: CanSignalSpec): LongRange {
        if (spec.length >= 64) return Long.MIN_VALUE..Long.MAX_VALUE
        return if (spec.signed) {
            val max = (1L shl (spec.length - 1)) - 1
            (-max - 1)..max
        } else {
            // 63 位时 1L shl 63 会溢出成负数,直接用 Long.MAX_VALUE
            if (spec.length == 63) 0L..Long.MAX_VALUE else 0L..<(1L shl spec.length)
        }
    }

    /**
     * 无效哨兵可能按无符号写(如 0xFFFF),而有符号信号解出来是 -1;
     * 先按原值比,再按位掩码比一次,两种写法都能识别。
     */
    private fun matchesInvalid(raw: Long, spec: CanSignalSpec): Boolean {
        val invalid = spec.invalidRaw ?: return false
        if (raw == invalid) return true
        if (spec.length >= 64) return false

        val mask = (1L shl spec.length) - 1
        return (raw and mask) == (invalid and mask)
    }
}
