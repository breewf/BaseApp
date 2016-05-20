package com.company.myapp.bean;

import com.ghy.baseapp.api.ResponseData;

/**
 * Created by GHY on 2016/5/3.
 */
public class Weather extends ResponseData {

    WeatherBean data;

    public WeatherBean getData() {
        return data;
    }

    public void setData(WeatherBean data) {
        this.data = data;
    }
}
