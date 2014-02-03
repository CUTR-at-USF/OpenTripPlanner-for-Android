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

import com.google.android.gms.maps.model.LatLng;

import android.location.Location;

import java.util.ArrayList;
import java.util.List;

import edu.usf.cutr.opentripplanner.android.model.Server;

/**
 * Various utilities related to location data
 *
 * @author Khoa Tran
 */

public class LocationUtil {

    // Borrowed from
    // http://jeffreysambells.com/posts/2010/05/27/decoding-polylines-from-google-maps-direction-api-with-java/

    /**
     * Decode a set of GeoPoints from an EncodedPolylineBean object from the OTP
     * server project
     *
     * @param encoded string from EncodedPolylineBean
     * @return set of GeoPoints represented by the EncodedPolylineBean string
     */
    public static List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<LatLng>();
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

            LatLng ll = new LatLng(((double) lat / 1E5),
                    (((double) lng / 1E5)));
            poly.add(ll);
        }

        return poly;
    }

    /**
     * Compares the current location of the user against a bounding box for a OTP server
     *
     * @param location        current location of the user
     * @param selectedServer  OTP server being compared to the current location
     * @param acceptableError the amount of allowed error, in meters
     * @return true if the location of the user is within the bounding box of the selectedServer,
     * false if it is not
     */
    public static boolean checkPointInBoundingBox(LatLng location, Server selectedServer,
            int acceptableError) {
        float[] resultLeft = new float[3];
        float[] resultRight = new float[3];
        float[] resultUp = new float[3];
        float[] resultDown = new float[3];
        float[] resultHorizontal = new float[3];
        float[] resultVertical = new float[3];

        double locationLat = location.latitude;
        double locationLon = location.longitude;

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

        Location.distanceBetween(locationLat, leftLon, locationLat, rightLon, resultHorizontal);
        Location.distanceBetween(upLat, locationLon, downLat, locationLon, resultVertical);

        if (resultLeft[0] + resultRight[0] - resultHorizontal[0] > acceptableError) {
            return false;
        } else if (resultUp[0] + resultDown[0] - resultVertical[0] > acceptableError) {
            return false;
        }

        return true;
    }
}
