package com.hy.basic.networkkt

import com.google.gson.annotations.SerializedName

/**
 * @author hy
 * @date 2021/9/27
 * desc: BaseHttpResponse
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

    private val errorCode: Int? = null
    private val errorMsg = ""


    private fun isValid(): Boolean {
        return errorCode == 0
    }

    @Throws(ApiException::class)
    fun throwAPIException() {
        //if (!isValid()) {
        //    throw ApiException(errorCode, errorMsg)
        //}
    }
}