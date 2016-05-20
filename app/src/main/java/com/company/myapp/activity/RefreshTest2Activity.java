package com.company.myapp.activity;

import com.company.myapp.R;
import com.ghy.baseapp.base.AbsBaseActivity;

public class RefreshTest2Activity extends AbsBaseActivity {

    @Override
    protected int getLayoutID() {
        return R.layout.activity_refresh_test2;
    }

    @Override
    protected String getToolBarTitle() {
        return "下拉刷新上拉加载ListView";
    }

    @Override
    protected void init() {

    }

}
