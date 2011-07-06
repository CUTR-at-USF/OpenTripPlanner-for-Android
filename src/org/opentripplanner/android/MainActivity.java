package org.opentripplanner.android;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.miscwidgets.widget.Panel;
import org.opentripplanner.android.contacts.ContactAPI;
import org.opentripplanner.android.contacts.ContactList;
import org.opentripplanner.api.model.EncodedPolylineBean;
import org.opentripplanner.api.model.Leg;
import org.opentripplanner.api.model.TripPlan;
import org.opentripplanner.api.ws.Request;
import org.opentripplanner.api.ws.Response;
import org.opentripplanner.routing.core.OptimizeType;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.MapView.Projection;
import org.osmdroid.views.overlay.MyLocationOverlay;
import org.osmdroid.views.overlay.PathOverlay;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.provider.Contacts.People;
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TextView.OnEditorActionListener;
import de.mastacode.http.Http;

public class MainActivity extends Activity implements OnSharedPreferenceChangeListener {

	private MapView mv;
	private MapController mc;
	private MyLocationOverlay mlo;
	private MenuItem mGPS;
	private MenuItem mMyLocation;
	private MenuItem mSettings;
	private MenuItem mExit;
	
	private ImageButton btnStartLocation;
	private ImageButton btnEndLocation;
	private Button btnPlanTrip;

	private EditText tbStartLocation;
	private EditText tbEndLocation;
	
	private Panel tripPanel;
	
	//MapItemizedOverlay itemizedoverlay;
	
	//OverlayItem startMarker;
	//OverlayItem endMarker;
	
	MapOverlay startMarker;
	MapOverlay endMarker;
	PathOverlay routeOverlay;
	
	private SharedPreferences prefs;
	
	private static LocationManager locationManager;
	private static final int EXIT_ID = 1;
	private static final int GPS_ID = 2;
	private static final int SETTINGS_ID = 3;
	private static final int MY_LOC_ID = 4;
	private static final int CHOOSE_CONTACT = 5;
	
	private static final String TAG = "OTP";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		prefs.registerOnSharedPreferenceChangeListener(this);

		locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

		btnStartLocation = (ImageButton) findViewById(R.id.btnStartLocation);
		btnEndLocation = (ImageButton) findViewById(R.id.btnEndLocation);
		tbStartLocation = (EditText) findViewById(R.id.tbStartLocation);
		tbEndLocation = (EditText) findViewById(R.id.tbEndLocation);
		btnPlanTrip = (Button) findViewById(R.id.btnPlanTrip);
		tripPanel = (Panel) findViewById(R.id.slidingDrawer1);
		
		tripPanel.setOpen(true, true);
		
		//Intent svc = new Intent(this, NavigationService.class);
		//startService(svc);
		
		GeoPoint currentLocation = getLastLocation();
		
		
		OnClickListener ocl = new OnClickListener() {
			@Override
			public void onClick(View v) {
				mBoundService.updateNotification();
				
				final int buttonID = v.getId();
				
				final CharSequence[] items = {"Current Location", "Contact Address", "Point on Map"};

				AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
				builder.setTitle("Choose Start Location");
				builder.setItems(items, new DialogInterface.OnClickListener() {
				    
					public void onClick(DialogInterface dialog, int item) {
				        Toast.makeText(getApplicationContext(), items[item], Toast.LENGTH_SHORT).show();
				        
				        if(items[item].equals("Current Location")) {
				        	if(buttonID == R.id.btnStartLocation) {
				        		tbStartLocation.setText("My Location");
				        		startMarker.setLocation(getLastLocation());
				        	} else if(buttonID == R.id.btnEndLocation) {
				        		tbEndLocation.setText("My Location");
				        		endMarker.setLocation(getLastLocation());
				        	}
				        } else if(items[item].equals("Contact Address")) {
				        	//TODO
//				        	Intent intent = new Intent(Intent.ACTION_PICK, People.CONTENT_URI);
//				        	startActivityForResult(intent, CHOOSE_CONTACT);
				        	
				        	ContactList cl = new ContactList();
				        	ContactAPI api = ContactAPI.getAPI();
				        	api.setCr(getContentResolver());
				        	api.setCur(managedQuery(People.CONTENT_URI, null, null, null, null));
				        	cl = api.newContactList();
				        	
				        	Intent intent = api.getContactIntent();
				        	startActivityForResult(intent, CHOOSE_CONTACT);
				        	
				        } else { //Point on Map
				        	if(buttonID == R.id.btnStartLocation) {
				        		tbStartLocation.setText(startMarker.getLocationFormatedString());
				        	} else if(buttonID == R.id.btnEndLocation) {
				        		tbEndLocation.setText(endMarker.getLocationFormatedString());
				        	}
				        }
				    }
				});
				AlertDialog alert = builder.create();
				alert.show();
			}
		};
		
		btnStartLocation.setOnClickListener(ocl);
		btnEndLocation.setOnClickListener(ocl);
		
		tbStartLocation.setImeOptions(EditorInfo.IME_ACTION_NEXT);
		tbEndLocation.setImeOptions(EditorInfo.IME_ACTION_DONE);
		tbEndLocation.requestFocus();
		tbEndLocation.setOnEditorActionListener(new OnEditorActionListener() {
			
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if(actionId == EditorInfo.IME_ACTION_DONE || event.getAction() == KeyEvent.ACTION_DOWN && 
						event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
					Log.d(TAG, "Finished inputting - plan trip now!");
					//TODO = call to plan trip button here?
				}
				return false;
			}
		});
		
		mv = (MapView) findViewById(R.id.mapview);
		mv.setBuiltInZoomControls(true);
		mv.setMultiTouchControls(true);

		mc = mv.getController();
		mc.setZoom(12);
		mc.setCenter(currentLocation);
		
		//Handle rotations                  final Object[] data = (Object[]) getLastNonConfigurationInstance();                  if ((data != null) && ((Boolean) data[0])) {                          mOsmv.getController().setZoom(16);                          showStep();                  } 
		
		
		

		mlo = new MyLocationOverlay(this, mv);
		// mlo.enableCompass();
		mv.getOverlays().add(mlo);

		//itemizedoverlay = new MapItemizedOverlay(getResources().getDrawable(R.drawable.arrow), new DefaultResourceProxyImpl(this), this);
		
		//startMarker = new OverlayItem("Start", "Starting location", currentLocation);
		//endMarker = new OverlayItem("End", "Ending location", getPoint(28.062286, -82.417717));
		
		//itemizedoverlay.addOverlayItem(overlayitem);
		//mv.getOverlays().add(itemizedoverlay);

		//Drawable marker = getResources().getDrawable(R.drawable.start);
		//marker.setBounds(0, 0, marker.getIntrinsicWidth(), marker.getIntrinsicHeight());
		//SitesOverlay so = new SitesOverlay(marker);
		//so.setFocusItemsOnTap(true);
		//so.setFocusedItem(0);
		//so.addOverlayItem(startMarker);
		//so.addOverlayItem(endMarker);
		//mv.getOverlays().add(so);

		
		//Drawable marker1 = getResources().getDrawable(R.drawable.end);
		//marker1.setBounds(0, 0, marker1.getIntrinsicWidth(), marker1.getIntrinsicHeight());
		//SitesOverlay so1 = new SitesOverlay(marker1);
		//so.setFocusItemsOnTap(true);
		//so.setFocusedItem(0);
		//so1.addOverlayItem(endMarker);
		//so.addOverlayItem(endMarker);
		//mv.getOverlays().add(so1);
		
		startMarker = new MapOverlay(this, R.drawable.start);
		startMarker.setLocation(currentLocation);
		mv.getOverlays().add(startMarker);
		
		endMarker = new MapOverlay(this, R.drawable.end);
		//endMarker.setLocation(getPoint(28.062286, -82.417717));
		endMarker.setLocation(currentLocation);
		mv.getOverlays().add(endMarker);
		
		routeOverlay = new PathOverlay(Color.DKGRAY, this);
		Paint paint = new Paint();
        paint.setColor(Color.DKGRAY);
        paint.setStrokeWidth(5.0f);
        paint.setStyle(Paint.Style.STROKE); 
        paint.setAlpha(200);
        routeOverlay.setPaint(paint);
        mv.getOverlays().add(routeOverlay);
		
		//SitesOverlay so = new SitesOverlay(getResources().getDrawable(R.drawable.icon));
		//so.addOverlayItem(overlayitem);
		//mv.getOverlays().add(so);
		
		new ServerSelector(this).execute(currentLocation);
		
		
/*		String u = "http://go.cutr.usf.edu:8083/opentripplanner-api-webapp/ws/metadata";
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
		Log.d(TAG, "Metadata: " + metadata.getMaxLatitude());*/
		
		btnPlanTrip.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				Request request = new Request();
				//request.setFrom("28.066192823902,-82.416927819827");
				//request.setTo("28.064072155861,-82.41109133301");
				request.setFrom(URLEncoder.encode(startMarker.getLocationFormatedString()));
				request.setTo(URLEncoder.encode(endMarker.getLocationFormatedString()));
				request.setArriveBy(false);
				request.setOptimize(OptimizeType.QUICK);
				
				
				try{
					Double maxWalk = Double.parseDouble(prefs.getString("max_walking_distance", "7600"));
					request.setMaxWalkDistance(maxWalk);
				} catch (NumberFormatException ex) {
					request.setMaxWalkDistance(new Double("7600"));
				}
				
				request.setWheelchair(prefs.getBoolean("wheelchair_accessible", false));
				
				//request.setDateTime("06/07/2011", URLEncoder.encode("11:34 am"));
				request.setDateTime(DateFormat.format("MM/dd/yy", System.currentTimeMillis()).toString(), 
						DateFormat.format("hh:mmaa", System.currentTimeMillis()).toString());
				
				new TripRequest(MainActivity.this).execute(request);
				
				
				/*String u = "http://go.cutr.usf.edu:8083/opentripplanner-api-webapp/ws/plan?fromPlace=28.066192823902,-82.416927819827&toPlace=28.064072155861,-82.41109133301&arr=Depart&min=QUICK&maxWalkDistance=7600&mode=WALK&itinID=1&submit&date=06/07/2011&time=11:34%20am";
				
				HttpClient client = new DefaultHttpClient();
				String result = "";
				try {
					result = Http.get(u).use(client).header("Accept", "application/json").charset("UTF-8").followRedirects(true).asString();
					Log.d(TAG, "Result: " + result);
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				GsonBuilder gsonb = new GsonBuilder();
				//DateDeserializer ds = new DateDeserializer();
				//gsonb.registerTypeAdapter(Date.class, ds);
				Gson gson = gsonb.create();
				 
				JSONObject j;
				Response plan = null;
				 
				try
				{
				    j = new JSONObject(result);
				    plan = gson.fromJson(j.toString(), Response.class);
				}
				catch(Exception e)
				{
				    e.printStackTrace();
				}
				Log.d(TAG, "TripPlan: " + plan.getPlan().from.lat + " time: " + plan.getPlan().date);
				*/
			}
		});
	}
	
//	@Override
//	public void onActivityResult(int requestCode, int resultCode, Intent data) {
//		super.onActivityResult(requestCode, resultCode, data);
//		
//		switch(requestCode) {
//			case (CHOOSE_CONTACT):
//				if(resultCode == Activity.RESULT_OK) {
//					Uri contactData = data.getData();
//					Cursor c = managedQuery(contactData, null, null, null, null);
//					if(c.moveToFirst()) {
//						Log.d(TAG, "Contact: " + c.getString(c.getColumnIndexOrThrow(People.DISPLAY_NAME)));
//						
//						String id = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
//						
////						Cursor postals = getContentResolver().query(ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_URI, null, ContactsContract.CommonDataKinds.StructuredPostal.CONTACT_ID + 
////								" = " + contactId, null, null);
////						int postFormattedNdx = postals.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS);
////						int postTypeNdx = postals.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.TYPE);
////						int postStreetNdx = postals.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.STREET);
////						while (postals.moveToNext()) {
////							String postalData = postals.getString(postFormattedNdx);
////							postalCat = postalCat+ postalData+ ", [";
////							postalData = String.valueOf(postals.getInt(postTypeNdx));
////							postalCat = postalCat+ postalData+ "], ";
////							postalData = postals.getString(postStreetNdx);
////							postalCat = postalCat+ postalData+ " ";         
////						}
////						postals.close();
//						
//						String where= ContactsContract.Data.CONTACT_ID
//							+ " = "
//							+ id
//							+ " AND ContactsContract.Data.MIMETYPE = '"
//							+ ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE
//							+ "'";
//							String[] projection = new String[] {
//							StructuredPostal.STREET, StructuredPostal.CITY,
//							StructuredPostal.POSTCODE, StructuredPostal.STREET,
//							StructuredPostal.REGION, StructuredPostal.COUNTRY,};
//							
//
//							Cursor cur = getContentResolver().query(
//							ContactsContract.Data.CONTENT_URI, projection, where, null,
//							StructuredPostal.COUNTRY + " asc");
//							
//							if(cur.moveToFirst()) {
//								Log.d(TAG, "Address? " + cur.getString(0));
//							}
//
//					}
//				}
//				break;
//		}
//	}
	
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
		mMyLocation = pMenu.add(0, MY_LOC_ID, Menu.NONE, R.string.my_location);
		mSettings = pMenu.add(0, SETTINGS_ID, Menu.NONE, R.string.settings);
		mExit = pMenu.add(0, EXIT_ID, Menu.NONE, R.string.exit);
		mGPS.setIcon(android.R.drawable.ic_menu_compass);
		mMyLocation.setIcon(android.R.drawable.ic_menu_mylocation);
		mSettings.setIcon(android.R.drawable.ic_menu_manage);
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
		} else if(pItem == mMyLocation) {
			zoomToCurrentLocation();
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
	
	private void moveMarker(Boolean start, GeoPoint point) {
		if(start) {
			startMarker.setLocation(point);
			tbStartLocation.setText(startMarker.getLocationFormatedString());
		} else {
			endMarker.setLocation(point);
			tbEndLocation.setText(endMarker.getLocationFormatedString());
		}
	}

	private void zoomToCurrentLocation() {
		mc.animateTo(getLastLocation());
	}

	class MapOverlay extends org.osmdroid.views.overlay.Overlay {

		private GeoPoint location;
		private int markerID = R.drawable.start;
		
		private Drawable marker = getResources().getDrawable(R.drawable.start);
		private boolean inDrag = false;
		private ImageView dragImage = null;
		private int xDragImageOffset = 0;
		private int yDragImageOffset = 0;
		private int xDragTouchOffset = 0;
		private int yDragTouchOffset = 0;
		private Point t = new Point(0, 0);
		private Point p = new Point(0, 0);

		public MapOverlay(Context ctx, int markerID) {
			super(ctx);
			this.markerID = markerID;
			this.marker = getResources().getDrawable(markerID);
			
			dragImage=(ImageView)findViewById(R.id.drag);
			
			
			xDragImageOffset=dragImage.getDrawable().getIntrinsicWidth()/2;
			yDragImageOffset=dragImage.getDrawable().getIntrinsicHeight();
			
			marker.setBounds(0, 0, marker.getIntrinsicWidth(), marker.getIntrinsicHeight());
		}

		public MapOverlay(MainActivity mainActivity, GeoPoint lastLocation) {
			super(mainActivity);
			location = lastLocation;
		}

		@Override
		protected void draw(final Canvas c, final MapView osmv, final boolean shadow) {
			// super.draw(c, osmv, shadow);

			Point screenPts = new Point();
			osmv.getProjection().toPixels(location, screenPts);
			
			Bitmap bmp = BitmapFactory.decodeResource(getResources(), markerID);
			c.drawBitmap(bmp, screenPts.x, screenPts.y, null);
			return;
		}
	
		@Override
		public boolean onLongPress(final MotionEvent e, final MapView mv) {
			Log.d(TAG, "LONG PRESS!");
			
			final CharSequence[] items = {"Start Location", "End Location"};

			AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
			builder.setTitle("Choose Type for Point");
			builder.setItems(items, new DialogInterface.OnClickListener() {
			    public void onClick(DialogInterface dialog, int item) {
			        Toast.makeText(getApplicationContext(), items[item], Toast.LENGTH_SHORT).show();
			        GeoPoint point = mv.getProjection().fromPixels(e.getX(), e.getY());
			        if(items[item].equals("Start Location")) {
			        	moveMarker(true, point);
			        } else {
			        	moveMarker(false, point);
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

				GeoPoint pt = pj.fromPixels(x - xDragTouchOffset, y - yDragTouchOffset);
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
						tbStartLocation.setText(coords);
					} else {
						tbEndLocation.setText(coords);
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

/*	class MapItemizedOverlay extends org.osmdroid.views.overlay.ItemizedOverlay<OverlayItem> {

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
	}*/

	private GeoPoint getPoint(double lat, double lon) {
		return (new GeoPoint((int) (lat * 1000000.0), (int) (lon * 1000000.0)));
	}
	
/*	private class SitesOverlay extends ItemizedOverlay<OverlayItem> {
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
		
		public SitesOverlay(Drawable marker) {
			super(marker, new DefaultResourceProxyImpl(getApplicationContext()));
		//super(i, marker, marker, Color.BLACK, null, new DefaultResourceProxyImpl(getApplicationContext()));
		this.marker=marker;

		dragImage=(ImageView)findViewById(R.id.drag);
		//dragImage.setImageDrawable(getResources().getDrawable(R.drawable.start));
		
		dragImage.setImageDrawable(marker);
		
		xDragImageOffset=dragImage.getDrawable().getIntrinsicWidth()/2;
		yDragImageOffset=dragImage.getDrawable().getIntrinsicHeight();

//		items.add(new OverlayItem("UN", "United Nations", getPoint(40.748963847316034,
//				-73.96807193756104)));
//		items.add(new OverlayItem("Lincoln Center",
//		"Home of Jazz at Lincoln Center", getPoint(40.76866299974387,
//				-73.98268461227417)));
//		items.add(new OverlayItem("Carnegie Hall",
//		"Where you go with practice, practice, practice", getPoint(40.765136435316755,
//				-73.97989511489868)));
//		items.add(new OverlayItem("The Downtown Club",
//		"Original home of the Heisman Trophy", getPoint(40.70686417491799,
//				-74.01572942733765)));
//
//		populate();
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
					tbEndLocation.setText(new StringBuilder().append(toDrop.getPoint().getLatitudeE6() / 1E6).append(", ")
			                .append(toDrop.getPoint().getLongitudeE6() / 1E6));
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
	}	*/
	
@Override
public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
		String key) {
	if(key == null) {
		return;
	}
	Log.v(TAG, "A preference was changed: " + key );
	if (key.equals("map_tile_source")) {
		mv.setTileSource(TileSourceFactory.getTileSource(prefs.getString("map_tile_source", "Mapnik")));
	}
	
}
	
}