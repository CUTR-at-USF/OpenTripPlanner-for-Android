package org.opentripplanner.android;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;
import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.MapView.Projection;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.MyLocationOverlay;
import org.osmdroid.views.overlay.OverlayItem;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import de.mastacode.http.Http;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class MainActivity extends Activity {

	private MapView mv;
	private MapController mc;
	private MyLocationOverlay mlo;
	private MenuItem mGPS;
	private MenuItem mSettings;
	private MenuItem mExit;
	
	private ImageButton btnStartLocation;
	private ImageButton btnEndLocation;
	private Button btnPlanTrip;

	MapItemizedOverlay itemizedoverlay;
	
	private static LocationManager locationManager;
	private static final int EXIT_ID = 1;
	private static final int GPS_ID = 2;
	private static final int SETTINGS_ID = 3;
	
	private static final String TAG = "MainActivity";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

		btnStartLocation = (ImageButton) findViewById(R.id.btnStartLocation);
		btnEndLocation = (ImageButton) findViewById(R.id.btnEndLocation);
		btnPlanTrip = (Button) findViewById(R.id.btnPlanTrip);
		
		
		//Intent svc = new Intent(this, NavigationService.class);
		//startService(svc);
		
		
		OnClickListener ocl = new OnClickListener() {
			@Override
			public void onClick(View v) {
				mBoundService.updateNotification();
				
				final CharSequence[] items = {"Current Location", "Contact Address", "Point on Map"};

				AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
				builder.setTitle("Choose Start Location");
				builder.setItems(items, new DialogInterface.OnClickListener() {
				    public void onClick(DialogInterface dialog, int item) {
				        Toast.makeText(getApplicationContext(), items[item], Toast.LENGTH_SHORT).show();
				    }
				});
				AlertDialog alert = builder.create();
				alert.show();
				
				//use v.getId() to determine start or end button
			}
		};
		
		btnStartLocation.setOnClickListener(ocl);
		btnEndLocation.setOnClickListener(ocl);
		
		mv = (MapView) findViewById(R.id.mapview);
		mv.setBuiltInZoomControls(true);
		mv.setMultiTouchControls(true);

		mc = mv.getController();
		mc.setZoom(20);
		//mc.setCenter(getLastLocation());
		
		mc.setCenter(getPoint(40.76793169992044, -73.98180484771729));
		
//		mv.setOnLongClickListener(new OnLongClickListener() {
//			
//			@Override
//			public boolean onLongClick(View v) {
//				Log.d(TAG, "Long clicked mv!");
//				return false;
//			}
//		});
		
		// ---Add a location marker---
		 //MapOverlay mapOverlay = new MapOverlay(this, getLastLocation());
		// List<Overlay> listOfOverlays = mv.getOverlays();
		// listOfOverlays.clear();
		// listOfOverlays.add(mapOverlay);
		//mv.getOverlays().add(mapOverlay);
		//mv.invalidate();

		mlo = new MyLocationOverlay(this, mv);
		// mlo.enableCompass();
		mv.getOverlays().add(mlo);

		itemizedoverlay = new MapItemizedOverlay(getResources().getDrawable(R.drawable.arrow), new DefaultResourceProxyImpl(this), this);
		
		GeoPoint point = getLastLocation();
		OverlayItem overlayitem = new OverlayItem("Title!", "Description!",
		point);
		//itemizedoverlay.addOverlayItem(overlayitem);
		//mv.getOverlays().add(itemizedoverlay);

		Drawable marker = getResources().getDrawable(R.drawable.start);
		marker.setBounds(0, 0, marker.getIntrinsicWidth(), marker.getIntrinsicHeight());
		SitesOverlay so = new SitesOverlay(marker, new ArrayList<OverlayItem>());
		so.setFocusItemsOnTap(true);
		//so.setFocusedItem(0);
		mv.getOverlays().add(so);

		
		
		//SitesOverlay so = new SitesOverlay(getResources().getDrawable(R.drawable.icon));
		//so.addOverlayItem(overlayitem);
		//mv.getOverlays().add(so);
		
		/*String Url = "https://spreadsheets.google.com/ccc?key=0AgWy8ujaGosCdDhxTC04cUZNeHo0eGFBQTBpU2dxN0E&hl=en&authkey=CK-H__IP";
		Url = "https://spreadsheets.google.com/pub?hl=en&hl=en&key=0AgWy8ujaGosCdDhxTC04cUZNeHo0eGFBQTBpU2dxN0E&single=true&gid=0&output=csv";
		
		HttpClient client = new DefaultHttpClient();
		String result = "";
		try {
			result = Http.get(Url).use(client).charset("UTF-8").followRedirects(true).asString();
			Log.d(TAG, "Spreadsheet: " + result);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		List<Server> knownServers = new ArrayList<Server>();
		
		CSVReader reader = new CSVReader(new StringReader(result));
		try {
			List<String[]> entries = reader.readAll();
			for (String[] e : entries) {
				if(e[0] == "Region") {
					continue; //Ignore the first line of the file
				}
				Server s = new Server(e[0], e[1], e[2], e[3], e[4], e[5]);
				knownServers.add(s);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Log.d(TAG, "Servers: " + knownServers.size());
		*/
		
		
		String u = "http://go.cutr.usf.edu:8083/opentripplanner-api-webapp/ws/metadata";
		HttpClient client = new DefaultHttpClient();
		String result = "";
		try {
			result = Http.get(u).use(client).header("Accept", "application/json").charset("UTF-8").followRedirects(true).asString();
			Log.d(TAG, "Result: " + result);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		GsonBuilder gsonb = new GsonBuilder();
		Gson gson = gsonb.create();
		 
		JSONObject j;
		GraphMetadata metadata = null;
		 
		try
		{
		    j = new JSONObject(result);
		    metadata = gson.fromJson(j.toString(), GraphMetadata.class);
		}
		catch(Exception e)
		{
		    e.printStackTrace();
		}
		Log.d(TAG, "Metadata: " + metadata.getMaxLatitude());
	}
	
	private NavigationService mBoundService;
	private Boolean mIsBound;

	private ServiceConnection mConnection = new ServiceConnection() {
	    public void onServiceConnected(ComponentName className, IBinder service) {
	        // This is called when the connection with the service has been
	        // established, giving us the service object we can use to
	        // interact with the service.  Because we have bound to a explicit
	        // service that we know is running in our own process, we can
	        // cast its IBinder to a concrete class and directly access it.
	        mBoundService = ((NavigationService.LocalBinder)service).getService();

	        // Tell the user about this for our demo.
	        Toast.makeText(MainActivity.this, "Connected service!",
	                Toast.LENGTH_SHORT).show();
	    }

	    public void onServiceDisconnected(ComponentName className) {
	        // This is called when the connection with the service has been
	        // unexpectedly disconnected -- that is, its process crashed.
	        // Because it is running in our same process, we should never
	        // see this happen.
	        mBoundService = null;
	        Toast.makeText(MainActivity.this, "Disconnected service!",
	                Toast.LENGTH_SHORT).show();
	    }
	};

	void doBindService() {
	    // Establish a connection with the service.  We use an explicit
	    // class name because we want a specific service implementation that
	    // we know will be running in our own process (and thus won't be
	    // supporting component replacement by other applications).
	    bindService(new Intent(MainActivity.this, 
	            NavigationService.class), mConnection, Context.BIND_AUTO_CREATE);
	    mIsBound = true;
	}

	void doUnbindService() {
	    if (mIsBound) {
	        // Detach our existing connection.
	        unbindService(mConnection);
	        mIsBound = false;
	    }
	}


	@Override
	protected void onResume() {
		super.onResume();
		mlo.enableMyLocation();
		// mlo.enableFollowLocation();
		mlo.enableCompass();
		doBindService();
	}

	@Override
	protected void onPause() {
		super.onPause();
		mlo.disableMyLocation();
		// mlo.disableFollowLocation();
		mlo.disableCompass();
		doUnbindService();
	}
	
	@Override
	protected void onDestroy(){
		super.onDestroy();
		//Intent svc = new Intent(this, NavigationService.class);
        //stopService(svc);
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu pMenu) {
		mGPS = pMenu.add(0, GPS_ID, Menu.NONE, R.string.enable_gps);
		mSettings = pMenu.add(0, SETTINGS_ID, Menu.NONE, R.string.settings);
		mExit = pMenu.add(0, EXIT_ID, Menu.NONE, R.string.exit);
		mGPS.setIcon(android.R.drawable.ic_menu_compass);
		mSettings.setIcon(android.R.drawable.ic_menu_preferences);
		mExit.setIcon(android.R.drawable.ic_menu_close_clear_cancel);
		return true;
	}

	public boolean onPrepareOptionsMenu(final Menu pMenu) {
		if (isGPSEnabled()) {
			mGPS.setTitle(R.string.disable_gps);
		} else {
			mGPS.setTitle(R.string.enable_gps);
		}
		return super.onPrepareOptionsMenu(pMenu);
	}

	public boolean onOptionsItemSelected(final MenuItem pItem) {
		if (pItem == mExit) {
			this.finish();
			return true;
		} else if (pItem == mGPS) {
			Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
			startActivity(myIntent);
		} else if (pItem == mSettings) {
			//TODO - settings activity
			startActivity(new Intent(this, SettingsActivity.class));
		}

		return false;
	}

	/*
	 * Get the last location the phone was at Based off example at
	 * http://www.androidsnippets
	 * .com/get-the-phones-last-known-location-using-locationmanager
	 */
	private GeoPoint getLastLocation() {
		LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		List<String> providers = lm.getProviders(true);
		Location l = null;

		for (int i = providers.size() - 1; i >= 0; i--) {
			l = lm.getLastKnownLocation(providers.get(i));
			if (l != null)
				break;
		}

		if (l == null) {
			return new GeoPoint(0, 0);
		}

		return new GeoPoint(l);
	}

	private Boolean isGPSEnabled() {
		return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
	}

	class MapOverlay extends org.osmdroid.views.overlay.Overlay {

		public GeoPoint p;

		public MapOverlay(Context ctx) {
			super(ctx);
			// TODO Auto-generated constructor stub
		}

		public MapOverlay(MainActivity mainActivity, GeoPoint lastLocation) {
			super(mainActivity);
			p = lastLocation;
		}

		@Override
		protected void draw(Canvas c, MapView osmv, boolean shadow) {
			// super.draw(c, osmv, shadow);

			Point screenPts = new Point();
			osmv.getProjection().toPixels(p, screenPts);

			Bitmap bmp = BitmapFactory.decodeResource(getResources(),
					R.drawable.arrow);
			c.drawBitmap(bmp, screenPts.x, screenPts.y, null);
			return;
		}

	
		
		@Override
		public boolean onLongPress(MotionEvent e, MapView mv) {
			Log.d(TAG, "LONG PRESS!");
			return true;
		}
	}

	class MapItemizedOverlay extends org.osmdroid.views.overlay.ItemizedOverlay<OverlayItem> {

		private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
		private Context mContext;
		
		public MapItemizedOverlay(Drawable defaultMarker,
				ResourceProxy resourceProxy) {
			super(defaultMarker, resourceProxy);
		}
		
		public MapItemizedOverlay(Drawable defaultMarker, ResourceProxy resourceProxy, Context context) {
			  super(defaultMarker, resourceProxy);
			  mContext = context;
		}

		@Override
		protected OverlayItem createItem(int i) {
		  return mOverlays.get(i);
		}

		@Override
		public int size() {
		  return mOverlays.size();
		}

		@Override
		public boolean onSnapToItem(int arg0, int arg1, Point arg2, MapView arg3) {
			// TODO Auto-generated method stub
			Log.d(TAG, "SnapToItem!");
			return false;
		}
		
		public void addOverlayItem(OverlayItem overlay) {
		    mOverlays.add(overlay);
		    populate();
		}
		
		protected boolean onTap(int index) {
		  OverlayItem item = mOverlays.get(index);
		  AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
		  dialog.setTitle(item.getTitle());
		  dialog.setMessage(item.getSnippet());
		  dialog.show();
		  return true;
		}
		
		@Override
		public boolean onLongPress(MotionEvent e, MapView mv) {
			Log.d(TAG, "LONG PRESS! " + e.getX() + " " + e.getY());
			
			final CharSequence[] items = {"Start Location", "End Location"};

			AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
			builder.setTitle("Choose Location Type");
			builder.setItems(items, new DialogInterface.OnClickListener() {
			    public void onClick(DialogInterface dialog, int item) {
			        Toast.makeText(getApplicationContext(), items[item], Toast.LENGTH_SHORT).show();
			    }
			});
			AlertDialog alert = builder.create();
			alert.show();
			
			GeoPoint point = mv.getProjection().fromPixels(e.getX(),
					e.getY());//new GeoPoint((double)e.getX(), (double)e.getY());
			OverlayItem overlayitem = new OverlayItem("Title!", "Description!",
			point);
			itemizedoverlay.addOverlayItem(overlayitem);
			mv.invalidate();
			
			//drawAt(new Canvas(), getResources().getDrawable(R.drawable.icon), (int)e.getX(), (int)e.getY(), true);
			return true;
		}
	}

	private GeoPoint getPoint(double lat, double lon) {
		return (new GeoPoint((int) (lat * 1000000.0), (int) (lon * 1000000.0)));
	}
	
	private class SitesOverlay extends ItemizedOverlayWithFocus<OverlayItem> {
		private List<OverlayItem> items=new ArrayList<OverlayItem>();
		private Drawable marker=null;
		private OverlayItem inDrag=null;
		private ImageView dragImage=null;
		private int xDragImageOffset=0;
		private int yDragImageOffset=0;
		private int xDragTouchOffset=0;
		private int yDragTouchOffset=0;
		private Point t = new Point(0, 0);
		private Point p = new Point(0, 0);
		
		public SitesOverlay(Drawable marker, List<OverlayItem> i) {
		super(i, marker, marker, Color.BLACK, null, new DefaultResourceProxyImpl(getApplicationContext()));
		this.marker=marker;

		dragImage=(ImageView)findViewById(R.id.drag);
		dragImage.setImageDrawable(getResources().getDrawable(R.drawable.start));
		
		xDragImageOffset=dragImage.getDrawable().getIntrinsicWidth()/2;
		yDragImageOffset=dragImage.getDrawable().getIntrinsicHeight();

		items.add(new OverlayItem("UN", "United Nations", getPoint(40.748963847316034,
				-73.96807193756104)));
		items.add(new OverlayItem("Lincoln Center",
		"Home of Jazz at Lincoln Center", getPoint(40.76866299974387,
				-73.98268461227417)));
		items.add(new OverlayItem("Carnegie Hall",
		"Where you go with practice, practice, practice", getPoint(40.765136435316755,
				-73.97989511489868)));
		items.add(new OverlayItem("The Downtown Club",
		"Original home of the Heisman Trophy", getPoint(40.70686417491799,
				-74.01572942733765)));

		populate();
		}

		@Override
		protected OverlayItem createItem(int i) {
		return(items.get(i));
		}

		public void addOverlayItem(OverlayItem overlay) {
		    items.add(overlay);
		    populate();
		}
		
		@Override
		public void draw(Canvas canvas, MapView mapView,
		boolean shadow) {
		super.draw(canvas, mapView, shadow);
		
		//boundCenterBottom(marker);
		}
		 
		@Override
		public int size() {
			if(items == null) {
				return 0;
			}
		return(items.size());
		}

		@Override
		public boolean onTouchEvent(MotionEvent event, MapView mapView) {
			final int action = event.getAction();
			final int x = (int) event.getX();
			final int y = (int) event.getY();
			final Projection pj = mapView.getProjection();
			
			boolean result = false;

			if (action == MotionEvent.ACTION_DOWN) {
				for (OverlayItem item : items) {

					pj.fromMapPixels(x, y, t);
					pj.toPixels(item.getPoint(), p);

                    if (hitTest(item, marker, t.x - p.x, t.y - p.y)) {
						result = true;
						inDrag = item;
						items.remove(inDrag);
						populate();

						xDragTouchOffset = 0;
						yDragTouchOffset = 0;

						setDragImagePosition(x, y);
						dragImage.setVisibility(View.VISIBLE);

						xDragTouchOffset = t.x - p.x;
						yDragTouchOffset = t.y - p.y;

						break;
					}
				}
			} else if (action == MotionEvent.ACTION_MOVE && inDrag != null) {
				dragImage.setVisibility(View.VISIBLE);
				setDragImagePosition(x, y);
				result = true;
			} else if (action == MotionEvent.ACTION_UP && inDrag != null) {
				dragImage.setVisibility(View.GONE);

				GeoPoint pt = pj.fromPixels(x - xDragTouchOffset, y - yDragTouchOffset);
				OverlayItem toDrop = new OverlayItem(inDrag.getTitle(),
						inDrag.getSnippet(), pt);

				items.add(toDrop);
				populate();
				inDrag = null;
				result = true;
				
				pj.fromMapPixels(x, y, t);
				
				if((t.x - p.x) == xDragTouchOffset && (t.y - p.y) == yDragTouchOffset){
					Log.d(TAG, "Do something here if desired because we didn't move item " + toDrop.getTitle());
				}
			}

			return (result || super.onTouchEvent(event, mapView));
		}
		
		private void setDragImagePosition(int x, int y) {
		RelativeLayout.LayoutParams lp=
		(RelativeLayout.LayoutParams)dragImage.getLayoutParams();

		lp.setMargins(x-xDragImageOffset-xDragTouchOffset,
		y-yDragImageOffset-yDragTouchOffset, 0, 0);
		dragImage.setLayoutParams(lp);
		}

		@Override
		public boolean onSnapToItem(int arg0, int arg1, Point arg2, MapView arg3) {
			// TODO Auto-generated method stub
			return false;
		}
		
		@Override
		public boolean onLongPress(MotionEvent e, MapView mv) {
			Log.d(TAG, "LONG PRESS! " + e.getX() + " " + e.getY());
			
			final CharSequence[] items = {"Start Location", "End Location"};

			AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
			builder.setTitle("Choose Location Type");
			builder.setItems(items, new DialogInterface.OnClickListener() {
			    public void onClick(DialogInterface dialog, int item) {
			        Toast.makeText(getApplicationContext(), items[item], Toast.LENGTH_SHORT).show();
			    }
			});
			AlertDialog alert = builder.create();
			alert.show();
			
			GeoPoint point = mv.getProjection().fromPixels(e.getX(),
					e.getY());//new GeoPoint((double)e.getX(), (double)e.getY());
			OverlayItem overlayitem = new OverlayItem("Title!", "Description!",
			point);
			//itemizedoverlay.addOverlayItem(overlayitem);
			
			addOverlayItem(overlayitem);
			mv.invalidate();
			
			//drawAt(new Canvas(), getResources().getDrawable(R.drawable.icon), (int)e.getX(), (int)e.getY(), true);
			return true;
		}
	}	
}