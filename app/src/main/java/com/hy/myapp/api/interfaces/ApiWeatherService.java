package com.hy.myapp.api.interfaces;

import com.hy.myapp.bean.WeatherBeanRetrofit;

import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

/**
 * api接口示例
 * Created by GHY on 2016/5/3.
 * 完整路径为：http://wthrcdn.etouch.cn/weather_mini?citykey=101010100
 * baseUrl为：http://wthrcdn.etouch.cn/
 */
public interface ApiWeatherService {

    @GET("weather_mini")
    Observable<WeatherBeanRetrofit> getWeatherInfo(@Query("citykey") String cityKey);

}
