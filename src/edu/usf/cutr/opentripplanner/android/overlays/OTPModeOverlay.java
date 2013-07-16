package edu.usf.cutr.opentripplanner.android.overlays;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.opentripplanner.routing.core.TraverseMode;
import org.osmdroid.ResourceProxy;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.MapView.Projection;
import org.osmdroid.views.overlay.Overlay;

import edu.usf.cutr.opentripplanner.android.R;
import edu.usf.cutr.opentripplanner.android.fragments.MainFragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class OTPModeOverlay extends Overlay {



	private ArrayList<GeoPoint> mPointArray = new ArrayList<GeoPoint>();
	
	private ArrayList<String> mLegModeArray = new ArrayList<String>();
	
	private MainFragment mainFragment;
	
	private static final String TAG = "OTP";

	public OTPModeOverlay(Context ctx) {
		super(ctx);
	}

	public OTPModeOverlay(MainFragment mainFragment) {
		super(mainFragment.getActivity());
		this.mainFragment = mainFragment;
	}

	public void addLeg(GeoPoint point, String legMode){
		mPointArray.add(point);
		mLegModeArray.add(legMode);
	}
	
	public OTPModeOverlay(ResourceProxy pResourceProxy) {
		super(pResourceProxy);
		// TODO Auto-generated constructor stub
	}


	@Override
	protected void draw(final Canvas c, final MapView osmv, final boolean shadow) {
		// super.draw(c, osmv, shadow);

		for (int i = 0; i < mLegModeArray.size(); i++){
			
			Point screenPts = new Point();
			osmv.getProjection().toPixels(mPointArray.get(i), screenPts);
			
			int icon = R.drawable.walk;
			TraverseMode mode = TraverseMode.valueOf((String) mLegModeArray.get(i));
			if(mode.compareTo(TraverseMode.BICYCLE)==0){
				icon = R.drawable.bicycle;
			} else if(mode.compareTo(TraverseMode.CAR)==0){
				icon = R.drawable.icon;
			} else if(mode.compareTo(TraverseMode.BUS)==0){
				icon = R.drawable.bus;
			} else if(mode.compareTo(TraverseMode.RAIL)==0){
				icon = R.drawable.rail;
			} else if(mode.compareTo(TraverseMode.FERRY)==0){
				icon = R.drawable.ferry;
			} else if(mode.compareTo(TraverseMode.GONDOLA)==0){
				icon = R.drawable.gondola;
			} else if(mode.compareTo(TraverseMode.SUBWAY)==0){
				icon = R.drawable.subway;
			} else if(mode.compareTo(TraverseMode.TRAM)==0){
				icon = R.drawable.tram;
			}
			
			Bitmap bmp = BitmapFactory.decodeResource(mainFragment.getResources(), icon);
			int verticalPositionCorrection = mainFragment.getActivity().getResources().getInteger(R.integer.mode_icons_position_vertical_correction);
			int horizontalPositionCorrection = mainFragment.getActivity().getResources().getInteger(R.integer.mode_icons_position_horizontal_correction);
			c.drawBitmap(bmp, screenPts.x + horizontalPositionCorrection, screenPts.y + verticalPositionCorrection, null);
				
		}			
		
		return;
	}
	
	 public void removeAllMode(){
		 mLegModeArray = new ArrayList<String>();
		 mPointArray = new ArrayList<GeoPoint>();
	 }

}
