package com.company.myapp.activity;

import com.company.myapp.R;
import com.ghy.baseapp.base.AbsBaseActivity;

public class RefreshTest1Activity extends AbsBaseActivity {

    @Override
    protected int getLayoutID() {
        return R.layout.activity_refresh_test1;
    }

    @Override
    protected String getToolBarTitle() {
        return "谷歌下拉刷新ListView";
    }

    @Override
    protected void init() {

    }

}
