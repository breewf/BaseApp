package com.hy.basic.vehicle.signal

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import com.hy.basic.vehicle.diagnostic.VehicleLog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.update

/**
 * 厂商广播信号源，把固定 action/extras 的广播转成通用信号流。
 *
 * 用法：
 * 用 [BroadcastSignalBinding] 声明 action 和取值键，构造后注册到 [VehicleSignalManager]。
 * 注意：默认按 exported 注册以接收系统/厂商广播；只收应用内广播时传 not-exported 标志。
 */
class BroadcastSignalSource(
    /** 数据源名 */
    override val name: String = "broadcast",
    /** 应用 Context */
    context: Context,
    /** 广播到信号 key 的映射 */
    bindings: List<BroadcastSignalBinding>,
    /** 注册标志，默认 [ContextCompat.RECEIVER_EXPORTED] */
    private val receiverFlags: Int = ContextCompat.RECEIVER_EXPORTED,
    /** 发送方权限；不需要时传 null */
    private val permission: String? = null,
) : SignalSource {

    /** 最新广播值 */
    private val values = MutableStateFlow<Map<String, SignalValue<*>>>(emptyMap())

    /** 按 key 索引 */
    private val byKey = bindings.associateBy { it.key }

    /** 同一个 action 可能对应多个信号 */
    private val byAction = bindings.groupBy { it.action }

    /** 保存 application context 供注册/反注册使用 */
    private val contextRef: Context = context.applicationContext

    /** 是否已关闭 */
    @Volatile
    private var closed = false

    /** 已注册的广播接收器 */
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (closed) return
            byAction[intent.action]?.forEach { binding ->
                publish(binding, intent)
            }
        }
    }

    /** 注册广播；action 为空直接报错，避免静默收不到数据 */
    init {
        require(byKey.size == bindings.size) { "BroadcastSignalBinding 存在重复 key" }
        require(byAction.isNotEmpty()) { "至少配置一个广播 action" }

        val filter = IntentFilter()
        byAction.keys.forEach { filter.addAction(it) }
        if (permission.isNullOrBlank()) {
            ContextCompat.registerReceiver(
                contextRef,
                receiver,
                filter,
                receiverFlags,
            )
        } else {
            ContextCompat.registerReceiver(
                contextRef,
                receiver,
                filter,
                permission,
                null,
                receiverFlags,
            )
        }
    }

    /** 订阅指定 key */
    override fun observe(key: String): Flow<SignalValue<*>> =
        values.mapNotNull { it[key] }.distinctUntilChanged()

    /** 最近一次广播值 */
    override fun latest(key: String): SignalValue<*>? = values.value[key]

    /** 广播源只读 */
    override suspend fun write(key: String, value: Any): Boolean = false

    /** 反注册接收器 */
    override fun close() {
        if (closed) return
        closed = true
        runCatching { contextRef.unregisterReceiver(receiver) }
    }

    /** 解析一条广播并写入 key */
    private fun publish(binding: BroadcastSignalBinding, intent: Intent) {
        if (closed) return
        val value = try {
            binding.converter?.invoke(intent)
                ?: binding.valueExtra?.let { intent.extras?.get(it) }
        } catch (e: Exception) {
            VehicleLog.w(TAG, "广播取值失败: ${binding.key}", e)
            null
        }

        val quality = if (value == null) {
            SignalQuality.INVALID
        } else {
            when (val raw = binding.qualityExtra?.let { intent.extras?.get(it) }) {
                is Boolean -> if (raw) SignalQuality.VALID else SignalQuality.INVALID
                is Number -> if (raw.toInt() != 0) SignalQuality.VALID else SignalQuality.INVALID
                else -> SignalQuality.VALID
            }
        }

        val timestamp = when (val raw = binding.timestampExtra?.let { intent.extras?.get(it) }) {
            is Number -> raw.toLong()
            else -> monotonicMs()
        }

        val signal = SignalValue(
            value = value,
            quality = quality,
            timestampMs = timestamp,
        )
        values.update { it + (binding.key to signal) }
    }

    companion object {

        /** 日志 tag */
        private const val TAG = "BroadcastSignalSource"
    }
}

/**
 * 一条广播信号绑定。
 *
 * 用法：
 * 取值键直接读 extras；格式特殊时传 [converter] 自己解析。
 * 注意：[valueExtra] 和 [converter] 至少要有一个。
 */
data class BroadcastSignalBinding(
    /** 管理器里的唯一 key */
    val key: String,
    /** 广播 action */
    val action: String,
    /** 值所在的 extra key */
    val valueExtra: String? = null,
    /** 质量所在的 extra key：Boolean 或 0/非 0 */
    val qualityExtra: String? = null,
    /** 时间戳所在的 extra key；不传则用当前单调时钟，传了需保持同一时钟基准 */
    val timestampExtra: String? = null,
    /** 自定义取值，适合一条广播里拼多个字段 */
    val converter: ((Intent) -> Any?)? = null,
) {

    init {
        require(key.isNotBlank()) { "信号 key 不能为空" }
        require(action.isNotBlank()) { "广播 action 不能为空" }
        require(valueExtra != null || converter != null) {
            "valueExtra 和 converter 至少要配置一个"
        }
    }
}
