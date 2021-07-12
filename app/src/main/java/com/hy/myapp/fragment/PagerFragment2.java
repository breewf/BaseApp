package com.hy.myapp.fragment;

import android.os.Bundle;
import android.view.View;

import com.hy.myapp.R;
import com.hy.baseapp.base.AbsBaseFragment;

/**
 * Created by HY on 2016/5/1.
 */
public class PagerFragment2 extends AbsBaseFragment {

    /** Fragment当前状态是否可见 */
    protected boolean isVisible = false;
    /** Fragment是否加载了数据 */
    protected boolean isLoad = false;

    @Override
    protected int getLayoutID() {
        return R.layout.fragment_pager2;
    }


    @Override
    protected void init(Bundle savedInstanceState, View contentView) {
//        Logger.i("fragment2 init。。。。。");
    }

    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(getUserVisibleHint()) {
            isVisible = true;
//            Logger.i("fragment2可见啦。。。。。");
            load();
            isLoad = true;
        } else {
            isVisible = false;
//            Logger.i("fragment2不可见。。。。。");
        }
    }

    private void load() {
        if (isLoad){
//            Logger.i("fragment2已经加载过数据，不再加载数据。。。。。");
        }else {
//            Logger.i("fragment2没有加载过数据，开始加载数据。。。。。");
        }

    }

}
