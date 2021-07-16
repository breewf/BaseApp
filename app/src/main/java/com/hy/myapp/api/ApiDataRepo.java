package com.hy.myapp.api;

import com.hy.basic.network.ApiResponse;
import com.hy.basic.network.IServiceApi;
import com.hy.basic.network.RetrofitClient;
import com.hy.basic.network.TransformerUtils;
import com.hy.myapp.bean.ReviewProductData;
import com.hy.myapp.bean.TestResultData;

import rx.Observable;

/**
 * @author hy
 * @date 2021/7/12
 * desc: ApiDataRepo
 **/
public class ApiDataRepo {

    private IServiceApi getServiceApi() {
        return RetrofitClient.getInstance().getServiceApi();
    }

    public Observable<ReviewProductData> getReviewProductDetail(String reviewProductId) {
        return getServiceApi().getReviewProductDetail(reviewProductId)
                .compose(TransformerUtils.ioToMainThread())
                .map(ApiResponse.handleApiResponse());
    }

    public Observable<TestResultData> getApiDataTest(String sid) {
        return getServiceApi().getApiDataTest(sid)
                .compose(TransformerUtils.ioToMainThread())
                .map(ApiResponse.handleApiResponseResult());
    }

    public Observable<String> getApiDataTest2() {
        return getServiceApi().getApiDataTest2("")
                .compose(TransformerUtils.ioToMainThread())
                .map(ApiResponse.handleApiResponseContent());
    }

}
