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
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

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

    private boolean showMessage;

    private boolean isWorking = false;

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
            ServerCheckerCompleteListener callback, boolean showMessage) {
        this.activity = activity;
        this.context = context;
        this.showMessage = showMessage;
        this.callback = callback;
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
                                context.getString(R.string.server_checker_progress),
                                true);
            }
        }
    }

    @Override
    protected String doInBackground(Server... params) {
        Server server = params[0];

        if (server == null) {
            Log.w(OTPApp.TAG,
                    "Tried to get server info when no server was selected");
            cancel(true);
            return null;
        } else {
            String message =
                    context.getResources().getString(R.string.server_checker_info_dialog_region)
                            + " " + server.getRegion() + "\n" + context.getResources()
                            .getString(R.string.server_checker_info_dialog_language) + " " + server
                            .getLanguage() + "\n" + context.getResources()
                            .getString(R.string.server_checker_info_dialog_contact) + " " + server
                            .getContactName() + " ("
                            + server.getContactEmail() + ")" + "\n" + context.getResources()
                            .getString(R.string.server_checker_info_dialog_url) + " " + server
                            .getBaseURL()
                            + "\n" + context.getResources()
                            .getString(R.string.server_checker_info_dialog_bounds) + " " + server
                            .getBounds() + "\n" + context.getResources()
                            .getString(R.string.server_checker_info_dialog_reachable) + " ";

            int status = 0;
            HttpURLConnection urlConnection = null;

            try {
                URL url = new URL(server.getBaseURL() + "/plan");
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setConnectTimeout(OTPApp.HTTP_CONNECTION_TIMEOUT);
                urlConnection.setReadTimeout(OTPApp.HTTP_SOCKET_TIMEOUT);
                urlConnection.connect();
                status = urlConnection.getResponseCode();
            } catch (IOException e) {
                Log.e(OTPApp.TAG, "Unable to reach server: " + e.getMessage());
                message = context.getResources().getString(R.string.server_checker_error_message)
                        + " "
                        + e.getMessage();
                return message;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }

            if (status == HttpURLConnection.HTTP_OK) {
                message += context.getResources().getString(R.string.yes);
                isWorking = true;
            } else {
                message += context.getResources().getString(R.string.no);
            }

            return message;
        }
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();

        Toast.makeText(context, context.getResources().getString(R.string.info_server_error),
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
                        .getString(R.string.server_checker_info_dialog_title));
                dialog.setMessage(result);
                dialog.setNeutralButton(context.getResources().getString(android.R.string.ok),
                        null);
                dialog.create().show();
            }
        } else {
            if (isWorking) {
                Toast.makeText(context,
                        context.getResources().getString(R.string.server_checker_successful),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context,
                        context.getResources().getString(R.string.custom_server_error),
                        Toast.LENGTH_SHORT).show();
            }
        }

        if (callback != null) {
            callback.onServerCheckerComplete(result, isWorking);
        }
    }

}
