package com.octo.android.robodemo;

import java.util.ArrayList;
import android.app.Activity;

/**
 * list of LabeledPoints to show, and the location to start at.
 * @author ericharlow
 */
public interface ContentHolder {
	public ArrayList<LabeledPoint> getList();
	public int getLocation();
	public boolean progressToNextPoint();
	public void resetLocation();
	public void updateCurrent(Activity context);
}
