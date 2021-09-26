package com.hy.baseapp.helper;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;

import com.hy.baseapp.component.dialog.animation.BounceEnter.BounceLeftEnter;
import com.hy.baseapp.component.dialog.animation.SlideExit.SlideRightExit;
import com.hy.baseapp.component.dialog.entity.DialogMenuItem;
import com.hy.baseapp.component.dialog.widget.ActionSheetDialog;
import com.hy.baseapp.component.dialog.widget.MaterialDialog;
import com.hy.baseapp.component.dialog.widget.NormalDialog;
import com.hy.baseapp.component.dialog.widget.NormalListDialog;
import com.hy.baseapp.component.dialog.widget.popup.BubblePopup;
import com.hy.baseapp.global.TitleInfo;

import java.util.ArrayList;

/**
 * Created by hy on 2016/5/4.
 * Dialog助手类
 */
public class DialogHelper {

    /**
     * 确定按钮
     */
    public interface OnConfirmClickListener {
        void onConfirmClick();
    }

    /**
     * 取消按钮
     */
    public interface OnCancelClickListener {
        void onCancelClick();
    }

    /**
     * 继续按钮
     * 中间按钮
     */
    public interface OnContinueClickListener {
        void onContinueClick();
    }

    /**
     * ListItem点击事件
     */
    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    /**
     * 显示normal样式对话框 标题和文字居左
     * 两个按钮 确认和取消
     *
     * @param context
     * @param title
     * @param content
     * @param confirmListener 确认按钮监听
     * @param cancelListener  取消按钮监听
     */
    public static void showNormalDialogDefault(Context context, String title, String content, final OnConfirmClickListener confirmListener, final OnCancelClickListener cancelListener) {
        if (context == null) {
            return;
        }
        final NormalDialog dialog = new NormalDialog(context);
        boolean isTitleShow = true;
        if (TextUtils.isEmpty(title)) {
            isTitleShow = false;
        }
        if (!isTitleShow) {
            dialog.isTitleShow(isTitleShow);
        } else {
            dialog.title(title);
        }
        dialog.content(content)
                .btnText(TitleInfo.BTN_CONFIRM, TitleInfo.BTN_CANCEL)
                //.showAnim(new FadeEnter())
                //.dismissAnim(new FadeExit())
                .show();
        dialog.setOnBtnClickL(
                () -> {
                    if (confirmListener != null) {
                        confirmListener.onConfirmClick();
                    }
                    dialog.dismiss();
                },
                () -> {
                    if (cancelListener != null) {
                        cancelListener.onCancelClick();
                    }
                    dialog.dismiss();
                });
    }

    /**
     * 显示normal样式对话框 标题和文字居左
     * 自定义两个按钮文本
     *
     * @param context
     * @param title
     * @param content
     * @param confirmListener 确认按钮监听
     * @param cancelListener  取消按钮监听
     */
    public static void showNormalDialogDefault(Context context, String title, String content, String confirm, String cancel, final OnConfirmClickListener confirmListener, final OnCancelClickListener cancelListener) {
        if (context == null) {
            return;
        }
        final NormalDialog dialog = new NormalDialog(context);
        boolean isTitleShow = true;
        if (TextUtils.isEmpty(title)) {
            isTitleShow = false;
        }
        if (!isTitleShow) {
            dialog.isTitleShow(isTitleShow);
        } else {
            dialog.title(title);
        }
        dialog.content(content)
                .btnText(confirm, cancel)
                //.showAnim(new FadeEnter())
                //.dismissAnim(new FadeExit())
                .show();
        dialog.setOnBtnClickL(
                () -> {
                    if (confirmListener != null) {
                        confirmListener.onConfirmClick();
                    }
                    dialog.dismiss();
                },
                () -> {
                    if (cancelListener != null) {
                        cancelListener.onCancelClick();
                    }
                    dialog.dismiss();
                });
    }

    /**
     * 显示normal样式对话框 标题和文字居中
     * 两个按钮 确认和取消
     *
     * @param context
     * @param title
     * @param content
     * @param confirmListener 确认按钮监听
     * @param cancelListener  取消按钮监听
     */
    public static void showNormalDialogDefaultCenter(Context context, String title, String content, final OnConfirmClickListener confirmListener, final OnCancelClickListener cancelListener) {
        if (context == null) {
            return;
        }
        final NormalDialog dialog = new NormalDialog(context);
        boolean isTitleShow = true;
        if (TextUtils.isEmpty(title)) {
            isTitleShow = false;
        }
        if (!isTitleShow) {
            dialog.isTitleShow(isTitleShow);
        } else {
            dialog.title(title);
        }
        dialog.content(content)
                .style(NormalDialog.STYLE_TWO)
                .btnText(TitleInfo.BTN_CONFIRM, TitleInfo.BTN_CANCEL)
                //.showAnim(new FadeEnter())
                //.dismissAnim(new FadeExit())
                .show();
        dialog.setOnBtnClickL(
                () -> {
                    if (confirmListener != null) {
                        confirmListener.onConfirmClick();
                    }
                    dialog.dismiss();
                },
                () -> {
                    if (cancelListener != null) {
                        cancelListener.onCancelClick();
                    }
                    dialog.dismiss();
                });
    }

    /**
     * 显示normal样式对话框 标题和文字居中
     * 自定义两个按钮文本
     *
     * @param context
     * @param title
     * @param content
     * @param confirmListener 确认按钮监听
     * @param cancelListener  取消按钮监听
     */
    public static void showNormalDialogDefaultCenter(Context context, String title, String content, String confirm, String cancel, final OnConfirmClickListener confirmListener, final OnCancelClickListener cancelListener) {
        if (context == null) {
            return;
        }
        final NormalDialog dialog = new NormalDialog(context);
        boolean isTitleShow = true;
        if (TextUtils.isEmpty(title)) {
            isTitleShow = false;
        }
        if (!isTitleShow) {
            dialog.isTitleShow(isTitleShow);
        } else {
            dialog.title(title);
        }
        dialog.content(content)
                .style(NormalDialog.STYLE_TWO)
                .btnText(confirm, cancel)
                //.showAnim(new FadeEnter())
                //.dismissAnim(new FadeExit())
                .show();
        dialog.setOnBtnClickL(
                () -> {
                    if (confirmListener != null) {
                        confirmListener.onConfirmClick();
                    }
                    dialog.dismiss();
                },
                () -> {
                    if (cancelListener != null) {
                        cancelListener.onCancelClick();
                    }
                    dialog.dismiss();
                });
    }


    /**
     * 只有一个button 标题和文字居左
     *
     * @param context
     * @param title
     * @param content
     * @param continueListener 继续按钮监听
     */
    public static void showNormalDialogOneBtn(Context context, String title, String content, String btnDes, final OnContinueClickListener continueListener) {
        if (context == null) {
            return;
        }
        final NormalDialog dialog = new NormalDialog(context);
        boolean isTitleShow = true;
        if (TextUtils.isEmpty(title)) {
            isTitleShow = false;
        }
        if (!isTitleShow) {
            dialog.isTitleShow(isTitleShow);
        } else {
            dialog.title(title);
        }
        dialog.content(content)
                .btnNum(1)
                .btnText(btnDes)
                //.showAnim(new FadeEnter())
                //.dismissAnim(new FadeExit())
                .show();
        dialog.setOnBtnClickL(
                () -> {
                    if (continueListener != null) {
                        continueListener.onContinueClick();
                    }
                    dialog.dismiss();
                });
    }

    /**
     * 只有一个button 标题和文字居中
     *
     * @param context
     * @param title
     * @param content
     * @param continueListener 继续按钮监听
     */
    public static void showNormalDialogOneBtnCenter(Context context, String title, String content, String btnDes, final OnContinueClickListener continueListener) {
        if (context == null) {
            return;
        }
        final NormalDialog dialog = new NormalDialog(context);
        boolean isTitleShow = true;
        if (TextUtils.isEmpty(title)) {
            isTitleShow = false;
        }
        if (!isTitleShow) {
            dialog.isTitleShow(isTitleShow);
        } else {
            dialog.title(title);
        }
        dialog.content(content)
                .style(NormalDialog.STYLE_TWO)
                .btnNum(1)
                .btnText(btnDes)
                //.showAnim(new FadeEnter())
                //.dismissAnim(new FadeExit())
                .show();
        dialog.setOnBtnClickL(
                () -> {
                    if (continueListener != null) {
                        continueListener.onContinueClick();
                    }
                    dialog.dismiss();
                });
    }

    /**
     * 有三个button 标题和文字居左
     *
     * @param context
     * @param title
     * @param content
     * @param confirmListener  确定按钮监听
     * @param continueListener 继续按钮监听
     * @param cancelListener   取消按钮监听
     */
    public static void showNormalDialogThreeBtn(Context context, String title, String content, String confirm, String con, String cancel,
                                                final OnConfirmClickListener confirmListener, final OnCancelClickListener cancelListener, final OnContinueClickListener continueListener) {
        if (context == null) {
            return;
        }
        final NormalDialog dialog = new NormalDialog(context);
        boolean isTitleShow = true;
        if (TextUtils.isEmpty(title)) {
            isTitleShow = false;
        }
        if (!isTitleShow) {
            dialog.isTitleShow(isTitleShow);
        } else {
            dialog.title(title);
        }
        dialog.content(content)
                .btnNum(3)
                .btnText(confirm, cancel, con)
                //.showAnim(new FadeEnter())
                //.dismissAnim(new FadeExit())
                .show();
        dialog.setOnBtnClickL(
                () -> {
                    if (confirmListener != null) {
                        confirmListener.onConfirmClick();
                    }
                    dialog.dismiss();
                },
                () -> {
                    if (cancelListener != null) {
                        cancelListener.onCancelClick();
                    }
                    dialog.dismiss();
                },
                () -> {
                    if (continueListener != null) {
                        continueListener.onContinueClick();
                    }
                    dialog.dismiss();
                });
    }

    /**
     * 有三个button 标题和文字居中
     *
     * @param context
     * @param title
     * @param content
     * @param confirmListener  确定按钮监听
     * @param continueListener 继续按钮监听
     * @param cancelListener   取消按钮监听
     */
    public static void showNormalDialogThreeBtnCenter(Context context, String title, String content, String confirm, String con, String cancel,
                                                      final OnConfirmClickListener confirmListener, final OnCancelClickListener cancelListener, final OnContinueClickListener continueListener) {
        if (context == null) {
            return;
        }
        final NormalDialog dialog = new NormalDialog(context);
        boolean isTitleShow = true;
        if (TextUtils.isEmpty(title)) {
            isTitleShow = false;
        }
        if (!isTitleShow) {
            dialog.isTitleShow(isTitleShow);
        } else {
            dialog.title(title);
        }
        dialog.content(content)
                .style(NormalDialog.STYLE_TWO)
                .btnNum(3)
                .btnText(confirm, cancel, con)
                //.showAnim(new FadeEnter())
                //.dismissAnim(new FadeExit())
                .show();
        dialog.setOnBtnClickL(
                () -> {
                    if (confirmListener != null) {
                        confirmListener.onConfirmClick();
                    }
                    dialog.dismiss();
                },
                () -> {
                    if (cancelListener != null) {
                        cancelListener.onCancelClick();
                    }
                    dialog.dismiss();
                },
                () -> {
                    if (continueListener != null) {
                        continueListener.onContinueClick();
                    }
                    dialog.dismiss();
                });
    }

    /**
     * 显示Material风格Dialog
     * 默认 显示取消和确定按钮
     *
     * @param context
     * @param title
     * @param content
     * @param confirmListener
     * @param cancelListener
     */
    public static void showMaterialDialogDefault(Context context, String title, String content, final OnConfirmClickListener confirmListener, final OnCancelClickListener cancelListener) {
        if (context == null) {
            return;
        }
        final MaterialDialog dialog = new MaterialDialog(context);
        boolean isTitleShow = true;
        if (TextUtils.isEmpty(title)) {
            isTitleShow = false;
        }
        if (!isTitleShow) {
            dialog.isTitleShow(isTitleShow);
        } else {
            dialog.title(title);
        }
        dialog.content(content)
                .btnText(TitleInfo.BTN_CANCEL, TitleInfo.BTN_CONFIRM)
                //.showAnim(new FadeEnter())
                //.dismissAnim(new FadeExit())
                .show();
        dialog.setOnBtnClickL(
                () -> {
                    if (cancelListener != null) {
                        cancelListener.onCancelClick();
                    }
                    dialog.dismiss();
                },
                () -> {
                    if (confirmListener != null) {
                        confirmListener.onConfirmClick();
                    }
                    dialog.dismiss();
                });
    }

    /**
     * 显示Material风格Dialog
     * 自定义按钮文本
     *
     * @param context
     * @param title
     * @param content
     * @param confirm
     * @param cancel
     * @param confirmListener
     * @param cancelListener
     */
    public static void showMaterialDialogDefault(Context context, String title, String content, String confirm, String cancel, final OnConfirmClickListener confirmListener, final OnCancelClickListener cancelListener) {
        if (context == null) {
            return;
        }
        final MaterialDialog dialog = new MaterialDialog(context);
        boolean isTitleShow = true;
        if (TextUtils.isEmpty(title)) {
            isTitleShow = false;
        }
        if (!isTitleShow) {
            dialog.isTitleShow(isTitleShow);
        } else {
            dialog.title(title);
        }
        dialog.content(content)
                .btnText(cancel, confirm)
                //.showAnim(new FadeEnter())
                //.dismissAnim(new FadeExit())
                .show();
        dialog.setOnBtnClickL(
                () -> {
                    if (cancelListener != null) {
                        cancelListener.onCancelClick();
                    }
                    dialog.dismiss();
                },
                () -> {
                    if (confirmListener != null) {
                        confirmListener.onConfirmClick();
                    }
                    dialog.dismiss();
                });
    }

    /**
     * 显示Material风格Dialog
     * 只有一个按钮
     *
     * @param context
     * @param title
     * @param content
     * @param continueListener
     */
    public static void showMaterialDialogOntBtn(Context context, String title, String content, String btnDes, final OnContinueClickListener continueListener) {
        if (context == null) {
            return;
        }
        final MaterialDialog dialog = new MaterialDialog(context);
        boolean isTitleShow = true;
        if (TextUtils.isEmpty(title)) {
            isTitleShow = false;
        }
        if (!isTitleShow) {
            dialog.isTitleShow(isTitleShow);
        } else {
            dialog.title(title);
        }
        dialog.content(content)
                .btnNum(1)
                .btnText(btnDes)
                //.showAnim(new FadeEnter())
                //.dismissAnim(new FadeExit())
                .show();
        dialog.setOnBtnClickL(
                () -> {
                    if (continueListener != null) {
                        continueListener.onContinueClick();
                    }
                    dialog.dismiss();
                });
    }

    /**
     * 显示Material风格Dialog
     * 三个按钮
     *
     * @param context
     * @param title
     * @param content
     * @param confirm
     * @param cancel
     * @param confirmListener
     * @param cancelListener
     */
    public static void showMaterialDialogThreeBtn(Context context, String title, String content, String confirm, String con, String cancel, final OnConfirmClickListener confirmListener, final OnCancelClickListener cancelListener, final OnContinueClickListener continueListener) {
        if (context == null) {
            return;
        }
        final MaterialDialog dialog = new MaterialDialog(context);
        boolean isTitleShow = true;
        if (TextUtils.isEmpty(title)) {
            isTitleShow = false;
        }
        if (!isTitleShow) {
            dialog.isTitleShow(isTitleShow);
        } else {
            dialog.title(title);
        }
        dialog.content(content)
                .btnNum(3)
                .btnText(cancel, confirm, con)
                //.showAnim(new FadeEnter())
                //.dismissAnim(new FadeExit())
                .show();
        dialog.setOnBtnClickL(
                () -> {
                    if (cancelListener != null) {
                        cancelListener.onCancelClick();
                    }
                    dialog.dismiss();
                },
                () -> {
                    if (confirmListener != null) {
                        confirmListener.onConfirmClick();
                    }
                    dialog.dismiss();
                },
                () -> {
                    if (continueListener != null) {
                        continueListener.onContinueClick();
                    }
                    dialog.dismiss();
                });
    }


    /**
     * 默认List样式
     *
     * @param context
     * @param title
     * @param menuItems
     * @param itemClickListener
     */
    public static void showNormalListDialog(Context context, String title, ArrayList<DialogMenuItem> menuItems, final OnItemClickListener itemClickListener) {
        if (context == null) {
            return;
        }
        final NormalListDialog dialog = new NormalListDialog(context, menuItems);
        boolean isTitleShow = true;
        if (TextUtils.isEmpty(title)) {
            isTitleShow = false;
        }
        if (!isTitleShow) {
            dialog.isTitleShow(isTitleShow);
        } else {
            dialog.title(title);
        }
        dialog.
                show();
        dialog.setOnOperItemClickL((parent, view, position, id) -> {
            if (itemClickListener != null) {
                itemClickListener.onItemClick(position);
            }
            dialog.dismiss();
        });
    }

    /**
     * 自定义List样式
     *
     * @param context
     * @param title
     * @param menuItems
     * @param itemClickListener
     */
    public static void showNormalListDialogCustom(Context context, String title, ArrayList<DialogMenuItem> menuItems, final OnItemClickListener itemClickListener) {
        if (context == null) {
            return;
        }
        final NormalListDialog dialog = new NormalListDialog(context, menuItems);
        boolean isTitleShow = true;
        if (TextUtils.isEmpty(title)) {
            isTitleShow = false;
        }
        if (!isTitleShow) {
            dialog.isTitleShow(isTitleShow);
        } else {
            dialog.title(title);
        }
        dialog.titleTextSize_SP(18)
                .titleBgColor(Color.parseColor("#409ED7"))
                .itemPressColor(Color.parseColor("#85D3EF"))
                .itemTextColor(Color.parseColor("#303030"))
                .itemTextSize(14)
                .cornerRadius(10)
                .widthScale(0.8f)
                .show();
        dialog.setOnOperItemClickL((parent, view, position, id) -> {
            if (itemClickListener != null) {
                itemClickListener.onItemClick(position);
            }
            dialog.dismiss();
        });
    }

    /**
     * QQ底部弹框样式
     *
     * @param context
     * @param title
     * @param stringItems
     * @param itemClickListener
     */
    public static void showActionSheetDialog(Context context, String title, String[] stringItems, final OnItemClickListener itemClickListener) {
        if (context == null) {
            return;
        }
        final ActionSheetDialog dialog = new ActionSheetDialog(context, stringItems, null);
        boolean isTitleShow = true;
        if (TextUtils.isEmpty(title)) {
            isTitleShow = false;
        }
        if (!isTitleShow) {
            dialog.isTitleShow(isTitleShow);
        } else {
            dialog.title(title);
        }
        dialog.titleTextSize_SP(14.5f)
                .show();
        dialog.setOnOperItemClickL((parent, view, position, id) -> {
            if (itemClickListener != null) {
                itemClickListener.onItemClick(position);
            }
            dialog.dismiss();
        });
    }

    /**
     * 在点击View的位置弹出提示对话框
     *
     * @param context
     * @param layoutId
     * @param dependView
     */
    public static void showBubblePopup(Context context, int layoutId, View dependView) {
        if (context == null) {
            return;
        }
        View inflate = View.inflate(context, layoutId, null);
        BubblePopup bubblePopup = new BubblePopup(context, inflate);
        bubblePopup.anchorView(dependView)
                .gravity(Gravity.BOTTOM)
                .showAnim(new BounceLeftEnter())
                .dismissAnim(new SlideRightExit())
                .autoDismiss(true)
                .show();
    }
}
