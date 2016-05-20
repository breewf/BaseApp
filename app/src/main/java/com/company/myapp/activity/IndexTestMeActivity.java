package com.company.myapp.activity;

import com.company.myapp.R;
import com.ghy.baseapp.activity.IndexTabActivity;
import com.ghy.baseapp.base.AbsBaseActivity;

import butterknife.OnClick;

public class IndexTestMeActivity extends AbsBaseActivity {

    @Override
    protected int getLayoutID() {
        return R.layout.activity_index_test_me;
    }

    @Override
    protected boolean isOpenToolBar() {
        return false;
    }

    @Override
    protected void init() {

    }

    @OnClick(R.id.tv_me_to_home)
    public void goToHome(){
        ((IndexTabActivity) this.getParent()).goToHomeActivity();
    }
}
