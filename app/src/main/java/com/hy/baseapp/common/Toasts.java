package com.hy.baseapp.common;

import android.app.Application;
import android.view.Gravity;
import android.widget.Toast;

import com.hy.myapp.App;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

/**
 * @author hy
 * @date 2021/7/12
 * desc: Toasts
 **/
public class Toasts {


    private static int sGravity = -1;
    private static int sXOffset = -1;
    private static int sYOffset = -1;

    private static Toast mToast;

    private static Application mApplication;

    public static void install(Application application) {
        mApplication = application;
        {
            sGravity = Gravity.CENTER;
            sXOffset = 0;
            sYOffset = 10;
        }
    }

    public static void showShort(String msg) {
        show(msg, Toast.LENGTH_SHORT);
    }

    public static void showLong(String msg) {
        show(msg, Toast.LENGTH_LONG);
    }

    private static void show(String msg, int duration) {
        showToast(msg, duration);
    }

    /**
     * 设置吐司位置
     *
     * @param gravity 位置
     * @param xOffset x 偏移
     * @param yOffset y 偏移
     */
    public static void setGravity(final int gravity, final int xOffset, final int yOffset) {
        sGravity = gravity;
        sXOffset = xOffset;
        sYOffset = yOffset;
    }

    private static synchronized void showToast(CharSequence s, int duration) {
        Observable.just(s)
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(new SubscriberExtension<CharSequence>() {
                    @Override
                    public void onCall(CharSequence s) {
                        check();
                        if (mToast != null) {
                            mToast.setText(s);
                            mToast.setDuration(duration);
                            if (sGravity != -1 || sXOffset != -1 || sYOffset != -1) {
                                mToast.setGravity(sGravity, sXOffset, sYOffset);
                            }
                            mToast.show();
                        }
                    }

                    @Override
                    public void onCompleted() {
                        super.onCompleted();
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                    }
                });

    }

    @SuppressWarnings("all")
    private static void check() {
        if (mApplication == null) {
            mApplication = App.getInstance();
        }
        if (mToast == null) {
            mToast = Toast.makeText(mApplication, "", Toast.LENGTH_SHORT);
        } else {
            mToast.cancel();
            mToast = null;
            mToast = Toast.makeText(mApplication, "", Toast.LENGTH_SHORT);
        }
    }

}
