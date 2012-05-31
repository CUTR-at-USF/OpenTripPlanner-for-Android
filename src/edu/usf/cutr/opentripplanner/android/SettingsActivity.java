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

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;

import edu.usf.cutr.opentripplanner.android.R;
import edu.usf.cutr.opentripplanner.android.model.Server;
import edu.usf.cutr.opentripplanner.android.sqlite.ServersDataSource;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.util.Log;
import android.widget.Toast;

/*
 * Modified by Khoa Tran
 */
public class SettingsActivity extends PreferenceActivity {
	private ListPreference mapTileProvider;
	private PreferenceCategory routingOptions;
	private CheckBoxPreference autoDetectServer;
	private EditTextPreference customServerURL;
	private Preference providerFeedbackButton;
	private Preference serverRefreshButton;
	
	private ServersDataSource datasource;

	private final String TAG = "OTP";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setDatasource(new ServersDataSource(this));

		addPreferencesFromResource(R.xml.preferences);

		mapTileProvider = (ListPreference) findPreference("map_tile_source");
		routingOptions = (PreferenceCategory) findPreference("routing_options");
		autoDetectServer = (CheckBoxPreference) findPreference("auto_detect_server");
		customServerURL = (EditTextPreference) findPreference("custom_server_url");

		ArrayList<CharSequence> names = new ArrayList<CharSequence>();
		ArrayList<ITileSource> tiles = TileSourceFactory.getTileSources();

		for (ITileSource iTileSource : tiles) {
			names.add(iTileSource.name());
		}

		mapTileProvider.setEntries(names.toArray(new CharSequence[names.size()]));
		mapTileProvider.setEntryValues(names.toArray(new CharSequence[names.size()]));
		mapTileProvider.setDefaultValue("Mapnik");

		hideCustomURLPref(autoDetectServer.isChecked());

		autoDetectServer.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				Boolean value = (Boolean) newValue;
				hideCustomURLPref(value);
				return true;
			}
		});

		providerFeedbackButton = (Preference)findPreference("otp_provider_feedback");
		providerFeedbackButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference arg0) {
				Log.v(TAG, "Provider Feedback Button clicked");
				String recipient = getString(R.string.email_otp_android_developer);
	        	
	        	String uriText = "mailto:"+recipient;
	        	
	        	String subject = "";
	            subject += "Android OTP user report problem(s) ";
	            Calendar c = Calendar.getInstance(); 
	            Date d = c.getTime();
	            subject += "[" + d.toGMTString() + "]";
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
		Date mostRecentDate = datasource.getMostRecentDate();
		
		serverRefreshButton = (Preference)findPreference("refresh_server_list");
		serverRefreshButton.setSummary("Server List Downloaded on "+mostRecentDate.toString());
		
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

	private void hideCustomURLPref(Boolean hidePref) {
		if(!hidePref) {
			routingOptions.addItemFromInflater(customServerURL);
		} else if (hidePref) {
			routingOptions.removePreference(customServerURL);
		}
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
}
