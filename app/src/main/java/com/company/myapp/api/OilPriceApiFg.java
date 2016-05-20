package com.company.myapp.api;


import android.support.v4.app.Fragment;

import com.company.myapp.api.cyp.BaseApiFgCYP;
import com.company.myapp.bean.OilPrice;
import com.company.myapp.fragment.ApiRequestTestFragment;
import com.ghy.baseapp.common.log.Log;

/**
 * Created by GHY on 2016/5/3.
 */
public class OilPriceApiFg extends BaseApiFgCYP<Fragment, OilPrice> {


    public OilPriceApiFg(Fragment fragment, String province) {
        super(fragment);
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
    public void onSuccess(Fragment fragment, OilPrice oilPrice) {
        Log.i("oilPriceApi==success");
        ((ApiRequestTestFragment) fragment).setOilPriceInfo("97号汽油价格：" + oilPrice.getData().getP97()
        +" 0号柴油价格："+oilPrice.getData().getP0());
    }

    @Override
    public void onError(Fragment fragment, int status, String msg) {
        Log.i("oilPriceApi==error");
        ((ApiRequestTestFragment) fragment).setOilPriceErrorInfo("出错了-->>"+msg);
    }
}
