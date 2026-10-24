@file:JvmName("WebViewExt")

package com.hy.basic.web

import android.annotation.SuppressLint
import android.net.Uri
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebView
import com.hy.basic.utils.isAppDebugBuild
import org.json.JSONObject

/**
 * desc: WebView 常用设置与操作。
 *
 * 设置集中在 [setupDefaultSettings],销毁走 [destroySafely]。
 * 加载失败时如果用 loadData 写错误页,会多一条历史记录,返回键容易乱;
 * 错误提示可以用原生 View 盖在 WebView 上(见 [BaseWebViewActivity])。
 */

/** 一次性配好常用开关。file:// 只开放 assets/res,不访问本地文件系统 */
@SuppressLint("SetJavaScriptEnabled")
fun WebView.setupDefaultSettings(
    javaScriptEnabled: Boolean = true,
    domStorageEnabled: Boolean = true,
    supportZoom: Boolean = false,
    cacheMode: Int = WebSettings.LOAD_DEFAULT,
    mixedContentMode: Int = WebSettings.MIXED_CONTENT_NEVER_ALLOW,
    userAgentSuffix: String? = null,
): WebView {
    val config = settings
    config.javaScriptEnabled = javaScriptEnabled
    // 现代 H5 都靠 localStorage,不开会出现"登录了刷新就掉"
    config.domStorageEnabled = domStorageEnabled
    config.cacheMode = cacheMode
    // https 页面里的 http 资源默认拦截,这是 targetSdk 21+ 的行为
    config.mixedContentMode = mixedContentMode
    config.setSupportZoom(supportZoom)
    config.builtInZoomControls = supportZoom
    config.displayZoomControls = false
    config.useWideViewPort = true
    config.loadWithOverviewMode = true
    // 关闭本地文件系统访问;assets 和 res 仍可通过 file:///android_asset 加载
    config.allowFileAccess = false
    config.allowContentAccess = true
    config.allowFileAccessFromFileURLs = false
    // 这两个开关会允许 file 页面跨域读取本地文件,默认保持关闭
    config.allowUniversalAccessFromFileURLs = false
    // 第三方 Cookie 需要单独打开,否则不少 H5 的登录态会丢
    CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
    userAgentSuffix?.takeIf { it.isNotBlank() }?.let {
        config.userAgentString = "${config.userAgentString} $it"
    }
    // 调试开关跟随 build 类型,release 下关闭
    WebView.setWebContentsDebuggingEnabled(context.isAppDebugBuild())
    return this
}

/** 加载地址:自动补协议;失败返回 false,不抛异常 */
fun WebView.loadUrlSafely(url: String?, headers: Map<String, String>? = null): Boolean {
    val target = url.toWebUrl() ?: return false
    return runCatching {
        if (headers.isNullOrEmpty()) loadUrl(target) else loadUrl(target, headers)
        true
    }.getOrDefault(false)
}

/**
 * 补全手输地址:"www.qq.com" -> "https://www.qq.com"。
 * 返回 null 表示不是网页地址,tel: / mailto: / intent:// 由调用方自己处理。
 */
fun String?.toWebUrl(): String? {
    val raw = this?.trim().orEmpty()
    if (raw.isEmpty()) return null
    // 先识别 host:port,避免被下面的 scheme 规则误判
    if (HOST_PORT.matches(raw)) return "http://$raw"
    // 用 containsMatchIn:matches 要求整串匹配,带协议的长 URL 会被判成 null
    if (SCHEME.containsMatchIn(raw)) return raw
    if (raw.startsWith("//")) return "https:$raw"
    if (DOMAIN.matches(raw)) return "https://$raw"
    return null
}

/** 取域名并转小写,传入的地址需要带协议 */
fun String?.webHost(): String? {
    val url = this?.trim().orEmpty()
    if (url.isEmpty()) return null
    return runCatching { Uri.parse(url).host }.getOrNull()
        ?.lowercase()
        ?.takeIf { it.isNotBlank() }
}

/** 销毁 WebView。先从视图树移除再 destroy,避免崩溃或泄漏 */
fun WebView.destroySafely() {
    (parent as? ViewGroup)?.removeView(this)
    runCatching { stopLoading() }
    runCatching { settings.javaScriptEnabled = false }
    runCatching { clearHistory() }
    runCatching { removeAllViews() }
    runCatching { destroy() }
}

/** 执行 JS。统一走 evaluateJavascript,不用 loadUrl("javascript:...") */
fun WebView.evaluateJs(script: String?, onResult: ((String?) -> Unit)? = null) {
    if (script.isNullOrBlank()) return
    runCatching { evaluateJavascript(script, onResult) }
}

/** 调用页面里的全局函数,参数按 JSON 转义;要返回值就用 [evaluateJs] 的 callback */
fun WebView.callJs(functionName: String, vararg args: Any?) {
    val params = args.joinToString(",") { arg ->
        when (arg) {
            null -> "null"
            is Number, is Boolean -> arg.toString()
            else -> JSONObject.quote(arg.toString())
        }
    }
    evaluateJs("$functionName($params)")
}

/** 清缓存。clearCache 是异步的,清完立刻 reload 可能还是旧资源 */
fun WebView.clearWebCache() {
    runCatching { clearCache(true) }
    runCatching { clearFormData() }
}

private val SCHEME = Regex("^(?i)(https?|file|about|data|javascript):")
private val HOST_PORT = Regex("^[a-zA-Z0-9.-]+:\\d+(/.*)?$")
private val DOMAIN = Regex("^[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)+(/.*)?$")
