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

package edu.usf.cutr.opentripplanner.android.fragments;

import static edu.usf.cutr.opentripplanner.android.OTPApp.PREFERENCE_KEY_CUSTOM_SERVER_BOUNDS;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.miscwidgets.widget.Panel;
import org.opentripplanner.api.ws.GraphMetadata;
import org.opentripplanner.api.ws.Request;
import org.opentripplanner.routing.core.OptimizeType;
import org.opentripplanner.routing.core.TraverseMode;
import org.opentripplanner.routing.core.TraverseModeSet;
import org.opentripplanner.v092snapshot.api.model.Itinerary;
import org.opentripplanner.v092snapshot.api.model.Leg;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Color;
import android.location.Address;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;

import edu.usf.cutr.opentripplanner.android.MyActivity;
import edu.usf.cutr.opentripplanner.android.OTPApp;
import edu.usf.cutr.opentripplanner.android.R;
import edu.usf.cutr.opentripplanner.android.SettingsActivity;
import edu.usf.cutr.opentripplanner.android.listeners.MetadataRequestCompleteListener;
import edu.usf.cutr.opentripplanner.android.listeners.OTPGeocodingListener;
import edu.usf.cutr.opentripplanner.android.listeners.OnFragmentListener;
import edu.usf.cutr.opentripplanner.android.listeners.ServerSelectorCompleteListener;
import edu.usf.cutr.opentripplanner.android.listeners.TripRequestCompleteListener;
import edu.usf.cutr.opentripplanner.android.maps.MyUrlTileProvider;
import edu.usf.cutr.opentripplanner.android.model.OTPBundle;
import edu.usf.cutr.opentripplanner.android.model.OptimizeSpinnerItem;
import edu.usf.cutr.opentripplanner.android.model.Server;
import edu.usf.cutr.opentripplanner.android.model.TraverseModeSpinnerItem;
import edu.usf.cutr.opentripplanner.android.sqlite.ServersDataSource;
import edu.usf.cutr.opentripplanner.android.tasks.MetadataRequest;
import edu.usf.cutr.opentripplanner.android.tasks.OTPGeocoding;
import edu.usf.cutr.opentripplanner.android.tasks.ServerChecker;
import edu.usf.cutr.opentripplanner.android.tasks.ServerSelector;
import edu.usf.cutr.opentripplanner.android.tasks.TripRequest;
import edu.usf.cutr.opentripplanner.android.util.DateTimeConversion;
import edu.usf.cutr.opentripplanner.android.util.LocationUtil;
import edu.usf.cutr.opentripplanner.android.util.RightDrawableOnTouchListener;

/**
 * Main UI screen of the app, showing the map.
 * 
 * @author Khoa Tran
 */

public class MainFragment extends Fragment implements
		OnSharedPreferenceChangeListener, ServerSelectorCompleteListener,
		TripRequestCompleteListener, MetadataRequestCompleteListener,
		OTPGeocodingListener, LocationListener,
		GooglePlayServicesClient.OnConnectionFailedListener,
		GooglePlayServicesClient.ConnectionCallbacks,
		GoogleMap.OnCameraChangeListener{
	
	//private View mainView;

	private GoogleMap mMap;
	private TileOverlay actualTileOverlay;
	private MenuItem mGPS;
	
	private LocationClient mLocationClient;
	
    LocationRequest mLocationRequest;
	
	private List<Polyline> route;

	private EditText tbStartLocation;
	private EditText tbEndLocation;
	private ListView ddlOptimization;
	private ListView ddlTravelMode;
	private ImageButton btnPlanTrip;
	private LatLng savedLastLocation;
	
	private View panelDisplayDirection;
	
	private Spinner itinerarySelectionSpinner;
	
	private boolean appStarts = true;
	
	private boolean isStartLocationGeocodingProcessed = false;
	private boolean isEndLocationGeocodingProcessed = false;
	
	private boolean isStartLocationChangedByUser = true;
	private boolean isEndLocationChangedByUser = true;
	
	private Context applicationContext;


	public LatLng getSavedLastLocation() {
		return savedLastLocation;
	}

	Panel directionPanel;

	private ImageButton btnDisplayDirection;
	
	private ImageButton btnCompass;
	private ImageButton btnMyLocation;
	
	private ImageButton btnHandle;
	private DrawerLayout drawerLayout;

	Marker startMarker;
	LatLng startMarkerPosition;

	Marker endMarker;
	LatLng endMarkerPosition;
	
	ArrayList<Marker> modeMarkers;
	
	Polyline boundariesPolyline;

	private SharedPreferences prefs;
	private OTPApp app;
	private static LocationManager locationManager;

	ArrayList<String> directionText = new ArrayList<String>();

	private Boolean needToRunAutoDetect = true;
	
	public Boolean getNeedToRunAutoDetect() {
		return needToRunAutoDetect;
	}

	public void setNeedToRunAutoDetect(Boolean needToRunAutoDetect) {
		this.needToRunAutoDetect = needToRunAutoDetect;
	}

	private OnFragmentListener fragmentListener;

	private boolean isRealLostFocus = true;
	
	private boolean restoredSavedState = false;

	public static final String TAG = "OTP";
	
	private float bearing = 0;
	private float tilt = 0;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			setFragmentListener((OnFragmentListener) activity);
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnFragmentListener");
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View mainView = inflater.inflate(R.layout.main, container, false);
				
		tbStartLocation = (EditText) mainView
				.findViewById(R.id.tbStartLocation);
		tbEndLocation = (EditText) mainView.findViewById(R.id.tbEndLocation);
		
		btnPlanTrip = (ImageButton) mainView.findViewById(R.id.btnPlanTrip);
		ddlOptimization = (ListView) mainView
				.findViewById(R.id.spinOptimization);
		ddlTravelMode = (ListView) mainView.findViewById(R.id.spinTravelMode);
		
		btnCompass = (ImageButton) mainView.findViewById(R.id.btnCompass);
		btnMyLocation = (ImageButton) mainView.findViewById(R.id.btnMyLocation);
		
		btnDisplayDirection = (ImageButton) mainView
				.findViewById(R.id.btnDisplayDirection);
		
		panelDisplayDirection = (ViewGroup) mainView.findViewById(R.id.panelDisplayDirection);
		
		btnHandle = (ImageButton) mainView.findViewById(R.id.btnHandle);
		drawerLayout = (DrawerLayout) mainView.findViewById(R.id.drawerLayout);
		
		tbStartLocation.setImeOptions(EditorInfo.IME_ACTION_NEXT);
		tbEndLocation.setImeOptions(EditorInfo.IME_ACTION_DONE);
		tbEndLocation.requestFocus();
		
		itinerarySelectionSpinner = (Spinner) mainView.findViewById(R.id.itinerarySelection);

		Log.v(TAG, "finish onStart()");

		return mainView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		Log.v(TAG, "onActivityCreated");
		super.onActivityCreated(savedInstanceState);
		
		applicationContext = getActivity().getApplicationContext();
		 
		app = ((OTPApp) getActivity().getApplication());
		
		prefs = PreferenceManager.getDefaultSharedPreferences(
				applicationContext);
		prefs.registerOnSharedPreferenceChangeListener(this);
		
		locationManager = (LocationManager) getActivity()
				.getSystemService(Context.LOCATION_SERVICE);
		
		mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(OTPApp.UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(OTPApp.FASTEST_INTERVAL);
        		
        mMap = retrieveMap(mMap);
		UiSettings uiSettings = mMap.getUiSettings();
		mMap.setMyLocationEnabled(true);
		mMap.setOnCameraChangeListener(this);
		uiSettings.setMyLocationButtonEnabled(false);
		uiSettings.setCompassEnabled(false);
		uiSettings.setAllGesturesEnabled(true);
		uiSettings.setZoomControlsEnabled(false);

		
		String overlayString = prefs.getString(OTPApp.PREFERENCE_KEY_MAP_TILE_SOURCE, applicationContext.getResources().getString(R.string.map_tiles_default_server)); 
		updateOverlay(overlayString);
		
		if (savedInstanceState == null){
			SharedPreferences.Editor prefsEditor = prefs.edit();
			prefsEditor.putBoolean(OTPApp.PREFERENCE_KEY_ORIGIN_IS_MY_LOCATION, true);
			prefsEditor.putBoolean(OTPApp.PREFERENCE_KEY_DESTINATION_IS_MY_LOCATION, false);
			prefsEditor.commit();
		}
		
		if (prefs.getBoolean(OTPApp.PREFERENCE_KEY_SELECTED_CUSTOM_SERVER, false)){
			String baseURL = prefs.getString(OTPApp.PREFERENCE_KEY_CUSTOM_SERVER_URL, "");
			Server s = new Server(baseURL);
			String bounds;
			setSelectedServer(s);
			if ((bounds = prefs.getString(OTPApp.PREFERENCE_KEY_CUSTOM_SERVER_BOUNDS, null)) != null){
				s.setBounds(bounds);
				addBoundariesRectangle(s);
			}
			
			Log.v(TAG, "Now using custom OTP server: " + baseURL);
		}
		else{
			ServersDataSource dataSource = ServersDataSource.getInstance(applicationContext);
			long serverId = prefs.getLong(OTPApp.PREFERENCE_KEY_SELECTED_SERVER, 0);
			if (serverId != 0){
				dataSource.open();
				Server s = new Server(dataSource.getServer(prefs.getLong(OTPApp.PREFERENCE_KEY_SELECTED_SERVER, 0)));
				dataSource.close();
				
				setSelectedServer(s);
				addBoundariesRectangle(s);
				
				Log.v(TAG, "Now using OTP server: " + s.getRegion());
			}
		}
		
		ArrayAdapter<OptimizeSpinnerItem> optimizationAdapter = new ArrayAdapter<OptimizeSpinnerItem>(
				getActivity(),
				android.R.layout.simple_list_item_single_choice,
				new OptimizeSpinnerItem[] {
						new OptimizeSpinnerItem("Quickest", OptimizeType.QUICK),
						new OptimizeSpinnerItem("Safest", OptimizeType.SAFE),
						new OptimizeSpinnerItem("Fewest Transfers",
								OptimizeType.TRANSFERS) });
		ddlOptimization.setAdapter(optimizationAdapter);
		ddlOptimization.setItemChecked(0, true);

		ArrayAdapter<TraverseModeSpinnerItem> traverseModeAdapter = new ArrayAdapter<TraverseModeSpinnerItem>(
				getActivity(), android.R.layout.simple_list_item_single_choice,
				new TraverseModeSpinnerItem[] {
						new TraverseModeSpinnerItem("Transit",
								new TraverseModeSet(TraverseMode.TRANSIT,
										TraverseMode.WALK)),
						new TraverseModeSpinnerItem("Bus Only",
								new TraverseModeSet(TraverseMode.BUSISH,
										TraverseMode.WALK)),
						new TraverseModeSpinnerItem("Train Only",
								new TraverseModeSet(TraverseMode.TRAINISH,
										TraverseMode.WALK)), // not sure
						new TraverseModeSpinnerItem("Walk Only",
								new TraverseModeSet(TraverseMode.WALK)),
						new TraverseModeSpinnerItem("Bicycle",
								new TraverseModeSet(TraverseMode.BICYCLE)),
						new TraverseModeSpinnerItem("Transit and Bicycle",
								new TraverseModeSet(TraverseMode.TRANSIT,
										TraverseMode.BICYCLE)) });
		ddlTravelMode.setAdapter(traverseModeAdapter);	
		ddlTravelMode.setItemChecked(0, true);
		
		Server selectedServer = app.getSelectedServer();	
		if (selectedServer != null){
			LatLng serverCenter = new LatLng(selectedServer.getCenterLatitude(), selectedServer.getCenterLongitude());
			mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(serverCenter, OTPApp.defaultInitialZoomLevel));	
		}

		restoreState(savedInstanceState);
		
		addInterfaceListeners();
		
		addMapListeners();
	}
	
	
	private void addInterfaceListeners(){
		
		OnTouchListener otlStart = new RightDrawableOnTouchListener(tbStartLocation) {
			@Override
			public boolean onDrawableTouch(final MotionEvent event) {
				// mBoundService.updateNotification();

				final CharSequence[] items = { "Current Location",
						"Contact Address", "Point on Map" };

				AlertDialog.Builder builder = new AlertDialog.Builder(MainFragment.this.getActivity());
				builder.setTitle("Choose Start Location");
				builder.setItems(items, new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int item) {
						if (items[item].equals("Current Location")) {	
					/*		myActivity = (MyActivity) activity;
							myActivity.getmLocationClient();
							Location loc = this.MainFragment.getmLocationClient().getLastLocation();*/
							LatLng mCurrentLatLng = getLastLocation();
							if (mCurrentLatLng != null){
								SharedPreferences.Editor prefsEditor = prefs.edit();
								setTextBoxLocation("My Location", true);
								prefsEditor.putBoolean(OTPApp.PREFERENCE_KEY_ORIGIN_IS_MY_LOCATION, true);

								if (mCurrentLatLng != null) {
									if (startMarker != null){
										startMarker.remove();
										startMarker = null;
									}
								}

								prefsEditor.commit();
							}
							else{
								Toast.makeText(MainFragment.this.applicationContext, applicationContext.getResources().getString(R.string.location_error), Toast.LENGTH_LONG).show();
							}
							
							
						} else if (items[item].equals("Contact Address")) {
							Intent intent = new Intent(Intent.ACTION_PICK);
							intent.setType(ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_TYPE);
							((MyActivity)MainFragment.this.getActivity()).setButtonStartLocation(true);

							((MyActivity)MainFragment.this.getActivity()).startActivityForResult(intent,
									OTPApp.CHOOSE_CONTACT_REQUEST_CODE);

						} else { // Point on Map
							if (startMarker != null){
								updateMarkerPosition(startMarker.getPosition(), true);
							}
							else{
								setTextBoxLocation("", true);
								tbStartLocation.setHint(getResources().getString(R.string.need_to_place_marker));
								tbStartLocation.requestFocus();
							}
						}
					}
				});
				AlertDialog alert = builder.create();
				alert.show();
				return true;
			}

		};
		
		
		tbStartLocation.setOnTouchListener(otlStart);
		
		
		OnTouchListener otlEnd = new RightDrawableOnTouchListener(tbEndLocation) {
			@Override
			public boolean onDrawableTouch(final MotionEvent event) {
				// mBoundService.updateNotification();

				final CharSequence[] items = { "Current Location",
						"Contact Address", "Point on Map" };

				AlertDialog.Builder builder = new AlertDialog.Builder(MainFragment.this.getActivity());
				builder.setTitle("Choose End Location");
				builder.setItems(items, new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int item) {
						if (items[item].equals("Current Location")) {	
					/*		myActivity = (MyActivity) activity;
							myActivity.getmLocationClient();
							Location loc = this.MainFragment.getmLocationClient().getLastLocation();*/
							LatLng mCurrentLatLng = getLastLocation();
							if (mCurrentLatLng != null){
								SharedPreferences.Editor prefsEditor = prefs.edit();
								setTextBoxLocation("My Location", false);
								prefsEditor.putBoolean(OTPApp.PREFERENCE_KEY_DESTINATION_IS_MY_LOCATION, true);

								if (mCurrentLatLng != null) {
									if (endMarker != null){
										endMarker.remove();
										endMarker = null;
									}
								}

								prefsEditor.commit();
							}
							else{
								Toast.makeText(MainFragment.this.applicationContext, applicationContext.getResources().getString(R.string.location_error), Toast.LENGTH_LONG).show();
							}
							
							
						} else if (items[item].equals("Contact Address")) {
							Intent intent = new Intent(Intent.ACTION_PICK);
							intent.setType(ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_TYPE);
							((MyActivity)MainFragment.this.getActivity()).setButtonStartLocation(false);

							((MyActivity)MainFragment.this.getActivity()).startActivityForResult(intent,
									OTPApp.CHOOSE_CONTACT_REQUEST_CODE);

						} else { // Point on Map
							if (endMarker != null){
								updateMarkerPosition(endMarker.getPosition(), false);
							}
							else{
								setTextBoxLocation("", false);
								tbEndLocation.setHint(getResources().getString(R.string.need_to_place_marker));
								tbEndLocation.requestFocus();
							}
						}
					}
				});
				AlertDialog alert = builder.create();
				alert.show();
				return true;
			}

		};
		
		tbEndLocation.setOnTouchListener(otlEnd);
		

		btnPlanTrip.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				LatLng mCurrentLatLng = getLastLocation();
				String startLocationString = null;
				String endLocationString = null;

				Boolean isOriginMyLocation = prefs.getBoolean(OTPApp.PREFERENCE_KEY_ORIGIN_IS_MY_LOCATION, false);
				Boolean isDestinationMyLocation = prefs.getBoolean(OTPApp.PREFERENCE_KEY_DESTINATION_IS_MY_LOCATION, false);
				
				panelDisplayDirection.setVisibility(View.INVISIBLE);
				if (route != null){
					for (Polyline p : route){
						p.remove();
					}
					route = null;
				}
				if (modeMarkers != null){
					for (Marker m : modeMarkers){
						m.remove();
					}
					modeMarkers = null;
				}

				
				if (isOriginMyLocation && isDestinationMyLocation){
					Toast.makeText(MainFragment.this.applicationContext, applicationContext.getResources().getString(R.string.origin_destination_are_mylocation), Toast.LENGTH_SHORT).show();
					return;
				}
				else if (isOriginMyLocation || isDestinationMyLocation){
					if (mCurrentLatLng == null){
						Toast.makeText(MainFragment.this.applicationContext, applicationContext.getResources().getString(R.string.location_error), Toast.LENGTH_LONG).show();
						return;
					}
					else {
						if (isOriginMyLocation){
							startLocationString = mCurrentLatLng.latitude + "," + mCurrentLatLng.longitude;
							if (endMarker == null){
								Toast.makeText(MainFragment.this.applicationContext, applicationContext.getResources().getString(R.string.need_to_place_markers_before_planning), Toast.LENGTH_SHORT).show();
								return;
							}
							else{
								endLocationString = endMarker.getPosition().latitude + "," + endMarker.getPosition().longitude;
							}
						}
						else if (isDestinationMyLocation){
							endLocationString = mCurrentLatLng.latitude + "," + mCurrentLatLng.longitude;
							if (startMarker == null){
								Toast.makeText(MainFragment.this.applicationContext, applicationContext.getResources().getString(R.string.need_to_place_markers_before_planning), Toast.LENGTH_SHORT).show();
								return;
							}
							else{
								startLocationString = startMarker.getPosition().latitude + "," + startMarker.getPosition().longitude;
							}
						}
					}
				}
				else{
					if ((startMarker == null) || (endMarker == null)){
						Toast.makeText(MainFragment.this.applicationContext, applicationContext.getResources().getString(R.string.need_to_place_markers_before_planning), Toast.LENGTH_SHORT).show();
						return;
					}
					else{
						startLocationString = startMarker.getPosition().latitude + "," + startMarker.getPosition().longitude;
						endLocationString = endMarker.getPosition().latitude + "," + endMarker.getPosition().longitude;
					}
				}
						
				if (!isStartLocationGeocodingProcessed && !isOriginMyLocation){
					Toast.makeText(MainFragment.this.applicationContext, applicationContext.getResources().getString(R.string.need_to_place_markers_before_planning), Toast.LENGTH_SHORT).show();
					return;
				}
				else if (!isEndLocationGeocodingProcessed && !isDestinationMyLocation){
					Toast.makeText(MainFragment.this.applicationContext, applicationContext.getResources().getString(R.string.need_to_place_markers_before_planning), Toast.LENGTH_SHORT).show();
					return;
				}
				
				
				Request request = new Request();
				try {
					request.setFrom(URLEncoder.encode(startLocationString, "UTF-8"));
					request.setTo(URLEncoder.encode(endLocationString, "UTF-8"));
				} catch (UnsupportedEncodingException e1) {
					e1.printStackTrace();
				}

				request.setArriveBy(false);
				
				OptimizeSpinnerItem optimizeSpinnerItem = (OptimizeSpinnerItem) ddlOptimization.getSelectedItem();
				if (optimizeSpinnerItem == null){
					optimizeSpinnerItem = (OptimizeSpinnerItem) ddlOptimization.getItemAtPosition(0);
				}
				request.setOptimize(optimizeSpinnerItem.getOptimizeType());
				
				TraverseModeSpinnerItem traverseModeSpinnerItem = (TraverseModeSpinnerItem) ddlTravelMode.getSelectedItem();
				if (traverseModeSpinnerItem == null){
					traverseModeSpinnerItem = (TraverseModeSpinnerItem) ddlTravelMode.getItemAtPosition(0);
				}
				request.setModes(traverseModeSpinnerItem.getTraverseModeSet());
				
				
				Integer defaultMaxWalkInt = applicationContext.getResources().getInteger(R.integer.max_walking_distance);

				try {
					Double maxWalk = Double.parseDouble(prefs.getString(OTPApp.PREFERENCE_KEY_MAX_WALKING_DISTANCE,
							defaultMaxWalkInt.toString()));
					request.setMaxWalkDistance(maxWalk);
				} catch (NumberFormatException ex) {
					request.setMaxWalkDistance((double)defaultMaxWalkInt);
				}

				request.setWheelchair(prefs.getBoolean(OTPApp.PREFERENCE_KEY_WHEEL_ACCESSIBLE,
						false));

				request.setDateTime(
						DateFormat.format("MM/dd/yy",
								System.currentTimeMillis()).toString(),
						DateFormat
								.format("hh:mmaa", System.currentTimeMillis())
								.toString());

				request.setShowIntermediateStops(Boolean.TRUE);
				
				WeakReference<Activity> weakContext = new WeakReference<Activity>(MainFragment.this.getActivity());

				new TripRequest(weakContext, MainFragment.this.applicationContext, app
						.getSelectedServer(), MainFragment.this)
						.execute(request);

				InputMethodManager imm = (InputMethodManager) MainFragment.this.getActivity()
						.getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(tbEndLocation.getWindowToken(), 0);
				imm.hideSoftInputFromWindow(tbStartLocation.getWindowToken(), 0);

			}
		});
		
		
		OnFocusChangeListener tbLocationOnFocusChangeListener = new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!isRealLostFocus) {
					isRealLostFocus = true;
					return;
				}
				TextView tv = (TextView) v;
				if (!hasFocus) {
					String text = tv.getText().toString();
					if (!text.isEmpty()){
						if (v.getId() == R.id.tbStartLocation && !isStartLocationGeocodingProcessed) {
							processAddress(true, tv.getText().toString());
						} else if (v.getId() == R.id.tbEndLocation && !isEndLocationGeocodingProcessed) {
							processAddress(false, tv.getText().toString());
						}
					} else {
						if (v.getId() == R.id.tbStartLocation){
							tv.setHint(getResources().getString(R.string.start_location_hint));
						}
						else if (v.getId() == R.id.tbEndLocation){	
							tv.setHint(getResources().getString(R.string.end_location_hint));
						}
					}
				}
			}
		};
		tbStartLocation.setOnFocusChangeListener(tbLocationOnFocusChangeListener);
		tbEndLocation.setOnFocusChangeListener(tbLocationOnFocusChangeListener);

		TextWatcher textWatcherStart = new TextWatcher() {

	        @Override
	        public void onTextChanged(CharSequence s, int start, int before, int count) {
	        }

	        @Override
	        public void beforeTextChanged(CharSequence s, int start, int count,
	                int after) {
	        }

	        @Override
	        public void afterTextChanged(Editable s) {
	        	if (isStartLocationChangedByUser){
					SharedPreferences.Editor prefsEditor = prefs.edit();
					prefsEditor.putBoolean(OTPApp.PREFERENCE_KEY_ORIGIN_IS_MY_LOCATION, false);
					prefsEditor.commit();
		            isStartLocationGeocodingProcessed = false;
	        	}
	        	else{
	        		isStartLocationChangedByUser = true;
	        	}
	        }
	    };
	    
	    

		TextWatcher textWatcherEnd = new TextWatcher() {

	        @Override
	        public void onTextChanged(CharSequence s, int start, int before, int count) {
	        }

	        @Override
	        public void beforeTextChanged(CharSequence s, int start, int count,
	                int after) {
	        }

	        @Override
	        public void afterTextChanged(Editable s) {
	        	if (isEndLocationChangedByUser){
					SharedPreferences.Editor prefsEditor = prefs.edit();
					prefsEditor.putBoolean(OTPApp.PREFERENCE_KEY_DESTINATION_IS_MY_LOCATION, false);
					prefsEditor.commit();
	        		isEndLocationGeocodingProcessed = false;
	        	}
	        	else{
	        		isEndLocationChangedByUser = true;
	        	}
	        }
	    };
	    
	    tbStartLocation.addTextChangedListener(textWatcherStart);
	    tbEndLocation.addTextChangedListener(textWatcherEnd);
		
		OnEditorActionListener tbLocationOnEditorActionListener = new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				if (v.getId() == R.id.tbStartLocation
						&& actionId == EditorInfo.IME_ACTION_NEXT
						|| (event != null
								&& event.getAction() == KeyEvent.ACTION_DOWN && event
								.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
					isRealLostFocus = false;
					processAddress(true, v.getText().toString());
				} else if (v.getId() == R.id.tbEndLocation
						&& actionId == EditorInfo.IME_ACTION_DONE
						|| (event != null
								&& event.getAction() == KeyEvent.ACTION_DOWN && event
								.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
					isRealLostFocus = false;
					processAddress(false, v.getText().toString());
				}
				return false;
			}
		};

		tbStartLocation
				.setOnEditorActionListener(tbLocationOnEditorActionListener);
		tbEndLocation
				.setOnEditorActionListener(tbLocationOnEditorActionListener);

		
		OnClickListener oclDisplayDirection = new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				saveOTPBundle();
				getFragmentListener().onSwitchedToDirectionFragment();
			}
		};
		btnDisplayDirection.setOnClickListener(oclDisplayDirection);

		// Do NOT show direction icon if there is no direction yet
		if (getFragmentListener().getCurrentItinerary().isEmpty()) {
			panelDisplayDirection.setVisibility(View.INVISIBLE);
		} else {
			panelDisplayDirection.setVisibility(View.VISIBLE);
		}

		
		OnClickListener oclCompass = new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				CameraPosition oldCameraPosition = mMap.getCameraPosition();
				CameraPosition newCameraPosition = new CameraPosition(oldCameraPosition.target, oldCameraPosition.zoom, 0, 0);
				mMap.animateCamera(CameraUpdateFactory.newCameraPosition(newCameraPosition));
			}
		};
		btnCompass.setOnClickListener(oclCompass);
		
		if ((bearing != 0) || (tilt != 0)){
			btnCompass.setVisibility(View.VISIBLE);
		} else {
			btnCompass.setVisibility(View.INVISIBLE);
		}
		
		OnClickListener oclMyLocation = new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				LatLng mCurrentLatLng = getLastLocation();
				
				if (mCurrentLatLng == null){
					Toast.makeText(applicationContext, applicationContext.getResources().getString(R.string.location_error), Toast.LENGTH_LONG).show();
				}
				else{
					if (mMap.getCameraPosition().zoom < OTPApp.defaultMyLocationZoomLevel){
						mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mCurrentLatLng, OTPApp.defaultMyLocationZoomLevel));
					}
					else{
						mMap.animateCamera(CameraUpdateFactory.newLatLng(getLastLocation()));
					}
				}
			}
		};
		btnMyLocation.setOnClickListener(oclMyLocation);
		
		
		AdapterView.OnItemSelectedListener itinerarySpinnerListener = new AdapterView.OnItemSelectedListener() {
		    @Override
		    public void  onItemSelected (AdapterView<?> parent, View view, int position, long id){
//		    	Toast.makeText(parent.getContext(), 
//		    				   Long.toString(id) + " chosen " +
//		    				   parent.getItemAtPosition(position).toString(), 
//		    				   Toast.LENGTH_SHORT).show();
		    	fragmentListener.onItinerarySelected(position);
		    	
		    	if (!restoredSavedState){
			    	showRouteOnMap(fragmentListener.getCurrentItinerary(), true);
		    	}
		    }

		    @Override
		    public void onNothingSelected (AdapterView<?> parent) {
		    	
		    }
		};
		int currentItineraryIndex = fragmentListener.getCurrentItineraryIndex();

		itinerarySelectionSpinner.setSelection(currentItineraryIndex);
		itinerarySelectionSpinner.setOnItemSelectedListener(itinerarySpinnerListener);
		
	}
	
	private void saveOTPBundle() {
		OTPBundle bundle = new OTPBundle();
		bundle.setFromText(tbStartLocation.getText().toString());
		bundle.setToText(tbEndLocation.getText().toString());

		this.getFragmentListener().setOTPBundle(bundle);
	}


	private void addMapListeners(){
		OnMapClickListener omcl = new OnMapClickListener() {
			@Override
			public void onMapClick(LatLng latlng) {
				if (tbStartLocation.hasFocus()){
					setMarker(true, latlng, true);
				}
				else{
					setMarker(false, latlng, true);
				}
			}
		};
		mMap.setOnMapClickListener(omcl);
		
		OnMarkerDragListener omdl = new OnMarkerDragListener() {
			LatLng markerPreviousPosition;
			
			@Override
			public void onMarkerDrag(Marker marker) {
			}

			@Override
			public void onMarkerDragEnd(Marker marker) {	
				LatLng markerLatlng = marker.getPosition();

				if (((app.getSelectedServer() != null) && LocationUtil.checkPointInBoundingBox(markerLatlng, app.getSelectedServer(), OTPApp.CHECK_BOUNDS_ACCEPTABLE_ERROR)) 
						|| (app.getSelectedServer() == null)){
					if ((startMarker != null) && (marker.hashCode() == startMarker.hashCode())){
						updateMarkerPosition(markerLatlng, true);
					}
					else if ((endMarker != null) && (marker.hashCode() == endMarker.hashCode())){
						updateMarkerPosition(markerLatlng, false);
					}
				}
				else{
					marker.setPosition(markerPreviousPosition);
					Toast.makeText(applicationContext, applicationContext.getResources().getString(R.string.marker_out_of_boundaries), Toast.LENGTH_SHORT).show();
				}
			}
			
			@Override
			public void onMarkerDragStart(Marker marker) {
				LatLng markerLatlng = marker.getPosition();

				if ((app.getSelectedServer() != null) && LocationUtil.checkPointInBoundingBox(markerLatlng, app.getSelectedServer(), OTPApp.CHECK_BOUNDS_ACCEPTABLE_ERROR)){
					markerPreviousPosition = markerLatlng;
				}
			}
		};
		mMap.setOnMarkerDragListener(omdl);
		
		OnMapLongClickListener omlcl = new OnMapLongClickListener() {
			@Override
			public void onMapLongClick(LatLng latlng) {
				final LatLng latLngFinal = latlng;
				final CharSequence[] items = {applicationContext.getResources().getString(R.string.start_marker_activated), applicationContext.getResources().getString(R.string.end_marker_activated)};

				AlertDialog.Builder builder = new AlertDialog.Builder(MainFragment.this.getActivity());
				builder.setTitle(getResources().getString(R.string.markers_dialog_title));
				builder.setItems(items, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {
						if (item == 0){
							setMarker(true, latLngFinal, true);
						}
						else{
							setMarker(false, latLngFinal, true);
						}
					}
				});
				AlertDialog alert = builder.create();
				alert.show();
			}
		};
		mMap.setOnMapLongClickListener(omlcl);
		
		OnClickListener oclH = new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				drawerLayout.openDrawer(Gravity.START);
			}
		};
		btnHandle.setOnClickListener(oclH);
	}
	
	
	private void restoreState(Bundle savedInstanceState){
		if (savedInstanceState != null){
			mMap = retrieveMap(mMap);
			restoredSavedState = true;
			setTextBoxLocation(savedInstanceState.getString(OTPApp.BUNDLE_KEY_TB_START_LOCATION), true);
			setTextBoxLocation(savedInstanceState.getString(OTPApp.BUNDLE_KEY_TB_END_LOCATION), false);
			CameraPosition camPosition = savedInstanceState.getParcelable(OTPApp.BUNDLE_KEY_MAP_CAMERA);
			if (camPosition != null){
				mMap.moveCamera(CameraUpdateFactory.newCameraPosition(camPosition));
			}
			
			if ((startMarkerPosition = savedInstanceState.getParcelable(OTPApp.BUNDLE_KEY_MAP_START_MARKER_POSITION)) != null){
				startMarker = addStartEndMarker(startMarkerPosition, true);
			}
			if ((endMarkerPosition = savedInstanceState.getParcelable(OTPApp.BUNDLE_KEY_MAP_END_MARKER_POSITION)) != null){
				endMarker = addStartEndMarker(endMarkerPosition, false);
			}
			
			isStartLocationGeocodingProcessed = savedInstanceState.getBoolean(OTPApp.BUNDLE_KEY_IS_START_LOCATION_GEOCODING_PROCESSED);
			isEndLocationGeocodingProcessed = savedInstanceState.getBoolean(OTPApp.BUNDLE_KEY_IS_END_LOCATION_GEOCODING_PROCESSED);
			appStarts = savedInstanceState.getBoolean(OTPApp.BUNDLE_KEY_APP_STARTS);
			isStartLocationChangedByUser = savedInstanceState.getBoolean(OTPApp.BUNDLE_KEY_IS_START_LOCATION_CHANGED_BY_USER);
			isEndLocationChangedByUser = savedInstanceState.getBoolean(OTPApp.BUNDLE_KEY_IS_END_LOCATION_CHANGED_BY_USER);
			
			ddlOptimization.setItemChecked(savedInstanceState.getInt(OTPApp.BUNDLE_KEY_DDL_OPTIMIZATION), true);
			ddlTravelMode.setItemChecked(savedInstanceState.getInt(OTPApp.BUNDLE_KEY_DDL_TRAVEL_MODE), true);
			OTPBundle otpBundle = (OTPBundle) savedInstanceState.getSerializable(OTPApp.BUNDLE_KEY_OTP_BUNDLE);
			if (otpBundle != null){
				List<Itinerary> itineraries = otpBundle.getItineraryList(); 
				getFragmentListener().onItinerariesLoaded(itineraries);
				getFragmentListener().onItinerarySelected(otpBundle.getCurrentItineraryIndex());
				fillItinerariesSpinner(itineraries);
			}
			showRouteOnMap(getFragmentListener().getCurrentItinerary(), false);
					
			isStartLocationChangedByUser = false;
			isEndLocationChangedByUser = false;
		}
		else{
			restoredSavedState = false;
		}
	}
	
	private GoogleMap retrieveMap(GoogleMap mMap) {
	    // Do a null check to confirm that we have not already instantiated the map.
	    if (mMap == null) {
	        mMap = ((SupportMapFragment) getFragmentManager().findFragmentById(R.id.map))
	                            .getMap();
	        // Check if we were successful in obtaining the map.
	        if (mMap == null) {
		        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(applicationContext);
		        
		        if(status!=ConnectionResult.SUCCESS){
		            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, getActivity(), OTPApp.CHECK_GOOGLE_PLAY_REQUEST_CODE);
		            dialog.show();
		        }	        
		    }

	    }
		
	    return mMap;
	}

	
	public void runAutoDetectServer(LatLng mCurrentLatLng){
		if (mCurrentLatLng == null){
			Toast.makeText(applicationContext, applicationContext.getResources().getString(R.string.location_error), Toast.LENGTH_LONG).show();
		}
		else{
			ServersDataSource dataSource = ServersDataSource.getInstance(applicationContext);
			WeakReference<Activity> weakContext = new WeakReference<Activity>(getActivity());
	
			ServerSelector serverSelector = new ServerSelector(weakContext, applicationContext, dataSource, this);
			serverSelector.execute(mCurrentLatLng);
			needToRunAutoDetect = false;
		}
	}
	
	private void setSelectedServer(Server s){
		restartMap();
		restartTextBoxes();
		
		app.setSelectedServer(s);
	}
	
	private void restartMap(){
		mMap.clear();
		
		startMarker = null;
		startMarkerPosition = null;
		endMarker = null;
		endMarkerPosition = null;
		route = null;
		modeMarkers = null;
		boundariesPolyline = null;
	}
	
	private void restartTextBoxes(){
		SharedPreferences.Editor prefsEditor = prefs.edit();
		setTextBoxLocation("My Location", true);
		prefsEditor.putBoolean(OTPApp.PREFERENCE_KEY_ORIGIN_IS_MY_LOCATION, true);
		prefsEditor.commit();
		
		setTextBoxLocation("", false);
	}
	
	private void setLocationTb(LatLng latlng, boolean isStartTb){
		DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
		decimalFormatSymbols.setDecimalSeparator('.');
		DecimalFormat decimalFormat = new DecimalFormat("#.00000", decimalFormatSymbols);
		if (isStartTb){
			setTextBoxLocation(decimalFormat.format(latlng.latitude) + ", " + decimalFormat.format(latlng.longitude), true);
		}
		else{
			setTextBoxLocation(decimalFormat.format(latlng.latitude) + ", " + decimalFormat.format(latlng.longitude), false);
		}
	}
	
	private void setMarker(boolean isStartMarker, LatLng latlng, boolean showMessage){
		SharedPreferences.Editor prefsEditor = prefs.edit();
		
		if (((app.getSelectedServer() != null) && LocationUtil.checkPointInBoundingBox(latlng, app.getSelectedServer(), OTPApp.CHECK_BOUNDS_ACCEPTABLE_ERROR)) 
				|| (app.getSelectedServer() == null)){
			if (showMessage){
				String toasText;
				if (isStartMarker){
					toasText = applicationContext.getResources().getString(R.string.start_marker_activated);
				}
				else{
					toasText = applicationContext.getResources().getString(R.string.end_marker_activated);
				}
				Toast.makeText(applicationContext, toasText, Toast.LENGTH_SHORT).show();
			}
			
			if(isStartMarker) {
				if (startMarker == null){
					startMarker = addStartEndMarker(latlng, true);
				}
				else{
					setMarkerPosition(true, latlng);
					startMarkerPosition = latlng;
				}
				MainFragment.this.setLocationTb(latlng, true);
				prefsEditor.putBoolean(OTPApp.PREFERENCE_KEY_ORIGIN_IS_MY_LOCATION, false);
				updateMarkerPosition(latlng, true);
			}
			else {
				if (endMarker == null){
					endMarker = addStartEndMarker(latlng, false);
				}			
				else{
					setMarkerPosition(false, latlng);
					endMarkerPosition = latlng;
				}
				MainFragment.this.setLocationTb(latlng, false);
				prefsEditor.putBoolean(OTPApp.PREFERENCE_KEY_DESTINATION_IS_MY_LOCATION, false);
				updateMarkerPosition(latlng, false);
			}
			prefsEditor.commit();
		}
		else{
			if (showMessage){
				Toast.makeText(applicationContext, applicationContext.getResources().getString(R.string.marker_out_of_boundaries), Toast.LENGTH_SHORT).show();
			}
		}
	}
	
	private void setMarkerPosition(boolean isStartMarker, LatLng latLng){
		if (isStartMarker){
			if (startMarker == null){
				startMarker = addStartEndMarker(latLng, true);
			}
			else{
				startMarker.setPosition(latLng);
			}
			startMarkerPosition = latLng;
		}
		else{
			if (endMarker == null){
				endMarker = addStartEndMarker(latLng, false);
			}
			else{
				endMarker.setPosition(latLng);			
			}
			endMarkerPosition = latLng;
		}		
	}
	
	private Marker addStartEndMarker(LatLng latLng, boolean isStartMarker){
		MarkerOptions markerOptions = new MarkerOptions().position(latLng)
														 .draggable(true);
		if (isStartMarker){
			markerOptions.title(applicationContext.getResources().getString(R.string.start_marker_title))
						 .snippet(applicationContext.getResources().getString(R.string.start_marker_description))
						 .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
			startMarkerPosition = latLng;
			return mMap.addMarker(markerOptions);
		}
		else{
			markerOptions.title(applicationContext.getResources().getString(R.string.end_marker_title))
						 .snippet(applicationContext.getResources().getString(R.string.end_marker_description))
						 .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
			endMarkerPosition = latLng;
			return mMap.addMarker(markerOptions);
		}
	}
	
	
	private String getLocationTbText(boolean isTbStartLocation){
		if (isTbStartLocation){
			return tbStartLocation.getText().toString();
		}
		else{
			return tbEndLocation.getText().toString();
		}
	}
	
	private void updateMarkerPosition(LatLng newLatLng, boolean isStartMarker){
		setLocationTb(newLatLng, isStartMarker);
		String locationText = getLocationTbText(isStartMarker);
		if (isStartMarker){
			isStartLocationChangedByUser = false;
		}
		else{
			isEndLocationChangedByUser = false;
		}
		processAddress(isStartMarker, locationText);
	}
	
	@Override
	public void onStart() {
		super.onStart();	

		mLocationClient = new LocationClient(applicationContext, this, this);
		//mLocationClient.connect();
		
		if (!mLocationClient.isConnected() && !mLocationClient.isConnecting()){
			mLocationClient.connect();
		}
	}
	
	public void connectLocationClient(){
		if (!mLocationClient.isConnected() && !mLocationClient.isConnecting()){
			mLocationClient.connect();
		}
	}
	
	public void disconnectLocationClient(){
		if (mLocationClient.isConnected()){
			mLocationClient.disconnect();
		}
	}
	
	public void onSaveInstanceState(Bundle bundle){
		super.onSaveInstanceState(bundle);
		bundle.putParcelable(OTPApp.BUNDLE_KEY_MAP_CAMERA, mMap.getCameraPosition());
		bundle.putParcelable(OTPApp.BUNDLE_KEY_MAP_START_MARKER_POSITION, startMarkerPosition);
		bundle.putParcelable(OTPApp.BUNDLE_KEY_MAP_END_MARKER_POSITION, endMarkerPosition);
		bundle.putBoolean(OTPApp.BUNDLE_KEY_APP_STARTS, appStarts);
		bundle.putBoolean(OTPApp.BUNDLE_KEY_IS_START_LOCATION_GEOCODING_PROCESSED, isStartLocationGeocodingProcessed);
		bundle.putBoolean(OTPApp.BUNDLE_KEY_IS_END_LOCATION_GEOCODING_PROCESSED, isEndLocationGeocodingProcessed);
		bundle.putBoolean(OTPApp.BUNDLE_KEY_IS_START_LOCATION_CHANGED_BY_USER, isStartLocationChangedByUser);
		bundle.putBoolean(OTPApp.BUNDLE_KEY_IS_END_LOCATION_CHANGED_BY_USER, isEndLocationChangedByUser);
		bundle.putString(OTPApp.BUNDLE_KEY_TB_START_LOCATION, tbStartLocation.getText().toString());
		bundle.putString(OTPApp.BUNDLE_KEY_TB_END_LOCATION, tbEndLocation.getText().toString());
		bundle.putInt(OTPApp.BUNDLE_KEY_DDL_OPTIMIZATION, ddlOptimization.getSelectedItemPosition());
		bundle.putInt(OTPApp.BUNDLE_KEY_DDL_TRAVEL_MODE, ddlTravelMode.getSelectedItemPosition());
		if (!fragmentListener.getCurrentItineraryList().isEmpty()){
			OTPBundle otpBundle = new OTPBundle();
			otpBundle.setItineraryList(fragmentListener.getCurrentItineraryList());
			otpBundle.setCurrentItineraryIndex(fragmentListener.getCurrentItineraryIndex());
			otpBundle.setCurrentItinerary(fragmentListener.getCurrentItinerary());
			bundle.putSerializable(OTPApp.BUNDLE_KEY_OTP_BUNDLE, otpBundle);
		}

	}

	public void processAddress(final boolean isStartTextBox, String address) {
		WeakReference<Activity> weakContext = new WeakReference<Activity>(getActivity());

		OTPGeocoding geocodingTask = new OTPGeocoding(weakContext, applicationContext,
				isStartTextBox, app.getSelectedServer(), prefs.getString(
						OTPApp.PREFERENCE_KEY_GEOCODER_PROVIDER, applicationContext.getResources().getString(R.string.geocoder_nominatim)),
				this);	
		LatLng mCurrentLatLng = getLastLocation();

		if(address.equalsIgnoreCase(this.getResources().getString(R.string.my_location))) {
			if (mCurrentLatLng != null){
				geocodingTask.execute(address, String.valueOf(mCurrentLatLng.latitude), String.valueOf(mCurrentLatLng.longitude));
			}
			else{
				Toast.makeText(applicationContext, applicationContext.getResources().getString(R.string.location_error), Toast.LENGTH_LONG).show();	
			}
		}
		else{
			geocodingTask.execute(address);
		}
	}


	@Override
	public void onResume() {
		super.onResume();
		
		Log.v(TAG, "MainFragment onResume");

/*		if (needToRunAutoDetect) {
			runAutoDetectServer(true);
			needToRunAutoDetect = false;
		}*/
		
	}

	@Override
	public void onPause() {

		super.onPause();
	}
	
	@Override
	public void onStop() {
		
       if (mLocationClient.isConnected()) {
 //   	   mLocationClient.removeLocationUpdates(this);
        }

        mLocationClient.disconnect();

		super.onStop();
	}

	@Override
	public void onDestroy() {
		// Release all map-related objects to make sure GPS is shut down when
		// the user leaves the app

		Log.d(TAG, "Released all map objects in MainFragment.onDestroy()");

		super.onDestroy();
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if (key == null) {
			return;
		}
		Log.v(TAG, "A preference was changed: " + key);
		if (key.equals(OTPApp.PREFERENCE_KEY_MAP_TILE_SOURCE)) {
			String overlayString = prefs.getString(OTPApp.PREFERENCE_KEY_MAP_TILE_SOURCE, applicationContext.getResources().getString(R.string.map_tiles_default_server));
			updateOverlay(overlayString);
		} else if (key.equals(OTPApp.PREFERENCE_KEY_SELECTED_CUSTOM_SERVER)) {
			if (prefs.getBoolean(OTPApp.PREFERENCE_KEY_SELECTED_CUSTOM_SERVER, false)){
				setSelectedServer(new Server(prefs.getString(OTPApp.PREFERENCE_KEY_CUSTOM_SERVER_URL, "")));
				Log.v(TAG, "Now using custom OTP server: " + prefs.getString(OTPApp.PREFERENCE_KEY_CUSTOM_SERVER_URL, ""));
				WeakReference<Activity> weakContext = new WeakReference<Activity>(getActivity());

				MetadataRequest metaRequest = new MetadataRequest(weakContext, applicationContext, this);
				metaRequest.execute(prefs.getString(OTPApp.PREFERENCE_KEY_CUSTOM_SERVER_URL, ""));
			}
			else{
				long serverId = prefs.getLong(OTPApp.PREFERENCE_KEY_SELECTED_SERVER, 0);
				if (serverId != 0){
					ServersDataSource dataSource = ServersDataSource.getInstance(applicationContext);
					dataSource.open();
					Server s = new Server(dataSource.getServer(prefs.getLong(OTPApp.PREFERENCE_KEY_SELECTED_SERVER, 0)));
					dataSource.close();
					
					setSelectedServer(s);
					addBoundariesRectangle(s);

					LatLng mCurrentLatLng = getLastLocation();
					LatLng serverCenter = new LatLng(s.getCenterLatitude(), s.getCenterLongitude());
					if (((mCurrentLatLng != null) && !LocationUtil.checkPointInBoundingBox(mCurrentLatLng, s, OTPApp.CHECK_BOUNDS_ACCEPTABLE_ERROR)) || (mCurrentLatLng == null)){
						mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(serverCenter, OTPApp.defaultInitialZoomLevel));	
						setMarker(true, serverCenter, false);
					}
				}
			}
			
		} else if (key.equals(OTPApp.PREFERENCE_KEY_AUTO_DETECT_SERVER)) {
			Log.v(TAG, "Detected change in auto-detect server preference. Value is now: " + prefs.getBoolean(OTPApp.PREFERENCE_KEY_AUTO_DETECT_SERVER, true));
			
			if (prefs.getBoolean(OTPApp.PREFERENCE_KEY_AUTO_DETECT_SERVER, true)) {
				needToRunAutoDetect = true;
			}
			else {
				needToRunAutoDetect = false;
			}
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu pMenu, MenuInflater inflater) {
		// MenuInflater inflater = getMenuInflater();
		super.onCreateOptionsMenu(pMenu, inflater);
		inflater.inflate(R.menu.menu, pMenu);
		mGPS = pMenu.getItem(0);
	}

	public void onPrepareOptionsMenu(final Menu pMenu) {
		if (isGPSEnabled()) {
			mGPS.setTitle(R.string.disable_gps);
		} else {
			mGPS.setTitle(R.string.enable_gps);
		}
		super.onPrepareOptionsMenu(pMenu);
	}

	public boolean onOptionsItemSelected(final MenuItem pItem) {
		OTPApp app = ((OTPApp) getActivity().getApplication());
		switch (pItem.getItemId()) {
		case R.id.exit:
			getActivity().finish();
			return true;
		case R.id.gps_settings:
			Intent myIntent = new Intent(
					Settings.ACTION_LOCATION_SOURCE_SETTINGS);
			startActivity(myIntent);
			break;
		case R.id.settings:
			needToRunAutoDetect = false;
			getActivity().startActivityForResult(
					new Intent(getActivity(), SettingsActivity.class),
					OTPApp.REFRESH_SERVER_LIST_REQUEST_CODE);
			break;
		case R.id.feedback:
			Server selectedServer = app.getSelectedServer();

			String[] recipients = { selectedServer.getContactEmail(),
					getString(R.string.email_otp_android_developer) };

			String uriText = "mailto:";
			for (int i = 0; i < recipients.length; i++) {
				uriText += recipients[i] + ";";
			}

			String subject = "";
			subject += "Android OTP user report OTP trip ";
			Date d = Calendar.getInstance().getTime();
			subject += "[" + d.toString() + "]";
			uriText += "?subject=" + subject;

			String content = ((MyActivity)getActivity()).getCurrentRequestString();
			
			try {
				uriText += "&body=" + URLEncoder.encode(content, "UTF-8");
			} catch (UnsupportedEncodingException e1) {
				e1.printStackTrace();
				return false;
			}

			Uri uri = Uri.parse(uriText);

			Intent sendIntent = new Intent(Intent.ACTION_SENDTO);
			sendIntent.setData(uri);
			startActivity(Intent.createChooser(sendIntent, "Send email"));

			break;
		case R.id.server_info:
			Server server = app.getSelectedServer();
			
			if (server == null) {
				Log.w(TAG,
						"Tried to get server info when no server was selected");
				Toast.makeText(applicationContext, applicationContext.getResources().getString(R.string.info_server_no_server_selected), Toast.LENGTH_SHORT).show();
				break;
			}
		
			WeakReference<Activity> weakContext = new WeakReference<Activity>(getActivity());

			ServerChecker serverChecker = new ServerChecker(weakContext, applicationContext, true);
			serverChecker.execute(server);
				

			break;
		default:
			break;
		}

		return false;
	}

	private Boolean isGPSEnabled() {
		return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
	}

	public void moveMarker(Boolean start, Address addr) {
		LatLng latlng = new LatLng(addr.getLatitude(), addr.getLongitude());
		if (start) {
			setMarkerPosition(true, latlng);
			setTextBoxLocation(addr.getAddressLine(addr
					.getMaxAddressLineIndex()), true);
		} else {
			setMarkerPosition(false, latlng);
			setTextBoxLocation(addr.getAddressLine(addr
					.getMaxAddressLineIndex()), false);
		}
	}
	
	public void zoomToGeocodingResult(boolean isStartLocation, Address addr) {
		LatLng latlng = new LatLng(addr.getLatitude(), addr.getLongitude());
		LatLng mCurrentLatLng = getLastLocation();
		
		if (isStartLocation){
			if (isStartLocationChangedByUser){
				if (endMarker != null){
					zoomToTwoPoints(latlng, endMarkerPosition);
				}
				else if (prefs.getBoolean(OTPApp.PREFERENCE_KEY_DESTINATION_IS_MY_LOCATION, false)){
					if (mCurrentLatLng == null){
						Toast.makeText(applicationContext, applicationContext.getResources().getString(R.string.location_error), Toast.LENGTH_LONG).show();
					}
					else{
						zoomToTwoPoints(latlng, mCurrentLatLng);
					}
				}
				else{
					zoomToLocation(latlng);
				}
			}
		}
		else {
			if (isEndLocationChangedByUser){
				if (startMarker != null){
					zoomToTwoPoints(startMarkerPosition, latlng);
				}
				else if (prefs.getBoolean(OTPApp.PREFERENCE_KEY_ORIGIN_IS_MY_LOCATION, false)){
					if (mCurrentLatLng == null){
						Toast.makeText(applicationContext, applicationContext.getResources().getString(R.string.location_error), Toast.LENGTH_LONG).show();
					}
					else{
						zoomToTwoPoints(mCurrentLatLng, latlng);
					}
				}
				else{
					zoomToLocation(latlng);
				}
			}
		}
	}

	public void zoomToLocation(LatLng latlng) {
		if (latlng != null) {
			mMap.animateCamera(CameraUpdateFactory.newLatLng(latlng));
		}
	}
	
	public void zoomToTwoPoints(LatLng pointA, LatLng pointB) {
		if ((pointA.latitude != pointB.latitude) && (pointA.longitude != pointB.longitude)){
			LatLngBounds.Builder boundsCreator = LatLngBounds.builder();
			
			boundsCreator.include(pointA);
			boundsCreator.include(pointB);

			mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(boundsCreator.build(), OTPApp.defaultPadding));	
		}
	}

	public void setTextBoxLocation(String text, boolean isStartTextBox) {
		if (isStartTextBox) {
			isStartLocationChangedByUser = false;
			tbStartLocation.setText(text);
		} else {
			isEndLocationChangedByUser = false;
			tbEndLocation.setText(text);
		}
	}

	public void showRouteOnMap(List<Leg> itinerary, boolean animateCamera) {
		Log.v(TAG,
				"(TripRequest) legs size = "
						+ Integer.toString(itinerary.size()));
		if (route != null){
			for (Polyline legLine : route) {
				legLine.remove();
			}
			route.clear();
		}
		if (modeMarkers != null){
			for (Marker modeMarker : modeMarkers){
				modeMarker.remove();
			}
		}
		route = new ArrayList<Polyline>();
		modeMarkers = new ArrayList<Marker>();
		
		if (!itinerary.isEmpty()) {
			List<LatLng> allGeoPoints = new ArrayList<LatLng>();
			LatLngBounds.Builder boundsCreator = LatLngBounds.builder();
			
			for (Leg leg : itinerary) {
				List<LatLng> points = LocationUtil.decodePoly(leg.legGeometry
						.getPoints());
				MarkerOptions modeMarkerOption = new MarkerOptions().position(points.get(0))
						                                        .icon(BitmapDescriptorFactory.fromResource(getPathIcon(leg.mode)));
				String title = "";
				String temp = "";
				if ((temp = leg.getRouteShortName()) != null){
					title += temp;
				}
				if ((temp = leg.getHeadsign()) != null){
					title += " " + temp;
					modeMarkerOption.title(title);
				}
				if ((temp = leg.getRouteLongName()) != null){
					modeMarkerOption.snippet(temp);
				}
				Marker modeMarker = mMap.addMarker(modeMarkerOption);
				modeMarkers.add(modeMarker);
				PolylineOptions options = new PolylineOptions().addAll(points)
						   .color(OTPApp.COLOR_ROUTE_LINE);
				Polyline routeLine = mMap.addPolyline(options);
				route.add(routeLine);
				for (LatLng point : points) {
					boundsCreator.include(point);
				}
				allGeoPoints.addAll(points);

			}
			if (animateCamera){
				mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(boundsCreator.build(), OTPApp.defaultPadding));
			}
		}
	}

	private int getPathIcon(String modeString){
		TraverseMode mode = TraverseMode.valueOf(modeString);
		int icon;
		
		if(mode.compareTo(TraverseMode.BICYCLE) == 0){
			icon = R.drawable.cycling;
		} else if(mode.compareTo(TraverseMode.CAR) == 0){
			icon = R.drawable.car;
		} else if((mode.compareTo(TraverseMode.BUS) == 0) || (mode.compareTo(TraverseMode.BUSISH) == 0)){
			icon = R.drawable.bus;
		} else if((mode.compareTo(TraverseMode.RAIL) == 0)  || (mode.compareTo(TraverseMode.TRAINISH) == 0)){
			icon = R.drawable.train;
		} else if(mode.compareTo(TraverseMode.FERRY) == 0){
			icon = R.drawable.ferry;
		} else if(mode.compareTo(TraverseMode.GONDOLA) == 0){
			icon = R.drawable.boat;
		} else if(mode.compareTo(TraverseMode.SUBWAY) == 0){
			icon = R.drawable.underground;
		} else if(mode.compareTo(TraverseMode.TRAM) == 0){
			icon = R.drawable.tramway;
		} else if(mode.compareTo(TraverseMode.WALK) == 0){
			icon = R.drawable.pedestriancrossing;
		} else if(mode.compareTo(TraverseMode.CABLE_CAR) == 0){
			icon = R.drawable.cablecar;
		} else if(mode.compareTo(TraverseMode.FUNICULAR) == 0){
			icon = R.drawable.funicolar;
		} else if(mode.compareTo(TraverseMode.TRANSIT) == 0){
			icon = R.drawable.road;
		} else if(mode.compareTo(TraverseMode.TRANSFER) == 0){
			icon = R.drawable.caution;
		}
		else{
			icon = R.drawable.road;
		}
		
		return icon;
	}

	/**
	 * @return the fragmentListener
	 */
	public OnFragmentListener getFragmentListener() {
		return fragmentListener;
	}

	/**
	 * @param fragmentListener
	 *            the fragmentListener to set
	 */
	public void setFragmentListener(OnFragmentListener fragmentListener) {
		this.fragmentListener = fragmentListener;
	}

	@Override
	public void onServerSelectorComplete(Server server) {
		//Update application server
		if (getActivity() != null){
			setSelectedServer(server);
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext);
			
			if (!prefs.getBoolean(OTPApp.PREFERENCE_KEY_SELECTED_CUSTOM_SERVER, false)){		
				LatLng mCurrentLatLng = getLastLocation();
				
				if ((mCurrentLatLng != null) && (LocationUtil.checkPointInBoundingBox(mCurrentLatLng, server, OTPApp.CHECK_BOUNDS_ACCEPTABLE_ERROR))){
					mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mCurrentLatLng, OTPApp.defaultInitialZoomLevel));
				}
				else{
					LatLng serverCenter = new LatLng(server.getCenterLatitude(), server.getCenterLongitude());
					mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(serverCenter, OTPApp.defaultInitialZoomLevel));
					setMarker(true, serverCenter, false);
				}
				
				addBoundariesRectangle(server);
			}
		}
	}

	@Override
	public void onTripRequestComplete(List<Itinerary> itineraries,
			String currentRequestString) {
		if (getActivity() != null){
			fillItinerariesSpinner(itineraries);
			if (!itineraries.isEmpty()){
				panelDisplayDirection.setVisibility(View.VISIBLE);
			}
			
			showRouteOnMap(itineraries.get(0).legs, true);
			OnFragmentListener ofl = getFragmentListener();

			// onItinerariesLoaded must be invoked before onItinerarySelected(0)
			ofl.onItinerariesLoaded(itineraries);
			ofl.onItinerarySelected(0);
			MyActivity myActivity = (MyActivity)getActivity();
			myActivity.setCurrentRequestString(currentRequestString);
		}
	}
	
	private void fillItinerariesSpinner(List<Itinerary> itineraryList){
		String[] itinerarySummaryList = new String[itineraryList.size()];
		for(int i=0; i<itinerarySummaryList.length; i++){
			Itinerary it = itineraryList.get(i);
			itinerarySummaryList[i] = Integer.toString(i+1) + ".   ";//Shown index is i + 1, to use 1-based indexes for the UI instead of 0-based
			itinerarySummaryList[i] += getString(R.string.total_duration) + " " + DateTimeConversion.getFormattedDurationText(it.duration/1000);
		//	itinerarySummaryList[i] += "   " + Long.toString(it.walkTime) + " meters";-->VREIXO
			itinerarySummaryList[i] +=  "   " + getString(R.string.walking_duration) + " " + DateTimeConversion.getFormattedDurationText(it.walkTime);
		}
		
		ArrayAdapter<String> itineraryAdapter = new ArrayAdapter<String>(this.getActivity().getApplicationContext(), android.R.layout.simple_spinner_item, itinerarySummaryList);
	
		itineraryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		itinerarySelectionSpinner.setAdapter(itineraryAdapter);
	}

	@Override
	public void onOTPGeocodingComplete(final boolean isStartTextbox,
			ArrayList<Address> addressesReturn) {
		if (getActivity() != null){
	
			if (isStartTextbox){
				isStartLocationGeocodingProcessed = true;
				if (isEndLocationGeocodingProcessed){
					InputMethodManager imm = (InputMethodManager) MainFragment.this.getActivity()
							.getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(tbStartLocation.getWindowToken(), 0);
				}
			}
			else{
				isEndLocationGeocodingProcessed = true;
			}
			// isRealLostFocus = false;
			
			try{
				AlertDialog.Builder geocoderAlert = new AlertDialog.Builder(
						getActivity());
				geocoderAlert.setTitle(R.string.geocoder_results_title)
						.setMessage(R.string.geocoder_no_results_message)
						.setCancelable(false)
						.setPositiveButton("OK", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
							}
						});
		
				if (addressesReturn.isEmpty()) {
					AlertDialog alert = geocoderAlert.create();
					alert.show();
					return;
				} else if (addressesReturn.size() == 1) {
					zoomToGeocodingResult(isStartTextbox, addressesReturn.get(0));
					moveMarker(isStartTextbox, addressesReturn.get(0));
					return;
				}
			
				AlertDialog.Builder geocoderSelector = new AlertDialog.Builder(
						getActivity());
				geocoderSelector.setTitle(R.string.choose_geocoder);
		
				final CharSequence[] addressesText = new CharSequence[addressesReturn
						.size()];
				for (int i = 0; i < addressesReturn.size(); i++) {
					Address addr = addressesReturn.get(i);
					addressesText[i] = addr.getAddressLine(0)
							+ "\n"
							+ ((addr.getAddressLine(1) != null) ? addr.getAddressLine(1) : "")
							+ ((addr.getAddressLine(2) != null) ? ", "
									+ addr.getAddressLine(2) : "");
					// addressesText[i] = addr.getAddressLine(0)+"\n"+
					// ((addr.getSubAdminArea()!=null) ? addr.getSubAdminArea()+", " :
					// "")+
					// ((addr.getAdminArea()!=null) ? addr.getAdminArea()+" " : "")+
					// ((addr.getPostalCode()!=null) ? addr.getPostalCode()+" " : "")+
					// ((addr.getCountryName()!=null) ? addr.getCountryName() : "");
					Log.v(TAG, addressesText[i].toString());
				}
		
				final ArrayList<Address> addressesTemp = addressesReturn;
				geocoderSelector.setItems(addressesText,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int item) {
								Address addr = addressesTemp.get(item);
								String addressLine = addr.getAddressLine(0)
										+ "\n"
										+ ((addr.getAddressLine(1) != null) ? addr.getAddressLine(1) : "")
										+ ((addr.getAddressLine(2) != null) ? ", "
												+ addr.getAddressLine(2) : "");
								addr.setAddressLine(addr.getMaxAddressLineIndex() + 1,
										addressLine);
								moveMarker(isStartTextbox, addr);
								zoomToGeocodingResult(isStartTextbox, addr);
								Log.v(TAG, "Chosen: " + addressesText[item]);
							}
						});
				AlertDialog alertGeocoder = geocoderSelector.create();
				alertGeocoder.show();
			}catch(Exception e){
				Log.e(TAG, "Error in Main Fragment Geocoding callback: " + e);
			}
		}
	}
	

	@Override
	public void onMetadataRequestComplete(GraphMetadata metadata) {
		if (getActivity() != null){
			double lowerLeftLatitude = metadata.getLowerLeftLatitude();
			double lowerLeftLongitude = metadata.getLowerLeftLongitude();
			double upperRightLatitude = metadata.getUpperRightLatitude();
			double upperRightLongitude = metadata.getUpperRightLongitude();
	
			Server selectedServer = app.getSelectedServer();
			
			String bounds = String.valueOf(lowerLeftLatitude) +
					"," + String.valueOf(lowerLeftLongitude) +
					"," + String.valueOf(upperRightLatitude) + "," + String.valueOf(upperRightLongitude);
			selectedServer.setBounds(bounds);
			
			SharedPreferences.Editor prefsEditor = PreferenceManager.getDefaultSharedPreferences(applicationContext).edit();
			prefsEditor.putString(PREFERENCE_KEY_CUSTOM_SERVER_BOUNDS, bounds);
			prefsEditor.commit();
			
			Log.v(TAG, "LowerLeft: " + Double.toString(lowerLeftLatitude)+","+Double.toString(lowerLeftLongitude));
			Log.v(TAG, "UpperRight" + Double.toString(upperRightLatitude)+","+Double.toString(upperRightLongitude));	
			
			addBoundariesRectangle(selectedServer);
			
			LatLng serverCenter = new LatLng(selectedServer.getCenterLatitude(), selectedServer.getCenterLongitude());
			
			LatLng mCurrentLatLng = getLastLocation();
			
			if ((mCurrentLatLng != null) && (LocationUtil.checkPointInBoundingBox(mCurrentLatLng, selectedServer, OTPApp.CHECK_BOUNDS_ACCEPTABLE_ERROR))){
				mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mCurrentLatLng, OTPApp.defaultInitialZoomLevel));
			}
			else{
				mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(serverCenter, OTPApp.defaultInitialZoomLevel));
				setMarker(true, serverCenter, false);
			}
		}
	}
	
	
	private void updateOverlay(String overlayString){
		if (actualTileOverlay != null){
			actualTileOverlay.remove();
		}
		if (overlayString.startsWith(OTPApp.MAP_TILE_GOOGLE)){
			int mapType = GoogleMap.MAP_TYPE_NORMAL;
			
			if (overlayString.equals(OTPApp.MAP_TILE_GOOGLE_HYBRID)){
				mapType = GoogleMap.MAP_TYPE_HYBRID;
			}
			else if (overlayString.equals(OTPApp.MAP_TILE_GOOGLE_NORMAL)){
				mapType = GoogleMap.MAP_TYPE_NORMAL;	
			}
			else if (overlayString.equals(OTPApp.MAP_TILE_GOOGLE_TERRAIN)){
				mapType = GoogleMap.MAP_TYPE_TERRAIN;
			}
			else if (overlayString.equals(OTPApp.MAP_TILE_GOOGLE_SATELLITE)){
				mapType = GoogleMap.MAP_TYPE_SATELLITE;	
			}
			mMap.setMapType(mapType);
		}
		else{
			mMap.setMapType(GoogleMap.MAP_TYPE_NONE);
			MyUrlTileProvider mTileProvider = new MyUrlTileProvider(OTPApp.CUSTOM_MAP_TILE_HEIGHT, OTPApp.CUSTOM_MAP_TILE_HEIGHT, overlayString);
			actualTileOverlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(mTileProvider).zIndex(OTPApp.CUSTOM_MAP_TILE_Z_INDEX));
		}
	}
	
	
	public LatLng getLastLocation() {
		if (mLocationClient.isConnected()){
			Location loc = mLocationClient.getLastLocation();
			if (loc != null){
				LatLng mCurrentLocation = new LatLng(loc.getLatitude(), loc.getLongitude());
				savedLastLocation = mCurrentLocation;
				return mCurrentLocation;
			}
		}
		if (savedLastLocation != null){
			return savedLastLocation;
		}
		return null;
	}

    /*
     * Called by Location Services if the attempt to
     * Location Services fails.
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        /*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(
                        getActivity(),
                        OTPApp.CONNECTION_FAILURE_RESOLUTION_REQUEST_CODE);
                /*
                 * Thrown if Google Play services canceled the original
                 * PendingIntent
                 */
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
			AlertDialog.Builder errorPlay = new AlertDialog.Builder(getActivity());
			errorPlay.setTitle("Play Services Error ")
					.setMessage(getResources().getString(R.string.play_services_error) + connectionResult.getErrorCode())
					.setNeutralButton("OK", null)
					.create()
					.show();
        }
    }

	@Override
	public void onConnected(Bundle connectionHint) {
		
        //mLocationClient.requestLocationUpdates(mLocationRequest, this);
		if (appStarts){
			appStarts = false;
			LatLng mCurrentLatLng = getLastLocation();	
			
			if (mCurrentLatLng != null){
				if (prefs.getBoolean(OTPApp.PREFERENCE_KEY_AUTO_DETECT_SERVER, true) && needToRunAutoDetect) {
					runAutoDetectServer(getLastLocation());
				}
				else{
					Server selectedServer = app.getSelectedServer();	
					if ((selectedServer != null) && selectedServer.areBoundsSet()){
						if (LocationUtil.checkPointInBoundingBox(mCurrentLatLng, selectedServer, OTPApp.CHECK_BOUNDS_ACCEPTABLE_ERROR)){
							mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mCurrentLatLng, OTPApp.defaultInitialZoomLevel));
						}
						else{
							LatLng serverCenter = new LatLng(selectedServer.getCenterLatitude(), selectedServer.getCenterLongitude());
							mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(serverCenter, OTPApp.defaultInitialZoomLevel));	
							setMarker(true, serverCenter, false);
						}
					}
					else{
						mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mCurrentLatLng, OTPApp.defaultInitialZoomLevel));
					}
				}
			}
		}	
	}

	@Override
	public void onDisconnected() {		
	}

	@Override
	public void onLocationChanged(Location location) {
        LatLng mCurrentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
		Server selectedServer = app.getSelectedServer();	
        
        if ((mCurrentLatLng != null) && (selectedServer != null) && (LocationUtil.checkPointInBoundingBox(mCurrentLatLng, selectedServer, OTPApp.CHECK_BOUNDS_ACCEPTABLE_ERROR))){
			mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mCurrentLatLng, OTPApp.defaultInitialZoomLevel));
		}
		else if ((selectedServer != null) && selectedServer.areBoundsSet()){
			LatLng serverCenter = new LatLng(selectedServer.getCenterLatitude(), selectedServer.getCenterLongitude());
			mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(serverCenter, OTPApp.defaultInitialZoomLevel));
			setMarker(true, serverCenter, false);
		}

        String msg = "Updated Location: " +
                Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude());
        Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show();
		
	}

	public void addBoundariesRectangle(Server server){
		List<LatLng> bounds = new ArrayList<LatLng>();
		bounds.add(new LatLng(server.getLowerLeftLatitude(), server.getLowerLeftLongitude()));
		bounds.add(new LatLng(server.getLowerLeftLatitude(), server.getUpperRightLongitude()));
		bounds.add(new LatLng(server.getUpperRightLatitude(), server.getUpperRightLongitude()));
		bounds.add(new LatLng(server.getUpperRightLatitude(), server.getLowerLeftLongitude()));
		bounds.add(new LatLng(server.getLowerLeftLatitude(), server.getLowerLeftLongitude()));

		PolylineOptions boundariesPolylineOptions = new PolylineOptions()
						 .addAll(bounds)
						 .color(Color.GRAY);
		boundariesPolyline = mMap.addPolyline(boundariesPolylineOptions);
	}

	@Override
	public void onCameraChange(CameraPosition position) {
		float newBearing;
		float newTilt;
		boolean showButton = false;
		boolean bearingTiltChanged = false;
		
		if ((newBearing = position.bearing) != bearing){
			bearingTiltChanged = true;
			bearing = newBearing;
			if (newBearing != 0){
				showButton = true;
			}
		}
		if ((newTilt = position.tilt) != tilt){
			bearingTiltChanged = true;
			tilt = newTilt;
			if (newTilt != 0){
				showButton = true;
			}
		}
		
		if (bearingTiltChanged){
			if (showButton){
				if (!btnCompass.isShown()){
					btnCompass.setVisibility(View.VISIBLE);
				}
			}
			else{
				if (btnCompass.isShown()){
					btnCompass.setVisibility(View.INVISIBLE);
				}			
			}
		}
	}
	
}