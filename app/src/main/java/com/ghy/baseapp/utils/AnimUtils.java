package com.ghy.baseapp.utils;

import android.animation.ObjectAnimator;

/**
 * Created by HY on 2016/5/1.
 * 动画工具类
 */
public class AnimUtils {

    /**
     * 淡入
     */
    public static void fadeIn(Object object){
        fadeIn(object,400,0);
    }

    /**
     * 淡入
     */
    public static void fadeIn(Object object,long duration,long delay){
        ObjectAnimator animator = ObjectAnimator.ofFloat(object, "alpha", 0f, 1f);
        animator.setDuration(duration);
        animator.setStartDelay(delay);
        animator.start();
    }

    /**
     * 淡出
     */
    public static void fadeOut(Object object){
        fadeOut(object,400,0);
    }

    /**
     * 淡出
     */
    public static void fadeOut(Object object,long duration,long delay){
        ObjectAnimator animator = ObjectAnimator.ofFloat(object, "alpha", 1f, 0f);
        animator.setDuration(duration);
        animator.setStartDelay(delay);
        animator.start();
    }

}
