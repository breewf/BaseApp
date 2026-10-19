package com.hy.basic.storage

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.google.gson.Gson
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * desc: SharedPreferences 扩展封装,MMKV 的备用方案(不想引三方库 / 只需极低频读写时用)。
 *
 * 写入统一收口到 edit{}(默认 apply 异步落盘,commit=true 才同步阻塞)。SP 的 getXxx 是磁盘读,
 * 别在主线程高频调;要 Flow 响应式持久化应上 DataStore,而不是在 SP 上轮询。
 */

private val spGson = Gson()

/** 按名字取独立 SP 文件;传 [defaultName] 走全局默认库 */
fun Context.sp(name: String = defaultSpName(context = this)): SharedPreferences =
    getSharedPreferences(name, Context.MODE_PRIVATE)

/** 与旧 PreferenceManager 默认行为对齐的文件名 */
fun defaultSpName(context: Context): String =
    context.packageName + "_preferences"

// ==== 读:全部带默认值,规避 getString 返回 null ====

fun SharedPreferences.str(key: String, def: String = ""): String = getString(key, def) ?: def
fun SharedPreferences.bool(key: String, def: Boolean = false): Boolean = getBoolean(key, def)
fun SharedPreferences.int(key: String, def: Int = 0): Int = getInt(key, def)
fun SharedPreferences.long(key: String, def: Long = 0L): Long = getLong(key, def)
fun SharedPreferences.float(key: String, def: Float = 0f): Float = getFloat(key, def)
fun SharedPreferences.strSet(key: String): Set<String> = getStringSet(key, emptySet()) ?: emptySet()

/** JSON 对象取回需要 Class;泛型集合请自行拿 Type 调 [spGson] */
fun <T : Any> SharedPreferences.getObject(key: String, clazz: Class<T>): T? =
    str(key, "").takeIf { it.isNotEmpty() }?.let { spGson.fromJson(it, clazz) }

// ==== 写:立即生效(apply 异步落盘);需同步返回时传 commit=true ====

fun SharedPreferences.putStr(key: String, value: String?, commit: Boolean = false) =
    edit(commit) { putString(key, value) }

fun SharedPreferences.putBool(key: String, value: Boolean, commit: Boolean = false) =
    edit(commit) { putBoolean(key, value) }

fun SharedPreferences.putInt(key: String, value: Int, commit: Boolean = false) =
    edit(commit) { putInt(key, value) }

fun SharedPreferences.putLong(key: String, value: Long, commit: Boolean = false) =
    edit(commit) { putLong(key, value) }

fun SharedPreferences.putFloat(key: String, value: Float, commit: Boolean = false) =
    edit(commit) { putFloat(key, value) }

fun SharedPreferences.putStrSet(key: String, value: Set<String>?, commit: Boolean = false) =
    edit(commit) { putStringSet(key, value) }

fun SharedPreferences.putObj(key: String, value: Any?, commit: Boolean = false) =
    edit(commit) { putString(key, spGson.toJson(value)) }

fun SharedPreferences.removeKey(key: String, commit: Boolean = false) =
    edit(commit) { remove(key) }

fun SharedPreferences.clearAll(commit: Boolean = false) = edit(commit) { clear() }

// ==== 属性委托:与 MMKVStore 对齐的写法,prefs 实例上直接 by ====

fun SharedPreferences.stringProp(key: String, def: String = "") =
    SPValue(this, key, def, { k, d -> getString(k, d) ?: d }) { k, v -> putString(k, v) }

fun SharedPreferences.booleanProp(key: String, def: Boolean = false) =
    SPValue(this, key, def, { k, d -> getBoolean(k, d) }) { k, v -> putBoolean(k, v) }

fun SharedPreferences.intProp(key: String, def: Int = 0) =
    SPValue(this, key, def, { k, d -> getInt(k, d) }) { k, v -> putInt(k, v) }

fun SharedPreferences.longProp(key: String, def: Long = 0L) =
    SPValue(this, key, def, { k, d -> getLong(k, d) }) { k, v -> putLong(k, v) }

fun SharedPreferences.floatProp(key: String, def: Float = 0f) =
    SPValue(this, key, def, { k, d -> getFloat(k, d) }) { k, v -> putFloat(k, v) }

fun SharedPreferences.stringSetProp(key: String, def: Set<String> = emptySet()) =
    SPValue(this, key, def, { k, d -> getStringSet(k, d) ?: d }) { k, v -> putStringSet(k, v) }

/**
 * SharedPreferences 属性委托。写内部用 apply 异步落盘;要同步落盘的场合改用上面的 putXxx(commit=true)。
 */
class SPValue<T>(
    private val prefs: SharedPreferences,
    private val key: String,
    private val defaultValue: T,
    private val read: SharedPreferences.(String, T) -> T,
    private val write: SharedPreferences.Editor.(String, T) -> SharedPreferences.Editor,
) : ReadWriteProperty<Any?, T> {

    override fun getValue(thisRef: Any?, property: KProperty<*>): T = prefs.read(key, defaultValue)

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        prefs.edit { write(key, value) }
    }
}
