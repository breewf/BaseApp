package com.hy.myapp;

import android.os.Handler;

import com.hy.baseapp.BaseApplication;
import com.hy.baseapp.common.Toasts;
import com.hy.basic.network.RetrofitClient;

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

        Toasts.install(this);
        RetrofitClient.getInstance().initClient();
    }
}
