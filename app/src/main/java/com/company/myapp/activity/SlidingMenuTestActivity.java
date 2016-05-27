package com.company.myapp.activity;

import android.view.View;
import android.widget.TextView;

import com.company.myapp.R;
import com.ghy.baseapp.base.AbsBaseActivity;
import com.ghy.baseapp.component.slidingmenu.SlidingMenu;

public class SlidingMenuTestActivity extends AbsBaseActivity {

    private SlidingMenu slidingMenu;

    @Override
    protected int getLayoutID() {
        return R.layout.activity_sliding_menu_test;
    }

    @Override
    protected String getToolBarTitle() {
        return "SlidingMenu侧滑菜单测试";
    }

    @Override
    protected boolean isOpenToolBar() {
        return false;
    }

    @Override
    protected void init() {

        initSlidingMenu();

    }

    /**
     * 初始化侧滑菜单
     */
    private void initSlidingMenu() {
        slidingMenu = new SlidingMenu(this);
        slidingMenu.setMode(SlidingMenu.LEFT);
        slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
        slidingMenu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        slidingMenu.setFadeDegree(0.35f);
        slidingMenu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
        slidingMenu.setMenu(R.layout.layout_sliding_menu);

        initSlidingView();
    }

    private void initSlidingView() {
        TextView tvLogin = (TextView) findViewById(R.id.slid_user_login);
        TextView tvRegist = (TextView) findViewById(R.id.slid_user_regist);
        if (tvLogin != null)
            tvLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
        if (tvRegist != null)
            tvRegist.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
    }

}
