package com.hy.myapp.activity;

import com.hy.myapp.R;
import com.hy.baseapp.base.AbsBaseActivity;
import com.hy.baseapp.helper.SnackBarHelper;
import com.hy.baseapp.helper.ToastHelper;

import com.hy.myapp.databinding.ActivitySnackBarTestBinding;

public class SnackBarTestActivity extends AbsBaseActivity {

    private ActivitySnackBarTestBinding binding;

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
        binding = ActivitySnackBarTestBinding.bind(getRootView());
        binding.btnSnack1.setOnClickListener(v -> sanck1());
        binding.btnSnack2.setOnClickListener(v -> sanck2());
        binding.btnSnack3.setOnClickListener(v -> sanck3());
        binding.btnSnack4.setOnClickListener(v -> sanck4());
    }

    public void sanck1(){
        SnackBarHelper.showSnackBar(binding.btnSnack1, "我是SnackBar", "我知道了",
                new SnackBarHelper.OnSnackBarClickListener() {
                    @Override
                    public void onClick() {
                        ToastHelper.getInstance().showToast("点击我知道了");
                    }
                });
    }

    public void sanck2(){
        SnackBarHelper.showSnackBarLong(binding.btnSnack2, "我是SnackBar Long", "我知道了",
                new SnackBarHelper.OnSnackBarClickListener() {
                    @Override
                    public void onClick() {
                        ToastHelper.getInstance().showToast("点击我知道了");
                    }
                });
    }

    public void sanck3(){
        int color = getResources().getColor(R.color.white);
        SnackBarHelper.showSnackBarCustom(binding.btnSnack3, "我是SnackBar Custom 显示4秒", "我知道了", 4000, color,
                new SnackBarHelper.OnSnackBarClickListener() {
                    @Override
                    public void onClick() {
                        ToastHelper.getInstance().showToast("点击我知道了");
                    }
                });
    }

    public void sanck4(){
        int color = getResources().getColor(R.color.white);
        SnackBarHelper.showSnackBarCustom(binding.btnSnack3, "我是SnackBar Custom Short", "我知道了", color,
                new SnackBarHelper.OnSnackBarClickListener() {
                    @Override
                    public void onClick() {
                        ToastHelper.getInstance().showToast("点击我知道了");
                    }
                });
    }

}
