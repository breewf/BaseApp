package com.hy.demo.event

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import com.hy.basic.BaseActivity
import com.hy.basic.event.EventBus
import com.hy.basic.event.observeEvent
import com.hy.myapp.databinding.ActivityEventBusDemoBinding

/**
 * desc: 全局事件总线演示主页。演示三类场景:一对多广播、Fragment 间通信、粘性跨页。
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
