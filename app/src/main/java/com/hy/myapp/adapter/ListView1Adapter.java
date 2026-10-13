package com.hy.myapp.adapter;

import android.view.View;

import com.hy.myapp.R;
import com.hy.baseapp.base.AbsAdapterItem;
import com.hy.myapp.databinding.ItemListViewTest1Binding;

/**
 * Created by GHY on 2016/5/10.
 */
public class ListView1Adapter extends AbsAdapterItem<String> {

    private ItemListViewTest1Binding binding;

    @Override
    public int getItemLayout() {
        return R.layout.item_list_view_test1;
    }

    @Override
    public void init(View contentView) {

        binding = ItemListViewTest1Binding.bind(contentView);

    }

    @Override
    public void bindData(String s) {
        binding.itemTvTest1.setText(s);
    }

}
