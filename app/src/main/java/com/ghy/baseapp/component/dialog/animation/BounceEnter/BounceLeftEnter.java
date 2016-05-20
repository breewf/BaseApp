package com.ghy.baseapp.component.dialog.animation.BounceEnter;

import android.util.DisplayMetrics;
import android.view.View;

import com.ghy.baseapp.component.dialog.animation.BaseAnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;


public class BounceLeftEnter extends BaseAnimatorSet {
	public BounceLeftEnter() {
		duration = 1000;
	}

	@Override
	public void setAnimation(View view) {

		DisplayMetrics dm = view.getContext().getResources().getDisplayMetrics();
		animatorSet.playTogether(ObjectAnimator.ofFloat(view, "alpha", 0, 1, 1, 1),
				ObjectAnimator.ofFloat(view, "translationX", -250 * dm.density, 30, -10, 0));
	}
}
