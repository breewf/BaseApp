package com.company.myapp.activity;

import com.company.myapp.R;
import com.ghy.baseapp.base.AbsBaseActivity;

public class ListViewTest1Activity extends AbsBaseActivity {

    @Override
    protected int getLayoutID() {
        return R.layout.activity_list_view_test1;
    }

    @Override
    protected String getToolBarTitle() {
        return "普通ListView";
    }

    @Override
    protected void init() {

    }

}
