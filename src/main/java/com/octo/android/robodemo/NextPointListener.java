package com.octo.android.robodemo;

import android.app.Activity;


/**
 * The need for this came about when trying to build a hybrid handler between a TouchHandler and a TouchAnimationHandler.
 * Simply provides a context, hopefully the correct one.
 * @author ericharlow
 */
public interface NextPointListener {
	
	public Activity onNeedContext();

}
