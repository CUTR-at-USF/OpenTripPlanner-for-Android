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

package edu.usf.cutr.opentripplanner.android.tasks;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources.NotFoundException;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import edu.usf.cutr.opentripplanner.android.OTPApp;
import edu.usf.cutr.opentripplanner.android.R;
import edu.usf.cutr.opentripplanner.android.listeners.OTPGeocodingListener;
import edu.usf.cutr.opentripplanner.android.model.Server;
import edu.usf.cutr.opentripplanner.android.pois.GooglePlaces;
import edu.usf.cutr.opentripplanner.android.pois.Nominatim;
import edu.usf.cutr.opentripplanner.android.pois.POI;
import edu.usf.cutr.opentripplanner.android.pois.Places;
import edu.usf.cutr.opentripplanner.android.util.LocationUtil;

/**
 * @author Khoa Tran
 */

public class OTPGeocoding extends AsyncTask<String, Integer, Long> {

    private WeakReference<Activity> activity;

    private Context context;

    private boolean isStartTextbox;

    private OTPGeocodingListener callback;

    private String placesService;

    private boolean geocodingForMarker;

    private ArrayList<Address> addressesReturn = new ArrayList<Address>();

    private Server selectedServer;

    public OTPGeocoding(WeakReference<Activity> activity, Context context, boolean isStartTextbox,
                        boolean geocodingForMarker, Server selectedServer, String placesService,
                        OTPGeocodingListener callback) {
        this.context = context;
        this.activity = activity;
        this.isStartTextbox = isStartTextbox;
        this.callback = callback;
        this.selectedServer = selectedServer;
        this.placesService = placesService;
        this.geocodingForMarker = geocodingForMarker;
    }

    protected void onPreExecute() {
        // Do nothing
    }

    protected Long doInBackground(String... reqs) {
        long count = reqs.length;

        String address = reqs[0];
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        if (address == null || address.equalsIgnoreCase("")) {
            return count;
        }

        if (address.equalsIgnoreCase(context.getString(R.string.text_box_my_location))) {
            String currentLat = reqs[1];
            String currentLng = reqs[2];
            LatLng latLng = new LatLng(Double.parseDouble(currentLat),
                    Double.parseDouble(currentLng));

            Address addressReturn = new Address(context.getResources().getConfiguration().locale);
            addressReturn.setLatitude(latLng.latitude);
            addressReturn.setLongitude(latLng.longitude);
            addressReturn.setAddressLine(addressReturn.getMaxAddressLineIndex() + 1,
                    context.getString(R.string.text_box_my_location));

            addressesReturn.add(addressReturn);

            return count;
        }

        List<Address> addresses = null;

        if (prefs.getBoolean(OTPApp.PREFERENCE_KEY_USE_ANDROID_GEOCODER, true)) {
            Geocoder gc = new Geocoder(context);
            try {
                if (selectedServer != null) {
                    addresses = (ArrayList<Address>) gc.getFromLocationName(address,
                            context.getResources().getInteger(R.integer.geocoder_max_results),
                            selectedServer.getLowerLeftLatitude(),
                            selectedServer.getLowerLeftLongitude(),
                            selectedServer.getUpperRightLatitude(),
                            selectedServer.getUpperRightLongitude());
                } else {
                    addresses = (ArrayList<Address>) gc.getFromLocationName(address,
                            context.getResources().getInteger(R.integer.geocoder_max_results));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        addresses = filterAddressesBBox(addresses);

        if ((addresses == null) || addresses.isEmpty()) {
            addresses = searchPlaces(address);

            for (Address addressRetrieved : addresses) {
                String str = addressRetrieved.getAddressLine(0);
                List<String> addressLines = Arrays.asList(str.split(", "));
                for (int j = 0; j < addressLines.size(); j++) {
                    addressRetrieved.setAddressLine(j, addressLines.get(j));
                }
            }
        }

        addresses = filterAddressesBBox(addresses);

        addressesReturn.addAll(addresses);

        return count;
    }

    /**
     * Filters the addresses obtained in geocoding process, removing the
     * results outside server limits.
     *
     * @param addresses list of addresses to filter
     * @return a new list filtered
     */
    private List<Address> filterAddressesBBox(List<Address> addresses) {
        if (!(addresses == null || addresses.isEmpty())) {
            CopyOnWriteArrayList<Address> addressesFiltered = new CopyOnWriteArrayList<Address>(addresses);

            for (Address address : addressesFiltered) {
                if (!LocationUtil.checkPointInBoundingBox(
                        new LatLng(address.getLatitude(), address.getLongitude()), selectedServer,
                        OTPApp.CHECK_BOUNDS_ACCEPTABLE_ERROR)) {
                    addressesFiltered.remove(address);
                }
            }

            return addressesFiltered;
        }
        return addresses;
    }

    /**
     * Try to grab the developer key from an unversioned resource file, if it exists
     *
     * @return the developer key from an unversioned resource file, or empty string if it doesn't
     * exist
     */
    private String getKeyFromResource() {
        String strKey = "";

        try {
            InputStream in = context.getResources().openRawResource(R.raw.googleplaceskey);
            BufferedReader r = new BufferedReader(new InputStreamReader(in));
            StringBuilder total = new StringBuilder();

            while ((strKey = r.readLine()) != null) {
                total.append(strKey);
            }

            strKey = total.toString();
            strKey = strKey.trim(); //Remove any whitespace
        } catch (NotFoundException e) {
            Log.w(OTPApp.TAG, "Warning - didn't find the google places key file:" + e);
        } catch (IOException e) {
            Log.w(OTPApp.TAG, "Error reading the developer key file:" + e);
        }

        return strKey;
    }

    private List<Address> searchPlaces(String name) {
        HashMap<String, String> params = new HashMap<String, String>();
        Places p;

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
            p = new GooglePlaces(getKeyFromResource());

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

            p = new Nominatim();

            Log.d(OTPApp.TAG, "Using Nominatim!");
        }

        ArrayList<POI> pois = new ArrayList<POI>();
        pois.addAll(p.getPlaces(params));

        List<Address> addresses = new ArrayList<Address>();

        for (POI poi : pois) {
            Log.d(OTPApp.TAG, poi.getName() + " " + poi.getLatitude() + "," + poi.getLongitude());
            Address address = new Address(context.getResources().getConfiguration().locale);
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

    protected void onCancelled(Long result) {
        Activity activityRetrieved = activity.get();
        if (activityRetrieved != null) {
            AlertDialog.Builder geocoderAlert = new AlertDialog.Builder(activityRetrieved);
            geocoderAlert.setTitle(R.string.geocoder_results_title)
                    .setMessage(R.string.geocoder_results_no_results_message)
                    .setCancelable(false)
                    .setPositiveButton(context.getResources().getString(android.R.string.ok),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                }
                            }
                    );

            AlertDialog alert = geocoderAlert.create();
            alert.show();
        }
        Log.e(OTPApp.TAG, "No geocoding processed!");
    }

    protected void onPostExecute(Long result) {
        callback.onOTPGeocodingComplete(isStartTextbox, addressesReturn, geocodingForMarker);
    }
}