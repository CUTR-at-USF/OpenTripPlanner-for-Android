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

package edu.usf.cutr.opentripplanner.android.pois;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;

import edu.usf.cutr.opentripplanner.android.OTPApp;

/**
 * A list of places obtained from Google's Places API
 * https://developers.google.com/places/documentation/
 *
 * @author Khoa Tran
 */

public class GooglePlaces implements Places {

    private String request = "https://maps.googleapis.com/maps/api/place/textsearch/json?";

    private String apiKey;

    public static final String PARAM_LOCATION = "location";

    public static final String PARAM_RADIUS = "radius";

    public static final String PARAM_NAME = "query";

    // JSON Node names
    private static final String TAG_RESULTS = "results";

    private static final String TAG_GEOMETRY = "geometry";

    private static final String TAG_LOCATION = "location";

    private static final String TAG_LATITUDE = "lat";

    private static final String TAG_LONGITUDE = "lng";

    private static final String TAG_NAME = "name";

    private static final String TAG_FORMATTED_ADDRESS = "formatted_address";

    JSONArray results = null;

    public GooglePlaces(String apiKey) {
        this.setApiKey(apiKey);
    }

    public JSONObject requestPlaces(String paramLocation, String paramRadius, String paramName) {
        StringBuilder builder = new StringBuilder();

        String encodedParamLocation = "";
        String encodedParamRadius = "";
        String encodedParamName;
        try {
            if ((paramLocation != null) && (paramRadius != null)) {
                encodedParamLocation = URLEncoder.encode(paramLocation, OTPApp.URL_ENCODING);
                encodedParamRadius = URLEncoder.encode(paramRadius, OTPApp.URL_ENCODING);
            }
            encodedParamName = URLEncoder.encode(paramName, OTPApp.URL_ENCODING);
        } catch (UnsupportedEncodingException e1) {
            Log.e(OTPApp.TAG, "Error encoding Google Places request");
            e1.printStackTrace();
            return null;
        }

        if ((paramLocation != null) && (paramRadius != null)) {
            request += "location=" + encodedParamLocation;
            request += "&radius=" + encodedParamRadius;
            request += "&query=" + encodedParamName;
        } else {
            request += "query=" + encodedParamName;
        }
        request += "&sensor=false";
        request += "&key=" + getApiKey();

        Log.d(OTPApp.TAG, request);

        HttpURLConnection urlConnection = null;

        try {
            URL url = new URL(request);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setConnectTimeout(OTPApp.HTTP_CONNECTION_TIMEOUT);
            urlConnection.setReadTimeout(OTPApp.HTTP_SOCKET_TIMEOUT);
            urlConnection.connect();
            int status = urlConnection.getResponseCode();

            if (status == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(urlConnection.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
            } else {
                Log.e(OTPApp.TAG,
                        "Error obtaining Google Places response, status code: \" + status");
            }
        } catch (IOException e) {
            Log.e(OTPApp.TAG, "Error obtaining Google Places response" + e.toString());
            e.printStackTrace();
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        Log.d(OTPApp.TAG, builder.toString());

        JSONObject json = null;
        try {
            json = new JSONObject(builder.toString());
        } catch (JSONException e) {
            Log.e(OTPApp.TAG, "Error parsing Google Places data " + e.toString());
        }

        return json;
    }

    public ArrayList<POI> getPlaces(HashMap<String, String> params) {
        ArrayList<POI> pois = new ArrayList<POI>();

        String paramLocation = params.get(PARAM_LOCATION);
        String paramRadius = params.get(PARAM_RADIUS);
        String paramName = params.get(PARAM_NAME);

        // Get JSON
        JSONObject json = this.requestPlaces(paramLocation, paramRadius, paramName);

        if (json != null) {
            // Decrypt JSON
            try {
                results = json.getJSONArray(TAG_RESULTS);

                for (int i = 0; i < results.length(); i++) {
                    JSONObject r = results.getJSONObject(i);

                    String name = r.getString(TAG_NAME);
                    String address = r.getString(TAG_FORMATTED_ADDRESS);

                    JSONObject geometry = r.getJSONObject(TAG_GEOMETRY);
                    JSONObject location = geometry.getJSONObject(TAG_LOCATION);
                    double lat = location.getDouble(TAG_LATITUDE);
                    double lon = location.getDouble(TAG_LONGITUDE);

                    POI point = new POI(name, address, lat, lon);
                    pois.add(point);
                }
            } catch (JSONException e) {
                Log.e(OTPApp.TAG, "Error parsing Google Places data " + e.toString());
                e.printStackTrace();
            }
        }

        return pois;
    }

    /**
     * @return the apiKey
     */
    public String getApiKey() {
        return apiKey;
    }

    /**
     * @param apiKey the apiKey to set
     */
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
}
