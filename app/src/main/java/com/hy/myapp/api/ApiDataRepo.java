package com.hy.myapp.api;

import com.hy.baseapp.base.net.ApiResponse;
import com.hy.baseapp.base.net.IServiceApi;
import com.hy.baseapp.base.net.RetrofitClient;
import com.hy.baseapp.base.net.TransformerUtils;
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

    public Observable<TestResultData> getApiDataTest(String id) {
        return getServiceApi().getApiDataTest()
                .compose(TransformerUtils.ioToMainThread())
                .map(ApiResponse.handleApiResponse());
    }

}
