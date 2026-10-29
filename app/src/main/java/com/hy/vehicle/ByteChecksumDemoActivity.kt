package com.hy.vehicle

import android.os.Bundle
import android.view.LayoutInflater
import com.hy.basic.BaseViewModel
import com.hy.basic.skin.BaseSkinActivity
import com.hy.basic.vehicle.protocol.bcdToIntOrNull
import com.hy.basic.vehicle.protocol.checksumSum8
import com.hy.basic.vehicle.protocol.checksumXor8
import com.hy.basic.vehicle.protocol.crc16Ccitt
import com.hy.basic.vehicle.protocol.crc16Modbus
import com.hy.basic.vehicle.protocol.crc32
import com.hy.basic.vehicle.protocol.crc8SaeJ1850
import com.hy.basic.vehicle.protocol.extractBits
import com.hy.basic.vehicle.protocol.hexToByteArray
import com.hy.basic.vehicle.protocol.i8At
import com.hy.basic.vehicle.protocol.toHexString
import com.hy.basic.vehicle.protocol.u16At
import com.hy.basic.vehicle.protocol.u32At
import com.hy.basic.vehicle.protocol.u8At
import com.hy.myapp.databinding.ActivityByteChecksumDemoBinding
import java.util.Locale

/**
 * 字节/位域/校验示例页。
 *
 * 三组样例轮换，重点对比 Intel 与 Motorola 位序。
 */
class ByteChecksumDemoActivity :
    BaseSkinActivity<ActivityByteChecksumDemoBinding, BaseViewModel>() {

    /** 显示基类 Toolbar */
    override val showToolBar: Boolean get() = true

    /** Toolbar 标题 */
    override val toolbarTitle: CharSequence get() = "字节与校验"

    /** 三组样例帧,轮换着看不同字节组合 */
    private val samples = listOf(
        "02 1A 00 00 00 00 00 00",
        "12 34 56 78 9A BC DE F0",
        "AA 55 01 02 03 04 05 06",
    ).map { it.hexToByteArray() }

    /** 当前样例下标 */
    private var index = 0

    /** 加载布局 */
    override fun createBinding(inflater: LayoutInflater) =
        ActivityByteChecksumDemoBinding.inflate(inflater)

    /** 绑定按钮并渲染首组样例 */
    override fun initView(savedInstanceState: Bundle?) {
        binding.btnNextSample.setOnClickListener {
            index = (index + 1) % samples.size
            render()
        }
        render()
    }

    /** 计算并刷新当前样例的解析结果 */
    private fun render() {
        val data = samples[index]
        val intel = data.extractBits(0, 12)
        val motorola = data.extractBits(7, 12, motorola = true)
        val bcd = byteArrayOf(0x23, 0x59).bcdToIntOrNull()

        binding.tvSample.text = "样例 ${index + 1}/${samples.size}   ${data.toHexString()}"

        binding.tvResult.text = buildString {
            appendLine("u8[0]        ${data.u8At(0)}")
            appendLine("i8[1]        ${data.i8At(1)}")
            appendLine("u16 LE       ${data.u16At(0)}")
            appendLine("u16 BE       ${data.u16At(0, littleEndian = false)}")
            appendLine("u32 LE       0x${data.u32At(0).hex8()}")
            appendLine()
            appendLine("位域 start=0 len=12 Intel    -> 0x${intel.toString(16).uppercase(Locale.US)}")
            appendLine("位域 start=7 len=12 Motorola -> 0x${motorola.toString(16).uppercase(Locale.US)}")
            appendLine("同一 startBit 在两种字节序下含义不同")
            appendLine()
            appendLine("BCD 23 59    -> ${bcd ?: "非法"}")
            appendLine()
            appendLine("sum8          0x${data.checksumSum8().hex2()}")
            appendLine("xor8          0x${data.checksumXor8().hex2()}")
            appendLine("CRC8 SAE      0x${data.crc8SaeJ1850().hex2()}")
            appendLine("CRC16 CCITT   0x${data.crc16Ccitt().hex4()}")
            appendLine("CRC16 Modbus  0x${data.crc16Modbus().hex4()}")
            appendLine("CRC32         0x${data.crc32().hex8()}")
        }
    }

    /** 两位十六进制 */
    private fun Int.hex2(): String = toString(16).uppercase(Locale.US).padStart(2, '0')

    /** 四位十六进制 */
    private fun Int.hex4(): String = toString(16).uppercase(Locale.US).padStart(4, '0')

    /** 八位十六进制 */
    private fun Long.hex8(): String = toString(16).uppercase(Locale.US).padStart(8, '0')
}
