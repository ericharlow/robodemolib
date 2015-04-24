package com.octo.android.robodemo;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.Display;
import android.view.View;
import android.view.View.OnLayoutChangeListener;

import com.octo.android.robodemo.handlers.HandlerType;

/**
 * A pojo class that wraps all information needed to display a point on screen. {@link LabeledPoint} embed a position
 * and a text to draw. The {@link Drawable} that illustrates this point will be provided by
 * {@link DrawView#getDrawViewAdapter()}. This class implements {@link Parcelable} so that it can be passed between the
 * Activity to illustrate and the {@link DemoActivity}.
 * 
 * @author sni
 * @author ericharlow
 */
public class LabeledPoint extends Point implements Parcelable {
	
	public static final int INVALID_VALUE = -1;

    /** The text associated to this point. */
    private String text;
    private int preferredWidth;
    private int preferredHeight;
//    private int handlerType;
    private HandlerType handlerType;
    private int referenceId;
    private int stringId;
    private int drawableId;
    private int contentDescriptionId;
    private float widthPercent;
    private float heightPercent;
    
    //experimental
    private boolean usePreferredSize;

	private void updatePositionUsingView(View v, final float widthPercent,
			final float heightPercent, boolean preferredSize) {
		if (v != null) {
        	usePreferredSize = preferredSize;
        	setMeasuredLocation(widthPercent, heightPercent, v);
        	if (usePreferredSize)
				setPreferredSize(v.getMeasuredWidth(), v.getMeasuredHeight());
        	v.addOnLayoutChangeListener(getOnLayoutChangeListener());
        }
	}
	
	private OnLayoutChangeListener getOnLayoutChangeListener() {
		return new OnLayoutChangeListener() {
			
			@Override
			public void onLayoutChange(View v, int left, int top, int right,
					int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
				setMeasuredLocation(widthPercent, heightPercent, v);
				if (usePreferredSize)
					setPreferredSize(v.getMeasuredWidth(), v.getMeasuredHeight());
	            v.removeOnLayoutChangeListener(this);
			}
		};
	}
    
    protected void setPreferredSize(int measuredWidth, int measuredHeight) {
    	// use min because of DrawView.doUseClearPorterDuffXfermode uses canvas.drawCircle
//    	int min = Math.min(measuredWidth, measuredHeight);
    	preferredWidth = measuredWidth;
    	preferredHeight = measuredHeight;
	}
    
    private static int getPreferredWidth(View v) {
    	int measuredWidth = v.getMeasuredWidth();
    	int measuredHeight = v.getMeasuredHeight();
    	return Math.min(measuredWidth, measuredHeight);
    }
    
    private static int getPreferredHeight(View v) {
    	int measuredWidth = v.getMeasuredWidth();
    	int measuredHeight = v.getMeasuredHeight();
    	return Math.min(measuredWidth, measuredHeight);
    }
    
    private static int getX(Activity activity, float widthPercent) {
    	Display display = activity.getWindowManager().getDefaultDisplay();
    	int screenWidth = getScreenWidth(display);
    	return (int) ( screenWidth * widthPercent );
    }
    
    private static int getY(Activity activity, float heightPercent) {
    	Display display = activity.getWindowManager().getDefaultDisplay();
    	int screenHeight = getScreenHeight(display);
    	return (int) ( screenHeight * heightPercent );
    }
    
    private LabeledPoint(Builder builder) {
    	this.text = builder.text;
    	this.x = builder.x;
    	this.y = builder.y;
    	this.usePreferredSize = builder.usePreferredSize;
    	this.handlerType = builder.handlerType;
    	this.referenceId = builder.referenceId;
    	this.stringId = builder.stringId;
    	this.drawableId = builder.drawableId;
    	this.contentDescriptionId = builder.contentDescriptionId;
	    this.preferredWidth = builder.preferredWidth;
	    this.preferredHeight = builder.preferredHeight;
    	this.heightPercent = builder.heightPercent;
    	this.widthPercent = builder.widthPercent;
    	if (builder.v != null)
    		builder.v.addOnLayoutChangeListener(getOnLayoutChangeListener()); 
    }

    public String getText() {
    	if (text == null)
    		return "";
        return text;
    }

    public void setText( String text ) {
        this.text = text;
    }
    
    /**
     * Use the context to update the position.
     * @param context - an Activity.
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	public void update(Context context) {
    	if (context instanceof Activity) {
    		Activity act = (Activity) context;
    		if (referenceId != INVALID_VALUE)
    			updatePositionUsingView(act.findViewById(referenceId), widthPercent, heightPercent, usePreferredSize);
    		else if (contentDescriptionId != INVALID_VALUE) {
    			// Same code as in Builder
    			ArrayList<View> foundViews = new ArrayList<View>(1);
    			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
    				act.getWindow().getDecorView().findViewsWithText(foundViews, act.getString(contentDescriptionId), View.FIND_VIEWS_WITH_CONTENT_DESCRIPTION);
    			}
    			View foundView = foundViews.isEmpty() ? null : foundViews.get(0);
    			updatePositionUsingView(foundView, widthPercent, heightPercent, usePreferredSize);
    		} else if (stringId != INVALID_VALUE) {
    			// Same code as in Builder
    			ArrayList<View> foundViews = new ArrayList<View>(1);
    			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
    				act.getWindow().getDecorView().findViewsWithText(foundViews, act.getString(stringId), View.FIND_VIEWS_WITH_TEXT);
    			}
    			View foundView = foundViews.isEmpty() ? null : foundViews.get(0);
    			updatePositionUsingView(foundView, widthPercent, heightPercent, usePreferredSize);
    		}
    	}
    }

    /**
     * Write this point to the specified parcel. To restore a point from a parcel, use readFromParcel()
     * 
     * @param out
     *            The parcel to write the point's coordinates into
     */
    @Override
    public void writeToParcel( Parcel out, int flags ) {
        out.writeInt( x );
        out.writeInt( y );
        out.writeInt(preferredHeight);
        out.writeInt(preferredWidth);
        out.writeValue(handlerType);
        out.writeInt(referenceId);
        out.writeInt(drawableId);
        out.writeInt(contentDescriptionId);
        out.writeFloat(heightPercent);
        out.writeFloat(widthPercent);
        out.writeByte((byte) (usePreferredSize ? 1 : 0));
        out.writeString( text );
    }

    /**
     * Parcel creator for points.
     */
    public static final Parcelable.Creator< LabeledPoint > CREATOR = new Parcelable.Creator< LabeledPoint >() {
        /**
         * Return a new point from the data in the specified parcel.
         */
        @SuppressLint("NewApi")
		@Override
        public LabeledPoint createFromParcel( Parcel in ) {
            LabeledPoint r = new LabeledPoint.Builder().build();
            r.readFromParcel( in );
            return r;
        }

        /**
         * Return an array of rectangles of the specified size.
         */
        @Override
        public LabeledPoint[] newArray( int size ) {
            return new LabeledPoint[ size ];
        }
    };

    /**
     * Set the point's coordinates from the data stored in the specified parcel. To write a point to a parcel, call
     * writeToParcel().
     * 
     * @param in
     *            The parcel to read the point's coordinates from
     */
    public void readFromParcel( Parcel in ) {
        x = in.readInt();
        y = in.readInt();
        preferredHeight = in.readInt();
        preferredWidth = in.readInt();
        handlerType = (HandlerType) in.readValue(HandlerType.class.getClassLoader());
        referenceId = in.readInt();
        drawableId = in.readInt();
        contentDescriptionId = in.readInt();
        heightPercent = in.readFloat();
        widthPercent = in.readFloat();
        usePreferredSize = in.readByte() != 0;
        text = in.readString();
    }

	private void setMeasuredLocation(final float widthPercent,
			final float heightPercent, View v) {
		int[] location = new int[ 2 ];
		v.getLocationOnScreen( location );
		x = location[ 0 ] + Math.round( widthPercent * v.getMeasuredWidth() / 100 );
		y = location[ 1 ] + Math.round( heightPercent * v.getMeasuredHeight() / 100 );
	}
	
	private static int getX(View v, float widthPercent) {
		int[] location = new int[ 2 ];
		v.getLocationOnScreen( location );
		return location[ 0 ] + Math.round( widthPercent * v.getMeasuredWidth() / 100 );
	}
	
	private static int getY(View v, float heightPercent) {
		int[] location = new int[ 2 ];
		v.getLocationOnScreen( location );
		return location[ 1 ] + Math.round( heightPercent * v.getMeasuredHeight() / 100 );
	}
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	@SuppressWarnings("deprecation")
	private static int getScreenWidth(Display display) {
		int screenWidth;
		if ( Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB_MR2 ) {
            Point outSize = new Point();
            display.getSize( outSize );
            screenWidth = outSize.x;
        } else {
            screenWidth = display.getWidth();
        }
		return screenWidth;
	}
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	@SuppressWarnings("deprecation")
	private static int getScreenHeight(Display display) {
		int screenHeight;
		if ( Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB_MR2 ) {
            Point outSize = new Point();
            display.getSize( outSize );
            screenHeight = outSize.y;
        } else {
            screenHeight = display.getHeight();
        }
		return screenHeight;
	}

	//experimental
	public int getPreferredWidth() {
		return preferredWidth;
	}

	//experimental
	public void setPreferredWidth(int preferedWidth) {
		this.preferredWidth = preferedWidth;
	}

	public int getPreferredHeight() {
		return preferredHeight;
	}

	public void setPreferredHeight(int preferedHeight) {
		this.preferredHeight = preferedHeight;
	}

	public boolean doUsePreferredSize() {
		return usePreferredSize;
	}

	public void setUsePreferredSize(boolean usePreferredSize) {
		this.usePreferredSize = usePreferredSize;
	}

	public HandlerType getHandlerType() {
		return handlerType;
	}

	public void setHandlerType(HandlerType handlerType) {
		this.handlerType = handlerType;
	}
	
	public int getDrawableId() {
		return drawableId;
	}

    /**
     * Common build configurations.<br/>
     * <br/>
     * <b>Location by View id:</b><br/>
     * new LabeledPoint.Builder().location(context,50, 50,R.id.ViewId)
     * .usePreferredSize(R.id.ViewId, true)
     * .text(context,R.string.StringId).build();
     * <br/><br/>
     * <b>Location by Screen coordinates:</b><br/>
     * new LabeledPoint.Builder().location(context, 0.38f, 0.06f)
     * .usePreferredSize(-1, true).preferredSize(180,140)
     * .text(context,R.string.StringId).build();
     */
	public static class Builder {
		
		private String text = "";
		private int x = -100;
		private int y = -100;
		private boolean usePreferredSize = false;
	    private int preferredWidth = 0;
	    private int preferredHeight = 0;
	    private HandlerType handlerType = HandlerType.TOUCHHANDLER;
	    private int referenceId = INVALID_VALUE;
	    private int stringId = INVALID_VALUE;
	    private int drawableId = INVALID_VALUE;
	    private int contentDescriptionId = INVALID_VALUE;
	    private float widthPercent = 50;
	    private float heightPercent = 50;
	    private View v = null;

        /**
         * Use the configuration to build an object.
         * @return The configured LabeledPoint.
         */
		public LabeledPoint build() {
			return new LabeledPoint(this);
		}
		
		public Builder text(String input) {text = input; return this;}
		public Builder text(Activity context, int stringId) {stringId(stringId); return this.text(context.getString(stringId));}
		public Builder x(int x) {this.x = x; return this;}
		public Builder y(int y) {this.y = y; return this;}
		public Builder usePreferredSize(int refId, boolean value) {referenceId(refId);usePreferredSize = value; return this;}
		public Builder usePreferredSizeWithContentDescription(int cdId, boolean value) {contentDescriptionId(cdId);usePreferredSize = value; return this;}
		public Builder usePreferredSizeWithText(int stringId, boolean value) {stringId(stringId);usePreferredSize = value; return this;}
		public Builder preferredWidth(int value) {preferredWidth = value; return this;}
		public Builder preferredHeight(int value) {preferredHeight = value; return this;}
		public Builder handlerType(HandlerType value) {handlerType = value; return this;}
		public Builder referenceId(int value) {referenceId = value;  return this;}
		public Builder drawable(int value) {drawableId = value; return this;}
		public Builder stringId(int value) {stringId = value; return this;}
		public Builder contentDescriptionId(int value) {contentDescriptionId = value; return this;}
		public Builder widthPercent(float value) {widthPercent = value; return this;}
		public Builder heightPercent(float value) {heightPercent = value; return this;}
		public Builder view(View v) {this.v = v; return this;}
		public Builder location(Activity context, float widthPercent, float heightPercent) {
			this.widthPercent(widthPercent);
			this.heightPercent(heightPercent);
			int value = LabeledPoint.getX(context, widthPercent);
			this.x(value);
			
			value = LabeledPoint.getY(context, heightPercent);
			this.y(value);
			
			return this;
		}
		
		public Builder location(View v, final float widthPercent, final float heightPercent) {
			this.widthPercent(widthPercent);
			this.heightPercent(heightPercent);
			
			if (v == null)
				return this;
			
			int value = LabeledPoint.getX(v, widthPercent);
			this.x(value);
			
			value = LabeledPoint.getY(v, heightPercent);
			this.y(value);
			
			this.preferredHeight(LabeledPoint.getPreferredHeight(v));
			this.preferredWidth(LabeledPoint.getPreferredWidth(v));
			
			return this.view(v);
		}
		
		public Builder location(Activity context,float widthPercent, float heightPercent, int referenceId) {
			this.referenceId(referenceId);
			return this.location(context.findViewById(referenceId), widthPercent, heightPercent);
		}
		
		@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
		public Builder location(Activity context, int contentDescriptionId) {
			this.contentDescriptionId(contentDescriptionId);
			ArrayList<View> foundViews = new ArrayList<View>(1);
			String contentDescription = context.getString(contentDescriptionId);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
				context.getWindow().getDecorView().findViewsWithText(foundViews, contentDescription, View.FIND_VIEWS_WITH_CONTENT_DESCRIPTION);
			}
			View foundView = foundViews.isEmpty() ? null : foundViews.get(0);
			return this.location(foundView, 50, 50);
		}
		
		public Builder preferredSize(int width, int height) {
			this.preferredWidth(width);
			return this.preferredHeight(height);
		}
	}
}