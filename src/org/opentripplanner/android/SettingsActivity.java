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
