package com.hy.demo.hint

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.snackbar.Snackbar
import com.hy.basic.BaseActivity
import com.hy.basic.hint.ToastKit
import com.hy.basic.hint.snackbar
import com.hy.basic.hint.toast
import com.hy.basic.hint.toastLong
import com.hy.myapp.databinding.ActivityHintDemoBinding
import com.hy.demo.model.DemoLogViewModel
import kotlinx.coroutines.launch

/**
 * desc: 轻量提示用法示例。两个动词:toast/toastLong(说完就走) + snackbar(能带操作)。
 * 全局样式在 ToastKit.style 与 SnackbarKit 颜色字段改一次即可。
 */
class HintDemoActivity : BaseActivity<ActivityHintDemoBinding, DemoLogViewModel>() {

    override val showToolBar: Boolean get() = true

    override val toolbarTitle: CharSequence get() = "Toast + Snackbar"

    /** 给"删除 + 撤销"用的可回滚数据 */
    private val inbox = mutableListOf("周报提醒", "客户回访", "限时满减活动", "会议纪要")

    /** Toast 位置开关,演示 [ToastKit.style] 是全局生效的 */
    private var toastAtTop = false

    override fun createBinding(inflater: LayoutInflater) =
        ActivityHintDemoBinding.inflate(inflater)

    override fun initView(savedInstanceState: Bundle?) {
        viewModel.header = "操作日志：提示本身是瞬时的,这里记的是「触发了什么、用户点了什么」。"
        renderInbox()

        binding.btnToastShort.setOnClickListener { showShortToast() }
        binding.btnToastLong.setOnClickListener { showLongToast() }
        binding.btnToastBurst.setOnClickListener { showBurstToast() }
        binding.btnToastReplace.setOnClickListener { showReplaceToast() }
        binding.btnToastStyle.setOnClickListener { switchToastPosition() }
        binding.btnSnack.setOnClickListener { showPlainSnackbar() }
        binding.btnSnackUndo.setOnClickListener { deleteWithUndo() }
        binding.btnSnackIndefinite.setOnClickListener { showRetrySnackbar() }
        binding.btnClearLog.setOnClickListener { viewModel.clear() }
    }

    override fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.logText.collect { binding.tvLog.text = it }
            }
        }
    }

    /** 1. 最常用的短提示 */
    private fun showShortToast() {
        toast("保存成功")
        viewModel.log("toast(\"保存成功\")")
    }

    /** 2. 长时长提示(约 3.5 秒),带插值也没问题 */
    private fun showLongToast() {
        val count = inbox.size
        toastLong("当前还有 $count 条待办，后台导出已开始")
        viewModel.log("toastLong(…) 已弹出")
    }

    /** 3. 连续触发:只会看到最后一条,ToastUtils 每弹一条前先 cancel 掉上一条 */
    private fun showBurstToast() {
        repeat(3) { toast("连弹测试 ${it + 1}/3") }
        viewModel.log("连弹 3 条 → 前两条立即被顶掉,屏幕上只剩「3/3」")
    }

    /** 4. 进度型提示要"覆盖"而不是堆叠:先 cancel 再弹 */
    private fun showReplaceToast() {
        ToastKit.cancel()
        toast("正在下载… 60%")
        viewModel.log("ToastKit.cancel() + toast() → 覆盖式更新")
    }

    /** 5. 全局样式:换位置是改一次配置,不是改每个调用点 */
    private fun switchToastPosition() {
        toastAtTop = !toastAtTop
        ToastKit.style(
            gravity = if (toastAtTop) Gravity.TOP or Gravity.CENTER_HORIZONTAL
            else Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL,
            yOffsetDp = if (toastAtTop) 96 else 64,
        )
        toast(if (toastAtTop) "从现在起 Toast 出现在上方" else "Toast 回到底部")
        viewModel.log(
            "ToastKit.style(gravity=${if (toastAtTop) "TOP" else "BOTTOM"}) 已全站生效" +
                "(Android 11+ 系统会限制自定义位置)",
        )
    }

    /** 6. 不带操作的 Snackbar:比 Toast 更显眼,又不打断输入 */
    private fun showPlainSnackbar() {
        viewModel.log("snackbar(\"已同步 12 条数据\")")
        snackbar("已同步 12 条数据")
    }

    /** 7. 撤销型提示:先真删,再给用户反悔机会,点撤销回滚 */
    private fun deleteWithUndo() {
        if (inbox.isEmpty()) {
            toast("已经清空了，没什么可删的")
            viewModel.log("列表已空,先 toast 说明原因")
            return
        }
        val title = inbox.removeAt(0)
        renderInbox()
        viewModel.log("删除「$title」")
        snackbar("已删除「$title」", "撤销") {
            inbox.add(0, title)
            renderInbox()
            viewModel.log("点「撤销」→ 已恢复「$title」")
        }
    }

    /** 8. 不自动消失:必须让用户点一下才能关掉,适合"重试""去设置"这类必须表态的场景 */
    private fun showRetrySnackbar() {
        viewModel.log("模拟请求失败 → 弹一条必须表态的 Snackbar")
        snackbar("当前网络不可用", "重试", duration = Snackbar.LENGTH_INDEFINITE) {
            viewModel.log("用户点「重试」→ 重新发起请求")
            toast("已重新发起请求")
        }
    }

    private fun renderInbox() {
        binding.tvInbox.text = if (inbox.isEmpty()) {
            "待办列表：(空)"
        } else {
            "待办列表：" + inbox.joinToString("、")
        }
    }
}
