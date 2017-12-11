package com.ghy.basic.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

/**
 * LoadingView 笑脸
 */

public class LVCircularSmile extends View {

    private Paint mPaint;

    private float mWidth = 0f;
    private float mEyeWidth = 0f;

    private float mPadding = 0f;
    private float startAngle = 0f;
    private boolean isSmile = false;
    private boolean isAnim = false;
    private float mAnimatedValue = 0f;
    private ValueAnimator valueAnimator;
    RectF rectF = new RectF();

    public LVCircularSmile(Context context) {
        this(context, null);
    }

    public LVCircularSmile(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LVCircularSmile(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initPaint();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (getMeasuredWidth() > getHeight())
            mWidth = getMeasuredHeight();
        else
            mWidth = getMeasuredWidth();
        mPadding = dip2px(10);
        mEyeWidth = dip2px(3);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        rectF = new RectF(mPadding, mPadding, mWidth - mPadding, mWidth - mPadding);
        mPaint.setStyle(Paint.Style.STROKE);
        //绘制下巴
        canvas.drawArc(rectF, startAngle, 180, false, mPaint);//第四个参数是否显示半径
        mPaint.setStyle(Paint.Style.FILL);
        if (!isAnim) {//绘制默认的两个小眼睛
            canvas.drawCircle(mPadding + mEyeWidth + mEyeWidth / 2, mWidth / 3, mEyeWidth, mPaint);
            canvas.drawCircle(mWidth - mPadding - mEyeWidth - mEyeWidth / 2, mWidth / 3, mEyeWidth, mPaint);
        }
        if (isSmile) {//下巴旋转时不绘制眼睛
            canvas.drawCircle(mPadding + mEyeWidth + mEyeWidth / 2, mWidth / 3, mEyeWidth, mPaint);
            canvas.drawCircle(mWidth - mPadding - mEyeWidth - mEyeWidth / 2, mWidth / 3, mEyeWidth, mPaint);
        }
    }

    /**
     * 初始化画笔
     */
    private void initPaint() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(Color.GRAY);
        mPaint.setStrokeWidth(dip2px(2));
    }

    /**
     * 设置颜色
     *
     * @param color
     */
    public void setViewColor(int color) {
        mPaint.setColor(color);
        postInvalidate();
    }

    private void OnAnimationUpdate(ValueAnimator valueAnimator) {
        mAnimatedValue = (float) valueAnimator.getAnimatedValue();
        if (mAnimatedValue < 0.5) {
            isSmile = false;
            startAngle = 720 * mAnimatedValue;
        } else {
            startAngle = 720;
            isSmile = true;
        }
        invalidate();
    }

    /**
     * 开启动画
     */
    public void startAnim() {
        stopAnim();
        startViewAnim(0f, 1f, 1000);
    }

    /**
     * 开启动画
     *
     * @param time 时间
     */
    public void startAnim(int time) {
        stopAnim();
        startViewAnim(0f, 1f, time);
    }

    /**
     * 停止动画
     */
    public void stopAnim() {
        isAnim = false;
        if (valueAnimator != null) {
            clearAnimation();
            valueAnimator.setRepeatCount(0);
            valueAnimator.cancel();
            valueAnimator.end();
            if (OnStopAnim() == 0) {
                valueAnimator.setRepeatCount(0);
                valueAnimator.cancel();
                valueAnimator.end();
            }
        }
    }

    private ValueAnimator startViewAnim(float startF, final float endF, long time) {
        isAnim = true;
        valueAnimator = ValueAnimator.ofFloat(startF, endF);
        valueAnimator.setDuration(time);
        valueAnimator.setInterpolator(new LinearInterpolator());

        valueAnimator.setRepeatCount(SetAnimRepeatCount());

        if (ValueAnimator.RESTART == SetAnimRepeatMode()) {
            valueAnimator.setRepeatMode(ValueAnimator.RESTART);
        } else if (ValueAnimator.REVERSE == SetAnimRepeatMode()) {
            valueAnimator.setRepeatMode(ValueAnimator.REVERSE);
        }

        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                OnAnimationUpdate(valueAnimator);
            }
        });
        if (!valueAnimator.isRunning()) {
            valueAnimator.start();
        }
        return valueAnimator;
    }

    private int OnStopAnim() {
        isSmile = false;
        mAnimatedValue = 0f;
        startAngle = 0f;
        return 0;
    }

    public int dip2px(float dpValue) {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    private int SetAnimRepeatMode() {
        return ValueAnimator.RESTART;
    }

    private int SetAnimRepeatCount() {
        return ValueAnimator.INFINITE;
    }
}
