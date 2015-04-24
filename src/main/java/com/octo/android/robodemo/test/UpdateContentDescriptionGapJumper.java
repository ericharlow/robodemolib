package com.octo.android.robodemo.test;

/**
 * Created by ericharlow on 2/18/2015.
 */
public class UpdateContentDescriptionGapJumper implements GapJumper {
    private static GapJumper instance;
    private IdlingResourceNotifier notifier;

    private UpdateContentDescriptionGapJumper() {}

    public static GapJumper getInstance() {
        if (instance == null)
            instance = new UpdateContentDescriptionGapJumper();
        return instance;
    }

    @Override
    public IdlingResourceNotifier getNotifier() {
        return notifier;
    }

    @Override
    public void setNotifier(IdlingResourceNotifier notifier) {
        this.notifier = notifier;
    }

    @Override
    public void setIdleStateForTest(boolean idle) {
        if(notifier != null)
            notifier.onNotify(idle);
    }

    @Override
    public void cleanup() {
        notifier = null;
        instance = null;
    }
}
