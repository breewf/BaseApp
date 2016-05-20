package com.company.myapp.fragment;

import com.company.myapp.adapter.MyRecyclerAdapterTest;
import com.ghy.baseapp.adapter.baserecycler.BaseQuickAdapter;
import com.ghy.baseapp.base.AbsBaseRecyclerFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by HY on 2016/5/1.
 */
public class RecyclerViewTestFragment4 extends AbsBaseRecyclerFragment {

    private List<String> list;

    @Override
    protected BaseQuickAdapter getAbsQuickAdapter() {
        return new MyRecyclerAdapterTest(getActivity(),list);
    }

    @Override
    protected int setRecyclerOrientation() {
        return ORIENTATION_HORIZONTAL;
    }

    @Override
    protected void init() {
        list = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            list.add("recycler item " + i);
        }
    }

}
