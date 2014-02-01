/*
 * Copyright 2011 Marcy Gordon
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package edu.usf.cutr.opentripplanner.android;

import android.app.Application;
import edu.usf.cutr.opentripplanner.android.model.Server;

/*
 * Modified by Khoa Tran
 */

public class OTPApp extends Application {
	
	public static final int	CONNECTION_FAILURE_RESOLUTION_REQUEST_CODE = 9000;
	
	public static final int CHECK_GOOGLE_PLAY_REQUEST_CODE = 3;
	
	public static final int SETTINGS_REQUEST_CODE = 2;
	
	public static final int CHOOSE_CONTACT_REQUEST_CODE = 1;
	
	public static final String METADATA_LOCATION = "/metadata";
	
	public static final String URL_ENCODING = "UTF-8";
	
	public static final String REFRESH_SERVER_RETURN_KEY = "RefreshServer";
	public static final String CHANGED_SELECTED_CUSTOM_SERVER_RETURN_KEY = "ChangedSelectedCustomServer";
	public static final String CHANGED_MAP_TILE_PROVIDER_RETURN_KEY = "ChangedMapTileProvider";

	public static final String TAG_FRAGMENT_MAIN_FRAGMENT = "mainFragment";
	public static final String TAG_FRAGMENT_DATE_TIME_DIALOG = "dateTimeDialog";
	
	public static final String MAP_TILE_GOOGLE = "Google";
	public static final String MAP_TILE_GOOGLE_HYBRID = "Google hybrid";
	public static final String MAP_TILE_GOOGLE_NORMAL = "Google normal";
	public static final String MAP_TILE_GOOGLE_SATELLITE = "Google satellite";
	public static final String MAP_TILE_GOOGLE_TERRAIN = "Google terrain";
    
    public static final int CUSTOM_MAP_TILE_HEIGHT = 256;
    public static final int CUSTOM_MAP_TILE_WIDTH = 256;
    public static final int CUSTOM_MAP_TILE_Z_INDEX = -1;
    
    public static final double BIKE_PARAMETERS_MIN_VALUE = 0.0;
    public static final double BIKE_PARAMETERS_QUICK_DEFAULT_VALUE = 1.0/3.0;
    public static final double BIKE_PARAMETERS_FLAT_DEFAULT_VALUE = 2.0/3.0;
    public static final double BIKE_PARAMETERS_MAX_VALUE = 1.0;

    public static final int CHECK_BOUNDS_ACCEPTABLE_ERROR = 1000;
    public static final float COORDINATES_IMPORTANT_DIFFERENCE = 2000f;
    
    public static final int CUSTOM_TILES_MAX_ZOOM_LEVEL = 20;
    //in meters
    public static final int MARKER_GEOCODING_MAX_ERROR = 100;
    
    private static final int MILLISECONDS_PER_SECOND = 1000;
    private static final int UPDATE_INTERVAL_IN_SECONDS = 5;
    public static final long UPDATE_INTERVAL = MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;
    private static final int FASTEST_INTERVAL_IN_SECONDS = 1;
    public static final long FASTEST_INTERVAL = MILLISECONDS_PER_SECOND * FASTEST_INTERVAL_IN_SECONDS;
    
    public static final String BUNDLE_KEY_MAP_FAILED = "Map failed";
    public static final String BUNDLE_KEY_MAP_CAMERA = "Map Camera";
    public static final String BUNDLE_KEY_MAP_START_MARKER_POSITION = "Map StartMarkerPosition";
    public static final String BUNDLE_KEY_MAP_END_MARKER_POSITION = "Map EndMarkerPosition";
    public static final String BUNDLE_KEY_MAP_ROUTE_POLYLINE_OPTIONS = "Map RoutePolylineOptions";
    public static final String BUNDLE_KEY_MAP_BOUNDARIES_POLYLINE_OPTIONS = "Map BoundariesPolylineOptions";
    public static final String BUNDLE_KEY_TB_START_LOCATION = "tbStartLocation";
    public static final String BUNDLE_KEY_TB_END_LOCATION = "tbEndLocation";
    public static final String BUNDLE_KEY_DDL_OPTIMIZATION = "ddlOptimization";
    public static final String BUNDLE_KEY_DDL_TRAVEL_MODE = "ddlTravelMode";
    public static final String BUNDLE_KEY_OTP_BUNDLE = "OTP bundle";
    public static final String BUNDLE_KEY_PANEL_STATE = "panel state";
    public static final String BUNDLE_KEY_APP_STARTS = "app starts";
    public static final String BUNDLE_KEY_IS_START_LOCATION_GEOCODING_PROCESSED = "startlocation geocoding processed";
    public static final String BUNDLE_KEY_IS_END_LOCATION_GEOCODING_PROCESSED = "enlocation geocoding processed";
    public static final String BUNDLE_KEY_IS_START_LOCATION_CHANGED_BY_USER = "startlocation changed by user";
    public static final String BUNDLE_KEY_IS_END_LOCATION_CHANGED_BY_USER = "endlocation changed by user";
    
    public static final String BUNDLE_KEY_RESULT_TRIP_START_LOCATION = "result trip start location";
    public static final String BUNDLE_KEY_RESULT_TRIP_END_LOCATION = "result trip end location";
   
    public static final String BUNDLE_KEY_SAVED_LAST_LOCATION = "saved last location";
    public static final String BUNDLE_KEY_SAVED_LAST_LOCATION_CHECKED_FOR_SERVER = "saved last location checked for server";


    public static final String BUNDLE_KEY_SEEKBAR_MIN_VALUE = "seekbar min value";
    public static final String BUNDLE_KEY_SEEKBAR_MAX_VALUE = "seekbar max value";

    public static final String BUNDLE_KEY_TRIP_DATE = "trip date";
    public static final String BUNDLE_KEY_ARRIVE_BY = "arrive by";
    
    public static final String BUNDLE_KEY_TIMEPICKER_SAVED_HOUR = "timepicker hours";
    public static final String BUNDLE_KEY_TIMEPICKER_SAVED_MINUTE = "timepicker minutes";

    public static final String BUNDLE_KEY_SETTINGS_INTENT = "timepicker minutes";
    
    public static final int COLOR_ROUTE_LINE = 0x7F0000FF;

	/**
	 * Preference keys
	 */
	public static final String PREFERENCE_KEY_ORIGIN_IS_MY_LOCATION = "origin_is_my_location";
	public static final String PREFERENCE_KEY_DESTINATION_IS_MY_LOCATION = "destination_is_my_location";
	public static final String PREFERENCE_KEY_MAP_TILE_SOURCE = "map_tile_source";
	public static final String PREFERENCE_KEY_GEOCODER_PROVIDER = "geocoder_provider";
	public static final String PREFERENCE_KEY_ROUTING_OPTIONS = "routing_options";
	public static final String PREFERENCE_KEY_AUTO_DETECT_SERVER = "auto_detect_server";
	public static final String PREFERENCE_KEY_CUSTOM_SERVER_URL = "custom_server_url";
	public static final String PREFERENCE_KEY_CUSTOM_SERVER_BOUNDS = "custom_server_bounds";
	public static final String PREFERENCE_KEY_CUSTOM_SERVER_URL_IS_VALID = "custom_server_url_is_valid";
	public static final String PREFERENCE_KEY_REFRESH_SERVER_LIST = "refresh_server_list";
	public static final String PREFERENCE_KEY_OTP_PROVIDER_FEEDBACK = "otp_provider_feedback";
	public static final String PREFERENCE_KEY_MAX_WALKING_DISTANCE = "max_walking_distance";
	public static final String PREFERENCE_KEY_WHEEL_ACCESSIBLE = "wheelchair_accessible";
	public static final String PREFERENCE_KEY_SELECTED_SERVER = "selected_server";
	public static final String PREFERENCE_KEY_SELECTED_CUSTOM_SERVER = "selected_custom_server";
	public static final String PREFERENCE_KEY_USE_ANDROID_GEOCODER = "use_android_geocoder";
	public static final String PREFERENCE_KEY_USE_INTELLIGENT_MARKERS = "use_intelligent_markers";
	public static final String PREFERENCE_KEY_USE_DEVICE_TIMEZONE = "use_device_timezone";
	
	
	private static Server selectedServer;
	
	public static final String TAG = "OTP";
	
	public static final int EXPIRATION_DAYS_FOR_SERVER_LIST = 3;
	
	public static final float defaultZoomStep = 1;
	public static final float defaultInitialZoomLevel = 12;
	public static final float defaultMediumZoomLevel = 14;
	public static final float defaultMyLocationZoomLevel = 16;
	
    public static final String FORMAT_COORDINATES = "#.00000";
    public static final String FORMAT_DISTANCE_METERS = "%.0f";
    public static final String FORMAT_DISTANCE_KILOMETERS = "%.1f";
    public static final String FORMAT_OTP_SERVER_DATE_RESPONSE = "yyyy-MM-dd\'T\'HH:mm:ssZZ";
	public static final String FORMAT_OTP_SERVER_DATE_QUERY = "MM/dd/yy";
	public static final String FORMAT_OTP_SERVER_TIME_QUERY = "kk:mm";


	/**
	 * Sets the currently selected OTP server
	 * 
	 * @param selectedServer the selected OTP server
	 */
	public void setSelectedServer(Server sServer) {
		selectedServer = sServer;
//		new MetadataRequest(activity).execute("");
	}

	/**
	 * Gets the currently selected OTP server
	 * 
	 * @return the currently selected OTP server
	 */
	public Server getSelectedServer() {
		return selectedServer;
	}
	
}
