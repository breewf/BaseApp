package com.hy.basic.network;

import rx.functions.Func1;

/**
 * @author hy
 * @date 2021/7/12
 * desc:
 **/
public class ApiResponse {

    /**
     * 对接口返回结果预处理
     */
    public static <T> Func1<HttpResponse<T>, T> handleApiResponse() {
        return restResult -> {
            // 处理接口返回的错误
            //if (!restResult.success) {
            //    throw new ApiException(restResult.code, restResult.msg);
            //}
            return restResult.data;
        };
    }

    /**
     * 对接口返回结果预处理
     */
    public static <T> Func1<HttpResponse<T>, T> handleApiResponseResult() {
        return restResult -> {
            // 处理接口返回的错误
            //if (!restResult.success) {
            //    throw new ApiException(restResult.code, restResult.msg);
            //}
            return restResult.result;
        };
    }

    /**
     * 对接口返回结果预处理
     */
    public static <T> Func1<HttpResponse<String>, String> handleApiResponseContent() {
        return restResult -> {
            // 处理接口返回的错误
            //if (!restResult.success) {
            //    throw new ApiException(restResult.code, restResult.msg);
            //}
            return restResult.content;
        };
    }
}
