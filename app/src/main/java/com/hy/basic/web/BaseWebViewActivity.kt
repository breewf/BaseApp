package com.hy.basic.web

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.net.http.SslError
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.ConsoleMessage
import android.webkit.RenderProcessGoneDetail
import android.webkit.SslErrorHandler
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.hy.basic.BaseActivity
import com.hy.basic.BaseViewModel
import com.hy.basic.utils.isAppDebugBuild
import com.hy.myapp.R
import com.hy.myapp.databinding.ActivityBaseWebBinding
import java.lang.reflect.ParameterizedType

/**
 * desc: WebView 页面基类。壳布局固定:进度条 + WebView + 错误页,子类一般只配地址和白名单。
 *
 * ```
 * class AgreementActivity : BaseWebViewActivity<AgreementViewModel>() {
 *     override val initialUrl = "https://m.xxx.com/agreement"
 *     override val allowedHosts = setOf("m.xxx.com")
 *     override val jsBridgeEnabled = true
 *     override fun onWebViewReady(b: Bundle?) {
 *         bridge?.on("close") { _, r -> r.success(); finish() }
 *     }
 * }
 * ```
 *
 * 返回键会先尝试网页后退;SSL 默认拦截;渲染进程崩溃时会重建 WebView。
 */
abstract class BaseWebViewActivity<VM : BaseViewModel> :
    BaseActivity<ActivityBaseWebBinding, VM>() {

    /** 当前 WebView。崩溃后会重建,引用会变,别长期缓存 */
    protected lateinit var webView: WebView
        private set

    /** JS Bridge,仅在 [jsBridgeEnabled] 为 true 时创建 */
    protected var bridge: WebJsBridge? = null
        private set

    /** 首屏地址 */
    protected open val initialUrl: String? = null

    /** 站内白名单。为空时所有 http(s) 链接都在 WebView 内打开 */
    protected open val allowedHosts: Set<String> = emptySet()

    /** 允许忽略证书错误的域名,默认空,一般只给内网或调试用 */
    protected open val sslTolerantHosts: Set<String> = emptySet()

    /** 是否创建 JS Bridge。http(s) 页面需要命中 [allowedHosts] 才能调用 */
    protected open val jsBridgeEnabled: Boolean = false

    /** JS 里访问桥的名字,默认 window.NativeBridge */
    protected open val jsBridgeName: String = "NativeBridge"

    /** 是否用网页 title 覆盖 Toolbar 标题 */
    protected open val useWebTitle: Boolean = true

    override val showToolBar: Boolean get() = true

    private var filePathCallback: ValueCallback<Array<Uri>>? = null

    /** 当前页面地址。Bridge 回调在 JavaBridge 线程,这里存字符串,避免跨线程读 WebView */
    @Volatile
    private var currentPageUrl: String? = null

    private var firstPageFinished = false

    /** 历史栈是否来自转屏恢复,恢复过的就不要再清 */
    private var restoredFromState = false

    private val fileChooserLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val callback = filePathCallback ?: return@registerForActivityResult
            filePathCallback = null
            // 取消时也要回传 null,不然下次点 <input file> 没反应
            callback.onReceiveValue(
                WebChromeClient.FileChooserParams.parseResult(result.resultCode, result.data),
            )
        }

    final override fun createBinding(inflater: LayoutInflater): ActivityBaseWebBinding =
        ActivityBaseWebBinding.inflate(inflater)

    @Suppress("UNCHECKED_CAST")
    override fun createViewModel(): VM {
        // 中间层把 ViewModel 泛型放到了第 1 个参数,BaseActivity 只解析第 2 个,这里补一次
        val vmClass = (javaClass.genericSuperclass as? ParameterizedType)
            ?.actualTypeArguments?.firstOrNull() as? Class<*>
        return if (vmClass != null) {
            ViewModelProvider(this)[vmClass as Class<VM>]
        } else {
            super.createViewModel()
        }
    }

    /** 固定装配,子类初始化写在 [onWebViewReady] */
    final override fun initView(savedInstanceState: Bundle?) {
        webView = createWebView()
        binding.webContainer.addView(webView, 0, matchParentParams())
        attachWebView()
        binding.btnWebRetry.setOnClickListener { retry() }
        setupBackPressed()
        setupImeInsets()
        onWebViewReady(savedInstanceState)
        // 转屏时优先恢复历史栈,没有历史再加载首屏
        restoredFromState = savedInstanceState != null && webView.restoreState(savedInstanceState) != null
        if (!restoredFromState) initialUrl?.let { loadUrl(it) }
    }

    /** WebView 和 Bridge 已就绪,在这里注册桥方法 */
    protected open fun onWebViewReady(savedInstanceState: Bundle?) {}

    /** 主 frame 加载完成。重定向、前进后退都会再次回调 */
    protected open fun onWebPageFinished(url: String?) {}

    /** 网页 title 回调,页面没有 title 时为 null */
    protected open fun onWebTitleChanged(title: String?) {}

    protected open fun createWebView(): WebView = WebView(this).apply {
        // WebView 自己会滚动,外层不要再套 ScrollView
        overScrollMode = View.OVER_SCROLL_NEVER
        setBackgroundColor(Color.WHITE)
        setupDefaultSettings()
        configureWebSettings(settings)
    }

    /** 子类可以在这里补 UA、缓存策略等 */
    protected open fun configureWebSettings(settings: WebSettings) {}

    /** 返回键处理,默认有历史就后退 */
    protected open fun handleWebBack(): Boolean {
        if (!webView.canGoBack()) return false
        webView.goBack()
        return true
    }

    /**
     * 页面能否调用 Bridge。file 页面和 [allowedHosts] 中的域名放行;
     * 运行在 JavaBridge 线程,不要在这里操作 UI。
     */
    protected open fun isBridgeTrusted(url: String?): Boolean {
        if (url.isNullOrBlank()) return false
        if (url.startsWith("file://")) return true
        val host = url.webHost() ?: return false
        return host in allowedHosts
    }

    protected fun loadUrl(url: String?) {
        val target = url.toWebUrl()
        // 地址不合法时打日志,否则 WebView 一直是白屏,看不出问题
        if (target == null) {
            Log.w(TAG, "地址无效,已忽略: $url")
            return
        }
        webView.loadUrlSafely(target)
    }

    protected fun reloadWeb() {
        binding.webErrorView.visibility = View.GONE
        webView.reload()
    }

    /** 显示错误页。用原生 View 盖住 WebView,不往历史里写错误 HTML */
    protected fun showWebError(message: CharSequence?) {
        binding.tvWebError.text = message ?: "页面加载失败"
        binding.webProgress.visibility = View.INVISIBLE
        binding.webErrorView.visibility = View.VISIBLE
    }

    private fun attachWebView() {
        webView.webViewClient = webClient
        webView.webChromeClient = chromeClient
        attachBridge()
    }

    private fun attachBridge() {
        if (!jsBridgeEnabled) return
        val jsBridge = bridge ?: WebJsBridge(
            name = jsBridgeName,
            evaluate = { script -> webView.evaluateJs(script) },
            currentUrl = { currentPageUrl },
            isTrusted = { url -> isBridgeTrusted(url) },
        ).also { bridge = it }
        webView.addJavascriptInterface(jsBridge, jsBridgeName)
    }

    private val webClient = object : WebViewClient() {

        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
            if (view !== webView) return true
            if (!request.isForMainFrame) return false
            return interceptUrl(request.url.toString())
        }

        @Deprecated("Deprecated in Java")
        @Suppress("DEPRECATION")
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            // API 23 只回调老方法,24+ 走上面那个
            return view !== webView || interceptUrl(url)
        }

        override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
            if (view !== webView) return
            currentPageUrl = url
            binding.webErrorView.visibility = View.GONE
            binding.webProgress.progress = 0
            binding.webProgress.visibility = View.VISIBLE
            bridge?.onPageStarted()
        }

        override fun onPageFinished(view: WebView, url: String?) {
            if (view !== webView) return
            binding.webProgress.visibility = View.INVISIBLE
            if (!url.isNullOrBlank()) currentPageUrl = url
            // 首屏可能被 302 拆成多条历史,清一次,避免返回键要按多次
            if (!firstPageFinished) {
                firstPageFinished = true
                if (!restoredFromState) runCatching { view.clearHistory() }
            }
            bridge?.let { if (isBridgeTrusted(url ?: currentPageUrl)) it.onPageReady() }
            onWebPageFinished(url)
        }

        override fun onReceivedError(
            view: WebView,
            request: WebResourceRequest,
            error: WebResourceError,
        ) {
            // 只处理主 frame;子资源失败和跳转取消(ERROR_UNKNOWN)都不影响整页
            if (view !== webView || !request.isForMainFrame) return
            if (error.errorCode == WebViewClient.ERROR_UNKNOWN) return
            // 同一次加载可能回调多次,已经显示就不再重复
            if (binding.webErrorView.visibility == View.VISIBLE) return
            val message = if (error.errorCode == WebViewClient.ERROR_FAILED_SSL_HANDSHAKE) {
                "证书校验失败,已拦截"
            } else {
                "页面打不开:${error.description}"
            }
            showWebError(message)
        }

        @SuppressLint("WebViewClientOnReceivedSslError")
        override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError) {
            val host = error.url?.webHost()
            if (view === webView && host != null && host in sslTolerantHosts) {
                Log.w(TAG, "放行证书错误: $host / ${error.primaryError}")
                handler.proceed()
            } else {
                handler.cancel()
                // 主 frame 失败会走 onReceivedError;子资源的证书错误不盖整页
                Log.w(TAG, "证书校验失败: $host")
            }
        }

        @RequiresApi(Build.VERSION_CODES.O)
        override fun onRenderProcessGone(view: WebView, detail: RenderProcessGoneDetail): Boolean {
            // 返回 true 表示崩溃已处理。旧的 WebView 不能再用,销毁后重建
            if (view !== webView) return true
            replaceWebView()
            showWebError("页面渲染进程崩溃,请重新加载")
            return true
        }
    }

    private val chromeClient = object : WebChromeClient() {

        override fun onProgressChanged(view: WebView, newProgress: Int) {
            if (view !== webView) return
            binding.webProgress.progress = newProgress
            binding.webProgress.visibility =
                if (newProgress >= 100) View.INVISIBLE else View.VISIBLE
        }

        override fun onReceivedTitle(view: WebView, title: String?) {
            if (view !== webView) return
            if (useWebTitle && !title.isNullOrBlank()) toolbar?.title = title
            onWebTitleChanged(title)
        }

        override fun onShowFileChooser(
            view: WebView,
            filePathCallback: ValueCallback<Array<Uri>>,
            fileChooserParams: WebChromeClient.FileChooserParams,
        ): Boolean {
            // 旧的 callback 不置空,再点文件选择会卡住
            this@BaseWebViewActivity.filePathCallback?.onReceiveValue(null)
            this@BaseWebViewActivity.filePathCallback = filePathCallback
            return runCatching {
                fileChooserLauncher.launch(fileChooserParams.createIntent())
                true
            }.getOrElse {
                this@BaseWebViewActivity.filePathCallback = null
                filePathCallback.onReceiveValue(null)
                false
            }
        }

        override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
            // 把页面 console 输出转到 Logcat,方便排查 H5
            if (this@BaseWebViewActivity.isAppDebugBuild()) {
                Log.d(
                    TAG,
                    "[${consoleMessage.messageLevel()}] ${consoleMessage.message()} " +
                        "@${consoleMessage.sourceId()}:${consoleMessage.lineNumber()}",
                )
            }
            return true
        }
    }

    /** 链接分流,返回 true 表示这次跳转不交给 WebView */
    private fun interceptUrl(rawUrl: String?): Boolean {
        val url = rawUrl?.trim().orEmpty()
        if (url.isEmpty()) return true
        val uri = runCatching { Uri.parse(url) }.getOrNull() ?: return false
        return when (uri.scheme?.lowercase()) {
            "http", "https" -> {
                val host = uri.host?.lowercase()
                val inApp = allowedHosts.isEmpty() || (host != null && host in allowedHosts)
                !inApp && openExternally(url, uri)
            }
            // file / 内部协议交给 WebView
            "file", "about", "data", "javascript", "blob" -> false
            // tel: / mailto: / intent:// 等交给系统
            else -> openExternally(url, uri)
        }
    }

    /** intent:// 需要用 parseUri 解析,直接 ACTION_VIEW 打不开 */
    private fun openExternally(rawUrl: String, uri: Uri): Boolean {
        val intent = if (rawUrl.startsWith("intent:", ignoreCase = true)) {
            runCatching { Intent.parseUri(rawUrl, Intent.URI_INTENT_SCHEME) }.getOrNull()
        } else {
            Intent(Intent.ACTION_VIEW, uri)
        }
        return intent == null || runCatching {
            startActivity(intent)
            true
        }.getOrElse {
            showToast("没有能打开这个链接的应用")
            true
        }
    }

    private fun setupBackPressed() {
        // 挂在 dispatcher 上,Toolbar 和系统返回键都会先走这里
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (handleWebBack()) return
                // 先关掉自己再转发,避免重复回调
                isEnabled = false
                onBackPressedDispatcher.onBackPressed()
            }
        })
    }

    private fun setupImeInsets() {
        // edge-to-edge 下 adjustResize 不再缩窗口,键盘弹起要自己补 IME 的底部 inset
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
            // 壳布局已经补过系统栏,这里只补键盘多出来的部分
            val extra = (ime - bars).coerceAtLeast(0)
            if (view.paddingBottom != extra) view.setPadding(0, 0, 0, extra)
            insets
        }
    }

    private fun retry() {
        val target = currentPageUrl?.takeIf { it.isNotBlank() } ?: initialUrl
        binding.webErrorView.visibility = View.GONE
        if (target.isNullOrBlank()) webView.reload() else webView.loadUrlSafely(target)
    }

    /** 渲染进程崩溃后重建 WebView,旧实例销毁,桥和 client 重新挂 */
    private fun replaceWebView() {
        val broken = webView
        filePathCallback?.onReceiveValue(null)
        filePathCallback = null
        runCatching { broken.removeJavascriptInterface(jsBridgeName) }
        binding.webContainer.removeView(broken)
        broken.destroySafely()
        firstPageFinished = false
        restoredFromState = false
        currentPageUrl = null
        bridge?.onPageStarted()
        webView = createWebView()
        binding.webContainer.addView(webView, 0, matchParentParams())
        attachWebView()
    }

    private fun matchParentParams() = FrameLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT,
    )

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // 连前进/后退栈一起恢复,否则转屏后回到首屏
        if (::webView.isInitialized) runCatching { webView.saveState(outState) }
    }

    override fun onPause() {
        if (::webView.isInitialized) webView.onPause()
        // pauseTimers 是进程级的,会影响其他 WebView,这里不用
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        if (::webView.isInitialized) webView.onResume()
    }

    override fun onDestroy() {
        if (::webView.isInitialized) {
            runCatching { webView.removeJavascriptInterface(jsBridgeName) }
            bridge?.release()
            binding.webContainer.removeView(webView)
            webView.destroySafely()
        }
        super.onDestroy()
    }

    private companion object {
        const val TAG = "BaseWebView"
    }
}
