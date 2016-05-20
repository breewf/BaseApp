package com.company.myapp.activity;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.company.myapp.R;
import com.ghy.baseapp.base.AbsBaseActivity;
import com.ghy.baseapp.helper.ToastHelper;
import com.rey.material.app.BottomSheetDialog;

import butterknife.OnClick;

public class SheetTestActivity extends AbsBaseActivity {

    @Override
    protected int getLayoutID() {
        return R.layout.activity_sheet_test;
    }

    @Override
    protected String getToolBarTitle() {
        return "BottomSheet底部弹出框";
    }

    @Override
    protected void init() {

    }

    @OnClick(R.id.btn_sheet1)
    public void sheet1() {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.layout_bottom_sheet, null);
        TextView textView = (TextView) view.findViewById(R.id.btn_bottom_sheet_ok1);
        bottomSheetDialog.contentView(view)
                .heightParam(ViewGroup.LayoutParams.WRAP_CONTENT)
                .inDuration(400)
                .outDuration(200)
                .cancelable(true)
                .show();
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ToastHelper.getInstance().showToast("葵花点穴手");
                bottomSheetDialog.dismiss();
            }
        });
    }

    @OnClick(R.id.btn_sheet2)
    public void sheet2() {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.layout_bottom_sheet, null);
        TextView textView = (TextView) view.findViewById(R.id.btn_bottom_sheet_ok1);
        bottomSheetDialog.contentView(view)
                .heightParam(ViewGroup.LayoutParams.MATCH_PARENT)
                .inDuration(400)
                .outDuration(200)
                .cancelable(true)
                .show();
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ToastHelper.getInstance().showToast("葵花点穴手");
                bottomSheetDialog.dismiss();
            }
        });
    }

}
