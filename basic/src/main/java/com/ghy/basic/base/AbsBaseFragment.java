package com.ghy.basic.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.ghy.basic.R;
import com.ghy.basic.common.logger.Logger;
import com.ghy.basic.utils.AppUtils;
import com.ghy.basic.view.LVCircularSmile;


/**
 * Created by HY on 2016/4/30.
 * fragment基类
 */
public abstract class AbsBaseFragment extends Fragment {

    private final String TAG = "AbsBaseFragment";
    private static final int NO_FRAGMENT_LAYOUT = 0;

    /**
     * 根布局view 和 子类添加内容布局view
     */
    private View mRootView, mContentView;

    /**
     * fragment根布局FrameLayout
     */
    private FrameLayout mRootFrameLayout;

    public static final int FRAGMENT_STATUS_SUCCESS = 0;//默认样式（加载默认布局）
    public static final int FRAGMENT_STATUS_LOADING = 1;//加载中样式（加载加载中布局）
    public static final int FRAGMENT_STATUS_NO_NET = 2;//无网络样式（加载无网络布局）
    public static final int FRAGMENT_STATUS_EMPTY = 3;//空页面样式（加载空页面布局）
    public static final int FRAGMENT_STATUS_ERROR = 4;//错误样式（加载错误布局）

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


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        int contentLayoutId = getLayoutID();
        mRootView = inflater.inflate(R.layout.abs_base_fragment, container, false);
        mRootFrameLayout = mRootView.findViewById(R.id.abs_base_frame_layout_fg);
        if (contentLayoutId != NO_FRAGMENT_LAYOUT) {
            mContentView = inflater.inflate(contentLayoutId, null);
            mRootFrameLayout.addView(mContentView);
        } else {
            Logger.w("fragmentContentView == null");
        }
        return mRootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initView();
        initData();
    }

    /**
     * 注意：此方法和逻辑跟activity一致
     * 设置fragment布局界面状态
     */
    View loadingLayoutView;//加载中布局
    View noNetLayoutView;//无网络布局
    View emptyLayoutView;//空页面布局
    View errorLayoutView;//错误页面布局
    LVCircularSmile loadingSmile;//loading动画

    public void setFragmentStatus(int fragmentStatus) {
        int bottomPadding = AppUtils.dip2px(getActivity(), 100);
        if (loadingSmile != null) loadingSmile.stopAnim();
        switch (fragmentStatus) {
            case FRAGMENT_STATUS_SUCCESS:
                removePageStatusView();
                mContentView.setVisibility(View.VISIBLE);
                break;
            case FRAGMENT_STATUS_LOADING:
                removePageStatusView();
                loadingLayoutView = getActivity().getLayoutInflater().inflate(R.layout.abs_base_loading_layout, null);
                loadingLayoutView.setPadding(0, 0, 0, bottomPadding);
                loadingSmile = loadingLayoutView.findViewById(R.id.loading_smile);
                mContentView.setVisibility(View.INVISIBLE);
                mRootFrameLayout.addView(loadingLayoutView);
                loadingSmile.setViewColor(getResources().getColor(R.color.text_normal));
                loadingSmile.startAnim(1000);
                break;
            case FRAGMENT_STATUS_NO_NET:
                removePageStatusView();
                noNetLayoutView = getActivity().getLayoutInflater().inflate(R.layout.abs_base_no_net_layout, null);
                noNetLayoutView.setPadding(0, 0, 0, bottomPadding);
                mRootFrameLayout.addView(noNetLayoutView);
                mContentView.setVisibility(View.INVISIBLE);
                setPageStatusViewClickListener(FRAGMENT_STATUS_NO_NET, noNetLayoutView);
                break;
            case FRAGMENT_STATUS_EMPTY:
                removePageStatusView();
                emptyLayoutView = getActivity().getLayoutInflater().inflate(R.layout.abs_base_empty_layout, null);
                emptyLayoutView.setPadding(0, 0, 0, bottomPadding);
                mRootFrameLayout.addView(emptyLayoutView);
                mContentView.setVisibility(View.INVISIBLE);
                setPageStatusViewClickListener(FRAGMENT_STATUS_EMPTY, emptyLayoutView);
                break;
            case FRAGMENT_STATUS_ERROR:
                removePageStatusView();
                errorLayoutView = getActivity().getLayoutInflater().inflate(R.layout.abs_base_error_layout, null);
                errorLayoutView.setPadding(0, 0, 0, bottomPadding);
                mRootFrameLayout.addView(errorLayoutView);
                mContentView.setVisibility(View.INVISIBLE);
                setPageStatusViewClickListener(FRAGMENT_STATUS_ERROR, errorLayoutView);
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
                    case FRAGMENT_STATUS_NO_NET:
                        onNoNetClick(view);
                        break;
                    case FRAGMENT_STATUS_EMPTY:
                        onEmptyClick(view);
                        break;
                    case FRAGMENT_STATUS_ERROR:
                        onErrorClick(view);
                        break;
                }
            }
        });
    }

}
