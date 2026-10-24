package com.hy.basic.web

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.webkit.JavascriptInterface
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

/**
 * desc: JS Bridge,页面调原生、原生回结果或推事件。
 *
 * 页面加载完成后会注入 helper,JS 侧直接用:
 * ```
 * WebBridge.call("toast", { text: "hi" }).then(...).catch(...)
 * WebBridge.on("nativePush", data => ...)     // 对应原生 emit("nativePush", data)
 * ```
 * 不注入 helper 时按协议手写:`NativeBridge.post(action, paramsJson, callbackId)`,
 * 原生回 `window.__webBridgeResult(callbackId, ok, data)`。
 *
 * @JavascriptInterface 不能漏,漏了 JS 里就是 undefined,而且不报错。
 * 回调线程是 JavaBridge,这里已经切到主线程;同一个 callbackId 只回一次。
 * 注入对 iframe 也生效,信任判断只能看顶层 URL。
 */
class WebJsBridge(
    private val name: String,
    private val evaluate: (String) -> Unit,
    private val currentUrl: () -> String?,
    private val isTrusted: (String?) -> Boolean = { true },
) {

    /** 业务处理器:params 是页面传的 JSON,解析失败时给空对象;result 用来回传结果 */
    fun interface Handler {
        fun handle(params: JSONObject, result: Result)
    }

    private val handlers = ConcurrentHashMap<String, Handler>()
    private val mainHandler = Handler(Looper.getMainLooper())
    private val pendingEvents = ArrayDeque<Pair<String, String>>()

    @Volatile
    private var pageReady = false

    fun on(action: String, handler: Handler): WebJsBridge = apply { handlers[action] = handler }

    /** 原生 → JS 事件。页面没加载完先缓存,加载完成后补发 */
    fun emit(event: String, data: Any? = null) {
        val payload = data.toJsJson()
        runOnMain {
            if (!pageReady) {
                if (pendingEvents.size >= MAX_PENDING_EVENTS) pendingEvents.removeFirst()
                pendingEvents.addLast(event to payload)
                return@runOnMain
            }
            evaluate(eventScript(event, payload))
        }
    }

    /** 页面开始跳转,上一页没发出去的事件丢弃 */
    fun onPageStarted() {
        pageReady = false
        pendingEvents.clear()
    }

    /** 页面加载完成,注入 helper 并补发缓存的事件 */
    fun onPageReady() {
        evaluate(jsHelper())
        pageReady = true
        while (pendingEvents.isNotEmpty()) {
            val (event, payload) = pendingEvents.removeFirst()
            evaluate(eventScript(event, payload))
        }
    }

    fun release() {
        handlers.clear()
        pendingEvents.clear()
        pageReady = false
    }

    /**
     * JS 入口,方法名和参数名就是协议。
     * 运行在 JavaBridge 线程,不能直接操作 UI。
     */
    @JavascriptInterface
    fun post(action: String?, paramsJson: String?, callbackId: String?) {
        val method = action?.trim().orEmpty()
        if (method.isEmpty()) return
        val result = Result(callbackId)
        // currentUrl 是主线程写入的快照,这里只读字符串,不碰 WebView
        if (!isTrusted(currentUrl())) {
            result.fail("当前页面不允许调用原生")
            return
        }
        val handler = handlers[method] ?: run {
            result.fail("未注册的 action: $method")
            return
        }
        val params = runCatching { JSONObject(paramsJson ?: "{}") }.getOrElse { JSONObject() }
        runOnMain {
            runCatching { handler.handle(params, result) }.onFailure {
                Log.e(TAG, "bridge [$method] 执行失败", it)
                result.fail(it.message ?: "native error")
            }
        }
    }

    inner class Result internal constructor(private val callbackId: String?) {

        private val replied = AtomicBoolean(false)

        fun success(data: Any? = null) = reply(true, data)

        fun fail(message: CharSequence? = null) = reply(false, message?.toString().orEmpty())

        private fun reply(ok: Boolean, data: Any?) {
            if (callbackId.isNullOrBlank()) return
            // 同一 id 只回一次,否则 JS 侧的 Promise 状态会乱
            if (!replied.compareAndSet(false, true)) return
            val payload = data.toJsJson()
            runOnMain {
                evaluate(
                    "window.__webBridgeResult&&window.__webBridgeResult(" +
                        "${JSONObject.quote(callbackId)},$ok,$payload)",
                )
            }
        }
    }

    private fun eventScript(event: String, payload: String): String =
        "window.__webBridgeEvent&&window.__webBridgeEvent(${JSONObject.quote(event)},$payload)"

    private fun jsHelper(): String = """
        (function () {
          if (window.WebBridge) return;
          var seq = 0, callbacks = {}, listeners = {};
          window.__webBridgeResult = function (id, ok, data) {
            var cb = callbacks[id];
            if (!cb) return;
            delete callbacks[id];
            clearTimeout(cb.timer);
            ok ? cb.resolve(data) : cb.reject(new Error(data || 'native error'));
          };
          window.__webBridgeEvent = function (event, data) {
            var fn = listeners[event];
            if (fn) fn(data);
          };
          window.WebBridge = {
            call: function (action, params) {
              return new Promise(function (resolve, reject) {
                var id = 'cb_' + (++seq) + '_' + Date.now();
                callbacks[id] = {
                  resolve: resolve,
                  reject: reject,
                  timer: setTimeout(function () {
                    // 原生一直没回结果时兜底,避免 Promise 永远挂着
                    if (callbacks[id]) { delete callbacks[id]; reject(new Error('native timeout')); }
                  }, 10000)
                };
                try {
                  window.$name.post(action, JSON.stringify(params || {}), id);
                } catch (e) {
                  delete callbacks[id];
                  reject(e);
                }
              });
            },
            on: function (event, fn) { listeners[event] = fn; }
          };
          window.dispatchEvent(new Event('WebBridgeReady'));
        })();
    """.trimIndent()

    private fun runOnMain(block: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) block() else mainHandler.post(block)
    }

    private fun Any?.toJsJson(): String = when (this) {
        null, JSONObject.NULL -> "null"
        is JSONObject, is JSONArray -> toString()
        is Number, is Boolean -> toString()
        else -> JSONObject.quote(toString())
    }

    private companion object {
        const val TAG = "WebJsBridge"
        const val MAX_PENDING_EVENTS = 20
    }
}
