package com.hy.basic.event

import android.util.Log
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.launch

/**
 * @author hy
 * desc: 全局事件总线 —— 基于 [MutableSharedFlow] 的极轻封装,解决"无直接引用关系"的
 * 组件间通信(Fragment ↔ Fragment、父子页、Activity ↔ 非 UI 代码、跨页面广播)。
 *
 * 为什么选 EventBus 而不是共享 ViewModel:
 * - 共享 ViewModel 只在"同一 Activity 作用域"内有效,跨页面(如详情页操作 → 列表页返回刷新)、
 *   Service/仓库层发事件就无能为力,且每个场景都要单独定义一个 ViewModel 类,页面一多就碎片化;
 * - 本总线是**全局单例 + 一条 SharedFlow**,一处订阅到处可达;类型即事件,订阅/发送都靠泛型,
 *   没有字符串 tag 注册表,IDE 可直接跳转,排查时 `logEnabled=true` 就能看到全部收发流水。
 *
 * 与基类 [com.hy.basic.page.UiEvent] 的分工(两条通道互不替代):
 * - `UiEvent` 是**单页内** ViewModel→View 的一次性事件(Toast/导航/回传),随页面 ViewModel 存亡;
 * - `EventBus` 是**跨页面/跨组件**的全局广播,任何地方都能 post / observe。
 *
 * 生命周期安全:推荐用扩展 [observeEvent] / [observeStickyEvent],订阅随宿主
 * (Activity/Fragment)销毁自动取消,无需手写 unregister,杜绝泄漏;handler 固定在主线程回调
 * (lifecycleScope 是 Dispatchers.Main.immediate),post 可在任意线程。
 *
 * 事件即类型:直接用 `data class` 描述事件即可(无需继承任何基类),同类型即同一条流。
 * 事件多参时用一个 data class 承载,别用一对多参数重载。
 */
object EventBus {

    private const val TAG = "EventBus"

    /** 无订阅者时的短时缓冲上限;瞬时堆积超限会丢弃最旧(与 BaseViewModel.uiEvent 同策略) */
    private const val BUFFER = 64

    /**
     * 调试开关:置 true 后每笔 post/订阅命中都打 Logcat(tag=[TAG]),
     * 排查"事件到底发没发、谁收了"时打开,定位完记得关掉。默认关闭零开销。
     */
    @Volatile
    var logEnabled: Boolean = false

    private val _events = MutableSharedFlow<Any>(extraBufferCapacity = BUFFER)

    /** 全局事件流(原始,按类型过滤后用 [observeEvent]/[flow]) */
    val events: SharedFlow<Any> = _events.asSharedFlow()

    /** 粘性事件缓存:按事件 Class 存最新一条,供"晚到的订阅者"补收 */
    private val stickyCache = ConcurrentHashMap<Class<*>, Any>()

    /**
     * 发送一个全局事件(可在任意线程)。返回是否成功入队(缓冲满时 false,一般可忽略)。
     */
    fun post(event: Any): Boolean {
        if (logEnabled) Log.d(TAG, "post → ${event.javaClass.simpleName}: $event")
        return _events.tryEmit(event)
    }

    /**
     * 发送粘性事件:除广播给当前订阅者外,还缓存最新一条,
     * 之后新起的 [observeStickyEvent] 会立即补收一次(适合"登录成功""配置变更"等
     * 需要被晚创建的页面也能拿到的场景)。
     */
    fun postSticky(event: Any) {
        stickyCache[event.javaClass] = event
        if (logEnabled) Log.d(TAG, "postSticky → ${event.javaClass.simpleName}: $event")
        post(event)
    }

    /** 取出(并移除)某类型的粘性缓存;不需要补收或消费完时手动清理 */
    fun <T : Any> clearSticky(type: Class<T>) {
        stickyCache.remove(type)
        if (logEnabled) Log.d(TAG, "clearSticky ← ${type.simpleName}")
    }

    /** 清空全部粘性缓存(如退出登录时一次性复位) */
    fun clearAllSticky() {
        stickyCache.clear()
        if (logEnabled) Log.d(TAG, "clearAllSticky")
    }

    /** 读某类型当前粘性缓存(不移除),供非协程处直接取值 */
    @Suppress("UNCHECKED_CAST")
    fun <T : Any> peekSticky(type: Class<T>): T? = stickyCache[type] as? T

    /** 通用订阅:返回按 [T] 过滤后的事件流,供需要自定义收集策略时使用 */
    inline fun <reified T : Any> flow(): Flow<T> = events.filterIsInstance<T>()

    /** 粘性流:有缓存则先补发缓存一次,再接续后续实时事件 */
    inline fun <reified T : Any> stickyFlow(): Flow<T> {
        val cached = peekSticky(T::class.java)
        val live = flow<T>()
        return if (cached != null) flow {
            @Suppress("UNCHECKED_CAST")
            emit(cached as T)
            emitAll(live)
        } else live
    }

    @PublishedApi
    internal fun logReceive(event: Any, owner: Any) {
        if (logEnabled) {
            Log.d(TAG, "  received by ${owner.javaClass.simpleName}: ${event.javaClass.simpleName}")
        }
    }
}

/**
 * 订阅事件(推荐):跟随宿主 [LifecycleOwner] 生命周期,从创建起收集、销毁时自动取消,
 * 无需手动 unregister。[handler] 固定在主线程执行。
 *
 * 用法(在 Activity/Fragment 里):
 * ```
 * // Fragment A 发的"刷新列表"事件,Fragment B / Activity 都能收到
 * observeEvent<RefreshEvent> { binding.tvLog.append("收到刷新:${it.reason}") }
 * ```
 *
 * 说明:收集贯穿整个存活期(created~destroyed),期间在后台也会接收(适合纯逻辑/文本追加);
 * 若只想在前台接收并避免后台更新 UI,可自行改用 `flow<T>()` 配 `repeatOnLifecycle(STARTED)`。
 */
inline fun <reified T : Any> LifecycleOwner.observeEvent(
    crossinline handler: (T) -> Unit,
) {
    lifecycleScope.launch {
        EventBus.flow<T>().collect {
            EventBus.logReceive(it, this@observeEvent)
            handler(it)
        }
    }
}

/**
 * 订阅粘性事件:若该类型已有缓存,订阅瞬间先补收一次,再持续接收后续事件。
 * 用于"晚创建的页面也要拿到最近一次全局状态"(如进入任意页都应知道用户已登录)。
 * 消费完可 [EventBus.clearSticky] 结束这次粘性。
 */
inline fun <reified T : Any> LifecycleOwner.observeStickyEvent(
    crossinline handler: (T) -> Unit,
) {
    lifecycleScope.launch {
        EventBus.stickyFlow<T>().collect {
            EventBus.logReceive(it, this@observeStickyEvent)
            handler(it)
        }
    }
}

/** 便捷发送:让任何持有事件对象的地方都能 `event.post()`,读起来更顺 */
fun Any.postToBus(): Boolean = EventBus.post(this)
