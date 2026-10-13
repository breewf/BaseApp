package com.hy.myapp.activity;

import android.os.Handler;

import com.hy.myapp.R;
import com.hy.baseapp.base.AbsBaseActivity;

import com.hy.myapp.databinding.ActivityLoadingTestBinding;

public class LoadingTestActivity extends AbsBaseActivity {

    private ActivityLoadingTestBinding binding;

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
        binding = ActivityLoadingTestBinding.bind(getRootView());
        binding.btnLoading1.setOnClickListener(v -> btnLoading1());
        binding.btnLoading2.setOnClickListener(v -> btnLoading2());
    }

    public void btnLoading1(){
        loadingProgressShow();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                loadingProgressDismiss();
            }
        },2000);
    }

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
