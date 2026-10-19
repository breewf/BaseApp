package com.hy.myapp.demo.dialog

import android.os.Bundle
import android.view.LayoutInflater
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.hy.basic.BaseActivity
import com.hy.basic.dialog.showOn
import com.hy.myapp.databinding.ActivityDialogFragmentDemoBinding
import com.hy.myapp.demo.model.DemoLogViewModel
import kotlinx.coroutines.launch

/**
 * desc: DialogFragment 弹框基类用法示例。
 * 与 DialogDemoActivity(函数式弹窗)的分工:“问完就走”用 showDialog/showCustomDialog;
 * 内容复杂/要退场动画/要拦返回键继承 BaseDialogFragment;
 * 底部滑出可拖拽继承 BaseBottomSheetDialogFragment。
 * 弹出统一走 showOn 扩展,已经在弹/状态已保存都不会崩。
 */
class DialogFragmentDemoActivity : BaseActivity<ActivityDialogFragmentDemoBinding, DemoLogViewModel>() {

    override val showToolBar: Boolean get() = true

    override val toolbarTitle: CharSequence get() = "DialogFragment 弹框基类"

    override fun createBinding(inflater: LayoutInflater) =
        ActivityDialogFragmentDemoBinding.inflate(inflater)

    override fun initView(savedInstanceState: Bundle?) {
        viewModel.header = "操作日志：弹框内部的动作(滚动/拖拽/返回键拦截)也会写到这里 —— 弹框与页面共享同一个 ViewModel。"
        binding.btnFullscreen.setOnClickListener { showReaderDialog() }
        binding.btnBottomCard.setOnClickListener { showRateCardDialog() }
        binding.btnSheetHalf.setOnClickListener { showShareSheet(peekRatio = 0.5f, expanded = false, hideable = true) }
        binding.btnSheetFull.setOnClickListener {
            showShareSheet(
                peekRatio = 0f,
                expanded = true,
                hideable = false,
                draggable = false,
                fillHeight = true,
            )
        }
        binding.btnClearLog.setOnClickListener { viewModel.clear() }
    }

    override fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.logText.collect { binding.tvLog.text = it }
            }
        }
    }

    /** 1. 全屏弹框:没读到底按返回键会被拦下来 */
    private fun showReaderDialog() {
        ReaderDialogFragment.newInstance("用户协议（演示）").showOn(this)
    }

    /** 2. 贴底卡片:宽度铺满 + 底部滑入滑出,没选星级不放行返回键 */
    private fun showRateCardDialog() {
        RateCardDialogFragment().showOn(this)
    }

    /** 3、4. 同一个底部面板类,靠 arguments 摆出两套 behavior:半屏可拖 / 满屏钉住 */
    private fun showShareSheet(
        peekRatio: Float,
        expanded: Boolean,
        hideable: Boolean,
        draggable: Boolean = true,
        fillHeight: Boolean = false,
    ) {
        ShareSheetDialogFragment.newInstance(
            peekRatio = peekRatio,
            expanded = expanded,
            hideable = hideable,
            draggable = draggable,
            fillHeight = fillHeight,
        ).showOn(this)
    }
}
