package com.hy.demo.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import java.util.Date

/** 优先级:演示枚举列需要自定义 TypeConverter(Room 不内建枚举支持) */
enum class Priority { LOW, NORMAL, HIGH }

/**
 * desc: 便签实体。演示三类非常规列的落库方式:
 * - tags(List<String>) 走 [com.hy.basic.db.CommonConverters] 的 JSON 转换
 * - createdAt(Date) 走时间戳转换
 * - priority(枚举) 走本文件的 [PriorityConverter]
 */
@Entity(tableName = "note")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String = "",
    val done: Boolean = false,
    val tags: List<String> = emptyList(),
    val priority: Priority = Priority.NORMAL,
    val createdAt: Date = Date(),
)

object PriorityConverter {

    @TypeConverter
    fun fromPriority(priority: Priority): String = priority.name

    @TypeConverter
    fun toPriority(value: String): Priority =
        runCatching { Priority.valueOf(value) }.getOrDefault(Priority.NORMAL)
}
