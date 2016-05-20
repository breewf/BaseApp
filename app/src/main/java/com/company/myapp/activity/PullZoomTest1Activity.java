package com.company.myapp.activity;

import com.company.myapp.R;
import com.ghy.baseapp.base.AbsBaseActivity;
import com.ghy.baseapp.view.PullZoomView;

import butterknife.Bind;

public class PullZoomTest1Activity extends AbsBaseActivity {


    @Bind(R.id.pull_zoom_view)
    PullZoomView pullZoomView;

    @Override
    protected int getLayoutID() {
        return R.layout.activity_pull_zoom_test1;
    }

    @Override
    protected String getToolBarTitle() {
        return "PullZoom下拉缩放";
    }

    @Override
    protected void init() {

        pullZoomView.setIsParallax(true);    //允许视差动画
        pullZoomView.setIsZoomEnable(true);  //允许头部放大
        pullZoomView.setSensitive(1.5f);     //敏感度1.5
        pullZoomView.setZoomTime(500);       //头部缩放时间500毫秒

    }


}
