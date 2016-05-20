package com.company.myapp.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import com.company.myapp.R;
import com.ghy.baseapp.base.AbsBaseFragment;

/**
 * Created by HY on 2016/5/1.
 */
public class LoadingTestFragment extends AbsBaseFragment {

    @Override
    protected int getLayoutID() {
        return R.layout.fragment_loading_test;
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
                setFragmentStatus(FRAGMENT_STATUS_SUCCESS);
            }
        },2000);

        getActivity().findViewById(R.id.btn_loading_line_fg).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        loadingProgressShow();

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                loadingProgressDismiss();
                            }
                        },4000);
                    }
                }
        );

        getActivity().findViewById(R.id.btn_loading_fg).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setFragmentStatus(FRAGMENT_STATUS_LOADING);
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                setFragmentStatus(FRAGMENT_STATUS_SUCCESS);
                            }
                        },2000);
                    }
                }
        );
    }

}
