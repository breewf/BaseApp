package com.hy.myapp.adapter;

import android.view.View;
import android.widget.TextView;

import com.hy.myapp.R;
import com.hy.baseapp.base.AbsAdapterItem;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by GHY on 2016/5/10.
 */
public class ListView2Adapter extends AbsAdapterItem<String> {
    @Override
    public int getItemLayout() {
        return R.layout.item_list_view_test2;
    }

    @Override
    public void init(View contentView) {

        ButterKnife.bind(this,contentView);

    }

    @Bind(R.id.item_tv_test2)
    TextView itemTvTest2;

    @Override
    public void bindData(String s) {
        itemTvTest2.setText(s);
    }

}
