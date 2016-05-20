package com.company.myapp.fragment;

import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;

import com.company.myapp.adapter.ListView1Adapter;
import com.ghy.baseapp.base.AbsAdapterItem;
import com.ghy.baseapp.base.AbsBaseDragListFragment;
import com.ghy.baseapp.helper.ToastHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by GHY on 2016/5/10.
 * 下拉刷新上拉加载更多的ListView
 */
public class ListViewRefreshTest2Fragment extends AbsBaseDragListFragment {

    private List mList;

    @Override
    protected AbsAdapterItem getAbsAdapterItem() {
        return new ListView1Adapter();
    }


    @Override
    protected void init() {

        mList = new ArrayList();
        for (int i = 0; i < 50; i++) {
            mList.add("this is item " + i);
        }
        setData(mList);
    }

    @Override
    protected void onRefreshStart() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mList.add(0, "I am new item,have fun!");
                setOnRefreshComplete(mList);
            }
        },3000);
    }

    @Override
    protected void onLoadMoreStart() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mList.add("I am load More item!");
                setOnLoadMoreComplete(mList);
            }
        },3000);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        super.onItemClick(parent, view, position, id);
        ToastHelper.getInstance().showToast("点击了 " + mList.get(position));
    }

}
