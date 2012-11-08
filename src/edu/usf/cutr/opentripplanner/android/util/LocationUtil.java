/*
 * Copyright 2012 University of South Florida
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

package edu.usf.cutr.opentripplanner.android.util;

import java.util.ArrayList;
import java.util.List;

import org.osmdroid.util.GeoPoint;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;

/**
 * 
 * @author Khoa Tran
 * 
 */

public class LocationUtil {
	/**
	 * Amount of time that a location is considered valid for that we will still
	 * use it as a starting location and snap the map to this location
	 */
	private static final int STALE_LOCATION_THRESHOLD = 60 * 60 * 1000; // 60
																		// minutes

	/*
	 * Get the last location the phone was at Based off example at
	 * http://www.androidsnippets
	 * .com/get-the-phones-last-known-location-using-locationmanager
	 * 
	 * @return GeoPoint of last location, or null if a location hasn't been
	 * acquired in the last STALE_LOCATION_THRESHOLD amount of time
	 */
	public static GeoPoint getLastLocation(Context context) {
		LocationManager lm = (LocationManager) context
				.getSystemService(Context.LOCATION_SERVICE);
		List<String> providers = lm.getProviders(true);
		Location l = null;

		for (int i = providers.size() - 1; i >= 0; i--) {
			l = lm.getLastKnownLocation(providers.get(i));
			if (l != null
					&& l.getProvider().equalsIgnoreCase(
							LocationManager.GPS_PROVIDER)) // Only break if we
															// have a GPS fix
															// location, since
															// this will be the
															// most accurate
															// location
															// provider. We want
															// to make sure we
															// loop through all
															// of them to find
															// GPS if available
				break;
		}

		if (l == null
				|| (Math.abs((System.currentTimeMillis() - l.getTime())) > STALE_LOCATION_THRESHOLD)) { // Check
																										// to
																										// make
																										// sure
																										// the
																										// location
																										// is
																										// recent
																										// (use
																										// ABS()
																										// to
																										// allow
																										// for
																										// small
																										// time
																										// sync
																										// differences
																										// between
																										// GPS
																										// clock
																										// and
																										// system
																										// clock)

			return null; // return null if no location was found in the last
							// STALE_LOCATION_THRESHOLD amount of time
		}

		return new GeoPoint(l);
	}

	// Borrowed from
	// http://jeffreysambells.com/posts/2010/05/27/decoding-polylines-from-google-maps-direction-api-with-java/
	/**
	 * Decode a set of GeoPoints from an EncodedPolylineBean object from the OTP server project
	 * @param encoded string from EncodedPolylineBean
	 * @return set of GeoPoints represented by the EncodedPolylineBean string
	 */
	public static List<GeoPoint> decodePoly(String encoded) {

		List<GeoPoint> poly = new ArrayList<GeoPoint>();
		int index = 0, len = encoded.length();
		int lat = 0, lng = 0;

		while (index < len) {
			int b, shift = 0, result = 0;
			do {
				b = encoded.charAt(index++) - 63;
				result |= (b & 0x1f) << shift;
				shift += 5;
			} while (b >= 0x20);
			int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
			lat += dlat;

			shift = 0;
			result = 0;
			do {
				b = encoded.charAt(index++) - 63;
				result |= (b & 0x1f) << shift;
				shift += 5;
			} while (b >= 0x20);
			int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
			lng += dlng;

			GeoPoint p = new GeoPoint((int) (((double) lat / 1E5) * 1E6),
					(int) (((double) lng / 1E5) * 1E6));
			poly.add(p);
		}

		return poly;
	}
}
