package com.hy.myapp.adapter;

import android.content.Context;

import com.hy.myapp.R;
import com.hy.baseapp.adapter.baserecycler.BaseMultiItemQuickAdapter;
import com.hy.baseapp.adapter.baserecycler.BaseViewHolder;

import java.util.List;

/**
 * Created by GHY on 2016/5/17.
 */
public class MyRecyclerViewAdapterMultiple extends BaseMultiItemQuickAdapter<MultipleItem> {

    public MyRecyclerViewAdapterMultiple(Context context, List<MultipleItem> data) {
        super(context, data);
        addItmeType(MultipleItem.LETTER, R.layout.item_list_view_item_type_letter);
        addItmeType(MultipleItem.CONTENT,R.layout.item_list_view_item_type_content);
    }

    @Override
    protected void convert(BaseViewHolder helper, MultipleItem item) {

        switch (helper.getItemViewType()){
            case MultipleItem.LETTER:
                helper.setText(R.id.item_tv_letter,item.getLetter());
                break;
            case MultipleItem.CONTENT:
                helper.setText(R.id.item_tv_content,item.getContent());
                break;
        }
    }
}
