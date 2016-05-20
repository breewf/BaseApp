package com.company.myapp.fragment;

import android.os.Handler;

import com.company.myapp.adapter.MyRecyclerAdapterTest;
import com.ghy.baseapp.adapter.baserecycler.BaseQuickAdapter;
import com.ghy.baseapp.base.AbsBaseDragRecyclerFragment;
import com.ghy.baseapp.helper.ToastHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by HY on 2016/5/1.
 */
public class RecyclerViewTestFragment5 extends AbsBaseDragRecyclerFragment {

    private List<String> list;
    private List<String> listAdd;

    @Override
    protected BaseQuickAdapter getAbsQuickAdapter() {
        return new MyRecyclerAdapterTest(getActivity(), list);
    }

    @Override
    protected void onRefreshStart() {

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                list.add(0, "Hello,I am a new Item");
                setOnRefreshComplete(list);
                ToastHelper.getInstance().showToast("刷新成功");
            }
        }, 3000);

    }

    @Override
    protected void onLoadMoreStart() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                listAdd = new ArrayList<>();
                listAdd.add("Hi I am a load Message1");
                listAdd.add("Hi I am a load Message2");
                setOnLoadMoreComplete(listAdd);
                ToastHelper.getInstance().showToast("加载成功");
            }
        }, 3000);
    }

    @Override
    protected void init() {
        list = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            list.add("recycler item " + i);
        }

        setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                ToastHelper.getInstance().showToast("点击了位置" + position);
            }
        });
    }

}
