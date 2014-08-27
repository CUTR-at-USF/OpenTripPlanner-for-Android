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

import java.util.concurrent.TimeUnit;

import edu.usf.cutr.opentripplanner.android.model.Server;

/*
 * Modified by Khoa Tran
 */

public class OTPApp extends Application {

    public static final int CONNECTION_FAILURE_RESOLUTION_REQUEST_CODE = 9000;

    public static final int CHECK_GOOGLE_PLAY_REQUEST_CODE = 3;

    public static final int SETTINGS_REQUEST_CODE = 2;

    public static final int CHOOSE_CONTACT_REQUEST_CODE = 1;

    public static final String METADATA_LOCATION = "/metadata";

    public static final String BIKE_RENTAL_LOCATION = "/bike_rental";

    public static final String TRIP_TIMES_UPDATES_LOCATION_BEFORE_ID = "/index/trips/";

    public static final String TRIP_TIMES_UPDATES_LOCATION_AFTER_ID = "/stoptimes";

    public static final String PLAN_LOCATION = "/plan";

    public static final String SERVER_INFO_LOCATION_OLD = "/serverinfo";

    public static final String SERVER_INFO_LOCATION_NEW = "";

    public static final int API_VERSION_V1 = 1;

    public static final int API_VERSION_PRE_V1 = 0;

    public static final String FOLDER_STRUCTURE_PREFIX_NEW = "/routers/default";

    public static final String FOLDER_STRUCTURE_PREFIX_OLD = "";

    public static final String URL_ENCODING = "UTF-8";

    public static final String REFRESH_SERVER_RETURN_KEY = "RefreshServer";

    public static final String CHANGED_SELECTED_CUSTOM_SERVER_RETURN_KEY
            = "ChangedSelectedCustomServer";

    public static final String CHANGED_MAP_TILE_PROVIDER_RETURN_KEY = "ChangedMapTileProvider";

    public static final String LIVE_UPDATES_DISABLED_RETURN_KEY = "RefreshServer";

    public static final String CHANGED_PARAMETERS_MUST_REQUEST_TRIP_RETURN_KEY = "ChangedParameters";

    public static final String TAG_FRAGMENT_MAIN_FRAGMENT = "mainFragment";

    public static final String TAG_FRAGMENT_DATE_TIME_DIALOG = "dateTimeDialog";

    public static final String MAP_TILE_GOOGLE = "Google";

    public static final String MAP_TILE_GOOGLE_HYBRID = "Google hybrid";

    public static final String MAP_TILE_GOOGLE_NORMAL = "Google normal";

    public static final String MAP_TILE_GOOGLE_SATELLITE = "Google satellite";

    public static final String MAP_TILE_GOOGLE_TERRAIN = "Google terrain";

    public static final int CUSTOM_MAP_TILE_SMALL_HEIGHT = 256;

    public static final int CUSTOM_MAP_TILE_SMALL_WIDTH = 256;

    public static final int CUSTOM_MAP_TILE_BIG_HEIGHT = 512;

    public static final int CUSTOM_MAP_TILE_BIG_WIDTH = 512;

    public static final int CUSTOM_MAP_TILE_Z_INDEX = -1;

    public static final double BIKE_PARAMETERS_MIN_VALUE = 0.0;

    public static final double BIKE_PARAMETERS_QUICK_DEFAULT_VALUE = 1.0 / 3.0;

    public static final double BIKE_PARAMETERS_FLAT_DEFAULT_VALUE = 2.0 / 3.0;

    public static final double BIKE_PARAMETERS_MAX_VALUE = 1.0;

    public static final int CHECK_BOUNDS_ACCEPTABLE_ERROR = 1000;

    public static final float COORDINATES_IMPORTANT_DIFFERENCE = 2000f;

    public static final int ADDRESS_MAX_LINES_TO_SHOW = 5;

    //in meters
    public static final int GEOCODING_MAX_ERROR = 100;

    public static final int HTTP_CONNECTION_TIMEOUT = 15000;

    public static final int HTTP_SOCKET_TIMEOUT = 15000;

    public static final String BUNDLE_KEY_MAP_FAILED = "Map failed";

    public static final String BUNDLE_KEY_MAP_CAMERA = "Map Camera";

    public static final String BUNDLE_KEY_MAP_START_MARKER_POSITION = "Map StartMarkerPosition";

    public static final String BUNDLE_KEY_MAP_END_MARKER_POSITION = "Map EndMarkerPosition";

    public static final String BUNDLE_KEY_TB_START_LOCATION = "tbStartLocation";

    public static final String BUNDLE_KEY_TB_END_LOCATION = "tbEndLocation";

    public static final String BUNDLE_KEY_OTP_BUNDLE = "OTP bundle";

    public static final String BUNDLE_KEY_APP_STARTS = "app starts";

    public static final String BUNDLE_KEY_IS_START_LOCATION_GEOCODING_PROCESSED
            = "startlocation geocoding processed";

    public static final String BUNDLE_KEY_IS_END_LOCATION_GEOCODING_PROCESSED
            = "enlocation geocoding processed";

    public static final String BUNDLE_KEY_IS_START_LOCATION_CHANGED_BY_USER
            = "startlocation changed by user";

    public static final String BUNDLE_KEY_IS_END_LOCATION_CHANGED_BY_USER
            = "endlocation changed by user";

    public static final String BUNDLE_KEY_RESULT_TRIP_START_LOCATION = "result trip start location";

    public static final String BUNDLE_KEY_RESULT_TRIP_END_LOCATION = "result trip end location";

    public static final String BUNDLE_KEY_SAVED_LAST_LOCATION = "saved last location";

    public static final String BUNDLE_KEY_SAVED_LAST_LOCATION_CHECKED_FOR_SERVER
            = "saved last location checked for server";

    public static final String BUNDLE_KEY_TRIP_DATE = "trip date";

    public static final String BUNDLE_KEY_ARRIVE_BY = "arrive by";

    public static final String BUNDLE_KEY_TIMEPICKER_SAVED_HOUR = "timepicker hours";

    public static final String BUNDLE_KEY_TIMEPICKER_SAVED_MINUTE = "timepicker minutes";

    public static final String BUNDLE_KEY_SETTINGS_INTENT = "timepicker minutes";

    public static final String BUNDLE_KEY__IS_ALARM_BIKE_RENTAL_ACTIVE = "is alarm bike rental active";

    public static final String BUNDLE_KEY_INTENT_TRIP_ID = "intent trip id";

    public static final String BUNDLE_KEY_PREVIOUS_OPTIMIZATION = "previous Optimization";

    public static final String BUNDLE_KEY_PREVIOUS_MODES = "previous mode";

    public static final String BUNDLE_KEY_PREVIOUS_BIKE_TRIANGLE_MIN_VALUE = "previous bike triangle min value";

    public static final String BUNDLE_KEY_PREVIOUS_BIKE_TRIANGLE_MAX_VALUE = "previous bike triangle max value";

    public static final int COLOR_ROUTE_LINE = 0x7F0000FF;

    /**
     * Preference keys
     */
    public static final String PREFERENCE_KEY_ORIGIN_IS_MY_LOCATION = "origin_is_my_location";

    public static final String PREFERENCE_KEY_DESTINATION_IS_MY_LOCATION
            = "destination_is_my_location";

    public static final String PREFERENCE_KEY_MAP_TILE_SOURCE = "map_tile_source";

    public static final String PREFERENCE_KEY_GEOCODER_PROVIDER = "geocoder_provider";

    public static final String PREFERENCE_KEY_AUTO_DETECT_SERVER = "auto_detect_server";

    public static final String PREFERENCE_KEY_CUSTOM_SERVER_URL = "custom_server_url";

    public static final String PREFERENCE_KEY_CUSTOM_SERVER_BOUNDS = "custom_server_bounds";

    public static final String PREFERENCE_KEY_CUSTOM_SERVER_URL_IS_VALID
            = "custom_server_url_is_valid";

    public static final String PREFERENCE_KEY_REFRESH_SERVER_LIST = "refresh_server_list";

    public static final String PREFERENCE_KEY_OTP_PROVIDER_FEEDBACK = "otp_provider_feedback";

    public static final String PREFERENCE_KEY_MAX_WALKING_DISTANCE = "max_walking_distance";

    public static final String PREFERENCE_KEY_WHEEL_ACCESSIBLE = "wheelchair_accessible";

    public static final String PREFERENCE_KEY_SELECTED_SERVER = "selected_server";

    public static final String PREFERENCE_KEY_SELECTED_CUSTOM_SERVER = "selected_custom_server";

    public static final String PREFERENCE_KEY_USE_ANDROID_GEOCODER = "use_android_geocoder";

    public static final String PREFERENCE_KEY_USE_INTELLIGENT_MARKERS = "use_intelligent_markers";

    public static final String PREFERENCE_KEY_USE_DEVICE_TIMEZONE = "use_device_timezone";

    public static final String PREFERENCE_KEY_LAST_TRAVERSE_MODE_SET = "last_traverse_mode_set";

    public static final String PREFERENCE_KEY_LAST_BIKE_TRIANGLE_MIN_VALUE = "last_bike_triangle_min_value";

    public static final String PREFERENCE_KEY_LAST_BIKE_TRIANGLE_MAX_VALUE = "last_bike_triangle_max_value";

    public static final String PREFERENCE_KEY_LAST_OPTIMIZATION = "last_optimization";

    public static final String PREFERENCE_KEY_API_VERSION = "last_api_version";

    public static final String PREFERENCE_KEY_FOLDER_STRUCTURE_PREFIX = "folder_structure_prefix";

    public static final String PREFERENCE_KEY_LIVE_UPDATES = "live_updates";

    public static final String PREFERENCE_KEY_LIVE_UPDATES_CATEGORY = "live_updates_category";

    public static final String PREFERENCE_KEY_REALTIME_AVAILABLE = "real_time_available";

    public static final String PREFERENCE_KEY_PREFERENCE_SCREEN = "preferences_screen";

    public static final String PREFERENCE_KEY_APP_VERSION = "app_version";

    public static final String PREFERENCE_KEY_EXECUTED_VERSION_CODE_13 = "executed_version_13";

    public static final String PREFERENCE_KEY_ABOUT = "about";

    public static final String PREFERENCE_KEY_ABOUT_CATEGORY = "about_category";

    private static Server selectedServer;

    public static final String TAG = "OTP";

    public static final int EXPIRATION_DAYS_FOR_SERVER_LIST = 3;

    public static final float defaultInitialZoomLevel = 12;

    public static final float defaultMediumZoomLevel = 14;

    public static final float defaultMyLocationZoomLevel = 16;

    public static final String FORMAT_COORDINATES = "#.00000";

    public static final String FORMAT_DISTANCE_METERS = "%.0f";

    public static final String FORMAT_DISTANCE_KILOMETERS = "%.1f";

    public static final String FORMAT_OTP_SERVER_DATE_RESPONSE = "yyyy-MM-dd\'T\'HH:mm:ssZZ";

    public static final String FORMAT_OTP_SERVER_DATE_QUERY = "MM/dd/yy";

    public static final String FORMAT_OTP_SERVER_TIME_QUERY = "kk:mm";

    public static final String OTP_RENTAL_QUALIFIER = "_RENT";

    public static final long DEFAULT_UPDATE_INTERVAL_BIKE_RENTAL = TimeUnit.SECONDS.toMillis(40);

    public static final long DEFAULT_UPDATE_INTERVAL_TRIP_TIME = TimeUnit.SECONDS.toMillis(60);

    public static final String INTENT_UPDATE_BIKE_RENTAL_ACTION
            = "edu.usf.cutr.opentripplanner.android.OpenTripPlanner.UPDATE_BIKE_RENTAL";

    public static final String INTENT_UPDATE_TRIP_TIME_ACTION
            = "edu.usf.cutr.opentripplanner.android.OpenTripPlanner.UPDATE_TRIP_TIME";

    public static final String INTENT_NOTIFICATION_ACTION_OPEN_APP
            = "edu.usf.cutr.opentripplanner.android.OpenTripPlanner.NOTIFICATION_OPEN_APP";

    public static final String INTENT_NOTIFICATION_ACTION_DISMISS_UPDATES
            = "edu.usf.cutr.opentripplanner.android.OpenTripPlanner.NOTIFICATION_DISMISS_UPDATES";

    public static final String INTENT_NOTIFICATION_RESUME_APP_WITH_TRIP_ID
            = "edu.usf.cutr.opentripplanner.android.OpenTripPlanner.NOTIFICATION_RESUME";

    public static final int NOTIFICATION_ID = 0;

    /**
     * Sets the currently selected OTP server
     *
     * @param sServer the selected OTP server
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
