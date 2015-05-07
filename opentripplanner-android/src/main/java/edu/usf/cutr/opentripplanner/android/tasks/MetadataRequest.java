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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.opentripplanner.api.ws.GraphMetadata;

import android.app.Activity;
import android.app.IntentService;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

import edu.usf.cutr.opentripplanner.android.OTPApp;
import edu.usf.cutr.opentripplanner.android.R;
import edu.usf.cutr.opentripplanner.android.listeners.MetadataRequestCompleteListener;

/**
 * @author Khoa Tran
 */
public class MetadataRequest extends IntentService {
    ObjectMapper mapper;

    public MetadataRequest() {
        super("MetadataRequest");
    }

    public void onHandleIntent(Intent intent) {
        String reqs = intent.getStringExtra("reqs");
        String prefix = PreferenceManager.getDefaultSharedPreferences(this)
                .getString(OTPApp.PREFERENCE_KEY_FOLDER_STRUCTURE_PREFIX,
                        OTPApp.FOLDER_STRUCTURE_PREFIX_NEW);
        String u = reqs + prefix + OTPApp.METADATA_LOCATION;
        Log.d(OTPApp.TAG, "URL: " + u);
        HttpURLConnection urlConnection = null;
        GraphMetadata metadata = null;
        try {
            URL url = new URL(u);
            if (mapper == null) {
                mapper = new ObjectMapper();
            }
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestProperty("Accept", "application/json");
            urlConnection.setConnectTimeout(OTPApp.HTTP_CONNECTION_TIMEOUT);
            urlConnection.setReadTimeout(OTPApp.HTTP_SOCKET_TIMEOUT);
            metadata = mapper.readValue(urlConnection.getInputStream(), GraphMetadata.class);
        } catch (IOException e) {
            Log.e(OTPApp.TAG, "Error fetching JSON or XML: " + e);
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        Intent resultIntent = new Intent(intent.getStringExtra("FILTER"));
        resultIntent.putExtra("metadata", metadata);
        LocalBroadcastManager.getInstance(this).sendBroadcast(resultIntent);
    }

    public static class MetadataRequestReceiver extends BroadcastReceiver {

        private ProgressDialog progressDialog;

        private Activity activity;

        private Context context;

        private MetadataRequestCompleteListener callback;

        public MetadataRequestReceiver(Activity activity, Context context,
                MetadataRequestCompleteListener callback) {
            this.activity = activity;
            this.context = context;
            this.callback = callback;
            progressDialog = new ProgressDialog(activity);
        }

        public void showProgressDialog() {
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(true);
            progressDialog = ProgressDialog.show(activity, "",
                    context.getResources().getString(R.string.task_progress_metadata_progress), true);
        }

        public void onReceive(Context receiverContext, Intent receiverIntent) {
            GraphMetadata metadata = (GraphMetadata) receiverIntent.getSerializableExtra("metadata");
            try {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            } catch (Exception e) {
                Log.e(OTPApp.TAG, "Error in Metadata Request PostExecute dismissing dialog: " + e);
            }

            if (metadata != null) {
                Toast.makeText(context,
                        context.getResources().getString(R.string.toast_metadata_request_successful),
                        Toast.LENGTH_SHORT).show();
                callback.onMetadataRequestComplete(metadata, true);
            } else {
                Toast.makeText(context, context.getResources().getString(R.string.toast_server_checker_info_error),
                        Toast.LENGTH_SHORT).show();

                Log.e(OTPApp.TAG, "No metadata!");
            }
        }
    }
}

