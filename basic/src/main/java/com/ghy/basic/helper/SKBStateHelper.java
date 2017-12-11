package com.ghy.basic.helper;

import android.graphics.Rect;
import android.view.View;
import android.view.ViewTreeObserver;

/**
 * Created by GHY on 2016/12/26.
 * Desc:软键盘(softKeyboard)打开、关闭监听
 */

public class SKBStateHelper implements ViewTreeObserver.OnGlobalLayoutListener {

    /**
     * 键盘打开、关闭监听
     */
    public interface SoftKeyboardStateListener {
        void onSoftKeyboardOpened();

        void onSoftKeyboardClosed();
    }

    private SoftKeyboardStateListener listener;
    private final View activityRootView;
    private boolean isSoftKeyboardOpened;

    public SKBStateHelper(View activityRootView) {
        this(activityRootView, false);
    }

    public SKBStateHelper(View activityRootView, boolean isSoftKeyboardOpened) {
        this.activityRootView = activityRootView;
        this.isSoftKeyboardOpened = isSoftKeyboardOpened;
        activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(this);
    }

    @Override
    public void onGlobalLayout() {
        final Rect r = new Rect();
        //r will be populated with the coordinates of your view that area still visible.
        activityRootView.getWindowVisibleDisplayFrame(r);

        final int heightDiff = activityRootView.getRootView().getHeight() - (r.bottom - r.top);
        if (!isSoftKeyboardOpened && heightDiff > 100) { // if more than 100 pixels, its probably a keyboard...
            isSoftKeyboardOpened = true;
            notifyOnSoftKeyboardOpened();
        } else if (isSoftKeyboardOpened && heightDiff < 100) {
            isSoftKeyboardOpened = false;
            notifyOnSoftKeyboardClosed();
        }
    }

    public void setIsSoftKeyboardOpened(boolean isSoftKeyboardOpened) {
        this.isSoftKeyboardOpened = isSoftKeyboardOpened;
    }

    public boolean isSoftKeyboardOpened() {
        return isSoftKeyboardOpened;
    }

    public void setSoftKeyboardStateListener(SoftKeyboardStateListener listener) {
        this.listener = listener;
    }

    private void notifyOnSoftKeyboardOpened() {
        if (listener != null) listener.onSoftKeyboardOpened();
    }

    private void notifyOnSoftKeyboardClosed() {
        if (listener != null) listener.onSoftKeyboardClosed();
    }
}
