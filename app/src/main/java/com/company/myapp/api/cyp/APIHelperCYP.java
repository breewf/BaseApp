package com.company.myapp.api.cyp;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;

import com.ghy.baseapp.BaseApplication;
import com.ghy.baseapp.api.ErrorCode;
import com.ghy.baseapp.common.log.Log;
import com.ghy.baseapp.common.utils.NetworkUtils;

/**
 * Created by GHY on 2016/5/3.
 * api请求助手类 CYP
 */
public class APIHelperCYP {

    private Context mContext;
    private static APIHelperCYP mInstance;

    public APIHelperCYP(Context context) {
        this.mContext = context;
    }

    public static APIHelperCYP getInstance() {
        if (mInstance == null) {
            mInstance = new APIHelperCYP(BaseApplication.getInstance().getApplicationContext());
        }
        return mInstance;
    }

    /**
     * api请求
     * fragment中请求
     */
    public void requestApi(BaseApiFgCYP baseApiFgCYP) {
        Log.i("Api == RequestApi == " + baseApiFgCYP);
        if (NetworkUtils.isConnected(mContext)) {
            //进行网络请求
            new OkHttpHelperCYP(baseApiFgCYP);
        } else {
            baseApiFgCYP.onError((Fragment) baseApiFgCYP.mReference.get(), ErrorCode.ERROR_NO_NET, "无网络连接");
        }
    }

    /**
     * api请求
     * activity中请求
     */
    public void requestApi(BaseApiAvCYP baseApiAvCYP) {
        Log.i("Api == RequestApi == " + baseApiAvCYP);
        if (NetworkUtils.isConnected(mContext)) {
            //进行网络请求
            new OkHttpHelperCYP(baseApiAvCYP);
        } else {
            baseApiAvCYP.onError((Activity) baseApiAvCYP.mReference.get(), ErrorCode.ERROR_NO_NET, "无网络连接");
        }
    }

}
