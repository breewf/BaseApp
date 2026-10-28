package com.hy.demo.db

import com.hy.myapp.App
import kotlinx.coroutines.flow.Flow

/**
 * desc: 便签数据仓库。对上层只暴露 Flow 与 suspend,DAO/建库细节都收在这里。
 * 建库用全局 Application 上下文,避免泄漏 Activity。
 */
object NoteRepository {

    private val dao get() = NoteDatabase.get(App.getInstance()).noteDao()

    fun observeAll(): Flow<List<NoteEntity>> = dao.observeAll()

    fun observeCount(): Flow<Int> = dao.observeCount()

    suspend fun add(title: String, content: String, priority: Priority, tags: List<String>): Long =
        dao.insert(NoteEntity(title = title, content = content, priority = priority, tags = tags))

    suspend fun toggle(note: NoteEntity) = dao.setDone(note.id, !note.done)

    /** 整行更新:走 BaseDao 的 @Update(按主键匹配),区别于 toggle 的 @Query 定点改 */
    suspend fun update(note: NoteEntity) = dao.update(note)

    suspend fun remove(note: NoteEntity) = dao.delete(note)

    suspend fun clear() = dao.deleteAll()

    suspend fun search(keyword: String): List<NoteEntity> = dao.search(keyword)
}
