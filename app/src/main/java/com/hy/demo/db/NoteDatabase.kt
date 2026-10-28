package com.hy.demo.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.hy.basic.db.CommonConverters
import com.hy.basic.db.RoomMigrations

/**
 * desc: 演示数据库。version 与 [MIGRATION_1_2] 一起体现"改表 + 升版本 + 注册迁移"的标准套路。
 *
 * 单例双检锁:RoomDatabase 建库开销大,全App 一个实例。exportSchema=true 会把建表 SQL 落到
 * app/schemas,配合迁移做版本校验。
 */
@Database(
    entities = [NoteEntity::class],
    version = 2,
    exportSchema = true,
)
@TypeConverters(CommonConverters::class, PriorityConverter::class)
abstract class NoteDatabase : RoomDatabase() {

    abstract fun noteDao(): NoteDao

    companion object {

        private const val NAME = "demo_note.db"

        /** v1 没有 priority 列,v2 补上;真实升级继续往这里追加 sqlMigration 即可 */
        private val MIGRATION_1_2 = RoomMigrations.sqlMigration(
            1, 2,
            "ALTER TABLE note ADD COLUMN priority TEXT NOT NULL DEFAULT 'NORMAL'",
        )

        @Volatile
        private var instance: NoteDatabase? = null

        fun get(context: Context): NoteDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext, NoteDatabase::class.java, NAME,
            )
                .addMigrations(MIGRATION_1_2)
                // 降级时兜底重建(数据可丢的缓存表可用;业务主表宁可写迁移也别 destructive)
                .fallbackToDestructiveMigrationOnDowngrade(false)
                .build()
                .also { instance = it }
        }
    }
}
