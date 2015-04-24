package com.octo.android.robodemo.handlers;

import java.lang.ref.WeakReference;

import com.octo.android.robodemo.DrawView;

import android.os.Handler;

/**
 * Handles the details related to all DemoHandlers.
 * @author ericharlow
 */
public abstract class BaseDemoHandler extends Handler implements DemoHandler {

    private WeakReference< DrawView > weakReference;
    private boolean forceDrawUpdate;

    public BaseDemoHandler( DrawView drawView) {
        this.weakReference = new WeakReference< DrawView >( drawView );
        this.forceDrawUpdate = false;
    }
    
    protected DrawView getDrawView() {
    	if ( weakReference == null || weakReference.get() == null || weakReference.get().getDrawViewAdapter() == null ) {
            return null;
        }
    	return weakReference.get();
    }
    
    public boolean getForceDrawUpdate() {
    	return forceDrawUpdate;
    }
    
    public void setForceDrawUpdate(boolean value) {
    	forceDrawUpdate = value;
    }
    
    public void onForceDrawUpdate() {
    	DrawView drawView = getDrawView();
    	if (drawView == null)
    		return;
    	
    	drawView.refreshDrawableState();
		drawView.invalidate();
    }

    public void onForceFinishUpdate() {
        if(getDrawView() != null)
            getDrawView().clickFinishClickListener();
    }
}
