package com.hy.basic.networkkt

import com.hy.myapp.bean.TestResultDataKt
import retrofit2.http.*

/**
 * @author hy
 * @date 2021/9/27
 * desc:
 **/
interface ApiService {

    /**
     * sid=28654780
     */
    @GET("https://api.apiopen.top/getSingleJoke")
    suspend fun getTestResult(@Query("sid") sid: String): BaseHttpResponse<TestResultDataKt?>

    @FormUrlEncoded
    @POST("https://api.uomg.com/api/rand.qinghua?format=json")
    suspend fun getApiTest2(
        @Field("id") id: String?
    ): BaseHttpResponse<String?>
}