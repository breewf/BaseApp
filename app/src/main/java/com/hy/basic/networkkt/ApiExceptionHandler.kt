package com.hy.basic.networkkt

import com.google.gson.JsonParseException
import org.json.JSONException
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * @author hy
 * @date 2021/9/27
 * desc: 异常处理
 **/
object ApiExceptionHandler {

    // 网络状态码
    private const val CODE_NET_ERROR = 4000
    private const val CODE_TIMEOUT = 4080
    private const val CODE_JSON_PARSE_ERROR = 4010
    private const val CODE_SERVER_ERROR = 5000

    // 业务状态码
    private const val CODE_AUTH_INVALID = 401

    fun handlerException(e: Throwable): Throwable {
        return if (e is HttpException) {
            ApiException(CODE_NET_ERROR, "网络异常(${e.code()},${e.message()})")
        } else if (e is UnknownHostException) {
            ApiException(CODE_NET_ERROR, "网络连接失败，请稍后再试")
        } else if (e is SocketTimeoutException) {
            ApiException(CODE_TIMEOUT, "请求超时，请稍后再试")
        } else if (e is IOException) {
            ApiException(CODE_NET_ERROR, "网络异常(${e.message})")
        } else if (e is JsonParseException || e is JSONException) {
            // Json解析失败
            ApiException(CODE_JSON_PARSE_ERROR, "数据解析错误，请稍后再试")
        } else e
    }

    fun handlerApiException(
        e: ApiException
    ): Boolean {
        val code: Int? = e.code
        val message: String? = e.message
        when (code) {
            CODE_AUTH_INVALID -> {

                return true
            }
        }
        return false
    }
}