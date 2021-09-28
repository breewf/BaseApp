package com.hy.basic.networkkt

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

        if (BuildConfig.DEBUG) {
            val httpLoggingInterceptor = HttpLoggingInterceptor()
            builder.addInterceptor(httpLoggingInterceptor.apply {
                httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
            })
        }

        return builder.build()
    }

    fun get(): ApiService {
        return api
    }
}