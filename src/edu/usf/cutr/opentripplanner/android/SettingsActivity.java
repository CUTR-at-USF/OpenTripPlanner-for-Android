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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import com.google.android.gms.maps.GoogleMap;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.webkit.URLUtil;
import android.widget.Toast;
import edu.usf.cutr.opentripplanner.android.listeners.ServerCheckerCompleteListener;
import edu.usf.cutr.opentripplanner.android.model.Server;
import edu.usf.cutr.opentripplanner.android.sqlite.ServersDataSource;
import edu.usf.cutr.opentripplanner.android.tasks.ServerChecker;

/*
 * Modified by Khoa Tran
 */
public class SettingsActivity extends PreferenceActivity implements ServerCheckerCompleteListener{
	private ListPreference mapTileProvider;
	private CheckBoxPreference autoDetectServer;
	private EditTextPreference customServerURL;
	private Preference providerFeedbackButton;
	private Preference serverRefreshButton;
	private CheckBoxPreference selectedCustomServer;
	private ListPreference geocoderProvider;
	private EditTextPreference maxWalkingDistance;
	
	private ServersDataSource datasource;

	private final String TAG = "OTP";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setDatasource(new ServersDataSource(this));

		addPreferencesFromResource(R.xml.preferences);

		mapTileProvider = (ListPreference) findPreference(OTPApp.PREFERENCE_KEY_MAP_TILE_SOURCE);
		geocoderProvider = (ListPreference) findPreference(OTPApp.PREFERENCE_KEY_GEOCODER_PROVIDER);
		autoDetectServer = (CheckBoxPreference) findPreference(OTPApp.PREFERENCE_KEY_AUTO_DETECT_SERVER);
		customServerURL = (EditTextPreference) findPreference(OTPApp.PREFERENCE_KEY_CUSTOM_SERVER_URL);
		selectedCustomServer = (CheckBoxPreference) findPreference(OTPApp.PREFERENCE_KEY_SELECTED_CUSTOM_SERVER);
		maxWalkingDistance = (EditTextPreference) findPreference(OTPApp.PREFERENCE_KEY_MAX_WALKING_DISTANCE);

		
		String[] entriesArray = getResources().getStringArray(R.array.map_tiles_servers_names);
		ArrayList<String> entries = new ArrayList<String>(Arrays.asList(entriesArray));
		entries.add(OTPApp.MAP_TILE_GOOGLE_NORMAL);
		entries.add(OTPApp.MAP_TILE_GOOGLE_SATELLITE);
		entries.add(OTPApp.MAP_TILE_GOOGLE_HYBRID);
		entries.add(OTPApp.MAP_TILE_GOOGLE_TERRAIN);
		mapTileProvider.setEntries(entries.toArray(new CharSequence[entries.size()]));
		
		String[] entriesValuesArray = getResources().getStringArray(R.array.map_tiles_servers_urls);
		ArrayList<String> entriesValues = new ArrayList<String>(Arrays.asList(entriesValuesArray));
		entriesValues.add(OTPApp.MAP_TILE_GOOGLE_NORMAL);
		entriesValues.add(OTPApp.MAP_TILE_GOOGLE_SATELLITE);
		entriesValues.add(OTPApp.MAP_TILE_GOOGLE_HYBRID);
		entriesValues.add(OTPApp.MAP_TILE_GOOGLE_TERRAIN);
		mapTileProvider.setEntryValues(entriesValues.toArray(new CharSequence[entriesValues.size()]));			
		mapTileProvider.setDefaultValue(getResources().getString(R.string.map_tiles_default_server));

		CharSequence geocoders[] = {getResources().getString(R.string.geocoder_nominatim), getResources().getString(R.string.geocoder_google_places)};
		geocoderProvider.setEntries(geocoders);
		geocoderProvider.setEntryValues(geocoders);
		geocoderProvider.setDefaultValue(getResources().getString(R.string.geocoder_nominatim));
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		
		if (prefs.getString(OTPApp.PREFERENCE_KEY_GEOCODER_PROVIDER, getResources().getString(R.string.geocoder_nominatim))
			.equals(getResources().getString(R.string.geocoder_nominatim))){
			geocoderProvider.setSummary(getResources().getString(R.string.geocoder_preference_provider_nominatim));
		}
		else{
			geocoderProvider.setSummary(getResources().getString(R.string.geocoder_preference_provider_google_places));
		}
		
		geocoderProvider.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				String value = (String) newValue;
				
				if (value.equals(getResources().getString(R.string.geocoder_nominatim))){
					geocoderProvider.setSummary(getResources().getString(R.string.geocoder_preference_provider_nominatim));
				}
				else{
					geocoderProvider.setSummary(getResources().getString(R.string.geocoder_preference_provider_google_places));	
				}

				return true;
			}

		});
		
		String actualMapTileProvider = prefs.getString(OTPApp.PREFERENCE_KEY_MAP_TILE_SOURCE, getResources().getString(R.string.tiles_mapnik));
		
		if (actualMapTileProvider.equals(getResources().getString(R.string.tiles_mapnik))){
			mapTileProvider.setSummary(getResources().getString(R.string.mapnik));
		}
		else if (actualMapTileProvider.equals(getResources().getString(R.string.tiles_maquest))){
			mapTileProvider.setSummary(getResources().getString(R.string.maquest));
		}
		else if (actualMapTileProvider.equals(getResources().getString(R.string.tiles_cyclemap))){
			mapTileProvider.setSummary(getResources().getString(R.string.cyclemap));
		}
		else if (actualMapTileProvider.equals(OTPApp.MAP_TILE_GOOGLE_NORMAL)){
			mapTileProvider.setSummary(OTPApp.MAP_TILE_GOOGLE_NORMAL);
		}
		else if (actualMapTileProvider.equals(OTPApp.MAP_TILE_GOOGLE_HYBRID)){
			mapTileProvider.setSummary(OTPApp.MAP_TILE_GOOGLE_NORMAL);
		}
		else if (actualMapTileProvider.equals(OTPApp.MAP_TILE_GOOGLE_SATELLITE)){
			mapTileProvider.setSummary(OTPApp.MAP_TILE_GOOGLE_NORMAL);
		}
		else if (actualMapTileProvider.equals(OTPApp.MAP_TILE_GOOGLE_TERRAIN)){
			mapTileProvider.setSummary(OTPApp.MAP_TILE_GOOGLE_NORMAL);
		}
		
		mapTileProvider.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				String value = (String) newValue;
				
				if (value.equals(getResources().getString(R.string.tiles_mapnik))){
					mapTileProvider.setSummary(getResources().getString(R.string.mapnik));
				}
				else if (value.equals(getResources().getString(R.string.tiles_maquest))){
					mapTileProvider.setSummary(getResources().getString(R.string.maquest));
				}
				else if (value.equals(getResources().getString(R.string.tiles_cyclemap))){
					mapTileProvider.setSummary(getResources().getString(R.string.cyclemap));
				}
				else if (value.equals(OTPApp.MAP_TILE_GOOGLE_NORMAL)){
					mapTileProvider.setSummary(OTPApp.MAP_TILE_GOOGLE_NORMAL);
				}
				else if (value.equals(OTPApp.MAP_TILE_GOOGLE_HYBRID)){
					mapTileProvider.setSummary(OTPApp.MAP_TILE_GOOGLE_NORMAL);
				}
				else if (value.equals(OTPApp.MAP_TILE_GOOGLE_SATELLITE)){
					mapTileProvider.setSummary(OTPApp.MAP_TILE_GOOGLE_NORMAL);
				}
				else if (value.equals(OTPApp.MAP_TILE_GOOGLE_TERRAIN)){
					mapTileProvider.setSummary(OTPApp.MAP_TILE_GOOGLE_NORMAL);
				}

				return true;
			}

		});
		
		maxWalkingDistance.setSummary(maxWalkingDistance.getText() + " " + getResources().getString(R.string.maximum_walk_description));	

		maxWalkingDistance.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				String value = (String) newValue;
				
				maxWalkingDistance.setSummary(value + " " + getResources().getString(R.string.maximum_walk_description));	

				return true;
			}

		});

		
		if (prefs.getBoolean(OTPApp.PREFERENCE_KEY_CUSTOM_SERVER_URL_IS_VALID, false)){
			customServerURL.setSummary(getResources().getString(R.string.custom_server_url_description));
		}
		else{
			selectedCustomServer.setEnabled(false);
			customServerURL.setSummary(getResources().getString(R.string.custom_server_url_error));
		}
		
		if (selectedCustomServer.isEnabled() && selectedCustomServer.isChecked()){
			autoDetectServer.setEnabled(false);
		}
		
		selectedCustomServer.setDependency(OTPApp.PREFERENCE_KEY_CUSTOM_SERVER_URL);

		customServerURL.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				String value = (String) newValue;
				
						
				if (URLUtil.isValidUrl(value)){		
					ServerChecker serverChecker = new ServerChecker(SettingsActivity.this, SettingsActivity.this, false);
					serverChecker.execute(new Server(value));
					return true;
				}
				
				Toast.makeText(SettingsActivity.this.getApplicationContext(), SettingsActivity.this.getApplicationContext().getResources().getString(R.string.custom_server_url_error), Toast.LENGTH_SHORT).show();
				
				customServerURL.setSummary(getResources().getString(R.string.custom_server_url_error));
						
				Intent returnIntent = new Intent();
				setResult(RESULT_OK, returnIntent);
				return false;
			}
		});

		selectedCustomServer.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				Boolean value = (Boolean) newValue;
				if (value){
					autoDetectServer.setChecked(false);
					autoDetectServer.setEnabled(false);
				}
				else{
					autoDetectServer.setEnabled(true);
				}
				
				Log.v(TAG, "Custom server Button clicked");
				Intent returnIntent = new Intent();
				setResult(RESULT_OK, returnIntent);
				
				return true;
			}
		});
		
		autoDetectServer.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				Boolean value = (Boolean) newValue;
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
				
				if (value){
					selectedCustomServer.setEnabled(false);
				}
				else{
					if (prefs.getBoolean(OTPApp.PREFERENCE_KEY_CUSTOM_SERVER_URL_IS_VALID, false)){
						selectedCustomServer.setEnabled(true);
					}
				}
				
				return true;
			}
		});

		providerFeedbackButton = (Preference)findPreference(OTPApp.PREFERENCE_KEY_OTP_PROVIDER_FEEDBACK);
		providerFeedbackButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference arg0) {
				Log.v(TAG, "Provider Feedback Button clicked");
				String recipient = getString(R.string.email_otp_android_developer);
	        	
	        	String uriText = "mailto:"+recipient;
	        	
	        	String subject = "";
	            subject += "Android OTP user report problem(s) ";
	            Date d = Calendar.getInstance().getTime(); 
	            subject += "[" + d.toString() + "]";
	            uriText += "?subject=" + subject;

	        	Uri uri = Uri.parse(uriText);

	        	Intent sendIntent = new Intent(Intent.ACTION_SENDTO);
	        	sendIntent.setData(uri);
	        	startActivity(Intent.createChooser(sendIntent, "Send email")); 

				return true;
			}
		});
		
		ServersDataSource datasource = this.getDatasource();
		datasource.open();
		Long mostRecentDate = datasource.getMostRecentDate();
		
		serverRefreshButton = (Preference)findPreference(OTPApp.PREFERENCE_KEY_REFRESH_SERVER_LIST);
		
		if(mostRecentDate != null){
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(mostRecentDate);
			serverRefreshButton.setSummary("Server List Downloaded on "+ cal.getTime());
		}else{
			serverRefreshButton.setSummary("Last Server List Download Unknown");
		}
		
		serverRefreshButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference arg0) {
				Log.v(TAG, "Server Refresh Button clicked");
				Intent returnIntent = new Intent();
				returnIntent.putExtra(OTPApp.REFRESH_SERVER_RETURN_KEY, true);
				setResult(RESULT_OK, returnIntent);
				finish();
				return true;
			}
		});
		
		datasource.close();
	}


	/**
	 * @return the datasource
	 */
	public ServersDataSource getDatasource() {
		return datasource;
	}

	/**
	 * @param datasource the datasource to set
	 */
	public void setDatasource(ServersDataSource datasource) {
		this.datasource = datasource;
	}

	@Override
	public void onServerCheckerComplete(String result, boolean isWorking) {
		SharedPreferences.Editor prefsEditor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
		if (isWorking){
			selectedCustomServer.setEnabled(true);
			if (selectedCustomServer.isChecked()){
				prefsEditor.putBoolean(OTPApp.PREFERENCE_KEY_SELECTED_CUSTOM_SERVER, true);
			}
			customServerURL.setSummary(getResources().getString(R.string.custom_server_url_description));
			prefsEditor.putBoolean(OTPApp.PREFERENCE_KEY_CUSTOM_SERVER_URL_IS_VALID, true);
		}
		else{
			autoDetectServer.setEnabled(true);
			selectedCustomServer.setChecked(false);
			selectedCustomServer.setEnabled(false);		
			customServerURL.setSummary(getResources().getString(R.string.custom_server_error));
			prefsEditor.putBoolean(OTPApp.PREFERENCE_KEY_CUSTOM_SERVER_URL_IS_VALID, false);
			prefsEditor.putBoolean(OTPApp.PREFERENCE_KEY_SELECTED_CUSTOM_SERVER, false);
		}
		
		prefsEditor.commit();
	}
}
