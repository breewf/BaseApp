package com.hy.myapp.adapter;

import android.view.View;

import com.hy.myapp.R;
import com.hy.baseapp.base.AbsAdapterItem;
import com.hy.myapp.databinding.ItemListViewTest2Binding;

/**
 * Created by GHY on 2016/5/10.
 */
public class ListView2Adapter extends AbsAdapterItem<String> {

    private ItemListViewTest2Binding binding;

    @Override
    public int getItemLayout() {
        return R.layout.item_list_view_test2;
    }

    @Override
    public void init(View contentView) {

        binding = ItemListViewTest2Binding.bind(contentView);

    }

    @Override
    public void bindData(String s) {
        binding.itemTvTest2.setText(s);
    }

}
