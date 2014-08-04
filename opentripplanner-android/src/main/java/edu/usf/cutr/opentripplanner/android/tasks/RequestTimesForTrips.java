/*
 * Copyright 2014 Vreixo Gonzalez on 2014
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
import android.content.Context;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.opentripplanner.index.model.TripTimeShort;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;

import edu.usf.cutr.opentripplanner.android.OTPApp;
import edu.usf.cutr.opentripplanner.android.R;
import edu.usf.cutr.opentripplanner.android.listeners.RequestTimesForTripsCompleteListener;

/**
 * @author Vreixo González
 */

public class RequestTimesForTrips extends AsyncTask<String, Integer, HashMap<String, List<TripTimeShort>>> {

    private WeakReference<Activity> activity;

    private Context context;

    private RequestTimesForTripsCompleteListener callback;

    private static ObjectMapper mapper = null;

    public RequestTimesForTrips(Context context, RequestTimesForTripsCompleteListener callback) {
        this.context = context;
        this.callback = callback;
    }

    protected void onPreExecute() {
        // Do nothing
    }

    protected HashMap<String, List<TripTimeShort>> doInBackground(String... reqs) {
        String prefix = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(OTPApp.PREFERENCE_KEY_FOLDER_STRUCTURE_PREFIX
                        , OTPApp.FOLDER_STRUCTURE_PREFIX_NEW);
        if (reqs.length <= 1){
            return null;
        }
        HashMap<String, List<TripTimeShort>> timesUpdatesForTrips = new HashMap<String,
                List<TripTimeShort>>(reqs.length - 1);

        HttpURLConnection urlConnection = null;
        List<TripTimeShort> updatedTripTimesList = null;

        try {
            if (mapper == null) {
                mapper = new ObjectMapper();
            }
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            JavaType bikeRentalStationListType = mapper.getTypeFactory().constructCollectionType(List.class, TripTimeShort.class);
            for (String tripId : reqs) {
                if (tripId.equals(reqs[0])) {
                    continue;
                }
                String encodedTripId = URLEncoder.encode(tripId, Charset.defaultCharset().name());
                String u = reqs[0] + prefix + OTPApp.TRIP_TIMES_UPDATES_LOCATION_BEFORE_ID
                        + encodedTripId + OTPApp.TRIP_TIMES_UPDATES_LOCATION_AFTER_ID;
                URL url = new URL(u);
                Log.d(OTPApp.TAG, "URL: " + u);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestProperty("Accept", "application/json");
                urlConnection.setConnectTimeout(OTPApp.HTTP_CONNECTION_TIMEOUT);
                urlConnection.setReadTimeout(OTPApp.HTTP_SOCKET_TIMEOUT);
                updatedTripTimesList = mapper.readValue(urlConnection.getInputStream(), bikeRentalStationListType);
                timesUpdatesForTrips.put(tripId, updatedTripTimesList);
            }
        } catch (IOException e) {
            Log.e(OTPApp.TAG, "Error fetching JSON or XML: " + e);
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }

        return timesUpdatesForTrips;
    }

    protected void onPostExecute(HashMap<String, List<TripTimeShort>> timesUpdatesForTrips) {
        if (timesUpdatesForTrips != null) {
            callback.onUpdateTripTimesComplete(timesUpdatesForTrips);
        } else {
            Toast.makeText(context, context.getResources().getString(R.string.toast_realtime_updates_fail),
                    Toast.LENGTH_SHORT).show();
            Log.e(OTPApp.TAG, "No bike rental stations!");
            callback.onUpdateTripTimesFail();
        }
    }
}
