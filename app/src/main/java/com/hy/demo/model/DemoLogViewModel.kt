package com.hy.demo.model

import com.hy.basic.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * desc: 演示页通用的"操作日志"ViewModel。
 *
 * 弹窗 / 提示类页面的结果本来就只给 UI 看,放 ViewModel 是为了跟其他 demo 页保持同一种写法
 * ([BaseViewModel] + StateFlow + `observeViewModel` 里 collect),新页面直接复用不重复造轮子。
 * 最新一条在最上面,最多保留 [MAX_LOG] 条。
 */
class DemoLogViewModel : BaseViewModel() {

    private val lines = ArrayDeque<String>()

    private val _logText = MutableStateFlow("")

    /** 日志区文本:说明行 + 最近若干条操作记录 */
    val logText: StateFlow<String> = _logText.asStateFlow()

    /** 页面说明行,进入页面时设置一次(不参与滚动记录) */
    var header: CharSequence = ""
        set(value) {
            field = value
            render()
        }

    /** 记一条操作结果 */
    fun log(message: CharSequence) {
        lines.addFirst("· $message")
        while (lines.size > MAX_LOG) lines.removeLast()
        render()
    }

    /** 清空记录,只留说明行 */
    fun clear() {
        lines.clear()
        render()
    }

    private fun render() {
        _logText.value = buildString {
            append(header)
            if (lines.isNotEmpty()) {
                append("\n")
                append(lines.joinToString("\n"))
            }
        }
    }

    private companion object {
        const val MAX_LOG = 8
    }
}
