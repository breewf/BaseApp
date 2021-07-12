package com.hy.basic.network;

import com.franmontiel.persistentcookiejar.PersistentCookieJar;
import com.franmontiel.persistentcookiejar.cache.SetCookieCache;
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor;
import com.hy.basic.network.interceptor.CommonParamsInterceptor;
import com.hy.basic.network.interceptor.HttpCacheInterceptor;
import com.hy.basic.network.interceptor.HttpHeaderInterceptor;
import com.hy.basic.network.interceptor.HttpLoggingInterceptor;
import com.hy.myapp.App;

import java.io.File;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * @author hy
 * @date 2021/7/12
 * desc: 网络请求
 **/
public class RetrofitClient {

    private IServiceApi mServiceApi;

    private RetrofitClient() {

    }

    public static RetrofitClient getInstance() {
        return RetrofitClientHolder.INSTANCE;
    }

    private static class RetrofitClientHolder {
        private static final RetrofitClient INSTANCE = new RetrofitClient();
    }

    public IServiceApi getServiceApi() {
        return mServiceApi;
    }

    /**
     * 初始化
     */
    public void initClient() {
        // 创建OkHttpClient
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                // 超时设置
                .connectTimeout(NetConstants.DEFAULT_CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(NetConstants.DEFAULT_READ_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(NetConstants.DEFAULT_WRITE_TIMEOUT, TimeUnit.SECONDS)
                // 错误重连
                .retryOnConnectionFailure(true)
                // 支持HTTPS
                .connectionSpecs(Arrays.asList(ConnectionSpec.CLEARTEXT, ConnectionSpec.MODERN_TLS))
                // cookie持久化
                .cookieJar(new PersistentCookieJar(new SetCookieCache(), new SharedPrefsCookiePersistor(App.getInstance())));

        //// Cookie不带持久化
        //builder.cookieJar(new CookieJar() {
        //
        //    private final HashMap<HttpUrl, List<Cookie>> cookieStore = new HashMap<>();
        //
        //    @Override
        //    public void saveFromResponse(@NonNull HttpUrl url, @NonNull List<Cookie> cookies) {
        //        cookieStore.put(url, cookies);
        //    }
        //
        //    @NonNull
        //    @Override
        //    public List<Cookie> loadForRequest(@NonNull HttpUrl url) {
        //        List<Cookie> cookies = cookieStore.get(url);
        //        return cookies != null ? cookies : new ArrayList<>();
        //    }
        //});

        // 添加各种拦截器
        addInterceptor(builder);

        // 创建Retrofit实例
        Retrofit retrofit = new Retrofit.Builder()
                .client(builder.build())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .baseUrl(NetConstants.BASE_URL_REVIEW)
                .build();

        // 创建API接口类
        mServiceApi = retrofit.create(IServiceApi.class);
    }

    /**
     * 添加拦截器
     */
    private void addInterceptor(OkHttpClient.Builder builder) {
        // Header
        builder.addInterceptor(new HttpHeaderInterceptor());
        // 公共参数
        builder.addInterceptor(new CommonParamsInterceptor());

        // 添加缓存控制策略
        File cacheDir = App.getInstance().getExternalCacheDir();
        if (cacheDir != null) {
            Cache cache = new Cache(cacheDir, NetConstants.DEFAULT_CACHE_SIZE);
            builder.cache(cache).addInterceptor(new HttpCacheInterceptor());
        }

        // 添加http log
        HttpLoggingInterceptor logger = new HttpLoggingInterceptor();
        builder.addInterceptor(logger);
    }


}
