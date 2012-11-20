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
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.PreferenceCategory;
import edu.usf.cutr.opentripplanner.android.model.Server;

/*
 * Modified by Khoa Tran
 */

public class OTPApp extends Application {
	
	public static final int REFRESH_SERVER_LIST_REQUEST_CODE = 2;
	
	public static final int CHOOSE_CONTACT_REQUEST_CODE = 1;
	
	public static final String REFRESH_SERVER_RETURN_KEY = "RefreshServer";
	
	/**
	 * Preference keys
	 */
	public static final String PREFERENCE_KEY_MAP_TILE_SOURCE = "map_tile_source";
	public static final String PREFERENCE_KEY_GEOCODER_PROVIDER = "geocoder_provider";
	public static final String PREFERENCE_KEY_ROUTING_OPTIONS = "routing_options";
	public static final String PREFERENCE_KEY_AUTO_DETECT_SERVER = "auto_detect_server";
	public static final String PREFERENCE_KEY_CUSTOM_SERVER_URL = "custom_server_url";
	public static final String PREFERENCE_KEY_REFRESH_SERVER_LIST = "refresh_server_list";
	public static final String PREFERENCE_KEY_OTP_PROVIDER_FEEDBACK = "otp_provider_feedback";
	public static final String PREFERENCE_KEY_MAX_WALKING_DISTANCE = "max_walking_distance";
	public static final String PREFERENCE_KEY_WHEEL_ACCESSIBLE = "wheelchair_accessible";
	
	private static Server selectedServer;
	
	public static final String TAG = "OTP";

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
