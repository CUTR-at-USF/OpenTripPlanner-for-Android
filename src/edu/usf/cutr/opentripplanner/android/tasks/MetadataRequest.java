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
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.usf.cutr.opentripplanner.android.MyActivity;
import edu.usf.cutr.opentripplanner.android.OTPApp;
import edu.usf.cutr.opentripplanner.android.model.Server;

/**
 * @author Khoa Tran
 *
 */

public class MetadataRequest extends AsyncTask<String, Integer, Long> {
	private GraphMetadata metadata;
	private static final String TAG = "OTP";
	private ProgressDialog progressDialog;
	private MyActivity activity;
	
	private static ObjectMapper mapper = null;

	public MetadataRequest(MyActivity activity) {
		this.activity = activity;
		progressDialog = new ProgressDialog(activity);
	}

	protected void onPreExecute() {
		progressDialog = ProgressDialog.show(activity, "",
				"Getting graph metadata. Please wait... ", true);
	}

	protected Long doInBackground(String... reqs) {
		int count = reqs.length;
		long totalSize = 0;
		for (int i = 0; i < count; i++) {
			metadata = requestMetadata();
			// publishProgress((int) ((i / (float) count) * 100));
		}
		return totalSize;
	}

	protected void onPostExecute(Long result) {
		if (progressDialog.isShowing()) {
			progressDialog.dismiss();
		}
		
		if (metadata != null) {
			OTPApp app = ((OTPApp) activity.getApplication());
			double lowerLeftLatitude = metadata.getLowerLeftLatitude();
			double lowerLeftLongitude = metadata.getLowerLeftLongitude();
			double upperRightLatitude = metadata.getUpperRightLatitude();
			double upperRightLongitude = metadata.getUpperRightLongitude();
			
			Server selectedServer = app.getSelectedServer();
			if(metadata.getLowerLeftLatitude()!=0) 
				selectedServer.setLowerLeftLatitude(lowerLeftLatitude);
			
			if(metadata.getLowerLeftLongitude()!=0) 
				selectedServer.setLowerLeftLongitude(lowerLeftLongitude);
			
			if(metadata.getUpperRightLatitude()!=0) 
				selectedServer.setUpperRightLatitude(upperRightLatitude);
			
			if(metadata.getUpperRightLongitude()!=0) 
				selectedServer.setUpperRightLongitude(upperRightLongitude);
			
			Log.v(TAG, "LowerLeft: " + Double.toString(lowerLeftLatitude)+","+Double.toString(lowerLeftLongitude));
			Log.v(TAG, "UpperRight" + Double.toString(upperRightLatitude)+","+Double.toString(upperRightLongitude));
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
	
	private GraphMetadata requestMetadata() {
		String res = "/metadata";
		
		OTPApp app = ((OTPApp) activity.getApplication());
		Server server = app.getSelectedServer();
		if (server == null) {
			//TODO - handle error for no server selected
			return null;
		}
		String u = server.getBaseURL() + res;

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