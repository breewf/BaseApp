package com.hy.myapp.activity;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.hy.myapp.R;
import com.hy.baseapp.base.AbsBaseActivity;
import com.hy.baseapp.helper.ToastHelper;
import com.hy.baseapp.view.WheelView;

import java.util.ArrayList;
import java.util.List;

import butterknife.OnClick;

public class WheelTestActivity extends AbsBaseActivity {

    /**
     * 选择城市按钮
     */
    private TextView tvProvinceDone;

    /**
     * 省份
     */
    private String provinceArray[] = {
            "安徽", "北京", "重庆", "福建", "甘肃",
            "广东", "广西", "贵州", "海南", "河北",
            "黑龙江", "河南", "湖北", "湖南", "江苏",
            "江西", "吉林", "辽宁", "内蒙古", "宁夏",
            "青海", "陕西", "山东", "上海", "山西",
            "四川", "天津", "新疆", "西藏", "云南", "浙江" };
    /**
     * 省份列表
     */
    private List<String> provinceStringList;

    /**
     * 选择的城市
     */
    private String selectedProvince;

    @Override
    protected int getLayoutID() {
        return R.layout.activity_wheel_test;
    }

    @Override
    protected String getToolBarTitle() {
        return "滚动控件测试";
    }

    @Override
    protected void init() {

    }

    @OnClick(R.id.btn_wheel_test)
    public void wheelTest() {
        //弹出底部省份选择蒙版
        showProvincePopupWindow();
    }

    /**
     * 底部省份选择蒙版
     */
    private PopupWindow popupWindow;

    private void showProvincePopupWindow() {
        //导入布局文件
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.layout_province_popup_window, null);
        //省份选择控件
        WheelView wheelView = (WheelView) popupView.findViewById(R.id.province_wv);
        //完成按钮
        tvProvinceDone = (TextView) popupView.findViewById(R.id.oil_province_select_done);

        popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        popupWindow.setOutsideTouchable(true);
        popupWindow.setAnimationStyle(R.style.AnimationPopupWindow);
        popupWindow.showAtLocation(this.findViewById(R.id.wheel_activity_layout),
                Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
        //加载省份列表
        initWheelView(wheelView);
    }

    private void initWheelView(WheelView wheelView) {
        provinceStringList = new ArrayList<>();
        for (int i = 0; i < provinceArray.length; i++) {
            provinceStringList.add(provinceArray[i]);
        }

        //上下各显示2个
        wheelView.setOffset(2);
        wheelView.setItems(provinceStringList);
        //默认选中的省份
        wheelView.setSeletion(1);
        //如果不滚动选择，默认为北京
        selectedProvince = "北京";
        wheelView.setOnWheelViewListener(new WheelView.OnWheelViewListener() {
            @Override
            public void onSelected(int selectedIndex, String item) {
                selectedProvince = item;
            }
        });

        /**
         * 选择省份点击事件
         */
        tvProvinceDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ToastHelper.getInstance().showToast("选择了 " + selectedProvince);
                if (popupWindow != null) popupWindow.dismiss();
            }
        });
    }

}
