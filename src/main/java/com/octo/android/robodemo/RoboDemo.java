package com.octo.android.robodemo;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class RoboDemo {

    public static final String SHARED_PREFERENCE_NAME = "default";
    public static final String BUNDLE_KEY_DEMO_ACTIVITY_ARRAY_LIST_POINTS = "BUNDLE_KEY_DEMO_ARRAY_LIST_POINTS";
    public static final String BUNDLE_KEY_DEMO_ACTIVITY_ID = "BUNDLE_KEY_DEMO_ACTIVITY_ID";

    /**
     * Prepares an intent for a DemoActivity.
     * 
     * @param intent
     *            the intent to be used to launch the sublcass of {@link DemoActivity}.
     * @param demoActivityId
     *            the id that will be used to store the information about the 'never show again' checkbox.
     * @param listPoints
     *            an {@link ArrayList} of {@link LabeledPoint} to Display.
     */
    public static void prepareDemoActivityIntent( Intent intent, String demoActivityId, ArrayList< LabeledPoint > listPoints ) {
        intent.putExtra( BUNDLE_KEY_DEMO_ACTIVITY_ID, demoActivityId );
        intent.putParcelableArrayListExtra( BUNDLE_KEY_DEMO_ACTIVITY_ARRAY_LIST_POINTS, listPoints );
    }

    /**
     * Allows to check if a demo activity has been set never to display again.
     * 
     * @param caller
     *            the context that is calling the {@link SharedPreferences}.
     * @param demoActivityId
     *            the id that will be used to store the information about the 'never show again' checkbox.
     */
    public static boolean isNeverShowAgain( Context caller, String demoActivityId ) {
    	return PreferenceManager.getDefaultSharedPreferences(caller).getBoolean( demoActivityId, false );
    }

    /**
     * Reset a demo activity to show again.
     * 
     * @param caller
     *            the context that is calling the {@link SharedPreferences}.
     * @param demoActivityId
     *            the id that will be used to store the information about the 'never show again' checkbox.
     */
    public static boolean showAgain( Context caller, String demoActivityId ) {
    	return PreferenceManager.getDefaultSharedPreferences(caller).edit().remove(demoActivityId).commit();
    }
    
    /**
     * Should the Demo be shown.
     * @param caller - the context that is calling the {@link SharedPreferences}.
     * @param demoActivityId - the id that will be used to store the information about showing the demo.
     * @return true if the demo should be shown.
     */
    public static boolean shouldShowDemo(Context caller, String demoActivityId) {
    	SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(caller);
    	return settings.getBoolean( demoActivityId, false );
    }
    
    /**
     * Set whether the demo should be shown.
     * @param caller - the context that is calling the {@link SharedPreferences}.
     * @param demoActivityId  - the id that will be used to store the information about showing the demo.
     * @param value - the value for whether the demo should be shown.
     */
    public static void setShowDemo(Context caller, String demoActivityId, boolean value) {
    	SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(caller);
    	settings.edit().putBoolean(demoActivityId, value).commit();
    }
    
    /**
     * 
     * @param caller - the context that is calling the {@link SharedPreferences}.
     * @param demoActivityId - the id that will be used to store the information about the 'never show again' checkbox.
     * @param value
     */
    public static void setNeverShowAgain(Context caller, String demoActivityId, boolean value) {
    	SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(caller).edit();
    	editor.putBoolean( demoActivityId, value );
    	editor.commit();
    }
}
