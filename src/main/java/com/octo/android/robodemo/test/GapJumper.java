package com.octo.android.robodemo.test;

/**
 * Created by ericharlow on 2/18/2015.
 */
public interface GapJumper {

    public interface IdlingResourceNotifier {
        void onNotify(boolean idle);
    }

    public IdlingResourceNotifier getNotifier();
    public void setNotifier(IdlingResourceNotifier notifier);
    public void setIdleStateForTest(boolean idle);
    public void cleanup();
}
