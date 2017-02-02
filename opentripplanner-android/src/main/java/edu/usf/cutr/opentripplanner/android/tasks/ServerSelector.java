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

import com.google.android.gms.maps.model.LatLng;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.util.Log;
import android.webkit.URLUtil;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import au.com.bytecode.opencsv.CSVReader;
import edu.usf.cutr.opentripplanner.android.OTPApp;
import edu.usf.cutr.opentripplanner.android.R;
import edu.usf.cutr.opentripplanner.android.exceptions.ServerListParsingException;
import edu.usf.cutr.opentripplanner.android.listeners.ServerCheckerCompleteListener;
import edu.usf.cutr.opentripplanner.android.listeners.ServerSelectorCompleteListener;
import edu.usf.cutr.opentripplanner.android.model.Server;
import edu.usf.cutr.opentripplanner.android.sqlite.ServersDataSource;
import edu.usf.cutr.opentripplanner.android.util.LocationUtil;

import static edu.usf.cutr.opentripplanner.android.OTPApp.PREFERENCE_KEY_AUTO_DETECT_SERVER;
import static edu.usf.cutr.opentripplanner.android.OTPApp.PREFERENCE_KEY_CUSTOM_SERVER_URL;
import static edu.usf.cutr.opentripplanner.android.OTPApp.PREFERENCE_KEY_CUSTOM_SERVER_URL_IS_VALID;
import static edu.usf.cutr.opentripplanner.android.OTPApp.PREFERENCE_KEY_SELECTED_CUSTOM_SERVER;
import static edu.usf.cutr.opentripplanner.android.OTPApp.PREFERENCE_KEY_SELECTED_SERVER;

/**
 * A task that retrieves the list of OTP servers from the Google Docs directory,
 * and if specified, automatically chooses the server based on the geographic bounds
 * and user current location
 *
 * @author Marcy Gordon
 * @author Khoa Tran
 */

public class ServerSelector extends AsyncTask<LatLng, Integer, Integer>
        implements ServerCheckerCompleteListener {

    private Server selectedServer;

    private ProgressDialog progressDialog;

    private WeakReference<Activity> activity;

    private Context context;

    private static List<Server> knownServers = new ArrayList<Server>();

    private boolean mustRefreshList = false;

    private boolean isAutoDetectEnabled = true;

    private ServerSelectorCompleteListener callback;

    private boolean selectedCustomServer;

    private boolean showDialog;

    public ServersDataSource dataSource = null;

    /**
     * Constructs a new ServerSelector
     *
     * @param mustRefreshList true if we should download a new list of servers from the Google
     *                        Doc, false if we should use cached list of servers
     * @param showDialog      true if a progress dialog is requested
     */
    public ServerSelector(WeakReference<Activity> activity, Context context,
            ServersDataSource dataSource, ServerSelectorCompleteListener callback,
            boolean mustRefreshList, boolean showDialog) {
        this.activity = activity;
        this.context = context;
        this.dataSource = dataSource;
        this.callback = callback;
        this.mustRefreshList = mustRefreshList;
        this.showDialog = showDialog;
        Activity activityRetrieved = activity.get();
        if ((activityRetrieved != null) && showDialog) {
            progressDialog = new ProgressDialog(activityRetrieved);
        }
    }

    protected void onPreExecute() {
        Activity activityRetrieved = activity.get();
        
        if ((activityRetrieved != null) && showDialog) {
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(true);
            progressDialog = ProgressDialog.show(activityRetrieved, "",
                    context.getResources().getString(R.string.task_progress_server_selector_progress), true);
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        isAutoDetectEnabled = prefs.getBoolean(OTPApp.PREFERENCE_KEY_AUTO_DETECT_SERVER,
                true);
    }


    protected Integer doInBackground(LatLng... latLng) {
        LatLng currentLocation = latLng[0];

        List<Server> serverList = null;

        // If not forced to refresh list
        if (!mustRefreshList) {
            // Check if servers are stored in SQLite?
            Log.d(OTPApp.TAG, "Attempt retrieving servers from sqlite");
            serverList = getServersFromSQLite();
        }

        // If forced to refresh list OR
        // If severs are not stored, download list from the Google Spreadsheet and Insert to database
        if (serverList == null || serverList.isEmpty() || mustRefreshList) {
            Log.d(OTPApp.TAG,
                    "No data from sqlite. Attempt retrieving servers from google spreadsheet");
            serverList = downloadServerList(
                    context.getResources().getString(R.string.servers_spreadsheet_url));

            // Insert new list to database
            if (serverList != null && !serverList.isEmpty()) {
                insertServerListToDatabase(serverList);
                serverList = getServersFromSQLite();
            }

            // If still null
            if (serverList == null || serverList.isEmpty()) {
                return null;
            }
        }

        knownServers.clear();
        knownServers.addAll(serverList);

        //If we're autodetecting a server, get the location find the optimal server
        if (isAutoDetectEnabled && (currentLocation != null)) {
            selectedServer = findOptimalSever(currentLocation);
        }

        return serverList.size();
    }

    private List<Server> getServersFromSQLite() {
        List<Server> servers = new ArrayList<Server>();

        dataSource.open();
        List<Server> values = dataSource.getMostRecentServers();
        String shown = "";
        for (Server s : values) {
            shown += s.getRegion() + s.getDate().toString() + "\n";
            servers.add(new Server(s));
        }
        Log.d(OTPApp.TAG, shown);
        dataSource.close();

        dataSource.open();
        Calendar someDaysBefore = Calendar.getInstance();
        someDaysBefore.add(Calendar.DAY_OF_MONTH, -OTPApp.EXPIRATION_DAYS_FOR_SERVER_LIST);
        Long serversUpdateDate = dataSource.getMostRecentDate();
        if ((serversUpdateDate != null) && (someDaysBefore.getTime().getTime()
                > serversUpdateDate)) {
            servers = null;
        }
        dataSource.close();
//		Toast.makeText(activity.getApplicationContext(), shown, Toast.LENGTH_SHORT).show();

        return servers;
    }

    /**
     * Downloads the list of OTP servers from the Google Doc directory
     *
     * @param urlString URL address of the Google Doc
     * @return Server a list of OTP servers contained in the Google Doc
     */
    private List<Server> downloadServerList(String urlString) {
        HttpURLConnection urlConnection = null;

        try {
            List<Server> serverList = new ArrayList<Server>();
            URL url = new URL(urlString);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setConnectTimeout(OTPApp.HTTP_CONNECTION_TIMEOUT);
            urlConnection.setReadTimeout(OTPApp.HTTP_SOCKET_TIMEOUT);
            urlConnection.connect();

            BufferedReader bufferedReader = new BufferedReader
                    (new InputStreamReader(urlConnection.getInputStream()));
            CSVReader reader = new CSVReader(bufferedReader);
            try {
                Long currentTime = Calendar.getInstance().getTime().getTime();

                List<String[]> serversStrings = reader.readAll();
                for (String[] serverString : serversStrings) {
                    if (serverString[0].equalsIgnoreCase("Region")) {
                        continue; //Ignore the first line of the file
                    }
                    for (String stringEntry : serverString) {
                        stringEntry = stringEntry.trim();
                    }
                    boolean allFieldsNotNull = true;
                    for (String serverField : serverString) {
                        serverField = serverField.trim();
                        if (serverField.equals("")) {
                            Log.e(OTPApp.TAG, "Some necessary fields are null, server not added");
                            allFieldsNotNull = false;
                            break;
                        }
                    }
                    if (allFieldsNotNull && (serverString.length >= 9)) {
                        try {
                            Server s = new Server(currentTime, serverString[0], serverString[1],
                                    serverString[2], serverString[3], serverString[4],
                                    serverString[5], serverString[6], serverString[7],
                                    serverString[8]);
                            serverList.add(s);
                        } catch (ServerListParsingException e) {
                            Log.e(OTPApp.TAG,
                                    "Error parsing necessary fields, server not added: " + e);
                        }
                    } else {
                        Log.e(OTPApp.TAG,
                                "Server does not provide necessary fields, server not added");
                    }
                }
            } catch (IOException e) {
                Log.e(OTPApp.TAG, "Problem reading CSV server file: " + e.getMessage());

                try {
                    reader.close();
                } catch (IOException e2) {
                    Log.e(OTPApp.TAG, "Error closing CSVReader file: " + e2);
                }
                return null;
            } finally {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e(OTPApp.TAG, "Error closing CSVReader file: " + e);
                }
            }
            Log.d(OTPApp.TAG, "Servers: " + serverList.size());
            return serverList;

        } catch (IOException e) {
            Log.e(OTPApp.TAG, "Unable to download spreadsheet with server list: " + e.getMessage());
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }


    }

    private void insertServerListToDatabase(List<Server> servers) {
        dataSource.open();
        for (Server server : servers) {
            if (dataSource.createServer(server) == null) {
                Log.e(OTPApp.TAG, "Some server fields are incorrect, server not added");
            }
        }
        dataSource.close();
    }

    /**
     * Automatically detects the correct OTP server based on the location of the device
     *
     * @param currentLocation location of the device
     * @return Server the OTP server that the location is within
     */
    private Server findOptimalSever(LatLng currentLocation) {
        if (currentLocation == null) {
            return null;
        }

        //If we've already selected a server, just return the one we selected
        if (selectedServer != null) {
            return selectedServer;
        }

        boolean isInBoundingBox;
        Server server = null;
        for (Server knownServer : knownServers) {
            isInBoundingBox = LocationUtil.checkPointInBoundingBox(currentLocation, knownServer);

            if (isInBoundingBox) {
                server = knownServer;
                break;
            }
        }

        return server;
    }

    protected void onPostExecute(Integer result) {
        if ((activity.get() != null) && showDialog) {
            try {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            } catch (Exception e) {
                Log.e(OTPApp.TAG, "Error in Server Selector PostExecute dismissing dialog: " + e);
            }
        }

        if (selectedServer != null) {
            //We've already auto-selected a server
            ServerChecker serverChecker = new ServerChecker(activity,
                    context, ServerSelector.this, false, false, true);
            serverChecker.execute(selectedServer);
        } else if (knownServers != null && !knownServers.isEmpty()) {
            Log.d(OTPApp.TAG,
                    "No server automatically selected.  User will need to choose the OTP server.");

            // Create dialog for user to choose
            List<String> serverNames = new ArrayList<String>();
            for (Server server : knownServers) {
                serverNames.add(server.getRegion());
            }

            Collections.sort(serverNames);

            serverNames.add(0, context.getResources().getString(R.string.server_checker_info_custom_server_name));

            final CharSequence[] items = serverNames.toArray(new CharSequence[serverNames.size()]);

            Activity activityRetrieved = activity.get();

            if (activityRetrieved != null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(activityRetrieved);
                builder.setTitle(context.getResources()
                        .getString(R.string.server_checker_info_title));
                        builder.setCancelable(false);
                builder.setItems(items, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int item) {

                        //If the user selected to enter a custom URL, they are shown this EditText box to enter it
                        if (items[item].equals(context.getResources()
                                .getString(R.string.server_checker_info_custom_server_name))) {
                            SharedPreferences prefs = PreferenceManager
                                    .getDefaultSharedPreferences(context);

                            Activity activityRetrieved = activity.get();

                            if (activityRetrieved != null) {
                                final EditText tbBaseURL = new EditText(activityRetrieved);
                                String actualCustomServer = prefs
                                        .getString(PREFERENCE_KEY_CUSTOM_SERVER_URL, "");
                                tbBaseURL.setText(actualCustomServer);

                                AlertDialog.Builder urlAlert = new AlertDialog.Builder(
                                        activityRetrieved);
                                urlAlert.setTitle(context.getResources()
                                        .getString(
                                                R.string.server_selector_custom_server_alert_title));
                                urlAlert.setView(tbBaseURL);
                                urlAlert.setPositiveButton(
                                        context.getResources().getString(android.R.string.ok),
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog,
                                                    int whichButton) {
                                                Editable tbEditable = tbBaseURL.getText();
                                                if (tbEditable != null) {
                                                    String value = tbEditable.toString().trim();
                                                    if (URLUtil.isValidUrl(value)) {
                                                        SharedPreferences.Editor prefsEditor
                                                                = PreferenceManager
                                                                .getDefaultSharedPreferences(
                                                                        context)
                                                                .edit();
                                                        prefsEditor
                                                                .putString(
                                                                        PREFERENCE_KEY_CUSTOM_SERVER_URL,
                                                                        value);

                                                        ServerChecker serverChecker
                                                                = new ServerChecker(activity,
                                                                context, ServerSelector.this, true,
                                                                true, false);
                                                        serverChecker.execute(
                                                                new Server(value, context));
                                                        prefsEditor.commit();
                                                    } else {
                                                        Toast.makeText(context,
                                                                context.getResources()
                                                                        .getString(
                                                                                R.string.settings_menu_custom_server_url_description_error_url),
                                                                Toast.LENGTH_SHORT).show();
                                                    }
                                                }

                                            }
                                        });
                                selectedCustomServer = true;
                                urlAlert.create().show();
                            }
                        } else {
                            //User picked server from the list
                            for (Server server : knownServers) {
                                //If this server region matches what the user picked, then set the server as the selected server
                                if (server.getRegion().equals(items[item])) {
                                    selectedServer = server;
                                    ServerChecker serverChecker = new ServerChecker(activity,
                                            context, ServerSelector.this, false, false, false);
                                    serverChecker.execute(selectedServer);
                                    break;
                                }
                            }
                        }
                        Log.d(OTPApp.TAG, "Chosen: " + items[item]);
                    }
                });
                builder.show();
            }
        } else {
            Log.e(OTPApp.TAG, "Server list could not be downloaded!!");
            Toast.makeText(context,
                    context.getResources().getString(R.string.toast_server_selector_refresh_server_list_error),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onServerCheckerComplete(String result, boolean isCustomServer,
            boolean isAutoDetected, boolean isWorking) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        SharedPreferences.Editor prefsEditor = prefs.edit();
        if (isCustomServer){
            if (isWorking) {
                prefsEditor.putBoolean(PREFERENCE_KEY_AUTO_DETECT_SERVER, false);
                prefsEditor.putBoolean(PREFERENCE_KEY_SELECTED_CUSTOM_SERVER, true);
                prefsEditor.putBoolean(PREFERENCE_KEY_CUSTOM_SERVER_URL_IS_VALID, true);
                prefsEditor.commit();
                if (selectedCustomServer) {
                    String baseURL = prefs.getString(PREFERENCE_KEY_CUSTOM_SERVER_URL, "");
                    selectedServer = new Server(baseURL, context);
                    callback.onServerSelectorComplete(selectedServer);
                }
            } else {
                prefsEditor.putBoolean(PREFERENCE_KEY_CUSTOM_SERVER_URL_IS_VALID, false);
                prefsEditor.putBoolean(PREFERENCE_KEY_SELECTED_CUSTOM_SERVER, false);
                prefsEditor.commit();
                Toast.makeText(context,
                        context.getResources().getString(R.string.toast_server_checker_error_bad_url),
                        Toast.LENGTH_SHORT).show();
            }
        }
        else{
            if (isWorking){
                if (isAutoDetected){
                    long serverId = prefs.getLong(OTPApp.PREFERENCE_KEY_SELECTED_SERVER, 0);
                    Server s = null;
                    boolean serverIsChanged = true;
                    if (serverId != 0) {
                        dataSource.open();
                        s = dataSource.getServer(prefs.getLong(OTPApp.PREFERENCE_KEY_SELECTED_SERVER, 0));
                        dataSource.close();
                    }
                    if (s != null) {
                        serverIsChanged = !(s.getRegion().equals(selectedServer.getRegion()));
                    }
                    if (showDialog || serverIsChanged) {
                        Toast.makeText(context,
                                context.getResources()
                                        .getString(R.string.toast_server_selector_detected) + " "
                                        + selectedServer.getRegion() + ". " + context.getResources()
                                        .getString(R.string.toast_server_selector_server_change_info),
                                Toast.LENGTH_SHORT).show();
                    }
                }
                prefsEditor.putLong(PREFERENCE_KEY_SELECTED_SERVER,
                        selectedServer.getId());
                prefsEditor.putBoolean(PREFERENCE_KEY_SELECTED_CUSTOM_SERVER, false);
                prefsEditor.commit();
                callback.onServerSelectorComplete(selectedServer);
            }
            else{
                Toast.makeText(context,
                        context.getResources()
                                .getString(R.string.
                                        toast_server_checker_error_unreachable_detected_server),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
}
