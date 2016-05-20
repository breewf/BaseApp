package com.company.myapp.fragment;

import android.os.Handler;

import com.company.myapp.adapter.MyRecyclerAdapterTest;
import com.ghy.baseapp.adapter.baserecycler.BaseQuickAdapter;
import com.ghy.baseapp.base.AbsBaseRefreshRecyclerFragment;
import com.ghy.baseapp.helper.ToastHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by HY on 2016/5/1.
 */
public class RecyclerViewTestFragment1 extends AbsBaseRefreshRecyclerFragment {

    private List<String> list;
    private List<String> listAdd;

    @Override
    protected void init() {
        list = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            list.add("recycler item " + i);
        }

        setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                ToastHelper.getInstance().showToast("点击" + list.get(position));
            }
        });

        setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public void onItemLongClick(int position) {
                ToastHelper.getInstance().showToast("长按" + list.get(position));
            }
        });

        setOnHeaderClickListener(new OnHeaderClickListener() {
            @Override
            public void onHeadClick() {
                ToastHelper.getInstance().showToast("点击了Header");
            }
        });
    }


    @Override
    protected boolean isOpenItemDecoration() {
        return true;
    }

    @Override
    protected BaseQuickAdapter getAbsQuickAdapter() {
        return new MyRecyclerAdapterTest(getActivity(), list);
    }


    @Override
    protected void onRefreshStart() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                ToastHelper.getInstance().showToast("刷新完成啦!");
                list.add(0, "Hi,I am a refresh item ");
                setOnRefreshComplete(list);
            }
        }, 3000);
    }

    @Override
    protected void onLoadMoreStart() {
        listAdd = new ArrayList<>();
        listAdd.add("I am a load Item1 !");
        listAdd.add("I am a load Item2 !");
        listAdd.add("I am a load Item3 !");
        listAdd.add("I am a load Item4 !");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                ToastHelper.getInstance().showToast("加载成功啦!");
                setOnLoadMoreComplete(listAdd);
            }
        }, 3000);
    }

}
