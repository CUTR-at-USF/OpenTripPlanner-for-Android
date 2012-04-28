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

package org.opentripplanner.android.tasks;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.miscwidgets.widget.Panel;
import org.opentripplanner.android.MyActivity;
import org.opentripplanner.android.OTPApp;
import org.opentripplanner.android.model.Server;
import org.opentripplanner.api.model.EncodedPolylineBean;
import org.opentripplanner.api.model.Leg;
import org.opentripplanner.api.ws.GraphMetadata;
import org.opentripplanner.api.ws.Request;
import org.opentripplanner.api.ws.Response;
import org.osmdroid.util.GeoPoint;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;
import de.mastacode.http.Http;

public class MetadataRequest extends AsyncTask<String, Integer, Long> {
	private GraphMetadata metadata;
	private static final String TAG = "OTP";
	private ProgressDialog progressDialog;
	private MyActivity activity;

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
			
			if(metadata.getLowerLeftLatitude()!=0) 
				app.setLowerLeftLatitude(lowerLeftLatitude);
			
			if(metadata.getLowerLeftLongitude()!=0) 
				app.setLowerLeftLongitude(lowerLeftLongitude);
			
			if(metadata.getUpperRightLatitude()!=0) 
				app.setUpperRightLatitude(upperRightLatitude);
			
			if(metadata.getUpperRightLongitude()!=0) 
				app.setUpperRightLongitude(upperRightLongitude);
			
			Log.e(TAG, Double.toString(lowerLeftLatitude)+","+Double.toString(lowerLeftLongitude));
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
		
		HttpClient client = new DefaultHttpClient();
		String result = "";
		try {
			result = Http.get(u).use(client).header("Accept", "application/xml").header("Keep-Alive","timeout=60, max=100").charset("UTF-8").followRedirects(true).asString();
			Log.d(TAG, "Result: " + result);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
		Serializer serializer = new Persister();

		GraphMetadata plan = null;
		try {
			plan = serializer.read(GraphMetadata.class, result);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Log.e(TAG, e.getMessage());
			e.printStackTrace();
			return null;
		}
		//TODO - handle errors and error responses
		if(plan == null) {
			Log.d(TAG, "No response for graphmetadata?");
			return null;
		}
		return plan;
	}
}