package com.company.myapp.api;

import android.app.Activity;

import com.company.myapp.activity.ApiRequestTestActivity;
import com.company.myapp.api.cyp.BaseApiAvCYP;
import com.company.myapp.bean.OilPrice;
import com.ghy.baseapp.common.log.Log;

/**
 * Created by GHY on 2016/5/3.
 */
public class OilPriceApiAv extends BaseApiAvCYP<Activity, OilPrice> {


    public OilPriceApiAv(Activity activity, String province) {
        super(activity);
        putParams("province", province);
    }

    @Override
    public String getApi() {
        return APIS.OIL_PRICE;
    }

    @Override
    public Class getClazz() {
        return OilPrice.class;
    }


    @Override
    public void onSuccess(Activity activity, OilPrice oilPrice) {
        Log.i("oilPriceApi==success");
        ((ApiRequestTestActivity) activity).setOilPriceInfo("97号汽油价格：" + oilPrice.getData().getP97()
        +" 93号汽油价格："+oilPrice.getData().getP93());
    }

    @Override
    public void onError(Activity activity, int status, String msg) {
        Log.i("oilPriceApi==error");
        ((ApiRequestTestActivity) activity).setOilPriceErrorInfo("出错了-->>"+msg);
    }
}
