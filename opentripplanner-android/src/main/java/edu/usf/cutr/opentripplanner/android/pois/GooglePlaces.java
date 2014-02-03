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

package edu.usf.cutr.opentripplanner.android.pois;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.usf.cutr.opentripplanner.android.OTPApp;

import android.util.Log;

/**
 * @author Khoa Tran
 *
 */

public class GooglePlaces implements Places{
	private String request = "https://maps.googleapis.com/maps/api/place/textsearch/json?";

	private String apiKey;

	private static String TAG = "OTP";
	
	public static final String PARAM_LOCATION = "location";
	public static final String PARAM_RADIUS = "radius";
	public static final String PARAM_NAME = "query";

	// JSON Node names
	private static final String TAG_HTML_ATTRIBUTIONS = "html_attributions";
	private static final String TAG_STATUS = "status";
	private static final String TAG_RESULTS = "results";
	private static final String TAG_GEOMETRY = "geometry";
	private static final String TAG_LOCATION = "location";
	private static final String TAG_VIEWPORT = "viewport"; 
	private static final String TAG_LATITUDE = "lat";
	private static final String TAG_LONGITUDE = "lng";
	private static final String TAG_ICON = "icon";
	private static final String TAG_NAME = "name";
	private static final String TAG_RATING = "rating";
	private static final String TAG_REFERENCE = "reference";
	private static final String TAG_TYPES = "types";
	private static final String TAG_VICINITY = "vicinity";
	private static final String TAG_FORMATTED_ADDRESS = "formatted_address";
	private static final String TAG_EVENTS = "events";
	private static final String TAG_EVENT_ID = "event_id";
	private static final String TAG_SUMMARY = "summary";
	private static final String TAG_URL = "url";

	// contacts JSONArray
	JSONArray results = null;

	public GooglePlaces(String apiKey){
		this.setApiKey(apiKey);
	}

	public JSONObject requestPlaces(String paramLocation, String paramRadius, String paramName){
		StringBuilder builder = new StringBuilder();
		HttpClient client = new DefaultHttpClient();
		
		String encodedParamLocation = "";
		String encodedParamRadius = "";
		String encodedParamName = "";
		try {
			if ((paramLocation != null) && (paramRadius != null)){
				encodedParamLocation = URLEncoder.encode(paramLocation, OTPApp.URL_ENCODING);
				encodedParamRadius = URLEncoder.encode(paramRadius, OTPApp.URL_ENCODING);
			}
			encodedParamName = URLEncoder.encode(paramName, OTPApp.URL_ENCODING);
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return null;
		}
		
		if ((paramLocation != null) && (paramRadius != null)){
			request += "location=" + encodedParamLocation;
			request += "&radius=" + encodedParamRadius;
			request += "&query=" + encodedParamName;
		}
		else{
			request += "query=" + encodedParamName;
		}
		request += "&sensor=false";
		request += "&key=" + getApiKey();

		Log.v(TAG, request);

		HttpGet httpGet = new HttpGet(request);
		try {
			HttpResponse response = client.execute(httpGet);
			StatusLine statusLine = response.getStatusLine();
			int statusCode = statusLine.getStatusCode();
			if (statusCode == 200) {
				HttpEntity entity = response.getEntity();
				InputStream content = entity.getContent();
				BufferedReader reader = new BufferedReader(new InputStreamReader(content));
				String line;
				while ((line = reader.readLine()) != null) {
					builder.append(line);
				}
			} else {
				Log.v(TAG, "Failed to download file");
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Log.v(TAG, builder.toString());
		
		JSONObject json = null;
		try {
			json = new JSONObject(builder.toString());
		} catch (JSONException e) {
			Log.e(TAG, "Error parsing data " + e.toString());
		}
		
		return json;
	}

	public ArrayList<POI> getPlaces(HashMap<String, String> params){
		ArrayList<POI> pois = new ArrayList<POI>();
		
		String paramLocation = params.get(PARAM_LOCATION);
		String paramRadius = params.get(PARAM_RADIUS); 
		String paramName = params.get(PARAM_NAME);
		
		// Get JSON
		JSONObject json = this.requestPlaces(paramLocation, paramRadius, paramName);

		if (json != null){
			// Decrypt JSON
			try {
				results = json.getJSONArray(TAG_RESULTS);

				for(int i = 0; i < results.length(); i++){
					JSONObject r = results.getJSONObject(i);

					String name = r.getString(TAG_NAME);
					String address = r.getString(TAG_FORMATTED_ADDRESS);

					JSONObject geometry = r.getJSONObject(TAG_GEOMETRY);
					JSONObject location = geometry.getJSONObject(TAG_LOCATION);
					double lat = location.getDouble(TAG_LATITUDE);
					double lon = location.getDouble(TAG_LONGITUDE);
					
					POI point = new POI(name, address, lat, lon);
					pois.add(point);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		return pois;
	}

	/**
	 * @return the apiKey
	 */
	public String getApiKey() {
		return apiKey;
	}

	/**
	 * @param apiKey the apiKey to set
	 */
	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}
}
