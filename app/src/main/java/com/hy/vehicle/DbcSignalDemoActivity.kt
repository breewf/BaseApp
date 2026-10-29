package com.hy.vehicle

import android.os.Bundle
import android.view.LayoutInflater
import com.hy.basic.BaseViewModel
import com.hy.basic.skin.BaseSkinActivity
import com.hy.basic.vehicle.protocol.can.CanFrame
import com.hy.basic.vehicle.protocol.dbc.DbcMessage
import com.hy.basic.vehicle.protocol.dbc.DbcParser
import com.hy.myapp.databinding.ActivityDbcSignalDemoBinding

/**
 * DBC 解析示例页。
 *
 * 按钮切换 DBC 组，只展示当前组的原文、解析结果和样例解码；点击结果区切换样例。
 */
class DbcSignalDemoActivity :
    BaseSkinActivity<ActivityDbcSignalDemoBinding, BaseViewModel>() {

    /** 显示基类 Toolbar */
    override val showToolBar: Boolean get() = true

    /** Toolbar 标题 */
    override val toolbarTitle: CharSequence get() = "DBC 解析"

    /** 多组 DBC 定义，解析结果按组切换展示 */
    private val dbcGroups = listOf(
        DbcGroup(
            name = "基础动力报文",
            note = "最简 BO_/SG_/BA_,对应基础字段",
            text = """
                BO_ 256 Powertrain: 8 ECU
                 SG_ Speed : 0|16@1+ (0.1,0) [0|250] "km/h" ECU
                 SG_ Gear : 16|4@1+ (1,0) [0|15] "" ECU
                 SG_ Torque : 39|12@0- (0.5,0) [-500|500] "Nm" ECU
                BA_ "GenMsgCycleTime" BO_ 256 20;
            """.trimIndent(),
        ),
        DbcGroup(
            name = "带注释和枚举",
            note = "VAL_ 枚举和 CM_ 注释会显示在信号定义里",
            text = """
                BO_ 256 Powertrain: 8 ECU
                 SG_ Speed : 0|16@1+ (0.1,0) [0|250] "km/h" ECU
                 SG_ Gear : 16|4@1+ (1,0) [0|15] "" ECU
                 SG_ Torque : 39|12@0- (0.5,0) [-500|500] "Nm" ECU
                VAL_ 256 Gear 0 "P" 1 "R" 2 "N" 3 "D" ;
                CM_ SG_ 256 Speed "整车车速";
                CM_ BO_ 256 "动力报文";
                BA_ "GenMsgCycleTime" BO_ 256 20;
            """.trimIndent(),
        ),
        DbcGroup(
            name = "扩展帧报文",
            note = "ID bit31 为 1,解析后 extended=true",
            messageId = 256,
            extended = true,
            text = """
                BO_ 2147483904 PowertrainExt: 8 ECU
                 SG_ Speed : 0|16@1+ (0.1,0) [0|250] "km/h" ECU
                 SG_ Gear : 16|4@1+ (1,0) [0|15] "" ECU
                 SG_ Torque : 39|12@0- (0.5,0) [-500|500] "Nm" ECU
                BA_ "GenMsgCycleTime" BO_ 256 20;
            """.trimIndent(),
        ),
        DbcGroup(
            name = "含问题行",
            note = "factor=0、min>max、长度非法，仍能解出报文",
            text = """
                BO_ 256 Powertrain: 8 ECU
                 SG_ Speed : 0|16@1+ (0.1,0) [0|250] "km/h" ECU
                 SG_ Gear : 16|4@1+ (0,0) [0|15] "" ECU
                 SG_ Torque : 39|12@0- (0.5,0) [500|-500] "Nm" ECU
                 SG_ BrokenSignal : 0|0@1+ (1,0) [0|0] "" ECU
                BA_ "GenMsgCycleTime" BO_ 256 20;
            """.trimIndent(),
        ),
    )

    /** 多组 DBC 只解析一次，切换 DBC 组时复用 */
    private val parsedGroups by lazy { dbcGroups.map { it to DbcParser.parse(it.text) } }

    /** 当前 DBC 组下标 */
    private var groupIndex = 0

    /** 当前样例下标 */
    private var sampleIndex = 0

    /** 多组仿真输入，点击结果区切换 */
    private val samples = listOf(
        Sample("正常巡航", speedKph = 60.5, gear = 3.0, torqueNm = 120.0),
        Sample("低速蠕行", speedKph = 8.2, gear = 3.0, torqueNm = 40.0),
        Sample("倒车", speedKph = 5.4, gear = 1.0, torqueNm = -30.0),
        Sample("高速超范围", speedKph = 300.0, gear = 3.0, torqueNm = 120.0),
        Sample("无效哨兵", speedKph = 0.0, gear = 0.0, torqueNm = 0.0, speedRawOverride = 0xFFFFL),
        Sample("大扭矩", speedKph = 80.0, gear = 3.0, torqueNm = 480.0),
    )

    /** 加载布局 */
    override fun createBinding(inflater: LayoutInflater) =
        ActivityDbcSignalDemoBinding.inflate(inflater)

    /** 绑定组切换和结果区点击 */
    override fun initView(savedInstanceState: Bundle?) {
        binding.btnDecode.setOnClickListener {
            groupIndex = (groupIndex + 1) % parsedGroups.size
            render()
        }
        binding.tvResult.setOnClickListener {
            sampleIndex = (sampleIndex + 1) % samples.size
            render()
        }
        render()
    }

    /** 只渲染当前 DBC 组和当前样例 */
    private fun render() {
        val (group, database) = parsedGroups[groupIndex]
        val sample = samples[sampleIndex]
        binding.btnDecode.text = "切换 DBC 组 (${groupIndex + 1}/${parsedGroups.size})"

        binding.tvResult.text = buildString {
            appendLine("【DBC 组 ${groupIndex + 1}/${parsedGroups.size}: ${group.name}】")
            if (group.note.isNotBlank()) appendLine("说明: ${group.note}")
            appendLine()

            appendLine("【DBC 原文】")
            appendLine(group.text)
            appendLine()

            appendLine("【解析结果】")
            val message = database.message(group.messageId, group.extended)
            if (message == null) {
                appendLine("未找到报文 id=0x${group.messageId.toString(16)}")
            } else {
                appendLine(definitionText(message))
                appendLine()

                appendLine("【当前样例 ${sampleIndex + 1}/${samples.size}: ${sample.name}】")
                appendLine("（点击结果区切换样例数据）")
                appendLine(
                    "输入: 车速 ${sample.speedKph} km/h, 档位 ${sample.gear.toInt()}, " +
                        "扭矩 ${sample.torqueNm} N·m"
                )
                sample.speedRawOverride?.let { raw ->
                    appendLine("车速 raw 强制写成 0x${raw.toString(16).uppercase()}")
                }

                val frame = buildFrame(message, sample)
                appendLine("帧: ${frame.hex()}")
                appendLine("解码结果:")
                message.decode(frame).forEach { value ->
                    appendLine(value.display(1))
                    appendLine(
                        "  raw=0x${value.raw.toString(16).uppercase()} " +
                            "quality=${value.quality}"
                    )
                }
            }
        }
    }

    /** 信号定义摘要，用于和 DBC 原文对照 */
    private fun definitionText(message: DbcMessage): String = buildString {
        appendLine(
            "报文 ${message.name}  id=0x${message.id.toString(16)}  " +
                "扩展帧=${message.extended}  DLC=${message.dlc}  " +
                "发送=${message.sender}  周期=${message.cycleTimeMs ?: "-"}ms"
        )
        if (!message.comment.isNullOrBlank()) {
            appendLine("报文备注: ${message.comment}")
        }
        message.signals.forEach { signal ->
            val spec = signal.spec
            val unit = spec.unit.ifBlank { "-" }
            val range = rangeText(spec.min, spec.max)
            appendLine(
                "${spec.name}: start=${spec.startBit} len=${spec.length} " +
                    "${spec.byteOrder} ${if (spec.signed) "signed" else "unsigned"}"
            )
            appendLine(
                "  factor=${spec.factor} offset=${spec.offset} unit=$unit range=$range"
            )
            signal.multiplexer?.let { appendLine("  复用=$it") }
            signal.startValueRaw?.let { appendLine("  起始raw=$it") }
            if (signal.receivers.isNotEmpty()) {
                appendLine("  接收=${signal.receivers.joinToString()}")
            }
            if (spec.valueLabels.isNotEmpty()) {
                val labels = spec.valueLabels.entries
                    .joinToString { "${it.key}=${it.value}" }
                appendLine("  labels=$labels")
            }
            if (!signal.comment.isNullOrBlank()) {
                appendLine("  备注=${signal.comment}")
            }
        }
    }

    /** 按样例写入信号,返回一帧仿真数据 */
    private fun buildFrame(message: DbcMessage, sample: Sample): CanFrame {
        var frame = message.frame(ByteArray(8))

        message.signal("Speed")?.let { signal ->
            frame = if (sample.speedRawOverride != null) {
                frame.writeRaw(signal.spec, sample.speedRawOverride)
            } else {
                frame.write(signal.spec, sample.speedKph)
            }
        }
        message.signal("Gear")?.let { signal ->
            frame = frame.write(signal.spec, sample.gear)
        }
        message.signal("Torque")?.let { signal ->
            frame = frame.write(signal.spec, sample.torqueNm)
        }
        return frame
    }

    /** 物理范围展示,两端都无限制时不显示 Infinity */
    private fun rangeText(min: Double, max: Double): String =
        if (min.isInfinite() && max.isInfinite()) "不限制" else "$min..$max"

    /** 一组 DBC 定义 */
    private data class DbcGroup(
        /** 组名 */
        val name: String,
        /** DBC 原文 */
        val text: String,
        /** 报文 ID,默认 256 */
        val messageId: Int = 256,
        /** 是否扩展帧 */
        val extended: Boolean = false,
        /** 组说明 */
        val note: String = "",
    )

    /** 一组仿真输入 */
    private data class Sample(
        /** 样例名 */
        val name: String,
        /** 车速(km/h) */
        val speedKph: Double,
        /** 档位 raw 值 */
        val gear: Double,
        /** 扭矩(N·m) */
        val torqueNm: Double,
        /** 覆盖车速 raw,用于构造无效哨兵 */
        val speedRawOverride: Long? = null,
    )
}
