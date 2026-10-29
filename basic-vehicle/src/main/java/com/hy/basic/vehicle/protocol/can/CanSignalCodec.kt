package com.hy.basic.vehicle.protocol.can

import com.hy.basic.vehicle.protocol.extractBits
import com.hy.basic.vehicle.protocol.extractSignedBits
import com.hy.basic.vehicle.protocol.setBit
import com.hy.basic.vehicle.signal.SignalQuality
import kotlin.math.roundToLong

/**
 * desc: 按 [CanSignalSpec] 做信号编解码。
 *
 * 解码流程:提位域 -> 有符号补码 -> factor/offset 换算 -> 无效哨兵/范围判定。
 * 写入流程反过来,物理值先按 factor/offset 还原成 raw,再按位序写回字节。
 *
 * 注意:
 * - 写物理值时会把 raw 限制在信号长度能表示的范围内,避免越界写到旁边信号;
 * - 64 位无符号信号直接用 Long 承载时,最高位会被当成符号位,实际项目里很少见,
 *   真遇到就单独处理,不要依赖这里的物理值换算;
 * - 无效判定优先看 [CanSignalSpec.invalidRaw],再做物理范围判断,两者顺序不要反。
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
