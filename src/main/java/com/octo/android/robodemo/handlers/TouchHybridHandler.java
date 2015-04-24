package com.octo.android.robodemo.handlers;

import com.octo.android.robodemo.DrawView;
import com.octo.android.robodemo.DrawViewAdapter;

import android.app.Activity;
import android.view.MotionEvent;

/**
 * Mix bag of everything else.
 * @author ericharlow
 */
public class TouchHybridHandler extends BaseDemoHandler {

	public TouchHybridHandler(DrawView drawView) {
		super(drawView);
	}

	@Override
	public boolean dispatchTouchEventDelegate(MotionEvent event) {
		DrawView drawView = getDrawView();
    	if (drawView == null) return false;
    	
    	DrawViewAdapter adapter = drawView.getDrawViewAdapter();
    	
		if (drawView.isTouchEventInLabeledPoint(event, adapter.getDrawableAt(adapter.getCurrentPosition()))) {
    		// Show Next Point
    		if (event.getAction() == MotionEvent.ACTION_UP) {
    			if(!adapter.progressToNextPoint()) {
                    onForceFinishUpdate();
                    return true;
                }
    		}

    		// Handle Touch Event
    		if (drawView.getTouchDispatchDelegate() != null) { // should exist only if touchHandler not touchAnimationHandler
    			boolean handled = drawView.getTouchDispatchDelegate().sendTouchEvent(event);
    			// Handle funky touchHybrid cases
    			if (handled == false) {
    				onForceDrawUpdate();
    				handled = true;
    			} else {
    				Activity context = null;
    				if (drawView.getNextPointListener() != null) {
    					context = drawView.getNextPointListener().onNeedContext();
    				}
    				adapter.updateCurrentPoint(context);
    				if (getForceDrawUpdate())
        				onForceDrawUpdate();
    			}
    			return handled;
    		} else
    			return true;
    	}
		return false;
	}

	@Override
	public void sendEmptyMessageDelayed() {}
}
