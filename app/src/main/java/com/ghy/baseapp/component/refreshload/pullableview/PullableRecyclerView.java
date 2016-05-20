package com.ghy.baseapp.component.refreshload.pullableview;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

public class PullableRecyclerView extends RecyclerView implements Pullable {

    public PullableRecyclerView(Context context) {
        super(context);
    }

    public PullableRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PullableRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean canPullDown() {
        //得到当前显示的最后一个item的view
        View firstChildView = getLayoutManager().getChildAt(0);
        //得到firstChildView的top坐标值
        int firstChildTop = firstChildView.getTop();
        //得到RecyclerView的顶部坐标减去顶部padding值，也就是显示内容最顶部的坐标
        int recyclerTop = getTop() - getPaddingTop();
        //通过这个firstChildView得到这个view当前的position值
        int firstPosition = getLayoutManager().getPosition(firstChildView);
        //判断firstChildView的top值跟recyclerTop
        //判断firstPosition是不是第一个position
        //如果两个条件都满足则说明是真正的滑动到了顶部
        if (firstChildTop == recyclerTop && firstPosition == 0)
            return true;
        else
            return false;
    }

    @Override
    public boolean canPullUp() {
        //得到当前显示的最后一个item的view
        View lastChildView = getLayoutManager().getChildAt(getLayoutManager().getChildCount() - 1);
        //得到lastChildView的bottom坐标值
        int lastChildBottom = lastChildView.getBottom();
        //得到RecyclerView的底部坐标减去底部padding值，也就是显示内容最底部的坐标
        int recyclerBottom = getBottom() - getPaddingBottom();
        //通过这个lastChildView得到这个view当前的position值
        int lastPosition = getLayoutManager().getPosition(lastChildView);
        //判断lastChildView的bottom值跟recyclerBottom
        //判断lastPosition是不是最后一个position
        //如果两个条件都满足则说明是真正的滑动到了底部
        if (lastChildBottom == recyclerBottom && lastPosition == getLayoutManager().getItemCount() - 1)
            return true;
        else
            return false;
    }
}
