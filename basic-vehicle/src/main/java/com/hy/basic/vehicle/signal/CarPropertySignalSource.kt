package com.hy.basic.vehicle.signal

import com.hy.basic.vehicle.diagnostic.VehicleLog
import java.io.Closeable
import java.util.concurrent.CopyOnWriteArrayList
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.update

/**
 * CarProperty 信号源，把车机属性回调转成通用信号流。
 *
 * 用法：
 * 宿主用 CarPropertyManager 实现 [CarPropertyClient]，再按属性 ID 配置 [CarPropertyBinding]。
 * 注意：模块不直接依赖 android.car，避免非车机工程编译失败；订阅和反注册由 client 负责。
 */
class CarPropertySignalSource(
    /** 数据源名 */
    override val name: String = "car",
    /** 宿主提供的 CarProperty 适配器 */
    private val client: CarPropertyClient,
    /** 属性到信号 key 的映射 */
    bindings: List<CarPropertyBinding>,
) : SignalSource {

    /** 最新的属性值 */
    private val values = MutableStateFlow<Map<String, SignalValue<*>>>(emptyMap())

    /** 按 key 索引 */
    private val byKey = bindings.associateBy { it.key }

    /** 同一属性可能绑定多个 key，只订阅一次 */
    private val byProperty = bindings.groupBy { it.propertyId to it.areaId }

    /** 已建立的属性订阅 */
    private val subscriptions = CopyOnWriteArrayList<Closeable>()

    /** 是否已关闭 */
    @Volatile
    private var closed = false

    init {
        require(byKey.size == bindings.size) { "CarPropertyBinding 存在重复 key" }

        byProperty.forEach { (property, targets) ->
            val (propertyId, areaId) = property
            client.get(propertyId, areaId)?.let { reading ->
                targets.forEach { publish(it, reading) }
            }
            val handle = client.subscribe(propertyId, areaId) { reading ->
                targets.forEach { publish(it, reading) }
            }
            handle?.let { subscriptions += it }
        }
    }

    /** 订阅指定属性 key */
    override fun observe(key: String): Flow<SignalValue<*>> =
        values.mapNotNull { it[key] }.distinctUntilChanged()

    /** 最近一次属性值 */
    override fun latest(key: String): SignalValue<*>? = values.value[key]

    /** 写属性；只读属性或宿主拒绝时返回 false */
    override suspend fun write(key: String, value: Any): Boolean {
        if (closed) return false
        val binding = byKey[key] ?: return false
        return try {
            client.set(binding.propertyId, binding.areaId, value)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            VehicleLog.e(TAG, "CarProperty 写入失败: $key", e)
            false
        }
    }

    /** 关闭全部属性订阅 */
    override fun close() {
        if (closed) return
        closed = true
        subscriptions.forEach { runCatching { it.close() } }
        subscriptions.clear()
    }

    /** 把一次属性回调写入对应 key */
    private fun publish(binding: CarPropertyBinding, reading: CarPropertyReading) {
        if (closed) return
        val quality = if (reading.value == null && reading.quality == SignalQuality.VALID) {
            SignalQuality.INVALID
        } else {
            reading.quality
        }
        val value = SignalValue(
            value = reading.value,
            quality = quality,
            timestampMs = reading.timestampMs,
            raw = reading.raw,
        )
        values.update { it + (binding.key to value) }
    }

    companion object {

        /** 日志 tag */
        private const val TAG = "CarPropertySource"
    }
}

/**
 * CarProperty 访问适配器。
 *
 * 用法：
 * 宿主用 `CarPropertyManager.registerCallback` 实现 [subscribe]，
 * 用 `getProperty` / `setProperty` 实现读写；回调可在 binder 线程触发。
 */
interface CarPropertyClient {

    /** 订阅属性变化；返回反注册句柄，没有订阅能力时返回 null */
    fun subscribe(
        propertyId: Int,
        areaId: Int,
        callback: (CarPropertyReading) -> Unit,
    ): Closeable?

    /** 当前属性值；拿不到返回 null */
    fun get(propertyId: Int, areaId: Int): CarPropertyReading?

    /** 写属性，成功返回 true */
    fun set(propertyId: Int, areaId: Int, value: Any): Boolean
}

/** 一次 CarProperty 读取结果 */
data class CarPropertyReading(
    /** 属性值，可能为 null(DELETE/不可用) */
    val value: Any?,
    /** 质量，默认有值即 VALID */
    val quality: SignalQuality = if (value == null) SignalQuality.INVALID else SignalQuality.VALID,
    /** 采样时间，默认取当前单调时钟；自定义时保持同一时钟基准 */
    val timestampMs: Long = monotonicMs(),
    /** 原始码值，没有就为 null */
    val raw: Long? = null,
)

/** 属性 ID 到信号 key 的绑定 */
data class CarPropertyBinding(
    /** 管理器里的唯一 key */
    val key: String,
    /** CarPropertyManager 属性 ID */
    val propertyId: Int,
    /** 属性区域 ID，全局属性用 0 */
    val areaId: Int = 0,
) {

    init {
        require(key.isNotBlank()) { "信号 key 不能为空" }
        require(propertyId >= 0) { "propertyId 不能为负: $propertyId" }
        require(areaId >= 0) { "areaId 不能为负: $areaId" }
    }
}
