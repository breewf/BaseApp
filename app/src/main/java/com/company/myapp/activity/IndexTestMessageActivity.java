package com.company.myapp.activity;

import com.company.myapp.R;
import com.ghy.baseapp.activity.IndexTabActivity;
import com.ghy.baseapp.base.AbsBaseActivity;

import butterknife.OnClick;

public class IndexTestMessageActivity extends AbsBaseActivity {

    @Override
    protected int getLayoutID() {
        return R.layout.activity_index_test_message;
    }

    @Override
    protected boolean isOpenToolBar() {
        return false;
    }

    @Override
    protected void init() {

    }

    @OnClick(R.id.tv_go_car_page)
    public void goCarPage(){
        ((IndexTabActivity) this.getParent()).goToCarActivity();
    }
}
