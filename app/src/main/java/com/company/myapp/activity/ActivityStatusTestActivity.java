package com.company.myapp.activity;

import android.os.Handler;
import android.view.View;

import com.company.myapp.R;
import com.ghy.baseapp.base.AbsBaseActivity;

public class ActivityStatusTestActivity extends AbsBaseActivity {


    @Override
    protected int getLayoutID() {
        return R.layout.activity_test1;
    }

    @Override
    protected String getToolBarTitle() {
        return "Activity状态切换测试页面";
    }

    @Override
    protected void init() {

        setActivityStatus(ACTIVITY_STATUS_LOADING);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                setActivityStatus(ACTIVITY_STATUS_NO_NET);
            }
        },2000);

    }

    @Override
    protected void onNoNetClick(View view) {
        super.onNoNetClick(view);
        setActivityStatus(ACTIVITY_STATUS_EMPTY);
    }

    @Override
    protected void onEmptyClick(View view) {
        super.onEmptyClick(view);
        setActivityStatus(ACTIVITY_STATUS_ERROR);
    }

    @Override
    protected void onErrorClick(View view) {
        super.onErrorClick(view);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                setActivityStatus(ACTIVITY_STATUS_SUCCESS);
            }
        }, 2000);
    }
}
