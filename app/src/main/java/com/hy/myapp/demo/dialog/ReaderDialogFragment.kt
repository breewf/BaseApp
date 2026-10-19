package com.hy.myapp.demo.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.hy.basic.dialog.BaseDialogFragment
import com.hy.basic.hint.ToastKit
import com.hy.myapp.databinding.DialogReaderFullscreenBinding
import com.hy.myapp.demo.model.DemoLogViewModel

/**
 * desc: 弹框示例
 *
 * 演示三件函数式弹窗([com.hy.basic.dialog.showCustomDialog])做不到的事:
 * 1. **占满整屏**:窗口不悬浮,标题栏/正文/底部按钮全由本布局排版,还带 edge-to-edge 系统栏适配;
 * 2. **返回键拦截**:[BaseDialogFragment.onBackPressed] 在"没读到底"时返回 true,弹框不关,
 *    并给 Toast + 宿主日志各一条(同一条路可以拿去做"再按一次退出");
 * 3. **内部交互 + 与宿主共享数据**:ScrollView 滚动监听解禁「同意」按钮,
 *    操作结果写进宿主的 [DemoLogViewModel](Activity 作用域,弹框和外页看同一份日志)。
 *
 * 进出场动画用基类缺省那套(整页从底部推上来,放在**窗口层**):
 * 全屏弹框想要淡入淡出时,过渡期露出来的是底下的浅色页面,看着就像“先闪一下白底再出内容”。
 * 真需要淡入淡出,重写 [BaseDialogFragment.animStyleRes] = R.style.AbsDialogAnimFade 即可;
 * 关闭走 [BaseDialogFragment.dismissWithAnimation],退场动画由系统在移除窗口时播完。
 */
class ReaderDialogFragment : BaseDialogFragment<DialogReaderFullscreenBinding>() {

    override val isFullscreen: Boolean get() = false

    /** 强制阅读:点外部/返回键都不能直接关掉,只能走按钮 */
    override val cancelOnTouchOutside: Boolean get() = false

    /** 与宿主 Activity 共享同一个 DemoLogViewModel(ViewModelStore 按类名取,拿到的是同一实例) */
    private val hostLog: DemoLogViewModel by lazy {
        ViewModelProvider(requireActivity())[DemoLogViewModel::class.java]
    }

    /** 是否已读到底:决定「同意」按钮可点、返回键是否放行 */
    private var readFinished = false

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?) =
        DialogReaderFullscreenBinding.inflate(inflater)

    override fun initView(savedInstanceState: Bundle?) {
        binding.tvTitle.text = arguments?.getString(ARG_TITLE) ?: "用户协议"
        binding.btnClose.setOnClickListener { dismissWithAnimation() }
        binding.btnReject.setOnClickListener {
            hostLog.log("全屏弹框 · 不同意,已关闭")
            dismissWithAnimation()
        }
        binding.btnAgree.setOnClickListener {
            hostLog.log("全屏弹框 · 已同意并继续")
            dismissWithAnimation()
        }
    }

    override fun initData() {
        binding.btnAgree.isEnabled = false
        binding.tvContent.text = mockAgreement()
        // 滚到最底部才解禁:canScrollVertically(1) == false 表示"再往下滚不动了"
        binding.scrollContent.setOnScrollChangeListener { _, _, _, _, _ ->
            if (!binding.scrollContent.canScrollVertically(1)) unlockAgree()
        }
        hostLog.log("全屏弹框 · 已弹出(试着按返回键)")
    }

    /**
     * 没读到底就按返回键 -> 返回 true 消费掉这次返回,弹框保持打开;
     * 读到底之后返回 false,交回基类按 cancelable 走带退场动画的关闭。
     */
    override fun onBackPressed(): Boolean {
        if (readFinished) return false
        ToastKit.show("请先阅读到底部")
        hostLog.log("全屏弹框 · 返回键被拦截(还没读到底)")
        return true
    }

    override fun onDialogDismiss() {
        hostLog.log("全屏弹框 · 已关闭(退场动画播完)")
    }

    private fun unlockAgree() {
        if (readFinished) return
        readFinished = true
        binding.btnAgree.isEnabled = true
        binding.tvHint.text = "已阅读到底部,可以同意了"
        hostLog.log("全屏弹框 · 已读到底,「同意」解禁")
    }

    /** 拼一段足够长的正文,把内容撑过一屏好验证滚动监听 */
    private fun mockAgreement(): CharSequence = buildString {
        append("以下内容由代码拼接,只为把正文撑到超过一屏。\n\n")
        for (index in 1..20) {
            append("第 ").append(index).append(" 条 演示占位条款:本机仅将必要信息用于界面展示与调试,不会上传,卸载后本地数据一并清除。\n")
        }
        append("\n—— 正文结束,继续往下已到底 ——")
    }

    companion object {
        private const val ARG_TITLE = "arg_title"

        /** 参数一律走 arguments:进程被杀重建后还能还原出标题 */
        fun newInstance(title: String = "用户协议"): ReaderDialogFragment =
            ReaderDialogFragment().apply {
                arguments = Bundle().apply { putString(ARG_TITLE, title) }
            }
    }
}
