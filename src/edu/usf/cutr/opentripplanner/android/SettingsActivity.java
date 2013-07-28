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

import static edu.usf.cutr.opentripplanner.android.OTPApp.PREFERENCE_KEY_AUTO_DETECT_SERVER;
import static edu.usf.cutr.opentripplanner.android.OTPApp.PREFERENCE_KEY_CUSTOM_SERVER_URL;
import static edu.usf.cutr.opentripplanner.android.OTPApp.PREFERENCE_KEY_CUSTOM_SERVER_URL_IS_VALID;
import static edu.usf.cutr.opentripplanner.android.OTPApp.PREFERENCE_KEY_GEOCODER_PROVIDER;
import static edu.usf.cutr.opentripplanner.android.OTPApp.PREFERENCE_KEY_MAP_TILE_SOURCE;
import static edu.usf.cutr.opentripplanner.android.OTPApp.PREFERENCE_KEY_OTP_PROVIDER_FEEDBACK;
import static edu.usf.cutr.opentripplanner.android.OTPApp.PREFERENCE_KEY_REFRESH_SERVER_LIST;
import static edu.usf.cutr.opentripplanner.android.OTPApp.PREFERENCE_KEY_SELECTED_CUSTOM_SERVER;
import static edu.usf.cutr.opentripplanner.android.OTPApp.REFRESH_SERVER_RETURN_KEY;

import java.util.Calendar;
import java.util.Date;

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
	
	private ServersDataSource datasource;

	private final String TAG = "OTP";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setDatasource(new ServersDataSource(this));

		addPreferencesFromResource(R.xml.preferences);

		mapTileProvider = (ListPreference) findPreference(PREFERENCE_KEY_MAP_TILE_SOURCE);
		geocoderProvider = (ListPreference) findPreference(PREFERENCE_KEY_GEOCODER_PROVIDER);
		autoDetectServer = (CheckBoxPreference) findPreference(PREFERENCE_KEY_AUTO_DETECT_SERVER);
		customServerURL = (EditTextPreference) findPreference(PREFERENCE_KEY_CUSTOM_SERVER_URL);
		selectedCustomServer = (CheckBoxPreference) findPreference(PREFERENCE_KEY_SELECTED_CUSTOM_SERVER);

		mapTileProvider.setDefaultValue(getResources().getString(R.string.map_tiles_default_server));
		mapTileProvider.setEntries(R.array.map_tiles_servers_names);
		mapTileProvider.setEntryValues(R.array.map_tiles_servers_urls);
		
		String[] availableGeocoderProviders = getResources().getStringArray(R.array.available_geocoder_providers);
		geocoderProvider.setEntries(availableGeocoderProviders);
		geocoderProvider.setEntryValues(availableGeocoderProviders);
		geocoderProvider.setDefaultValue(availableGeocoderProviders[0]);
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		if (prefs.getBoolean(PREFERENCE_KEY_CUSTOM_SERVER_URL_IS_VALID, false)){
			if (selectedCustomServer.isChecked()){
				selectedCustomServer.setSummary(getResources().getString(R.string.selected_custom_server_summary_activate));
			}
			else{
				selectedCustomServer.setSummary(getResources().getString(R.string.selected_custom_server_summary_disactivate));
			}
			customServerURL.setSummary(getResources().getString(R.string.custom_server_url_description));
		}
		else{
			selectedCustomServer.setSummary(getResources().getString(R.string.selected_custom_server_summary_disabled));
			selectedCustomServer.setEnabled(false);
			customServerURL.setSummary(getResources().getString(R.string.custom_server_url_error));
		}
		
		if (selectedCustomServer.isEnabled() && selectedCustomServer.isChecked()){
			autoDetectServer.setEnabled(false);
		}
		
		selectedCustomServer.setDependency(PREFERENCE_KEY_CUSTOM_SERVER_URL);

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
					selectedCustomServer.setSummary(getResources().getString(R.string.selected_custom_server_summary_activate));
				}
				else{
					autoDetectServer.setEnabled(true);
					selectedCustomServer.setSummary(getResources().getString(R.string.selected_custom_server_summary_disactivate));
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
					if (prefs.getBoolean(PREFERENCE_KEY_CUSTOM_SERVER_URL_IS_VALID, false)){
						selectedCustomServer.setEnabled(true);
					}
				}
				
				return true;
			}
		});

		providerFeedbackButton = (Preference)findPreference(PREFERENCE_KEY_OTP_PROVIDER_FEEDBACK);
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
		
		serverRefreshButton = (Preference)findPreference(PREFERENCE_KEY_REFRESH_SERVER_LIST);
		
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
				returnIntent.putExtra(REFRESH_SERVER_RETURN_KEY, true);
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
				selectedCustomServer.setSummary(getResources().getString(R.string.selected_custom_server_summary_activate));
				prefsEditor.putBoolean(PREFERENCE_KEY_SELECTED_CUSTOM_SERVER, true);
			}
			else{
				selectedCustomServer.setSummary(getResources().getString(R.string.selected_custom_server_summary_disactivate));
			}
			customServerURL.setSummary(getResources().getString(R.string.custom_server_url_description));
			prefsEditor.putBoolean(PREFERENCE_KEY_CUSTOM_SERVER_URL_IS_VALID, true);
		}
		else{
			autoDetectServer.setEnabled(true);
			selectedCustomServer.setChecked(false);
			selectedCustomServer.setEnabled(false);		
			selectedCustomServer.setSummary(getResources().getString(R.string.selected_custom_server_summary_disabled));
			customServerURL.setSummary(getResources().getString(R.string.custom_server_error));
			prefsEditor.putBoolean(PREFERENCE_KEY_CUSTOM_SERVER_URL_IS_VALID, false);
			prefsEditor.putBoolean(PREFERENCE_KEY_SELECTED_CUSTOM_SERVER, false);
		}
		
		prefsEditor.commit();
	}
}
