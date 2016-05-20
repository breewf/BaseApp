package com.ghy.baseapp;

import android.app.Application;

import com.company.myapp.BuildConfig;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.ghy.baseapp.common.log.Log;
import com.ghy.baseapp.common.logger.LogLevel;
import com.ghy.baseapp.common.logger.Logger;
import com.liulishuo.filedownloader.FileDownloader;

/**
 * Created by GHY on 2016/4/29.
 */
public class BaseApplication extends Application {

    private static BaseApplication mApplicationInstance = null;

    private final String DEFAULT_LOGGER_TAG = "LOGGER";

    public static synchronized BaseApplication getInstance() {
        return mApplicationInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mApplicationInstance = this;

        //初始化logger
        initLogger();
        //初始化Fresco
        initFresco();
        //初始化文件下载
        initFileDownLoad();
    }

    /**
     * 初始化文件下载
     * https://github.com/lingochamp/FileDownloader/blob/master/README-zh.md
     */
    private void initFileDownLoad() {
        FileDownloader.init(this);
    }

    /**
     * 初始化图片加载Fresco
     * 文档地址：http://fresco-cn.org/
     */
    private void initFresco() {
        Fresco.initialize(this);
    }

    /**
     * 初始化Logger
     * GitHub地址： https://github.com/orhanobut/logger
     *
     * 基本配置
     * Logger.init(YOUR_TAG) // default PRETTYLOGGER or use just init()
     * .methodCount(3)                 // default 2
     * .hideThreadInfo()               // default shown
     * .logLevel(LogLevel.NONE)        // default LogLevel.FULL
     * .methodOffset(2)                // default 0
     * .logTool(new AndroidLogTool()); // custom log tool, optional
     */
    private void initLogger() {
        if (BuildConfig.DEBUG) {
            Logger.init(DEFAULT_LOGGER_TAG).logLevel(LogLevel.FULL);// default LogLevel.FULL
        } else {
            //release versions不打印log
            Log.isPrint = false;
            Logger.init(DEFAULT_LOGGER_TAG).logLevel(LogLevel.NONE);// default LogLevel.FULL
        }
    }

}
