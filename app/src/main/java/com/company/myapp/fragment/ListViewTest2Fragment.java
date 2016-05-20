package com.company.myapp.fragment;

import android.view.View;
import android.widget.AdapterView;

import com.company.myapp.R;
import com.company.myapp.adapter.ListView2Adapter;
import com.ghy.baseapp.base.AbsAdapterItem;
import com.ghy.baseapp.base.AbsBaseListFragment;
import com.ghy.baseapp.helper.ToastHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by GHY on 2016/5/10.
 */
public class ListViewTest2Fragment extends AbsBaseListFragment {

    private List mList;

    @Override
    protected AbsAdapterItem getAbsAdapterItem() {
        return new ListView2Adapter();
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
    protected void init() {

        mList = new ArrayList();
        for (int i = 0; i < 50; i++) {
            mList.add("this is item " + i);
        }
        setData(mList);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        super.onItemClick(parent, view, position, id);
        if (position == 0) {
            ToastHelper.getInstance().showToast("点击了Header");
        } else if (position == mList.size() + 1) {
            ToastHelper.getInstance().showToast("点击了Footer");
        } else {
            ToastHelper.getInstance().showToast("点击了 " + mList.get(position - 1));
        }
    }
}
