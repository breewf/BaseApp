package com.hy.myapp.adapter;

import android.view.View;

import com.hy.myapp.R;
import com.hy.baseapp.base.AbsAdapterItem;

import java.util.Map;

import com.hy.myapp.databinding.ItemListViewItemTypeContentBinding;

/**
 * Created by GHY on 2016/5/10.
 */
public class ListViewItemTypeContentAdapter extends AbsAdapterItem<Map<String,String>> {

    private ItemListViewItemTypeContentBinding binding;

    @Override
    public int getItemLayout() {
        return R.layout.item_list_view_item_type_content;
    }

    @Override
    public void init(View contentView) {

        binding = ItemListViewItemTypeContentBinding.bind(contentView);

    }

    @Override
    public void bindData(Map<String,String> map) {
        binding.itemTvContent.setText(map.get("key"));
    }

}
