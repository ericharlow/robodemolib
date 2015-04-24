package com.octo.android.robodemo;

import java.lang.reflect.Field;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.text.Layout;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.WindowManager;

import com.octo.android.robodemo.handlers.HandlerType;

// ============================================================================================
// INNER CLASSES
// ============================================================================================
/**
 * Sample DrawViewAdapter.
 * 
 * @author sni
 * @author ericharlow
 */
public class DefaultDrawViewAdapter implements DrawViewAdapter {

    private static final float TEXT_MARGIN = 7;
    private static final float DEFAULT_FONT_SIZE = 22;
    private Drawable drawable;
    private TextPaint textPaint;
    private int maxTextWidth = 80;
    private int screenWidth = 0;
    private int screenHeight = 0;
    private ContentHolder content;
    private int margin;
    private Context context;
    
    public DefaultDrawViewAdapter( Context context, ContentHolder content) {
    	this (context, initializeDefaultDrawable(context), initializeDefaultTextPaint(context), content);
    }
    
    public DefaultDrawViewAdapter( Context context, Drawable drawable, TextPaint textPaint, ContentHolder content) {
      this.context = context;
      this.drawable = drawable;
      this.textPaint = textPaint;
      this.content = content;
    	
      initialize();
    }

    public Context getContext() {
        return context;
    }

    private static TextPaint initializeDefaultTextPaint(Context context) {
        TextPaint textPaint = new TextPaint();
        textPaint.setColor( context.getResources().getColor( android.R.color.white ) );
        textPaint.setShadowLayer( 2.0f, 0, 2.0f, android.R.color.black );
        // http://stackoverflow.com/questions/3061930/how-to-set-unit-for-paint-settextsize
        textPaint.setTextSize( TypedValue.applyDimension( TypedValue.COMPLEX_UNIT_DIP, DEFAULT_FONT_SIZE, context.getResources().getDisplayMetrics() ) );
        return textPaint;
    }
    
    private static Drawable initializeDefaultDrawable(Context context) {
		return context.getResources().getDrawable( R.drawable.ic_lockscreen_handle );
	}

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	@SuppressWarnings("deprecation")
    private void initialize() {
        WindowManager wm = (WindowManager) context.getSystemService( Context.WINDOW_SERVICE );
        Display display = wm.getDefaultDisplay();
        if ( Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB_MR2 ) {
            Point outSize = new Point();
            display.getSize( outSize );
            screenHeight = outSize.y;
            screenWidth = outSize.x;
        } else {
            screenWidth = display.getWidth();
            screenHeight = display.getHeight();
        }
        if ( context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT ) {
            maxTextWidth = screenWidth / 2;
        } else {
            maxTextWidth = screenWidth / 3;
        }
        margin = (int) TypedValue.applyDimension( TypedValue.COMPLEX_UNIT_DIP, TEXT_MARGIN, context.getResources().getDisplayMetrics() );
    }

    @Override
    public int getPointsCount() {
        if (content == null || content.getList() == null)
            return 0;
        return content.getList().size();
    }

    @Override
    public Drawable getDrawableAt( int position ) {   	
        LabeledPoint point = getListPoint(position);
        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        //experimental - works
        Drawable localCopy = drawable;
        Drawable workingCopy = drawable;
        int id = point.getDrawableId();
        if (id != LabeledPoint.INVALID_VALUE) {
        	localCopy = context.getResources().getDrawable(id);
        	workingCopy = localCopy;
        }
        if (point.doUsePreferredSize()) {
        	width = point.getPreferredWidth();
        	height = point.getPreferredHeight();
        	
        	if (workingCopy instanceof GradientDrawable) {
        		localCopy = workingCopy.getConstantState().newDrawable().mutate();
        		((GradientDrawable) localCopy).setSize(width, height);
        	}
        }
        	
        localCopy.setBounds( point.x - width / 2, point.y - height / 2, point.x + width / 2, point.y + height / 2 );
        return localCopy;
    }
    
	private LabeledPoint getListPoint(int position) {
		if (position < 0 || content == null || content.getList() == null || position >= content.getList().size()) {
			return new LabeledPoint.Builder().build();
    	}
		return content.getList().get( position );
	}
	
	public void setContent(ContentHolder content) {
		this.content = content;
	}

    @Override
    public Layout getTextLayoutAt( int position ) {
        String text = getListPoint(position).getText();
        Rect bounds = new Rect();
        textPaint.getTextBounds( text, 0, text.length(), bounds );

        int width = Math.min( bounds.width(), maxTextWidth );
        StaticLayout staticLayout = new StaticLayout( text, textPaint, width, Alignment.ALIGN_CENTER, 1, 0, false );
        return staticLayout;
    }

    @Override
    public Point getTextPointAt( int position ) {
        Drawable drawable = getDrawableAt( position );
        Layout textLayout = getTextLayoutAt( position );
        LabeledPoint point = getListPoint(position);

        if (drawableIsCircle(drawable)) {
        	final int marginX = drawable.getIntrinsicWidth() / 3 + margin;
            final int marginY = drawable.getIntrinsicHeight() / 3 + margin;
            int textX = point.x > screenWidth / 2 ? point.x - marginX - textLayout.getWidth() : point.x + marginX;
            int textY = point.y > screenHeight / 2 ? point.y - marginY - textLayout.getHeight() : point.y + marginY;
            return new Point( textX, textY );
        } else {	// it better be a rectangle
        	// if is top half
        	if (point.y < screenHeight / 2) {
        		if (point.x - (textLayout.getWidth()/2) <= 0)
        			return placeBottomRight(point, textLayout);
        		else if (point.x + (textLayout.getWidth() / 2) >= screenWidth)
        			return placeBottomLeft(point, textLayout);
        		else
        			return placeBottomCenter(point, textLayout);
        	} else {
        		if (point.x - (textLayout.getWidth()/2) <= 0)
        			return placeTopRight(point, textLayout);
        		else if (point.x + (textLayout.getWidth() / 2) >= screenWidth)
        			return placeTopLeft(point, textLayout);
        		else
        			return placeTopCenter(point, textLayout);
        	}	
        }
    }
    
    private Point placeBottomCenter(LabeledPoint point, Layout textLayout) {
    	int temp = point.getPreferredHeight() / 2;
    	if (temp == 0)
    		temp = drawable.getIntrinsicHeight() / 2;
    	return new Point(point.x - (textLayout.getWidth() / 2), point.y + (temp) + margin);
    }
    
    private Point placeBottomRight(LabeledPoint point, Layout textLayout) {
    	int temp = point.getPreferredHeight() / 2;
    	if (temp == 0)
    		temp = drawable.getIntrinsicHeight() / 2;
    	return new Point(point.x, point.y + temp + margin);
    }
    
    private Point placeBottomLeft(LabeledPoint point, Layout textLayout) {
    	int temp = point.getPreferredHeight() / 2;
    	if (temp == 0)
    		temp = drawable.getIntrinsicHeight() / 2;
    	return new Point(point.x - textLayout.getWidth(), point.y + temp + margin);
    }
    
    private Point placeTopCenter(LabeledPoint point, Layout textLayout) {
    	int temp = point.getPreferredHeight() / 2;
    	if (temp == 0)
    		temp = drawable.getIntrinsicHeight() / 2;
    	return new Point(point.x - (textLayout.getWidth() / 2), point.y - (temp) - textLayout.getHeight() - margin);
    }
    
    private Point placeTopRight(LabeledPoint point, Layout textLayout) {
    	int temp = point.getPreferredHeight() / 2;
    	if (temp == 0)
    		temp = drawable.getIntrinsicHeight() / 2;
    	return new Point(point.x , point.y - (temp) - textLayout.getHeight() - margin);
    }
    
    private Point placeTopLeft(LabeledPoint point, Layout textLayout) {
    	int temp = point.getPreferredHeight() / 2;
    	if (temp == 0)
    		temp = drawable.getIntrinsicHeight() / 2;
    	return new Point(point.x - textLayout.getWidth(), point.y - (temp) - textLayout.getHeight() - margin);
    }
    
//    private Point placeLeftCenter(Point point, Layout textLayout) {
//    	return new Point(point.x - (drawable.getIntrinsicWidth() / 2) - textLayout.getWidth() - margin, point.y - (textLayout.getHeight() / 2));
//    }
//    
//    private Point placeRightCenter(Point point, Layout textLayout) {
//    	return new Point(point.x + (drawable.getIntrinsicWidth() / 2) + margin, point.y - (textLayout.getHeight() / 2));
//    }
    
    private boolean drawableIsCircle(Drawable drawable) {
		// using internals and reflection, but what else can be done?
		if (drawable instanceof GradientDrawable) {
			GradientDrawable gd = (GradientDrawable) drawable;
			Drawable.ConstantState state = gd.getConstantState();
			try {
				Field f = state.getClass().getField("mShape");
				int shape = f.getInt(state);
				return shape == GradientDrawable.OVAL;
			} catch (Exception e) {
				Log.e(this.getClass().getSimpleName(), e.getMessage());
			}
		}
		return false;
	}

	@Override
	public String getTextAt(int position) {
		return getListPoint(position).getText();
	}

	@Override
	public HandlerType getHandlerTypeAt(int position) {
		return getListPoint(position).getHandlerType();
	}
	
	@Override
	public int getCurrentPosition() {
		if (content == null)
			return 0;
		return content.getLocation();
	}

	@Override
	public boolean progressToNextPoint() {
		return content.progressToNextPoint();
	}

	@Override
	public void updateCurrentPoint(Activity context) {
		if (content == null)
			return;
		content.updateCurrent(context);
	}
}