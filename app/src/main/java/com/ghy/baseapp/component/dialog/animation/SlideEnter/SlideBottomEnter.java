package com.ghy.baseapp.component.dialog.animation.SlideEnter;

import android.util.DisplayMetrics;
import android.view.View;

import com.ghy.baseapp.component.dialog.animation.BaseAnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;


public class SlideBottomEnter extends BaseAnimatorSet {
	@Override
	public void setAnimation(View view) {
		DisplayMetrics dm = view.getContext().getResources().getDisplayMetrics();
		animatorSet.playTogether(
				ObjectAnimator.ofFloat(view, "translationY", 250 * dm.density, 0),
				ObjectAnimator.ofFloat(view, "alpha", 0.2f, 1));
	}
}
