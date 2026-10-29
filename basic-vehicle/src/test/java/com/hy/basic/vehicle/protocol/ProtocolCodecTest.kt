package com.hy.basic.vehicle.protocol

import com.hy.basic.vehicle.protocol.can.CanByteOrder
import com.hy.basic.vehicle.protocol.can.CanFrame
import com.hy.basic.vehicle.protocol.can.CanSignalSpec
import com.hy.basic.vehicle.signal.SignalQuality
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

/**
 * desc: 协议工具的回归测试。
 *
 * 位域和 CRC 是最容易"看起来对、实际错一位"的地方,这里用固定报文和标准 check 值锁住行为。
 */
class ProtocolCodecTest {

    /** 同一 startBit 在两种字节序下取值不同 */
    @Test
    fun `Intel 与 Motorola 同一 startBit 结果不同`() {
        val data = "12 34 56 78 9A BC DE F0".hexToByteArray()

        assertEquals(0x412L, data.extractBits(0, 12))
        assertEquals(0x123L, data.extractBits(7, 12, motorola = true))
    }

    /** 有符号位域按长度补码 */
    @Test
    fun `有符号位域按长度补码`() {
        val data = byteArrayOf(0x80.toByte(), 0xF0.toByte())

        assertEquals(-128L, data.extractSignedBits(0, 8))
        assertEquals(0x80L, data.extractBits(0, 8))
    }

    /** hex 文本与 BCD 编解码往返 */
    @Test
    fun `十六进制与 BCD 往返`() {
        val data = "0x23, 0x59".hexToByteArray()

        assertEquals("23 59", data.toHexString())
        assertEquals(2359, data.bcdToIntOrNull())
        assertEquals(0x23.toByte(), 23.toBcdByte())
    }

    /** 用标准 check 值校验 CRC 参数 */
    @Test
    fun `校验与 CRC 标准 check 值`() {
        val check = "123456789".toByteArray(Charsets.US_ASCII)

        assertEquals(0x4B, check.crc8SaeJ1850())
        assertEquals(0x29B1, check.crc16Ccitt())
        assertEquals(0x4B37, check.crc16Modbus())
        assertEquals(0xCBF43926L, check.crc32())
    }

    /** 物理值写入后能按原信号解回 */
    @Test
    fun `CAN 信号写入后能解回原值`() {
        val speed = CanSignalSpec(
            name = "车速",
            startBit = 0,
            length = 16,
            factor = 0.1,
            min = 0.0,
            max = 250.0,
        )
        val torque = CanSignalSpec(
            name = "扭矩",
            startBit = 39,
            length = 12,
            byteOrder = CanByteOrder.MOTOROLA,
            signed = true,
            factor = 0.5,
            min = -500.0,
            max = 500.0,
        )

        var frame = CanFrame.standard(0x18F, ByteArray(8))
        frame = frame.write(speed, 60.5)
        frame = frame.write(torque, -120.0)

        assertEquals(60.5, frame.decode(speed).physical, 0.0001)
        assertEquals(-120.0, frame.decode(torque).physical, 0.0001)
    }

    /** 0xFFFF 哨兵值应判为无效 */
    @Test
    fun `无效哨兵值判定为 INVALID`() {
        val speed = CanSignalSpec(
            name = "车速",
            startBit = 0,
            length = 16,
            factor = 0.1,
            min = 0.0,
            max = 250.0,
            invalidRaw = 0xFFFF,
        )
        val frame = CanFrame.standard(0x18F, ByteArray(8))
            .writeRaw(speed, 0xFFFF)

        val value = frame.decode(speed)

        assertFalse(value.isValid)
        assertEquals(SignalQuality.INVALID, value.quality)
        assertEquals("车速: --", value.display())
    }

    /** 有符号信号按无符号哨兵写时也要能识别 */
    @Test
    fun `有符号信号的哨兵按位匹配`() {
        val temp = CanSignalSpec(
            name = "温度",
            startBit = 0,
            length = 8,
            signed = true,
            invalidRaw = 0xFF,
        )
        val frame = CanFrame.standard(0x100, byteArrayOf(0xFF.toByte()))

        assertEquals(SignalQuality.INVALID, frame.decode(temp).quality)
    }
}
