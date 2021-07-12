package com.hy.baseapp.activity;

import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.hy.myapp.R;
import com.hy.baseapp.base.AbsBaseActivity;

import butterknife.Bind;
import butterknife.OnClick;

public class TabLayoutMeActivity extends AbsBaseActivity {

    @Bind(R.id.tab_tv_left)
    TextView tabTextLeft;
    @Bind(R.id.tab_tv_center)
    TextView tabTextCenter;
    @Bind(R.id.tab_tv_right)
    TextView tabTextRight;
    @Bind(R.id.tab_iv_line)
    ImageView tabIvLine;

    @Bind(R.id.tv_my_tab1)
    TextView textView1;
    @Bind(R.id.tv_my_tab2)
    TextView textView2;
    @Bind(R.id.tv_my_tab3)
    TextView textView3;

    /**
     * 存放tab中textView的数组
     */
    private TextView tabTvArray[] = new TextView[3];
    /**
     * 存放View的数组
     */
    private TextView viewArray[] = new TextView[3];
    /**
     * 指示器图片初始值
     */
    private int offset = 0; //初始偏移量
    private int oneOffset = 0; //一个页面偏移量
    private int currIndex = 0; // 当前tab编号
    private int bmpW; // 图片宽度

    @Override
    protected int getLayoutID() {
        return R.layout.activity_tab_layout_me;
    }

    @Override
    protected String getToolBarTitle() {
        return "自定义的TabLayout";
    }

    @Override
    protected void init() {

        initTab();

        initTabImage();

    }

    /**
     * 设置tab
     */
    private void initTab() {
        tabTextLeft.setText("推荐");
        tabTextCenter.setText("最热");
        tabTextRight.setText("最新");
        tabTvArray[0] = tabTextLeft;
        tabTvArray[1] = tabTextCenter;
        tabTvArray[2] = tabTextRight;

        viewArray[0] = textView1;
        viewArray[1] = textView2;
        viewArray[2] = textView3;
    }

    /**
     * 初始化指示器图片
     */
    private void initTabImage() {
        DisplayMetrics dm = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(dm);
        int screenW = dm.widthPixels; // 获取手机屏幕宽度分辨率
        bmpW = BitmapFactory.decodeResource(getResources(), R.mipmap.tab_line).getWidth();
        offset = (screenW / 3 - bmpW) / 2; // 获取图片偏移量
        oneOffset = offset * 2 + bmpW; // 一个页面的偏移量
        // 给imageView设置平移，使下划线平移到初始位置（平移一个offset）
        Matrix matrix = new Matrix();
        matrix.postTranslate(offset, 0);
        tabIvLine.setImageMatrix(matrix);
    }

    /**
     * Tab点击事件
     *
     * @param view
     */
    @OnClick({R.id.tab_tv_left, R.id.tab_tv_center, R.id.tab_tv_right})
    public void onTabClick(View view) {
        switch (view.getId()) {
            case R.id.tab_tv_left:
                changeTab(0);
                changeView(0);
                break;
            case R.id.tab_tv_center:
                changeTab(1);
                changeView(1);
                break;
            case R.id.tab_tv_right:
                changeTab(2);
                changeView(2);
                break;
        }
    }

    /**
     * 改变布局
     *
     * @param number
     */
    private void changeView(int number) {
        for (int i = 0; i < viewArray.length; i++) {
            if (i == number) {
                viewArray[i].setVisibility(View.VISIBLE);
            } else {
                viewArray[i].setVisibility(View.GONE);
            }
        }
    }

    /**
     * 改变Tab响应事件
     *
     * @param tabNumber
     */
    private void changeTab(int tabNumber) {
        // 指示器平移动画
        Animation animation = new TranslateAnimation(currIndex * oneOffset, tabNumber
                * oneOffset, 0, 0);
        currIndex = tabNumber;
        animation.setFillAfter(true); // 动画终止时停留在最后一帧
        animation.setDuration(300); // 动画持续时间，300毫秒
        tabIvLine.startAnimation(animation); // imageView开始平移动画
        //改变指示线
        for (int i = 0; i < tabTvArray.length; i++) {
            if (tabNumber == i) {
                tabTvArray[i].setTextColor(getResources().getColor(R.color.color_fc8825));
            } else {
                tabTvArray[i].setTextColor(getResources().getColor(R.color.black));
            }
        }
    }

}
