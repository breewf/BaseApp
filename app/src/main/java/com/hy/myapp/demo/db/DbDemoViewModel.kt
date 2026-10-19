package com.hy.myapp.demo.db

import androidx.lifecycle.viewModelScope
import com.hy.basic.BaseViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * desc: 便签页 ViewModel。observeAll()/observeCount() 是 Room 的 Flow 查询,
 * 数据一变自动重发;转成 StateFlow 供 Activity 稳定收集。
 */
class DbDemoViewModel : BaseViewModel() {

    val notes: StateFlow<List<NoteEntity>> = NoteRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val count: StateFlow<Int> = NoteRepository.observeCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    fun add(title: String, content: String, priority: Priority) {
        viewModelScope.launch { NoteRepository.add(title, content, priority, tags = listOf("便签")) }
    }

    fun toggle(note: NoteEntity) {
        viewModelScope.launch { NoteRepository.toggle(note) }
    }

    /** 改:编辑已有便签的标题/内容/优先级,@Update 后 Flow 自动重发 */
    fun update(note: NoteEntity) {
        viewModelScope.launch { NoteRepository.update(note) }
    }

    fun remove(note: NoteEntity) {
        viewModelScope.launch { NoteRepository.remove(note) }
    }

    fun clear() {
        viewModelScope.launch { NoteRepository.clear() }
    }
}
