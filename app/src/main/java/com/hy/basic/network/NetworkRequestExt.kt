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
 * 按响应格式分两类:
 * - 信封接口([BaseHttpResponse],含 code/msg/data):[request] / [launchRequest] / [awaitRequest]
 * - 裸 JSON 接口(第三方/公共接口,无信封):[requestRaw] / [launchRawRequest] / [awaitRawRequest]
 *
 * 按场景选用法:
 * 1. 单接口、只关心成功数据 -> [launchRequest](loading 与异常全自动,[onResult] 里只写业务)
 * 2. 同一方法内串行多个接口、需要判断上一步是否成功 -> [awaitRequest](失败返回 null,异常已上报)
 * 3. 需要精细组合操作符(节流/合并/分页/多值流) -> 直接用 [request] 返回的 Flow
 */

/**
 * 信封接口基础请求流:执行 [block]、校验业务码、按优先级解包数据。
 * 空响应/空数据视为异常;所有异常经 [ApiExceptionHandler.toApiException] 归一化后抛给下游。
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
}.execute()

/**
 * 裸 JSON 基础请求流:直接以接口返回的对象作为结果,不做信封解包。
 */
fun <T> BaseViewModel.requestRaw(block: suspend () -> T): Flow<T> =
    flow { emit(block()) }.execute()

/**
 * 统一执行策略:IO 调度 + 异常归一化。
 * 协程取消(页面销毁/主动取消)必须原样抛出,否则破坏结构化并发。
 */
private fun <T> Flow<T>.execute(): Flow<T> = this
    .flowOn(Dispatchers.IO)
    .catch { e ->
        if (e is CancellationException) throw e
        throw ApiExceptionHandler.toApiException(e)
    }

/**
 * 收集请求:loading 与全局异常统一处理,[onResult] 只接收成功解包后的数据。
 *
 * @return 请求是否成功(便于串行编排时决定是否继续;异常已通过 [BaseViewModel.apiExceptionEvent] 上报)
 */
suspend fun <T> BaseViewModel.launchRequest(
    showLoading: Boolean = true,
    block: suspend ApiService.() -> BaseHttpResponse<T>?,
    onResult: (T) -> Unit
): Boolean = launchFlow(request(block), showLoading, onResult)

/** 同 [launchRequest],用于无 code/msg/data 包裹的第三方公共接口 */
suspend fun <T> BaseViewModel.launchRawRequest(
    showLoading: Boolean = true,
    block: suspend () -> T,
    onResult: (T) -> Unit
): Boolean = launchFlow(requestRaw(block), showLoading, onResult)

/**
 * 挂起等待信封结果:成功返回数据,失败返回 null(异常已上报,无需再写 try/catch)。
 * 适合"先查列表再查详情"这类串行依赖场景。
 */
suspend fun <T> BaseViewModel.awaitRequest(
    showLoading: Boolean = true,
    block: suspend ApiService.() -> BaseHttpResponse<T>?
): T? {
    var data: T? = null
    val success = launchRequest(showLoading, block) { data = it }
    return if (success) data else null
}

/** 同 [awaitRequest],用于裸 JSON 接口 */
suspend fun <T> BaseViewModel.awaitRawRequest(
    showLoading: Boolean = true,
    block: suspend () -> T
): T? {
    var data: T? = null
    val success = launchRawRequest(showLoading, block) { data = it }
    return if (success) data else null
}

private suspend fun <T> BaseViewModel.launchFlow(
    flow: Flow<T>,
    showLoading: Boolean,
    onResult: (T) -> Unit
): Boolean {
    if (showLoading) startLoading()
    var success = false
    try {
        flow.collect { onResult(it) }
        success = true
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        postApiException(ApiExceptionHandler.toApiException(e))
    } finally {
        if (showLoading) stopLoading()
    }
    return success
}
