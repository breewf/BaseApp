package com.hy.baseapp.helper;

import android.content.Context;

import com.hy.baseapp.component.mydialog.MyDialog;
import com.hy.baseapp.global.TitleInfo;

/**
 * Created by hy on 2016/5/6.
 * 自定义对话框助手类
 */
public class MyDialogHelper {


    /**
     * 显示确定取消按钮
     *
     * @param context
     * @param title
     * @param content
     * @param confirmListener
     * @param cancelListener
     */
    public static void showDialogDefault(Context context, String title, String content,
                                         final MyDialog.OnConfirmClickListener confirmListener, final MyDialog.OnCancelClickListener cancelListener) {
        MyDialog myDialog = new MyDialog(context, title, content, TitleInfo.BTN_CONFIRM, TitleInfo.BTN_CANCEL);
        myDialog.show();
        if (confirmListener != null) {
            myDialog.setOnConfirmClickListener(confirmListener);
        }
        if (cancelListener != null) {
            myDialog.setOnCancelClickListener(cancelListener);
        }
    }

    /**
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
    public static void showDialogCustom(Context context, String title, String content, String confirm, String cancel,
                                        final MyDialog.OnConfirmClickListener confirmListener, final MyDialog.OnCancelClickListener cancelListener) {
        MyDialog myDialog = new MyDialog(context, title, content, confirm, cancel);
        myDialog.show();
        if (confirmListener != null) {
            myDialog.setOnConfirmClickListener(confirmListener);
        }
        if (cancelListener != null) {
            myDialog.setOnCancelClickListener(cancelListener);
        }
    }
}
