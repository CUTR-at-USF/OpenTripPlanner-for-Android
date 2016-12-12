/*
 * Copyright 2013 University of South Florida
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
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.opentripplanner.api.resource.ServerInfo;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

import edu.usf.cutr.opentripplanner.android.OTPApp;
import edu.usf.cutr.opentripplanner.android.R;
import edu.usf.cutr.opentripplanner.android.listeners.ServerCheckerCompleteListener;
import edu.usf.cutr.opentripplanner.android.model.Server;

public class ServerChecker extends AsyncTask<Server, Long, String> {

    private ProgressDialog progressDialog;

    private WeakReference<Activity> activity;

    private Context context;

    private ServerCheckerCompleteListener callback = null;

    private boolean showMessage = false;

    private boolean isWorking = false;

    private static ObjectMapper mapper = null;

    private boolean isCustomServer = false;

    private boolean isAutoDetected = false;

    private boolean showToast = false;

    /**
     * Constructs a new ServerChecker
     */
    public ServerChecker(WeakReference<Activity> activity, Context context, boolean showMessage) {
        this.activity = activity;
        this.context = context;
        this.showMessage = showMessage;
        Activity activityRetrieved = activity.get();
        if (activityRetrieved != null) {
            progressDialog = new ProgressDialog(activityRetrieved);
        }
    }

    /**
     * Constructs a new ServerChecker
     */
    public ServerChecker(WeakReference<Activity> activity, Context context,
            ServerCheckerCompleteListener callback, boolean isCustomServer, boolean showToast,
            boolean isAutoDetected) {
        this.activity = activity;
        this.context = context;
        this.callback = callback;
        this.isCustomServer = isCustomServer;
        this.isAutoDetected = isAutoDetected;
        this.showToast = showToast;
        Activity activityRetrieved = activity.get();
        if (activityRetrieved != null) {
            progressDialog = new ProgressDialog(activityRetrieved);
        }
    }

    @Override
    protected void onPreExecute() {
        if (activity.get() != null) {
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(true);
            Activity activityRetrieved = activity.get();
            if (activityRetrieved != null) {
                progressDialog = ProgressDialog
                        .show(activityRetrieved, "",
                                context.getString(R.string.task_progress_server_checker_progress),
                                true);
            }
        }
    }

    @Override
    protected String doInBackground(Server... params) {
        Server server = params[0];
        ServerInfo serverInfo;

        if (server == null) {
            Log.w(OTPApp.TAG,
                    "Tried to get server info when no server was selected");
            cancel(true);
            return null;
        } else {
            String message =
                    context.getResources().getString(R.string.server_checker_info_region)
                            + " " + server.getRegion() + "\n" + context.getResources()
                            .getString(R.string.server_checker_info_language) + " " + server
                            .getLanguage() + "\n" + context.getResources()
                            .getString(R.string.server_checker_info_contact) + " " + server
                            .getContactName() + " ("
                            + server.getContactEmail() + ")" + "\n" + context.getResources()
                            .getString(R.string.server_checker_info_url) + " " + server
                            .getBaseURL()
                            + "\n" + context.getResources()
                            .getString(R.string.server_checker_info_bounds) + " " + server
                            .getBounds() + "\n" + context.getResources()
                            .getString(R.string.server_checker_info_bike_rental) + " ";
            if (server.getOffersBikeRental()){
                message +=  context.getResources().getString(android.R.string.yes);
            }
            else{
                message += context.getResources().getString(android.R.string.no);
            }

            message += "\n" + context.getResources()
                            .getString(R.string.server_checker_info_reachable) + " ";

            if (mapper == null) {
                mapper = new ObjectMapper();
            }

            int status = 0;
            HttpURLConnection urlConnection = null;
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            SharedPreferences.Editor prefsEditor = PreferenceManager.getDefaultSharedPreferences(
                    context).edit();

            try {
                URL url = new URL(server.getBaseURL() + OTPApp.SERVER_INFO_LOCATION_NEW);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setConnectTimeout(OTPApp.HTTP_CONNECTION_TIMEOUT);
                urlConnection.setReadTimeout(OTPApp.HTTP_SOCKET_TIMEOUT);
                urlConnection.connect();
                status = urlConnection.getResponseCode();
                serverInfo = mapper.readValue(urlConnection.getInputStream(), ServerInfo.class);
                prefsEditor.putString(OTPApp.PREFERENCE_KEY_FOLDER_STRUCTURE_PREFIX, OTPApp.FOLDER_STRUCTURE_PREFIX_NEW);
            } catch (IOException e1) {
                Log.e(OTPApp.TAG, "Server not working with API V1, trying again this time with" +
                        " old version: "
                        + e1.getMessage());
                try {
                    URL url = new URL(server.getBaseURL() + OTPApp.SERVER_INFO_LOCATION_OLD);
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setConnectTimeout(OTPApp.HTTP_CONNECTION_TIMEOUT);
                    urlConnection.setReadTimeout(OTPApp.HTTP_SOCKET_TIMEOUT);
                    urlConnection.connect();
                    status = urlConnection.getResponseCode();
                    serverInfo = mapper.readValue(urlConnection.getInputStream(), ServerInfo.class);
                    prefsEditor.putString(OTPApp.PREFERENCE_KEY_FOLDER_STRUCTURE_PREFIX, OTPApp.FOLDER_STRUCTURE_PREFIX_OLD);
                } catch (IOException e2) {
                    Log.e(OTPApp.TAG, "Unable to reach server: " + e2.getMessage());
                    message = context.getResources().getString(R.string.toast_server_checker_error_unreachable)
                            + " "
                            + e2.getMessage();
                    return message;
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                }
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }

            if (serverInfo != null){
                int api_version = OTPApp.API_VERSION_V3;
                if (serverInfo.serverVersion.major == 0){
                    if (serverInfo.serverVersion.minor >= OTPApp.API_VERSION_MINOR_019){
                        api_version = OTPApp.API_VERSION_V2;
                    }else if (serverInfo.serverVersion.minor >= OTPApp.API_VERSION_MINOR_011){
                        api_version = OTPApp.API_VERSION_V1;
                    }else {
                        api_version = OTPApp.API_VERSION_PRE_V1;
                    }
                }
                prefsEditor.putInt(OTPApp.PREFERENCE_KEY_API_VERSION,
                        api_version);
                prefsEditor.commit();
            }


            if (status == HttpURLConnection.HTTP_OK) {
                message += context.getResources().getString(android.R.string.yes);
                isWorking = true;
            } else {
                message += context.getResources().getString(android.R.string.no);
            }

            return message;
        }
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();

        Toast.makeText(context, context.getResources().getString(R.string.toast_server_checker_info_error),
                Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPostExecute(String result) {
        if (activity.get() != null) {
            try {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            } catch (Exception e) {
                Log.e(OTPApp.TAG, "Error in Server Checker PostExecute dismissing dialog: " + e);
            }
        }

        if (showMessage) {
            Activity activityRetrieved = activity.get();
            if (activityRetrieved != null) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(activityRetrieved);
                dialog.setTitle(context.getResources()
                        .getString(R.string.server_checker_info_title));
                dialog.setMessage(result);
                dialog.setNeutralButton(context.getResources().getString(android.R.string.ok),
                        null);
                dialog.create().show();
            }
        } else{
            if (isCustomServer && showToast) {
                if (isWorking) {
                    Toast.makeText(context,
                            context.getResources().getString(R.string.toast_server_checker_successful),
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context,
                            context.getResources().getString(R.string.settings_menu_custom_server_url_description_error_unreachable),
                            Toast.LENGTH_SHORT).show();
                }
            }
            if (callback != null) {
                callback.onServerCheckerComplete(result, isCustomServer, isAutoDetected, isWorking);
            }
        }
    }

}
