package com.hy.basic.network

import com.google.gson.JsonParseException
import org.json.JSONException
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * @author hy
 * desc: 异常归一化处理。将各类网络/解析异常统一转为 [ApiException],
 * 业务侧只需面向 code/message 处理,无需感知底层异常类型
 **/
object ApiExceptionHandler {

    // 通用错误码
    const val CODE_UNKNOWN = 4999            // 未知错误
    const val CODE_EMPTY_DATA = 4998         // 接口无数据
    const val CODE_NET_ERROR = 4000          // 网络连接异常
    const val CODE_TIMEOUT = 4080            // 请求超时
    const val CODE_JSON_PARSE_ERROR = 4010   // 数据解析错误
    const val CODE_SERVER_ERROR = 5000       // 服务端异常

    // 业务状态码(与后端约定)
    const val CODE_AUTH_INVALID = 401        // 登录态失效,子类可在 onApiException 中跳转登录

    /**
     * 将任意异常归一化为 [ApiException]。
     * 注意 when 分支顺序:UnknownHostException/SocketTimeoutException 均为 IOException 子类,必须在前。
     */
    fun toApiException(e: Throwable): ApiException = when (e) {
        is ApiException -> e
        is HttpException -> ApiException(
            if (e.code() >= 500) CODE_SERVER_ERROR else CODE_NET_ERROR,
            "网络异常(${e.code()},${e.message()})"
        )
        is UnknownHostException -> ApiException(CODE_NET_ERROR, "网络连接失败，请稍后再试")
        is SocketTimeoutException -> ApiException(CODE_TIMEOUT, "请求超时，请稍后再试")
        is JsonParseException, is JSONException -> ApiException(CODE_JSON_PARSE_ERROR, "数据解析错误，请稍后再试")
        is IOException -> ApiException(CODE_NET_ERROR, "网络异常(${e.message})")
        else -> ApiException(CODE_UNKNOWN, e.message ?: "未知错误")
    }
}
