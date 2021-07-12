package com.hy.baseapp.helper;

import android.animation.ObjectAnimator;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.CycleInterpolator;
import android.view.animation.TranslateAnimation;


/**
 * Created by HY on 2016/12/26.
 * 动画工具类
 */
public class AnimHelper {

    /**
     * 淡入
     */
    public static void fadeIn(View view) {
        fadeIn(view, 400, 0);
    }

    /**
     * 淡入
     */
    public static void fadeIn(View view, long duration) {
        fadeIn(view, duration, 0);
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
    public static void fadeOut(View view, long duration) {
        fadeOut(view, duration, 0);
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

    /**
     * EditText抖动动画
     *
     * @param view
     */
    public static void shakeAnim(View view) {
        Animation translateAnimation = new TranslateAnimation(0, 10, 0, 0);
        translateAnimation.setInterpolator(new CycleInterpolator(4));
        translateAnimation.setDuration(400);
        view.startAnimation(translateAnimation);
    }

    /**
     * 向下arrow箭头向上旋转动画
     *
     * @param view
     */
    public static void arrowUpAnim(View view) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "rotation", 0f, 180f);
        animator.setDuration(400);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.start();
    }

    /**
     * 向上arrow箭头向下旋转动画
     *
     * @param view
     */
    public static void arrowDownAnim(View view) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "rotation", 180f, 0f);
        animator.setDuration(400);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.start();
    }

    /**
     * 旋转
     *
     * @param view       旋转的view
     * @param angleValue 要旋转的角度
     * @param duration   时长
     */
    public static void rotationAngle(View view, float angleValue, long duration) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "rotation", 0f, angleValue);
        animator.setDuration(duration);
        animator.start();
    }

}
