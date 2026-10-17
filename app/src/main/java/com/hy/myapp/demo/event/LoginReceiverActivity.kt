package com.hy.myapp.demo.event

import android.os.Bundle
import android.view.LayoutInflater
import com.hy.basic.BaseActivity
import com.hy.basic.event.EventBus
import com.hy.basic.event.observeStickyEvent
import com.hy.myapp.databinding.ActivityLoginReceiverBinding

/**
 * desc: 粘性事件补收演示页 —— 在主页 [EventBus.postSticky] 发 [LoginEvent] **之后**才被创建,
 * 用 [com.hy.basic.event.observeStickyEvent] 订阅仍能立即拿到最近一次登录事件。
 *
 * 这正是粘性通道相对实时通道(`observeEvent`)的价值:实时通道错过不补,
 * 而"进入任意页都应知道的全局状态"(登录态/主题/配置)用粘性可保证晚到的订阅者也拿得到。
 * 消费完点底部按钮 [EventBus.clearSticky] 结束这次粘性,避免后续页面重复补收。
 */
class LoginReceiverActivity :
    BaseActivity<ActivityLoginReceiverBinding, BusDemoViewModel>() {

    override val showToolBar: Boolean get() = true

    override val toolbarTitle: CharSequence get() = "子页 · 粘性补收"

    override fun createBinding(inflater: LayoutInflater) =
        ActivityLoginReceiverBinding.inflate(inflater)

    override fun initView(savedInstanceState: Bundle?) {
        binding.tvInfo.text = "等待粘性登录事件…(可返回主页先点\"发登录(粘性)\")"

        // 有缓存则订阅瞬间先补收一条,之后若有新的登录也持续接收
        observeStickyEvent<LoginEvent> { e ->
            binding.tvInfo.text = "补收到登录:\n用户 = ${e.userName}\ntoken = ${e.token}\n\n(本页创建晚于发送,靠粘性补收)"
        }

        binding.btnClear.setOnClickListener {
            EventBus.clearSticky(LoginEvent::class.java)
            showLongToast("已清除粘性登录,再进入本页将无补收")
        }
    }
}
