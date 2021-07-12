package com.hy.myapp.adapter;

import android.view.View;
import android.widget.TextView;

import com.hy.myapp.R;
import com.hy.baseapp.base.AbsAdapterItem;

import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by GHY on 2016/5/10.
 */
public class ListViewItemTypeContentAdapter extends AbsAdapterItem<Map<String,String>> {

    @Override
    public int getItemLayout() {
        return R.layout.item_list_view_item_type_content;
    }

    @Override
    public void init(View contentView) {

        ButterKnife.bind(this,contentView);

    }

    @Bind(R.id.item_tv_content)
    TextView itemTvContent;

    @Override
    public void bindData(Map<String,String> map) {
        itemTvContent.setText(map.get("key"));
    }

}
