package com.hy.myapp.activity;

import com.hy.myapp.R;
import com.hy.baseapp.base.AbsBaseActivity;
import com.hy.baseapp.component.mydialog.MyDialog;
import com.hy.baseapp.helper.MyDialogHelper;
import com.hy.baseapp.helper.ToastHelper;

import com.hy.myapp.databinding.ActivityMyDialogTestBinding;

public class MyDialogTestActivity extends AbsBaseActivity {

    private ActivityMyDialogTestBinding binding;

    @Override
    protected int getLayoutID() {
        return R.layout.activity_my_dialog_test;
    }

    @Override
    protected String getToolBarTitle() {
        return "MyDialog对话框测试";
    }

    @Override
    protected void init() {
        binding = ActivityMyDialogTestBinding.bind(getRootView());
        binding.btnMyDialog1.setOnClickListener(v -> myDialogClick1());
        binding.btnMyDialog2.setOnClickListener(v -> myDialogClick2());
        binding.btnMyDialog3.setOnClickListener(v -> myDialogClick3());
        binding.btnMyDialog4.setOnClickListener(v -> myDialogClick4());
        binding.btnMyDialog5.setOnClickListener(v -> myDialogClick5());
        binding.btnMyDialog6.setOnClickListener(v -> myDialogClick6());
    }

    public void myDialogClick1() {
        MyDialogHelper.showDialogDefault(this, "新华社消息", "离别的天，灰色的蓝",
                new MyDialog.OnConfirmClickListener() {
                    @Override
                    public void onConfirmClick() {
                        ToastHelper.getInstance().showToast("确定啦");
                    }
                },
                new MyDialog.OnCancelClickListener() {
                    @Override
                    public void onCancelClick() {
                        ToastHelper.getInstance().showToast("取消啦");
                    }
                });
    }

    public void myDialogClick2() {
        MyDialogHelper.showDialogDefault(this, null, "离别的天，灰色的蓝",
                new MyDialog.OnConfirmClickListener() {
                    @Override
                    public void onConfirmClick() {
                        ToastHelper.getInstance().showToast("确定啦");
                    }
                },
                new MyDialog.OnCancelClickListener() {
                    @Override
                    public void onCancelClick() {
                        ToastHelper.getInstance().showToast("取消啦");
                    }
                });
    }

    public void myDialogClick3() {
        MyDialogHelper.showDialogDefault(this, "新华社消息", null,
                new MyDialog.OnConfirmClickListener() {
                    @Override
                    public void onConfirmClick() {
                        ToastHelper.getInstance().showToast("确定啦");
                    }
                },
                new MyDialog.OnCancelClickListener() {
                    @Override
                    public void onCancelClick() {
                        ToastHelper.getInstance().showToast("取消啦");
                    }
                });
    }

    public void myDialogClick4() {
        MyDialogHelper.showDialogCustom(this, "新华社消息", "离别的天，灰色的蓝", "我知道了", "好吧好吧",
                new MyDialog.OnConfirmClickListener() {
                    @Override
                    public void onConfirmClick() {
                        ToastHelper.getInstance().showToast("我知道了");
                    }
                },
                new MyDialog.OnCancelClickListener() {
                    @Override
                    public void onCancelClick() {
                        ToastHelper.getInstance().showToast("好吧好吧");
                    }
                });
    }

    public void myDialogClick5() {
        MyDialogHelper.showDialogCustom(this, "新华社消息", "离别的天，灰色的蓝", "我知道了", null,
                new MyDialog.OnConfirmClickListener() {
                    @Override
                    public void onConfirmClick() {
                        ToastHelper.getInstance().showToast("我知道了");
                    }
                }, null);
    }

    public void myDialogClick6() {
        MyDialogHelper.showDialogCustom(this, "新华社消息", "离别的天，灰色的蓝", "", "好吧好吧",
                null,
                new MyDialog.OnCancelClickListener() {
                    @Override
                    public void onCancelClick() {
                        ToastHelper.getInstance().showToast("好吧好吧");
                    }
                });
    }

}
