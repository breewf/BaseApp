package com.hy.myapp.activity;

import com.hy.myapp.R;
import com.hy.baseapp.base.AbsBaseActivity;

public class FragmentStatusActivity extends AbsBaseActivity {

    @Override
    protected int getLayoutID() {
        return R.layout.activity_test2;
    }

    @Override
    protected String getToolBarTitle() {
        return "Fragment页面状态切换测试";
    }

    @Override
    protected void init() {

    }

}
