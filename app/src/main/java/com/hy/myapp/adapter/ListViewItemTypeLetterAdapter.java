package com.hy.myapp.adapter;

import android.view.View;

import com.hy.myapp.R;
import com.hy.baseapp.base.AbsAdapterItem;

import java.util.Map;

import com.hy.myapp.databinding.ItemListViewItemTypeLetterBinding;

/**
 * Created by GHY on 2016/5/10.
 */
public class ListViewItemTypeLetterAdapter extends AbsAdapterItem<Map<String,String>> {

    private ItemListViewItemTypeLetterBinding binding;

    @Override
    public int getItemLayout() {
        return R.layout.item_list_view_item_type_letter;
    }

    @Override
    public void init(View contentView) {

        binding = ItemListViewItemTypeLetterBinding.bind(contentView);

    }

    @Override
    public void bindData(Map<String,String> map) {
        binding.itemTvLetter.setText(map.get("key"));
    }

}
