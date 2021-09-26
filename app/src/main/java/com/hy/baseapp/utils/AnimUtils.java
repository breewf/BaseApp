package com.hy.baseapp.utils;

import android.animation.ObjectAnimator;
import android.view.View;

/**
 * @author hy
 * @date 2021/9/26
 * desc:
 **/
public class AnimUtils {

    /**
     * 淡入
     */
    public static void fadeIn(View view) {
        fadeIn(view, 400, 0);
    }

    /**
     * 淡入
     */
    public static void fadeIn(View view, long duration, long delay) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
        animator.setDuration(duration);
        animator.setStartDelay(delay);
        animator.start();
    }

    /**
     * 淡出
     */
    public static void fadeOut(View view) {
        fadeOut(view, 400, 0);
    }

    /**
     * 淡出
     */
    public static void fadeOut(View view, long duration, long delay) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f);
        animator.setDuration(duration);
        animator.setStartDelay(delay);
        animator.start();
    }

}
