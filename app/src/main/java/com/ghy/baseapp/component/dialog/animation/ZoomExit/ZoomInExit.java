package com.ghy.baseapp.component.dialog.animation.ZoomExit;

import android.view.View;

import com.ghy.baseapp.component.dialog.animation.BaseAnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;


public class ZoomInExit extends BaseAnimatorSet {
	@Override
	public void setAnimation(View view) {
		animatorSet.playTogether(
				ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.25f, 0),
				ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.25f, 0),
				ObjectAnimator.ofFloat(view, "alpha", 1, 0, 0));
	}
}
