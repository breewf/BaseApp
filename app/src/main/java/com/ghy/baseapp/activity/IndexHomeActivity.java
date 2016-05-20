package com.ghy.baseapp.activity;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import com.company.myapp.R;
import com.company.myapp.fragment.PagerFragment1;
import com.company.myapp.fragment.PagerFragment2;
import com.company.myapp.fragment.PagerFragment3;
import com.company.myapp.fragment.PagerFragment4;
import com.ghy.baseapp.base.AbsBaseActivity;
import com.ghy.baseapp.view.MyTabView;

import java.util.ArrayList;
import java.util.List;

/**
 * 使用fragment+viewPager的导航Tab Activity
 */
public class IndexHomeActivity extends AbsBaseActivity {

    private String[] mTitle = {"首页", "爱车", "消息", "我"};
    private int[] mIconSelect = {R.mipmap.tab_icon_home_select, R.mipmap.tab_icon_car_select, R.mipmap.tab_icon_message_select, R.mipmap.tab_icon_me_select};
    private int[] mIconNormal = {R.mipmap.tab_icon_home, R.mipmap.tab_icon_car, R.mipmap.tab_icon_message, R.mipmap.tab_icon_me};
    private ViewPager mViewPager;
    private MyTabView mTabView;

    private List<Fragment> fragments;

    @Override
    protected int getLayoutID() {
        return R.layout.abs_index_home_activity;
    }

    @Override
    protected String getToolBarTitle() {
        return "底部Tab颜色渐变";
    }

    @Override
    protected void init() {

        initFragment();

        mViewPager = (ViewPager) findViewById(R.id.id_view_pager);
        mViewPager.setAdapter(new PageAdapter(getSupportFragmentManager()));
        mViewPager.setOffscreenPageLimit(3);
        mTabView = (MyTabView) findViewById(R.id.id_tab);
        if (mTabView != null) mTabView.setViewPager(mViewPager);
    }

    private void initFragment() {
        fragments = new ArrayList<>();
        Fragment fragment1 = new PagerFragment1();
        fragments.add(fragment1);
        Fragment fragment2 = new PagerFragment2();
        fragments.add(fragment2);
        Fragment fragment3 = new PagerFragment3();
        fragments.add(fragment3);
        Fragment fragment4 = new PagerFragment4();
        fragments.add(fragment4);
    }

    /**
     * pagerAdapter
     */
    class PageAdapter extends FragmentPagerAdapter implements MyTabView.OnItemIconTextSelectListener {

        public PageAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int[] onIconSelect(int position) {
            int icon[] = new int[2];
            icon[0] = mIconSelect[position];
            icon[1] = mIconNormal[position];
            return icon;
        }

        @Override
        public String onTextSelect(int position) {
            return mTitle[position];
        }

        @Override
        public int getCount() {
            return mTitle.length;
        }
    }

}
