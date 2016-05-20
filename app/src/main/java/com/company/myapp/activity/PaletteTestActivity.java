package com.company.myapp.activity;

import android.animation.FloatEvaluator;
import android.animation.ValueAnimator;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.graphics.Palette;
import android.view.Window;
import android.view.animation.LinearInterpolator;

import com.company.myapp.R;
import com.company.myapp.fragment.PalettePageFragment;
import com.ghy.baseapp.base.AbsBaseActivity;

import butterknife.Bind;

public class PaletteTestActivity extends AbsBaseActivity implements AbsBaseActivity.ToolBarRightTvClickListener,
        ViewPager.OnPageChangeListener {

    @Bind(R.id.tab_layout_palette)
    TabLayout tabLayoutPalette;
    @Bind(R.id.tab_palette_viewpager)
    ViewPager viewPagerPalette;

    public static PaletteTestActivity mInstance;

    private String tabTitles[] = new String[]{"tab1", "tab2", "tab3", "tab4"};

    private boolean isFirstStart = true;

    @Override
    protected int getLayoutID() {
        return R.layout.activity_palette_test;
    }

    @Override
    protected String getToolBarTitle() {
        return "Palette颜色拾取";
    }

    @Override
    protected int setToolBarRightType() {
        return TOOLBAR_RIGHT_TYPE_TV;
    }

    @Override
    protected String setToolBarRightTv() {
        return "ListView";
    }

    @Override
    protected void init() {
        mInstance = this;

        setOnToolBarRightTvClickListener(this);

        //页面加载中
        setActivityStatus(ACTIVITY_STATUS_LOADING);

        viewPagerPalette.setAdapter(new MyTabPagerAdapter(getSupportFragmentManager()));
        viewPagerPalette.setOffscreenPageLimit(3);
        viewPagerPalette.setOnPageChangeListener(this);
        tabLayoutPalette.setupWithViewPager(viewPagerPalette);
        tabLayoutPalette.setTabMode(TabLayout.MODE_FIXED);

    }

    @Override
    public void onToolBarRightTvClick() {
        startActivity(this,PaletteListTestActivity.class);
    }

    public void getColor(Bitmap bitmap) {
        if (isFirstStart){
            setActivityStatus(ACTIVITY_STATUS_SUCCESS);
            isFirstStart = false;
        }
        if (bitmap == null) return;
        // 使用Palette来设置从Bitmap中提取出的颜色
        Palette.generateAsync(bitmap, new Palette.PaletteAsyncListener() {
            @Override
            public void onGenerated(Palette palette) {
                Palette.Swatch vibrantSwatch = palette.getVibrantSwatch();
                if (vibrantSwatch == null) return;
//                setColor(vibrantSwatch.getRgb());
                //使用颜色加深过度动画
                startAnimation(vibrantSwatch.getRgb());
            }
        });
    }

    /**
     * 设置颜色变化
     *
     * @param color
     */
    private void setColor(int color) {
        if (getToolBar() != null) getToolBar().setBackgroundColor(color);
        tabLayoutPalette.setBackgroundColor(color);
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            Window window = getWindow();
            //顶部状态栏颜色加深
            window.setStatusBarColor(getDarkerColor(color));
        }
    }

    /**
     * 使用动画过度颜色
     */
    private float colorAlpha;

    private void startAnimation(final int color) {
        ValueAnimator anim = ValueAnimator.ofObject(new FloatEvaluator(), 0, 0.2);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                colorAlpha = (Float) animation.getAnimatedValue();
                // 以处理过的变淡（0.3）的色值为参考色值起点，colorAlpha(color,(float) 0.3)
                // 在500毫秒内逐渐加深颜色到正常值（0.2）
                setColor(colorDark(colorAlpha(color,(float) 0.3),colorAlpha));
            }
        });
        anim.setInterpolator(new LinearInterpolator());
        anim.setDuration(500);
        anim.start();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        getColor(PalettePageFragment.bitmaps[position]);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    /**
     * 转换为深色
     *
     * @param color
     * @return
     */
    public int getDarkerColor(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv); // convert to hsv
        hsv[1] = hsv[1] + 0.1f; // more saturation
        hsv[2] = hsv[2] - 0.1f; // less brightness
        int darkerColor = Color.HSVToColor(hsv);
        return darkerColor;
    }

    /**
     * 转换为浅色
     *
     * @param color
     * @return
     */
    public int getBrighterColor(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv); // convert to hsv
        hsv[1] = hsv[1] - 0.1f; // less saturation
        hsv[2] = hsv[2] + 0.1f; // more brightness
        int brighterColor = Color.HSVToColor(hsv);
        return brighterColor;
    }

    /**
     * 颜色加深处理
     *
     * @param color
     * @return
     */
    public int colorDark(int color,float alpha) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv); // convert to hsv
        hsv[1] = hsv[1] + alpha; // more saturation
        hsv[2] = hsv[2] - alpha; // less brightness
        int darkerColor = Color.HSVToColor(hsv);
        return darkerColor;
    }

    /**
     * 颜色变淡处理
     *
     * @param color
     * @param alpha
     * @return
     */
    private int colorAlpha(int color, float alpha) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv); // convert to hsv
        hsv[1] = hsv[1] - alpha; // less saturation
        hsv[2] = hsv[2] + alpha; // more brightness
        int brighterColor = Color.HSVToColor(hsv);
        return brighterColor;
    }

    /**
     * ViewPager适配器
     */
    private class MyTabPagerAdapter extends FragmentPagerAdapter {

        public MyTabPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return PalettePageFragment.newInstance(position);
        }

        @Override
        public int getCount() {
            return tabTitles.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return tabTitles[position];
        }
    }
}
