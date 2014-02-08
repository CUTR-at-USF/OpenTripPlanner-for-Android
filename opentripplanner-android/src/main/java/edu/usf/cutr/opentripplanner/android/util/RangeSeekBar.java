/*
 * Copyright 2013 Stephan Tittel, Peter Sinnott, and Thomas Barrasso
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 * See https://code.google.com/p/range-seek-bar/ for details.
 */

package edu.usf.cutr.opentripplanner.android.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.ImageView;

import java.math.BigDecimal;

import edu.usf.cutr.opentripplanner.android.R;

/**
 * Widget that lets users select a minimum and maximum value on a given
 * numerical range. The range value types can be one of Long, Double, Integer,
 * Float, Short, Byte or BigDecimal.<br />
 * <br />
 * Improved {@link MotionEvent} handling for smoother use, anti-aliased painting
 * for improved aesthetics.
 *
 * @param <T> The Number type of the range values. One of Long, Double, Integer,
 *            Float, Short, Byte or BigDecimal.
 * @author Stephan Tittel (stephan.tittel@kom.tu-darmstadt.de)
 * @author Peter Sinnott (psinnott@gmail.com)
 * @author Thomas Barrasso (tbarrasso@sevenplusandroid.org)
 */
public class RangeSeekBar<T extends Number> extends ImageView {

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    // private final Bitmap thumbImage = BitmapFactory.decodeResource(
    // getResources(), R.drawable.seek_thumb_normal);
    private final Bitmap thumbImage;

    // private final Bitmap thumbPressedImage = BitmapFactory.decodeResource(
    // getResources(), R.drawable.seek_thumb_pressed);
    private final Bitmap thumbPressedImage;

    private final float thumbWidth;

    private final float thumbHalfWidth;

    private final float thumbHalfHeight;

    private final float lineHeight;

    private final float padding;

    private final T absoluteMinValue, absoluteMaxValue;

    private final NumberType numberType;

    private final double absoluteMinValuePrim, absoluteMaxValuePrim;

    private double normalizedMinValue = 0d;

    private double normalizedMaxValue = 1d;

    private Thumb pressedThumb = null;

    private boolean notifyWhileDragging = true;

    public final boolean IS_MULTI_COLORED;

    public final int SINGLE_COLOR;

    public int LEFT_COLOR;

    public int MIDDLE_COLOR;

    public int RIGHT_COLOR;

    public static final int BACKGROUND_COLOR = Color.GRAY;

    private OnRangeSeekBarChangeListener<T> listener;

    /**
     * Default color of a {@link RangeSeekBar}, #FF33B5E5. This is also known as
     * "Ice Cream Sandwich" blue.
     */
    public static final int DEFAULT_COLOR = Color.argb(0xFF, 0x33, 0xB5, 0xE5);

    /**
     * Callback listener interface to notify about changed range values.
     *
     * @param <T> The Number type the RangeSeekBar has been declared with.
     * @author Stephan Tittel (stephan.tittel@kom.tu-darmstadt.de)
     */
    public interface OnRangeSeekBarChangeListener<T> {

        public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar,
                T minValue, T maxValue);
    }

    /**
     * Registers given listener callback to notify about changed selected
     * values.
     *
     * @param listener The listener to notify about changed selected values.
     */
    public void setOnRangeSeekBarChangeListener(
            OnRangeSeekBarChangeListener<T> listener) {
        this.listener = listener;
    }

    /**
     * An invalid pointer id.
     */
    public static final int INVALID_POINTER_ID = 255;

    // Localized constants from MotionEvent for compatibility
    // with API < 8 "Froyo".
    public static final int ACTION_POINTER_UP = 0x6,
            ACTION_POINTER_INDEX_MASK = 0x0000ff00,
            ACTION_POINTER_INDEX_SHIFT = 8;

    private float mDownMotionX;

    private int mActivePointerId = INVALID_POINTER_ID;

    /**
     * On touch, this offset plus the scaled value from the position of the
     * touch will form the progress value. Usually 0.
     */
    float mTouchProgressOffset;

    private int mScaledTouchSlop;

    private boolean mIsDragging;

    /**
     * Creates a new RangeSeekBar.
     *
     * @param absoluteMinValue The minimum value of the selectable range.
     * @param absoluteMaxValue The maximum value of the selectable range.
     * @throws IllegalArgumentException Will be thrown if min/max value type is not one of Long,
     *                                  Double, Integer, Float, Short, Byte or BigDecimal.
     */
    public RangeSeekBar(T absoluteMinValue, T absoluteMaxValue, Context context)
            throws IllegalArgumentException {
        super(context);
        this.absoluteMinValue = absoluteMinValue;
        this.absoluteMaxValue = absoluteMaxValue;
        absoluteMinValuePrim = absoluteMinValue.doubleValue();
        absoluteMaxValuePrim = absoluteMaxValue.doubleValue();
        numberType = NumberType.fromNumber(absoluteMinValue);
        IS_MULTI_COLORED = false;
        SINGLE_COLOR = Color.argb(0xFF, 0x33, 0xB5, 0xE5);
        LEFT_COLOR = 0;
        MIDDLE_COLOR = 0;
        RIGHT_COLOR = 0;
        thumbImage = BitmapFactory.decodeResource(getResources(),
                R.drawable.seek_thumb_normal);
        thumbPressedImage = BitmapFactory.decodeResource(getResources(),
                R.drawable.seek_thumb_pressed);
        thumbWidth = thumbImage.getWidth();
        thumbHalfWidth = 0.5f * thumbWidth;
        thumbHalfHeight = 0.5f * thumbImage.getHeight();
        lineHeight = 0.3f * thumbHalfHeight;
        padding = thumbHalfWidth;

        // make RangeSeekBar focusable. This solves focus handling issues in
        // case EditText widgets are being used along with the RangeSeekBar
        // within ScollViews.
        setFocusable(true);
        setFocusableInTouchMode(true);
        init();
    }

    /**
     * Creates a new RangeSeekBar.
     *
     * @param absoluteMinValue   The minimum value of the selectable range.
     * @param absoluteMaxValue   The maximum value of the selectable range.
     * @param singleColor        The <tt>int</tt> of the color to go between the two thumb
     *                           points will Default to #FF33B5E5 (ICS) if 0 is sent.
     * @param thumbNormal_ResID  The ResourceID of the image for the normal thumb icon.
     * @param thumbPressed_ResID The ResourceID of the image for the pressed thumb icon.
     * @throws IllegalArgumentException Will be thrown if min/max value type is not one of Long,
     *                                  Double, Integer, Float, Short, Byte or BigDecimal.
     */
    public RangeSeekBar(T absoluteMinValue, T absoluteMaxValue,
            Context context, int singleColor, int thumbNormal_ResID,
            int thumbPressed_ResID) throws IllegalArgumentException {
        super(context);
        this.absoluteMinValue = absoluteMinValue;
        this.absoluteMaxValue = absoluteMaxValue;
        absoluteMinValuePrim = absoluteMinValue.doubleValue();
        absoluteMaxValuePrim = absoluteMaxValue.doubleValue();
        numberType = NumberType.fromNumber(absoluteMinValue);
        IS_MULTI_COLORED = false;
        SINGLE_COLOR = singleColor < 0 ? singleColor : Color.argb(0xFF, 0x33,
                0xB5, 0xE5);
        LEFT_COLOR = 0;
        MIDDLE_COLOR = 0;
        RIGHT_COLOR = 0;
        thumbImage = BitmapFactory.decodeResource(getResources(),
                thumbNormal_ResID);
        thumbPressedImage = BitmapFactory.decodeResource(getResources(),
                thumbPressed_ResID);
        thumbWidth = thumbImage.getWidth();
        thumbHalfWidth = 0.5f * thumbWidth;
        thumbHalfHeight = 0.5f * thumbImage.getHeight();
        lineHeight = 0.3f * thumbHalfHeight;
        padding = thumbHalfWidth;

        // make RangeSeekBar focusable. This solves focus handling issues in
        // case EditText widgets are being used along with the RangeSeekBar
        // within ScollViews.
        setFocusable(true);
        setFocusableInTouchMode(true);
        init();
    }

    /**
     * Creates a new RangeSeekBar.
     *
     * @param absoluteMinValue   The minimum value of the selectable range.
     * @param absoluteMaxValue   The maximum value of the selectable range.
     * @param leftColor          The <tt>int</tt> of the color to go between the left most
     *                           point and the left (min) thumb point. will Default to
     *                           #FFFF0000 (RED) if 0 is sent.
     * @param middleColor        The <tt>int</tt> of the color to go between the two thumb
     *                           points will Default to #FF00FF00 (GREEN) if 0 is sent.
     * @param rightColor         The <tt>int</tt> of the color to go between the right most
     *                           point and the right (max) thumb point. will Default to
     *                           #FF0000FF (BLUE) if 0 is sent.
     * @param thumbNormal_ResID  The ResourceID of the image for the normal thumb icon.
     * @param thumbPressed_ResID The ResourceID of the image for the pressed thumb icon.
     * @throws IllegalArgumentException Will be thrown if min/max value type is not one of Long,
     *                                  Double, Integer, Float, Short, Byte or BigDecimal.
     */
    public RangeSeekBar(T absoluteMinValue, T absoluteMaxValue,
            Context context, int leftColor, int middleColor, int rightColor,
            int thumbNormal_ResID, int thumbPressed_ResID)
            throws IllegalArgumentException {
        super(context);
        this.absoluteMinValue = absoluteMinValue;
        this.absoluteMaxValue = absoluteMaxValue;
        absoluteMinValuePrim = absoluteMinValue.doubleValue();
        absoluteMaxValuePrim = absoluteMaxValue.doubleValue();
        numberType = NumberType.fromNumber(absoluteMinValue);

        // Added so we can draw right colors
        IS_MULTI_COLORED = true;
        SINGLE_COLOR = 0;
        LEFT_COLOR = leftColor < 0 ? leftColor : Color.argb(0xFF, 0xFF, 0x00,
                0x00);
        MIDDLE_COLOR = middleColor < 0 ? middleColor : Color.argb(0xFF, 0x00,
                0xFF, 0x00);
        RIGHT_COLOR = rightColor < 0 ? rightColor : Color.argb(0xFF, 0x00,
                0x00, 0xFF);
        thumbImage = BitmapFactory.decodeResource(getResources(),
                thumbNormal_ResID);
        thumbPressedImage = BitmapFactory.decodeResource(getResources(),
                thumbPressed_ResID);
        thumbWidth = thumbImage.getWidth();
        thumbHalfWidth = 0.5f * thumbWidth;
        thumbHalfHeight = 0.5f * thumbImage.getHeight();
        lineHeight = 0.3f * thumbHalfHeight;
        padding = thumbHalfWidth;

        // make RangeSeekBar focusable. This solves focus handling issues in
        // case EditText widgets are being used along with the RangeSeekBar
        // within ScollViews.
        setFocusable(true);
        setFocusableInTouchMode(true);
        init();
    }

    private final void init() {
        mScaledTouchSlop = ViewConfiguration.get(getContext())
                .getScaledTouchSlop();
    }

    public boolean isNotifyWhileDragging() {
        return notifyWhileDragging;
    }

    /**
     * Should the widget notify the listener callback while the user is still
     * dragging a thumb? Default is false.
     */
    public void setNotifyWhileDragging(boolean flag) {
        this.notifyWhileDragging = flag;
    }

    /**
     * Returns the absolute minimum value of the range that has been set at
     * construction time.
     *
     * @return The absolute minimum value of the range.
     */
    public T getAbsoluteMinValue() {
        return absoluteMinValue;
    }

    /**
     * Returns the absolute maximum value of the range that has been set at
     * construction time.
     *
     * @return The absolute maximum value of the range.
     */
    public T getAbsoluteMaxValue() {
        return absoluteMaxValue;
    }

    /**
     * Returns the currently selected min value.
     *
     * @return The currently selected min value.
     */
    public T getSelectedMinValue() {
        return normalizedToValue(normalizedMinValue);
    }

    /**
     * Sets the currently selected minimum value. The widget will be invalidated
     * and redrawn.
     *
     * @param value The Number value to set the minimum value to. Will be clamped
     *              to given absolute minimum/maximum range.
     */
    public void setSelectedMinValue(T value) {
        // in case absoluteMinValue == absoluteMaxValue, avoid division by zero
        // when normalizing.
        if (0 == (absoluteMaxValuePrim - absoluteMinValuePrim)) {
            setNormalizedMinValue(0d);
        } else {
            setNormalizedMinValue(valueToNormalized(value));
        }
    }

    /**
     * Returns the currently selected max value.
     *
     * @return The currently selected max value.
     */
    public T getSelectedMaxValue() {
        return normalizedToValue(normalizedMaxValue);
    }

    /**
     * Sets the currently selected maximum value. The widget will be invalidated
     * and redrawn.
     *
     * @param value The Number value to set the maximum value to. Will be clamped
     *              to given absolute minimum/maximum range.
     */
    public void setSelectedMaxValue(T value) {
        // in case absoluteMinValue == absoluteMaxValue, avoid division by zero
        // when normalizing.
        if (0 == (absoluteMaxValuePrim - absoluteMinValuePrim)) {
            setNormalizedMaxValue(1d);
        } else {
            setNormalizedMaxValue(valueToNormalized(value));
        }
    }

    /**
     * Handles thumb selection and movement. Notifies listener callback on
     * certain events.
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (!isEnabled()) {
            return false;
        }

        int pointerIndex;

        final int action = event.getAction();
        switch (action & MotionEvent.ACTION_MASK) {

            case MotionEvent.ACTION_DOWN:
                // Remember where the motion event started
                mActivePointerId = event.getPointerId(event.getPointerCount() - 1);
                pointerIndex = event.findPointerIndex(mActivePointerId);
                mDownMotionX = event.getX(pointerIndex);

                pressedThumb = evalPressedThumb(mDownMotionX);

                // Only handle thumb presses.
                if (pressedThumb == null) {
                    return super.onTouchEvent(event);
                }

                setPressed(true);
                invalidate();
                onStartTrackingTouch();
                trackTouchEvent(event);
                attemptClaimDrag();

                break;
            case MotionEvent.ACTION_MOVE:
                if (pressedThumb != null) {

                    if (mIsDragging) {
                        trackTouchEvent(event);
                    } else {
                        // Scroll to follow the motion event
                        pointerIndex = event.findPointerIndex(mActivePointerId);
                        final float x = event.getX(pointerIndex);

                        if (Math.abs(x - mDownMotionX) > mScaledTouchSlop) {
                            setPressed(true);
                            invalidate();
                            onStartTrackingTouch();
                            trackTouchEvent(event);
                            attemptClaimDrag();
                        }
                    }

                    if (notifyWhileDragging && listener != null) {
                        listener.onRangeSeekBarValuesChanged(this,
                                getSelectedMinValue(), getSelectedMaxValue());
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mIsDragging) {
                    trackTouchEvent(event);
                    onStopTrackingTouch();
                    setPressed(false);
                } else {
                    // Touch up when we never crossed the touch slop threshold
                    // should be interpreted as a tap-seek to that location.
                    onStartTrackingTouch();
                    trackTouchEvent(event);
                    onStopTrackingTouch();
                }

                pressedThumb = null;
                invalidate();
                if (listener != null) {
                    listener.onRangeSeekBarValuesChanged(this,
                            getSelectedMinValue(), getSelectedMaxValue());
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN: {
                final int index = event.getPointerCount() - 1;
                // final int index = ev.getActionIndex();
                mDownMotionX = event.getX(index);
                mActivePointerId = event.getPointerId(index);
                invalidate();
                break;
            }
            case MotionEvent.ACTION_POINTER_UP:
                onSecondaryPointerUp(event);
                invalidate();
                break;
            case MotionEvent.ACTION_CANCEL:
                if (mIsDragging) {
                    onStopTrackingTouch();
                    setPressed(false);
                }
                invalidate(); // see above explanation
                break;
        }
        return true;
    }

    private final void onSecondaryPointerUp(MotionEvent ev) {
        final int pointerIndex = (ev.getAction() & ACTION_POINTER_INDEX_MASK)
                >> ACTION_POINTER_INDEX_SHIFT;

        final int pointerId = ev.getPointerId(pointerIndex);
        if (pointerId == mActivePointerId) {
            // This was our active pointer going up. Choose
            // a new active pointer and adjust accordingly.
            // TODO: Make this decision more intelligent.
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            mDownMotionX = ev.getX(newPointerIndex);
            mActivePointerId = ev.getPointerId(newPointerIndex);
        }
    }

    private final void trackTouchEvent(MotionEvent event) {
        final int pointerIndex = event.findPointerIndex(mActivePointerId);
        final float x = event.getX(pointerIndex);

        if (Thumb.MIN.equals(pressedThumb)) {
            setNormalizedMinValue(screenToNormalized(x));
        } else if (Thumb.MAX.equals(pressedThumb)) {
            setNormalizedMaxValue(screenToNormalized(x));
        }
    }

    /**
     * Tries to claim the user's drag motion, and requests disallowing any
     * ancestors from stealing events in the drag.
     */
    private void attemptClaimDrag() {
        if (getParent() != null) {
            getParent().requestDisallowInterceptTouchEvent(true);
        }
    }

    /**
     * This is called when the user has started touching this widget.
     */
    void onStartTrackingTouch() {
        mIsDragging = true;
    }

    /**
     * This is called when the user either releases his touch or the touch is
     * canceled.
     */
    void onStopTrackingTouch() {
        mIsDragging = false;
    }

    /**
     * Ensures correct size of the widget.
     */
    @Override
    protected synchronized void onMeasure(int widthMeasureSpec,
            int heightMeasureSpec) {
        int width = 200;
        if (MeasureSpec.UNSPECIFIED != MeasureSpec.getMode(widthMeasureSpec)) {
            width = MeasureSpec.getSize(widthMeasureSpec);
        }
        int height = thumbImage.getHeight();
        if (MeasureSpec.UNSPECIFIED != MeasureSpec.getMode(heightMeasureSpec)) {
            height = Math.min(height, MeasureSpec.getSize(heightMeasureSpec));
        }
        setMeasuredDimension(width, height);
    }

    /**
     * Draws the widget on the given canvas.
     */
    @Override
    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        paint.setStyle(Style.FILL);
        paint.setAntiAlias(true);

        if (!IS_MULTI_COLORED) {
            // draw seek bar background line
            final RectF rect = new RectF(padding,
                    0.5f * (getHeight() - lineHeight), getWidth() - padding,
                    0.5f * (getHeight() + lineHeight));
            paint.setColor(BACKGROUND_COLOR);
            canvas.drawRect(rect, paint);

            // draw seek bar active range line
            rect.left = normalizedToScreen(normalizedMinValue);
            rect.right = normalizedToScreen(normalizedMaxValue);

            // orange color
            paint.setColor(SINGLE_COLOR);
            canvas.drawRect(rect, paint);
        } else {
            final RectF rectR = new RectF(padding,
                    0.5f * (getHeight() - lineHeight),
                    normalizedToScreen(normalizedMinValue),
                    0.5f * (getHeight() + lineHeight));
            paint.setColor(LEFT_COLOR);
            canvas.drawRect(rectR, paint);

            // draw seek bar background line
            final RectF rectY = new RectF(padding,
                    0.5f * (getHeight() - lineHeight), getWidth() - padding,
                    0.5f * (getHeight() + lineHeight));

            // draw seek bar active range line
            rectY.left = normalizedToScreen(normalizedMinValue);
            rectY.right = normalizedToScreen(normalizedMaxValue);

            paint.setColor(MIDDLE_COLOR);
            canvas.drawRect(rectY, paint);

            final RectF rectG = new RectF(
                    normalizedToScreen(normalizedMaxValue),
                    0.5f * (getHeight() - lineHeight), getWidth() - padding,
                    0.5f * (getHeight() + lineHeight));
            paint.setColor(RIGHT_COLOR);
            canvas.drawRect(rectG, paint);
        }

        // draw minimum thumb
        drawThumb(normalizedToScreen(normalizedMinValue),
                Thumb.MIN.equals(pressedThumb), canvas);

        // draw maximum thumb
        drawThumb(normalizedToScreen(normalizedMaxValue),
                Thumb.MAX.equals(pressedThumb), canvas);
    }

    /**
     * Overridden to save instance state when device orientation changes. This
     * method is called automatically if you assign an id to the RangeSeekBar
     * widget using the {@link #setId(int)} method. Other members of this class
     * than the normalized min and max values don't need to be saved.
     */
    @Override
    protected Parcelable onSaveInstanceState() {
        final Bundle bundle = new Bundle();
        bundle.putParcelable("SUPER", super.onSaveInstanceState());
        bundle.putDouble("MIN", normalizedMinValue);
        bundle.putDouble("MAX", normalizedMaxValue);
        return bundle;
    }

    /**
     * Overridden to restore instance state when device orientation changes.
     * This method is called automatically if you assign an id to the
     * RangeSeekBar widget using the {@link #setId(int)} method.
     */
    @Override
    protected void onRestoreInstanceState(Parcelable parcel) {
        final Bundle bundle = (Bundle) parcel;
        super.onRestoreInstanceState(bundle.getParcelable("SUPER"));
        normalizedMinValue = bundle.getDouble("MIN");
        normalizedMaxValue = bundle.getDouble("MAX");
    }

    /**
     * Draws the "normal" resp. "pressed" thumb image on specified x-coordinate.
     *
     * @param screenCoord The x-coordinate in screen space where to draw the image.
     * @param pressed     Is the thumb currently in "pressed" state?
     * @param canvas      The canvas to draw upon.
     */
    private void drawThumb(float screenCoord, boolean pressed, Canvas canvas) {
        canvas.drawBitmap(pressed ? thumbPressedImage : thumbImage, screenCoord
                - thumbHalfWidth,
                (float) ((0.5f * getHeight()) - thumbHalfHeight), paint);
    }

    /**
     * Decides which (if any) thumb is touched by the given x-coordinate.
     *
     * @param touchX The x-coordinate of a touch event in screen space.
     * @return The pressed thumb or null if none has been touched.
     */
    private Thumb evalPressedThumb(float touchX) {
        Thumb result = null;
        boolean minThumbPressed = isInThumbRange(touchX, normalizedMinValue);
        boolean maxThumbPressed = isInThumbRange(touchX, normalizedMaxValue);
        if (minThumbPressed && maxThumbPressed) {
            // if both thumbs are pressed (they lie on top of each other),
            // choose the one with more room to drag. this avoids "stalling" the
            // thumbs in a corner, not being able to drag them apart anymore.
            result = (touchX / getWidth() > 0.5f) ? Thumb.MIN : Thumb.MAX;
        } else if (minThumbPressed) {
            result = Thumb.MIN;
        } else if (maxThumbPressed) {
            result = Thumb.MAX;
        }
        return result;
    }

    /**
     * Decides if given x-coordinate in screen space needs to be interpreted as
     * "within" the normalized thumb x-coordinate.
     *
     * @param touchX               The x-coordinate in screen space to check.
     * @param normalizedThumbValue The normalized x-coordinate of the thumb to check.
     * @return true if x-coordinate is in thumb range, false otherwise.
     */
    private boolean isInThumbRange(float touchX, double normalizedThumbValue) {
        return Math.abs(touchX - normalizedToScreen(normalizedThumbValue)) <= thumbHalfWidth;
    }

    /**
     * Sets normalized min value to value so that 0 <= value <= normalized max
     * value <= 1. The View will get invalidated when calling this method.
     *
     * @param value The new normalized min value to set.
     */
    public void setNormalizedMinValue(double value) {
        normalizedMinValue = Math.max(0d,
                Math.min(1d, Math.min(value, normalizedMaxValue)));
        invalidate();
    }

    /**
     * Sets normalized max value to value so that 0 <= normalized min value <=
     * value <= 1. The View will get invalidated when calling this method.
     *
     * @param value The new normalized max value to set.
     */
    public void setNormalizedMaxValue(double value) {
        normalizedMaxValue = Math.max(0d,
                Math.min(1d, Math.max(value, normalizedMinValue)));
        invalidate();
    }

    /**
     * Converts a normalized value to a Number object in the value space between
     * absolute minimum and maximum.
     */
    @SuppressWarnings("unchecked")
    private T normalizedToValue(double normalized) {
        return (T) numberType.toNumber(absoluteMinValuePrim + normalized
                * (absoluteMaxValuePrim - absoluteMinValuePrim));
    }

    /**
     * Converts the given Number value to a normalized double.
     *
     * @param value The Number value to normalize.
     * @return The normalized double.
     */
    private double valueToNormalized(T value) {
        if (0 == absoluteMaxValuePrim - absoluteMinValuePrim) {
            // prevent division by zero, simply return 0.
            return 0d;
        }
        return (value.doubleValue() - absoluteMinValuePrim)
                / (absoluteMaxValuePrim - absoluteMinValuePrim);
    }

    /**
     * Converts a normalized value into screen space.
     *
     * @param normalizedCoord The normalized value to convert.
     * @return The converted value in screen space.
     */
    private float normalizedToScreen(double normalizedCoord) {
        return (float) (padding + normalizedCoord * (getWidth() - 2 * padding));
    }

    /**
     * Converts screen space x-coordinates into normalized values.
     *
     * @param screenCoord The x-coordinate in screen space to convert.
     * @return The normalized value.
     */
    private double screenToNormalized(float screenCoord) {
        int width = getWidth();
        if (width <= 2 * padding) {
            // prevent division by zero, simply return 0.
            return 0d;
        } else {
            double result = (screenCoord - padding) / (width - 2 * padding);
            return Math.min(1d, Math.max(0d, result));
        }
    }

    /**
     * Thumb constants (min and max).
     */
    private static enum Thumb {
        MIN, MAX
    }

    ;

    /**
     * Utility enumaration used to convert between Numbers and doubles.
     *
     * @author Stephan Tittel (stephan.tittel@kom.tu-darmstadt.de)
     */
    private static enum NumberType {
        LONG, DOUBLE, INTEGER, FLOAT, SHORT, BYTE, BIG_DECIMAL;

        public static <E extends Number> NumberType fromNumber(E value)
                throws IllegalArgumentException {
            if (value instanceof Long) {
                return LONG;
            }
            if (value instanceof Double) {
                return DOUBLE;
            }
            if (value instanceof Integer) {
                return INTEGER;
            }
            if (value instanceof Float) {
                return FLOAT;
            }
            if (value instanceof Short) {
                return SHORT;
            }
            if (value instanceof Byte) {
                return BYTE;
            }
            if (value instanceof BigDecimal) {
                return BIG_DECIMAL;
            }
            throw new IllegalArgumentException("Number class '"
                    + value.getClass().getName() + "' is not supported");
        }

        public Number toNumber(double value) {
            switch (this) {
                case LONG:
                    return new Long((long) value);
                case DOUBLE:
                    return value;
                case INTEGER:
                    return new Integer((int) value);
                case FLOAT:
                    return new Float(value);
                case SHORT:
                    return new Short((short) value);
                case BYTE:
                    return new Byte((byte) value);
                case BIG_DECIMAL:
                    return new BigDecimal(value);
            }
            throw new InstantiationError("can't convert " + this
                    + " to a Number object");
        }
    }

    public int getLeftColor() {
        return LEFT_COLOR;
    }

    public void setLeftColor(int leftColor) {
        this.LEFT_COLOR = leftColor;
    }

    public int getMiddleColor() {
        return MIDDLE_COLOR;
    }

    public void setMiddleColor(int middleColor) {
        this.MIDDLE_COLOR = middleColor;
    }

    public int getRightColor() {
        return RIGHT_COLOR;
    }

    public void setRightColor(int rightColor) {
        this.RIGHT_COLOR = rightColor;
    }
}
