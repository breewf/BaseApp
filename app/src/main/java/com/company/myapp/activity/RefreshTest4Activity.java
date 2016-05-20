package com.company.myapp.activity;

import com.company.myapp.R;
import com.ghy.baseapp.base.AbsBaseActivity;

public class RefreshTest4Activity extends AbsBaseActivity {

    @Override
    protected int getLayoutID() {
        return R.layout.activity_refresh_test4;
    }

    @Override
    protected String getToolBarTitle() {
        return "下拉刷新上拉加载ScrollView";
    }

    @Override
    protected void init() {

    }

}
