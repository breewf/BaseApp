package com.hy.basic.storage

import android.os.Parcelable
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tencent.mmkv.MMKV
import java.lang.reflect.Type
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * desc: MMKV 本地键值存储封装。App 启动时已 MMKV.initialize,直接用 [default] 或 [kv] 分库即可。
 *
 * 写入即时落盘,没有 SP 的 apply/commit 概念;取值一律带默认值。相比裸用 MMKV 补了三件事:
 * JSON 对象存取、属性委托(声明即读写)、多进程实例统一入口。
 */
object MMKVStore {

    @PublishedApi
    internal val gson = Gson()

    /** 默认库,普通全局配置放这 */
    val default: MMKV get() = MMKV.defaultMMKV()

    /**
     * 按业务分库。multiProcess=true 走多进程模式:跨进程读写才安全,
     * 但同一 mmapID 所有进程必须用相同模式,混用会触发 checkProcessMode 崩溃。
     */
    fun kv(mmapID: String, multiProcess: Boolean = false): MMKV =
        if (multiProcess) MMKV.mmkvWithID(mmapID, MMKV.MULTI_PROCESS_MODE)
        else MMKV.mmkvWithID(mmapID)

    // ==== 基础类型:put 立即生效,get 缺省回落默认值 ====

    fun putString(key: String, value: String?) = default.encode(key, value)
    fun putBoolean(key: String, value: Boolean) = default.encode(key, value)
    fun putInt(key: String, value: Int) = default.encode(key, value)
    fun putLong(key: String, value: Long) = default.encode(key, value)
    fun putFloat(key: String, value: Float) = default.encode(key, value)
    fun putStringSet(key: String, value: Set<String>?) = default.encode(key, value)
    fun putBytes(key: String, value: ByteArray?) = default.encode(key, value)

    fun getString(key: String, def: String = ""): String = default.decodeString(key, def) ?: def
    fun getBoolean(key: String, def: Boolean = false): Boolean = default.decodeBool(key, def)
    fun getInt(key: String, def: Int = 0): Int = default.decodeInt(key, def)
    fun getLong(key: String, def: Long = 0L): Long = default.decodeLong(key, def)
    fun getFloat(key: String, def: Float = 0f): Float = default.decodeFloat(key, def)
    fun getStringSet(key: String): Set<String> = default.decodeStringSet(key, emptySet()) ?: emptySet()
    fun getBytes(key: String): ByteArray? = default.decodeBytes(key)

    // ==== 对象 ====

    /** Parcelable 走 MMKV 原生 Parcel 序列化,比 JSON 快,但改类字段要注意兼容旧数据 */
    fun <T : Parcelable> putParcelable(key: String, value: T?) = default.encode(key, value)

    fun <T : Parcelable> getParcelable(key: String, clazz: Class<T>): T? =
        default.decodeParcelable(key, clazz)

    /** 任意对象转 JSON 存。列表/Map 这类泛型用带 [Type] 的重载配合 [typeOf] */
    fun putObject(key: String, value: Any?): Boolean =
        if (value == null) default.encode(key, null as String?) else default.encode(key, gson.toJson(value))

    fun <T : Any> getObject(key: String, clazz: Class<T>): T? =
        default.decodeString(key, null)?.let { gson.fromJson(it, clazz) }

    fun <T : Any> getObject(key: String, type: Type): T? =
        default.decodeString(key, null)?.let { gson.fromJson<T>(it, type) }

    // ==== 通用操作 ====

    fun contains(key: String): Boolean = default.contains(key)

    /** 计数器一步到位:读旧值 +delta 写回(主线程内安全,跨进程并发自增需自己加锁) */
    fun increase(key: String, delta: Int = 1): Int {
        val next = default.decodeInt(key, 0) + delta
        default.encode(key, next)
        return next
    }

    fun remove(key: String) = default.removeValueForKey(key)

    fun removeAll(keys: Array<String>) = default.removeValuesForKeys(keys)

    fun clear() = default.clearAll()

    fun allKeys(): Array<String> = default.allKeys() ?: emptyArray()

    // ==== 属性委托:var xxx by MMKVStore.xxxProp(...) 声明即读写,无需再手写 key ====

    fun stringProp(key: String, def: String = "") =
        MMKVValue(default, key, def, { k, d -> decodeString(k, d) ?: d }) { k, v -> encode(k, v) }

    fun booleanProp(key: String, def: Boolean = false) =
        MMKVValue(default, key, def, { k, d -> decodeBool(k, d) }) { k, v -> encode(k, v) }

    fun intProp(key: String, def: Int = 0) =
        MMKVValue(default, key, def, { k, d -> decodeInt(k, d) }) { k, v -> encode(k, v) }

    fun longProp(key: String, def: Long = 0L) =
        MMKVValue(default, key, def, { k, d -> decodeLong(k, d) }) { k, v -> encode(k, v) }

    fun floatProp(key: String, def: Float = 0f) =
        MMKVValue(default, key, def, { k, d -> decodeFloat(k, d) }) { k, v -> encode(k, v) }

    fun stringSetProp(key: String, def: Set<String> = emptySet()) =
        MMKVValue(default, key, def, { k, d -> decodeStringSet(k, d) ?: d }) { k, v -> encode(k, v) }

    /** 对象/集合走 JSON 存储的委托;List<User> 之类请用 jsonProp(key, default, typeOf<List<User>>()) */
    inline fun <reified T : Any> jsonProp(key: String, default: T): MMKVValue<T> =
        MMKVValue(
            kv = this.default,
            key = key,
            defaultValue = default,
            read = { k, d -> decodeString(k, null)?.let { gson.fromJson(it, T::class.java) } ?: d },
            write = { k, v -> encode(k, gson.toJson(v)) },
        )

    fun jsonProp(key: String, default: Set<String>, type: Type): MMKVValue<Set<String>> =
        MMKVValue(
            kv = this.default,
            key = key,
            defaultValue = default,
            read = { k, d -> decodeString(k, null)?.let { gson.fromJson<Set<String>>(it, type) } ?: d },
            write = { k, v -> encode(k, gson.toJson(v)) },
        )
}

/**
 * MMKV 属性委托。读写都直接打到指定的 [kv] 实例上,`by` 之后这个属性就是持久化字段。
 */
class MMKVValue<T>(
    private val kv: MMKV,
    private val key: String,
    private val defaultValue: T,
    private val read: MMKV.(String, T) -> T,
    private val write: MMKV.(String, T) -> Boolean,
) : ReadWriteProperty<Any?, T> {

    override fun getValue(thisRef: Any?, property: KProperty<*>): T = kv.read(key, defaultValue)

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        kv.write(key, value)
    }
}

/** 取泛型 Type,给 MMKVStore.getObject / jsonProp 存 List<T>、Map 这类结构用 */
inline fun <reified T> typeOf(): Type = object : TypeToken<T>() {}.type
