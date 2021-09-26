package com.hy.baseapp.helper;

import android.view.View;

import com.google.android.material.snackbar.Snackbar;

/**
 * Created by GHY on 2016/5/9.
 * SnackBar弹出框
 */
public class SnackBarHelper {

    public interface OnSnackBarClickListener {
        void onClick();
    }

    /**
     * 显示一个SnackBar
     *
     * @param view
     * @param content
     * @param actionString
     * @param listener
     */
    public static void showSnackBar(View view, String content, String actionString, final OnSnackBarClickListener listener) {
        Snackbar.make(view, content, Snackbar.LENGTH_SHORT).setAction(actionString, v -> {
            if (listener != null) {
                listener.onClick();
            }
        }).show();
    }

    /**
     * 显示一个SnackBar
     *
     * @param view
     * @param content
     * @param actionString
     * @param listener
     */
    public static void showSnackBarLong(View view, String content, String actionString, final OnSnackBarClickListener listener) {
        Snackbar.make(view, content, Snackbar.LENGTH_LONG).setAction(actionString, v -> {
            if (listener != null) {
                listener.onClick();
            }
        }).show();
    }

    /**
     * 显示一个SnackBar
     *
     * @param view
     * @param content
     * @param actionString
     * @param color
     * @param listener
     */
    public static void showSnackBarCustom(View view, String content, String actionString, int color, final OnSnackBarClickListener listener) {
        Snackbar.make(view, content, Snackbar.LENGTH_SHORT).setActionTextColor(color)
                .setAction(actionString, v -> {
                    if (listener != null) {
                        listener.onClick();
                    }
                }).show();
    }

    /**
     * 显示一个SnackBar
     *
     * @param view
     * @param content
     * @param actionString
     * @param duration
     * @param color
     * @param listener
     */
    public static void showSnackBarCustom(View view, String content, String actionString, int duration, int color, final OnSnackBarClickListener listener) {
        Snackbar.make(view, content, Snackbar.LENGTH_SHORT).setDuration(duration).setActionTextColor(color)
                .setAction(actionString, v -> {
                    if (listener != null) {
                        listener.onClick();
                    }
                }).show();
    }
}
