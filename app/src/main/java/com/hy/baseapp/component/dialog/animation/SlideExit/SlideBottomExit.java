package com.hy.baseapp.component.dialog.animation.SlideExit;

import android.util.DisplayMetrics;
import android.view.View;

import com.hy.baseapp.component.dialog.animation.BaseAnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;


public class SlideBottomExit extends BaseAnimatorSet {
	@Override
	public void setAnimation(View view) {
		DisplayMetrics dm = view.getContext().getResources().getDisplayMetrics();
		animatorSet.playTogether(
				ObjectAnimator.ofFloat(view, "translationY", 0, 250 * dm.density),
				ObjectAnimator.ofFloat(view, "alpha", 1, 0));
	}
}
