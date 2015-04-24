package com.octo.android.robodemo;

import java.lang.reflect.Field;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.text.Layout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation.AnimationListener;

import com.octo.android.robodemo.handlers.AnimatorHandler;
import com.octo.android.robodemo.handlers.DemoHandler;
import com.octo.android.robodemo.handlers.HandlerType;
import com.octo.android.robodemo.handlers.TouchAnimationHandler;
import com.octo.android.robodemo.handlers.TouchHandler;
import com.octo.android.robodemo.handlers.TouchHybridHandler;
import com.octo.android.robodemo.test.UpdateContentDescriptionGapJumper;

/**
 * This view will draw all {@link LabeledPoint} on its surface. It uses a {@link DrawViewAdapter } to get the content to
 * draw.
 * 
 * @author sni
 * @author ericharlow
 */
public class DrawView extends View {

    private static final int DRAW_UNDER_TEXT_CORNER_RADIUS = 7;
    private static final int DEFAULT_FONT_SIZE = 22;

    /**
     * The default delay between points in animation in ms.
     */
    private static final long DELAY_BETWEEN_POINTS = 1000;

    private DrawViewAdapter drawViewAdapter;
    private HandlerType handlerType;
    private DemoHandler handler;
    private long delayBetweenPoints;
    private boolean isShowingAllPointsAtTheEndOfAnimation;
    private boolean isDrawingOnePointAtATime;
    private boolean isAutomatedTestMode;

    private AnimationListener animationListener;
    private NextPointListener nextPointListener;
    private OnClickListener finishClickListener;

    private Paint underTextPaint;

    private boolean isClearPorterDuffXfermodeEnabled = true;
    
    private TouchDispatchDelegate mTouchDispatchDelegate;

    public DrawView( Context context, AttributeSet attrs, int defStyle ) {
        super( context, attrs, defStyle );
        
        if (attrs!=null) {
            TypedArray a = context.obtainStyledAttributes(attrs,
                                                      R.styleable.DrawView,
                                                      defStyle, 0);
            TextPaint textPaint = new TextPaint();
            textPaint.setColor(a.getColor(R.styleable.DrawView_textColor, Color.WHITE));
            textPaint.setAntiAlias(a.getBoolean(R.styleable.DrawView_textAntiAlias, true));
            textPaint.setTextSize(a.getDimension(R.styleable.DrawView_textSize, DEFAULT_FONT_SIZE));
            textPaint.setShadowLayer(a.getFloat(R.styleable.DrawView_shadowLayerBlurRadius, 2.0f),
            		a.getFloat(R.styleable.DrawView_shadowLayerXOffset, 0),
            		a.getFloat(R.styleable.DrawView_shadowLayerYOffset, 2.0f),
            		a.getColor(R.styleable.DrawView_shadowLayerColor, Color.BLACK));
            
            Drawable drawable = a.getDrawable(R.styleable.DrawView_drawable);
            drawViewAdapter = new DefaultDrawViewAdapter(context, drawable, textPaint, null);
            
            underTextPaint = new Paint();
            underTextPaint.setColor( a.getColor(R.styleable.DrawView_underTextPaintColor, Color.DKGRAY));
            underTextPaint.setAlpha( a.getInt(R.styleable.DrawView_underTextPaintAlpha, 150) );
            
            isShowingAllPointsAtTheEndOfAnimation = a.getBoolean(R.styleable.DrawView_isShowingAllPointsAtTheEndOfAnimation, true);
            isDrawingOnePointAtATime = a.getBoolean(R.styleable.DrawView_isDrawingOnePointAtATime, false);
            delayBetweenPoints = a.getInt(R.styleable.DrawView_delayBetweenPoints, (int) DELAY_BETWEEN_POINTS);
//            handlerType = a.getInt(R.styleable.DrawView_handlerType, -1);
            handlerType = HandlerType.TOUCHHANDLER;
            initializeHandler(handlerType);
            
            a.recycle();
          }
    }

	public DrawView( Context context, AttributeSet attrs ) {
		this(context, attrs, 0);
    }

    public DrawView( Context context ) {
    	this(context, null);
    }

    @Override
    public void onDraw( Canvas canvas ) {
        super.onDraw( canvas );

        if ( isAnimationTerminated() ) {
        	updateHandler(drawViewAdapter.getCurrentPosition());
            if ( isShowingAllPointsAtTheEndOfAnimation ) {
                for ( int index = 0; index < getAdapterPointCount(); index++ ) {
                    drawPoint( index, canvas );
                }
            } else {
                drawPoint( drawViewAdapter.getCurrentPosition(), canvas );
            }
        } else {
            if ( drawViewAdapter.getCurrentPosition() >= 0 ) {
            	updateHandler(drawViewAdapter.getCurrentPosition());
                if ( isDrawingOnePointAtATime ) {
                    drawPoint( drawViewAdapter.getCurrentPosition(), canvas );
                } else {
                    for ( int index = 0; index <= drawViewAdapter.getCurrentPosition(); index++ ) {
                        drawPoint( index, canvas );
                    }
                }
            }
        }
    }

    public void setIsClearPorterDuffXfermodeEnabled( boolean isClearPorterDuffXfermodeEnabled ) {
        this.isClearPorterDuffXfermodeEnabled = isClearPorterDuffXfermodeEnabled;
    }

    public void setUnderTextPaint( Paint underTextPaint ) {
        this.underTextPaint = underTextPaint;
    }

    public boolean isAnimationTerminated() {
        return drawViewAdapter.getCurrentPosition() >= getAdapterPointCount() - 1;
    }

    /**
     * Restarts the animation from the beginning.
     */
    public void resetAnimation() {
        handler.removeMessages( AnimatorHandler.ANIMATION_MESSAGE_ID );
        if ( animationListener != null ) {
            animationListener.onAnimationStart( null );
        }
        handler.sendEmptyMessageDelayed( AnimatorHandler.ANIMATION_MESSAGE_ID, delayBetweenPoints );
    }

    /**
     * Restarts the animation from the beginning.
     */
    public void terminateAnimation() {
        handler.removeMessages( AnimatorHandler.ANIMATION_MESSAGE_ID );
        if ( animationListener != null ) {
            animationListener.onAnimationEnd( null );
        }
        refreshDrawableState();
        invalidate();
    }
    
    private int getAdapterPointCount() {
    	return drawViewAdapter == null ? 0 : drawViewAdapter.getPointsCount();
    }

    public void setDrawViewAdapter( DrawViewAdapter drawViewAdapter ) {
        this.drawViewAdapter = drawViewAdapter;
    }

    public DrawViewAdapter getDrawViewAdapter() {
        return drawViewAdapter;
    }
    
    /**
     * Supply the data to the Adapter.
     * @see DefaultDrawViewAdapter
     * @see ContentHolder
     * @param content
     */
    public void setDrawViewAdapterContent(ContentHolder content) {
    	((DefaultDrawViewAdapter) drawViewAdapter).setContent(content);
    	if (drawViewAdapter.getCurrentPosition() > getAdapterPointCount() - 1) // almost the same as isAnimationTerminated()
    		terminateAnimation();
    }

    /**
     * Sets the delay between animation of two points.
     * 
     * @param delayBetweenPoints
     *            the new delay of animation (in ms).
     */
    public void setDelayBetweenPoints( long delayBetweenPoints ) {
        this.delayBetweenPoints = delayBetweenPoints;
        handlerType = HandlerType.ANIMATIONHANDLER;
        initializeHandler(handlerType);
    }

    public long getDelayBetweenPoints() {
        return delayBetweenPoints;
    }

    public boolean isShowingAllPointsAtTheEndOfAnimation() {
        return isShowingAllPointsAtTheEndOfAnimation;
    }

    /**
     * Whether or not all points are displayed at the end of all points animation.
     * 
     * @param isShowingAllPointsAtTheEndOfAnimation
     *            if true, all points will be visible at the end of animation, nothing will be displayed otherwise.
     * 
     */
    public void setShowingAllPointsAtTheEndOfAnimation( boolean isShowingAllPointsAtTheEndOfAnimation ) {
        this.isShowingAllPointsAtTheEndOfAnimation = isShowingAllPointsAtTheEndOfAnimation;
    }

    /**
     * Whether to show one point at a time or a point and all previous points.
     * 
     * @param isDrawingOnePointAtATime
     *            if true, only one point will be displayed at a time. If false, a point and all its predecessors will
     *            be displayed simultaneously.
     */
    public void setDrawingOnePointAtATime( boolean isDrawingOnePointAtATime ) {
        this.isDrawingOnePointAtATime = isDrawingOnePointAtATime;
    }

    public boolean isDrawingOnePointAtATime() {
        return isDrawingOnePointAtATime;
    }

    /**
     * Sets an {@link AnimationListener} that will be notified at the beginning and end of the animation of all points.
     * 
     * @param animationListener
     */
    public void setAnimationListener( AnimationListener animationListener ) {
        this.animationListener = animationListener;
    }

    public AnimationListener getAnimationListener() {
        return animationListener;
    }

    public NextPointListener getNextPointListener() {
		return nextPointListener;
	}

	public void setNextPointListener(NextPointListener nextPointListener) {
		this.nextPointListener = nextPointListener;
	}

    public void setFinishClickListener(OnClickListener listener) {
        this.finishClickListener = listener;
    }

    public void clickFinishClickListener() {
        if (finishClickListener != null)
            finishClickListener.onClick(null);
    }

	public boolean isAutomatedTestMode() {
		return isAutomatedTestMode;
	}

	public void setAutomatedTestMode(boolean isAutomatedTestMode) {
		this.isAutomatedTestMode = isAutomatedTestMode;
	}
	
	/**
	 * @see res/values/attrs.xml for the handlerType.
	 * @param position - which type of handler to create.
	 */
	public void updateHandler(int position) {
		handlerType = drawViewAdapter.getHandlerTypeAt(position);
		initializeHandler(handlerType);
	}

    private void initializeHandler(HandlerType type) {
        // Remove any ongoing messages
        final boolean handlerIsNull = handler == null;
        final boolean typeOutOfRange = isInvalidHandlerType(type);
        if (!handlerIsNull && !typeOutOfRange)
            handler.removeMessages(AnimatorHandler.ANIMATION_MESSAGE_ID);

        switch (type){
                case ANIMATIONHANDLER:
                    handler = new AnimatorHandler(this, delayBetweenPoints);
                    break;
                case TOUCHHANDLER:
                    handler = new TouchHandler(this);
                    break;
                case TOUCHANIMATIONHANDLER:
                    handler = new TouchAnimationHandler(this);
                    break;
                case TOUCHHYBRIDHANDLER:
                    handler = new TouchHybridHandler(this);
                    break;
                case DRAWUPDATETOUCHHANDLER:
                    handler = new TouchHandler(this);
                    handler.setForceDrawUpdate(true);
                    break;
                case DRAWUPDATETOUCHHYBRIDHANDLER:
                    handler = new TouchHybridHandler(this);
                    handler.setForceDrawUpdate(true);
                    break;
                default:
                    handler = new TouchAnimationHandler(this);
                    break;
        }
    }

    private boolean isInvalidHandlerType(HandlerType type) {
        switch(type) {
            case ANIMATIONHANDLER:
            case TOUCHHANDLER:
            case TOUCHANIMATIONHANDLER:
            case TOUCHHYBRIDHANDLER:
            case DRAWUPDATETOUCHHANDLER:
            case DRAWUPDATETOUCHHYBRIDHANDLER:
                return false;
            default:
                return true;
        }
    }

    /**
     * Draw the point at the position specified by {@link DrawViewAdapter#getTextPointAt(int)}
     * 
     * @param position
     *            the index of the point to draw.
     * @param canvas
     *            the canvas on which to draw the point at position.
     */
    protected void drawPoint( int position, Canvas canvas ) {
    	updatePoint(position);
        drawText( position, canvas );
        drawDrawable( position, canvas );
        updateContentDescription(position);
    }

	/**
     * Draw the text of the point at a given position specified by {@link DrawViewAdapter#getTextPointAt(int)}
     * 
     * @param position
     *            the index of the point to draw.
     * @param canvas
     *            the canvas on which to draw the point at position.
     */
    protected void drawText( int position, Canvas canvas ) {
        Point point = drawViewAdapter.getTextPointAt( position );
        canvas.save();
        canvas.translate( point.x, point.y );
        Layout layout = drawViewAdapter.getTextLayoutAt( position );
        doDrawUnderTextPaint( canvas, layout );
        layout.draw( canvas );
        canvas.restore();
    }

    /**
     * Draw some surface under text. This method is called just before drawing each {@link LabeledPoint}'s text.
     * 
     * @param canvas
     *            the canvas in which we draw.
     * @param layout
     *            the {@link Layout} associated to a {@link LabeledPoint}.
     */
    protected void doDrawUnderTextPaint( Canvas canvas, Layout layout ) {
        if ( underTextPaint != null ) {
            int margin = DRAW_UNDER_TEXT_CORNER_RADIUS;
            RectF rect = new RectF( -margin, -margin, layout.getWidth() + margin * 2, layout.getHeight() + margin * 2 );
            canvas.drawRoundRect( rect, 2 * margin, 2 * margin, underTextPaint );
        }
    }

    /**
     * Draw the drawable of the point at a given position specified by {@link DrawViewAdapter#getDrawableAt(int)}
     * 
     * @param position
     *            the index of the point to draw.
     * @param canvas
     *            the canvas on which to draw the point at position.
     */
    protected void drawDrawable( int position, Canvas canvas ) {
        Drawable drawable = drawViewAdapter.getDrawableAt( position );
        if ( drawable == null ) {
            return;
        }
        doUseClearPorterDuffXfermode( canvas, drawable );
        drawable.draw( canvas );
        /*
         * Shall we add some debug mode ? Paint paint = new Paint(); paint.setColor(
         * getContext().getResources().getColor( android.R.color.holo_orange_dark ) ); canvas.drawCircle(
         * drawable.getBounds().exactCenterX(), drawable.getBounds().exactCenterY(), 5, paint );
         */
    }

    /**
     * if PorterDuff xfermode is active, this method can be used to remove the background inside the {@link Drawable}
     * associated to a {@link LabeledPoint}. This method is called just before drawing each {@link LabeledPoint}'s
     * drawable.
     * 
     * @param canvas
     *            the canvas on which to draw the point at position.
     * @param drawable
     *            the {@link Drawable} that is going to be drawn.
     * @see #isClearPorterDuffXfermodeEnabled
     * @see #setIsClearPorterDuffXfermodeEnabled(boolean)
     */
    protected void doUseClearPorterDuffXfermode( Canvas canvas, Drawable drawable ) {
        if ( isClearPorterDuffXfermodeEnabled ) {
            Paint p = new Paint();
            p.setXfermode( new PorterDuffXfermode( Mode.CLEAR ) );
            if (drawableIsCircle(drawable))
            	drawForCircle(canvas, drawable, p);
            else
            	drawForRectangle(canvas, drawable, p);
        }
    }

    private boolean drawableIsCircle(Drawable drawable) {
    	// using internals and reflection, but what else can be done?
    	Drawable.ConstantState state = drawable.getConstantState();
    	try {
    		Field f = state.getClass().getField("mShape");
    		int shape = f.getInt(state);
    		return shape == GradientDrawable.OVAL;
    	} catch (Exception e) {
    		Log.e(this.getClass().getSimpleName(), e.getMessage());
    	}
    	return false;
    }

	private void drawForRectangle(Canvas canvas, Drawable drawable, Paint p) {
		canvas.drawRect(drawable.copyBounds(), p);
	}

	private void drawForCircle(Canvas canvas, Drawable drawable, Paint p) {
		int cx = drawable.getBounds().centerX();
		int cy = drawable.getBounds().centerY();
		int radius = drawable.getIntrinsicHeight() / 2 - 3;
		canvas.drawCircle( cx, cy, radius, p );
	}

    @Override
	public boolean dispatchTouchEvent(MotionEvent event) {
    	if (isAutomatedTestMode)
    		event = putTouchEventInLabledPoint(event, drawViewAdapter.getDrawableAt( drawViewAdapter.getCurrentPosition()));
    	
    	final boolean handled = handler.dispatchTouchEventDelegate(event);
    	if (handled)
    		return handled;
    	else {
            return super.dispatchTouchEvent(event);
        }
	}
    
    /**
     * Allow UiAutomator to know the text of the current point through content description.
     * @param - position of the {@link LabeledPoint}.
     */
    private void updateContentDescription(int position) {
    	String text = drawViewAdapter.getTextAt(position);
    	setContentDescription(text);
        UpdateContentDescriptionGapJumper.getInstance().setIdleStateForTest(true);
    }
    
    private void updatePoint(int position) {
    	NextPointListener listener = getNextPointListener();
		if (listener == null)
			return;
		drawViewAdapter.updateCurrentPoint(listener.onNeedContext());
	}

	boolean isTouchHandler() {
        switch (handlerType) {
            case TOUCHHANDLER:
            case TOUCHANIMATIONHANDLER:
            case TOUCHHYBRIDHANDLER:
                return true;
            default:
                return false;
        }
	}

	public boolean isTouchEventInLabeledPoint(MotionEvent event, Drawable drawable) {
		if (drawableIsCircle(drawable)) {
			float x = event.getX();
			float y = event.getY();
			int center_x = drawable.getBounds().centerX();
			int center_y = drawable.getBounds().centerY();
			int radius = drawable.getIntrinsicWidth() / 2 - 3;
			
			//change < to <= to include points on circle
			boolean result = Math.pow((x - center_x), 2) + Math.pow((y - center_y), 2) < Math.pow(radius, 2);
			return result;
		}

		return drawable.getBounds().contains((int) event.getX(), (int) event.getY());
	}
	
	private MotionEvent putTouchEventInLabledPoint(MotionEvent event, Drawable drawable) {
		event.setLocation(drawable.getBounds().centerX(), drawable.getBounds().centerY());
		return event;
	}

	public TouchDispatchDelegate getTouchDispatchDelegate() {
		return mTouchDispatchDelegate;
	}

	public void setTouchDispatchDelegate(TouchDispatchDelegate touchDispatchDelegate) {
//		if (!isTouchDrivenAnimationHandler()) // consider using logic like if is a touch handler
			this.mTouchDispatchDelegate = touchDispatchDelegate;
	}
}