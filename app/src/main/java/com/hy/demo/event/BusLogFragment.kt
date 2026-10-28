package com.hy.demo.event

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hy.basic.BaseFragment
import com.hy.basic.event.EventBus
import com.hy.basic.event.observeEvent
import com.hy.myapp.databinding.FragmentBusLogBinding

/**
 * desc: 事件总线演示的“订阅侧”窗口,左右各一个实例,复用同一套逻辑。
 * 用 observeEvent 订阅 CartEvent,随 View 销毁自动取消。
 */
class BusLogFragment : BaseFragment<FragmentBusLogBinding, BusDemoViewModel>() {

    private val role: String
        get() = arguments?.getString(ARG_ROLE) ?: "Fragment"

    private var received = 0

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentBusLogBinding.inflate(inflater, container, false)

    override fun initView(savedInstanceState: Bundle?) {
        binding.tvRole.text = role
        appendLog("已订阅加购事件,等待广播…")

        // 发送:标记来源为本 Fragment 的角色名,方便对照另一侧的接收日志
        binding.btnSend.setOnClickListener {
            received++
            EventBus.post(CartEvent(from = role, skuName = "${role}选的商品", count = received))
        }

        // 订阅:挂在 viewLifecycleOwner,View 重建/销毁自动随之生效与取消
        viewLifecycleOwner.observeEvent<CartEvent> { e ->
            appendLog("收到「${e.from}」加购:${e.skuName} ×${e.count}")
        }
    }

    private fun appendLog(line: String) {
        binding.tvLog.append("$line\n")
        // 文本高度在 append 后才会重新测量,post 到下一帧布局完成再滚到底部
        binding.scroll.post {
            binding.scroll.fullScroll(View.FOCUS_DOWN)
        }
    }

    companion object {
        private const val ARG_ROLE = "arg_role"

        fun newInstance(role: String) = BusLogFragment().apply {
            arguments = Bundle().apply { putString(ARG_ROLE, role) }
        }
    }
}
