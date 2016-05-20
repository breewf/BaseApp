package com.company.myapp.fragment;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.company.myapp.R;
import com.company.myapp.api.OilPriceApiFg;
import com.company.myapp.api.cyp.APIHelperCYP;
import com.ghy.baseapp.base.AbsBaseFragment;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * Created by HY on 2016/5/1.
 */
public class ApiRequestTestFragment extends AbsBaseFragment {

    @Bind(R.id.tv_oil_fg)
    TextView textViewOil;

    @Override
    protected int getLayoutID() {
        return R.layout.fragment_test_api_request;
    }

    @Override
    protected void init(Bundle savedInstanceState, View contentView) {

    }

    @OnClick(R.id.btn_get_oil_fg)
    public void getOliPrice(){
        APIHelperCYP.getInstance().requestApi(new OilPriceApiFg(this,"北京"));
    }

    public void setOilPriceInfo(String info){
        textViewOil.setText(info);
    }

    public void setOilPriceErrorInfo(String info){
        textViewOil.setText(info);
    }

}
