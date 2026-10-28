package com.hy.demo.db

import androidx.room.Dao
import androidx.room.Query
import com.hy.basic.db.BaseDao
import kotlinx.coroutines.flow.Flow

/**
 * desc: 便签 DAO。增删改继承自 [BaseDao],这里只写查询与订阅。
 * 返回 [Flow] 的 @Query 会在表数据变化时自动重新发射,UI 无需手动刷新。
 */
@Dao
interface NoteDao : BaseDao<NoteEntity> {

    @Query("SELECT * FROM note ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<NoteEntity>>

    @Query("SELECT COUNT(*) FROM note")
    fun observeCount(): Flow<Int>

    @Query("SELECT * FROM note WHERE title LIKE '%' || :keyword || '%' ORDER BY createdAt DESC")
    suspend fun search(keyword: String): List<NoteEntity>

    @Query("UPDATE note SET done = :done WHERE id = :id")
    suspend fun setDone(id: Long, done: Boolean)

    @Query("DELETE FROM note")
    suspend fun deleteAll()
}
