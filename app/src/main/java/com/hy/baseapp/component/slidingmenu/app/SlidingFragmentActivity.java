package com.hy.baseapp.component.slidingmenu.app;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

import com.hy.baseapp.component.slidingmenu.SlidingMenu;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

public class SlidingFragmentActivity extends FragmentActivity implements SlidingActivityBase {

    private SlidingActivityHelper mHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHelper = new SlidingActivityHelper(this);
        mHelper.onCreate(savedInstanceState);
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mHelper.onPostCreate(savedInstanceState);
    }

    @Override
    public View findViewById(int id) {
        View v = super.findViewById(id);
        if (v != null) {
            return v;
        }
        return mHelper.findViewById(id);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mHelper.onSaveInstanceState(outState);
    }

    @Override
    public void setContentView(int id) {
        setContentView(getLayoutInflater().inflate(id, null));
    }

    @Override
    public void setContentView(View v) {
        setContentView(v, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
    }

    @Override
    public void setContentView(View v, LayoutParams params) {
        super.setContentView(v, params);
        mHelper.registerAboveContentView(v, params);
    }

    @Override
    public void setBehindContentView(int id) {
        setBehindContentView(getLayoutInflater().inflate(id, null));
    }

    @Override
    public void setBehindContentView(View v) {
        setBehindContentView(v, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
    }

    @Override
    public void setBehindContentView(View v, LayoutParams params) {
        mHelper.setBehindContentView(v, params);
    }

    @Override
    public SlidingMenu getSlidingMenu() {
        return mHelper.getSlidingMenu();
    }

    @Override
    public void toggle() {
        mHelper.toggle();
    }

    @Override
    public void showContent() {
        mHelper.showContent();
    }

    @Override
    public void showMenu() {
        mHelper.showMenu();
    }

    @Override
    public void showSecondaryMenu() {
        mHelper.showSecondaryMenu();
    }

    @Override
    public void setSlidingActionBarEnabled(boolean b) {
        mHelper.setSlidingActionBarEnabled(b);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        boolean b = mHelper.onKeyUp(keyCode, event);
        if (b) {
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

}
