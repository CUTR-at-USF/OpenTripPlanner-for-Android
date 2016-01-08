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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

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
 * A list of places obtained from MapQuest's Nominatim API
 * http://developer.mapquest.com/web/products/open/nominatim
 *
 * @author Khoa Tran
 */

public class Nominatim implements Places {

    private String request = "http://open.mapquestapi.com/nominatim/v1/search?format=json";

    public static final String PARAM_NAME = "q";

    public static final String PARAM_LEFT = "left";

    public static final String PARAM_TOP = "top";

    public static final String PARAM_RIGHT = "right";

    public static final String PARAM_BOTTOM = "bottom";


    // JSON Node names
    private static final String TAG_LATITUDE = "lat";

    private static final String TAG_LONGITUDE = "lon";

    private static final String TAG_NAME = "display_name";

    private String mApiKey;

    public Nominatim(String apiKey) {
        mApiKey = apiKey;
    }

    //	http://open.mapquestapi.com/nominatim/v1/search?format=json&q=Walmart&viewbox=-82.8511308,27.6236434,-82.0559399,28.3251809&bounded=1

    public JSONArray requestPlaces(String paramName, String left, String top, String right,
                                   String bottom) {
        StringBuilder builder = new StringBuilder();

        String encodedParamName;
        String encodedParamLeft = "";
        String encodedParamTop = "";
        String encodedParamRight = "";
        String encodedParamBottom = "";
        try {
            encodedParamName = URLEncoder.encode(paramName, OTPApp.URL_ENCODING);
            if ((left != null) && (top != null) && (right != null) && (bottom != null)) {
                encodedParamLeft = URLEncoder.encode(left, OTPApp.URL_ENCODING);
                encodedParamTop = URLEncoder.encode(top, OTPApp.URL_ENCODING);
                encodedParamRight = URLEncoder.encode(right, OTPApp.URL_ENCODING);
                encodedParamBottom = URLEncoder.encode(bottom, OTPApp.URL_ENCODING);
            }
        } catch (UnsupportedEncodingException e1) {
            Log.e(OTPApp.TAG, "Error encoding Nominatim request");
            e1.printStackTrace();
            return null;
        }

        request += "&q=" + encodedParamName;
        if ((left != null) && (top != null) && (right != null) && (bottom != null)) {
            request += "&viewbox=" + encodedParamLeft
                    + "," + encodedParamTop
                    + "," + encodedParamRight
                    + "," + encodedParamBottom;
            request += "&bounded=1";
        }

        request += "&key=" + mApiKey;

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
                Log.e(OTPApp.TAG, "Error obtaining Nominatim response, status code: " + status);
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(OTPApp.TAG, "Error obtaining Nominatim response" + e.toString());
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        Log.d(OTPApp.TAG, builder.toString());

        JSONArray json = null;
        try {
            json = new JSONArray(builder.toString());
        } catch (JSONException e) {
            Log.e(OTPApp.TAG, "Error parsing Nominatim data " + e.toString());
        }

        return json;
    }

    public ArrayList<POI> getPlaces(HashMap<String, String> params) {
        ArrayList<POI> pois = new ArrayList<POI>();

        String paramName = params.get(PARAM_NAME);
        String paramLeft = params.get(PARAM_LEFT);
        String paramTop = params.get(PARAM_TOP);
        String paramRight = params.get(PARAM_RIGHT);
        String paramBottom = params.get(PARAM_BOTTOM);

        // Get JSON
        JSONArray json = this
                .requestPlaces(paramName, paramLeft, paramTop, paramRight, paramBottom);

        if (json != null) {
            // Decrypt JSON
            try {
                for (int i = 0; i < json.length(); i++) {
                    JSONObject r = json.getJSONObject(i);

                    String name = r.getString(TAG_NAME);
                    double lat = Double.parseDouble(r.getString(TAG_LATITUDE));
                    double lon = Double.parseDouble(r.getString(TAG_LONGITUDE));

                    POI point = new POI(name, lat, lon);
                    pois.add(point);
                }
            } catch (JSONException e) {
                Log.e(OTPApp.TAG, "Error parsing Google Places data " + e.toString());
                e.printStackTrace();
            }
        }

        return pois;
    }
}
