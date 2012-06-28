/*
 * Copyright 2012 University of South Florida
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
 */

package edu.usf.cutr.opentripplanner.android.overlays;

import java.util.Locale;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.MapView.Projection;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import edu.usf.cutr.opentripplanner.android.R;
import edu.usf.cutr.opentripplanner.android.fragments.MainFragment;

/**
 * 
 * @author Khoa Tran
 *
 */

public class MapOverlay extends org.osmdroid.views.overlay.Overlay {

	private GeoPoint location;
	private int markerID = R.drawable.start;

	private Drawable marker = null;
	private boolean inDrag = false;
	private ImageView dragImage = null;
	private int xDragImageOffset = 0;
	private int yDragImageOffset = 0;
	private int xDragTouchOffset = 0;
	private int yDragTouchOffset = 0;
	private Point t = new Point(0, 0);
	private Point p = new Point(0, 0);
	
	private MainFragment mainFragment;
	
	private static final String TAG = "OTP";

	public MapOverlay(Context ctx, int markerID, View view) {
		super(ctx);
		setMarker(markerID, view);
	}

	public MapOverlay(MainFragment mainFragment, int markerID, View view) {
		super(mainFragment.getActivity());
		this.mainFragment = mainFragment;
		marker = mainFragment.getResources().getDrawable(R.drawable.start);
		setMarker(markerID, view);
	}

	public void setMarker(int markerID, View view){
		this.markerID = markerID;
		this.marker = mainFragment.getResources().getDrawable(markerID);

		dragImage=(ImageView)view.findViewById(R.id.drag);


		xDragImageOffset=dragImage.getDrawable().getIntrinsicWidth()/2;
		yDragImageOffset=dragImage.getDrawable().getIntrinsicHeight();

		marker.setBounds(0, 0, marker.getIntrinsicWidth(), marker.getIntrinsicHeight());
	}

	@Override
	protected void draw(final Canvas c, final MapView osmv, final boolean shadow) {
		// super.draw(c, osmv, shadow);

		Point screenPts = new Point();
		osmv.getProjection().toPixels(location, screenPts);

		Bitmap bmp = BitmapFactory.decodeResource(mainFragment.getResources(), markerID);
		c.drawBitmap(bmp, screenPts.x, screenPts.y, null);
		return;
	}

	@Override
	public boolean onLongPress(final MotionEvent e, final MapView mv) {
		Log.d(TAG, "LONG PRESS!");

		final CharSequence[] items = {"Start Location", "End Location"};

		AlertDialog.Builder builder = new AlertDialog.Builder(mainFragment.getActivity());
		builder.setTitle("Choose Type for Point");
		final MapOverlay mo = this;
		builder.setItems(items, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				Toast.makeText(mainFragment.getActivity().getApplicationContext(), items[item], Toast.LENGTH_SHORT).show();
				GeoPoint point = (GeoPoint) mv.getProjection().fromPixels(e.getX(), e.getY());
				Address addr = new Address(Locale.US);
				addr.setLatitude(point.getLatitudeE6()/1E6);
				addr.setLongitude(point.getLongitudeE6()/1E6);
				addr.setAddressLine(addr.getMaxAddressLineIndex()+1, mo.getLocationFormatedString());
				if(items[item].equals("Start Location")) {
					mainFragment.moveMarker(true, addr);
				} else {
					mainFragment.moveMarker(false, addr);
				}
			}
		});
		AlertDialog alert = builder.create();
		alert.show();

		return true;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event, MapView mapView) {
		final int action = event.getAction();
		final int x = (int) event.getX();
		final int y = (int) event.getY();
		final Projection pj = mapView.getProjection();

		boolean result = false;

		if (action == MotionEvent.ACTION_DOWN) {
			//Log.d(TAG, "Touch down!");
			//for (OverlayItem item : items) {

			pj.fromMapPixels(x, y, t);
			pj.toPixels(this.getLocation(), p);

			if (marker.getBounds().contains(t.x - p.x, t.y - p.y)) {
				result = true;
				inDrag = true;
				this.setEnabled(false);
				//items.remove(inDrag);
				//populate();

				xDragTouchOffset = 0;
				yDragTouchOffset = 0;

				setDragImagePosition(x, y);
				dragImage.setVisibility(View.VISIBLE);

				xDragTouchOffset = t.x - p.x;
				yDragTouchOffset = t.y - p.y;

				dragImage.setImageDrawable(marker);
				//break;
			}
			//}
		} else if (action == MotionEvent.ACTION_MOVE && inDrag != false) {
			dragImage.setVisibility(View.VISIBLE);
			setDragImagePosition(x, y);
			result = true;
		} else if (action == MotionEvent.ACTION_UP && inDrag != false) {
			dragImage.setVisibility(View.GONE);

//			GeoPoint pt = (GeoPoint) pj.fromPixels(x - xDragTouchOffset, y - yDragTouchOffset);
			GeoPoint pt = (GeoPoint) pj.fromPixels(x-xDragImageOffset-xDragTouchOffset,	y-yDragImageOffset-yDragTouchOffset);
			//OverlayItem toDrop = new OverlayItem(inDrag.getTitle(),
			//		inDrag.getSnippet(), pt);

			this.setLocation(pt);
			this.setEnabled(true);
			//items.add(toDrop);
			//populate();
			inDrag = false;
			result = true;

			//pj.fromMapPixels(x, y, t);

			//if((t.x - p.x) == xDragTouchOffset && (t.y - p.y) == yDragTouchOffset){
			//Log.d(TAG, "Do something here if desired because we didn't move item " + toDrop.getTitle());

			StringBuilder coords = new StringBuilder().append(pt.getLatitudeE6() / 1E6).append(", ").append(pt.getLongitudeE6() / 1E6);
			if(markerID == R.drawable.start) {
				mainFragment.setTextBoxLocation(coords.toString(), true);
			} else {
				mainFragment.setTextBoxLocation(coords.toString(), false);
			}
			//}
		}

		return (result || super.onTouchEvent(event, mapView));
	}

	/*@Override
	public boolean onSingleTapConfirmed(final MotionEvent e, final MapView mapView) {
		Log.d(TAG, "SINGLE TAP CONFIRMED!");
		return false;
	}*/

	public GeoPoint getLocation() {
		return location;
	}

	public void setLocation(GeoPoint location) {
		this.location = location;
	}

	public int getMarkerID() {
		return markerID;
	}

	public void setMarkerID(int markerID) {
		this.markerID = markerID;
	}

	private void setDragImagePosition(int x, int y) {
		RelativeLayout.LayoutParams lp=
				(RelativeLayout.LayoutParams)dragImage.getLayoutParams();

		lp.setMargins(x-xDragImageOffset-xDragTouchOffset,
				y-yDragImageOffset-yDragTouchOffset, 0, 0);
		dragImage.setLayoutParams(lp);
	}

	public String getLocationFormatedString() {
		return new StringBuilder().append(location.getLatitudeE6() / 1E6).append(", ").append(location.getLongitudeE6() / 1E6).toString();
	}
}