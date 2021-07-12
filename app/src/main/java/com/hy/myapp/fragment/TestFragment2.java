package com.hy.myapp.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import com.hy.myapp.R;
import com.hy.baseapp.base.AbsBaseFragment;

/**
 * Created by HY on 2016/5/1.
 */
public class TestFragment2 extends AbsBaseFragment {

    @Override
    protected int getLayoutID() {
        return R.layout.fragment_test2;
    }

    @Override
    protected int setLoadingProgressType() {
        return LOADING_PROGRESS_TYPE_LINE;
    }

    @Override
    protected void init(Bundle savedInstanceState, View contentView) {

        setFragmentStatus(FRAGMENT_STATUS_LOADING);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                setFragmentStatus(FRAGMENT_STATUS_ERROR);
            }
        },2000);
    }


    @Override
    protected void onErrorClick(View view) {
        super.onErrorClick(view);
        setFragmentStatus(FRAGMENT_STATUS_SUCCESS);
    }
}
