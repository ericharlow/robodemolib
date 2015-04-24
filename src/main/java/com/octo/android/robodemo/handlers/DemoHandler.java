package com.octo.android.robodemo.handlers;

import android.os.Message;
import android.view.MotionEvent;

/**
 * Definition of a DemoHandler.
 * @author ericharlow
 */
public interface DemoHandler {
	public boolean dispatchTouchEventDelegate(MotionEvent event);
	public void sendEmptyMessageDelayed();
	public void handleMessage( Message msg );
	public void removeMessages(int animationMessageId);
	public boolean sendEmptyMessageDelayed(int animationMessageId,
			long delayBetweenPoints);
	
	public boolean getForceDrawUpdate();
	public void setForceDrawUpdate(boolean value);
	public void onForceDrawUpdate();
}
