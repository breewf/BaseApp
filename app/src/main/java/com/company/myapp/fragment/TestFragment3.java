package com.company.myapp.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import com.company.myapp.R;
import com.ghy.baseapp.base.AbsBaseFragment;

/**
 * Created by HY on 2016/5/1.
 */
public class TestFragment3 extends AbsBaseFragment {

    @Override
    protected int getLayoutID() {
        return R.layout.fragment_test3;
    }

    @Override
    protected void init(Bundle savedInstanceState, View contentView) {

        setFragmentStatus(FRAGMENT_STATUS_LOADING);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                setFragmentStatus(FRAGMENT_STATUS_EMPTY);
            }
        },2000);
    }


    @Override
    protected void onEmptyClick(View view) {
        super.onEmptyClick(view);
        setFragmentStatus(FRAGMENT_STATUS_LOADING);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                setFragmentStatus(FRAGMENT_STATUS_SUCCESS);
            }
        },2000);
    }
}
