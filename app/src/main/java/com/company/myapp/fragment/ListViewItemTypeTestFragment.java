package com.company.myapp.fragment;

import android.view.View;
import android.widget.AdapterView;

import com.company.myapp.R;
import com.company.myapp.adapter.ListViewItemTypeContentAdapter;
import com.company.myapp.adapter.ListViewItemTypeLetterAdapter;
import com.ghy.baseapp.base.AbsAdapterItem;
import com.ghy.baseapp.base.AbsBaseListFragment;
import com.ghy.baseapp.helper.ToastHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by GHY on 2016/5/10.
 * 多种item类型的listView
 */
public class ListViewItemTypeTestFragment extends AbsBaseListFragment {

    private List<Map<String, String>> mList;
    private Map<String, String> mapContent;
    private Map<String, String> mapLetter;

    private String[] mLetter = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J"};

    private static final int TYPE_LETTER = 0;
    private static final int TYPE_CONTENT = 1;

    @Override
    protected AbsAdapterItem getAbsAdapterItem() {
        return null;
    }

    @Override
    protected int getAbsViewTypeCount() {
        return 2;
    }

    @Override
    protected AbsAdapterItem getAbsAdapterItem(int type) {
        switch (type) {
            case TYPE_LETTER:
                return new ListViewItemTypeLetterAdapter();
            case TYPE_CONTENT:
                return new ListViewItemTypeContentAdapter();
        }
        return super.getAbsAdapterItem(type);
    }

    @Override
    protected int getAbsItemViewType(int position) {

        return mList.get(position).get("key").contains("letter") ? TYPE_LETTER : TYPE_CONTENT;
    }

    @Override
    protected int getHeaderLayout() {
        return R.layout.item_list_view_header1;
    }

    @Override
    protected void init() {

        mList = new ArrayList();
        for (int i = 0; i < 10; i++) {
            mapLetter = new HashMap<>();
            mapLetter.put("key", "letter" + mLetter[i]);
            mList.add(mapLetter);
            for (int j = 0; j < 5; j++) {
                mapContent = new HashMap<>();
                mapContent.put("key", mLetter[i] + j + "");
                mList.add(mapContent);
            }
        }
        setData(mList);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        super.onItemClick(parent, view, position, id);
        if (position == 0) {
            ToastHelper.getInstance().showToast("点击了Header");
        } else {
            ToastHelper.getInstance().showToast("点击了 " + mList.get(position - 1).get("key"));
        }
    }
}
