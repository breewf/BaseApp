package com.hy.basic.network

import com.hy.basic.BuildConfig
import java.util.concurrent.TimeUnit
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * @author hy
 * desc: 网络层唯一入口。OkHttpClient / Retrofit / 接口实例的构建与持有全部收敛在这里。
 *
 * 使用优先级:能用上一级就不要用下一级:
 * 1. ViewModel 内用 launchRequest / request(调用处不需要出现本对象)
 * 2. 自建 Repository 等确需接口实例时:`Api.apiService.xxx`
 * 3. 多域名:`Api.create<ReviewService>(NetConstants.BASE_URL_REVIEW)`
 * 4. 全局定制:[registerInterceptor] 一处注册,全项目生效
 */
object Api {

    /** 默认接口实例:进程内只创建一次,线程安全 */
    val apiService: ApiService by lazy { create() }

    private val okHttpClientLazy = lazy { buildOkHttpClient() }

    /** 全局共享:连接池/线程池/Dispatcher 只此一份,多域名时复用而非各自新建 */
    val okHttpClient: OkHttpClient by okHttpClientLazy

    private val customInterceptors = mutableListOf<Interceptor>()

    /**
     * 注册全局拦截器(Cookie 管理、公共参数/头、网络缓存策略等)。
     * 建议在 Application 初始化时调用;因 [okHttpClient] 懒加载构建,必须在首次请求之前。
     */
    fun registerInterceptor(interceptor: Interceptor) {
        check(!okHttpClientLazy.isInitialized()) {
            "拦截器必须在首次网络请求之前注册,否则不会生效"
        }
        customInterceptors += interceptor
    }

    /** 创建接口实现。多域名时传入各自 baseUrl,共用同一份 OkHttp */
    inline fun <reified S : Any> create(baseUrl: String = NetConstants.BASE_URL): S =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(S::class.java)

    private fun buildOkHttpClient(): OkHttpClient = OkHttpClient.Builder().apply {
        connectTimeout(NetConstants.DEFAULT_CONNECT_TIMEOUT, TimeUnit.SECONDS)
        readTimeout(NetConstants.DEFAULT_READ_TIMEOUT, TimeUnit.SECONDS)
        writeTimeout(NetConstants.DEFAULT_WRITE_TIMEOUT, TimeUnit.SECONDS)
        // 连接失败自动重试(仅幂等请求会被重试)
        retryOnConnectionFailure(true)
        // 自定义拦截器在前,日志放最后:保证打印出的是补全公共头之后的最终请求
        customInterceptors.forEach { addInterceptor(it) }
        if (BuildConfig.DEBUG) addInterceptor(logInterceptor())
    }.build()

    private fun logInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
}
