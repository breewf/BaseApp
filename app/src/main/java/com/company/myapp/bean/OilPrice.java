package com.company.myapp.bean;

import com.company.myapp.api.cyp.ResponseDataCYP;

/**
 * 油价
 * Created by GHY on 2016/2/17.
 */
public class OilPrice extends ResponseDataCYP{

    private OilPriceInfo data;

    public OilPriceInfo getData() {
        return data;
    }

    public void setData(OilPriceInfo data) {
        this.data = data;
    }
}
