package com.hy.basic.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * desc: 迁移助手。绝大多数版本升级只是几条 DDL,用 [sqlMigration] 把语句串起来即可,省掉匿名类样板。
 *
 * 迁移策略两档:
 * - 能保住数据 -> 写 [sqlMigration],注册进 databaseBuilder.addMigrations(...)。每跨一个版本都要给一条,
 *   缺中间某条 Room 会直接抛 IllegalStateException(而不是悄悄丢数据)。
 * - 数据可丢弃 -> 走 builder 的 fallbackToDestructiveMigration(...) 兜底重建。业务主表禁用,只给缓存表用。
 * 升级时务必同步把 @Database.version 调大,否则迁移不会触发。
 */
object RoomMigrations {

    /** 顺序执行 statements 的轻量迁移。startVersion/endVersion 必须与 @Database 版本对得上 */
    fun sqlMigration(startVersion: Int, endVersion: Int, vararg statements: String): Migration =
        object : Migration(startVersion, endVersion) {
            override fun migrate(db: SupportSQLiteDatabase) {
                statements.forEach(db::execSQL)
            }
        }
}
