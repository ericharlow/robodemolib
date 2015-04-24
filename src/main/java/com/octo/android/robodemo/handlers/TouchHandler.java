package com.octo.android.robodemo.handlers;

import com.octo.android.robodemo.DemoFragment;
import com.octo.android.robodemo.DrawView;
import com.octo.android.robodemo.DrawViewAdapter;
import com.octo.android.robodemo.RoboDemo;
import com.octo.android.robodemo.TouchDispatchDelegate;
import com.octo.android.robodemo.test.UpdateContentDescriptionGapJumper;

import android.view.MotionEvent;

/**
 * Walkthrough - passes touches through to view.
 * May need under view to change on each touch. 
 * Any way to guarantee demo view update?
 * Use force draw update if under view doesn't change or to make demo view update.
 * @author ericharlow
 */
public class TouchHandler extends BaseDemoHandler {

	public TouchHandler(DrawView drawView) {
		super(drawView);
	}

	@Override
	public boolean dispatchTouchEventDelegate(MotionEvent event) {
		DrawView drawView = getDrawView();
    	if (drawView == null) return false;
    	
    	DrawViewAdapter adapter = drawView.getDrawViewAdapter();
    	
		if (drawView.isTouchEventInLabeledPoint(event, adapter.getDrawableAt(adapter.getCurrentPosition()))) {
			// Handle Touch Event
			boolean handled = true;
			TouchDispatchDelegate delegate = drawView.getTouchDispatchDelegate();
    		if (delegate != null) { // should exist only if touchHandler not touchAnimationHandler
    			handled = delegate.sendTouchEvent(event);
    		}
    		
    		// Show Next Point
    		if (event.getAction() == MotionEvent.ACTION_UP && handled) {
                if (adapter.progressToNextPoint()) {
                    if (getForceDrawUpdate()) {
                        onForceDrawUpdate();
                    }
                    if (!RoboDemo.isNeverShowAgain(drawView.getContext().getApplicationContext(),  DemoFragment.DEMO_FRAGMENT_ID)) {
                        UpdateContentDescriptionGapJumper.getInstance().setIdleStateForTest(false);
                    }
                } else {
                    onForceFinishUpdate();
                }
    		}

    		return handled;
    	}
		return false;
	}

	@Override
	public void sendEmptyMessageDelayed() {}

}
