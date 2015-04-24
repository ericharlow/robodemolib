package com.octo.android.robodemo;

import android.app.Activity;

import com.octo.android.robodemo.ContentHolder;

import java.util.ArrayList;

/**
 * Stores the {@link com.octo.android.robodemo.LabeledPoint}s for help demonstrations.
 * see SleekliteIntroPoints for how to implement.
 * Created by ericharlow on 3/12/2015.
 */
public class DemoPointsContentHolder {
    private static ContentHolder content;

    public static ContentHolder getContentHolder() {
        return content;
    }

    public static void setContentHolder(ContentHolder holder) {
        content = holder;
    }

    public static void setPoints(ArrayList<LabeledPoint> points) {
        content = new LabeledPointsContentHolder(points,0);
    }

    private static class LabeledPointsContentHolder implements ContentHolder {

        private int location;
        private ArrayList<LabeledPoint> list;

        public LabeledPointsContentHolder(ArrayList<LabeledPoint> list, int location) {
            this.list = list;
            this.location = location;
        }

        @Override
        public ArrayList<LabeledPoint> getList() {
            return list;
        }

        @Override
        public int getLocation() {
            return location;
        }

        @Override
        public boolean progressToNextPoint() {
            location++;
            if (location >= list.size()) {
                resetLocation();
                return false;
            }
            return true;
        }

        @Override
        public void resetLocation() {
            location = 0;
        }

        @Override
        public void updateCurrent(Activity context) {
            if(validList() && validLocation())
                list.get(location).update(context);
        }

        private boolean validList() {
            if (list == null)
                return false;
            return true;
        }

        private boolean validLocation() {
            if (location < 0 || location >= list.size())
                return false;
            return true;
        }

    }
}
