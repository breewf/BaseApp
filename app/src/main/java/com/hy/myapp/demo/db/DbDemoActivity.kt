package com.hy.myapp.demo.db

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.hy.basic.BaseActivity
import com.hy.myapp.R
import com.hy.myapp.databinding.ActivityDbDemoBinding
import kotlinx.coroutines.launch

/**
 * desc: Room 便签演示。上半区录入,下半区列表由 Flow 驱动 —— 增删改后 observeAll() 自动重发,
 * renderNotes() 重建行视图,无需手动刷新。
 */
class DbDemoActivity : BaseActivity<ActivityDbDemoBinding, DbDemoViewModel>() {

    override val showToolBar: Boolean get() = true
    override val toolbarTitle: CharSequence get() = "Room 数据库"

    private var priority = Priority.NORMAL

    /** 非空表示正在编辑这条;录入区标题按钮随之变“保存” */
    private var editing: NoteEntity? = null

    override fun createBinding(inflater: LayoutInflater) = ActivityDbDemoBinding.inflate(inflater)

    override fun initView(savedInstanceState: Bundle?) {
        binding.btnAdd.setOnClickListener { submit() }
        binding.btnClear.setOnClickListener { viewModel.clear() }
        binding.btnPriority.setOnClickListener { cyclePriority() }
        binding.btnEditCancel.setOnClickListener { resetForm() }
    }

    override fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.notes.collect(::renderNotes) }
                launch { viewModel.count.collect { binding.tvCount.text = "共 $it 条" } }
            }
        }
    }

    /** 录入区提交:editing 为空走新增,否则用当前输入覆盖原行(@Update) */
    private fun submit() {
        val title = binding.etTitle.text.toString().trim()
        if (title.isEmpty()) {
            showToast("标题不能为空")
            return
        }
        val content = binding.etContent.text.toString().trim()
        val current = editing
        if (current == null) {
            viewModel.add(title, content, priority)
        } else {
            viewModel.update(current.copy(title = title, content = content, priority = priority))
        }
        resetForm()
    }

    /** 点“编辑”:回填内容进录入区并切到保存态 */
    private fun startEdit(note: NoteEntity) {
        editing = note
        binding.etTitle.setText(note.title)
        binding.etContent.setText(note.content)
        priority = note.priority
        binding.btnPriority.text = "优先级：${labelOf(priority)}"
        binding.btnAdd.text = "保存"
        binding.btnEditCancel.visibility = View.VISIBLE
    }

    /** 回到新增态(保存后或取消编辑都走这里) */
    private fun resetForm() {
        editing = null
        priority = Priority.NORMAL
        binding.etTitle.text.clear()
        binding.etContent.text.clear()
        binding.btnPriority.text = "优先级：${labelOf(priority)}"
        binding.btnAdd.text = "新增"
        binding.btnEditCancel.visibility = View.GONE
    }

    private fun cyclePriority() {
        val values = Priority.entries.toTypedArray()
        priority = values[(priority.ordinal + 1) % values.size]
        binding.btnPriority.text = "优先级：${labelOf(priority)}"
    }

    /** Flow 每次发射重建列表:点行切完成,「编辑」改内容,「删除」移除 */
    private fun renderNotes(list: List<NoteEntity>) {
        binding.llNotes.removeAllViews()
        if (list.isEmpty()) {
            binding.llNotes.addView(plainText("还没有便签，输入标题点「新增」试试", R.color.color_999999))
            return
        }
        list.forEach { note -> binding.llNotes.addView(noteRow(note)) }
    }

    private fun noteRow(note: NoteEntity): LinearLayout {
        val summary = buildString {
            append(if (note.done) "☑ " else "☐ ")
            append(note.title)
            append("  · ${labelOf(note.priority)}")
            if (note.content.isNotEmpty()) append("\n${note.content}")
        }
        val label = plainText(summary, if (note.done) R.color.color_999999 else R.color.black_alpha)
            .apply {
                setOnClickListener { viewModel.toggle(note) }
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }
        val del = plainText("删除", R.color.colorPrimary).apply {
            gravity = Gravity.CENTER
            setPadding(dp(12), dp(6), dp(12), dp(6))
            setOnClickListener { viewModel.remove(note) }
        }
        val edit = plainText("编辑", R.color.colorPrimary).apply {
            gravity = Gravity.CENTER
            setPadding(dp(12), dp(6), dp(12), dp(6))
            setOnClickListener { startEdit(note) }
        }
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(10), 0, dp(10))
            addView(label)
            addView(edit)
            addView(del)
        }
    }

    private fun plainText(text: CharSequence, colorRes: Int): TextView = TextView(this).apply {
        this.text = text
        textSize = 15f
        setTextColor(ContextCompat.getColor(this@DbDemoActivity, colorRes))
    }

    private fun labelOf(p: Priority): String = when (p) {
        Priority.LOW -> "低"
        Priority.NORMAL -> "中"
        Priority.HIGH -> "高"
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()
}
