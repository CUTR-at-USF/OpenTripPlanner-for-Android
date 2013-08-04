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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.opentripplanner.api.ws.GraphMetadata;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.usf.cutr.opentripplanner.android.R;
import edu.usf.cutr.opentripplanner.android.listeners.MetadataRequestCompleteListener;

/**
 * @author Khoa Tran
 *
 */

public class MetadataRequest extends AsyncTask<String, Integer, GraphMetadata> {
	private GraphMetadata metadata;
	private static final String TAG = "OTP";
	private ProgressDialog progressDialog;
	private Context context;
	
	private MetadataRequestCompleteListener callback;
	
	private static ObjectMapper mapper = null;

	public MetadataRequest(Context context, MetadataRequestCompleteListener callback) {
		this.context = context;
		this.callback = callback;
		progressDialog = new ProgressDialog(context);
	}

	protected void onPreExecute() {
		progressDialog = ProgressDialog.show(context,"",
				context.getResources().getString(R.string.metadata_request_progress), true);

	}

	protected GraphMetadata doInBackground(String... reqs) {
		int count = reqs.length;
		for (int i = 0; i < count; i++) {
			String serverURL = reqs[0];
			metadata = requestMetadata(serverURL);
			// publishProgress((int) ((i / (float) count) * 100));
		}
		return metadata;
	}

	protected void onPostExecute(GraphMetadata metadata) {
		try{
			if (progressDialog != null && progressDialog.isShowing()) {
				progressDialog.dismiss();
			}
		}catch(Exception e){
			Log.e(TAG, "Error in Metadata Request PostExecute dismissing dialog: " + e);
		}

		Toast.makeText(context, context.getResources().getString(R.string.metadata_request_successful), Toast.LENGTH_SHORT).show();
		

		if (metadata != null) {
			callback.onMetadataRequestComplete(metadata);
		} else {
			// TODO - handle errors here?
//			String msg = "No metadata";
//			AlertDialog.Builder feedback = new AlertDialog.Builder(activity);
//			feedback.setTitle("Error Planning Trip");
//			feedback.setMessage(msg);
//			feedback.setNeutralButton("OK", null);
//			feedback.create().show();
			
			Log.e(TAG, "No metadata!");
		}
	}
	
	private GraphMetadata requestMetadata(String serverURL) {
		String res = context.getResources().getString(R.string.metadata_location);

		String u = serverURL + res;

		Log.d(TAG, "URL: " + u);
			
		HttpURLConnection urlConnection = null;
		URL url = null;
		GraphMetadata plan = null;

		try {

			url = new URL(u);

			disableConnectionReuseIfNecessary(); // For bugs in HttpURLConnection pre-Froyo

			// Serializer serializer = new Persister();
			
			if(mapper == null){
				mapper = new ObjectMapper();
			}

			urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setRequestProperty("Accept", "application/json");

			// plan = serializer.read(Response.class, result);
			plan = mapper.readValue(urlConnection.getInputStream(),
					GraphMetadata.class);
			
		} catch (IOException e) {
			Log.e(TAG, "Error fetching JSON or XML: " + e);
			e.printStackTrace();
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
		//if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {  //Should change to this once we update to Android 4.1 SDK
		if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.ECLAIR_MR1) {
			System.setProperty("http.keepAlive", "false");
		}
	}
}