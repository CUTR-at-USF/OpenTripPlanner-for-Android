package org.opentripplanner.android;

import java.util.ArrayList;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.MapView.Projection;
import org.osmdroid.views.overlay.Overlay;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;

public class OTPPathOverlay extends Overlay {
	private static final String TAG = "OTP";
	/**
	 * Stores points, converted to the map projection.
	 */
	private ArrayList<ArrayList<Point>> mPointsArray = new ArrayList<ArrayList<Point>>();

	/**
	 * Number of points that have precomputed values.
	 */
	private ArrayList<Integer> mPointsPrecomputedArray = new ArrayList<Integer>();

	/**
	 * Paint settings.
	 */
	protected ArrayList<Paint> mPaintArray = new ArrayList<Paint>();
	
	private ArrayList<Path> mPathArray = new ArrayList<Path>();

	private ArrayList<Point> mTempPoint1Array = new ArrayList<Point>();
	private ArrayList<Point> mTempPoint2Array = new ArrayList<Point>();
	
	// bounding rectangle for the current line segment.
    private ArrayList<Rect> mLineBoundsArray = new ArrayList<Rect>();

	public OTPPathOverlay(final int color, final Context ctx) {
		this(color, new DefaultResourceProxyImpl(ctx));
	}

	public OTPPathOverlay(final int color, final ResourceProxy pResourceProxy) {
		super(pResourceProxy);
	}

	/**
	 * @param color
	 */
	 public void addPath(int color){
		 ArrayList<Point> mPoints = new ArrayList<Point>();
		 mPointsArray.add(mPoints);

		 Integer mPointsPrecomputed = new Integer(0);
		 mPointsPrecomputedArray.add(mPointsPrecomputed);

		 Paint mPaint = new Paint();
		 mPaint.setColor(color);
		 mPaint.setStrokeWidth(5.0f);
		 mPaint.setStyle(Paint.Style.STROKE);
		 mPaint.setAlpha(200);
		 mPaintArray.add(mPaint);
		 
		 final Path mPath = new Path();
		 mPathArray.add(mPath);

		 final Point mTempPoint1 = new Point();
		 mTempPoint1Array.add(mTempPoint1);

		 final Point mTempPoint2 = new Point();
		 mTempPoint2Array.add(mTempPoint2);
		 
		 final Rect mLineBounds = new Rect();
		 mLineBoundsArray.add(mLineBounds);
	 }

	 public ArrayList<Point> getPath(int index){
		 if(index>=mPointsArray.size())
			 return null;
		 return mPointsArray.get(index);
	 }

	 public void removeAllPath(){
		 mPointsArray = new ArrayList<ArrayList<Point>>();
		 mPointsPrecomputedArray = new ArrayList<Integer>();
		 mPaintArray = new ArrayList<Paint>();
		 mPathArray = new ArrayList<Path>();
		 mTempPoint1Array = new ArrayList<Point>();
		 mTempPoint2Array = new ArrayList<Point>();
		 mLineBoundsArray = new ArrayList<Rect>();
	 }

	 public void setColor(int index, final int color) {
		 mPaintArray.get(index).setColor(color);
	 }

	 public void setAlpha(int index, final int a) {
		 mPaintArray.get(index).setAlpha(a);
	 }
	 
	 public Paint getPaint(int index) {
         return mPaintArray.get(index);
 }

 public void setPaint(int index, Paint pPaint) {	 
         if (pPaint == null)
                 throw new IllegalArgumentException("pPaint argument cannot be null");
         Paint mPaint = mPaintArray.get(index);
         mPaint = pPaint;
 }

	 public void clearPath(int index) {
		 ArrayList<Point> mPoints = mPointsArray.get(index); 
		 mPoints = new ArrayList<Point>();

		 Integer mPointsPrecomputed = mPointsPrecomputedArray.get(index);
		 mPointsPrecomputed = 0;
	 }

	 public void addPoint(int index, final GeoPoint pt) {
		 this.addPoint(index, pt.getLatitudeE6(), pt.getLongitudeE6());
//		 Log.v(TAG, "addPoint in OTPPathOverlay");
	 }

	 public void addPoint(int index, final int latitudeE6, final int longitudeE6) {
		 final Point pt = new Point(latitudeE6, longitudeE6);
		 mPointsArray.get(index).add(pt);
	 }

	 public int getNumberOfPoints(int index) {
		 return mPointsArray.get(index).size();
	 }

	 @Override
	 protected void draw(final Canvas canvas, final MapView mapView, final boolean shadow) {
//		 Log.v("Test", Integer.toString(mPointsArray.size()) + " draw OTPPathOvelay");
		 for(int i=0; i<mPointsArray.size(); i++){
			 drawPathOverlay(i, canvas, mapView, shadow);
		 }
	 }

	 /**
	  *
	  * @author Viesturs Zarins
	  *
	  *         This method draws a path line in given color.
	  */
	 /**
	  * This method draws the line. Note - highly optimized to handle long paths, proceed with care.
	  * Should be fine up to 10K points.
	  */
	 protected void drawPathOverlay(int index, final Canvas canvas, final MapView mapView, final boolean shadow) {

		 if (shadow) {
			 return;
		 }
		 
		 ArrayList<Point> tempPoints = mPointsArray.get(index);
		 ArrayList<Point> mPoints = new ArrayList<Point>();
		 
		 if (tempPoints.size() < 2) {
			 // nothing to paint
			 return;
		 }

		 final Projection pj = mapView.getProjection();

		 // precompute new points to the intermediate projection.
		 final int size = tempPoints.size();
//		 Log.v(TAG, "size = " + Integer.toString(size)+" drawPathOverlay OTPPathOverlay");
		 
		 Integer mPointsPrecomputed = new Integer(mPointsPrecomputedArray.get(index));
		 while (mPointsPrecomputed < size) {
//			 Log.v(TAG, "mPointsPrecomputed = " + Integer.toString(mPointsPrecomputed)+" drawPathOverlay OTPPathOverlay");
			 final Point inPt = tempPoints.get(mPointsPrecomputed);
			 Point outPt = new Point();
			 pj.toMapPixelsProjected(inPt.x, inPt.y, outPt);
			 mPoints.add(outPt);
			 mPointsPrecomputed++;
		 }

		 Point screenPoint0 = null; // points on screen
		 Point screenPoint1 = null;
		 Point projectedPoint0; // points from the points list
		 Point projectedPoint1;
		 
		 Path mPath = new Path(mPathArray.get(index));
		 Rect mLineBounds = new Rect(mLineBoundsArray.get(index));
		 Point mTempPoint1 = new Point(mTempPoint1Array.get(index));
		 Point mTempPoint2 = new Point(mTempPoint2Array.get(index));
		 Paint mPaint = new Paint(mPaintArray.get(index));
		// clipping rectangle in the intermediate projection, to avoid performing projection.
         final Rect clipBounds = pj.fromPixelsToProjected(pj.getScreenRect());

         mPath.rewind();
         projectedPoint0 = mPoints.get(size - 1);
         mLineBounds.set(projectedPoint0.x, projectedPoint0.y, projectedPoint0.x, projectedPoint0.y);
//         Log.v("Test", "[size="+Integer.toString(size)+"] 0 linebounds "+Integer.toString(mLineBounds.top)+","+Integer.toString(mLineBounds.left)+","+Integer.toString(mLineBounds.bottom)+","+Integer.toString(mLineBounds.right)+" drawPathOverlay OTPPathOverlay");
//         Log.v("Test", "projectedPoint0 = "+Integer.toString(projectedPoint0.x)+","+Integer.toString(projectedPoint0.y) );
         
//		 Log.v(TAG, "size = "+Integer.toString(size)+" drawPathOverlay OTPPathOverlay");
		 for (int i = size - 2; i >= 0; i--) {
			 // compute next points
			 projectedPoint1 = mPoints.get(i);
//			 Log.v(TAG, "0' projectedPoint1 "+Integer.toString(projectedPoint1.x)+","+Integer.toString(projectedPoint1.y)+" drawPathOverlay OTPPathOverlay");
			 mLineBounds.union(projectedPoint1.x, projectedPoint1.y);
			 
//			 Log.v(TAG, "1 i = "+Integer.toString(i)+" drawPathOverlay OTPPathOverlay");
//			 Log.v(TAG, "1 clipbounds "+Integer.toString(clipBounds.top)+","+Integer.toString(clipBounds.left)+","+Integer.toString(clipBounds.bottom)+","+Integer.toString(clipBounds.right)+" drawPathOverlay OTPPathOverlay");
//			 Log.v(TAG, "1 linebounds "+Integer.toString(mLineBounds.top)+","+Integer.toString(mLineBounds.left)+","+Integer.toString(mLineBounds.bottom)+","+Integer.toString(mLineBounds.right)+" drawPathOverlay OTPPathOverlay");
//			 Log.v(TAG, "intersect index="+Integer.toString(index));
			 if (!Rect.intersects(clipBounds, mLineBounds)) {
//				 Log.v(TAG, "Why");
				 // skip this line, move to next point
				 projectedPoint0 = projectedPoint1;
				 screenPoint0 = null;
				 continue;
			 }

			 // the starting point may be not calculated, because previous segment was out of clip
			 // bounds
			 if (screenPoint0 == null) {
				 screenPoint0 = pj.toMapPixelsTranslated(projectedPoint0, mTempPoint1);
				 mPath.moveTo(screenPoint0.x, screenPoint0.y);
			 }

			 screenPoint1 = pj.toMapPixelsTranslated(projectedPoint1, mTempPoint2);

//			 Log.v(TAG, "2 i = "+Integer.toString(i)+" drawPathOverlay OTPPathOverlay");
			 // skip this point, too close to previous point
			 if (Math.abs(screenPoint1.x - screenPoint0.x)
					 + Math.abs(screenPoint1.y - screenPoint0.y) <= 1) {
				 continue;
			 }
			 
//			 Log.v(TAG, "3 i = "+Integer.toString(i)+" drawPathOverlay OTPPathOverlay");
			 mPath.lineTo(screenPoint1.x, screenPoint1.y);
		 }
//		 Log.v(TAG, "drawPath index="+Integer.toString(index));
		 canvas.drawPath(mPath, mPaint);

//		 Log.v(TAG, "buffercount = "+Integer.toString(bufferCount)+" drawPathOverlay OTPPathOverlay");
	 }
}