package com.hy.baseapp.component.dialog.animation.FlipEnter;

import android.util.DisplayMetrics;
import android.view.View;

import com.hy.baseapp.component.dialog.animation.BaseAnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;


public class FlipTopEnter extends BaseAnimatorSet {
	@Override
	public void setAnimation(View view) {
		DisplayMetrics dm = view.getContext().getResources().getDisplayMetrics();
		animatorSet.playTogether(
				ObjectAnimator.ofFloat(view, "rotationX", 90, 0),
				ObjectAnimator.ofFloat(view, "translationY", -200 * dm.density, 0),
				ObjectAnimator.ofFloat(view, "alpha", 0.2f, 1));
	}
}
