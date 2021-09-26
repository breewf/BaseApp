package com.hy.baseapp.component.mydialog;


import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.hy.myapp.R;

/**
 * Created by hy on 2016/05/06
 */
public class MyDialog extends Dialog {

    private Context context;
    /*
    * 取消按钮
    * */
    private TextView btnCancel;
    /*
    * 确定按钮
    * */
    private TextView btnConfirm;
    /*
    * 间隔确定按钮和取消按钮的view
    * */
    private View viewBtnCut;
    /*
   * 提示标题（粗体）
   * */
    private TextView mainTitle;
    /*
    * 提示描述
    * */
    private TextView titleDesc;
    /*
    * 没有标题时的间距view
    * */
    private View viewTvPadding;

    private String mTitle;
    private String mDes;
    private String mConfirm;
    private String mCancel;

    public interface OnConfirmClickListener {
        void onConfirmClick();
    }

    public interface OnCancelClickListener {
        void onCancelClick();
    }

    public MyDialog(Context context, String mTitle, String mDes, String mConfirm, String mCancel) {
        super(context, R.style.MyDialog);
        this.context = context;
        this.mTitle = mTitle;
        this.mDes = mDes;
        this.mConfirm = mConfirm;
        this.mCancel = mCancel;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createView();
    }

    /**
     * 加载Dialog布局
     */
    private void createView() {
        LayoutInflater inflater = LayoutInflater.from(context);
        View contentView = inflater.inflate(R.layout.abs_my_dialog, null);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(contentView);
        initView(contentView);
        Window dialogWindow = getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        DisplayMetrics d = context.getResources().getDisplayMetrics();
        lp.width = (int) (d.widthPixels * 0.8);
        dialogWindow.setAttributes(lp);
    }

    /**
     * 初始化
     *
     * @param contentView
     */
    private void initView(View contentView) {
        mainTitle = (TextView) contentView.findViewById(R.id.dailog_main_title);
        titleDesc = (TextView) contentView.findViewById(R.id.dailog_title_desc);
        btnConfirm = (TextView) contentView.findViewById(R.id.xiaoyi_confirm);
        btnCancel = (TextView) contentView.findViewById(R.id.xiaoyi_cance);
        viewTvPadding = contentView.findViewById(R.id.view_tv_padding);
        viewBtnCut = contentView.findViewById(R.id.view_btn_cut);

        if (mTitle != null && !"".equals(mTitle)) {
            mainTitle.setVisibility(View.VISIBLE);
            mainTitle.setText(mTitle);
        }

        if (mDes != null && !"".equals(mDes)) {
            titleDesc.setVisibility(View.VISIBLE);
            titleDesc.setText(mDes);
        }

         /*
         * 若不存在title只有des，显示间距view
         * */
        if ((mTitle == null || "".equals(mTitle)) && (mDes != null && !"".equals(mDes))) {
            viewTvPadding.setVisibility(View.VISIBLE);
        }

        if (mConfirm != null && !"".equals(mConfirm)) {
            btnConfirm.setVisibility(View.VISIBLE);
            btnConfirm.setText(mConfirm);
        }

        if (mCancel != null && !"".equals(mCancel)) {
            btnCancel.setVisibility(View.VISIBLE);
            btnCancel.setText(mCancel);
        }

        /*
        * 存在两个按钮则按钮之间有间距，显示间隔view
        * */
        if ((mConfirm != null && !"".equals(mConfirm)) && (mCancel != null && !"".equals(mCancel))) {
            viewBtnCut.setVisibility(View.VISIBLE);
        }

    }

    public void setOnConfirmClickListener(final OnConfirmClickListener confirmListener) {
        if (confirmListener != null) {
            btnConfirm.setOnClickListener(v -> {
                confirmListener.onConfirmClick();
                dismiss();
            });
        }
    }

    public void setOnCancelClickListener(final OnCancelClickListener cancelListener) {
        if (cancelListener != null) {
            btnCancel.setOnClickListener(v -> {
                cancelListener.onCancelClick();
                dismiss();
            });
        }
    }

}
