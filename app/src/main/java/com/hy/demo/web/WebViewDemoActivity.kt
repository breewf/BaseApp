package com.hy.demo.web

import android.os.Bundle
import com.hy.basic.BaseViewModel
import com.hy.basic.dialog.showDialog
import com.hy.basic.hint.toast
import com.hy.basic.utils.appVersionCode
import com.hy.basic.utils.appVersionName
import com.hy.basic.utils.deviceModel
import com.hy.basic.utils.sdkVersionName
import com.hy.basic.web.BaseWebViewActivity
import com.hy.basic.web.callJs
import com.hy.basic.web.clearWebCache
import com.hy.myapp.R
import org.json.JSONObject

/**
 * desc: WebView 基类演示。默认打开 assets 里的本地页,不联网也能试 JS Bridge;
 * 进度条、SSL、错误页在页面里各留了一个链接。
 */
class WebViewDemoActivity : BaseWebViewActivity<BaseViewModel>() {

    /** 本地演示页,file:// 只读 assets,不影响真实 H5 页面 */
    override val initialUrl: String = "file:///android_asset/webview_demo.html"

    /** 启用 Bridge。本地页走 file://,接入真实 H5 时配 allowedHosts */
    override val jsBridgeEnabled: Boolean = true

    /** 留空时站外链接也在 WebView 内打开;填了域名则只内开白名单 */
    override val allowedHosts: Set<String> = emptySet()

    /** 自签名证书演示用,只放行这一个域名 */
    override val sslTolerantHosts: Set<String> = setOf("self-signed.badssl.com")

    override val toolbarTitle: CharSequence = "WebView 封装"

    /** 标题固定,不用 H5 的 title 覆盖 */
    override val useWebTitle: Boolean = false

    override fun onWebViewReady(savedInstanceState: Bundle?) {
        setupMenu()
        registerBridge()
    }

    private fun setupMenu() {
        val tb = toolbar ?: return
        tb.inflateMenu(R.menu.menu_web_view_demo)
        tb.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_reload -> {
                    reloadWeb()
                    true
                }
                R.id.action_push_event -> {
                    // 页面没加载完时 emit 会先缓存,加载完成后再补发
                    bridge?.emit("nativePush", "原生推送 @${System.currentTimeMillis() % 100000}")
                    true
                }
                R.id.action_call_js -> {
                    webView.callJs("showNativeCall", "来自原生的参数")
                    true
                }
                R.id.action_clear_cache -> {
                    webView.clearWebCache()
                    toast("缓存已清,重新加载才看得到效果")
                    true
                }
                else -> false
            }
        }
    }

    private fun registerBridge() {
        val bridge = bridge ?: return
        bridge.on("toast") { params, result ->
            toast(params.optString("text").ifBlank { "来自 JS 的 Toast" })
            result.success()
        }
        bridge.on("deviceInfo") { _, result ->
            result.success(
                JSONObject()
                    .put("model", deviceModel())
                    .put("system", "Android ${sdkVersionName()}")
                    .put("app", "${appVersionName()} (${appVersionCode()})")
                    .put("bridge", jsBridgeName),
            )
        }
        bridge.on("confirm") { params, result ->
            var answered = false
            showDialog {
                title = params.optString("title", "来自 JS 的确认")
                message = params.optString("message", "确定要继续吗?")
                confirm("确定") {
                    answered = true
                    result.success(true)
                }
                cancel("取消") {
                    answered = true
                    result.success(false)
                }
                // 返回键或点外部关掉也要给 JS 结果,不然 Promise 只能等超时
                onDismiss = { if (!answered) result.success(false) }
            }
        }
    }
}
