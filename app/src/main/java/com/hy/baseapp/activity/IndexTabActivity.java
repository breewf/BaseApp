package com.hy.baseapp.activity;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TabHost;

import com.hy.myapp.R;
import com.hy.myapp.activity.IndexTestCarActivity;
import com.hy.myapp.activity.IndexTestHomeActivity;
import com.hy.myapp.activity.IndexTestMeActivity;
import com.hy.myapp.activity.IndexTestMessageActivity;

/**
 * 使用RadioGroup的底部导航Tab Activity
 */
public class IndexTabActivity extends TabActivity implements RadioGroup.OnCheckedChangeListener {

    private RadioGroup mBottomTab;
    private TabHost mTabHost;

    private Intent mIntentPageOne;
    private Intent mIntentPageTwo;
    private Intent mIntentPageThree;
    private Intent mIntentPageFour;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.abs_index_tab_activity);
        mBottomTab = (RadioGroup) findViewById(R.id.main_tab);
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
            ((RadioButton) mBottomTab.findViewById(R.id.tab_intent1)).setChecked(true);
        } else if (source.equals("intent_page_two")) {
            ((RadioButton) mBottomTab.findViewById(R.id.tab_intent2)).setChecked(true);
        } else if (source.equals("intent_page_three")) {
            ((RadioButton) mBottomTab.findViewById(R.id.tab_intent3)).setChecked(true);
        } else if (source.equals("intent_page_four")) {
            ((RadioButton) mBottomTab.findViewById(R.id.tab_intent4)).setChecked(true);
        } else {
            //do nothing
        }
    }

    /**
     * 跳转到首页
     */
    public void goToHomeActivity() {
        Intent intent = new Intent();
        intent.putExtra("source", "intent_page_one");
        dispatchIntent(intent);
    }

    /**
     * 跳转到爱车
     */
    public void goToCarActivity() {
        Intent intent = new Intent();
        intent.putExtra("source", "intent_page_two");
        dispatchIntent(intent);
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
        mIntentPageOne = new Intent(this, IndexTestHomeActivity.class);
        mIntentPageTwo = new Intent(this, IndexTestCarActivity.class);
        mIntentPageThree = new Intent(this, IndexTestMessageActivity.class);
        mIntentPageFour = new Intent(this, IndexTestMeActivity.class);
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.tab_intent1:
                mTabHost.setCurrentTabByTag("intent_page_one");
                break;
            case R.id.tab_intent2:
                mTabHost.setCurrentTabByTag("intent_page_two");
                break;
            case R.id.tab_intent3:
                mTabHost.setCurrentTabByTag("intent_page_three");
                break;
            case R.id.tab_intent4:
                mTabHost.setCurrentTabByTag("intent_page_four");
                break;
        }
    }

}
