package com.hy.myapp.fragment;

import android.os.Bundle;
import android.view.View;

import com.hy.myapp.R;
import com.hy.baseapp.base.AbsBaseFragment;

/**
 * Created by HY on 2016/5/1.
 */
public class PagerFragment1 extends AbsBaseFragment {

    @Override
    protected int getLayoutID() {
        return R.layout.fragment_pager1;
    }


    @Override
    protected void init(Bundle savedInstanceState, View contentView) {
//        Logger.i("fragment1 init。。。");
    }

}
