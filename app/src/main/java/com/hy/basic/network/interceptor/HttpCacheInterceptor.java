package com.hy.basic.network.interceptor;

import com.hy.basic.utils.AppNetworkUtils;

import java.io.IOException;

import androidx.annotation.NonNull;
import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @author hy
 * @date 2021/7/12
 * desc:
 **/
public class HttpCacheInterceptor implements Interceptor {
    @NonNull
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        // 无网络时，始终使用本地Cache
        if (!AppNetworkUtils.isConnected()) {
            request = request.newBuilder()
                    .cacheControl(CacheControl.FORCE_CACHE)
                    .build();
        }

        Response response = chain.proceed(request);
        if (AppNetworkUtils.isConnected()) {
            // 有网络时，设置缓存过期时间0个小时
            int maxAge = 0;
            response.newBuilder()
                    .header("Cache-Control", "public, max-age=" + maxAge)
                    // 清除头信息，因为服务器如果不支持，会返回一些干扰信息，不清除下面无法生效
                    .removeHeader("Pragma")
                    .build();
        } else {
            // 无网络时，设置缓存过期超时时间为4周
            int maxStale = 60 * 60 * 24 * 28;
            response.newBuilder()
                    .header("Cache-Control", "public, only-if-cached, max-stale=" + maxStale)
                    .removeHeader("Pragma")
                    .build();
        }
        return response;
    }
}
