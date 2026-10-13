package com.hy.basic.network

import com.hy.basic.BaseViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

/**
 * @author hy
 * desc: Retrofit + 协程统一请求封装
 *
 * 分层:
 * - [request]:基础请求流。IO 执行、解包 [BaseHttpResponse]、异常统一转为 [ApiException] 抛给下游
 * - [launchRequest]:推荐入口。在 viewModelScope 中收集,自动维护 loading 与全局异常上报,
 *   业务侧 [launchRequest.onResult] 只需处理成功数据
 *
 * 需要精细处理单项异常/多接口合并时,直接使用 [request] 返回的 Flow 自行组合操作符。
 */

/**
 * 基础请求流:执行 [block] 并解包业务数据。
 * [block] 的接收者就是 [ApiService],直接写接口方法名即可(不要写 Api.xxx 前缀)。
 * 空响应视为异常;所有异常经 [ApiExceptionHandler.toApiException] 归一化后向上抛出,
 * 由下游([launchRequest] 或调用方自定义 catch)决定最终处理方式。
 */
fun <T> BaseViewModel.request(
    block: suspend ApiService.() -> BaseHttpResponse<T>?
): Flow<T> = flow {
    val response = block(Api.apiService)
        ?: throw ApiException(ApiExceptionHandler.CODE_EMPTY_DATA, "接口未返回数据")
    response.throwApiException()
    emit(
        response.unwrap() ?: throw ApiException(
            ApiExceptionHandler.CODE_EMPTY_DATA,
            "接口数据为空"
        )
    )
}.flowOn(Dispatchers.IO)
    .catch { e ->
        // 协程取消(页面销毁/主动取消)必须原样抛出,否则破坏结构化并发
        if (e is CancellationException) throw e
        throw ApiExceptionHandler.toApiException(e)
    }

/**
 * 收集请求:loading 与全局异常统一处理,[onResult] 只接收成功解包后的数据。
 * 异常自动上报 [BaseViewModel.postApiException],由页面基类统一回调 onApiException。
 *
 * @param showLoading 是否自动维护 [BaseViewModel.isLoading](如下拉刷新等静默请求可关闭)
 */
suspend fun <T> BaseViewModel.launchRequest(
    showLoading: Boolean = true,
    block: suspend ApiService.() -> BaseHttpResponse<T>?,
    onResult: (T) -> Unit
) {
    if (showLoading) startLoading()
    try {
        request(block).collect { onResult(it) }
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        postApiException(e as? ApiException ?: ApiExceptionHandler.toApiException(e))
    } finally {
        if (showLoading) stopLoading()
    }
}
