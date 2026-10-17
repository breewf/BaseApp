package com.hy.myapp.demo.event

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import com.hy.basic.BaseActivity
import com.hy.basic.event.EventBus
import com.hy.basic.event.observeEvent
import com.hy.myapp.databinding.ActivityEventBusDemoBinding

/**
 * desc: 全局事件总线演示主页 —— 与 [BusLogFragment] 联动,演示三类典型通信场景。
 *
 * 覆盖:
 * 1. **一对多广播**:顶部/任一 Fragment 发 [CartEvent],另一 Fragment + 本 Activity 同时收到
 *    (替代 EventBus 那种"散落的注册/反注册",订阅随生命周期自动收放);
 * 2. **Fragment 间 / 父子通信**:左右两个 Fragment 无直接引用,靠总线解耦;
 * 3. **粘性跨页**:[EventBus.postSticky] 发 [LoginEvent] 后,再打开 [LoginReceiverActivity],
 *    晚创建的页面订阅时仍能立即补收到最近一次登录(发送在前、接收在后也不怕)。
 */
class EventBusDemoActivity :
    BaseActivity<ActivityEventBusDemoBinding, BusDemoViewModel>() {

    private var topCount = 0

    override val showToolBar: Boolean get() = true

    override val toolbarTitle: CharSequence get() = "全局事件总线"

    override fun createBinding(inflater: LayoutInflater) =
        ActivityEventBusDemoBinding.inflate(inflater)

    override fun initView(savedInstanceState: Bundle?) {
        // 只首次进入时挂两个订阅窗口,转屏/恢复复用 FragmentManager 已恢复的实例
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(binding.flLeft.id, BusLogFragment.newInstance("左侧 Fragment"))
                .replace(binding.flRight.id, BusLogFragment.newInstance("右侧 Fragment"))
                .commit()
        }

        // 打开总线流水日志,Logcat 过滤 tag=EventBus 可看到全部收发(演示用,定位完应关闭)
        EventBus.logEnabled = true

        binding.btnPostTop.setOnClickListener {
            topCount++
            EventBus.post(CartEvent(from = "页面顶部", skuName = "顶栏推荐位", count = topCount))
        }

        binding.btnLogin.setOnClickListener {
            EventBus.postSticky(
                LoginEvent(userName = "breewf", token = "tk-${System.currentTimeMillis()}"),
            )
            showLongToast("已发送粘性登录事件,点\"打开子页补收\"验证晚创建页面也能收到")
        }

        binding.btnOpen.setOnClickListener {
            startActivity(Intent(this, LoginReceiverActivity::class.java))
        }

        // 宿主自身订阅:任何来源的加购事件都在顶部留痕,直观验证"多处接收同一广播"
        observeEvent<CartEvent> { e ->
            hostLog("收到「${e.from}」加购:${e.skuName} ×${e.count}")
        }
    }

    private fun hostLog(line: String) {
        binding.tvHostLog.text = "$line\n(另见左右两栏同时打印)"
    }

    override fun onDestroy() {
        // 演示结束关掉日志,避免污染其他页面的 Logcat
        EventBus.logEnabled = false
        super.onDestroy()
    }
}
