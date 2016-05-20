package com.ghy.baseapp.component.dialog.animation.FlipEnter;

import android.view.View;

import com.ghy.baseapp.component.dialog.animation.BaseAnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;


public class FlipVerticalSwingEnter extends BaseAnimatorSet {
	public FlipVerticalSwingEnter() {
		duration = 1000;
	}

	@Override
	public void setAnimation(View view) {
		animatorSet.playTogether(
				ObjectAnimator.ofFloat(view, "rotationX", 90, -10, 10, 0),
				ObjectAnimator.ofFloat(view, "alpha", 0.25f, 0.5f, 0.75f, 1));
	}
}
