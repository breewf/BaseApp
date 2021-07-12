package com.hy.myapp.activity;

import com.hy.myapp.R;
import com.hy.baseapp.activity.IndexTabActivity;
import com.hy.baseapp.base.AbsBaseActivity;

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
