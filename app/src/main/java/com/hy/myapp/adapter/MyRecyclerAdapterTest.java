package com.hy.myapp.adapter;

import android.content.Context;

import com.hy.myapp.R;
import com.hy.baseapp.adapter.baserecycler.BaseQuickAdapter;
import com.hy.baseapp.adapter.baserecycler.BaseViewHolder;

import java.util.List;

/**
 * Created by GHY on 2016/5/16.
 */
public class MyRecyclerAdapterTest extends BaseQuickAdapter<String> {

    public MyRecyclerAdapterTest(Context context, List<String> data) {
        super(context, R.layout.item_recycler_view_test1, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, String item) {
        helper.setText(R.id.recycler_view_text,item);
    }
}
