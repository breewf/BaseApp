package com.hy.myapp;

import android.os.Handler;

import com.hy.baseapp.BaseApplication;
import com.hy.baseapp.common.Toasts;
import com.hy.baseapp.base.net.RetrofitClient;
import com.tencent.mmkv.MMKV;

/**
 * Created by GHY on 2016/4/29.
 */
public class App extends BaseApplication {

    private static App sInstance;
    private static Handler mHandler;

    public static App getInstance() {
        return sInstance;
    }

    public static Handler getMainHandler() {
        return mHandler;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
        mHandler = new Handler(getMainLooper());

        // MMKV 使用前必须初始化一次,官方推荐在 Application.onCreate
        MMKV.initialize(this);

        Toasts.install(this);
        RetrofitClient.getInstance().initClient();
    }
}
