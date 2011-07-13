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

package org.opentripplanner.android;

import java.util.ArrayList;

import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;

public class SettingsActivity extends PreferenceActivity {
	private ListPreference mapTileProvider;
	private PreferenceCategory routingOptions;
	private CheckBoxPreference autoDetectServer;
	private EditTextPreference customServerURL;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	
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
		
	}
	
	private void hideCustomURLPref(Boolean hidePref) {
		if(!hidePref) {
			routingOptions.addItemFromInflater(customServerURL);
		} else if (hidePref) {
			routingOptions.removePreference(customServerURL);
		}
	}
}
