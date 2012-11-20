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

import edu.usf.cutr.opentripplanner.android.model.Server;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;

/**
 * Various utilities related to location data
 * 
 * @author Khoa Tran
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
	 * Decode a set of GeoPoints from an EncodedPolylineBean object from the OTP
	 * server project
	 * 
	 * @param encoded
	 *            string from EncodedPolylineBean
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
	
	/**
	 * Compares the current location of the user against a bounding box for a OTP server
	 * 
	 * @param location current location of the user
	 * @param selectedServer OTP server being compared to the current location
	 * @param acceptableError the amount of allowed error, in meters
	 * @return true if the location of the user is within the bounding box of the selectedServer, false if it is not
	 */
	public static boolean checkPointInBoundingBox(GeoPoint location, Server selectedServer, int acceptableError){
		float[] resultLeft = new float[3];
		float[] resultRight = new float[3];
		float[] resultUp = new float[3];
		float[] resultDown = new float[3];
		float[] resultHorizontal = new float[3];
		float[] resultVertical = new float[3];

		double locationLat = location.getLatitudeE6()/ 1E6;
		double locationLon = location.getLongitudeE6()/ 1E6;

		double leftLat = locationLat;
		double leftLon = selectedServer.getLowerLeftLongitude();
		double rightLat = locationLat;
		double rightLon = selectedServer.getUpperRightLongitude();

		Location.distanceBetween(locationLat, locationLon, leftLat, leftLon, resultLeft);
		Location.distanceBetween(locationLat, locationLon, rightLat, rightLon, resultRight);

		double upLat = selectedServer.getUpperRightLatitude();
		double upLon = locationLon;
		double downLat = selectedServer.getLowerLeftLatitude();
		double downLon = locationLon;

		Location.distanceBetween(locationLat, locationLon, upLat, upLon, resultUp);
		Location.distanceBetween(locationLat, locationLon, downLat, downLon, resultDown);

		Location.distanceBetween(upLat, leftLon, upLat, rightLon, resultHorizontal);
		Location.distanceBetween(upLat, leftLon, downLat, leftLon, resultVertical);

		if(resultLeft[0]+resultRight[0]-resultHorizontal[0] > acceptableError){
			return false;
		} else if(resultUp[0]+resultDown[0]-resultVertical[0] > acceptableError){
			return false;
		}

		return true;
	}
}
