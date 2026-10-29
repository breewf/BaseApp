package com.hy.basic.vehicle.signal

import com.hy.basic.vehicle.diagnostic.VehicleLog
import com.hy.basic.vehicle.protocol.can.CanFrame
import com.hy.basic.vehicle.protocol.can.CanSignalSpec
import com.hy.basic.vehicle.protocol.dbc.DbcDatabase
import com.hy.basic.vehicle.transport.CanTransport
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * CAN/DBC 信号源，把 [CanTransport] 的原始帧解码成通用信号流。
 *
 * 用法：
 * `CanSignalSource.fromDatabase("can", transport, db)` 后注册到 [VehicleSignalManager]。
 * 注意：key 默认包含报文 ID；多路复用信号当前按普通信号解码，复杂 DBC 需自行过滤。
 * 源只订阅 transport，不负责关闭它。
 */
class CanSignalSource(
    /** 数据源名 */
    override val name: String = "can",
    /** 原始 CAN 收发 */
    private val transport: CanTransport,
    /** 信号到 key 的映射 */
    bindings: List<CanSignalBinding>,
    /** 外部作用域；为空时源自己创建并在 close 时取消 */
    scope: CoroutineScope? = null,
) : SignalSource {

    /** 解码后的最新值 */
    private val values = MutableStateFlow<Map<String, SignalValue<*>>>(emptyMap())

    /** 按 key 索引 */
    private val byKey = bindings.associateBy { it.key }

    /** 按报文索引，收帧时只解匹配的信号 */
    private val byMessage = bindings.groupBy { it.messageId to it.extended }

    /** 最近一帧，写出时用来保留其它信号位 */
    private val lastFrames = ConcurrentHashMap<Pair<Int, Boolean>, CanFrame>()

    /** 数据源作用域 */
    private val sourceScope = scope ?: CoroutineScope(SupervisorJob() + Dispatchers.Default)

    /** 是否由本实例管理作用域 */
    private val ownsScope = scope == null

    /** 是否已关闭 */
    @Volatile
    private var closed = false

    init {
        require(byKey.size == bindings.size) { "CanSignalBinding 存在重复 key" }
        sourceScope.launch {
            try {
                transport.frames.collect { frame -> onFrame(frame) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                VehicleLog.e(TAG, "CAN 信号源订阅失败", e)
            }
        }
    }

    /** 订阅指定 CAN 信号 */
    override fun observe(key: String): Flow<SignalValue<*>> =
        values.mapNotNull { it[key] }.distinctUntilChanged()

    /** 最近一次解码值 */
    override fun latest(key: String): SignalValue<*>? = values.value[key]

    /** 写物理值；优先复用最近一帧，保留同报文其它信号 */
    override suspend fun write(key: String, value: Any): Boolean {
        if (closed) return false
        val binding = byKey[key] ?: return false
        val number = value as? Number ?: return false
        val base = lastFrames[binding.messageId to binding.extended]
            ?: CanFrame(
                id = binding.messageId,
                data = ByteArray(binding.dataLength),
                extended = binding.extended,
                fd = binding.fd,
            )
        val frame = try {
            base.write(binding.spec, number.toDouble())
                .copy(timestampMs = monotonicMs())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            VehicleLog.e(TAG, "CAN 信号编码失败: $key", e)
            return false
        }

        return try {
            transport.send(frame)
            true
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            VehicleLog.e(TAG, "CAN 信号写入失败: $key", e)
            false
        }
    }

    /** 停止订阅；transport 由调用方管理 */
    override fun close() {
        if (closed) return
        closed = true
        if (ownsScope) sourceScope.cancel()
    }

    /** 收到一帧后批量更新命中的信号 */
    private fun onFrame(frame: CanFrame) {
        if (closed) return
        val messageKey = frame.id to frame.extended
        val matched = byMessage[messageKey] ?: return
        lastFrames[messageKey] = frame

        val timestamp = frame.timestampMs.takeIf { it > 0 } ?: monotonicMs()
        val updates = HashMap<String, SignalValue<*>>(matched.size)
        for (binding in matched) {
            val value = try {
                val decoded = frame.decode(binding.spec)
                SignalValue(
                    value = if (decoded.isValid) decoded.physical else null,
                    quality = decoded.quality,
                    timestampMs = timestamp,
                    raw = decoded.raw,
                )
            } catch (e: Exception) {
                VehicleLog.w(TAG, "信号解码失败: ${binding.key}", e)
                SignalValue<Double>(null, SignalQuality.INVALID, timestamp)
            }
            updates[binding.key] = value
        }
        values.update { it + updates }
    }

    companion object {

        /** 日志 tag */
        private const val TAG = "CanSignalSource"

        /** 从 DBC 全量生成绑定；报文 ID 和信号名拼成 key */
        fun fromDatabase(
            name: String = "can",
            transport: CanTransport,
            database: DbcDatabase,
            keyPrefix: String = "can",
        ): CanSignalSource {
            val bindings = database.messages.flatMap { message ->
                message.signals.map { signal ->
                    CanSignalBinding(
                        key = CanSignalKeys.of(
                            messageId = message.id,
                            signalName = signal.name,
                            extended = message.extended,
                            prefix = keyPrefix,
                        ),
                        messageId = message.id,
                        extended = message.extended,
                        dataLength = if (message.dlc in 1..64) message.dlc else 8,
                        fd = message.dlc > 8,
                        spec = signal.spec,
                    )
                }
            }
            return CanSignalSource(name, transport, bindings)
        }
    }
}

/**
 * 一条 CAN 信号绑定。
 *
 * 用法：
 * 手工构造时把 DBC 里的信号描述放进 [spec]，key 建议用 [CanSignalKeys.of] 生成。
 */
data class CanSignalBinding(
    /** 管理器里的唯一 key */
    val key: String,
    /** 所属报文 ID，不含扩展帧标志 */
    val messageId: Int,
    /** 是否扩展帧 */
    val extended: Boolean = false,
    /** 报文数据长度，写出时用来构造空帧 */
    val dataLength: Int = 8,
    /** 是否 CAN FD */
    val fd: Boolean = false,
    /** 信号定义 */
    val spec: CanSignalSpec,
) {

    init {
        require(key.isNotBlank()) { "信号 key 不能为空" }
        require(dataLength in 1..(if (fd) 64 else 8)) { "dataLength 超出范围: $dataLength" }
    }
}

/** CAN 信号 key 的拼装规则，保证不同报文下的同名信号不冲突 */
object CanSignalKeys {

    /** 生成 `can:0x18F:Speed` 或 `can:x0x18F:Speed` 形式的 key */
    fun of(
        messageId: Int,
        signalName: String,
        extended: Boolean = false,
        prefix: String = "can",
    ): String {
        require(messageId >= 0) { "messageId 不能为负: $messageId" }
        require(signalName.isNotBlank()) { "信号名不能为空" }
        val idText = messageId.toString(16).uppercase(Locale.US)
        val extendedFlag = if (extended) "x" else ""
        return "$prefix:${extendedFlag}0x$idText:$signalName"
    }
}
