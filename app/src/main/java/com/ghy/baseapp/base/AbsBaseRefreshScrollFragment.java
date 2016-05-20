package com.ghy.baseapp.base;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ScrollView;

import com.company.myapp.R;
import com.ghy.baseapp.common.logger.Logger;

/**
 * Created by GHY on 2016/5/11.
 * AbsBaseRefreshScrollFragment
 * 谷歌下拉刷新ScrollView
 */
public abstract class AbsBaseRefreshScrollFragment extends AbsBaseFragment {

    private static final String TAG = "AbsBaseRefreshScrollFragment";
    private static final int NO_LAYOUT = 0;

    private ScrollView scrollView;

    /**
     * 谷歌下拉刷新控件
     */
    private SwipeRefreshLayout mRefreshLayout;


    protected ScrollView getScrollView() {
        return scrollView;
    }


    protected abstract void init();

    protected abstract int getContentLayout();

    /**
     * 下拉刷新完成
     */
    protected void setOnRefreshComplete() {
        mRefreshLayout.setRefreshing(false);
    }

    /**
     * 开始刷新
     */
    abstract protected void onRefreshStart();

    @Override
    protected int getLayoutID() {
        return R.layout.abs_base_refresh_scroll;
    }

    @Override
    protected void init(Bundle savedInstanceState, View contentView) {
        mRefreshLayout = (SwipeRefreshLayout) contentView;
        scrollView = (ScrollView) contentView.findViewById(R.id.abs_base_refresh_scroll);
        int layoutId = getContentLayout();
        if (layoutId != NO_LAYOUT) {
            View mContentView = LayoutInflater.from(getActivity()).inflate(layoutId, null);
            scrollView.addView(mContentView);
        } else {
            Logger.w("RefreshScrollFragmentContentView == null");
        }

        //下拉刷新监听
        mRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                onRefreshStart();
            }
        });
        init();
    }

}
