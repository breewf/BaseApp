package com.company.myapp.activity;

import android.widget.TextView;

import com.company.myapp.R;
import com.company.myapp.api.OilPriceApiAv;
import com.company.myapp.api.WeatherApi;
import com.company.myapp.api.cyp.APIHelperCYP;
import com.company.myapp.api.interfaces.ApiWeatherService;
import com.company.myapp.bean.WeatherBeanRetrofit;
import com.ghy.baseapp.api.APIHelper;
import com.ghy.baseapp.api.RetrofitHelper;
import com.ghy.baseapp.base.AbsBaseActivity;
import com.ghy.baseapp.common.log.Log;
import com.ghy.baseapp.helper.ToastHelper;

import butterknife.Bind;
import butterknife.OnClick;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class ApiRequestTestActivity extends AbsBaseActivity {

    @Bind(R.id.tv_weather1)
    TextView textView1;
    @Bind(R.id.tv_oil)
    TextView textViewOil;
    @Bind(R.id.tv_weather2)
    TextView textView2;

    @Override
    protected int getLayoutID() {
        return R.layout.activity_api_request_test;
    }

    @Override
    protected String getToolBarTitle() {
        return "Api请求测试页面";
    }

    @Override
    protected void init() {
    }

    @OnClick(R.id.btn_get_weather1)
    public void getWeather1() {
        //activity中请求天气数据Get
        APIHelper.getInstance().requestApi(new WeatherApi(this));
    }

    @OnClick(R.id.btn_get_oil)
    public void getOil() {
        //activity中请求油价数据Post
        APIHelperCYP.getInstance().requestApi(new OilPriceApiAv(this, "北京"));
    }

    @OnClick(R.id.btn_get_weather2)
    public void getWeather2() {
        //使用Retrofit中请求天气数据
        retrofitRequest();
    }

    /**
     * 使用retrofit请求
     */
    private void retrofitRequest() {
        ApiWeatherService weatherService = RetrofitHelper.getRetrofit().create(ApiWeatherService.class);
        Observable<WeatherBeanRetrofit> observable = weatherService.getWeatherInfo("101010100");
        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<WeatherBeanRetrofit>() {
                    @Override
                    public void onCompleted() {
                        Log.i("RxJava----", "onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("RxJava----", "onError" + e.toString());
                        ToastHelper.getInstance().showToast("请求出错");
                    }

                    @Override
                    public void onNext(WeatherBeanRetrofit weatherBean) {
                        textView2.setText(weatherBean.getData().getGanmao());
                    }
                });
    }

    public void setWeather1Info(String info) {
        textView1.setText(info);
    }

    public void setOilPriceInfo(String info) {
        textViewOil.setText(info);
    }

    public void setOilPriceErrorInfo(String info) {
        textViewOil.setText(info);
    }

    public void setWeather1ErrorInfo(String info) {
        textView1.setText(info);
    }
}
