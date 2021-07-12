package com.hy.basic.network.interceptor;


import java.io.IOException;

import androidx.annotation.NonNull;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @author hy
 * @date 2021/7/12
 * desc:
 **/
public class HttpLoggingInterceptor implements Interceptor {
    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request request = chain.request();
        return chain.proceed(request);
    }
}
