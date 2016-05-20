package com.company.myapp.api.cyp;

import android.app.Activity;
import android.text.TextUtils;

import com.ghy.baseapp.api.ErrorCode;
import com.ghy.baseapp.api.Header;
import com.ghy.baseapp.common.log.Log;
import com.google.gson.Gson;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by GHY on 2016/5/3.
 * api请求基类(activity)
 */
public abstract class BaseApiAvCYP<T extends Activity, K extends ResponseDataCYP> {

    public static final int REQUEST_METHOD_GET = 0;
    public static final int REQUEST_METHOD_POST = 1;

    /**
     * 设置api
     *
     * @return
     */
    public abstract String getApi();

    /**
     * 设置class
     *
     * @return
     */
    public abstract Class<K> getClazz();

    /**
     * 设置 发送请求方法
     * 默认为post
     *
     * @return
     */
    public int getRequestMethod() {
        return REQUEST_METHOD_POST;
    }

    /**
     * 设置请求header
     *
     * @return
     */
    public Map<String, String> getHeader() {
        return Header.getHeader();
    }

    /**
     * 设置请求参数
     */
    private Map<String, String> map = new HashMap<>();

    protected Map putParams(String key, String value) {
        if (TextUtils.isEmpty(value)) return map;
        map.put(key, value);
        return map;
    }

    /**
     * 获取请求参数
     *
     * @return
     */
    public Map<String, String> getParams() {
        return map;
    }

    public WeakReference<T> mReference;

    public BaseApiAvCYP(T activity) {
        mReference = new WeakReference<>(activity);
    }

    /**
     * 请求成功
     *
     * @param t
     * @param k
     */
    public abstract void onSuccess(T t, K k);

    /**
     * 请求失败
     *
     * @param t
     * @param status
     * @param msg
     */
    public abstract void onError(T t, int status, String msg);

    /**
     * 获取到的数据
     *
     * @param data
     */
    public void onResponse(String data) {

        T t = mReference.get();
        Log.i("Api == onResponse before parser ==" + data);
        try {
            K k = new Gson().fromJson(data, getClazz());
            if (k.resCode == 0) {
                Log.i("Api == onResponse after parser ==" + k.toString());
                onSuccess(t, k);
            } else {
                onError(t, ErrorCode.ERROR_RETURN, k.msg + "");
                Log.i("Api == onResponse error!");
            }
        } catch (Exception e) {
            onError(t, ErrorCode.ERROR_PARSER, "数据解析异常");
        }

    }

}
