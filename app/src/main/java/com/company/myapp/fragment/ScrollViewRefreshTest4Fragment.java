package com.company.myapp.fragment;

import android.os.Handler;
import android.widget.ListView;
import android.widget.ScrollView;

import com.company.myapp.R;
import com.ghy.baseapp.base.AbsBaseDragScrollFragment;
import com.ghy.baseapp.helper.ToastHelper;
import com.ghy.baseapp.utils.ViewUtils;

/**
 * Created by GHY on 2016/5/10.
 * 下拉刷新上拉加载ScrollView
 */
public class ScrollViewRefreshTest4Fragment extends AbsBaseDragScrollFragment {


    private ScrollView scrollView;
    private ListView listView;

    @Override
    protected void init() {

        listView = (ListView) getActivity().findViewById(R.id.abs_base_list);
        scrollView = (ScrollView) getActivity().findViewById(R.id.refresh_drag_scroll_content_view);
        //重设ListView高度，需手动把scrollView滚动到顶部
        ViewUtils.setListViewHeight(listView);
        scrollView.smoothScrollTo(0,0);
    }

    @Override
    protected int getContentLayout() {
        return R.layout.fragment_scroll_refresh_test4;
    }

    @Override
    protected void onRefreshStart() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                ToastHelper.getInstance().showToast("刷新完成啦");
                setOnRefreshComplete();
            }
        },3000);
    }

    @Override
    protected void onLoadMoreStart() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                ToastHelper.getInstance().showToast("加载完成啦");
                setOnLoadMoreComplete();
            }
        },3000);
    }

}
