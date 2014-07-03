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
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.opentripplanner.routing.bike_rental.BikeRentalStation;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import edu.usf.cutr.opentripplanner.android.OTPApp;
import edu.usf.cutr.opentripplanner.android.R;
import edu.usf.cutr.opentripplanner.android.listeners.BikeRentalLoadCompleteListener;

/**
 * @author Vreixo González
 */

public class BikeRentalLoad extends AsyncTask<String, Integer, List<BikeRentalStation>> {

    private ProgressDialog progressDialog;

    private WeakReference<Activity> activity;

    private Context context;

    private BikeRentalLoadCompleteListener callback;

    private static ObjectMapper mapper = null;

    public BikeRentalLoad(WeakReference<Activity> activity, Context context,
                          BikeRentalLoadCompleteListener callback) {
        this.activity = activity;
        this.context = context;
        this.callback = callback;
        Activity activityRetrieved = activity.get();
        if (activityRetrieved != null) {
            progressDialog = new ProgressDialog(activityRetrieved);
        }
    }

    protected void onPreExecute() {
        // Do nothing
    }

    protected List<BikeRentalStation> doInBackground(String... reqs) {
        String u = reqs[0] + OTPApp.BIKE_RENTAL_LOCATION;
        Log.d(OTPApp.TAG, "URL: " + u);

        HttpURLConnection urlConnection = null;
        List<BikeRentalStation> bikeRentalStationList = null;

        try {
            URL url = new URL(u);
            if (mapper == null) {
                mapper = new ObjectMapper();
            }
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestProperty("Accept", "application/json");
            urlConnection.setConnectTimeout(OTPApp.HTTP_CONNECTION_TIMEOUT);
            urlConnection.setReadTimeout(OTPApp.HTTP_SOCKET_TIMEOUT);
            JavaType bikeRentalStationListType = mapper.getTypeFactory().constructCollectionType(List.class, BikeRentalStation.class);
            bikeRentalStationList = mapper.readValue(urlConnection.getInputStream(), bikeRentalStationListType);
        } catch (IOException e) {
            Log.e(OTPApp.TAG, "Error fetching JSON or XML: " + e);
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return bikeRentalStationList;
    }

    protected void onPostExecute(List<BikeRentalStation> bikeRentalStationList) {
        if (activity.get() != null) {
            try {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            } catch (Exception e) {
                Log.e(OTPApp.TAG, "Error in bike rental load stations Request PostExecute dismissing dialog: " + e);
            }
        }

        if (bikeRentalStationList != null) {
            Toast.makeText(context,
                    context.getResources().getString(R.string.toast_bike_rental_load_request_successful),
                    Toast.LENGTH_SHORT).show();
            callback.onBikeRentalStationListLoad(bikeRentalStationList);
        } else {
            Toast.makeText(context, context.getResources().getString(R.string.toast_bike_rental_load_request_error),
                    Toast.LENGTH_SHORT).show();

            Log.e(OTPApp.TAG, "No bike rental stations!");
        }
    }
}
