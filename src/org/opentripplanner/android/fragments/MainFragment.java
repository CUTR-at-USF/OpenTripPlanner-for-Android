package org.opentripplanner.android.fragments;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpStatus;
import org.apache.http.impl.client.DefaultHttpClient;
import org.miscwidgets.widget.Panel;
import org.opentripplanner.android.MyActivity;
import org.opentripplanner.android.OTPApp;
import org.opentripplanner.android.OnFragmentListener;
import org.opentripplanner.android.R;
import org.opentripplanner.android.contacts.ContactAPI;
import org.opentripplanner.android.contacts.ContactList;
import org.opentripplanner.android.model.OTPBundle;
import org.opentripplanner.android.model.OTPPathOverlay;
import org.opentripplanner.android.model.OptimizeSpinnerItem;
import org.opentripplanner.android.model.Server;
import org.opentripplanner.android.model.TraverseModeSpinnerItem;
import org.opentripplanner.android.tasks.ServerSelector;
import org.opentripplanner.android.tasks.TripRequest;
import org.opentripplanner.android.util.ItineraryDecrypt;
import org.opentripplanner.api.model.EncodedPolylineBean;
import org.opentripplanner.api.model.Itinerary;
import org.opentripplanner.api.model.Leg;
import org.opentripplanner.api.ws.Request;
import org.opentripplanner.routing.core.OptimizeType;
import org.opentripplanner.routing.core.TraverseMode;
import org.opentripplanner.routing.core.TraverseModeSet;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.MapView.Projection;
import org.osmdroid.views.overlay.MyLocationOverlay;

import de.mastacode.http.Http;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.provider.Contacts.People;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TextView.OnEditorActionListener;

public class MainFragment extends Fragment implements OnSharedPreferenceChangeListener{
	
	private MapView mv;
	private MapController mc;
	private MyLocationOverlay mlo;
	private MenuItem mGPS;

	private EditText tbStartLocation;
	private EditText tbEndLocation;
	private ImageButton btnStartLocation;
	private ImageButton btnEndLocation;
	private Spinner ddlOptimization;
	private Spinner ddlTravelMode;
	private Button btnPlanTrip;

	private Spinner ddlGeocoder;

	private Panel tripPanel;
	Panel directionPanel;
	
	private ImageButton btnDisplayDirection;

	MapOverlay startMarker;
	MapOverlay endMarker;
	OTPPathOverlay routeOverlay;

	private SharedPreferences prefs;
	private OTPApp app;
	private static LocationManager locationManager;
	
//	private List<Itinerary> itineraries = null;
	
	ArrayList<String> directionText = new ArrayList<String>();

	private Boolean needToRunAutoDetect = false;
	
	private OnFragmentListener fragmentListener;

	private static final int CHOOSE_CONTACT = 1;

	/**
	 * Amount of time that a location is considered valid for that we will still use it as a starting location and snap the map to this location
	 */
	private static final int STALE_LOCATION_THRESHOLD = 60 * 60 * 1000;  //60 minutes

	private static final String TAG = "OTP";
	
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
        	setFragmentListener((OnFragmentListener) activity);
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnFragmentListener");
        }
    }
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
//	    setRetainInstance(true);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		View mainView = inflater.inflate(R.layout.main, container, false); 
		
		final Activity activity = this.getActivity();
		
		prefs = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
		prefs.registerOnSharedPreferenceChangeListener(this);

		app = ((OTPApp) activity.getApplication());

		locationManager = (LocationManager) activity.getSystemService(activity.LOCATION_SERVICE);

		btnStartLocation = (ImageButton) mainView.findViewById(R.id.btnStartLocation);
		btnEndLocation = (ImageButton) mainView.findViewById(R.id.btnEndLocation);
		tbStartLocation = (EditText) mainView.findViewById(R.id.tbStartLocation);
		tbEndLocation = (EditText) mainView.findViewById(R.id.tbEndLocation);
		btnPlanTrip = (Button) mainView.findViewById(R.id.btnPlanTrip);
		tripPanel = (Panel) mainView.findViewById(R.id.slidingDrawer1);
//		directionPanel = (Panel) mainView.findViewById(R.id.rightPanel3);
		ddlOptimization = (Spinner) mainView.findViewById(R.id.spinOptimization);
		ddlTravelMode = (Spinner) mainView.findViewById(R.id.spinTravelMode);
		
		btnDisplayDirection = (ImageButton) mainView.findViewById(R.id.btnDisplayDirection);

		tripPanel.setOpen(true, true);

		ArrayAdapter optimizationAdapter = new ArrayAdapter(activity, android.R.layout.simple_spinner_item, new OptimizeSpinnerItem[] {
				new OptimizeSpinnerItem("Quickest", OptimizeType.QUICK),
				new OptimizeSpinnerItem("Safest", OptimizeType.SAFE),
				new OptimizeSpinnerItem("Fewest Transfers", OptimizeType.TRANSFERS)
		});

		optimizationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		ddlOptimization.setAdapter(optimizationAdapter);

		ArrayAdapter traverseModeAdapter = new ArrayAdapter(activity, android.R.layout.simple_spinner_item, new TraverseModeSpinnerItem[] {
				new TraverseModeSpinnerItem("Transit", new TraverseModeSet(TraverseMode.TRANSIT, TraverseMode.WALK)),
				new TraverseModeSpinnerItem("Bus Only", new TraverseModeSet(TraverseMode.BUSISH, TraverseMode.WALK)),
				new TraverseModeSpinnerItem("Train Only", new TraverseModeSet(TraverseMode.TRAINISH, TraverseMode.WALK)), //not sure
				new TraverseModeSpinnerItem("Walk Only", new TraverseModeSet(TraverseMode.WALK)),
				new TraverseModeSpinnerItem("Bicycle", new TraverseModeSet(TraverseMode.BICYCLE)),
				new TraverseModeSpinnerItem("Transit and Bicycle", new TraverseModeSet(TraverseMode.TRANSIT, TraverseMode.BICYCLE))
		});

		traverseModeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		ddlTravelMode.setAdapter(traverseModeAdapter);

		//Intent svc = new Intent(this, NavigationService.class);
		//startService(svc);

		GeoPoint currentLocation = getLastLocation();

		//TODO set a proper currentLocation when getLastLocation() return null 
		if(currentLocation==null){
			currentLocation = new GeoPoint(28.065467, -82.419004);
		}

		OnClickListener ocl = new OnClickListener() {
			@Override
			public void onClick(View v) {
				//mBoundService.updateNotification();

				final int buttonID = v.getId();

				final CharSequence[] items = {"Current Location", "Contact Address", "Point on Map"};

				AlertDialog.Builder builder = new AlertDialog.Builder(activity);
				builder.setTitle("Choose Start Location");
				builder.setItems(items, new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int item) {
						if(items[item].equals("Current Location")) {
							GeoPoint p = getLastLocation();

							if(buttonID == R.id.btnStartLocation) {
								tbStartLocation.setText("My Location");

								if(p != null){
									startMarker.setLocation(p);
								}
							} else if(buttonID == R.id.btnEndLocation) {
								tbEndLocation.setText("My Location");
								if(p != null){
									endMarker.setLocation(p);
								}
							}
						} else if(items[item].equals("Contact Address")) {
							//TODO - fix contacts selector

							ContactList cl = new ContactList();
							ContactAPI api = ContactAPI.getAPI();
							api.setCr(activity.getContentResolver());
							api.setCur(activity.managedQuery(People.CONTENT_URI, null, null, null, null));
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
	    OnEditorActionListener tbLocationOnEditorActionListener = new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if(v.getId()==R.id.tbStartLocation && 
						actionId == EditorInfo.IME_ACTION_NEXT || 
						(event!=null && 
						event.getAction() == KeyEvent.ACTION_DOWN && 
						event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
					processAddress(true, v.getText().toString());
				} else if(v.getId()==R.id.tbEndLocation && 
						actionId == EditorInfo.IME_ACTION_DONE || 
						(event!=null &&
						event.getAction() == KeyEvent.ACTION_DOWN && 
						event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
					processAddress(false, v.getText().toString());
					//Log.d(TAG, "Finished inputting - plan trip now!");
					//TODO - call to plan trip button here?
				}
				return false;
			}
		};
		
		tbEndLocation.setOnEditorActionListener(tbLocationOnEditorActionListener);

		OnFocusChangeListener tbLocationOnFocusChangeListener = new OnFocusChangeListener(){
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				// TODO Auto-generated method stub
				TextView tv = (TextView) v;
				if(!hasFocus){
					if(v.getId()==R.id.tbStartLocation) {
						processAddress(true, tv.getText().toString());
					} else if(v.getId()==R.id.tbEndLocation){
						processAddress(false, tv.getText().toString());
					}
				}
			}
		};
		tbStartLocation.setOnFocusChangeListener(tbLocationOnFocusChangeListener);
		tbEndLocation.setOnFocusChangeListener(tbLocationOnFocusChangeListener);

		mv = (MapView) mainView.findViewById(R.id.mapview);
		mv.setBuiltInZoomControls(true);
		mv.setMultiTouchControls(true);

		mc = mv.getController();
		mc.setZoom(12);

		if(currentLocation != null){
			mc.setCenter(currentLocation);
		}

		//Handle rotations                  final Object[] data = (Object[]) getLastNonConfigurationInstance();                  if ((data != null) && ((Boolean) data[0])) {                          mOsmv.getController().setZoom(16);                          showStep();                  } 

		mlo = new MyLocationOverlay(activity, mv);
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
		
		startMarker = new MapOverlay(this, R.drawable.start, mainView);
		startMarker.setLocation(currentLocation);
		//		startMarker.setLocation(getPoint(28.066531327775138,-82.40525321904555));
		//		startMarker.setLocation(getPoint(35.151354,33.353805));
		mv.getOverlays().add(startMarker);

		endMarker = new MapOverlay(this, R.drawable.end, mainView);
		endMarker.setLocation(currentLocation);
		//		endMarker.setLocation(getPoint(28.0576685,-82.4198807));
		//		endMarker.setLocation(getPoint(35.168756, 33.372688));
		mv.getOverlays().add(endMarker);

		//		routeOverlay = new PathOverlay(Color.DKGRAY, this);
		//		Paint paint = new Paint();
		//        paint.setColor(Color.GREEN);
		//        paint.setStrokeWidth(5.0f);
		//        paint.setStyle(Paint.Style.STROKE); 
		//        paint.setAlpha(200);
		//        routeOverlay.setPaint(paint);
		//		mv.getOverlays().add(routeOverlay);

		routeOverlay = new OTPPathOverlay(Color.DKGRAY, activity);
		mv.getOverlays().add(routeOverlay);

		//TODO - fix below?
		if (prefs.getBoolean("auto_detect_server", true)) {
			if (app.getSelectedServer() == null) {
				new ServerSelector((MyActivity)activity).execute(currentLocation);
			} else {
				Log.v(TAG, "Already selected a server!!");
			}
		} else {
			String baseURL = prefs.getString("custom_server_url", "");
			if(baseURL.length() > 5) {
				app.setSelectedServer(new Server(baseURL), (MyActivity)activity);
				Log.v(TAG, "Now using custom OTP server: " + baseURL);
			} else {
				//TODO - handle issue when field is cleared/blank
			}
		}

		btnPlanTrip.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				Request request = new Request();
				request.setFrom(URLEncoder.encode(startMarker.getLocationFormatedString()));
				request.setTo(URLEncoder.encode(endMarker.getLocationFormatedString()));
				request.setArriveBy(false);

				request.setOptimize(((OptimizeSpinnerItem)ddlOptimization.getSelectedItem()).getOptimizeType());
				request.setModes(((TraverseModeSpinnerItem)ddlTravelMode.getSelectedItem()).getTraverseModeSet());

				try{
					Double maxWalk = Double.parseDouble(prefs.getString("max_walking_distance", "7600"));
					request.setMaxWalkDistance(maxWalk);
				} catch (NumberFormatException ex) {
					request.setMaxWalkDistance(new Double("7600"));
				}

				request.setWheelchair(prefs.getBoolean("wheelchair_accessible", false));

				request.setDateTime(DateFormat.format("MM/dd/yy", System.currentTimeMillis()).toString(), 
						DateFormat.format("hh:mmaa", System.currentTimeMillis()).toString());

				new TripRequest(MainFragment.this).execute(request);
			}
		});
		
		final OnFragmentListener ofl = this.getFragmentListener();
		
		OnClickListener oclDisplayDirection = new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				ofl.onDirectionFragmentSwitched();
			}
		};
		btnDisplayDirection.setOnClickListener(oclDisplayDirection);
		
//		Do NOT show direction icon if there is no direction yet
		if(ofl.getCurrentItinerary()==null){
			btnDisplayDirection.setVisibility(View.INVISIBLE);
		} else {
			btnDisplayDirection.setVisibility(View.VISIBLE);
		}
		
//		get previous state if already exist
		OTPBundle otpBundle = ofl.getOTPBundle();
		if(otpBundle!=null){
			retrievePreviousState(otpBundle);
		}
		
		Log.v(TAG, "finish onStart()");
		
		return mainView;
	}
	
	private void retrievePreviousState(OTPBundle bundle){
		tbStartLocation.setText(bundle.getFromText());
		tbEndLocation.setText(bundle.getToText());
		startMarker.setLocation(bundle.getStartLocation());
		endMarker.setLocation(bundle.getEndLocation());
		ddlOptimization.setSelection(bundle.getOptimization());
		ddlTravelMode.setSelection(bundle.getTravelMode());
		
		this.showRouteOnMap(bundle.getCurrentItinerary());
//		asd
	}
	
	private void saveOTPBundle(){
		OTPBundle bundle = new OTPBundle();
		bundle.setFromText(tbStartLocation.getText().toString());
		bundle.setToText(tbEndLocation.getText().toString());
		bundle.setStartLocation(startMarker.getLocation());
		bundle.setEndLocation(endMarker.getLocation());
		bundle.setOptimization(ddlOptimization.getSelectedItemPosition());
		bundle.setTravelMode(ddlTravelMode.getSelectedItemPosition());
		
		this.getFragmentListener().setOTPBundle(bundle);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		Log.v(TAG,"onActivityCreated");
		super.onActivityCreated(savedInstanceState);
	}
	
	private void processAddress(final boolean isStartTextBox, String address){
		AlertDialog.Builder geocoderAlert = new AlertDialog.Builder(this.getActivity());
		geocoderAlert.setTitle(R.string.geocoder_results_title)
					 .setMessage(R.string.geocoder_no_results_message)
					 .setCancelable(false)
					 .setPositiveButton("OK", new DialogInterface.OnClickListener() {
						 public void onClick(DialogInterface dialog, int id) {
						 }
					  });
		
		if(address==null || 
			address.equalsIgnoreCase("")) {
			AlertDialog alert = geocoderAlert.create();
			alert.show();
			return;
		}
		
		if(address.equalsIgnoreCase(getString(R.string.my_location))) {
			GeoPoint currentLocation = getLastLocation();
			if(currentLocation==null){
				geocoderAlert.setMessage(R.string.no_gps_signal);
				AlertDialog alert = geocoderAlert.create();
				alert.show();
				return;
			}
			
			moveMarker(isStartTextBox, currentLocation, getString(R.string.my_location));
			return;
		}
		
		Geocoder gc = new Geocoder(this.getActivity());
		ArrayList<Address> addresses = null;
		try {
			addresses = (ArrayList<Address>)gc.getFromLocationName(address, 
															R.integer.geocoder_max_results, 
															app.getLowerLeftLatitude(), 
															app.getLowerLeftLongitude(), 
															app.getUpperRightLatitude(), 
															app.getUpperRightLongitude());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if(addresses==null || addresses.isEmpty()){
			AlertDialog alert = geocoderAlert.create();
			alert.show();
			return;
		}
		
		final CharSequence[] addressesText = new CharSequence[addresses.size()];
		for(int i=0; i<addresses.size(); i++){
			Address addr = addresses.get(i);
			addressesText[i] =  addr.getAddressLine(0)+"\n"+
					((addr.getSubAdminArea()!=null) ? addr.getSubAdminArea()+", " : "")+
					((addr.getAdminArea()!=null) ? addr.getAdminArea()+" " : "")+
					((addr.getPostalCode()!=null) ? addr.getPostalCode()+" " : "")+
					((addr.getCountryName()!=null) ? addr.getCountryName() : "");
			Log.v("Test", addressesText[i].toString());
		}
		
		if(addresses.size()==1){
			Address addr = addresses.get(0);
			moveMarker(isStartTextBox, new GeoPoint(addr.getLatitude(), addr.getLongitude()), addressesText[0].toString());
			return;
		}

		AlertDialog.Builder geocoderSelector = new AlertDialog.Builder(this.getActivity());
		geocoderSelector.setTitle(R.string.choose_geocoder);
		
		final ArrayList<Address> addressesTemp = addresses;
		geocoderSelector.setItems(addressesText, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				Address addr = addressesTemp.get(item);
				moveMarker(isStartTextBox, new GeoPoint(addr.getLatitude(), addr.getLongitude()), addressesText[item].toString());
				Log.v(TAG, "Chosen: " + addressesText[item]);
			}
		});
		AlertDialog alertGeocoder = geocoderSelector.create();
		alertGeocoder.show();

	}

	

//	private NavigationService mBoundService;
//	private Boolean mIsBound;
//
//	private ServiceConnection mConnection = new ServiceConnection() {
//		public void onServiceConnected(ComponentName className, IBinder service) {
//			mBoundService = ((NavigationService.LocalBinder)service).getService();
//
//			Toast.makeText(getActivity(), "Connected service!",
//					Toast.LENGTH_SHORT).show();
//		}
//
//		public void onServiceDisconnected(ComponentName className) {
//			mBoundService = null;
//			Toast.makeText(getActivity(), "Disconnected service!",
//					Toast.LENGTH_SHORT).show();
//		}
//	};
//
//	void doBindService() {
//		bindService(new Intent(this.getActivity(), 
//				NavigationService.class), mConnection, Context.BIND_AUTO_CREATE);
//		mIsBound = true;
//	}
//
//	void doUnbindService() {
//		if (mIsBound) {
//			unbindService(mConnection);
//			mIsBound = false;
//		}
//	}


	@Override
	public void onResume() {
		super.onResume();
		mlo.enableMyLocation();
		// mlo.enableFollowLocation();
		mlo.enableCompass();
		//doBindService();

		if(needToRunAutoDetect) {
			GeoPoint currentLoc = getLastLocation();
			if(currentLoc != null){
				Log.v(TAG, "Relaunching auto detection for server");
				MyActivity ma = (MyActivity)this.getActivity();
				new ServerSelector(ma).execute(currentLoc);
			}
			needToRunAutoDetect = false;
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		mlo.disableMyLocation();
		// mlo.disableFollowLocation();
		mlo.disableCompass();
		//doUnbindService();
		
//		Save states before leaving
		this.saveOTPBundle();
	}

	@Override
	public void onDestroy(){
		super.onDestroy();
		//Intent svc = new Intent(this, NavigationService.class);
		//stopService(svc);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if(key == null) {
			return;
		}
		Log.v(TAG, "A preference was changed: " + key );
		if (key.equals("map_tile_source")) {
			mv.setTileSource(TileSourceFactory.getTileSource(prefs.getString("map_tile_source", "Mapnik")));
		} else if (key.equals("custom_server_url")) {
			String baseURL = prefs.getString("custom_server_url", "");
			if(baseURL.length() > 5) {
				app.setSelectedServer(new Server(baseURL), (MyActivity)this.getActivity());
				Log.v(TAG, "Now using custom OTP server: " + baseURL);
			} else {
				//TODO - handle issue when field is cleared/blank
			}
		} else if(key.equals("auto_detect_server")) {
			if (prefs.getBoolean("auto_detect_server", true)) {
				//TODO - fix this not displaying!!
				needToRunAutoDetect = true;
			}
		}
	}

	@Override
//	public boolean onCreateOptionsMenu(final Menu pMenu) {
//		MenuInflater inflater = getMenuInflater();
//		inflater.inflate(R.menu.menu, pMenu);
//		mGPS = pMenu.getItem(0);
//		return true;
//	}

	public void onPrepareOptionsMenu(final Menu pMenu) {
		if (isGPSEnabled()) {
			mGPS.setTitle(R.string.disable_gps);
		} else {
			mGPS.setTitle(R.string.enable_gps);
		}
		super.onPrepareOptionsMenu(pMenu);
	}

	public boolean onOptionsItemSelected(final MenuItem pItem) {
		switch (pItem.getItemId()) {
		case R.id.exit:
			this.getActivity().finish();
			return true;
		case R.id.gps_settings:
			Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
			startActivity(myIntent);
			break;
		case R.id.my_location:
			zoomToCurrentLocation();
			break;
		case R.id.settings:
//			startActivity(new Intent(this, SettingsActivity.class));
			break;
		case R.id.feedback:
			//TODO - feedback activity
			AlertDialog.Builder feedback = new AlertDialog.Builder(this.getActivity());
			feedback.setTitle("Not Yet Implemented");
			feedback.setMessage("The feedback feature is not yet implemented.");
			feedback.setNeutralButton("OK", null);
			feedback.create().show();
			break;
		case R.id.server_info:
			Server server = app.getSelectedServer();
			if(server == null){
				Log.w(TAG, "Tried to get server info when no server was selected");
				break;
			}
			StringBuilder message = new StringBuilder("Region:  " + server.getRegion());
			message.append("\nLanguage:  " + server.getLanguage());
			message.append("\nContact:  " + server.getContact() + " (" + server.getContactEmail() + ")");
			message.append("\nURL:  " + server.getBaseURL());

			//TODO - fix server info bounds
			//message.append("\nBounds: " + server.getBounds());

			message.append("\nCurrently reachable: ");

			int status = 0;
			try {
				status = Http.get(server.getBaseURL()).use(new DefaultHttpClient()).asResponse().getStatusLine().getStatusCode();
			} catch (IOException e) {
				Log.e(TAG, "Unable to reach server: " + e.getMessage());
			}

			if(status == HttpStatus.SC_OK) {
				message.append("Yes");
			} else {
				message.append("No");
			}


			AlertDialog.Builder dialog = new AlertDialog.Builder(this.getActivity());
			dialog.setTitle("OpenTripPlanner Server Info");
			dialog.setMessage(message);
			dialog.setNeutralButton("OK", null);
			dialog.create().show();
			break;
		default:
			break;
		}

		return false;
	}

	/*
	 * Get the last location the phone was at
	 *  Based off example at http://www.androidsnippets.com/get-the-phones-last-known-location-using-locationmanager
	 * 
	 * @return GeoPoint of last location, or null if a location hasn't been acquired in the last STALE_LOCATION_THRESHOLD amount of time
	 */
	private GeoPoint getLastLocation() {
		LocationManager lm = (LocationManager) this.getActivity().getSystemService(Context.LOCATION_SERVICE);
		List<String> providers = lm.getProviders(true);
		Location l = null;

		for (int i = providers.size() - 1; i >= 0; i--) {
			l = lm.getLastKnownLocation(providers.get(i));
			if (l != null && l.getProvider().equalsIgnoreCase(LocationManager.GPS_PROVIDER))  //Only break if we have a GPS fix location, since this will be the most accurate location provider.  We want to make sure we loop through all of them to find GPS if available
				break;
		}

		if (l == null  || (Math.abs((System.currentTimeMillis() - l.getTime())) > STALE_LOCATION_THRESHOLD)) {  //Check to make sure the location is recent (use ABS() to allow for small time sync differences between GPS clock and system clock)
			
			return null; //return null if no location was found in the last STALE_LOCATION_THRESHOLD amount of time
		}

		return new GeoPoint(l);
	}

	private Boolean isGPSEnabled() {
		return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
	}

	private void moveMarker(Boolean start, GeoPoint point, String text) {
		if(start) {
			startMarker.setLocation(point);
			if (text==null) {
				tbStartLocation.setText(startMarker.getLocationFormatedString());
			} else {
				tbStartLocation.setText(text);
			}
		} else {
			endMarker.setLocation(point);
			if (text==null) {
				tbEndLocation.setText(endMarker.getLocationFormatedString());
			} else {
				tbEndLocation.setText(text);
			}
		}
		
	}

	private void zoomToCurrentLocation() {
		GeoPoint p = getLastLocation();

		if(p !=null){
			mc.animateTo(p);
		}
	}

	private GeoPoint getPoint(double lat, double lon) {
		return (new GeoPoint((int) (lat * 1000000.0), (int) (lon * 1000000.0)));
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

		public MapOverlay(Context ctx, int markerID, View view) {
			super(ctx);
			setMarker(markerID, view);
		}

		public MapOverlay(MainFragment mainFragment, int markerID, View view) {
			super(mainFragment.getActivity());
			setMarker(markerID, view);
		}
		
		public void setMarker(int markerID, View view){
			this.markerID = markerID;
			this.marker = getResources().getDrawable(markerID);

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

			Bitmap bmp = BitmapFactory.decodeResource(getResources(), markerID);
			c.drawBitmap(bmp, screenPts.x, screenPts.y, null);
			return;
		}

		@Override
		public boolean onLongPress(final MotionEvent e, final MapView mv) {
			Log.d(TAG, "LONG PRESS!");

			final CharSequence[] items = {"Start Location", "End Location"};

			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle("Choose Type for Point");
			builder.setItems(items, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item) {
					Toast.makeText(getActivity().getApplicationContext(), items[item], Toast.LENGTH_SHORT).show();
					GeoPoint point = mv.getProjection().fromPixels(e.getX(), e.getY());
					if(items[item].equals("Start Location")) {
						moveMarker(true, point, null);
					} else {
						moveMarker(false, point, null);
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
	
	public void showRouteOnMap(List<Leg> itinerary){
		Log.v(TAG, "(TripRequest) legs size = "+Integer.toString(itinerary.size()));
		if (!itinerary.isEmpty()) {
			btnDisplayDirection.setVisibility(View.VISIBLE);
			routeOverlay.removeAllPath();
			int index = 0;
			for (Leg leg : itinerary) {
				int pathColor = getPathColor(leg.mode);
				routeOverlay.addPath(pathColor);
				List<GeoPoint> points = EncodedPolylineBean
						.decodePoly(leg.legGeometry.getPoints());
//				Log.v(TAG, "(TripRequest) points size = "+Integer.toString(points.size())
//							+ " mode = "+leg.mode+" agencyId = "+leg.agencyId);
				for (GeoPoint geoPoint : points) {
					routeOverlay.addPoint(index, geoPoint);
				}
				index++;
			}
		}
	}

	private int getPathColor(String mode){
		if(mode.equalsIgnoreCase("WALK")){
			return Color.DKGRAY;
		} else if(mode.equalsIgnoreCase("BUS")){
			return Color.RED;
		} else if(mode.equalsIgnoreCase("TRAIN")) {
			return Color.YELLOW;
		} else if(mode.equalsIgnoreCase("BICYCLE")){
			return Color.BLUE;
		}
		return Color.WHITE;
	}

	/**
	 * @return the fragmentListener
	 */
	public OnFragmentListener getFragmentListener() {
		return fragmentListener;
	}

	/**
	 * @param fragmentListener the fragmentListener to set
	 */
	public void setFragmentListener(OnFragmentListener fragmentListener) {
		this.fragmentListener = fragmentListener;
	}
}
