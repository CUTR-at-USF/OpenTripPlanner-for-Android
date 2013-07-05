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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.opentripplanner.api.ws.Request;
import org.opentripplanner.v092snapshot.api.model.Itinerary;
import org.opentripplanner.v092snapshot.api.ws.Response;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
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
	private static final String TAG = "OTP";
	private ProgressDialog progressDialog;
	private Context context;
	private String currentRequestString = "";
	private Server selectedServer;
	private TripRequestCompleteListener callback;

	public TripRequest(Context context, Server selectedServer, TripRequestCompleteListener callback) {
		this.context = context;
		this.selectedServer = selectedServer;
		this.callback = callback;
		progressDialog = new ProgressDialog(context);
	}

	protected void onPreExecute() {
		progressDialog = ProgressDialog.show(context, "",
				context.getText(R.string.tripplanner_progress), true);
	}

	protected Long doInBackground(Request... reqs) {
		int count = reqs.length;
		long totalSize = 0;
		for (int i = 0; i < count; i++) {
			response = requestPlan(reqs[i]);
		}
		return totalSize;
	}
	
	protected void  onCancelled(Long result){

		try{		
			if (progressDialog != null && progressDialog.isShowing()) {
				progressDialog.dismiss();
			}
		}catch(Exception e){
			Log.e(TAG, "Error in TripRequest Cancelled dismissing dialog: " + e);
		}
		
		AlertDialog.Builder geocoderAlert = new AlertDialog.Builder(context);
		geocoderAlert.setTitle(R.string.tripplanner_results_title)
				.setMessage(R.string.tripplanner_no_results_message)
				.setCancelable(false)
				.setPositiveButton(R.string.confirmation, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
					}
				});

		AlertDialog alert = geocoderAlert.create();
		alert.show();
				
		Log.e(TAG, "No route to display!");
	}

	protected void onPostExecute(Long result) {
		try{		
			if (progressDialog != null && progressDialog.isShowing()) {
				progressDialog.dismiss();
			}
		}catch(Exception e){
			Log.e(TAG, "Error in TripRequest PostExecute dismissing dialog: " + e);
		}
		
		if (response != null && response.getPlan() != null && response.getPlan().getItinerary().get(0) != null) {
			
			List<Itinerary> itineraries = response.getPlan().getItinerary();
			
//			mainFragment.setItineraries(itineraries);
			
//			List<Leg> legs = response.getPlan().itinerary.get(0).legs;
			
			callback.onTripRequestComplete(itineraries, currentRequestString);
		} else {
			// TODO - handle errors here?
			if(response != null && response.getError() != null) {
				String msg = response.getError().toString();
				AlertDialog.Builder feedback = new AlertDialog.Builder(context);
				feedback.setTitle("Error Planning Trip");
				feedback.setMessage(msg);
				feedback.setNeutralButton("OK", null);
				feedback.create().show();
			}
			Log.e(TAG, "No route to display!");
		}
	}
	
	private Response requestPlan(Request requestParams) {
		HashMap<String, String> tmp = requestParams.getParameters();

		Collection c = tmp.entrySet();
		Iterator itr = c.iterator();

		String params = "";
		boolean first = true;
		while(itr.hasNext()){
			if(first) {
				params += "?" + itr.next();
				first = false;
			} else {
				params += "&" + itr.next();						
			}
		}
		
		//fromPlace=28.066192823902,-82.416927819827
		//&toPlace=28.064072155861,-82.41109133301
		//&arr=Depart
		//&min=QUICK
		//&maxWalkDistance=7600
		//&mode=WALK
		//&itinID=1
		//&submit
		//&date=06/07/2011
		//&time=11:34%20am
		
		//&showIntermediateStops=TRUE
		
		//String u = "http://go.cutr.usf.edu:8083/opentripplanner-api-webapp/ws/plan?fromPlace=28.066192823902,-82.416927819827&toPlace=28.064072155861,-82.41109133301&arr=Depart&min=QUICK&maxWalkDistance=7600&mode=WALK&itinID=1&submit&date=06/07/2011&time=11:34%20am";
		//String u = "http://go.cutr.usf.edu:8083/opentripplanner-api-webapp/ws/plan" + params;
		
		String res = "/plan";
		
		if (selectedServer == null) {
			//TODO - handle error for no server selected
			return null;
		}
		String u = selectedServer.getBaseURL() + res + params;
		
		//Below fixes a bug where the New York OTP server will whine
		//if doesn't get the parameter for intermediate places
		if(selectedServer.getRegion().equalsIgnoreCase("New York")) {
			u += "&intermediatePlaces=";
		}
		
		Log.d(TAG, "URL: " + u);
		
		currentRequestString = u;
		
//		HttpClient client = new DefaultHttpClient();
//		String result = "";
//		try {
//			result = Http.get(u).use(client).header("Accept", "application/xml").header("Keep-Alive","timeout=60, max=100").charset("UTF-8").followRedirects(true).asString();
//			Log.d(TAG, "Result: " + result);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			return null;
//		}
		
		//Serializer serializer = new Persister();  //Simple XML
		

//		Response plan = null;
//		try {
//			plan = JacksonConfig.getObjectReaderInstance().readValue(result, Response.class);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			Log.e(TAG, e.getMessage());
//			e.printStackTrace();
//			return null;
//		}
//		//TODO - handle errors and error responses
//		if(plan == null) {
//			Log.d(TAG, "No response?");
//			return null;
//		}
		
		HttpURLConnection urlConnection = null;
		URL url = null;
		Response plan = null;

		try {

			url = new URL(u);

			disableConnectionReuseIfNecessary(); // For bugs in HttpURLConnection pre-Froyo

			urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setRequestProperty("Accept", "application/json");
			urlConnection.setConnectTimeout(context.getResources().getInteger(R.integer.connection_timeout));
			urlConnection.setReadTimeout(context.getResources().getInteger(R.integer.socket_timeout));
			// plan = serializer.read(Response.class, result);
			plan = JacksonConfig.getObjectReaderInstance().readValue(urlConnection.getInputStream());
			urlConnection.disconnect();
			
		} catch (java.net.SocketTimeoutException e) {
			Log.e(TAG, "Timeout fetching JSON or XML: " + e);
			e.printStackTrace();
			cancel(true);
		} catch (IOException e) {
			Log.e(TAG, "Error fetching JSON or XML: " + e);
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
		//if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {  //TODO - Should change to this once we update to Android 4.1 SDK
		if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.ECLAIR_MR1) {
			System.setProperty("http.keepAlive", "false");
		}
	}
}
