package com.hy.myapp.activity;

import android.widget.Button;

import com.hy.myapp.R;
import com.hy.baseapp.base.AbsBaseActivity;
import com.hy.baseapp.component.dialog.entity.DialogMenuItem;
import com.hy.baseapp.helper.DialogHelper;
import com.hy.baseapp.helper.ToastHelper;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.OnClick;

public class DialogTestActivity extends AbsBaseActivity {

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

    }

    @OnClick(R.id.btn_dialog1)
    public void dialogCilck1() {
        DialogHelper.showNormalDialogDefault(this, "我是标题", "感觉有好事要发生。。。", null, null);
    }

    @OnClick(R.id.btn_dialog2)
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


    @OnClick(R.id.btn_dialog4)
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

    @OnClick(R.id.btn_dialog5)
    public void dialogCilck5() {
        DialogHelper.showNormalDialogDefault(this, "", "没了标题，我将如何存在", null, null);
    }

    @OnClick(R.id.btn_dialog6)
    public void dialogCilck6() {
        DialogHelper.showNormalDialogDefaultCenter(this, "", "没了标题，我还是我", null, null);
    }

    @OnClick(R.id.btn_dialog8)
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

    @OnClick(R.id.btn_dialog9)
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

    @OnClick(R.id.btn_dialog10)
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

    @OnClick(R.id.btn_dialog11)
    public void dialogCilck11() {
        DialogHelper.showMaterialDialogOntBtn(this, "Material", "嗨！这是一个 MaterialDialog，希望你能喜欢", "我是一个按钮",
                new DialogHelper.OnContinueClickListener() {
                    @Override
                    public void onContinueClick() {
                        ToastHelper.getInstance().showToast("一个按钮知道啦");
                    }
                });
    }

    @OnClick(R.id.btn_dialog12)
    public void dialogCilck12() {
        DialogHelper.showMaterialDialogOntBtn(this, "", "嗨！这是一个 MaterialDialog，希望你能喜欢", "我是一个按钮", null);
    }

    @OnClick(R.id.btn_dialog13)
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


    @OnClick(R.id.btn_dialog15)
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

    @OnClick(R.id.btn_dialog16)
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

    @OnClick(R.id.btn_dialog17)
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

    @OnClick(R.id.btn_dialog18)
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

    @Bind(R.id.btn_dialog19)
    Button button19;
    @Bind(R.id.btn_dialog20)
    Button button20;

    @OnClick(R.id.btn_dialog19)
    public void dialogCilck19() {
        DialogHelper.showBubblePopup(this,R.layout.popup_bubble_text,button19);
    }

    @OnClick(R.id.btn_dialog20)
    public void dialogCilck20() {
        DialogHelper.showBubblePopup(this,R.layout.popup_bubble_image,button20);
    }

    @OnClick(R.id.btn_dialog21)
    public void dialogCilck21() {
        ToastHelper.getInstance().showToast("吐司 1");
        ToastHelper.getInstance().showToast("吐司 2");
        ToastHelper.getInstance().showToast("吐司 3");
        ToastHelper.getInstance().showToast("吐司 4");
    }

    @OnClick(R.id.btn_dialog22)
    public void dialogCilck22() {
        ToastHelper.getInstance().showSingleToast("吐司 1");
        ToastHelper.getInstance().showSingleToast("吐司 2");
        ToastHelper.getInstance().showSingleToast("吐司 3");
        ToastHelper.getInstance().showSingleToast("吐司 4");
    }

}
