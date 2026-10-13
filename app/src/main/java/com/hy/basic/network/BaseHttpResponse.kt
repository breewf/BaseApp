package com.hy.basic.network

import com.google.gson.annotations.SerializedName

/**
 * @author hy
 * desc: 通用接口响应体。
 * 历史原因不同接口的数据字段不统一(data/result/content 并存),
 * 统一通过 [unwrap] 按优先级取值;
 * 新接口建议后端收敛为 code/msg/data 三字段后,只保留 data 即可。
 **/
class BaseHttpResponse<T> {

    @SerializedName("code")
    var code: Int = -1

    @SerializedName("msg")
    var msg: String? = null

    @SerializedName("data")
    var data: T? = null

    @SerializedName("statusCode")
    var statusCode: String? = null

    @SerializedName("desc")
    var desc: String? = null

    @SerializedName("result")
    var result: T? = null

    @SerializedName("content")
    var content: String? = null

    /**
     * 按 data -> result -> content 的优先级解包业务数据
     * (命名避开 getData,防止与 data 属性的 JVM getter 冲突)
     */
    @Suppress("UNCHECKED_CAST")
    fun unwrap(): T? = data ?: result ?: content as? T

    /**
     * 业务级校验钩子:后端统一错误码规范后(如 code != 0 视为业务失败),
     * 在此抛出 [ApiException] 即可让所有请求走统一的异常通道,
     * 无需改动各业务 ViewModel。
     */
    @Throws(ApiException::class)
    fun throwApiException() {
        // 示例(接口规范统一后启用):
        // if (code != 0) throw ApiException(code, msg ?: "请求失败")
    }
}
