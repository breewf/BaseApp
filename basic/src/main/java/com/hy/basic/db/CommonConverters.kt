package com.hy.basic.db

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Date

/**
 * desc: 通用 TypeConverter。日期 <-> epochMillis、字符串集合 <-> JSON。
 * 数据库上挂 `@TypeConverters(CommonConverters::class)` 即可全局复用;实体用枚举再单独补一个转换器。
 */
object CommonConverters {

    private val gson = Gson()

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? = date?.time

    @TypeConverter
    fun timestampToDate(value: Long?): Date? = value?.let { Date(it) }

    @TypeConverter
    fun stringListToJson(list: List<String>?): String = gson.toJson(list)

    @TypeConverter
    fun jsonToStringList(json: String?): List<String> =
        json?.let { gson.fromJson<List<String>>(it, object : TypeToken<List<String>>() {}.type) }
            ?: emptyList()
}
