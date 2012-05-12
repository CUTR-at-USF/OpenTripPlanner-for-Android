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
import org.opentripplanner.android.OTPApp;
import org.opentripplanner.android.fragments.MainFragment;
import org.opentripplanner.android.model.Server;
import org.opentripplanner.api.model.Itinerary;
import org.opentripplanner.api.model.Leg;
import org.opentripplanner.api.ws.Request;
import org.opentripplanner.api.ws.Response;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import de.mastacode.http.Http;

/**
 * Modified by Khoa Tran
 *
 */

public class TripRequest extends AsyncTask<Request, Integer, Long> {
	private Response response;
	private static final String TAG = "OTP";
	private ProgressDialog progressDialog;
	private MainFragment mainFragment;

	public TripRequest(MainFragment mainFragment) {
		this.mainFragment = mainFragment;
		progressDialog = new ProgressDialog(mainFragment.getActivity());
	}

	protected void onPreExecute() {
		progressDialog = ProgressDialog.show(mainFragment.getActivity(), "",
				"Generating trip. Please wait... ", true);
	}

	protected Long doInBackground(Request... reqs) {
		int count = reqs.length;
		long totalSize = 0;
		for (int i = 0; i < count; i++) {
			// totalSize += Downloader.downloadFile(reqs[i]);
			response = requestPlan(reqs[i]);
			// publishProgress((int) ((i / (float) count) * 100));
		}
		return totalSize;
	}

//	     protected void onProgressUpdate(Integer... progress) {
//	       //  setProgressPercent(progress[0]);
//	    	 setProgress(progress[0]);
//	    	 //TODO - fix tag
//	    	 Log.v(TAG, "Progress: " + progress[0]);
//	     }

	protected void onPostExecute(Long result) {
		if (progressDialog.isShowing()) {
			progressDialog.dismiss();
		}
		
		if (response != null && response.getPlan() != null && response.getPlan().itinerary.get(0) != null) {
			
			List<Itinerary> itineraries = response.getPlan().itinerary;
			
//			mainFragment.setItineraries(itineraries);
			
			List<Leg> legs = response.getPlan().itinerary.get(0).legs;
			mainFragment.showRouteOnMap(legs);
			mainFragment.getFragmentListener().onItinerarySelected(legs);
		} else {
			// TODO - handle errors here?
			if(response != null && response.getError() != null) {
				String msg = response.getError().getMsg();
				AlertDialog.Builder feedback = new AlertDialog.Builder(mainFragment.getActivity());
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
		
		OTPApp app = ((OTPApp) mainFragment.getActivity().getApplication());
		Server server = app.getSelectedServer();
		if (server == null) {
			//TODO - handle error for no server selected
			return null;
		}
		String u = server.getBaseURL() + res + params;
		
		//Below fixes a bug where the New York OTP server will whine
		//if doesn't get the parameter for intermediate places
		if(server.getRegion().equalsIgnoreCase("New York")) {
			u += "&intermediatePlaces=";
		}
		
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

		Response plan = null;
		try {
			plan = serializer.read(Response.class, result);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Log.e(TAG, e.getMessage());
			e.printStackTrace();
			return null;
		}
		//TODO - handle errors and error responses
		if(plan == null) {
			Log.d(TAG, "No response?");
			return null;
		}
		return plan;
	}
}
