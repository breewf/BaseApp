package com.company.myapp.activity;

import com.company.myapp.R;
import com.ghy.baseapp.base.AbsBaseActivity;

public class RecyclerViewTest1Activity extends AbsBaseActivity {

    @Override
    protected int getLayoutID() {
        return R.layout.activity_recycler_view_test1;
    }

    @Override
    protected String getToolBarTitle() {
        return "带下拉上拉的RecyclerView";
    }

    @Override
    protected void init() {

    }

}
