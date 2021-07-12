package com.hy.myapp.api;

import com.hy.basic.network.ApiResponse;
import com.hy.basic.network.IServiceApi;
import com.hy.basic.network.ResponseSubscriber;
import com.hy.basic.network.RetrofitClient;
import com.hy.basic.network.TransformerUtils;
import com.hy.myapp.bean.ReviewProductData;

/**
 * @author hy
 * @date 2021/7/12
 * desc: ApiDataRepo
 **/
public class ApiDataRepo {

    private IServiceApi getServiceApi() {
        return RetrofitClient.getInstance().getServiceApi();
    }

    public void getReviewProductDetail(String reviewProductId, ResponseSubscriber<ReviewProductData> subscriber) {
        getServiceApi().getReviewProductDetail(reviewProductId)
                .compose(TransformerUtils.ioToMainThread())
                .map(ApiResponse.handleApiResponse())
                .subscribe(subscriber);
    }

}
