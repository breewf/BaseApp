package com.ghy.baseapp.activity;

import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import com.company.myapp.R;
import com.company.myapp.fragment.PagerFragment1;
import com.company.myapp.fragment.PagerFragment2;
import com.company.myapp.fragment.PagerFragment3;
import com.ghy.baseapp.base.AbsBaseActivity;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;

public class TabLayoutActivity extends AbsBaseActivity {

    @Bind(R.id.tab_layout)
    TabLayout tabLayout;
    @Bind(R.id.tab_layout_viewpager)
    ViewPager viewPager;

    private String tabTitles[] = new String[]{"tab1","tab2","tab3"};
    private List<Fragment> fragments;

    @Override
    protected int getLayoutID() {
        return R.layout.activity_tab_layout;
    }

    @Override
    protected String getToolBarTitle() {
        return "谷歌TabLayout使用示例";
    }

    @Override
    protected void init() {

        initFragment();

        viewPager.setAdapter(new MyTabPagerAdapter(getSupportFragmentManager()));
        tabLayout.setupWithViewPager(viewPager);
        //MODE_FIXED是占满布局，MODE_SCROLLABLE是适应可滑动
        tabLayout.setTabMode(TabLayout.MODE_FIXED);

    }

    private void initFragment() {
        fragments = new ArrayList<>();
        Fragment fragment1 = new PagerFragment1();
        fragments.add(fragment1);
        Fragment fragment2 = new PagerFragment2();
        fragments.add(fragment2);
        Fragment fragment3 = new PagerFragment3();
        fragments.add(fragment3);
    }

    /**
     * ViewPager适配器
     */
    private class MyTabPagerAdapter extends FragmentPagerAdapter{

        public MyTabPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return tabTitles[position];
        }
    }
}
