package com.company.myapp.activity;

import com.company.myapp.R;
import com.ghy.baseapp.base.AbsBaseActivity;

public class ListViewTest2Activity extends AbsBaseActivity {

    @Override
    protected int getLayoutID() {
        return R.layout.activity_list_view_test2;
    }

    @Override
    protected String getToolBarTitle() {
        return "带header和footer的ListView";
    }

    @Override
    protected void init() {

    }

}
