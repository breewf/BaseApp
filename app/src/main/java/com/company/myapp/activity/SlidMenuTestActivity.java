package com.company.myapp.activity;

import android.view.View;

import com.company.myapp.R;
import com.ghy.baseapp.base.AbsBaseActivity;
import com.ghy.baseapp.component.slidmenu.SlidingMenu;

import butterknife.Bind;

public class SlidMenuTestActivity extends AbsBaseActivity {

    @Bind(R.id.id_slid_menu)
    SlidingMenu slidingMenu;

    @Override
    protected int getLayoutID() {
        return R.layout.activity_slid_menu_test;
    }

    @Override
    protected String getToolBarTitle() {
        return "SlidingMenu侧滑菜单";
    }

    @Override
    protected boolean isOpenToolBar() {
        return false;
    }

    @Override
    protected void init() {

    }

    /**
     * 打开关闭侧滑按钮
     *
     * @param view
     */
    public void toggleMenu(View view) {
        slidingMenu.toggle();
    }

}
