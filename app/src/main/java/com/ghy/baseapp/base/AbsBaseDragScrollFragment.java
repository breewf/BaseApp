package com.ghy.baseapp.base;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ScrollView;

import com.company.myapp.R;
import com.ghy.baseapp.common.logger.Logger;
import com.ghy.baseapp.component.refreshload.PullToRefreshLayout;
import com.ghy.baseapp.component.refreshload.pullableview.PullableScrollView;

/**
 * Created by GHY on 2016/5/11.
 * AbsBaseDragScrollFragment
 * 下拉刷新上拉加载ScrollView
 */
public abstract class AbsBaseDragScrollFragment extends AbsBaseFragment {

    private static final String TAG = "AbsBaseDragScrollFragment";
    private static final int NO_LAYOUT = 0;

    private PullableScrollView scrollView;

    /**
     * 可自定义下拉刷新控件
     */
    private PullToRefreshLayout mPullToRefreshLayout;


    protected ScrollView getScrollView() {
        return scrollView;
    }


    protected abstract void init();

    protected abstract int getContentLayout();

    /**
     * 下拉刷新完成
     */
    protected void setOnRefreshComplete() {
        mPullToRefreshLayout.refreshFinish(PullToRefreshLayout.SUCCEED);
    }

    /**
     * 加载更多完成
     */
    protected void setOnLoadMoreComplete() {
        mPullToRefreshLayout.loadmoreFinish(PullToRefreshLayout.SUCCEED);
    }


    /**
     * 开始刷新
     */
    abstract protected void onRefreshStart();

    /**
     * 开始加载更多
     */
    abstract protected void onLoadMoreStart();


    @Override
    protected int getLayoutID() {
        return R.layout.abs_base_drag_scroll;
    }

    @Override
    protected void init(Bundle savedInstanceState, View contentView) {
        mPullToRefreshLayout = (PullToRefreshLayout) contentView;
        scrollView = (PullableScrollView) contentView.findViewById(R.id.refresh_drag_scroll_content_view);
        int layoutId = getContentLayout();
        if (layoutId != NO_LAYOUT) {
            View mContentView = LayoutInflater.from(getActivity()).inflate(layoutId, null);
            scrollView.addView(mContentView);
        } else {
            Logger.w("DragScrollFragmentContentView == null");
        }

        //刷新监听
        mPullToRefreshLayout.setOnRefreshListener(new PullToRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh(PullToRefreshLayout pullToRefreshLayout) {
                onRefreshStart();
            }

            @Override
            public void onLoadMore(PullToRefreshLayout pullToRefreshLayout) {
                onLoadMoreStart();
            }
        });

        init();
    }

}
