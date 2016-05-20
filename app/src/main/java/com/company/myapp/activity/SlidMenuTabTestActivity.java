package com.company.myapp.activity;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TabHost;

import com.company.myapp.R;
import com.ghy.baseapp.component.slidmenu.SlidingMenu;

public class SlidMenuTabTestActivity extends TabActivity implements RadioGroup.OnCheckedChangeListener{

    private RadioGroup mBottomTab;
    private TabHost mTabHost;
    private SlidingMenu slidingMenu;

    private Intent mIntentPageOne;
    private Intent mIntentPageTwo;
    private Intent mIntentPageThree;
    private Intent mIntentPageFour;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slid_menu_tab_test);
        slidingMenu = (SlidingMenu) findViewById(R.id.id_slid_menu_tab);
        //设置侧滑菜单不缩放，只向右展开
        slidingMenu.setScale(false);
        mBottomTab = (RadioGroup) findViewById(R.id.slid_main_tab);
        mBottomTab.setOnCheckedChangeListener(this);
        mTabHost = getTabHost();
        initTabIntent();
        initTabs();
    }

    /**
     * 分发intent
     * 用于跳转到各个页面
     *
     * @param intent
     */
    private void dispatchIntent(Intent intent) {
        if (intent == null || intent.getStringExtra("source") == null) return;
        String source = intent.getStringExtra("source");
        mTabHost.setCurrentTabByTag(source);
        if (source.equals("intent_page_one")) {
            ((RadioButton) mBottomTab.findViewById(R.id.slid_tab_intent1)).setChecked(true);
        } else if (source.equals("intent_page_two")) {
            ((RadioButton) mBottomTab.findViewById(R.id.slid_tab_intent2)).setChecked(true);
        } else if (source.equals("intent_page_three")) {
            ((RadioButton) mBottomTab.findViewById(R.id.slid_tab_intent3)).setChecked(true);
        } else if (source.equals("intent_page_four")) {
            ((RadioButton) mBottomTab.findViewById(R.id.slid_tab_intent4)).setChecked(true);
        } else {
            //do nothing
        }
    }


    private void initTabs() {
        mTabHost.addTab(mTabHost
                .newTabSpec("intent_page_one")
                .setIndicator("intent_page_one", getResources().getDrawable(R.mipmap.ic_launcher))
                .setContent(mIntentPageOne));

        mTabHost.addTab(mTabHost
                .newTabSpec("intent_page_two")
                .setIndicator("intent_page_two", getResources().getDrawable(R.mipmap.ic_launcher))
                .setContent(mIntentPageTwo));

        mTabHost.addTab(mTabHost
                .newTabSpec("intent_page_three")
                .setIndicator("intent_page_three", getResources().getDrawable(R.mipmap.ic_launcher))
                .setContent(mIntentPageThree));

        mTabHost.addTab(mTabHost
                .newTabSpec("intent_page_four")
                .setIndicator("intent_page_four", getResources().getDrawable(R.mipmap.ic_launcher))
                .setContent(mIntentPageFour));
    }

    private void initTabIntent() {
        mIntentPageOne = new Intent(this, SlidMenuIndexTestHomeActivity.class);
        mIntentPageTwo = new Intent(this, SlidMenuIndexTestCarActivity.class);
        mIntentPageThree = new Intent(this,SlidMenuIndexTestMessageActivity.class);
        mIntentPageFour = new Intent(this, SlidMenuIndexTestMeActivity.class);
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.slid_tab_intent1:
                mTabHost.setCurrentTabByTag("intent_page_one");
                break;
            case R.id.slid_tab_intent2:
                mTabHost.setCurrentTabByTag("intent_page_two");
                break;
            case R.id.slid_tab_intent3:
                mTabHost.setCurrentTabByTag("intent_page_three");
                break;
            case R.id.slid_tab_intent4:
                mTabHost.setCurrentTabByTag("intent_page_four");
                break;
        }
    }

}
