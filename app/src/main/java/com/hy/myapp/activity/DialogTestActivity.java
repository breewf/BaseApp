package com.hy.myapp.activity;

import com.hy.myapp.R;
import com.hy.baseapp.base.AbsBaseActivity;
import com.hy.baseapp.component.dialog.entity.DialogMenuItem;
import com.hy.baseapp.helper.DialogHelper;
import com.hy.baseapp.helper.ToastHelper;

import java.util.ArrayList;

import com.hy.myapp.databinding.ActivityDialogTestBinding;

public class DialogTestActivity extends AbsBaseActivity {

    private ActivityDialogTestBinding binding;

    @Override
    protected int getLayoutID() {
        return R.layout.activity_dialog_test;
    }

    @Override
    protected String getToolBarTitle() {
        return "Dialog弹框测试";
    }

    @Override
    protected void init() {
        binding = ActivityDialogTestBinding.bind(getRootView());
        binding.btnDialog1.setOnClickListener(v -> dialogCilck1());
        binding.btnDialog2.setOnClickListener(v -> dialogCilck2());
        binding.btnDialog4.setOnClickListener(v -> dialogCilck4());
        binding.btnDialog5.setOnClickListener(v -> dialogCilck5());
        binding.btnDialog6.setOnClickListener(v -> dialogCilck6());
        binding.btnDialog8.setOnClickListener(v -> dialogCilck8());
        binding.btnDialog9.setOnClickListener(v -> dialogCilck9());
        binding.btnDialog10.setOnClickListener(v -> dialogCilck10());
        binding.btnDialog11.setOnClickListener(v -> dialogCilck11());
        binding.btnDialog12.setOnClickListener(v -> dialogCilck12());
        binding.btnDialog13.setOnClickListener(v -> dialogCilck13());
        binding.btnDialog15.setOnClickListener(v -> dialogCilck15());
        binding.btnDialog16.setOnClickListener(v -> dialogCilck16());
        binding.btnDialog17.setOnClickListener(v -> dialogCilck17());
        binding.btnDialog18.setOnClickListener(v -> dialogCilck18());
        binding.btnDialog19.setOnClickListener(v -> dialogCilck19());
        binding.btnDialog20.setOnClickListener(v -> dialogCilck20());
        binding.btnDialog21.setOnClickListener(v -> dialogCilck21());
        binding.btnDialog22.setOnClickListener(v -> dialogCilck22());
    }

    public void dialogCilck1() {
        DialogHelper.showNormalDialogDefault(this, "我是标题", "感觉有好事要发生。。。", null, null);
    }

    public void dialogCilck2() {
        DialogHelper.showNormalDialogDefault(this, "粗大事了", "你敢点击按钮吗？？？", new DialogHelper.OnConfirmClickListener() {
            @Override
            public void onConfirmClick() {
                ToastHelper.getInstance().showToast("算你厉害，点击了确定");
            }
        }, new DialogHelper.OnCancelClickListener() {
            @Override
            public void onCancelClick() {
                ToastHelper.getInstance().showToast("算你厉害，点击了取消");
            }
        });
    }


    public void dialogCilck4() {
        DialogHelper.showNormalDialogDefaultCenter(this, "乖了个乖", "你敢点击按钮吗？？？", new DialogHelper.OnConfirmClickListener() {
            @Override
            public void onConfirmClick() {
                ToastHelper.getInstance().showToast("算你厉害，点击了确定");
            }
        }, new DialogHelper.OnCancelClickListener() {
            @Override
            public void onCancelClick() {
                ToastHelper.getInstance().showToast("算你厉害，点击了取消");
            }
        });
    }

    public void dialogCilck5() {
        DialogHelper.showNormalDialogDefault(this, "", "没了标题，我将如何存在", null, null);
    }

    public void dialogCilck6() {
        DialogHelper.showNormalDialogDefaultCenter(this, "", "没了标题，我还是我", null, null);
    }

    public void dialogCilck8() {
        DialogHelper.showNormalDialogThreeBtnCenter(this, "嘿嘿", "你好你好。。。。。。。。。。", "好的", "继续", "不好",
                new DialogHelper.OnConfirmClickListener() {
                    @Override
                    public void onConfirmClick() {
                        ToastHelper.getInstance().showToast("点击了好的");
                    }
                },
                new DialogHelper.OnCancelClickListener() {
                    @Override
                    public void onCancelClick() {
                        ToastHelper.getInstance().showToast("点击了不好");
                    }
                },
                new DialogHelper.OnContinueClickListener() {
                    @Override
                    public void onContinueClick() {
                        ToastHelper.getInstance().showToast("点击了继续");
                    }
                });
    }

    public void dialogCilck9() {
        DialogHelper.showMaterialDialogDefault(this, "Material", "嗨！这是一个 MaterialDialog，希望你能喜欢",
                new DialogHelper.OnConfirmClickListener() {
                    @Override
                    public void onConfirmClick() {
                        ToastHelper.getInstance().showToast("点击了确定");
                    }
                },
                new DialogHelper.OnCancelClickListener() {
                    @Override
                    public void onCancelClick() {
                        ToastHelper.getInstance().showToast("点击了取消");
                    }
                });
    }

    public void dialogCilck10() {
        DialogHelper.showMaterialDialogDefault(this, "Material", "嗨！这是一个 MaterialDialog，希望你能喜欢", "我很喜欢", "我不喜欢",
                new DialogHelper.OnConfirmClickListener() {
                    @Override
                    public void onConfirmClick() {
                        ToastHelper.getInstance().showToast("我很喜欢");
                    }
                },
                new DialogHelper.OnCancelClickListener() {
                    @Override
                    public void onCancelClick() {
                        ToastHelper.getInstance().showToast("我不喜欢");
                    }
                });
    }

    public void dialogCilck11() {
        DialogHelper.showMaterialDialogOntBtn(this, "Material", "嗨！这是一个 MaterialDialog，希望你能喜欢", "我是一个按钮",
                new DialogHelper.OnContinueClickListener() {
                    @Override
                    public void onContinueClick() {
                        ToastHelper.getInstance().showToast("一个按钮知道啦");
                    }
                });
    }

    public void dialogCilck12() {
        DialogHelper.showMaterialDialogOntBtn(this, "", "嗨！这是一个 MaterialDialog，希望你能喜欢", "我是一个按钮", null);
    }

    public void dialogCilck13() {
        DialogHelper.showMaterialDialogThreeBtn(this, "三个按钮", "我是三个按钮的MaterialDialog，希望你能喜欢", "确定", "继续", "取消",
                new DialogHelper.OnConfirmClickListener() {
                    @Override
                    public void onConfirmClick() {
                        ToastHelper.getInstance().showToast("确定");
                    }
                },
                new DialogHelper.OnCancelClickListener() {
                    @Override
                    public void onCancelClick() {
                        ToastHelper.getInstance().showToast("取消");
                    }
                },
                new DialogHelper.OnContinueClickListener() {
                    @Override
                    public void onContinueClick() {
                        ToastHelper.getInstance().showToast("继续");
                    }
                });
    }


    public void dialogCilck15() {
        final ArrayList<DialogMenuItem> mMenuItems = new ArrayList<>();
        mMenuItems.add(new DialogMenuItem("收藏", R.mipmap.ic_winstyle_favor));
        mMenuItems.add(new DialogMenuItem("下载", R.mipmap.ic_winstyle_download));
        mMenuItems.add(new DialogMenuItem("分享", R.mipmap.ic_winstyle_share));
        mMenuItems.add(new DialogMenuItem("删除", R.mipmap.ic_winstyle_delete));
        mMenuItems.add(new DialogMenuItem("歌手", R.mipmap.ic_winstyle_artist));
        mMenuItems.add(new DialogMenuItem("专辑", R.mipmap.ic_winstyle_album));
        DialogHelper.showNormalListDialog(this, "请选择", mMenuItems,
                new DialogHelper.OnItemClickListener() {
                    @Override
                    public void onItemClick(int position) {
                        ToastHelper.getInstance().showToast("选择了：" + mMenuItems.get(position).mOperName);
                    }
                });
    }

    public void dialogCilck16() {
        final ArrayList<DialogMenuItem> mMenuItems = new ArrayList<>();
        mMenuItems.add(new DialogMenuItem("收藏", R.mipmap.ic_winstyle_favor));
        mMenuItems.add(new DialogMenuItem("下载", R.mipmap.ic_winstyle_download));
        mMenuItems.add(new DialogMenuItem("分享", R.mipmap.ic_winstyle_share));
        mMenuItems.add(new DialogMenuItem("删除", R.mipmap.ic_winstyle_delete));
        mMenuItems.add(new DialogMenuItem("歌手", R.mipmap.ic_winstyle_artist));
        mMenuItems.add(new DialogMenuItem("专辑", R.mipmap.ic_winstyle_album));
        DialogHelper.showNormalListDialogCustom(this, "请选择", mMenuItems,
                new DialogHelper.OnItemClickListener() {
                    @Override
                    public void onItemClick(int position) {
                        ToastHelper.getInstance().showToast("选择了：" + mMenuItems.get(position).mOperName);
                    }
                });
    }

    public void dialogCilck17() {
        final String[] stringItems = {"接收消息并提醒", "接收消息但不提醒", "收进群助手且不提醒", "屏蔽群消息" };
        DialogHelper.showActionSheetDialog(this, "选择群消息提醒方式\r\n(该群在电脑的设置:接收消息并提醒)", stringItems,
                new DialogHelper.OnItemClickListener() {
                    @Override
                    public void onItemClick(int position) {
                        ToastHelper.getInstance().showToast("选择了：" + stringItems[position]);
                    }
                });
    }

    public void dialogCilck18() {
        final String[] stringItems = {"版本更新", "帮助与反馈", "退出QQ" };
        DialogHelper.showActionSheetDialog(this, "", stringItems,
                new DialogHelper.OnItemClickListener() {
                    @Override
                    public void onItemClick(int position) {
                        ToastHelper.getInstance().showToast("选择了：" + stringItems[position]);
                    }
                });
    }

    public void dialogCilck19() {
        DialogHelper.showBubblePopup(this,R.layout.popup_bubble_text,binding.btnDialog19);
    }

    public void dialogCilck20() {
        DialogHelper.showBubblePopup(this,R.layout.popup_bubble_image,binding.btnDialog20);
    }

    public void dialogCilck21() {
        ToastHelper.getInstance().showToast("吐司 1");
        ToastHelper.getInstance().showToast("吐司 2");
        ToastHelper.getInstance().showToast("吐司 3");
        ToastHelper.getInstance().showToast("吐司 4");
    }

    public void dialogCilck22() {
        ToastHelper.getInstance().showSingleToast("吐司 1");
        ToastHelper.getInstance().showSingleToast("吐司 2");
        ToastHelper.getInstance().showSingleToast("吐司 3");
        ToastHelper.getInstance().showSingleToast("吐司 4");
    }

}
