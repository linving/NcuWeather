package org.wangpeng.ncuweather.animations;
import android.view.animation.Animation;
import android.view.animation.Transformation;


public class btnAnima extends Animation {

	@Override
	public boolean isInitialized() {
		return super.isInitialized();
	}

	@Override
	protected void applyTransformation(float interpolatedTime, Transformation t) {
		
		t.getMatrix().setTranslate(0,  (float) (Math.sin(interpolatedTime*10)*30));
		
		super.applyTransformation(interpolatedTime, t);
	}

}
