package com.company.myapp.activity;

import android.widget.Button;

import com.company.myapp.R;
import com.ghy.baseapp.base.AbsBaseActivity;
import com.ghy.baseapp.helper.SnackBarHelper;
import com.ghy.baseapp.helper.ToastHelper;

import butterknife.Bind;
import butterknife.OnClick;

public class SnackBarTestActivity extends AbsBaseActivity {

    @Override
    protected int getLayoutID() {
        return R.layout.activity_snack_bar_test;
    }

    @Override
    protected String getToolBarTitle() {
        return "SnackBar弹出测试";
    }

    @Override
    protected void init() {

    }

    @Bind(R.id.btn_snack1)
    Button btnSnack1;
    @OnClick(R.id.btn_snack1)
    public void sanck1(){
        SnackBarHelper.showSnackBar(btnSnack1, "我是SnackBar", "我知道了",
                new SnackBarHelper.OnSnackBarClickListener() {
                    @Override
                    public void onClick() {
                        ToastHelper.getInstance().showToast("点击我知道了");
                    }
                });
    }

    @Bind(R.id.btn_snack2)
    Button btnSnack2;
    @OnClick(R.id.btn_snack2)
    public void sanck2(){
        SnackBarHelper.showSnackBarLong(btnSnack2, "我是SnackBar Long", "我知道了",
                new SnackBarHelper.OnSnackBarClickListener() {
                    @Override
                    public void onClick() {
                        ToastHelper.getInstance().showToast("点击我知道了");
                    }
                });
    }

    @Bind(R.id.btn_snack3)
    Button btnSnack3;
    @OnClick(R.id.btn_snack3)
    public void sanck3(){
        int color = getResources().getColor(R.color.white);
        SnackBarHelper.showSnackBarCustom(btnSnack3, "我是SnackBar Custom 显示4秒", "我知道了", 4000, color,
                new SnackBarHelper.OnSnackBarClickListener() {
                    @Override
                    public void onClick() {
                        ToastHelper.getInstance().showToast("点击我知道了");
                    }
                });
    }

    @Bind(R.id.btn_snack4)
    Button btnSnack4;
    @OnClick(R.id.btn_snack4)
    public void sanck4(){
        int color = getResources().getColor(R.color.white);
        SnackBarHelper.showSnackBarCustom(btnSnack3, "我是SnackBar Custom Short", "我知道了", color,
                new SnackBarHelper.OnSnackBarClickListener() {
                    @Override
                    public void onClick() {
                        ToastHelper.getInstance().showToast("点击我知道了");
                    }
                });
    }

}
