package com.octo.android.robodemo.handlers;

import android.os.Message;
import android.view.MotionEvent;

import com.octo.android.robodemo.DrawView;

/**
 * Animate the point to draw on screen.
 * Showcase - shows based on a timer.
 * @author sni
 * @author ericharlow
 */
public class AnimatorHandler extends BaseDemoHandler {
    public static final int ANIMATION_MESSAGE_ID = 0;
    private long delayBetweenPoints;
//    private long startTime;

    /**
     *
     * @param drawView
     * @param delayBetweenPoints
     */
    public AnimatorHandler( DrawView drawView, long delayBetweenPoints ) {
    	super(drawView);
        this.delayBetweenPoints = delayBetweenPoints;
    }
    
    @Override
    public boolean dispatchTouchEventDelegate(MotionEvent event) {
    	return true;
    }

	public void sendEmptyMessageDelayed() {
    	super.sendEmptyMessageDelayed( AnimatorHandler.ANIMATION_MESSAGE_ID, delayBetweenPoints );
    }

    @Override
    public void handleMessage( Message msg ) {
        super.handleMessage( msg );
        if ( msg.what == ANIMATION_MESSAGE_ID ) {
            DrawView drawView = getDrawView();
            if (drawView == null) return;
            
            if (drawView.getDrawViewAdapter().progressToNextPoint())
                onForceDrawUpdate();
            else
                onForceFinishUpdate();
        }
    }
}
