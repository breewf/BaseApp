package com.company.myapp.fragment;

import com.company.myapp.R;
import com.company.myapp.adapter.MyRecyclerAdapterTest;
import com.ghy.baseapp.adapter.baserecycler.BaseQuickAdapter;
import com.ghy.baseapp.base.AbsBaseRecyclerFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by HY on 2016/5/1.
 */
public class RecyclerViewTestFragment3 extends AbsBaseRecyclerFragment {

    private List<String> list;

    @Override
    protected BaseQuickAdapter getAbsQuickAdapter() {
        return new MyRecyclerAdapterTest(getActivity(),list);
    }

    @Override
    protected void init() {
        list = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            list.add("recycler item " + i);
        }
    }

    @Override
    protected int getHeaderLayout() {
        return R.layout.item_list_view_header1;
    }

    @Override
    protected int getFooterLayout() {
        return R.layout.item_list_view_footer1;
    }

    @Override
    protected boolean isOpenItemDecoration() {
        return true;
    }


}
