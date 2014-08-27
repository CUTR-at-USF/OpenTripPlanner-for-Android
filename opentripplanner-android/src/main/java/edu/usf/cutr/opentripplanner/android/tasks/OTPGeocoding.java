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
import android.os.AsyncTask;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import edu.usf.cutr.opentripplanner.android.OTPApp;
import edu.usf.cutr.opentripplanner.android.R;
import edu.usf.cutr.opentripplanner.android.listeners.OTPGeocodingListener;
import edu.usf.cutr.opentripplanner.android.model.Server;
import edu.usf.cutr.opentripplanner.android.util.LocationUtil;
import edu.usf.cutr.opentripplanner.android.util.CustomAddress;

/**
 * @author Khoa Tran
 */

public class OTPGeocoding extends AsyncTask<String, Integer, Long> {

    private WeakReference<Activity> activity;

    private Context context;

    private boolean isStartTextbox;

    private OTPGeocodingListener callback;

    private boolean geocodingForMarker;

    private ArrayList<CustomAddress> addressesReturn = new ArrayList<CustomAddress>();

    private Server selectedServer;

    public OTPGeocoding(WeakReference<Activity> activity, Context context, boolean isStartTextbox,
                        boolean geocodingForMarker, Server selectedServer,
                        OTPGeocodingListener callback) {
        this.context = context;
        this.activity = activity;
        this.isStartTextbox = isStartTextbox;
        this.callback = callback;
        this.selectedServer = selectedServer;
        this.geocodingForMarker = geocodingForMarker;
    }

    protected void onPreExecute() {
        // Do nothing
    }

    protected Long doInBackground(String... reqs) {
        long count = reqs.length;
        addressesReturn = LocationUtil.processGeocoding(context, selectedServer, geocodingForMarker, reqs);
        return count;
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