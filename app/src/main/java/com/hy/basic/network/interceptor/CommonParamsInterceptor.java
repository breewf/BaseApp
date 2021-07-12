package com.hy.basic.network.interceptor;

import com.hy.baseapp.common.SHA1;

import java.io.IOException;

import androidx.annotation.NonNull;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @author hy
 * @date 2021/7/12
 * desc: 请求公共参数
 **/
public class CommonParamsInterceptor implements Interceptor {

    private static final String REQUEST_METHOD_GET = "GET";
    private static final String REQUEST_METHOD_POST = "POST";

    @NonNull
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();

        if (REQUEST_METHOD_GET.equals(request.method())) {
            HttpUrl httpUrl = request.url().newBuilder()
                    .addQueryParameter("device", "Android")
                    .build();
            request = request.newBuilder().url(httpUrl).build();
        } else if (REQUEST_METHOD_POST.equals(request.method())) {
            if (request.body() instanceof FormBody) {
                FormBody.Builder bodyBuilder = new FormBody.Builder();
                FormBody formBody = (FormBody) request.body();

                for (int i = 0; i < formBody.size(); i++) {
                    bodyBuilder.addEncoded(formBody.encodedName(i), formBody.encodedValue(i));
                }

                long t = (int) (System.currentTimeMillis() / 1000);
                // 时间戳（单位秒）
                String time = Long.toString(t);
                // 任意字段
                String nonce = "huxiu_android_app";
                String privateKey = "app_api_huxiu_com";
                String originalSignature = time + privateKey + nonce;
                // sh1 加密后的签名
                String signature = SHA1.getSha1(originalSignature);

                formBody = bodyBuilder
                        .addEncoded("token", "")
                        .addEncoded("imei", "")
                        .addEncoded("mac_addr", "")
                        .addEncoded("nonce", nonce)
                        .addEncoded("signature", signature)
                        .addEncoded("timestamp", time)
                        .addEncoded("provider", "")
                        .addEncoded("is_notification", "0")
                        .addEncoded("data_version", "")
                        .build();

                request = request.newBuilder().post(formBody).build();
            }
        }

        return chain.proceed(request);
    }
}
