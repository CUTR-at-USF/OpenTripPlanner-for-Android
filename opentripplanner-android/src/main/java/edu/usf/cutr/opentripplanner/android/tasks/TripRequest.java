/*
 * Copyright 2011 Marcy Gordon
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package edu.usf.cutr.opentripplanner.android.tasks;

import org.opentripplanner.api.model.error.PlannerError;
import org.opentripplanner.api.ws.Message;
import org.opentripplanner.api.ws.Request;
import org.opentripplanner.api.model.Itinerary;
import org.opentripplanner.api.ws.Response;
import org.opentripplanner.routing.core.TraverseMode;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import edu.usf.cutr.opentripplanner.android.OTPApp;
import edu.usf.cutr.opentripplanner.android.R;
import edu.usf.cutr.opentripplanner.android.listeners.TripRequestCompleteListener;
import edu.usf.cutr.opentripplanner.android.model.Server;
import edu.usf.cutr.opentripplanner.android.util.JacksonConfig;

/**
 * AsyncTask that invokes a trip planning request to the OTP Server
 *
 * @author Khoa Tran
 * @author Sean Barbeau (conversion to Jackson)
 */

public class TripRequest extends AsyncTask<Request, Integer, Long> {

    private Response response;

    private ProgressDialog progressDialog;

    private WeakReference<Activity> activity;

    private Context context;

    private Resources resources;

    private String currentRequestString = "";

    private Server selectedServer;

    private TripRequestCompleteListener callback;

    public TripRequest(WeakReference<Activity> activity, Context context, Resources resources,
                       Server selectedServer, TripRequestCompleteListener callback) {
        this.activity = activity;
        this.context = context;
        this.selectedServer = selectedServer;
        this.callback = callback;
        this.resources = resources;
        if (activity != null) {
            Activity activityRetrieved = activity.get();
            progressDialog = new ProgressDialog(activityRetrieved);
        }
    }

    protected void onPreExecute() {
        if (activity.get() != null) {
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(true);
            Activity activityRetrieved = activity.get();
            if (activityRetrieved != null) {
                progressDialog = ProgressDialog.show(activityRetrieved, "",
                        resources.getText(R.string.task_progress_tripplanner_progress), true);
            }
        }
    }

    protected Long doInBackground(Request... reqs) {
        long totalSize = 0;
        if (selectedServer == null) {
            Toast.makeText(context,
                    resources.getString(R.string.toast_no_server_selected_error),
                    Toast.LENGTH_SHORT).show();
            return null;
        }
        else{
            String prefix = PreferenceManager.getDefaultSharedPreferences(context)
                    .getString(OTPApp.PREFERENCE_KEY_FOLDER_STRUCTURE_PREFIX
                            , OTPApp.FOLDER_STRUCTURE_PREFIX_NEW);
            String baseURL = selectedServer.getBaseURL();
            for (Request req : reqs) {
                response = requestPlan(req, prefix, baseURL);
            }
        }
        return totalSize;
    }

    protected void onCancelled(Long result) {

        try {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        } catch (Exception e) {
            Log.e(OTPApp.TAG, "Error in TripRequest Cancelled dismissing dialog: " + e);
        }

        Activity activityRetrieved = activity.get();
        if (activityRetrieved != null) {
            AlertDialog.Builder geocoderAlert = new AlertDialog.Builder(activityRetrieved);
            geocoderAlert.setTitle(R.string.tripplanner_results_title)
                    .setMessage(R.string.tripplanner_error_request_timeout)
                    .setCancelable(false)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    });

            AlertDialog alert = geocoderAlert.create();
            alert.show();
        }

        Log.e(OTPApp.TAG, "No route to display!");
    }

    protected void onPostExecute(Long result) {
        if (activity.get() != null) {
            try {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            } catch (Exception e) {
                Log.e(OTPApp.TAG, "Error in TripRequest PostExecute dismissing dialog: " + e);
            }
        }

        if (response != null && response.getPlan() != null
                && response.getPlan().getItinerary().get(0) != null) {

            List<Itinerary> itineraries = response.getPlan().getItinerary();

            callback.onTripRequestComplete(itineraries, currentRequestString);
        } else {
            Activity activityRetrieved = activity.get();
            if (activityRetrieved != null) {
                AlertDialog.Builder feedback = new AlertDialog.Builder(activityRetrieved);
                feedback.setTitle(resources
                        .getString(R.string.tripplanner_error_dialog_title));
                feedback.setNeutralButton(resources.getString(android.R.string.ok),
                        null);
                String msg = resources
                        .getString(R.string.tripplanner_error_not_defined);

                PlannerError error = response.getError();
                if (error != null) {
                    int errorCode = error.getId();

                    if (response != null && response.getError() != null
                            && errorCode != Message.PLAN_OK
                            .getId()) {

                        msg = getErrorMessage(response.getError().getId());
                        if (msg == null) {
                            msg = response.getError().getMsg();
                        }
                    }
                }
                feedback.setMessage(msg);
                feedback.create().show();
            }

            Log.e(OTPApp.TAG, "No route to display!");
        }
    }

    protected String getErrorMessage(int errorCode) {
        if (errorCode == Message.SYSTEM_ERROR.getId()) {
            return (resources.getString(R.string.tripplanner_error_system));
        } else if (errorCode == Message.OUTSIDE_BOUNDS.getId()) {
            return (resources.getString(R.string.tripplanner_error_outside_bounds));
        } else if (errorCode == Message.PATH_NOT_FOUND.getId()) {
            return (resources.getString(R.string.tripplanner_error_path_not_found));
        } else if (errorCode == Message.NO_TRANSIT_TIMES.getId()) {
            return (resources.getString(R.string.tripplanner_error_no_transit_times));
        } else if (errorCode == Message.REQUEST_TIMEOUT.getId()) {
            return (resources.getString(R.string.tripplanner_error_request_timeout));
        } else if (errorCode == Message.BOGUS_PARAMETER.getId()) {
            return (resources.getString(R.string.tripplanner_error_bogus_parameter));
        } else if (errorCode == Message.GEOCODE_FROM_NOT_FOUND.getId()) {
            return (resources
                    .getString(R.string.tripplanner_error_geocode_from_not_found));
        } else if (errorCode == Message.GEOCODE_TO_NOT_FOUND.getId()) {
            return (resources
                    .getString(R.string.tripplanner_error_geocode_to_not_found));
        } else if (errorCode == Message.GEOCODE_FROM_TO_NOT_FOUND.getId()) {
            return (resources
                    .getString(R.string.tripplanner_error_geocode_from_to_not_found));
        } else if (errorCode == Message.TOO_CLOSE.getId()) {
            return (resources.getString(R.string.tripplanner_error_too_close));
        } else if (errorCode == Message.LOCATION_NOT_ACCESSIBLE.getId()) {
            return (resources
                    .getString(R.string.tripplanner_error_location_not_accessible));
        } else if (errorCode == Message.GEOCODE_FROM_AMBIGUOUS.getId()) {
            return (resources
                    .getString(R.string.tripplanner_error_geocode_from_ambiguous));
        } else if (errorCode == Message.GEOCODE_TO_AMBIGUOUS.getId()) {
            return (resources
                    .getString(R.string.tripplanner_error_geocode_to_ambiguous));
        } else if (errorCode == Message.GEOCODE_FROM_TO_AMBIGUOUS.getId()) {
            return (resources
                    .getString(R.string.tripplanner_error_geocode_from_to_ambiguous));
        } else if (errorCode == Message.UNDERSPECIFIED_TRIANGLE.getId()
                || errorCode == Message.TRIANGLE_NOT_AFFINE.getId()
                || errorCode == Message.TRIANGLE_OPTIMIZE_TYPE_NOT_SET.getId()
                || errorCode == Message.TRIANGLE_VALUES_NOT_SET.getId()) {
            return (resources.getString(R.string.tripplanner_error_triangle));
        } else {
            return null;
        }
    }

    protected Response requestPlan(Request requestParams, String prefix, String baseURL) {
        HashMap<String, String> tmp = requestParams.getParameters();

        Collection c = tmp.entrySet();
        Iterator itr = c.iterator();

        String params = "";
        boolean first = true;
        while (itr.hasNext()) {
            if (first) {
                params += "?" + itr.next();
                first = false;
            } else {
                params += "&" + itr.next();
            }
        }

        if (requestParams.getBikeRental()) {
            String updatedString;
            if (prefix.equals(OTPApp.FOLDER_STRUCTURE_PREFIX_NEW)){
                updatedString = params.replace(TraverseMode.BICYCLE.toString(),
                        TraverseMode.BICYCLE.toString() + OTPApp.OTP_RENTAL_QUALIFIER);
            }
            else{
                updatedString = params.replace(TraverseMode.BICYCLE.toString(),
                        TraverseMode.BICYCLE.toString() + ", " + TraverseMode.WALK.toString());
            }

            params = updatedString;
        }


        if (requestParams.getModes().getTrainish()) {
            // TraverseModeSet.toString() enumerates activated modes, which might not be supported by server
            // so we filter them out. Should be solved differently, e.g. by  decoupling and introducing
            // an interface with version dependent implementations
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

            if (prefs.getInt(OTPApp.PREFERENCE_KEY_API_VERSION, OTPApp.API_VERSION_V3)
                    >= OTPApp.API_VERSION_V3) {
                String updatedString;
                updatedString = params.replace(TraverseMode.TRAINISH.toString(), "");
                updatedString = updatedString.replace(TraverseMode.BUSISH.toString(), "");

                params = updatedString;
            }
        }

        String u = baseURL + prefix + OTPApp.PLAN_LOCATION + params;

        Log.d(OTPApp.TAG, "URL: " + u);

        currentRequestString = u;

        HttpURLConnection urlConnection = null;
        URL url;
        Response plan = null;

        try {
            url = new URL(u);

            disableConnectionReuseIfNecessary(); // For bugs in HttpURLConnection pre-Froyo

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestProperty("Accept", "application/json");
            urlConnection.setConnectTimeout(OTPApp.HTTP_CONNECTION_TIMEOUT);
            urlConnection.setReadTimeout(OTPApp.HTTP_SOCKET_TIMEOUT);
            plan = JacksonConfig.getObjectReaderInstance()
                    .readValue(urlConnection.getInputStream());
        } catch (java.net.SocketTimeoutException e) {
            Log.e(OTPApp.TAG, "Timeout fetching JSON or XML: " + e);
            e.printStackTrace();
            cancel(true);
        } catch (IOException e) {
            Log.e(OTPApp.TAG, "Error fetching JSON or XML: " + e);
            e.printStackTrace();
            cancel(true);
            // Reset timestamps to show there was an error
            // requestStartTime = 0;
            // requestEndTime = 0;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return plan;
    }

    /**
     * Disable HTTP connection reuse which was buggy pre-froyo
     */
    private void disableConnectionReuseIfNecessary() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
            System.setProperty("http.keepAlive", "false");
        }
    }
}
