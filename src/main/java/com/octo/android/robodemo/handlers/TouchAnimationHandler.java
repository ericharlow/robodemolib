package com.octo.android.robodemo.handlers;

import com.octo.android.robodemo.DrawView;

import android.app.Activity;
import android.view.MotionEvent;

/**
 * Touch Driven Showcase - shows next point based on a touch, touches are not passed through
 * @author ericharlow
 */
public class TouchAnimationHandler extends BaseDemoHandler {

	public TouchAnimationHandler(DrawView drawView) {
		super(drawView);
	}

	@Override
	public boolean dispatchTouchEventDelegate(MotionEvent event) {
		DrawView drawView = getDrawView();
		if (drawView == null) return false;

		// Show Next Point
		if (event.getAction() == MotionEvent.ACTION_UP) {
            if (drawView.getDrawViewAdapter().progressToNextPoint()) {
                Activity context = null;
                if (drawView.getNextPointListener() != null) {
                    context = drawView.getNextPointListener().onNeedContext();
                }
                drawView.getDrawViewAdapter().updateCurrentPoint(context);
                onForceDrawUpdate();
            } else {
                onForceFinishUpdate();
            }
		}
		return true;
	}

	@Override
	public void sendEmptyMessageDelayed() {
	}

}
