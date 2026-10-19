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
 * desc: 全局事件总线,基于 MutableSharedFlow,解决无直接引用关系的组件间通信。
 *
 * 与 UiEvent 的分工:UiEvent 是单页内 ViewModel→View 的一次性事件;
 * EventBus 是跨页面/跨组件的全局广播,任何地方都能 post / observe。
 *
 * 生命周期安全:observeEvent / observeStickyEvent 随宿主销毁自动取消。
 * 事件即类型:直接用 data class 描述事件,同类型即同一条流。
 */
object EventBus {

    private const val TAG = "EventBus"

    /** 无订阅者时的短时缓冲上限;瞬时堆积超限会丢弃最旧(与 BaseViewModel.uiEvent 同策略) */
    private const val BUFFER = 64

    /** 调试开关:置 true 后每笔 post/订阅命中都打 Logcat,定位完记得关 */
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
 * 订阅事件(推荐):跟随宿主 LifecycleOwner 生命周期,销毁时自动取消。
 * 收集贯穿整个存活期,若只想在前台接收可自行用 flow<T>() 配 repeatOnLifecycle(STARTED)。
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
