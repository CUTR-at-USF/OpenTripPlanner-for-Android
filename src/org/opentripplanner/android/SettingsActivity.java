package org.opentripplanner.android;

import java.util.ArrayList;

import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;

public class SettingsActivity extends PreferenceActivity {
	private ListPreference mapTileProvider;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	
		addPreferencesFromResource(R.xml.preferences);
		
		mapTileProvider = (ListPreference) findPreference("map_tile_source");
		
		ArrayList<CharSequence> names = new ArrayList<CharSequence>();
		ArrayList<ITileSource> tiles = TileSourceFactory.getTileSources();
	
		for (ITileSource iTileSource : tiles) {
			names.add(iTileSource.name());
		}

		mapTileProvider.setEntries(names.toArray(new CharSequence[names.size()]));
		mapTileProvider.setEntryValues(names.toArray(new CharSequence[names.size()]));
		mapTileProvider.setDefaultValue("Mapnik");
	}
}
