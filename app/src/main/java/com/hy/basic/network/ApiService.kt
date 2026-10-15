package com.hy.basic.network

import com.hy.myapp.demo.network.RandomStringData
import com.hy.myapp.demo.network.TestResultDataKt
import retrofit2.http.*

/**
 * @author hy
 * @date 2021/9/27
 * desc:
 **/
interface ApiService {

    /**
     * https://api.apiopen.top/docs
     *
     * {
     *   "code": 200,
     *   "data": {
     *     "from": "朱敦儒《鹧鸪天·西都作》",
     *     "name": "玉楼金阙慵归去，且插梅花醉洛阳。"
     *   },
     *   "message": "成功"
     * }
     *
     * 返回：{"code":200,"message":"成功","data":{"name":"万户千门成野草，只缘一曲后庭花。","from":"刘禹锡《台城》"}}
     */
    @GET("https://api.apiopen.top/api/tools/famous-sentence")
    suspend fun getTestResult(): BaseHttpResponse<TestResultDataKt?>

    /**
     * length:字符串长度 Default value : 16
     * type:字符串类型: upper(全大写), lower(全小写), mixed(大小写混合)
     * includeNumbers:boolean 是否包含数字
     *
     * 返回：{"code":200,"message":"成功","data":{"string":"Su0FjBE40lcMN47D"}}
     */
    @GET("https://api.apiopen.top/api/tools/random-string")
    suspend fun getApiTest(
        @Query("length") length: Int,
        @Query("type") type: String?,
        @Query("includeNumbers") includeNumbers: Boolean
    ): BaseHttpResponse<RandomStringData?>
}