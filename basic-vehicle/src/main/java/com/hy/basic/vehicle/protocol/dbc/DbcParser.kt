package com.hy.basic.vehicle.protocol.dbc

import com.hy.basic.vehicle.protocol.can.CanByteOrder
import com.hy.basic.vehicle.protocol.can.CanSignalSpec
import java.io.File
import java.nio.charset.Charset

/**
 * desc: DBC 文本解析。
 *
 * 只处理车机常用的几类语句:BO_ / SG_ / VAL_ / CM_ / BA_。
 * EV_、ENVVAR_、SIG_VALTYPE_ 等复杂语法直接忽略,不影响已解析出的消息。
 *
 * 注意:
 * - DBC 的 @1 是 Intel(小端),@0 是 Motorola(大端),和帧里的字节序别混;
 * - 扩展帧 ID 的 bit31 是标志位,解析后会拆成 id + extended;
 * - 单行解析失败只记 [DbcDatabase.issues],不会中断整个文件。
 */
object DbcParser {

    // ==== DBC 语句正则 ====

    /** BO_ 报文行 */
    private val messageRegex = Regex("""^BO_\s+(\S+)\s+(\S+)\s*:\s*(\d+)\s+(\S+)\s*$""")

    /** SG_ 信号行 */
    private val signalRegex = Regex(
        "^SG_\\s+(\\S+)\\s*([Mm]\\d*M?)?\\s*:\\s*(\\d+)\\|(\\d+)@([01])([+-])\\s*" +
            "\\(\\s*([^,]+?)\\s*,\\s*([^)]+?)\\s*\\)\\s*\\[\\s*([^|]*?)\\s*\\|\\s*([^\\]]*?)\\s*\\]\\s*" +
            "\"([^\"]*)\"\\s*(.*)$",
    )

    /** 报文注释 */
    private val messageCommentRegex = Regex("""^CM_\s+BO_\s+(\S+)\s+"(.*)";?$""")

    /** 信号注释 */
    private val signalCommentRegex = Regex("""^CM_\s+SG_\s+(\S+)\s+(\S+)\s+"(.*)";?$""")

    /** VAL_ 枚举值描述 */
    private val valueDescriptionRegex = Regex("""^VAL_\s+(\S+)\s+(\S+)\s+(.*?);?$""")

    /** VAL_ 里的单个 raw -> 文本 */
    private val valuePairRegex = Regex("(-?\\d+)\\s+\"([^\"]*)\"")

    /** 报文周期 */
    private val cycleTimeRegex = Regex("""^BA_\s+"GenMsgCycleTime"\s+BO_\s+(\S+)\s+(\d+)\s*;?$""")

    /** 信号起始 raw 值 */
    private val startValueRegex = Regex("""^BA_\s+"GenSigStartValue"\s+SG_\s+(\S+)\s+(\S+)\s+(\S+)\s*;?$""")

    /** 解析 DBC 文本;问题行记录在结果里,不中断整体解析 */
    fun parse(text: String): DbcDatabase {
        val issues = mutableListOf<String>()
        val messages = LinkedHashMap<String, MutableMessage>()
        var current: MutableMessage? = null

        statements(text).forEach { line ->
            when {
                line.startsWith("BO_") -> {
                    val match = messageRegex.matchEntire(line)
                    if (match == null) {
                        issues += "BO_ 行格式不正确: $line"
                        current = null
                        return@forEach
                    }

                    val rawId = parseLong(match.groupValues[1])
                    if (rawId == null) {
                        issues += "报文 ID 无法解析: ${match.groupValues[1]}"
                        current = null
                        return@forEach
                    }

                    val id = (rawId and 0x7FFFFFFFL).toInt()
                    val extended = rawId and 0x80000000L != 0L
                    val dlc = match.groupValues[3].toIntOrNull()
                    if (dlc == null || dlc !in 0..64) {
                        issues += "报文 ${match.groupValues[2]} 的 DLC 非法: ${match.groupValues[3]}"
                    }

                    val message = MutableMessage(
                        id = id,
                        name = match.groupValues[2],
                        dlc = dlc ?: 0,
                        sender = match.groupValues[4],
                        extended = extended,
                    )
                    messages[keyOf(id, extended)] = message
                    current = message
                }

                line.startsWith("SG_") -> {
                    val message = current
                    if (message == null) {
                        issues += "SG_ 没有对应的 BO_: $line"
                    } else {
                        parseSignal(message, line, issues)
                    }
                }

                line.startsWith("CM_") -> parseComment(line, messages, issues)

                line.startsWith("VAL_") -> parseValueDescriptions(line, messages, issues)

                line.startsWith("BA_") -> parseAttribute(line, messages)
            }
        }

        return DbcDatabase(
            messages = messages.values.map { it.toImmutable() },
            issues = issues,
        )
    }

    /** 读文件解析;DBC 常见 UTF-8 或 GBK,按实际编码传 */
    fun parseFile(file: File, charset: Charset = Charsets.UTF_8): DbcDatabase =
        parse(file.readText(charset))

    /** 解析一条 SG_ 信号 */
    private fun parseSignal(
        message: MutableMessage,
        line: String,
        issues: MutableList<String>,
    ) {
        val match = signalRegex.matchEntire(line)
        if (match == null) {
            issues += "SG_ 行格式不正确: $line"
            return
        }

        val name = match.groupValues[1]
        val multiplexer = match.groupValues[2].takeIf { it.isNotBlank() }
        val startBit = match.groupValues[3].toIntOrNull()
        val length = match.groupValues[4].toIntOrNull()
        if (startBit == null || length == null || startBit < 0 || length !in 1..64) {
            issues += "信号 $name 的起始位/长度非法: ${match.groupValues[3]}|${match.groupValues[4]}"
            return
        }

        var factor = match.groupValues[7].trim().toDoubleOrNull() ?: 1.0
        if (factor == 0.0) {
            issues += "信号 $name 的 factor 为 0,按 1.0 处理"
            factor = 1.0
        }
        val offset = match.groupValues[8].trim().toDoubleOrNull() ?: 0.0

        var min = match.groupValues[9].trim().toDoubleOrNull() ?: Double.NEGATIVE_INFINITY
        var max = match.groupValues[10].trim().toDoubleOrNull() ?: Double.POSITIVE_INFINITY
        if (min == 0.0 && max == 0.0) {
            // DBC 里 [0|0] 通常表示未定义范围
            min = Double.NEGATIVE_INFINITY
            max = Double.POSITIVE_INFINITY
        } else if (min > max) {
            issues += "信号 $name 的范围 min>max,按不限制处理"
            min = Double.NEGATIVE_INFINITY
            max = Double.POSITIVE_INFINITY
        }

        val receivers = match.groupValues[12]
            .split(',')
            .map { it.trim() }
            .filter { it.isNotEmpty() && it != "Vector__XXX" }

        val spec = CanSignalSpec(
            name = name,
            startBit = startBit,
            length = length,
            byteOrder = if (match.groupValues[5] == "1") {
                CanByteOrder.INTEL
            } else {
                CanByteOrder.MOTOROLA
            },
            signed = match.groupValues[6] == "-",
            factor = factor,
            offset = offset,
            unit = match.groupValues[11],
            min = min,
            max = max,
            valueLabels = emptyMap(),
        )
        message.signals += MutableSignal(spec, multiplexer, receivers)
    }

    /** 解析 CM_ 注释 */
    private fun parseComment(
        line: String,
        messages: Map<String, MutableMessage>,
        issues: MutableList<String>,
    ) {
        signalCommentRegex.matchEntire(line)?.let { match ->
            val signal = findSignal(messages, match.groupValues[1], match.groupValues[2])
            if (signal == null) {
                issues += "CM_ 找不到信号: ${match.groupValues[1]} / ${match.groupValues[2]}"
            } else {
                signal.comment = match.groupValues[3]
            }
            return
        }

        messageCommentRegex.matchEntire(line)?.let { match ->
            val message = findMessage(messages, match.groupValues[1])
            if (message == null) {
                issues += "CM_ 找不到报文: ${match.groupValues[1]}"
            } else {
                message.comment = match.groupValues[2]
            }
            return
        }

        // 其它 CM_(节点/环境变量)暂不处理
    }

    /** 解析 VAL_ 枚举描述 */
    private fun parseValueDescriptions(
        line: String,
        messages: Map<String, MutableMessage>,
        issues: MutableList<String>,
    ) {
        val match = valueDescriptionRegex.matchEntire(line)
        if (match == null) {
            issues += "VAL_ 行格式不正确: $line"
            return
        }

        val signal = findSignal(messages, match.groupValues[1], match.groupValues[2])
        if (signal == null) {
            issues += "VAL_ 找不到信号: ${match.groupValues[1]} / ${match.groupValues[2]}"
            return
        }

        val labels = valuePairRegex.findAll(match.groupValues[3].trimEnd(';'))
            .mapNotNull { pair ->
                val raw = pair.groupValues[1].toLongOrNull() ?: return@mapNotNull null
                raw to pair.groupValues[2]
            }
            .toMap()
        if (labels.isEmpty()) {
            issues += "VAL_ 没有解析到枚举值: $line"
            return
        }

        signal.spec = signal.spec.copy(valueLabels = signal.spec.valueLabels + labels)
    }

    /** 解析 BA_ 周期/起始值 */
    private fun parseAttribute(line: String, messages: Map<String, MutableMessage>) {
        cycleTimeRegex.matchEntire(line)?.let { match ->
            findMessage(messages, match.groupValues[1])?.cycleTimeMs =
                match.groupValues[2].toIntOrNull()
            return
        }

        startValueRegex.matchEntire(line)?.let { match ->
            findSignal(messages, match.groupValues[1], match.groupValues[2])?.startValueRaw =
                parseLong(match.groupValues[3])
            return
        }

        // 其它 BA_ 属性暂不处理
    }

    /** 按报文 token 找信号 */
    private fun findSignal(
        messages: Map<String, MutableMessage>,
        messageToken: String,
        signalName: String,
    ): MutableSignal? =
        findMessage(messages, messageToken)?.signals?.firstOrNull { it.spec.name == signalName }

    /** 按 ID 或名称找报文 */
    private fun findMessage(
        messages: Map<String, MutableMessage>,
        token: String,
    ): MutableMessage? {
        val rawId = parseLong(token)
        if (rawId != null) {
            val id = (rawId and 0x7FFFFFFFL).toInt()
            val extended = rawId and 0x80000000L != 0L
            return messages[keyOf(id, extended)] ?: messages.values.firstOrNull { it.id == id }
        }
        return messages.values.firstOrNull { it.name == token }
    }

    /**
     * 把 DBC 拆成语句:
     * CM_ / VAL_ 可能跨行,统一累积到分号;其它语句一行一条。
     */
    private fun statements(text: String): List<String> {
        val result = mutableListOf<String>()
        val buffer = StringBuilder()

        fun flush() {
            if (buffer.isNotEmpty()) {
                result += buffer.toString()
                buffer.clear()
            }
        }

        text.lineSequence().forEach { raw ->
            val line = raw.trim().removePrefix("\uFEFF")
            if (line.isEmpty()) return@forEach

            // 上一条多行语句没写分号时,遇到新语句先收尾,避免把后续内容吞掉
            if (buffer.isNotEmpty() && !buffer.endsWith(";") && startsNewStatement(line)) {
                flush()
            }

            if (line.startsWith("CM_") || line.startsWith("VAL_")) {
                if (buffer.isNotEmpty()) buffer.append(' ')
                buffer.append(line)
                if (line.endsWith(";")) flush()
            } else {
                result += line
            }
        }
        flush()
        return result
    }

    /** 是否是新的 DBC 语句 */
    private fun startsNewStatement(line: String): Boolean =
        line.startsWith("BO_") ||
            line.startsWith("SG_") ||
            line.startsWith("CM_") ||
            line.startsWith("VAL_") ||
            line.startsWith("BA_")

    /** 解析十进制/0x 前缀整数 */
    private fun parseLong(token: String): Long? {
        val text = token.trim()
        return text.toLongOrNull()
            ?: text.removePrefix("0x").removePrefix("0X").toLongOrNull(16)
    }

    /** 报文查找 key */
    private fun keyOf(id: Int, extended: Boolean): String = "$extended:$id"

    /** 解析中的可变报文,最后统一转成 DbcMessage */
    private class MutableMessage(
        val id: Int,
        val name: String,
        val dlc: Int,
        val sender: String,
        val extended: Boolean,
    ) {
        val signals = mutableListOf<MutableSignal>()

        var cycleTimeMs: Int? = null

        var comment: String? = null

        /** 转成对外不可变模型 */
        fun toImmutable(): DbcMessage = DbcMessage(
            id = id,
            name = name,
            dlc = dlc,
            sender = sender,
            extended = extended,
            cycleTimeMs = cycleTimeMs,
            comment = comment,
            signals = signals.map {
                DbcSignal(
                    spec = it.spec,
                    multiplexer = it.multiplexer,
                    receivers = it.receivers,
                    comment = it.comment,
                    startValueRaw = it.startValueRaw,
                )
            },
        )
    }

    /** 解析中的可变信号 */
    private class MutableSignal(
        var spec: CanSignalSpec,
        val multiplexer: String?,
        val receivers: List<String>,
    ) {
        var comment: String? = null

        var startValueRaw: Long? = null
    }
}
