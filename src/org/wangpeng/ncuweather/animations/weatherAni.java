package org.wangpeng.ncuweather.animations;

import android.view.animation.Animation;
import android.view.animation.Transformation;

public class weatherAni extends Animation {

	@Override
	protected void applyTransformation(float interpolatedTime, Transformation t) {
		
		//t.getMatrix().setTranslate((float) (Math.sin(interpolatedTime*10)*10), 0);
		t.setAlpha(interpolatedTime);
		super.applyTransformation(interpolatedTime, t);
	}

}
