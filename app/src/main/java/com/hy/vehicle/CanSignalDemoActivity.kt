package com.hy.vehicle

import android.os.Bundle
import android.view.LayoutInflater
import com.hy.basic.BaseViewModel
import com.hy.basic.skin.BaseSkinActivity
import com.hy.basic.vehicle.protocol.can.CanByteOrder
import com.hy.basic.vehicle.protocol.can.CanFrame
import com.hy.basic.vehicle.protocol.can.CanSignalCodec
import com.hy.basic.vehicle.protocol.can.CanSignalSpec
import com.hy.myapp.databinding.ActivityCanSignalDemoBinding
import java.util.Locale

/**
 * desc: CAN 信号解析验证页。
 *
 * 用 8 字节仿真帧演示 Intel/Motorola 混排、factor/offset、枚举标签和无效值。
 * 第 2 帧故意让车速超物理上限,第 3 帧写 0xFFFF 哨兵值,用来确认无效判定生效。
 */
class CanSignalDemoActivity :
    BaseSkinActivity<ActivityCanSignalDemoBinding, BaseViewModel>() {

    /** 显示基类 Toolbar */
    override val showToolBar: Boolean get() = true

    /** Toolbar 标题 */
    override val toolbarTitle: CharSequence get() = "CAN 信号解析"

    /** 车速:0.1 km/h,16 位 Intel,0xFFFF 为无效值 */
    private val speedSpec = CanSignalSpec(
        name = "车速",
        startBit = 0,
        length = 16,
        byteOrder = CanByteOrder.INTEL,
        factor = 0.1,
        unit = "km/h",
        min = 0.0,
        max = 250.0,
        invalidRaw = 0xFFFF,
    )

    /** 档位:4 位枚举 */
    private val gearSpec = CanSignalSpec(
        name = "档位",
        startBit = 16,
        length = 4,
        valueLabels = mapOf(0L to "P", 1L to "R", 2L to "N", 3L to "D"),
    )

    /** 电机温度:8 位有符号,offset -40 */
    private val tempSpec = CanSignalSpec(
        name = "电机温度",
        startBit = 24,
        length = 8,
        signed = true,
        offset = -40.0,
        unit = "℃",
        min = -40.0,
        max = 150.0,
    )

    /** 扭矩:12 位 Motorola 有符号 */
    private val torqueSpec = CanSignalSpec(
        name = "扭矩",
        startBit = 39,
        length = 12,
        byteOrder = CanByteOrder.MOTOROLA,
        signed = true,
        factor = 0.5,
        unit = "N·m",
        min = -500.0,
        max = 500.0,
    )

    /** 本帧包含的信号 */
    private val specs = listOf(speedSpec, gearSpec, tempSpec, torqueSpec)

    /** 正常 / 超范围 / 无效三帧 */
    private val frames = listOf(
        buildFrame(60.5, 3.0, 35.0, 120.0),
        buildFrame(300.0, 3.0, 35.0, 120.0),
        buildFrame(0.0, 0.0, -40.0, 0.0).writeRaw(speedSpec, 0xFFFF),
    )

    /** 当前帧下标 */
    private var index = 0

    /** 加载布局 */
    override fun createBinding(inflater: LayoutInflater) =
        ActivityCanSignalDemoBinding.inflate(inflater)

    /** 绑定切换按钮并渲染首帧 */
    override fun initView(savedInstanceState: Bundle?) {
        binding.btnNextFrame.setOnClickListener {
            index = (index + 1) % frames.size
            render()
        }
        render()
    }

    /** 用物理值拼一帧仿真报文 */
    private fun buildFrame(
        speedKph: Double,
        gear: Double,
        temperature: Double,
        torque: Double,
    ): CanFrame {
        val data = ByteArray(8)
        CanSignalCodec.writeBits(data, speedSpec, CanSignalCodec.encodeRaw(speedSpec, speedKph))
        CanSignalCodec.writeBits(data, gearSpec, CanSignalCodec.encodeRaw(gearSpec, gear))
        CanSignalCodec.writeBits(data, tempSpec, CanSignalCodec.encodeRaw(tempSpec, temperature))
        CanSignalCodec.writeBits(data, torqueSpec, CanSignalCodec.encodeRaw(torqueSpec, torque))
        return CanFrame.standard(0x18F, data, timestampMs = System.currentTimeMillis())
    }

    /** 解码并刷新界面 */
    private fun render() {
        val frame = frames[index]
        binding.tvFrame.text = "ID 0x18F   长度 ${frame.byteCount}\n${frame.hex()}"

        binding.tvSignals.text = buildString {
            specs.forEach { spec ->
                val value = frame.decode(spec)
                appendLine(value.display(if (spec.length <= 4) 0 else 1))
                appendLine(
                    "  raw=0x${value.raw.toString(16).uppercase(Locale.US)}  " +
                        "quality=${value.quality}"
                )
            }
            appendLine()
            appendLine("第 1 帧正常;第 2 帧车速 300 超出上限;第 3 帧车速写 0xFFFF 无效哨兵。")
            appendLine("无效信号统一显示 --,别用 0 顶替。")
        }
    }
}
