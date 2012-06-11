package edu.usf.cutr.opentripplanner.android.util;

import java.util.List;

import org.osmdroid.util.GeoPoint;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;

public class LocationUtil {
	/**
	 * Amount of time that a location is considered valid for that we will still use it as a starting location and snap the map to this location
	 */
	private static final int STALE_LOCATION_THRESHOLD = 60 * 60 * 1000;  //60 minutes
	
	/*
	 * Get the last location the phone was at
	 *  Based off example at http://www.androidsnippets.com/get-the-phones-last-known-location-using-locationmanager
	 * 
	 * @return GeoPoint of last location, or null if a location hasn't been acquired in the last STALE_LOCATION_THRESHOLD amount of time
	 */
	public static GeoPoint getLastLocation(Context context) {
		LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		List<String> providers = lm.getProviders(true);
		Location l = null;

		for (int i = providers.size() - 1; i >= 0; i--) {
			l = lm.getLastKnownLocation(providers.get(i));
			if (l != null && l.getProvider().equalsIgnoreCase(LocationManager.GPS_PROVIDER))  //Only break if we have a GPS fix location, since this will be the most accurate location provider.  We want to make sure we loop through all of them to find GPS if available
				break;
		}

		if (l == null  || (Math.abs((System.currentTimeMillis() - l.getTime())) > STALE_LOCATION_THRESHOLD)) {  //Check to make sure the location is recent (use ABS() to allow for small time sync differences between GPS clock and system clock)

			return null; //return null if no location was found in the last STALE_LOCATION_THRESHOLD amount of time
		}

		return new GeoPoint(l);
	}
}
