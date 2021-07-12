package com.hy.myapp.activity;

import android.os.Handler;

import com.hy.myapp.R;
import com.hy.baseapp.base.AbsBaseActivity;

import butterknife.OnClick;

public class LoadingTestActivity extends AbsBaseActivity {

    @Override
    protected int getLayoutID() {
        return R.layout.activity_loading_test;
    }

    @Override
    protected String getToolBarTitle() {
        return "loading加载中测试";
    }

    @Override
    protected void init() {
    }

    @OnClick(R.id.btn_loading1)
    public void btnLoading1(){
        loadingProgressShow();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                loadingProgressDismiss();
            }
        },2000);
    }

    @OnClick(R.id.btn_loading2)
    public void btnLoading2(){

        setActivityStatus(ACTIVITY_STATUS_LOADING);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                setActivityStatus(ACTIVITY_STATUS_SUCCESS);
            }
        },2000);
    }

}
