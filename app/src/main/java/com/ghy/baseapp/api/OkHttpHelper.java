package com.ghy.baseapp.api;

import android.app.Activity;
import android.app.Fragment;

import com.company.myapp.api.APIS;
import com.ghy.baseapp.api.okhttp.OkHttpUtils;
import com.ghy.baseapp.api.okhttp.callback.StringCallback;
import com.ghy.baseapp.common.log.Log;

import java.util.Iterator;
import java.util.Map;

import okhttp3.Call;

/**
 * Created by GHY on 2016/5/3.
 * OkHttp网络请求
 * activity或fragment中请求
 */
public class OkHttpHelper {

    public static final int METHOD_GET = 0;
    public static final int METHOD_POST = 1;

    public OkHttpHelper(BaseApiAv baseApiAv) {
        startRequest(baseApiAv,null);
    }

    public OkHttpHelper(BaseApiFg baseApiFg) {
        startRequest(null,baseApiFg);
    }

    /**
     * 进行网络请求
     *
     * @param baseApiAv
     * @param baseApiFg
     */
    private void startRequest(final BaseApiAv baseApiAv,final BaseApiFg baseApiFg) {
        if (baseApiAv == null && baseApiFg == null) return;
        int type = 0;
        String url;
        Map paramsMap;
        int requestMethod;
        if (baseApiAv != null && baseApiFg == null) type = 1;
        if (baseApiAv == null && baseApiFg != null) type = 2;
        if (type == 1){
            url = APIS.BASE_URL_MY + baseApiAv.getApi();
            paramsMap = baseApiAv.getParams();
            requestMethod = baseApiAv.getRequestMethod();
        }else if (type == 2){
            url = APIS.BASE_URL_MY + baseApiFg.getApi();
            paramsMap = baseApiFg.getParams();
            requestMethod = baseApiFg.getRequestMethod();
        }else {
            return;
        }
        Log.i("Api == RequestUrl== " + url.concat("?").concat(parseParams(paramsMap)));
        final int finalType = type;
        switch (requestMethod){
            case METHOD_GET:
                OkHttpUtils.get().url(url).params(paramsMap).build()
                        .execute(new StringCallback() {
                            @Override
                            public void onError(Call call, Exception e) {
                                if (finalType == 1){
                                    baseApiAv.onError((Activity) baseApiAv.mReference.get(), ErrorCode.ERROR_REQUEST, "请求出错：" + e.toString());
                                }else {
                                    baseApiFg.onError((Fragment) baseApiFg.mReference.get(), ErrorCode.ERROR_REQUEST, "请求出错：" + e.toString());
                                }
                            }

                            @Override
                            public void onResponse(String response) {
                                if (finalType == 1){
                                    baseApiAv.onResponse(response.toString());
                                }else {
                                    baseApiFg.onResponse(response.toString());
                                }
                            }
                        });
                break;
            case METHOD_POST:
                OkHttpUtils.post().url(url).params(paramsMap).build()
                        .execute(new StringCallback() {
                            @Override
                            public void onError(Call call, Exception e) {
                                if (finalType == 1){
                                    baseApiAv.onError((Activity) baseApiAv.mReference.get(), ErrorCode.ERROR_REQUEST, "请求出错：" + e.toString());
                                }else {
                                    baseApiFg.onError((Fragment) baseApiFg.mReference.get(), ErrorCode.ERROR_REQUEST, "请求出错：" + e.toString());
                                }
                            }

                            @Override
                            public void onResponse(String response) {
                                if (finalType == 1){
                                    baseApiAv.onResponse(response.toString());
                                }else {
                                    baseApiFg.onResponse(response.toString());
                                }
                            }
                        });
                break;
        }
    }

    private String parseParams(Map<String, String> params) {
        StringBuffer sb = new StringBuffer();
        Iterator<String> keys = params.keySet().iterator();
        while (keys.hasNext()) {
            String key = keys.next().toString();
            String value = params.get(key);
            if (value == null) {
                Log.e("API==the value of the key:".concat(key).concat(" is null"));
                keys.remove();
                continue;
            }
            sb.append(key).append("=").append(value).append("&");
        }
        return sb.toString();
    }
}
