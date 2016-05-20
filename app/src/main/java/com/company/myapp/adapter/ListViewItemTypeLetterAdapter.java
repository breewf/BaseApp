package com.company.myapp.adapter;

import android.view.View;
import android.widget.TextView;

import com.company.myapp.R;
import com.ghy.baseapp.base.AbsAdapterItem;

import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by GHY on 2016/5/10.
 */
public class ListViewItemTypeLetterAdapter extends AbsAdapterItem<Map<String,String>> {

    @Override
    public int getItemLayout() {
        return R.layout.item_list_view_item_type_letter;
    }

    @Override
    public void init(View contentView) {

        ButterKnife.bind(this,contentView);

    }

    @Bind(R.id.item_tv_letter)
    TextView itemTvLetter;

    @Override
    public void bindData(Map<String,String> map) {
        itemTvLetter.setText(map.get("key"));
    }

}
