package com.company.myapp.api;

import android.app.Activity;

import com.company.myapp.activity.ApiRequestTestActivity;
import com.company.myapp.bean.Weather;
import com.ghy.baseapp.api.BaseApiAv;
import com.ghy.baseapp.common.log.Log;

/**
 * Created by GHY on 2016/5/3.
 */
public class WeatherApi extends BaseApiAv<Activity, Weather> {

    public WeatherApi(Activity activity) {
        super(activity);
        //设置入参
        putParams("citykey", String.valueOf(101010100));
    }

    @Override
    public String getApi() {
        return APIS.WEATHER;
    }

    @Override
    public Class<Weather> getClazz() {
        return Weather.class;
    }

    @Override
    public int getRequestMethod() {
        return REQUEST_METHOD_GET;
    }

    @Override
    public void onSuccess(Activity activity, Weather weather) {
        Log.i("weatherApi==success");
        ((ApiRequestTestActivity) activity).setWeather1Info(
                weather.getData().getCity() + " " +
                        weather.getData().getGanmao() + " " +
                        weather.getData().getWendu() + "℃");

    }

    @Override
    public void onError(Activity activity, int status, String msg) {
        Log.i("weatherApi==error" + "--errorCode " + status + " --msg " + msg);
        ((ApiRequestTestActivity) activity).setWeather1ErrorInfo("出错了-->>"+msg);
    }

}
