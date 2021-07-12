package com.hy.baseapp.api;

import com.hy.myapp.api.APIS;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by GHY on 2016/5/4.
 * Retrofit 网络请求
 */
public class RetrofitHelper {

    /**
     * Retrofit
     * @return
     */
    public static Retrofit getRetrofit(){
        Retrofit retrofit = new Retrofit.Builder()
                //baseUrl
                .baseUrl(APIS.BASE_URL_MY)
                //如果你想接收json结果并解析，你必须把Gson Converter作为一个独立的依赖添加进来
                .addConverterFactory(GsonConverterFactory.create())
                //Retrofit团队有已经准备好了的CallAdapter module。其中最著名的module可能是为RxJava准备的CallAdapter，它将作为Observable返回
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
        return retrofit;
    }
}


