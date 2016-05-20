package com.company.myapp.fragment;

import android.os.Bundle;
import android.view.View;

import com.company.myapp.R;
import com.ghy.baseapp.base.AbsBaseFragment;
import com.ghy.baseapp.helper.DateTimeHelper;
import com.ghy.baseapp.helper.ToastHelper;

import butterknife.OnClick;

/**
 * Created by GHY on 2016/5/9.
 */
public class DateTimeFragment extends AbsBaseFragment {
    @Override
    protected int getLayoutID() {
        return R.layout.fragment_date_time;
    }

    @Override
    protected void init(Bundle savedInstanceState, View contentView) {

    }

    @OnClick(R.id.btn_data_select1)
    public void dataSelect1() {
        DateTimeHelper.showDateDialogLight(getFragmentManager(), new DateTimeHelper.OnDateConfirmClickListener() {
            @Override
            public void onConfirmClick(String date) {
                ToastHelper.getInstance().showToast("选择日期：" + date);
            }
        });
    }

    @OnClick(R.id.btn_data_select2)
    public void dataSelect2() {
        DateTimeHelper.showDateDialogLight(getFragmentManager(), "选择", "取消", new DateTimeHelper.OnDateConfirmClickListener() {
            @Override
            public void onConfirmClick(String date) {
                ToastHelper.getInstance().showToast("选择日期：" + date);
            }
        });
    }

    @OnClick(R.id.btn_data_select3)
    public void dataSelect3() {
        DateTimeHelper.showDateDialogDark(getFragmentManager(), new DateTimeHelper.OnDateConfirmClickListener() {
            @Override
            public void onConfirmClick(String date) {
                ToastHelper.getInstance().showToast("选择日期：" + date);
            }
        });
    }

    @OnClick(R.id.btn_data_select4)
    public void dataSelect4() {
        DateTimeHelper.showTimeDialogLight(getFragmentManager(), new DateTimeHelper.OnTimeConfirmClickListener() {
            @Override
            public void onConfirmClick(String date) {
                ToastHelper.getInstance().showToast("选择时间：" + date);
            }
        });
    }

    @OnClick(R.id.btn_data_select5)
    public void dataSelect5() {
        DateTimeHelper.showTimeDialogLight(getFragmentManager(), "选择", "取消", new DateTimeHelper.OnTimeConfirmClickListener() {
            @Override
            public void onConfirmClick(String date) {
                ToastHelper.getInstance().showToast("选择时间：" + date);
            }
        });
    }

    @OnClick(R.id.btn_data_select6)
    public void dataSelect6() {
        DateTimeHelper.showTimeDialogDark(getFragmentManager(), new DateTimeHelper.OnTimeConfirmClickListener() {
            @Override
            public void onConfirmClick(String date) {
                ToastHelper.getInstance().showToast("选择时间：" + date);
            }
        });
    }

}
