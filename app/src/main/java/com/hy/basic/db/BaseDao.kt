package com.hy.basic.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Update

/**
 * desc: DAO 基类。具体 DAO 写 `interface XxxDao : BaseDao<XxxEntity>` 就直接拿到增删改,
 * 查询/订阅按需在自己的 DAO 里补 @Query。
 *
 * Room 支持泛型 @Dao 基接口,只要子接口把实体类型填死。写操作一律 suspend,在 Room 自己的
 * 调度线程执行,不碰主线程。
 */
@Dao
interface BaseDao<T> {

    /** 单条插入,主键自增时返回新行 id */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: T): Long

    /** 批量插入,返回各行 id */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertList(entities: List<T>): List<Long>

    @Update
    suspend fun update(entity: T): Int

    @Update
    suspend fun updateList(entities: List<T>): Int

    @Delete
    suspend fun delete(entity: T): Int
}
