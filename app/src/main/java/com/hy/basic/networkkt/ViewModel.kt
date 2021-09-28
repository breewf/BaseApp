package com.hy.basic.networkkt

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*

/**
 * @author hy
 * @date 2021/9/27
 * desc: ViewModel
 **/
/**
 * 使用flow+retrofit
 * @param request
 * @return Flow<BaseHttpResponse<T>>
 */
suspend fun <T> BaseViewModel.request(
    request: suspend ApiService.() -> BaseHttpResponse<T>?
): Flow<BaseHttpResponse<T>> {
    val vm = this

    return flow {
        val response = request(Api.get()) ?: throw IllegalArgumentException("ViewModel:获取响应数据为空")
        response.throwAPIException()
        emit(response)
    }.flowOn(Dispatchers.IO)
        .onCompletion { cause ->
            run {
                cause?.let {
                    throw catchException(
                        vm,
                        it
                    )
                }// 这里再重新把捕获的异常再次抛出，调用的时候如果有必要可以再次 catch 获取异常
            }
        }
}

fun catchException(vm: BaseViewModel, e: Throwable): Throwable {
    e.printStackTrace()

    if (e is java.util.concurrent.CancellationException) {
        return e
    }
    val exception = ApiExceptionHandler.handlerException(e)
    exception.let {
        if (it is ApiException) {
            vm.onApiException(it)
        } else {
            Log.e("ViewModel---->", "catchException:" + e.message)
        }
    }
    return exception
}

fun BaseViewModel.onApiException(exception: ApiException) {
    if (ApiExceptionHandler.handlerApiException(exception)) {
        apiExceptionEvent.value = exception
        return
    } else {
        Log.e("ViewModel---->", "catchApiException:" + exception.message)
    }
}

fun <T> Flow<T>.catchError(bloc: Throwable.() -> Unit) = catch { cause -> bloc(cause) }

suspend fun <T> Flow<T>.next(bloc: suspend T.() -> Unit): Unit = catch { }.collect { bloc(it) }