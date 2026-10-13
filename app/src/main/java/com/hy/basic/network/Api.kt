package com.hy.basic.network

import com.hy.myapp.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * @author hy
 * @date 2021/9/27
 * desc: Api
 **/
object Api {

    private val api by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(NetConstants.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(getOkHttpClient())
            .build()
        retrofit.create(ApiService::class.java)
    }

    private fun getOkHttpClient(): OkHttpClient {
        val builder: OkHttpClient.Builder = OkHttpClient.Builder()
            .readTimeout(NetConstants.DEFAULT_READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(NetConstants.DEFAULT_WRITE_TIMEOUT, TimeUnit.SECONDS)
            .connectTimeout(NetConstants.DEFAULT_CONNECT_TIMEOUT, TimeUnit.SECONDS)
            // 连接失败时自动重试(幂等请求)
            .retryOnConnectionFailure(true)

        if (BuildConfig.DEBUG) {
            val httpLoggingInterceptor = HttpLoggingInterceptor()
            builder.addInterceptor(httpLoggingInterceptor.apply {
                httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
            })
        }

        // 统一头信息(如需鉴权 header/Cookie 管理,在此追加 Interceptor)
        return builder.build()
    }

    /** 获取接口实例;后续多域名时可按模块拆分为多个 lazy Retrofit */
    fun get(): ApiService {
        return api
    }
}