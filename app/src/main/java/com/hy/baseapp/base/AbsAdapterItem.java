package com.hy.baseapp.base;

import android.view.View;

/**
 * Created by GHY on 16/05/10.
 * listView adapter Item类型
 */
public abstract class AbsAdapterItem<T> {

    public abstract int getItemLayout();

    public abstract void init(View contentView);

    public abstract void bindData(T t);

}
