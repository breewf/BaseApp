package com.ghy.basic.base;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.ghy.basic.R;
import com.ghy.basic.common.logger.Logger;
import com.ghy.basic.utils.AppUtils;
import com.ghy.basic.view.LVCircularSmile;


/**
 * Created by GHY on 2016/4/29.
 * Activity基类
 */
public abstract class AbsBaseActivity extends AppCompatActivity {

    private final String TAG = "AbsBaseActivity";
    private static final int NO_LAYOUT = 0;

    /**
     * activity布局样式
     */
    public static final int ACTIVITY_STATUS_SUCCESS = 0;//默认样式（加载默认布局）
    public static final int ACTIVITY_STATUS_LOADING = 1;//加载中样式（加载加载中布局）
    public static final int ACTIVITY_STATUS_NO_NET = 2;//无网络样式（加载无网络布局）
    public static final int ACTIVITY_STATUS_EMPTY = 3;//空页面样式（加载空页面布局）
    public static final int ACTIVITY_STATUS_ERROR = 4;//错误样式（加载错误布局）

    /**
     * 根布局View
     */
    private View mRootFrameView;
    /**
     * 根布局FrameLayout
     */
    private FrameLayout mRootFrameLayout;
    /**
     * 标题栏布局
     */
    protected LinearLayout mTitleBarLayout;
    /**
     * 子类添加的View
     */
    private View mSonView;

    /**
     * 获取布局ID
     *
     * @return
     */
    protected abstract int getLayoutID();

    /**
     * 初始化view
     */
    protected abstract void initView();

    /**
     * 初始化data
     */
    protected abstract void initData();

    /**
     * 是否打开TitleBar
     * 默认为true
     *
     * @return
     */
    protected boolean isOpenTitleBar() {
        return true;
    }

    /**
     * 无网络点击事件
     *
     * @param view
     */
    protected void onNoNetClick(View view) {
    }

    /**
     * 空页面点击事件
     *
     * @param view
     */
    protected void onEmptyClick(View view) {
    }

    /**
     * 错误面点击事件
     *
     * @param view
     */
    protected void onErrorClick(View view) {
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //保持屏幕竖屏
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        int id = getLayoutID();
        //加载根布局
        mRootFrameView = getLayoutInflater().inflate(R.layout.abs_base_activity, null);
        mTitleBarLayout = mRootFrameView.findViewById(R.id.abs_base_title_bar);
        mRootFrameLayout = mRootFrameView.findViewById(R.id.abs_base_frame_layout);
        //设置布局
        setContentView(mRootFrameView);
        //加载子类布局
        if (id != NO_LAYOUT) {
            mSonView = getLayoutInflater().inflate(id, null);
            mRootFrameLayout.addView(mSonView);
        } else {
            Logger.e("mSonView == null");
        }
        //initTitleBar
        initTitleBar();
        //初始化
        initView();
        initData();
    }

    /**
     * 设置activity布局界面状态
     */
    View loadingLayoutView;//加载中布局
    View noNetLayoutView;//无网络布局
    View emptyLayoutView;//空页面布局
    View errorLayoutView;//错误页面布局
    LVCircularSmile loadingSmile;//loading动画

    public void setActivityStatus(int activityStatus) {
        int bottomPadding = AppUtils.dip2px(this, 100);
        if (loadingSmile != null) loadingSmile.stopAnim();
        switch (activityStatus) {
            case ACTIVITY_STATUS_SUCCESS:
                removePageStatusView();
                mSonView.setVisibility(View.VISIBLE);
                break;
            case ACTIVITY_STATUS_LOADING:
                removePageStatusView();
                loadingLayoutView = getLayoutInflater().inflate(R.layout.abs_base_loading_layout, null);
                loadingLayoutView.setPadding(0, 0, 0, bottomPadding);
                loadingSmile = loadingLayoutView.findViewById(R.id.loading_smile);
                mSonView.setVisibility(View.INVISIBLE);
                mRootFrameLayout.addView(loadingLayoutView);
                loadingSmile.setViewColor(getResources().getColor(R.color.text_normal));
                loadingSmile.startAnim(1000);
                break;
            case ACTIVITY_STATUS_NO_NET:
                removePageStatusView();
                noNetLayoutView = getLayoutInflater().inflate(R.layout.abs_base_no_net_layout, null);
                noNetLayoutView.setPadding(0, 0, 0, bottomPadding);
                mRootFrameLayout.addView(noNetLayoutView);
                mSonView.setVisibility(View.INVISIBLE);
                setPageStatusViewClickListener(ACTIVITY_STATUS_NO_NET, noNetLayoutView);
                break;
            case ACTIVITY_STATUS_EMPTY:
                removePageStatusView();
                emptyLayoutView = getLayoutInflater().inflate(R.layout.abs_base_empty_layout, null);
                emptyLayoutView.setPadding(0, 0, 0, bottomPadding);
                mRootFrameLayout.addView(emptyLayoutView);
                mSonView.setVisibility(View.INVISIBLE);
                setPageStatusViewClickListener(ACTIVITY_STATUS_EMPTY, emptyLayoutView);
                break;
            case ACTIVITY_STATUS_ERROR:
                removePageStatusView();
                errorLayoutView = getLayoutInflater().inflate(R.layout.abs_base_error_layout, null);
                errorLayoutView.setPadding(0, 0, 0, bottomPadding);
                mRootFrameLayout.addView(errorLayoutView);
                mSonView.setVisibility(View.INVISIBLE);
                setPageStatusViewClickListener(ACTIVITY_STATUS_ERROR, errorLayoutView);
                break;
        }
    }

    /**
     * 移除添加的页面状态布局View
     */
    private void removePageStatusView() {
        if (loadingLayoutView != null) mRootFrameLayout.removeView(loadingLayoutView);
        if (noNetLayoutView != null) mRootFrameLayout.removeView(noNetLayoutView);
        if (emptyLayoutView != null) mRootFrameLayout.removeView(emptyLayoutView);
        if (errorLayoutView != null) mRootFrameLayout.removeView(errorLayoutView);
    }

    /**
     * 设置页面状态点击事件
     *
     * @param pageStatus
     * @param pageView
     */
    private void setPageStatusViewClickListener(final int pageStatus, View pageView) {
        if (pageView == null) return;
        pageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (pageStatus) {
                    case ACTIVITY_STATUS_NO_NET:
                        onNoNetClick(view);
                        break;
                    case ACTIVITY_STATUS_EMPTY:
                        onEmptyClick(view);
                        break;
                    case ACTIVITY_STATUS_ERROR:
                        onErrorClick(view);
                        break;
                }
            }
        });
    }

    /**
     * 初始化标题栏显示与隐藏
     */
    private void initTitleBar() {
        if (isOpenTitleBar()) {
            mTitleBarLayout.setVisibility(View.VISIBLE);
        } else {
            mTitleBarLayout.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
