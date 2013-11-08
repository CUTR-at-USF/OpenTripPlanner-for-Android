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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.miscwidgets.widget.Panel;
import org.opentripplanner.api.ws.GraphMetadata;
import org.opentripplanner.api.ws.Request;
import org.opentripplanner.routing.core.OptimizeType;
import org.opentripplanner.routing.core.TraverseMode;
import org.opentripplanner.routing.core.TraverseModeSet;
import org.opentripplanner.v092snapshot.api.model.Itinerary;
import org.opentripplanner.v092snapshot.api.model.Leg;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.DrawerLayout.DrawerListener;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;
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
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
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
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
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
import edu.usf.cutr.opentripplanner.android.listeners.DateCompleteListener;
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
import edu.usf.cutr.opentripplanner.android.util.DateTimeDialog;
import edu.usf.cutr.opentripplanner.android.util.ItineraryDecrypt;
import edu.usf.cutr.opentripplanner.android.util.LocationUtil;
import edu.usf.cutr.opentripplanner.android.util.RangeSeekBar;
import edu.usf.cutr.opentripplanner.android.util.RangeSeekBar.OnRangeSeekBarChangeListener;
import edu.usf.cutr.opentripplanner.android.util.RightDrawableOnTouchListener;

/**
 * Main UI screen of the app, showing the map.
 * 
 * @author Khoa Tran
 */

public class MainFragment extends Fragment implements
		ServerSelectorCompleteListener,
		TripRequestCompleteListener, MetadataRequestCompleteListener,
		OTPGeocodingListener, LocationListener,
		DateCompleteListener, OnRangeSeekBarChangeListener<Double>,
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
	private ImageButton btnDateDialog;
	private LatLng savedLastLocation;
	private LatLng savedLastLocationCheckedForServer;

	private Address startAddress;
	private Address endAddress;
	
	private String resultTripStartLocation;
	private String resultTripEndLocation;
	
	private View panelDisplayDirection;
	
	private Spinner itinerarySelectionSpinner;
	
	private RangeSeekBar<Double> bikeTriangleParameters;
	private ViewGroup bikeTriangleParametersLayout;	
	
	private ViewGroup handleDrawer;	
	private ViewGroup navigationDrawerLeftPane;	

	private boolean appStarts = true;
	
	private boolean isStartLocationGeocodingProcessed = false;
	private boolean isEndLocationGeocodingProcessed = false;
	
	private boolean isStartLocationChangedByUser = true;
	private boolean isEndLocationChangedByUser = true;
	
	private boolean requestTripAfterStartGeocoding = false;
	private boolean requestTripAfterEndGeocoding = false;
	private boolean requestTripAfterStartEndGeocoding = false;
	private boolean geocodingBeenRequested = false;
	
	private Context applicationContext;
	
	private boolean mapFailed;


	public LatLng getSavedLastLocation() {
		return savedLastLocation;
	}

	Panel directionPanel;

	private ImageButton btnDisplayDirection;
	
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

	private Boolean needToUpdateServersList = false;
	
	public Boolean getNeedToUpdateServersList() {
		return needToUpdateServersList;
	}

	public void setNeedToUpdateServersList(Boolean needToUpdateServersList) {
		this.needToUpdateServersList = needToUpdateServersList;
	}
	
	private Boolean needToRunAutoDetect = false;
	
	public Boolean getNeedToRunAutoDetect() {
		return needToRunAutoDetect;
	}

	public void setNeedToRunAutoDetect(Boolean needToRunAutoDetect) {
		this.needToRunAutoDetect = needToRunAutoDetect;
	}
	
	private boolean appResumed;

	private OnFragmentListener fragmentListener;

	private boolean isRealLostFocus = true;
	
	public static final String TAG = "OTP";

	private float maxZoomLevel;
	
	private Date tripDate;

	private boolean arriveBy;
	
	private double bikeTriangleMinValue = OTPApp.BIKE_PARAMETERS_QUICK_DEFAULT_VALUE; 
	private double bikeTriangleMaxValue = OTPApp.BIKE_PARAMETERS_FLAT_DEFAULT_VALUE;


	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			((MyActivity) activity).setDateCompleteCallback(this);
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
		
		final View mainView = inflater.inflate(R.layout.main, container, false);
		
		ViewTreeObserver vto = mainView.getViewTreeObserver(); 
		vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() { 
		    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
			@Override 
		    public void onGlobalLayout() { 
		        MainFragment.removeOnGlobalLayoutListener(mainView, this);
				int locationtbEndLocation[] = new int[2];
				tbEndLocation.getLocationInWindow(locationtbEndLocation);
				int locationItinerarySelectionSpinner[] = new int[2];
				itinerarySelectionSpinner.getLocationInWindow(locationItinerarySelectionSpinner);
				int locationbtnHandle[] = new int[2];
				btnHandle.getLocationInWindow(locationbtnHandle);
				DisplayMetrics metrics = MainFragment.this.getResources().getDisplayMetrics();
				int windowHeight = metrics.heightPixels;
				int paddingMargin = MainFragment.this.getResources().getInteger(R.integer.map_padding_margin);
				if (mMap != null){
					mMap.setPadding(locationbtnHandle[0] + btnHandle.getWidth()/2 + paddingMargin,
							locationtbEndLocation[1] + tbEndLocation.getHeight()/2 + paddingMargin,
							0,
							windowHeight - locationItinerarySelectionSpinner[1] + paddingMargin);
				}
		    } 
		});
				
		tbStartLocation = (EditText) mainView
				.findViewById(R.id.tbStartLocation);
		tbEndLocation = (EditText) mainView.findViewById(R.id.tbEndLocation);
		
		btnPlanTrip = (ImageButton) mainView.findViewById(R.id.btnPlanTrip);
		ddlOptimization = (ListView) mainView
				.findViewById(R.id.spinOptimization);
		ddlTravelMode = (ListView) mainView.findViewById(R.id.spinTravelMode);

		bikeTriangleParameters = new RangeSeekBar<Double>(OTPApp.BIKE_PARAMETERS_MIN_VALUE, OTPApp.BIKE_PARAMETERS_MAX_VALUE, this.getActivity().getApplicationContext(), R.color.sysRed, R.color.sysGreen, R.color.sysBlue, R.drawable.seek_thumb_normal, R.drawable.seek_thumb_pressed);
		
		// add RangeSeekBar to pre-defined layout
		bikeTriangleParametersLayout = (ViewGroup) mainView.findViewById(R.id.bikeParametersLayout);

		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.BELOW, R.id.bikeParametersTags);
				
		bikeTriangleParametersLayout.addView(bikeTriangleParameters, params);
		
		btnMyLocation = (ImageButton) mainView.findViewById(R.id.btnMyLocation);
		
		btnDateDialog = (ImageButton) mainView.findViewById(R.id.btnDateDialog);
		
		btnDisplayDirection = (ImageButton) mainView
				.findViewById(R.id.btnDisplayDirection);
		
		handleDrawer = (ViewGroup) mainView.findViewById(R.id.handleDrawer);;	
		navigationDrawerLeftPane = (ViewGroup) mainView.findViewById(R.id.navigationDrawerLeftPane);
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
	
	
	@SuppressWarnings("deprecation")
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private static void removeOnGlobalLayoutListener(View v, ViewTreeObserver.OnGlobalLayoutListener listener){
	    if (Build.VERSION.SDK_INT < 16) {
	        v.getViewTreeObserver().removeGlobalOnLayoutListener(listener);
	    } else {
	        v.getViewTreeObserver().removeOnGlobalLayoutListener(listener);
	    }
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		Log.v(TAG, "onActivityCreated");
		super.onActivityCreated(savedInstanceState);
		
		applicationContext = getActivity().getApplicationContext();
		 
        mMap = retrieveMap(mMap);

		app = ((OTPApp) getActivity().getApplication());
		
		prefs = PreferenceManager.getDefaultSharedPreferences(
				applicationContext);
		
		locationManager = (LocationManager) getActivity()
				.getSystemService(Context.LOCATION_SERVICE);
		
		mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(OTPApp.UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(OTPApp.FASTEST_INTERVAL);

		
		if (savedInstanceState == null){
			SharedPreferences.Editor prefsEditor = prefs.edit();
			prefsEditor.putBoolean(OTPApp.PREFERENCE_KEY_ORIGIN_IS_MY_LOCATION, true);
			prefsEditor.putBoolean(OTPApp.PREFERENCE_KEY_DESTINATION_IS_MY_LOCATION, false);
			prefsEditor.commit();
			bikeTriangleParameters.setSelectedMinValue(OTPApp.BIKE_PARAMETERS_QUICK_DEFAULT_VALUE);
			bikeTriangleParameters.setSelectedMaxValue(OTPApp.BIKE_PARAMETERS_FLAT_DEFAULT_VALUE);
		}
		
		if (!mapFailed){
			if (prefs.getBoolean(OTPApp.PREFERENCE_KEY_SELECTED_CUSTOM_SERVER, false)){
				String baseURL = prefs.getString(OTPApp.PREFERENCE_KEY_CUSTOM_SERVER_URL, "");
				Server s = new Server(baseURL, applicationContext);
				String bounds;
				setSelectedServer(s, false);
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
					Server s = dataSource.getServer(prefs.getLong(OTPApp.PREFERENCE_KEY_SELECTED_SERVER, 0));
					dataSource.close();
					
					if (s != null){
						setSelectedServer(s, false);
						addBoundariesRectangle(s);
						Log.v(TAG, "Now using OTP server: " + s.getRegion());
					}
				}
			}
		}
		
		ArrayAdapter<OptimizeSpinnerItem> optimizationAdapter = new ArrayAdapter<OptimizeSpinnerItem>(
				getActivity(),
				android.R.layout.simple_list_item_single_choice,
				new OptimizeSpinnerItem[] {
						new OptimizeSpinnerItem(getResources().getString(R.string.optimization_quick), OptimizeType.QUICK),
						new OptimizeSpinnerItem(getResources().getString(R.string.optimization_safe), OptimizeType.SAFE),
						new OptimizeSpinnerItem(getResources().getString(R.string.optimization_fewest_transfers),
								OptimizeType.TRANSFERS) });
		ddlOptimization.setAdapter(optimizationAdapter);

		ArrayAdapter<TraverseModeSpinnerItem> traverseModeAdapter = new ArrayAdapter<TraverseModeSpinnerItem>(
				getActivity(), android.R.layout.simple_list_item_single_choice,
				new TraverseModeSpinnerItem[] {
						new TraverseModeSpinnerItem(getResources().getString(R.string.mode_transit),
								new TraverseModeSet(TraverseMode.TRANSIT,
										TraverseMode.WALK)),
						new TraverseModeSpinnerItem(getResources().getString(R.string.mode_bus),
								new TraverseModeSet(TraverseMode.BUSISH,
										TraverseMode.WALK)),
						new TraverseModeSpinnerItem(getResources().getString(R.string.mode_train),
								new TraverseModeSet(TraverseMode.TRAINISH,
										TraverseMode.WALK)), // not sure
						new TraverseModeSpinnerItem(getResources().getString(R.string.mode_walk),
								new TraverseModeSet(TraverseMode.WALK)),
						new TraverseModeSpinnerItem(getResources().getString(R.string.mode_bicycle),
								new TraverseModeSet(TraverseMode.BICYCLE)),
						new TraverseModeSpinnerItem(getResources().getString(R.string.mode_transit_bicycle),
								new TraverseModeSet(TraverseMode.TRANSIT,
										TraverseMode.BICYCLE)) });
		ddlTravelMode.setAdapter(traverseModeAdapter);	
		
		
		Server selectedServer = app.getSelectedServer();	
		if (selectedServer != null){
			if (!mapFailed){
				mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(getServerCenter(selectedServer), getServerInitialZoom(selectedServer)));			
			}
		}

		restoreState(savedInstanceState);
		
			
		if (savedInstanceState == null){
			ddlOptimization.setItemChecked(0, true);
			ddlTravelMode.setItemChecked(0, true);
			showBikeParameters(false);
			arriveBy = false;
			setTextBoxLocation(getResources().getString(R.string.my_location), true);
		}
			
		if (!mapFailed){
			initializeMapInterface(mMap);
		}
	}
	
	
	private void initializeMapInterface(GoogleMap mMap){
		UiSettings uiSettings = mMap.getUiSettings();
		mMap.setMyLocationEnabled(true);
		mMap.setOnCameraChangeListener(this);
		uiSettings.setMyLocationButtonEnabled(false);
		uiSettings.setCompassEnabled(true);
		uiSettings.setAllGesturesEnabled(true);
		uiSettings.setZoomControlsEnabled(false);
		
		String overlayString = prefs.getString(OTPApp.PREFERENCE_KEY_MAP_TILE_SOURCE, applicationContext.getResources().getString(R.string.map_tiles_default_server));
		updateOverlay(overlayString);
		
		addMapListeners();
		addInterfaceListeners();
	}
	
	
	private void addInterfaceListeners(){
		
		DrawerListener dl = new DrawerListener() {
			@Override
			public void onDrawerStateChanged(int arg0) {
			}
			
			@Override
			public void onDrawerSlide(View arg0, float arg1) {
				
				InputMethodManager imm = (InputMethodManager) MainFragment.this.getActivity()
						.getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(tbEndLocation.getWindowToken(), 0);
				imm.hideSoftInputFromWindow(tbStartLocation.getWindowToken(), 0);
			}
			
			@Override
			public void onDrawerOpened(View arg0) {
				tbStartLocation.clearFocus();
				tbEndLocation.clearFocus();				
			}
			
			@Override
			public void onDrawerClosed(View arg0) {
			}
		};
		drawerLayout.setDrawerListener(dl);
		
		OnTouchListener otlStart = new RightDrawableOnTouchListener(tbStartLocation) {
			@Override
			public boolean onDrawableTouch(final MotionEvent event) {
				// mBoundService.updateNotification();

				final CharSequence[] items = { getResources().getString(R.string.location_type_current_location),
						getResources().getString(R.string.location_type_contact), getResources().getString(R.string.location_type_map_point) };

				AlertDialog.Builder builder = new AlertDialog.Builder(MainFragment.this.getActivity());
				builder.setTitle(getResources().getString(R.string.choose_location_type_start));
				builder.setItems(items, new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int item) {
						if (items[item].equals(getResources().getString(R.string.location_type_current_location))) {	
					/*		myActivity = (MyActivity) activity;
							myActivity.getmLocationClient();
							Location loc = this.MainFragment.getmLocationClient().getLastLocation();*/
							LatLng mCurrentLatLng = getLastLocation();
							if (mCurrentLatLng != null){
								SharedPreferences.Editor prefsEditor = prefs.edit();
								setTextBoxLocation(getResources().getString(R.string.my_location), true);
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
							
							
						} else if (items[item].equals(getResources().getString(R.string.location_type_contact))) {
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

				final CharSequence[] items = { getResources().getString(R.string.location_type_current_location),
						getResources().getString(R.string.location_type_contact), getResources().getString(R.string.location_type_map_point) };

				AlertDialog.Builder builder = new AlertDialog.Builder(MainFragment.this.getActivity());
				builder.setTitle(getResources().getString(R.string.choose_location_type_end));
				builder.setItems(items, new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int item) {
						if (items[item].equals(getResources().getString(R.string.location_type_current_location))) {	
					/*		myActivity = (MyActivity) activity;
							myActivity.getmLocationClient();
							Location loc = this.MainFragment.getmLocationClient().getLastLocation();*/
							LatLng mCurrentLatLng = getLastLocation();
							if (mCurrentLatLng != null){
								SharedPreferences.Editor prefsEditor = prefs.edit();
								setTextBoxLocation(getResources().getString(R.string.my_location), false);
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
							
							
						} else if (items[item].equals(getResources().getString(R.string.location_type_contact))) {
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
				processRequestTrip();
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
					
					if (!TextUtils.isEmpty(text)){
						if (v.getId() == R.id.tbStartLocation 
								&& !isStartLocationGeocodingProcessed 
								&& !prefs.getBoolean(OTPApp.PREFERENCE_KEY_ORIGIN_IS_MY_LOCATION, true)) {
							processAddress(true, tv.getText().toString(), false);
						} else if (v.getId() == R.id.tbEndLocation 
								&& !isEndLocationGeocodingProcessed
								&& !prefs.getBoolean(OTPApp.PREFERENCE_KEY_DESTINATION_IS_MY_LOCATION, true)) {
							processAddress(false, tv.getText().toString(), false);
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
				if (v.getId() == R.id.tbStartLocation){
						if (actionId == EditorInfo.IME_ACTION_NEXT
						|| (event != null
								&& event.getAction() == KeyEvent.ACTION_DOWN && event
								.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
							isRealLostFocus = false;
							if (!isStartLocationGeocodingProcessed
									&& !prefs.getBoolean(OTPApp.PREFERENCE_KEY_ORIGIN_IS_MY_LOCATION, true)){
								geocodingBeenRequested = true;
								processAddress(true, v.getText().toString(), false);
							}
						}
				} else if (v.getId() == R.id.tbEndLocation){
						if (actionId == EditorInfo.IME_ACTION_DONE
						|| (event != null
								&& event.getAction() == KeyEvent.ACTION_DOWN && event
								.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
							processRequestTrip();
						}
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
		
		OnClickListener oclDateDialog = new OnClickListener() {
			@Override
			public void onClick(View arg0) {
			    FragmentTransaction ft = MainFragment.this.getActivity().getSupportFragmentManager().beginTransaction();
			    Fragment prev = MainFragment.this.getActivity().getSupportFragmentManager().findFragmentByTag(OTPApp.TAG_FRAGMENT_DATE_TIME_DIALOG);
			    if (prev != null) {
			        ft.remove(prev);
			    }
			    ft.addToBackStack(null);

			    // Create and show the dialog.
			    DateTimeDialog newFragment = new DateTimeDialog();
			    
			    Date dateDialogDate;
			    if (tripDate == null){
			    	dateDialogDate = Calendar.getInstance().getTime();
			    }
			    else{
			    	dateDialogDate = tripDate;
			    }
			    
				Bundle bundle = new Bundle();
				bundle.putSerializable(OTPApp.BUNDLE_KEY_TRIP_DATE, dateDialogDate);
				bundle.putBoolean(OTPApp.BUNDLE_KEY_ARRIVE_BY, arriveBy);
				newFragment.setArguments(bundle);
				ft.commit();

				newFragment.show(MainFragment.this.getActivity().getSupportFragmentManager(), OTPApp.TAG_FRAGMENT_DATE_TIME_DIALOG);
		    }
		};
		btnDateDialog.setOnClickListener(oclDateDialog);
		
		AdapterView.OnItemSelectedListener itinerarySpinnerListener = new AdapterView.OnItemSelectedListener() {
		    @Override
		    public void  onItemSelected (AdapterView<?> parent, View view, int position, long id){
//		    	Toast.makeText(parent.getContext(), 
//		    				   Long.toString(id) + " chosen " +
//		    				   parent.getItemAtPosition(position).toString(), 
//		    				   Toast.LENGTH_SHORT).show();
		    	fragmentListener.onItinerarySelected(position);
		    	
	    		if (!appResumed){
			    	showRouteOnMap(fragmentListener.getCurrentItinerary(), true);
	    		}
	    		else{
			    	showRouteOnMap(fragmentListener.getCurrentItinerary(), false);
			    	appResumed = false;
	    		}
		    }

		    @Override
		    public void onNothingSelected (AdapterView<?> parent) {
		    	
		    }
		};
		int currentItineraryIndex = fragmentListener.getCurrentItineraryIndex();

		itinerarySelectionSpinner.setSelection(currentItineraryIndex);
		itinerarySelectionSpinner.setOnItemSelectedListener(itinerarySpinnerListener);
		

		
		bikeTriangleParameters.setOnRangeSeekBarChangeListener(new OnRangeSeekBarChangeListener<Double>() {
	        @Override
	        public void onRangeSeekBarValuesChanged(RangeSeekBar<?> rangeSeekBar, Double minValue, Double maxValue) {
	                // handle changed range values
	                Log.i(TAG, "User selected new range values: MIN=" + minValue + ", MAX=" + maxValue);
	        }

		});
		
		ddlTravelMode.setOnItemClickListener(new OnItemClickListener() {
	        public void onItemClick(AdapterView<?> parent, View view,
	                int position, long id) { 
	        	TraverseModeSpinnerItem traverseModeSpinnerItem = (TraverseModeSpinnerItem) ddlTravelMode.getItemAtPosition(position);			
	        	if (traverseModeSpinnerItem.getTraverseModeSet().contains(TraverseMode.BICYCLE)){
		    		setBikeOptimizationAdapter(true);
		    		showBikeParameters(true);
	        	}
	        	else{
		    		setBikeOptimizationAdapter(false);
		    		showBikeParameters(false);
	        	}
	        }
	    });
		
		ddlOptimization.setOnItemClickListener(new OnItemClickListener() {
	        public void onItemClick(AdapterView<?> parent, View view,
	                int position, long id) {

	        	OptimizeSpinnerItem optimizeSpinnerItem = (OptimizeSpinnerItem) ddlOptimization.getItemAtPosition(position);				
	        	showBikeParameters(optimizeSpinnerItem.getOptimizeType().equals(OptimizeType.TRIANGLE));

	        }
	    });
		
		bikeTriangleParameters.setOnRangeSeekBarChangeListener(this);
	}
	
	/**
	 * Wrapper to call request trip, triggering geocoding processes if it's
	 * necessary.
	 */
	private void processRequestTrip(){
		isRealLostFocus = false;
		if (!isEndLocationGeocodingProcessed
				&& !prefs.getBoolean(OTPApp.PREFERENCE_KEY_DESTINATION_IS_MY_LOCATION, true)
				&& !geocodingBeenRequested){
			requestTripAfterEndGeocoding = true;
			geocodingBeenRequested = true;
			processAddress(false, tbEndLocation.getText().toString(), false);
		}
		else if (!isStartLocationGeocodingProcessed
				&& !prefs.getBoolean(OTPApp.PREFERENCE_KEY_ORIGIN_IS_MY_LOCATION, true)
				&& !geocodingBeenRequested){
			requestTripAfterStartGeocoding = true;
			geocodingBeenRequested = true;
			processAddress(true, tbStartLocation.getText().toString(), false);
		}
		else if (!isStartLocationGeocodingProcessed
				&& !prefs.getBoolean(OTPApp.PREFERENCE_KEY_ORIGIN_IS_MY_LOCATION, true)
				&& !isEndLocationGeocodingProcessed
				&& !prefs.getBoolean(OTPApp.PREFERENCE_KEY_DESTINATION_IS_MY_LOCATION, true)
				&& !geocodingBeenRequested){
			requestTripAfterStartGeocoding = true;
			requestTripAfterEndGeocoding = true;
			requestTripAfterStartEndGeocoding = true;
			geocodingBeenRequested = true;
			processAddress(true, tbStartLocation.getText().toString(), false);
			processAddress(false, tbEndLocation.getText().toString(), false);
		}
		else{
			requestTrip();
		}	
	}
	
	/**
	 * Sends information of the text boxes to fragment listener class through a
	 * bundle.
	 * <p>
	 * Fragment listener provides intercommunication with other fragments or classes.
	 */
	private void saveOTPBundle() {
		OTPBundle bundle = new OTPBundle();
		bundle.setFromText(resultTripStartLocation);
		bundle.setToText(resultTripEndLocation);

		this.getFragmentListener().setOTPBundle(bundle);
	}


	private void addMapListeners(){
		OnMapClickListener omcl = new OnMapClickListener() {
			@Override
			public void onMapClick(LatLng latlng) {
				InputMethodManager imm = (InputMethodManager) MainFragment.this.getActivity()
						.getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(tbEndLocation.getWindowToken(), 0);
				imm.hideSoftInputFromWindow(tbStartLocation.getWindowToken(), 0);
				
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
			
			@Override
			public void onMarkerDrag(Marker marker) {
			}

			@Override
			public void onMarkerDragEnd(Marker marker) {	
				LatLng markerLatlng = marker.getPosition();

				if (((app.getSelectedServer() != null) && LocationUtil.checkPointInBoundingBox(markerLatlng, app.getSelectedServer(), OTPApp.CHECK_BOUNDS_ACCEPTABLE_ERROR)) 
						|| (app.getSelectedServer() == null)){
					if ((startMarker != null) && (marker.hashCode() == startMarker.hashCode())){
						if (prefs.getBoolean(OTPApp.PREFERENCE_KEY_USE_INTELLIGENT_MARKERS, true)){
							updateMarkerPosition(markerLatlng, true);
						}
						else{
							isStartLocationGeocodingProcessed = true;
						}
						startMarkerPosition = markerLatlng;
					}
					else if ((endMarker != null) && (marker.hashCode() == endMarker.hashCode())){
						if (prefs.getBoolean(OTPApp.PREFERENCE_KEY_USE_INTELLIGENT_MARKERS, true)){
							updateMarkerPosition(markerLatlng, false);
						}
						else{
							isEndLocationGeocodingProcessed = true;
						}
						endMarkerPosition = markerLatlng;
					}
				}
				else{

					if ((startMarker != null) && (marker.hashCode() == startMarker.hashCode())){
						marker.setPosition(startMarkerPosition);
					}
					else{
						marker.setPosition(endMarkerPosition);
					}
					Toast.makeText(applicationContext, applicationContext.getResources().getString(R.string.marker_out_of_boundaries), Toast.LENGTH_SHORT).show();
				}
			}
			
			@Override
			public void onMarkerDragStart(Marker marker) {
				InputMethodManager imm = (InputMethodManager) MainFragment.this.getActivity()
						.getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(tbEndLocation.getWindowToken(), 0);
				imm.hideSoftInputFromWindow(tbStartLocation.getWindowToken(), 0);
			}
		};
		mMap.setOnMarkerDragListener(omdl);
		
		OnMapLongClickListener omlcl = new OnMapLongClickListener() {
			@Override
			public void onMapLongClick(LatLng latlng) {
				InputMethodManager imm = (InputMethodManager) MainFragment.this.getActivity()
						.getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(tbEndLocation.getWindowToken(), 0);
				imm.hideSoftInputFromWindow(tbStartLocation.getWindowToken(), 0);
				
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
				drawerLayout.openDrawer(Gravity.LEFT);
			}
		};
		btnHandle.setOnClickListener(oclH);
		
		OnInfoWindowClickListener omliwcl = new OnInfoWindowClickListener() {
			
			@Override
			public void onInfoWindowClick(Marker modeMarker) {
				saveOTPBundle();
				OTPBundle otpBundle = getFragmentListener().getOTPBundle();
				Matcher matcher = Pattern.compile("\\d+").matcher(modeMarker.getTitle());
				if(matcher.find()){
					String numberString = modeMarker.getTitle().substring(0, matcher.end());
					//Step indexes shown to the user are in a scale starting by 1 but instructions steps internally start by 0
					int currentStepIndex = Integer.parseInt(numberString) - 1;
					otpBundle.setCurrentStepIndex(currentStepIndex);
					otpBundle.setFromInfoWindow(true);
					getFragmentListener().setOTPBundle(otpBundle);
					getFragmentListener().onSwitchedToDirectionFragment();		
				}
							
			}
		};
		mMap.setOnInfoWindowClickListener(omliwcl);
	}
	
	
	private void restoreState(Bundle savedInstanceState){
		if (savedInstanceState != null){
			mMap = retrieveMap(mMap);
			
			if (!mapFailed){
				boolean mapFailedBefore = savedInstanceState.getBoolean(OTPApp.BUNDLE_KEY_MAP_FAILED);
				
				if (mapFailedBefore){
					enableUIElements(true);
					
					initializeMapInterface(mMap);
				}

	    		if (!mapFailedBefore){
					String overlayString = prefs.getString(OTPApp.PREFERENCE_KEY_MAP_TILE_SOURCE, applicationContext.getResources().getString(R.string.map_tiles_default_server));
					updateOverlay(overlayString);
	    		}
				
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
				
				savedLastLocation = savedInstanceState.getParcelable(OTPApp.BUNDLE_KEY_SAVED_LAST_LOCATION);
				savedLastLocationCheckedForServer = savedInstanceState.getParcelable(OTPApp.BUNDLE_KEY_SAVED_LAST_LOCATION_CHECKED_FOR_SERVER);

	    		showBikeParameters(false);
	    		
				ddlTravelMode.setItemChecked(savedInstanceState.getInt(OTPApp.BUNDLE_KEY_DDL_TRAVEL_MODE), true);
	        	TraverseModeSpinnerItem traverseModeSpinnerItem = (TraverseModeSpinnerItem) ddlTravelMode.getItemAtPosition(ddlTravelMode.getCheckedItemPosition());			
	        	if (traverseModeSpinnerItem.getTraverseModeSet().contains(TraverseMode.BICYCLE)){
		    		setBikeOptimizationAdapter(true);
					ddlOptimization.setItemChecked(savedInstanceState.getInt(OTPApp.BUNDLE_KEY_DDL_OPTIMIZATION), true);
		    		OptimizeSpinnerItem optimizeSpinnerItem = (OptimizeSpinnerItem) ddlOptimization.getItemAtPosition(ddlOptimization.getCheckedItemPosition());
		    		if (optimizeSpinnerItem.getOptimizeType().equals(OptimizeType.TRIANGLE)){
			    		showBikeParameters(true);
		    		}
	        	}
				ddlTravelMode.setItemChecked(savedInstanceState.getInt(OTPApp.BUNDLE_KEY_DDL_TRAVEL_MODE), true);

				OTPBundle otpBundle = (OTPBundle) savedInstanceState.getSerializable(OTPApp.BUNDLE_KEY_OTP_BUNDLE);
				if (otpBundle != null){
					List<Itinerary> itineraries = otpBundle.getItineraryList(); 
					getFragmentListener().onItinerariesLoaded(itineraries);
					getFragmentListener().onItinerarySelected(otpBundle.getCurrentItineraryIndex());
					fillItinerariesSpinner(itineraries);
				}
				showRouteOnMap(getFragmentListener().getCurrentItinerary(), false);
				
				Date savedTripDate = (Date) savedInstanceState.getSerializable(OTPApp.BUNDLE_KEY_TRIP_DATE);
				if (savedTripDate != null){
					tripDate = savedTripDate;
				}
				arriveBy = savedInstanceState.getBoolean(OTPApp.BUNDLE_KEY_ARRIVE_BY, false);
				
				if (savedInstanceState.getString(OTPApp.BUNDLE_KEY_RESULT_TRIP_START_LOCATION) != null){
					resultTripStartLocation = savedInstanceState.getString(OTPApp.BUNDLE_KEY_RESULT_TRIP_START_LOCATION);
				}
				if (savedInstanceState.getString(OTPApp.BUNDLE_KEY_RESULT_TRIP_END_LOCATION) != null){
					resultTripEndLocation = savedInstanceState.getString(OTPApp.BUNDLE_KEY_RESULT_TRIP_END_LOCATION);
				}
				
				bikeTriangleMinValue = savedInstanceState.getDouble(OTPApp.BUNDLE_KEY_SEEKBAR_MIN_VALUE);
				bikeTriangleMaxValue = savedInstanceState.getDouble(OTPApp.BUNDLE_KEY_SEEKBAR_MAX_VALUE);
				bikeTriangleParameters.setSelectedMinValue(bikeTriangleMinValue);
				bikeTriangleParameters.setSelectedMaxValue(bikeTriangleMaxValue);

				isStartLocationChangedByUser = false;
				isEndLocationChangedByUser = false;
			}
		}
	}
	
	/**
	 * Activates/disactivates all the UI, avoiding to take care of the possible
	 * listeners functions if the application is in a non working state.
	 * 
	 * @param enable if true elements will be enabled
	 */
	private void enableUIElements(boolean enable){
		int visibility;
		if (enable){
			setHasOptionsMenu(true);
			visibility = View.VISIBLE;
		}
		else{
			setHasOptionsMenu(false);
			visibility = View.INVISIBLE;
		}
		tbStartLocation.setVisibility(visibility);
		tbEndLocation.setVisibility(visibility);
		btnPlanTrip.setVisibility(visibility);
		btnDateDialog.setVisibility(visibility);
		btnMyLocation.setVisibility(visibility);
		handleDrawer.setVisibility(visibility);
		panelDisplayDirection.setVisibility(visibility);
		navigationDrawerLeftPane.setVisibility(visibility);
	}
	
	private void requestTrip(){
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
			request.setFrom(URLEncoder.encode(startLocationString, OTPApp.URL_ENCODING));
			request.setTo(URLEncoder.encode(endLocationString, OTPApp.URL_ENCODING));
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		
		
		request.setArriveBy(arriveBy);
		
		OptimizeSpinnerItem optimizeSpinnerItem = (OptimizeSpinnerItem) ddlOptimization.getItemAtPosition(ddlOptimization.getCheckedItemPosition());
		if (optimizeSpinnerItem == null){
			optimizeSpinnerItem = (OptimizeSpinnerItem) ddlOptimization.getItemAtPosition(0);
		}
		
		request.setOptimize(optimizeSpinnerItem.getOptimizeType());
		
		if (optimizeSpinnerItem.getOptimizeType().equals(OptimizeType.TRIANGLE)){
			request.setTriangleTimeFactor(bikeTriangleMinValue);
			request.setTriangleSlopeFactor(bikeTriangleMaxValue - bikeTriangleMinValue);
			request.setTriangleSafetyFactor(1 - bikeTriangleMaxValue);
		}
		
		TraverseModeSpinnerItem traverseModeSpinnerItem = (TraverseModeSpinnerItem) ddlTravelMode.getItemAtPosition(ddlTravelMode.getCheckedItemPosition());				
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
		
		Date requestTripDate;
		if (tripDate == null){
			requestTripDate = Calendar.getInstance().getTime();
		}
		else{
			requestTripDate = tripDate;
		}

		request.setDateTime(
				DateFormat.format(OTPApp.FORMAT_OTP_SERVER_DATE_QUERY,
						requestTripDate.getTime()).toString(),
				DateFormat
						.format(OTPApp.FORMAT_OTP_SERVER_TIME_QUERY, requestTripDate.getTime()).toString());

		request.setShowIntermediateStops(Boolean.TRUE);
		
		WeakReference<Activity> weakContext = new WeakReference<Activity>(MainFragment.this.getActivity());

		new TripRequest(weakContext, MainFragment.this.applicationContext, app
				.getSelectedServer(), MainFragment.this)
				.execute(request);

		InputMethodManager imm = (InputMethodManager) MainFragment.this.getActivity()
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(tbEndLocation.getWindowToken(), 0);
		imm.hideSoftInputFromWindow(tbStartLocation.getWindowToken(), 0);
		
		tripDate = null;
	}
	
	/**
	 * Retrieves a map if the map fragment parameter is null.
	 * <p>
	 * If there is an error tries to solve it checking if it was because of
	 * "Google Play Services" sending the corresponding intent.
	 * 
	 * @param mMap map fragment to check if the map is already initialized
	 * @return initialized map fragment
	 */
	private GoogleMap retrieveMap(GoogleMap mMap) {
	    // Do a null check to confirm that we have not already instantiated the map.
		mapFailed = false;
		
	    if (mMap == null) {
	        mMap = ((SupportMapFragment) getFragmentManager().findFragmentById(R.id.map))
	                            .getMap();
	        // Check if we were successful in obtaining the map.
	        if (mMap == null) {
		        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(applicationContext);
		        
		        if(status!=ConnectionResult.SUCCESS){
		        	enableUIElements(false);
		            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, getActivity(), OTPApp.CHECK_GOOGLE_PLAY_REQUEST_CODE);
		            dialog.show();
		            mapFailed = true;
		        }	        
		    }

	    }
		
	    return mMap;
	}
	
	/**
	 * Wrapper to trigger functions to disable bike parameters and effectively
	 * show them as inactive (faded).
	 * 
	 * @param enable when true parameters are shown
	 */
	private void showBikeParameters(boolean enable){
		setRangeSeekBarStateColors(enable, bikeTriangleParameters);
		disableEnableControls(enable, bikeTriangleParametersLayout);
	}
	
	/**
	 * Changes optimization spinner values to show values compatibles with
	 * bikes or with transit. 
	 * <p>
	 * Replaces fewest transfers with safer trip options.
	 * 
	 * @param enable when true spinner is set to bike values
	 */
	private void setBikeOptimizationAdapter(boolean enable){
		ArrayAdapter<OptimizeSpinnerItem> optimizationAdapter;
		
		if (enable){
			optimizationAdapter = new ArrayAdapter<OptimizeSpinnerItem>(
					getActivity(),
					android.R.layout.simple_list_item_single_choice,
					new OptimizeSpinnerItem[] {
							new OptimizeSpinnerItem(getResources().getString(R.string.optimization_quick), OptimizeType.QUICK),
							new OptimizeSpinnerItem(getResources().getString(R.string.optimization_safe), OptimizeType.SAFE),
							new OptimizeSpinnerItem(getResources().getString(R.string.optimization_bike_triangle), OptimizeType.TRIANGLE) });
			ddlOptimization.setAdapter(optimizationAdapter);
			ddlOptimization.setItemChecked(2, true);
		}
		else {
			optimizationAdapter = new ArrayAdapter<OptimizeSpinnerItem>(
					getActivity(),
					android.R.layout.simple_list_item_single_choice,
					new OptimizeSpinnerItem[] {
							new OptimizeSpinnerItem(getResources().getString(R.string.optimization_quick), OptimizeType.QUICK),
							new OptimizeSpinnerItem(getResources().getString(R.string.optimization_safe), OptimizeType.SAFE),
							new OptimizeSpinnerItem(getResources().getString(R.string.optimization_fewest_transfers),
									OptimizeType.TRANSFERS) });
			ddlOptimization.setAdapter(optimizationAdapter);
			ddlOptimization.setItemChecked(0, true);
		}
	}
	
	/**
	 * Toggles between standard colors and faded colors for the passed seekbar
	 * to visually show that it's disabled.
	 * 
	 * @param enable when true standard colors are used
	 * @param seekBar bar that will be toggled
	 */
	private void setRangeSeekBarStateColors(boolean enable, RangeSeekBar<Double> seekBar){
		if (enable){
			seekBar.setLeftColor(getResources().getColor(R.color.sysRed));
			seekBar.setMiddleColor(getResources().getColor(R.color.sysGreen));
			seekBar.setRightColor(getResources().getColor(R.color.sysBlue));
		}
		else{
			seekBar.setLeftColor(getResources().getColor(R.color.sysRedFaded));
			seekBar.setMiddleColor(getResources().getColor(R.color.sysGreenFaded));
			seekBar.setRightColor(getResources().getColor(R.color.sysBlueFaded));
		}
		
	}
	
	/**
	 * Recursively enable/disable all the views contained in a ViewGroup and
	 * it's descendants. 
	 * 
	 * @param enable when true views will be disable
	 * @param vg a ViewGroup that will be modified
	 */
	private void disableEnableControls(boolean enable, ViewGroup vg){
	    for (int i = 0; i < vg.getChildCount(); i++){
	       View child = vg.getChildAt(i);
	       child.setEnabled(enable);
	       if (child instanceof ViewGroup){ 
	          disableEnableControls(enable, (ViewGroup)child);
	       }
	    }
	}


	/**
	 * Triggers ServerSelector task to retrieve servers list.
	 * <p>
	 * Server list will be downloaded or retrieved from the database.
	 * <p>
	 * A valid location should be passed to perform server autodetection if the
	 * preference is set. If location is null a toast will be displayed
	 * informing of the error.
	 * <p>
	 * It it's not possible or not requested to autodetect the server list will
	 * be displayed.
	 * 
	 * @param mCurrentLatLng location to use if servers should be detected
	 */
	public void runAutoDetectServer(LatLng mCurrentLatLng, boolean showDialog){
		if ((mCurrentLatLng == null) || (mMap == null)){
			Toast.makeText(applicationContext, applicationContext.getResources().getString(R.string.location_error), Toast.LENGTH_LONG).show();
		}
		else{
			ServersDataSource dataSource = ServersDataSource.getInstance(applicationContext);
			WeakReference<Activity> weakContext = new WeakReference<Activity>(getActivity());
	
			ServerSelector serverSelector = new ServerSelector(weakContext, applicationContext, dataSource, this, needToUpdateServersList, showDialog);
			serverSelector.execute(mCurrentLatLng);
			savedLastLocationCheckedForServer = mCurrentLatLng;
		}
		setNeedToRunAutoDetect(false);
		setNeedToUpdateServersList(false);
	}
	
	/**
	 * Triggers ServerSelector task to retrieve servers list.
	 * <p>
	 * Server list will be downloaded or retrieved from the database.
	 * <p>
	 * A servers list will be displayed or a toast informing of the error.
	 * <p>
	 */
	public void runAutoDetectServerNoLocation(boolean showDialog){
		ServersDataSource dataSource = ServersDataSource.getInstance(applicationContext);
		WeakReference<Activity> weakContext = new WeakReference<Activity>(getActivity());

		ServerSelector serverSelector = new ServerSelector(weakContext, applicationContext, dataSource, this, needToUpdateServersList, showDialog);
		LatLng latLngList[] = new LatLng[1];
		latLngList[0] = null;
		serverSelector.execute(latLngList);
		setNeedToRunAutoDetect(false);
		setNeedToUpdateServersList(false);
	}
	
	/**
	 * Registers the server in the OTPApp class.
	 * <p>
	 * UI is restored to avoid presence of all server data, removing all
	 * objects from the map and restarting text boxes to default contents.
	 * <p>
	 * OTPApp can be requested calling to getActivity by other fragments.
	 * 
	 * @param s
	 * @param restartUI
	 */
	private void setSelectedServer(Server s, boolean restartUI){
		if (restartUI){
			restartMap();
			restartTextBoxes();
		}
		
		app.setSelectedServer(s);
	}
	
	/**
	 * Removes all map objects and the global variables that reference them in
	 * this fragment.
	 */
	private void restartMap(){
		if (startMarker != null){
			startMarker.remove();
		}
		if (endMarker != null){
			endMarker.remove();
		}
		if (modeMarkers != null){
			for (Marker m : modeMarkers){
				m.remove();
			}		}
		if (route != null){
			for (Polyline p : route){
				p.remove();
			}		}
		if (boundariesPolyline != null){
			boundariesPolyline.remove();
		}
		
		startMarker = null;
		startMarkerPosition = null;
		endMarker = null;
		endMarkerPosition = null;
		route = null;
		modeMarkers = null;
		boundariesPolyline = null;
	}
	
	/**
	 * Sets text boxes to initial default locations.
	 * <p>
	 * MyLocation for start text box and empty for end text box.
	 * <p>
	 * Accordingly preference with key PREFERENCE_KEY_ORIGIN_IS_MY_LOCATION
	 * is set. 
	 */
	private void restartTextBoxes(){
		SharedPreferences.Editor prefsEditor = prefs.edit();
		setTextBoxLocation(applicationContext.getResources().getString(R.string.my_location), true);
		prefsEditor.putBoolean(OTPApp.PREFERENCE_KEY_ORIGIN_IS_MY_LOCATION, true);
		prefsEditor.commit();
		
		setTextBoxLocation("", false);
	}
	
	/**
	 * Writes coordinates of latlng to the selected text box. 
	 * 
	 * @param latlng object containing the coordinates to set
	 * @param isStartTb when true start text box is set otherwise end text box
	 */
	private void setLocationTb(LatLng latlng, boolean isStartTb){
		DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
		decimalFormatSymbols.setDecimalSeparator('.');
		DecimalFormat decimalFormat = new DecimalFormat(OTPApp.FORMAT_COORDINATES, decimalFormatSymbols);
		if (isStartTb){
			setTextBoxLocation(decimalFormat.format(latlng.latitude) + ", " + decimalFormat.format(latlng.longitude), true);
		}
		else{
			setTextBoxLocation(decimalFormat.format(latlng.latitude) + ", " + decimalFormat.format(latlng.longitude), false);
		}
	}
	
	/**
	 * Moves or adds (if didn't existed) a start/end marker to latlng position
	 * and updates its text box.
	 * <p>
	 * If preference with key PREFERENCE_KEY_USE_INTELLIGENT_MARKERS is set
	 * geocoding will be triggered for text boxes.
	 * <p>
	 * If the marker does not fit in selected server bounds marker won't be set
	 * and a warning will be shown.
	 * 
	 * @param isStartMarker when true start marker will be set
	 * @param latlng the location to move on
	 * @param showMessage whether show or not informative message on success
	 */
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
				if (prefs.getBoolean(OTPApp.PREFERENCE_KEY_USE_INTELLIGENT_MARKERS, true)){
					updateMarkerPosition(latlng, true);
				}
				else{
					isStartLocationGeocodingProcessed = true;
				}
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
				if (prefs.getBoolean(OTPApp.PREFERENCE_KEY_USE_INTELLIGENT_MARKERS, true)){
					updateMarkerPosition(latlng, false);
				}
				else{
					isEndLocationGeocodingProcessed = true;
				}
			}
			prefsEditor.commit();
		}
		else{
			if (showMessage){
				Toast.makeText(applicationContext, applicationContext.getResources().getString(R.string.marker_out_of_boundaries), Toast.LENGTH_SHORT).show();
			}
		}
	}
	
	/**
	 * Updates marker or creates a new one if doesn't exit to the passed latlng
	 * <p>
	 * Accordingly updates the field used for save/restore purposes.
	 *   
	 * @param isStartMarker if true start marker will be changed, end marker
	 * otherwise
	 * @param latLng contains the coordinates of the position to be changed to
	 */
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
	
	/**
	 * Creates and adds to the map a new start/end marker.
	 * <p>
	 * Accordingly updates the field used for save/restore purposes.
	 * 
	 * @param latLng the position to initialize the new marker
	 * @param isStartMarker if true a start marker will be created
	 * @return the new marker created
	 */
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
	
	/**
	 * Updates the text box contents to the given location and triggers
	 * geocoding for that location to update the text box.
	 * <p>
	 * This is a wrapper for setLocationTb, processAddres and accordingly change
	 * the field to control if the text box was changed by the user.
	 * 
	 * @param newLatLng
	 * @param isStartMarker
	 */
	private void updateMarkerPosition(LatLng newLatLng, boolean isStartMarker){
		setLocationTb(newLatLng, isStartMarker);
		String locationText = getLocationTbText(isStartMarker);
		if (isStartMarker){
			isStartLocationChangedByUser = false;
		}
		else{
			isEndLocationChangedByUser = false;
		}
		processAddress(isStartMarker, locationText, true);
	}
	
	@Override
	public void onStart() {
		super.onStart();	
		
		mLocationClient = new LocationClient(applicationContext, this, this);
		//mLocationClient.connect();
		
		
		if (mapFailed){
				mMap = ((SupportMapFragment) getFragmentManager().findFragmentById(R.id.map))
                        .getMap();
    // Check if we were successful in obtaining the map.
				if (mMap != null) {
					enableUIElements(true);
					
					initializeMapInterface(mMap);
					
					runAutoDetectServerNoLocation(true);
				}
			}
		
		/*if (mapFailed){
			Intent i = this.getActivity().getBaseContext().getPackageManager()
		             .getLaunchIntentForPackage( this.getActivity().getBaseContext().getPackageName() );
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(i);
		}*/
		
		connectLocationClient();
	}
	
	/**
	 * Connects the LocationClient.
	 * <p>
	 * To avoid errors only tries if is not pending for another connection
	 * request or is disconnected.
	 */
	public void connectLocationClient(){
		if (!mLocationClient.isConnected() && !mLocationClient.isConnecting()){
			mLocationClient.connect();
		}
	}
	
	/**
	 * Disconnects the LocationClient.
	 * <p>
	 * To avoid errors only tries if it's connected.
	 */
	public void disconnectLocationClient(){
		if (mLocationClient.isConnected()){
			mLocationClient.disconnect();
		}
	}
	
	public void onSaveInstanceState(Bundle bundle){
		super.onSaveInstanceState(bundle);
		
		bundle.putBoolean(OTPApp.BUNDLE_KEY_MAP_FAILED, mapFailed);

		if (!mapFailed){
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
			bundle.putInt(OTPApp.BUNDLE_KEY_DDL_OPTIMIZATION, ddlOptimization.getCheckedItemPosition());
			bundle.putInt(OTPApp.BUNDLE_KEY_DDL_TRAVEL_MODE, ddlTravelMode.getCheckedItemPosition());
			
			bundle.putParcelable(OTPApp.BUNDLE_KEY_SAVED_LAST_LOCATION, savedLastLocation);
			bundle.putParcelable(OTPApp.BUNDLE_KEY_SAVED_LAST_LOCATION_CHECKED_FOR_SERVER, savedLastLocationCheckedForServer);
			
			if (resultTripStartLocation != null){
				bundle.putString(OTPApp.BUNDLE_KEY_RESULT_TRIP_START_LOCATION, resultTripStartLocation);
			}
			if (resultTripEndLocation != null){
				bundle.putString(OTPApp.BUNDLE_KEY_RESULT_TRIP_END_LOCATION, resultTripEndLocation);
			}

			bundle.putDouble(OTPApp.BUNDLE_KEY_SEEKBAR_MIN_VALUE, bikeTriangleMinValue);
			bundle.putDouble(OTPApp.BUNDLE_KEY_SEEKBAR_MAX_VALUE, bikeTriangleMaxValue);
			
			bundle.putSerializable(OTPApp.BUNDLE_KEY_TRIP_DATE, tripDate);
			bundle.putBoolean(OTPApp.BUNDLE_KEY_ARRIVE_BY, arriveBy);
			
			if (!fragmentListener.getCurrentItineraryList().isEmpty()){
				OTPBundle otpBundle = new OTPBundle();
				otpBundle.setFromText(resultTripStartLocation);
				otpBundle.setToText(resultTripEndLocation);
				otpBundle.setItineraryList(fragmentListener.getCurrentItineraryList());
				otpBundle.setCurrentItineraryIndex(fragmentListener.getCurrentItineraryIndex());
				otpBundle.setCurrentItinerary(fragmentListener.getCurrentItinerary());
				bundle.putSerializable(OTPApp.BUNDLE_KEY_OTP_BUNDLE, otpBundle);
			}
		}

	}

	/**
	 * Triggers geocoding for chosen text box with passed text.
	 * <p>
	 * If address contents are the String used to identify user's location
	 * ("MyLocation" for example) user location is passed to know the
	 * corresponding address. 
	 * In this case user's location shouldn't be null, if it is a toast is
	 * shown. 
	 * 
	 * @param isStartTextBox
	 * @param address
	 * @param geocodingForMarker
	 */
	public void processAddress(final boolean isStartTextBox, String address, boolean geocodingForMarker) {
		WeakReference<Activity> weakContext = new WeakReference<Activity>(getActivity());

		OTPGeocoding geocodingTask = new OTPGeocoding(weakContext, applicationContext,
				isStartTextBox, geocodingForMarker, app.getSelectedServer(), prefs.getString(
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
		appResumed = true;
		
		Log.v(TAG, "MainFragment onResume");
	}

	@Override
	public void onPause() {
		
		super.onPause();
	}
	
	@Override
	public void onStop() {
		disconnectLocationClient();

		super.onStop();
	}

	@Override
	public void onDestroy() {
		// Release all map-related objects to make sure GPS is shut down when
		// the user leaves the app

		Log.d(TAG, "Released all map objects in MainFragment.onDestroy()");

		super.onDestroy();
	}

	
	public void updateSelectedServer(){
		if (prefs.getBoolean(OTPApp.PREFERENCE_KEY_SELECTED_CUSTOM_SERVER, false)){
			setSelectedServer(new Server(prefs.getString(OTPApp.PREFERENCE_KEY_CUSTOM_SERVER_URL, ""), applicationContext), true);
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
				
				setSelectedServer(s, true);
				addBoundariesRectangle(s);

				LatLng mCurrentLatLng = getLastLocation();
				
				if ((mCurrentLatLng != null) && (LocationUtil.checkPointInBoundingBox(mCurrentLatLng, s, OTPApp.CHECK_BOUNDS_ACCEPTABLE_ERROR))){
					mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mCurrentLatLng, getServerInitialZoom(s)));
				}
				else{
					mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(getServerCenter(s), getServerInitialZoom(s)));
					setMarker(true, getServerCenter(s), false);
				}
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
	
	@Override
	public void onPrepareOptionsMenu(final Menu pMenu) {
		if (isGPSEnabled()) {
			mGPS.setTitle(R.string.disable_gps);
		} else {
			mGPS.setTitle(R.string.enable_gps);
		}
		super.onPrepareOptionsMenu(pMenu);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem pItem) {
		OTPApp app = ((OTPApp) getActivity().getApplication());
		switch (pItem.getItemId()) {
		case R.id.gps_settings:
			Intent myIntent = new Intent(
					Settings.ACTION_LOCATION_SOURCE_SETTINGS);
			startActivity(myIntent);
			break;
		case R.id.settings:
			getActivity().startActivityForResult(
					new Intent(getActivity(), SettingsActivity.class),
					OTPApp.SETTINGS_REQUEST_CODE);
			break;
		case R.id.feedback:
			Server selectedServer = app.getSelectedServer();

			String[] recipients = { selectedServer.getContactEmail(),
					getString(R.string.feedback_email_android_developer) };

			String uriText = "mailto:";
			for (int i = 0; i < recipients.length; i++) {
				uriText += recipients[i] + ";";
			}

			String subject = "";
			subject += getResources().getString(R.string.feedback_subject);
			Date d = Calendar.getInstance().getTime();
			subject += "[" + d.toString() + "]";
			uriText += "?subject=" + subject;

			String content = ((MyActivity)getActivity()).getCurrentRequestString();
			
			try {
				uriText += "&body=" + URLEncoder.encode(content, OTPApp.URL_ENCODING);
			} catch (UnsupportedEncodingException e1) {
				e1.printStackTrace();
				return false;
			}

			Uri uri = Uri.parse(uriText);

			Intent sendIntent = new Intent(Intent.ACTION_SENDTO);
			sendIntent.setData(uri);
			startActivity(Intent.createChooser(sendIntent, getResources().getString(R.string.feedback_send_email)));

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

	/**
	 * Wrapper to other functions: moves the marker to the location included
	 * in the address, updates text box and zooms to that position.
	 * 
	 * @param isStartMarker if true start marker will be changed
	 * @param addr will location and text information
	 */
	public void moveMarker(Boolean isStartMarker, Address addr) {
		if (isStartMarker) {
			startAddress = addr;
		} else {
			endAddress = addr;
		}
		LatLng latlng = new LatLng(addr.getLatitude(), addr.getLongitude());
		setMarkerPosition(isStartMarker, latlng);
		setTextBoxLocation(getStringAddress(addr, false), isStartMarker);
		zoomToGeocodingResult(isStartMarker, addr);
	}
	
	/**
	 * Wrapper to other functions: moves the marker to the location included
	 * in the address, updates text box and zooms to that position.
	 * <p>
	 * This only happens if the new location is closer than a constant to
	 * marker previous location. Otherwise address is only used as reference
	 * and text box is updated to "Marker close to [addr]".
	 * 
	 * @param isStartMarker if true start marker will be changed
	 * @param addr will location and text information
	 */
	public void moveMarkerRelative(Boolean isStartMarker, Address addr) {
		float results[] = new float[1];
		double addresLat = addr.getLatitude();
		double addresLon = addr.getLongitude();
		
		Marker marker;
		if (isStartMarker) {
			marker = startMarker;
			startAddress = addr;
		} else {
			marker = endMarker;
			endAddress = addr;
		}
		
		Location.distanceBetween(marker.getPosition().latitude, marker.getPosition().longitude, addresLat, addresLon, results);
		
		if (results[0] < OTPApp.MARKER_GEOCODING_MAX_ERROR){
			LatLng newLatlng = new LatLng(addresLat, addresLon);
			setMarkerPosition(isStartMarker, newLatlng);
			setTextBoxLocation(getStringAddress(addr, false), isStartMarker);
		}
		else{
			setTextBoxLocation(getResources().getString(R.string.textbox_close_to_marker) + " " + getStringAddress(addr, false), isStartMarker);
		}

	}
	
	private String getStringAddress(Address address, boolean multilines){
		if (address.getMaxAddressLineIndex() >= 0){
			
			String result = address.getAddressLine(0);
			
			if (multilines){
				for (int i = 1; i <= address.getMaxAddressLineIndex(); i++){
					if (i == 1){
						result += "\n"; 
						if (address.getAddressLine(i) != null){
							result +=  address.getAddressLine(i);
						}
					}
					else if (i == 2){
						result += "\n"; 			
						if (address.getAddressLine(i) != null){
							result +=  address.getAddressLine(i);
						}
					}
					else{
						if (address.getAddressLine(i) != null){
							result += ", " + address.getAddressLine(i);
						}
					}
				}
			}
			else{
				for (int i = 1; i <= address.getMaxAddressLineIndex(); i++){
					if (address.getAddressLine(i) != null){
						result += ", " + address.getAddressLine(i);
					}
				}
			}

			return result;
		}
		else{
			return null;
		}
	}
	
	/**
	 * Zooms to addr or to addr and the location of the other marker if it's
	 * not the first marker.
	 * <p>
	 * If the other location is "MyLocation" will also be included in zoom.
	 * 
	 * @param isStartLocation if true addr is for start location
	 * @param addr with the location to zoom at
	 */
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
			mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng, OTPApp.defaultMediumZoomLevel));
		}
	}
	
	public void zoomToTwoPoints(LatLng pointA, LatLng pointB) {
		if ((pointA.latitude != pointB.latitude) && (pointA.longitude != pointB.longitude)){
			LatLngBounds.Builder boundsCreator = LatLngBounds.builder();
			
			boundsCreator.include(pointA);
			boundsCreator.include(pointB);

			mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(boundsCreator.build(), getResources().getInteger(R.integer.default_padding)));	
		}
	}

	/**
	 * Updates start/end text box contents to the given text.
	 * 
	 * @param text contents to insert
	 * @param isStartTextBox if true start box will be used
	 */
	public void setTextBoxLocation(String text, boolean isStartTextBox) {
		if (isStartTextBox) {
			isStartLocationChangedByUser = false;
			tbStartLocation.setText(text);
		} else {
			isEndLocationChangedByUser = false;
			tbEndLocation.setText(text);
		}
	}

	/**
	 * Draws the route on the map.
	 * <p>
	 * To indicate the full route a polyline will be drawn using all points in 
	 * itinerary.
	 * <p>
	 * On each method of transportation change a mode marker will be added.
	 * <p>
	 * Mode marker for transit step will display stop name, departure time and
	 * headsign.
	 * Mode marker for walk/bike connection, guidance to next point and distance and time
	 * to get there.
	 * <p>
	 * Previous routes are removed from the map.
	 * 
	 * @param itinerary the information to be drawn
	 * @param animateCamera if true map will be zoomed to exactly fit the route
	 * after the drawing
	 */
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
		Marker firstTransitMarker = null;
		
		if (!itinerary.isEmpty()) {
			List<LatLng> allGeoPoints = new ArrayList<LatLng>();
			LatLngBounds.Builder boundsCreator = LatLngBounds.builder();
			


			int agencyTimeZoneOffset = 0;
			int stepIndex = 0;
			
			for (Leg leg : itinerary) {
				stepIndex++;
				
				if (leg.getAgencyTimeZoneOffset() != 0){
					agencyTimeZoneOffset = leg.getAgencyTimeZoneOffset();
				}
				
				List<LatLng> points = LocationUtil.decodePoly(leg.legGeometry
						.getPoints());
				
				float scaleFactor = getResources().getFraction(R.fraction.scaleFactor, 1, 1);
				 
				Drawable d= getResources().getDrawable(getPathIcon(leg.mode));
				BitmapDrawable bd=(BitmapDrawable) d.getCurrent();
				Bitmap b=bd.getBitmap();
				Bitmap bhalfsize=Bitmap.createScaledBitmap(b, (int)(b.getWidth()/scaleFactor), (int)(b.getHeight()/scaleFactor), false);
				
				MarkerOptions modeMarkerOption = new MarkerOptions().position(points.get(0))
						                                        .icon(BitmapDescriptorFactory.fromBitmap(bhalfsize));


				TraverseMode traverseMode = TraverseMode.valueOf((String) leg.mode);

				if (traverseMode.isTransit()){
					modeMarkerOption.title(stepIndex + ". " + ItineraryDecrypt.getLocalizedStreetName(leg.getFrom().name, applicationContext.getResources()) + " " + DateTimeConversion.getTimeWithContext(applicationContext, agencyTimeZoneOffset, Long.parseLong(leg.getStartTime()), true));
					if (leg.getHeadsign() != null){
						modeMarkerOption.snippet(leg.getHeadsign());
					}
				}
				else{
					if (traverseMode.equals(TraverseMode.WALK)){
						modeMarkerOption.title(stepIndex + ". " + getResources().getString(R.string.before_distance_walk)
								+ " " + getResources().getString(R.string.connector_destination) + " " +  ItineraryDecrypt.getLocalizedStreetName(leg.getTo().name, applicationContext.getResources()));
					}
					else if (traverseMode.equals(TraverseMode.BICYCLE)){
						modeMarkerOption.title(stepIndex + ". " + getResources().getString(R.string.before_distance_bike)
								+ " " + getResources().getString(R.string.connector_destination) + " " +  ItineraryDecrypt.getLocalizedStreetName(leg.getTo().name, applicationContext.getResources()));
					}
					modeMarkerOption.snippet(DateTimeConversion.getFormattedDurationTextNoSeconds(leg.duration/1000, applicationContext) + " " + "-" + " " 
							+ String.format(OTPApp.FORMAT_DISTANCE_METERS_SHORT, leg.getDistance()) + getResources().getString(R.string.distance_unit));
				}

				
				Marker modeMarker = mMap.addMarker(modeMarkerOption);
				modeMarkers.add(modeMarker);
				
				if(traverseMode.isTransit()){
					//because on transit two step-by-step indications are generated (get on / get off)
					stepIndex++;
					
					if (firstTransitMarker == null){
						firstTransitMarker = modeMarker;
					}
				}
				PolylineOptions options = new PolylineOptions().addAll(points)
						   .width(5 * scaleFactor)
						   .color(OTPApp.COLOR_ROUTE_LINE);
				Polyline routeLine = mMap.addPolyline(options);
				route.add(routeLine);
				for (LatLng point : points) {
					boundsCreator.include(point);
				}
				allGeoPoints.addAll(points);

			}
			if (animateCamera){
				if (firstTransitMarker != null){
					firstTransitMarker.showInfoWindow();
				}
				mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(boundsCreator.build(), getResources().getInteger(R.integer.default_padding)));
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

	public OnFragmentListener getFragmentListener() {
		return fragmentListener;
	}

	public void setFragmentListener(OnFragmentListener fragmentListener) {
		this.fragmentListener = fragmentListener;
	}

	@Override
	public void onServerSelectorComplete(Server server) {
		//Update application server
		if (getActivity() != null){
			setSelectedServer(server, true);
			updateSelectedServer();
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
			
			if ((startAddress != null) && (prefs.getBoolean(OTPApp.PREFERENCE_KEY_USE_INTELLIGENT_MARKERS, true))){
				resultTripStartLocation = ((startAddress.getAddressLine(0) != null) ? startAddress.getAddressLine(0) : "")
						+ ", "
						+ ((startAddress.getAddressLine(1) != null) ? startAddress.getAddressLine(1) : "");
			}
			else{
				resultTripStartLocation = tbStartLocation.getText().toString();
			}
			if ((endAddress != null) && (prefs.getBoolean(OTPApp.PREFERENCE_KEY_USE_INTELLIGENT_MARKERS, true))){
				resultTripEndLocation = ((endAddress.getAddressLine(0) != null) ? endAddress.getAddressLine(0) : "")
						+ ", "
						+ ((endAddress.getAddressLine(1) != null) ? endAddress.getAddressLine(1) : "");
			}
			else{
				resultTripEndLocation = tbEndLocation.getText().toString();
			}
		}
	}
	
	private void fillItinerariesSpinner(List<Itinerary> itineraryList){
		String[] itinerarySummaryList = new String[itineraryList.size()];

		int agencyTimeZoneOffset = 0;
		
		for(int i=0; i<itinerarySummaryList.length; i++){
			boolean isTagSet = false;
			Itinerary it = itineraryList.get(i);
			itinerarySummaryList[i] = Integer.toString(i+1) + ".   ";//Shown index is i + 1, to use 1-based indexes for the UI instead of 0-base
			for (Leg leg : it.legs){
				TraverseMode traverseMode = TraverseMode.valueOf((String) leg.mode);
				//OTP can't handle more than two timezones per request, so this is safe
				if (leg.getAgencyTimeZoneOffset() != 0){
					agencyTimeZoneOffset = leg.getAgencyTimeZoneOffset();
				}
				if(traverseMode.isTransit()){
					itinerarySummaryList[i] += getString(R.string.before_route) + " " + leg.getRouteShortName();
					itinerarySummaryList[i] += DateTimeConversion.getTimeWithContext(applicationContext, agencyTimeZoneOffset, Long.parseLong(leg.getStartTime()), true);
					itinerarySummaryList[i] += " " + "-" + " " + DateTimeConversion.getFormattedDurationTextNoSeconds(it.duration/1000, applicationContext);
					isTagSet = true;
					break;
				}
			}
			if (!isTagSet){
				if (it.legs.size() == 1){
					TraverseMode traverseMode = TraverseMode.valueOf((String) it.legs.get(0).mode);
					if (traverseMode.equals(TraverseMode.WALK)){
						itinerarySummaryList[i] += getString(R.string.before_distance_walk) + " " + String.format(OTPApp.FORMAT_DISTANCE_METERS_SHORT, it.walkDistance) + getResources().getString(R.string.distance_unit);
						itinerarySummaryList[i] += " " + getString(R.string.connector_time_full) + " " + DateTimeConversion.getFormattedDurationTextNoSeconds(it.duration/1000, applicationContext);
					}
					else if (traverseMode.equals(TraverseMode.BICYCLE)){
						itinerarySummaryList[i] += getString(R.string.before_distance_bike) + " " + String.format(OTPApp.FORMAT_DISTANCE_METERS_SHORT, it.walkDistance) + getResources().getString(R.string.distance_unit);
						itinerarySummaryList[i] += " " + getString(R.string.connector_time_full) + " " + DateTimeConversion.getFormattedDurationTextNoSeconds(it.duration/1000, applicationContext);
					}
				}
				else{
					itinerarySummaryList[i] += getString(R.string.total_duration) + " " + DateTimeConversion.getFormattedDurationTextNoSeconds(it.duration/1000, applicationContext);
				}
			}

		}
		
		ArrayAdapter<String> itineraryAdapter = new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_spinner_item, itinerarySummaryList);
	
		itineraryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		itinerarySelectionSpinner.setAdapter(itineraryAdapter);
	}

	@Override
	public void onOTPGeocodingComplete(final boolean isStartTextbox,
			ArrayList<Address> addressesReturn, boolean geocodingForMarker) {
		if (getActivity() != null){	
			if (isStartTextbox){
				isStartLocationGeocodingProcessed = true;
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
						.setPositiveButton(getResources().getString(android.R.string.ok), new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
							}
						});
		
				if (addressesReturn.isEmpty()) {
					AlertDialog alert = geocoderAlert.create();
					alert.show();
					return;
				} else if (addressesReturn.size() == 1) {
					if (geocodingForMarker){
						moveMarkerRelative(isStartTextbox, addressesReturn.get(0));
					}
					else{
						moveMarker(isStartTextbox, addressesReturn.get(0));
					}
					geocodingBeenRequested = false;
					requestTripAfterGeocoding();
					return;
				}
			
				AlertDialog.Builder geocoderSelector = new AlertDialog.Builder(
						getActivity());
				geocoderSelector.setTitle(R.string.choose_geocoder);
		
				final CharSequence[] addressesText = new CharSequence[addressesReturn
						.size()];
				for (int i = 0; i < addressesReturn.size(); i++) {
					Address addr = addressesReturn.get(i);
					addressesText[i] = getStringAddress(addr, true);

					Log.v(TAG, addressesText[i].toString());
				}
		
				final ArrayList<Address> addressesTemp = addressesReturn;
				geocoderSelector.setItems(addressesText,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int item) {
								Address addr = addressesTemp.get(item);
								moveMarker(isStartTextbox, addr);
								Log.v(TAG, "Chosen: " + addressesText[item]);
								geocodingBeenRequested = false;
								MainFragment.this.requestTripAfterGeocoding();
							}
						});
				AlertDialog alertGeocoder = geocoderSelector.create();
				alertGeocoder.show();
			}catch(Exception e){
				Log.e(TAG, "Error in Main Fragment Geocoding callback: " + e);
			}
		}
	}
	
	/**
	 * Checks if a trip was requested, and requested geocoding done.
	 * <p>
	 * If it's necessary request will be processed and control variables
	 * restarted.
	 */
	private void requestTripAfterGeocoding(){
		if (requestTripAfterStartGeocoding){
			requestTripAfterStartGeocoding = false;
			if (!requestTripAfterStartEndGeocoding){
				requestTrip();
			}
		}
		else if (requestTripAfterEndGeocoding){
			requestTripAfterEndGeocoding = false;
			if (!requestTripAfterStartEndGeocoding){
				requestTrip();
			}		
		}
		else if (requestTripAfterStartEndGeocoding){
			requestTripAfterStartEndGeocoding = false;
			requestTrip();
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
			
			LatLng mCurrentLatLng = getLastLocation();
			
			if ((mCurrentLatLng != null) && (LocationUtil.checkPointInBoundingBox(mCurrentLatLng, selectedServer, OTPApp.CHECK_BOUNDS_ACCEPTABLE_ERROR))){
				mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mCurrentLatLng, getServerInitialZoom(selectedServer)));
			}
			else{
				mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(getServerCenter(selectedServer), getServerInitialZoom(selectedServer)));
				setMarker(true, getServerCenter(selectedServer), false);
			}
		}
	}
	
	/**
	 * Changes the tiles used to display the map and sets max zoom level.
	 * 
	 * @param overlayString tiles URL for custom tiles or description for
	 * Google ones
	 */
	public void updateOverlay(String overlayString){
		if (overlayString == null){
			overlayString = prefs.getString(OTPApp.PREFERENCE_KEY_MAP_TILE_SOURCE, applicationContext.getResources().getString(R.string.map_tiles_default_server));
		}
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
			maxZoomLevel = mMap.getMaxZoomLevel();
		}
		else{
			if (overlayString.equals(getResources().getString(R.string.tiles_mapnik))){
				maxZoomLevel = getResources().getInteger(R.integer.tiles_mapnik_max_zoom);
			}
			else{
				maxZoomLevel = getResources().getInteger(R.integer.tiles_maquest_max_zoom);
			}
			mMap.setMapType(GoogleMap.MAP_TYPE_NONE);
			MyUrlTileProvider mTileProvider = new MyUrlTileProvider(OTPApp.CUSTOM_MAP_TILE_HEIGHT, OTPApp.CUSTOM_MAP_TILE_HEIGHT, overlayString);
			actualTileOverlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(mTileProvider).zIndex(OTPApp.CUSTOM_MAP_TILE_Z_INDEX));
			
			if (mMap.getCameraPosition().zoom > maxZoomLevel){
				mMap.moveCamera(CameraUpdateFactory.zoomTo(maxZoomLevel));
			}
		}
	}
	
	/**
	 * Returns last location coordinates.
	 * <p>
	 * This is obtained from the Location Client if it's connected and retrurns
	 * a valid Location. If not saved last location is provided.
	 * <p>
	 * On successful call to Location Client saved last location is updated.
	 * 
	 * @return a LatLng object with the most updated user coordinates 
	 */
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
    	if (!mapFailed){
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
    			errorPlay.setTitle(getResources().getString(R.string.play_services_error_title))
    					.setMessage(getResources().getString(R.string.play_services_error) + connectionResult.getErrorCode())
    					.setNeutralButton(getResources().getString(android.R.string.ok), null)
    					.create()
    					.show();
            }
    	}
    }

	@Override
	public void onConnected(Bundle connectionHint) {
		Location mCurrentLocation = mLocationClient.getLastLocation();

		if ((!mapFailed)){
			if (mCurrentLocation != null){
				//mLocationClient.requestLocationUpdates(mLocationRequest, this);
				double savedLatitude = 0;
				double savedLongitude = 0;
				float distance[] = new float[1];
				distance[0] = 0;
				if (savedLastLocationCheckedForServer != null){
					savedLatitude = savedLastLocationCheckedForServer.latitude;
					savedLongitude = savedLastLocationCheckedForServer.longitude;
				}
				
				LatLng mCurrentLatLng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
				
				Location.distanceBetween(savedLatitude, savedLongitude, mCurrentLatLng.latitude, mCurrentLatLng.longitude, distance);
				
				if (!checkServersAreUpdated()){
					runAutoDetectServer(mCurrentLatLng, false);
				}
				else{
					if (needToRunAutoDetect){
						runAutoDetectServer(mCurrentLatLng, true);
					}
					else if (prefs.getBoolean(OTPApp.PREFERENCE_KEY_AUTO_DETECT_SERVER, true)) {
						
						if ((app.getSelectedServer() != null) 
								&& (!LocationUtil.checkPointInBoundingBox(mCurrentLatLng, app.getSelectedServer(), OTPApp.CHECK_BOUNDS_ACCEPTABLE_ERROR))
								&& (((savedLastLocationCheckedForServer != null) && (distance[0] > OTPApp.COORDINATES_IMPORTANT_DIFFERENCE)) 
										|| (savedLastLocationCheckedForServer == null))){
							runAutoDetectServer(mCurrentLatLng, false);
						}
						else if (app.getSelectedServer() == null){
							runAutoDetectServer(mCurrentLatLng, true);
						}
					}
					else {
						if (mCurrentLatLng != null){
							if (appStarts){
								Server selectedServer = app.getSelectedServer();	
								if ((selectedServer != null) && selectedServer.areBoundsSet()){
									if (LocationUtil.checkPointInBoundingBox(mCurrentLatLng, selectedServer, OTPApp.CHECK_BOUNDS_ACCEPTABLE_ERROR)){
										mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mCurrentLatLng, getServerInitialZoom(selectedServer)));
									}
									else{
										mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(getServerCenter(selectedServer), getServerInitialZoom(selectedServer)));	
										setMarker(true, getServerCenter(selectedServer), false);
									}
								}
								else{
									mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mCurrentLatLng, getServerInitialZoom(selectedServer)));
								}
							}
						}
					}
			
					appStarts = false;
				}
			}
			else if (app.getSelectedServer() == null){
				runAutoDetectServerNoLocation(true);
			}
		}
	        
	}
	
	private boolean checkServersAreUpdated(){
		ServersDataSource dataSource = ServersDataSource.getInstance(applicationContext);
		dataSource.open();
		boolean result;
		Calendar someDaysBefore = Calendar.getInstance();
		someDaysBefore.add(Calendar.DAY_OF_MONTH, - OTPApp.EXPIRATION_DAYS_FOR_SERVER_LIST);
		Long serversUpdateDate = dataSource.getMostRecentDate();
		if ((serversUpdateDate != null) && (someDaysBefore.getTime().getTime() > serversUpdateDate)){
			result = false;
		}
		else{
			result = true;
		}
		dataSource.close();
		
		return result;
	}

	@Override
	public void onDisconnected() {		
	}

	@Override
	public void onLocationChanged(Location location) {
        LatLng mCurrentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
		Server selectedServer = app.getSelectedServer();	
        
        if ((mCurrentLatLng != null) && (selectedServer != null) && (LocationUtil.checkPointInBoundingBox(mCurrentLatLng, selectedServer, OTPApp.CHECK_BOUNDS_ACCEPTABLE_ERROR))){
			mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mCurrentLatLng, getServerInitialZoom(selectedServer)));
		}
		else if ((selectedServer != null) && selectedServer.areBoundsSet()){
			mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(getServerCenter(selectedServer), getServerInitialZoom(selectedServer)));
			setMarker(true, getServerCenter(selectedServer), false);
		}

        /*
        String msg = "Updated Location: " +
                Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude());
        Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show();*/
		
	}

	/**
	 * Draws rectangle in the map to represent the bounds, using selected
	 * server fields for lower left and upper right coordinates.
	 * 
	 * @param server from which coordinates will be pulled
	 */
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
	
	public float getServerInitialZoom(Server s){
		if (s.isZoomSet()){
			return s.getInitialZoom();
		}
		else{
			return OTPApp.defaultInitialZoomLevel;
		}
	}
	
	public LatLng getServerCenter(Server s){
		if (s.isCenterSet()){
			return new LatLng(s.getCenterLatitude(), s.getCenterLongitude());
		}
		else{
			return new LatLng(s.getGeometricalCenterLatitude(), s.getGeometricalCenterLongitude());
		}
	}

	@Override
	public void onCameraChange(CameraPosition position) {
		if (position.zoom > maxZoomLevel) {
			mMap.moveCamera(CameraUpdateFactory.zoomTo(maxZoomLevel));
		}
	}


	@Override
	public void onDateComplete(Date tripDate, boolean arriveBy) {
		this.tripDate = tripDate;
		this.arriveBy = arriveBy;
		String tripTime = tripDate.toString() + arriveBy;
		Log.v(TAG, tripTime);
	}

	@Override
	public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar,
			Double minValue, Double maxValue) {
		bikeTriangleMinValue = minValue;
		bikeTriangleMaxValue = maxValue;
		String bikeParam = minValue.toString() + maxValue.toString();
		Log.v(TAG, bikeParam);
	}
	
}