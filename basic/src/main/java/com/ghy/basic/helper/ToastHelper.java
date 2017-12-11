package com.ghy.basic.helper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.Toast;

/**
 * Toast助手类
 */
public class ToastHelper {

    @SuppressLint("StaticFieldLeak")
    private static ToastHelper mInstance;
    private Toast mToast;
    private Context context;

    private ToastHelper(Context context) {
        this.context = context;
    }

    /**
     * 获取Toast助手类实例
     *
     * @return
     */
    public static ToastHelper getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new ToastHelper(context.getApplicationContext());
        }
        return mInstance;
    }

    private Toast getSingleToast(int resId) {
        if (mToast == null) {
            mToast = Toast.makeText(context, resId, Toast.LENGTH_SHORT);
        } else {
            mToast.setText(resId);
        }
        return mToast;
    }

    private Toast getSingleToast(String text) {
        if (mToast == null) {
            mToast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
        } else {
            mToast.setText(text);
        }
        return mToast;
    }

    private Toast getSingleLongToast(int resId) {
        if (mToast == null) {
            mToast = Toast.makeText(context, resId, Toast.LENGTH_LONG);
        } else {
            mToast.setText(resId);
        }
        return mToast;
    }

    private Toast getSingleLongToast(String text) {
        if (mToast == null) {
            mToast = Toast.makeText(context, text, Toast.LENGTH_LONG);
        } else {
            mToast.setText(text);
        }
        return mToast;
    }

    private Toast getToast(int resId) {
        return Toast.makeText(context, resId, Toast.LENGTH_SHORT);
    }

    private Toast getToast(String text) {
        return Toast.makeText(context, text, Toast.LENGTH_SHORT);
    }

    private Toast getLongToast(int resId) {
        return Toast.makeText(context, resId, Toast.LENGTH_LONG);
    }

    private Toast getLongToast(String text) {
        return Toast.makeText(context, text, Toast.LENGTH_LONG);
    }

    public void showSingleToast(int resId) {
        getSingleToast(resId).show();
    }


    public void showSingleToast(String text) {
        getSingleToast(text).show();
    }

    public void showSingleLongToast(int resId) {
        getSingleLongToast(resId).show();
    }


    public void showSingleLongToast(String text) {
        getSingleLongToast(text).show();
    }

    public void showToast(int resId) {
        getToast(resId).show();
    }

    public void showToast(String text) {
        getToast(text).show();
    }

    public void showLongToast(int resId) {
        getLongToast(resId).show();
    }

    public void showLongToast(String text) {
        getLongToast(text).show();
    }

}
