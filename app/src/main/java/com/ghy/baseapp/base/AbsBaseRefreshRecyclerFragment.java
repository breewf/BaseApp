package com.ghy.baseapp.base;

import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.company.myapp.R;
import com.ghy.baseapp.adapter.baserecycler.BaseQuickAdapter;
import com.ghy.baseapp.common.log.Log;
import com.ghy.baseapp.component.recyclerdivider.HorizontalDividerItemDecoration;
import com.ghy.baseapp.component.recyclerdivider.VerticalDividerItemDecoration;

import java.util.List;

import butterknife.Bind;

/**
 * Created by GHY on 2016/5/16.
 * AbsBaseRefreshRecyclerFragment
 * 带下拉（谷歌）和上拉的recyclerView
 */
public abstract class AbsBaseRefreshRecyclerFragment extends AbsBaseFragment {


    @Bind(R.id.base_drag_recycler_view)
    RecyclerView recyclerView;

    @Bind(R.id.base_drag_recycler_swipeLayout)
    SwipeRefreshLayout swipeRefreshLayout;

    private static final int NO_LAYOUT = 0;

    /**
     * recycleView的方向
     */
    public final int ORIENTATION_VERTICAL = 0;
    public final int ORIENTATION_HORIZONTAL = 1;

    private BaseQuickAdapter adapter;

    protected abstract void init();

    protected abstract BaseQuickAdapter getAbsQuickAdapter();

    private OnItemClickListener itemClickListener;
    private OnItemLongClickListener itemLongClickListener;
    private OnHeaderClickListener headerClickListener;
    private OnFooterClickListener footerClickListener;

    /**
     * header点击事件
     */
    public interface OnHeaderClickListener {
        void onHeadClick();
    }

    /**
     * footer点击事件
     */
    public interface OnFooterClickListener {
        void onFooterClick();
    }

    /**
     * item点击事件
     */
    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    /**
     * item长按事件
     */
    public interface OnItemLongClickListener {
        void onItemLongClick(int position);
    }

    /**
     * 开始刷新
     */
    abstract protected void onRefreshStart();

    /**
     * 开始加载更多
     */
    abstract protected void onLoadMoreStart();


    /**
     * 设置recyclerView header 布局
     *
     * @return
     */
    protected int getHeaderLayout() {
        return 0;
    }

    /**
     * 设置recyclerView footer 布局
     *
     * @return
     */
    protected int getFooterLayout() {
        return 0;
    }

    /**
     * 设置recyclerView 方向
     * 默认为VERTICAL
     *
     * @return
     */
    protected int setRecyclerOrientation() {
        return ORIENTATION_VERTICAL;
    }

    /**
     * 是否添加item分割线
     * 默认为false
     *
     * @return
     */
    protected boolean isOpenItemDecoration() {
        return false;
    }

    /**
     * 是否开启加载更多
     * 默认为true
     *
     * @return
     */
    protected boolean isOpenLoadMore() {
        return true;
    }

    @Override
    protected int getLayoutID() {
        return R.layout.abs_base_refresh_recycler;
    }

    /**
     * 设置数据
     */
    protected void setData(List list) {
        if (adapter != null) {
            adapter.setData(list);
        } else {
            Log.e("recycleViewAdapter == null");
        }
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.itemLongClickListener = listener;
    }

    public void setOnHeaderClickListener(OnHeaderClickListener listener) {
        this.headerClickListener = listener;
    }

    public void setOnFooterClickListener(OnFooterClickListener listener) {
        this.footerClickListener = listener;
    }

    /**
     * 加载数据完毕，结束刷新
     */
    public void setOnRefreshComplete(List list) {
        setData(list);
        swipeRefreshLayout.setRefreshing(false);
    }

    /**
     * 加载数据完毕，结束刷新
     */
    public void setOnLoadMoreComplete(List list) {
        adapter.notifyDataChangedAfterLoadMore(list, true);
    }

    @Override
    protected void init(Bundle savedInstanceState, View contentView) {

        init();

        //下拉刷新
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                onRefreshStart();
            }
        });

        //获取adapter
        if (getAbsQuickAdapter() != null) {
            adapter = getAbsQuickAdapter();
        } else {
            Log.e("QuickAdapter == null");
            return;
        }

        //设置布局
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        int orientation = setRecyclerOrientation();
        if (orientation == ORIENTATION_VERTICAL) {
            layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        } else {
            layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        }
        recyclerView.setLayoutManager(layoutManager);
        //设置adapter
        recyclerView.setAdapter(adapter);

        //添加分割线
        if (isOpenItemDecoration()) {
            if (orientation == ORIENTATION_VERTICAL) {
                recyclerView.addItemDecoration(new HorizontalDividerItemDecoration.Builder(getActivity()).build());
            } else {
                recyclerView.addItemDecoration(new VerticalDividerItemDecoration.Builder(getActivity()).build());
            }
        }

        //设置item的点击事件监听
        adapter.setOnRecyclerViewItemClickListener(new BaseQuickAdapter.OnRecyclerViewItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (itemClickListener != null) itemClickListener.onItemClick(position);
            }
        });

        //设置item的长按事件监听
        adapter.setOnRecyclerViewItemLongClickListener(new BaseQuickAdapter.OnRecyclerViewItemLongClickListener() {
            @Override
            public boolean onItemLongClick(View view, int position) {
                if (itemLongClickListener != null) itemLongClickListener.onItemLongClick(position);
                return false;
            }
        });

        //开启动画
        adapter.openLoadAnimation(BaseQuickAdapter.ALPHAIN);

        //添加header
        if (getHeaderLayout() != NO_LAYOUT) {
            View headerView = LayoutInflater.from(getContext()).inflate(getHeaderLayout(), null, false);
            headerView.setLayoutParams(new DrawerLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            headerView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (headerClickListener != null) headerClickListener.onHeadClick();
                }
            });
            adapter.addHeaderView(headerView);
        }

        //添加footer，注意此项和加载更多不共存，开启footer则不能使用加载更多
        if (getFooterLayout() != NO_LAYOUT) {
            //如果设置了上拉加载更多，则此项不生效
            if (!isOpenLoadMore()) {
                View footerView = LayoutInflater.from(getContext()).inflate(getFooterLayout(), null, false);
                footerView.setLayoutParams(new DrawerLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                footerView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (footerClickListener != null) footerClickListener.onFooterClick();
                    }
                });
                adapter.addFooterView(footerView);
            }
        }

        //注意需要传递pageSize或者使用setPageSize()方法
        adapter.openLoadMore(1, isOpenLoadMore());
        adapter.setOnLoadMoreListener(new BaseQuickAdapter.RequestLoadMoreListener() {
            @Override
            public void onLoadMoreRequested() {
                onLoadMoreStart();
            }
        });

    }
}
