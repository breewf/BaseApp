package com.ghy.basic.component.scrollgesture;

import android.view.GestureDetector;
import android.view.MotionEvent;

/**
 * Created by GHY on 2017/5/4.
 * Desc: 滑动方向监听
 */

public class ScrollGestureListener extends GestureDetector.SimpleOnGestureListener {

    public static final int SCROLL_DIR_UP = 1;
    public static final int SCROLL_DIR_RIGHT = 2;
    public static final int SCROLL_DIR_DOWN = 3;
    public static final int SCROLL_DIR_LEFT = 4;

    public interface ScrollCallBack {
        void scrollDirection(int direction);
    }

    private ScrollCallBack mScrollCallBack;

    public ScrollGestureListener(ScrollCallBack scrollCallBack) {
        mScrollCallBack = scrollCallBack;
    }

    @Override
    public boolean onScroll(MotionEvent startEvent, MotionEvent finishEvent, float distanceX, float distanceY) {
        if (startEvent == null || finishEvent == null)
            return super.onScroll(startEvent, finishEvent, distanceX, distanceY);

        float moveX = finishEvent.getX() - startEvent.getX();
        float moveY = finishEvent.getY() - startEvent.getY();
        if (Math.abs(moveX) > Math.abs(moveY)) {
            if (Math.abs(moveX) > 120.0F) {
                if (moveX > 0.0F) {
                    //SCROLL_DIR_RIGHT
                    if (mScrollCallBack != null)
                        mScrollCallBack.scrollDirection(SCROLL_DIR_RIGHT);
                } else {
                    //SCROLL_DIR_LEFT
                    if (mScrollCallBack != null)
                        mScrollCallBack.scrollDirection(SCROLL_DIR_LEFT);
                }
            }
        } else if (Math.abs(moveY) > 120.0F) {
            if (moveY > 0.0F) {
                //SCROLL_DIR_DOWN
                if (mScrollCallBack != null) mScrollCallBack.scrollDirection(SCROLL_DIR_DOWN);
            } else {
                //SCROLL_DIR_UP
                if (mScrollCallBack != null) mScrollCallBack.scrollDirection(SCROLL_DIR_UP);
            }
        }
        return super.onScroll(startEvent, finishEvent, distanceX, distanceY);
    }

}

