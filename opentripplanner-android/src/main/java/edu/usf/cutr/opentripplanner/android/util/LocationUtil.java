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
import com.google.maps.android.PolyUtil;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import edu.usf.cutr.opentripplanner.android.OTPApp;
import edu.usf.cutr.opentripplanner.android.R;
import edu.usf.cutr.opentripplanner.android.model.Server;
import edu.usf.cutr.opentripplanner.android.pois.GooglePlaces;
import edu.usf.cutr.opentripplanner.android.pois.Nominatim;
import edu.usf.cutr.opentripplanner.android.pois.POI;
import edu.usf.cutr.opentripplanner.android.pois.Places;

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
     * @return true if the location of the user is within the bounding box of the selectedServer,
     * false if it is not
     */
    public static boolean checkPointInBoundingBox(LatLng location, Server selectedServer) {
        LatLng lowerLeft = new LatLng(selectedServer.getLowerLeftLatitude(),
                selectedServer.getLowerLeftLongitude());
        LatLng upperLeft = new LatLng(selectedServer.getUpperRightLatitude(),
                selectedServer.getLowerLeftLongitude());
        LatLng upperRight = new LatLng(selectedServer.getUpperRightLatitude(),
                selectedServer.getUpperRightLongitude());
        LatLng lowerRight = new LatLng(selectedServer.getLowerLeftLatitude(),
                selectedServer.getUpperRightLongitude());

        List<LatLng> rectangle = new ArrayList<LatLng>(2);
        rectangle.add(lowerLeft);
        rectangle.add(upperLeft);
        rectangle.add(upperRight);
        rectangle.add(lowerRight);

        return PolyUtil.containsLocation(location, rectangle, true);
    }

    public static ArrayList<CustomAddress> processGeocoding(Context context, Server selectedServer,
                                                            String... reqs) {
        return processGeocoding(context, selectedServer, false, reqs);
    }

    public static ArrayList<CustomAddress> processGeocoding(Context context, Server selectedServer, boolean geocodingForMarker, String... reqs) {
        ArrayList<CustomAddress> addressesReturn = new ArrayList<CustomAddress>();

        String address = reqs[0];
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        if (address == null || address.equalsIgnoreCase("")) {
            return null;
        }

        double latitude = 0, longitude = 0;
        boolean latLngSet = false;

        try{
            if (reqs.length >= 3) {
                latitude = Double.parseDouble(reqs[1]);
                longitude = Double.parseDouble(reqs[2]);
                latLngSet = true;
            }
        }
        catch(Exception e){
            Log.d(OTPApp.TAG, "Geocoding without reference latitude/longitude");
        }

        if (address.equalsIgnoreCase(context.getString(R.string.text_box_my_location))) {
            if (latLngSet){
                CustomAddress addressReturn = new CustomAddress(context.getResources().getConfiguration().locale);
                addressReturn.setLatitude(latitude);
                addressReturn.setLongitude(longitude);
                addressReturn.setAddressLine(addressReturn.getMaxAddressLineIndex() + 1,
                        context.getString(R.string.text_box_my_location));

                addressesReturn.add(addressReturn);

                return addressesReturn;
            }
            return null;
        }

        List<CustomAddress> addresses = new ArrayList<CustomAddress>();

        if (prefs.getBoolean(OTPApp.PREFERENCE_KEY_USE_ANDROID_GEOCODER, true)) {
            Geocoder gc = new Geocoder(context);
            try {
                List<Address> androidTypeAddresses;
                if (selectedServer != null) {
                    androidTypeAddresses = gc.getFromLocationName(address,
                            context.getResources().getInteger(R.integer.geocoder_max_results),
                            selectedServer.getLowerLeftLatitude(),
                            selectedServer.getLowerLeftLongitude(),
                            selectedServer.getUpperRightLatitude(),
                            selectedServer.getUpperRightLongitude());
                } else {
                    androidTypeAddresses = gc.getFromLocationName(address,
                            context.getResources().getInteger(R.integer.geocoder_max_results));
                }
                for (Address androidTypeAddress : androidTypeAddresses){
                    addresses.add(new CustomAddress(androidTypeAddress));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        addresses = filterAddressesBBox(selectedServer, addresses);

        boolean resultsCloseEnough = true;

        if (geocodingForMarker && latLngSet){
            float results[] = new float[1];
            resultsCloseEnough = false;

            for (CustomAddress addressToCheck : addresses){
                Location.distanceBetween(latitude, longitude,
                        addressToCheck.getLatitude(), addressToCheck.getLongitude(), results);
                if (results[0] < OTPApp.GEOCODING_MAX_ERROR) {
                    resultsCloseEnough = true;
                    break;
                }
            }
        }

        if ((addresses == null) || addresses.isEmpty() || !resultsCloseEnough) {
            if (addresses == null){
                addresses = new ArrayList<CustomAddress>();
            }
            addresses.addAll(searchPlaces(context, selectedServer, address));

            for (CustomAddress addressRetrieved : addresses) {
                String str = addressRetrieved.getAddressLine(0);
                List<String> addressLines = Arrays.asList(str.split(", "));
                for (int j = 0; j < addressLines.size(); j++) {
                    addressRetrieved.setAddressLine(j, addressLines.get(j));
                }
            }
        }

        addresses = filterAddressesBBox(selectedServer, addresses);

        if (geocodingForMarker && latLngSet && addresses != null && !addresses.isEmpty()){
            float results[] = new float[1];
            float minDistanceToOriginalLatLon = Float.MAX_VALUE;
            CustomAddress closestAddress = addresses.get(0);

            for (CustomAddress addressToCheck : addresses){
                Location.distanceBetween(latitude, longitude,
                        addressToCheck.getLatitude(), addressToCheck.getLongitude(), results);
                if (results[0] < minDistanceToOriginalLatLon){
                    closestAddress = addressToCheck;
                    minDistanceToOriginalLatLon = results[0];
                }
            }
            addressesReturn.add(closestAddress);
        }
        else{
            addressesReturn.addAll(addresses);
        }

        return addressesReturn;
    }

    /**
     * Filters the addresses obtained in geocoding process, removing the
     * results outside server limits.
     *
     * @param addresses list of addresses to filter
     * @return a new list filtered
     */
    private static List<CustomAddress> filterAddressesBBox(Server selectedServer, List<CustomAddress> addresses) {
        if ((!(addresses == null || addresses.isEmpty())) && selectedServer != null) {
            for (Iterator<CustomAddress> it=addresses.iterator(); it.hasNext();) {
                CustomAddress address = it.next();
                if (!LocationUtil.checkPointInBoundingBox(
                        new LatLng(address.getLatitude(), address.getLongitude()),
                        selectedServer)) {
                    it.remove();
                }
            }
        }
        return addresses;
    }

    /**
     * Try to grab the developer key from an unversioned resource file, if it exists
     *
     * @param context
     * @param apiKeyResourceId Resource ID of the raw file containing the API key
     * @return the developer key from a resource file, or empty string if it doesn't
     * exist
     */
    private static String getKeyFromResource(Context context, int apiKeyResourceId) {
        String strKey = "";

        try {
            InputStream in = context.getResources().openRawResource(apiKeyResourceId);
            BufferedReader r = new BufferedReader(new InputStreamReader(in));
            StringBuilder total = new StringBuilder();

            while ((strKey = r.readLine()) != null) {
                total.append(strKey);
            }

            strKey = total.toString();
            strKey = strKey.trim(); //Remove any whitespace
        } catch (Resources.NotFoundException e) {
            Log.w(OTPApp.TAG, "Warning - didn't find the google places key file:" + e);
        } catch (IOException e) {
            Log.w(OTPApp.TAG, "Error reading the developer key file:" + e);
        }

        return strKey;
    }

    private static List<CustomAddress> searchPlaces(Context context, Server selectedServer, String name) {
        HashMap<String, String> params = new HashMap<String, String>();
        Places p;
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(
                context);
        String placesService = mPrefs.getString(
                OTPApp.PREFERENCE_KEY_GEOCODER_PROVIDER,
                context.getResources().getString(R.string.geocoder_nominatim));

        if (placesService
                .equals(context.getResources().getString(R.string.geocoder_google_places))) {
            params.put(GooglePlaces.PARAM_NAME, name);
            if (selectedServer != null) {
                params.put(GooglePlaces.PARAM_LOCATION,
                        Double.toString(selectedServer.getGeometricalCenterLatitude()) + ","
                                + Double.toString(selectedServer.getGeometricalCenterLongitude())
                );
                params.put(GooglePlaces.PARAM_RADIUS, Double.toString(selectedServer.getRadius()));
            }
            p = new GooglePlaces(getKeyFromResource(context, R.raw.googleplaceskey));

            Log.d(OTPApp.TAG, "Using Google Places!");
        } else {
            params.put(Nominatim.PARAM_NAME, name);
            if (selectedServer != null) {
                params.put(Nominatim.PARAM_LEFT,
                        Double.toString(selectedServer.getLowerLeftLongitude()));
                params.put(Nominatim.PARAM_TOP,
                        Double.toString(selectedServer.getLowerLeftLatitude()));
                params.put(Nominatim.PARAM_RIGHT,
                        Double.toString(selectedServer.getUpperRightLongitude()));
                params.put(Nominatim.PARAM_BOTTOM,
                        Double.toString(selectedServer.getUpperRightLatitude()));
            }

            p = new Nominatim(getKeyFromResource(context, R.raw.mapquestgeocoderkey));

            Log.d(OTPApp.TAG, "Using Nominatim!");
        }

        ArrayList<POI> pois = new ArrayList<POI>();
        pois.addAll(p.getPlaces(params));

        List<CustomAddress> addresses = new ArrayList<CustomAddress>();

        for (POI poi : pois) {
            Log.d(OTPApp.TAG, poi.getName() + " " + poi.getLatitude() + "," + poi.getLongitude());
            CustomAddress address = new CustomAddress(context.getResources().getConfiguration().locale);
            address.setLatitude(poi.getLatitude());
            address.setLongitude(poi.getLongitude());
            String addressLine;

            if (poi.getAddress() != null) {
                if (!poi.getAddress().contains(poi.getName())) {
                    addressLine = (poi.getName() + ", " + poi.getAddress());
                } else {
                    addressLine = poi.getAddress();
                }
            } else {
                addressLine = poi.getName();
            }
            address.setAddressLine(address.getMaxAddressLineIndex() + 1, addressLine);
            addresses.add(address);
        }

        return addresses;
    }


}
