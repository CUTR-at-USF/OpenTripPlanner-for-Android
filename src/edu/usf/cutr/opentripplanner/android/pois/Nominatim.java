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

import android.util.Log;
import edu.usf.cutr.opentripplanner.android.OTPApp;

/**
 * @author Khoa Tran
 *
 */

public class Nominatim implements Places{
	private String request = "http://open.mapquestapi.com/nominatim/v1/search?format=json";

	private static String TAG = "OTP";

	public static final String PARAM_NAME = "q";
	public static final String PARAM_LEFT = "left";
	public static final String PARAM_TOP = "top";
	public static final String PARAM_RIGHT = "right";
	public static final String PARAM_BOTTOM = "bottom";



	// JSON Node names
	private static final String TAG_PLACE_ID = "html_attributions";
	private static final String TAG_LICENSE = "licence";
	private static final String TAG_OSM_TYPE = "osm_type";
	private static final String TAG_OSM_ID = "osm_id";
	private static final String TAG_BOUNDING_BOX = "boundingbox";
	private static final String TAG_LATITUDE = "lat";
	private static final String TAG_LONGITUDE = "lon";
	private static final String TAG_NAME = "display_name";
	private static final String TAG_CLASS = "class";
	private static final String TAG_TYPE = "type";

	// contacts JSONArray
	JSONArray json = null;

	public Nominatim(){
	}

	//	http://open.mapquestapi.com/nominatim/v1/search?format=json&q=Walmart&viewbox=-82.8511308,27.6236434,-82.0559399,28.3251809&bounded=1

	public JSONArray requestPlaces(String paramName, String left, String top, String right, String bottom){
		StringBuilder builder = new StringBuilder();
		HttpClient client = new DefaultHttpClient();
		
		String encodedParamName = "";
		String encodedParamLeft= "";
		String encodedParamTop = "";
		String encodedParamRight = "";
		String encodedParamBottom = "";
		try {
			encodedParamName = URLEncoder.encode(paramName, OTPApp.URL_ENCODING);
			if ((left != null) && (top != null) && (right != null) && (bottom != null)){
				encodedParamLeft = URLEncoder.encode(left, OTPApp.URL_ENCODING);
				encodedParamTop = URLEncoder.encode(top, OTPApp.URL_ENCODING);
				encodedParamRight = URLEncoder.encode(right, OTPApp.URL_ENCODING);
				encodedParamBottom = URLEncoder.encode(bottom, OTPApp.URL_ENCODING);
			}
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return null;
		}
		
		request += "&q=" + encodedParamName;
		if ((left != null) && (top != null) && (right != null) && (bottom != null)){
			request += "&viewbox=" + encodedParamLeft
					+ "," + encodedParamTop
					+ "," + encodedParamRight
					+ "," + encodedParamBottom;
			request += "&bounded=1";
		}

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

		JSONArray json = null;
		try {
			json = new JSONArray(builder.toString());
		} catch (JSONException e) {
			Log.e(TAG, "Error parsing data " + e.toString());
		}

		return json;
	}

	public ArrayList<POI> getPlaces(HashMap<String, String> params){
		ArrayList<POI> pois = new ArrayList<POI>();

		String paramName = params.get(PARAM_NAME);
		String paramLeft = params.get(PARAM_LEFT);
		String paramTop = params.get(PARAM_TOP);
		String paramRight = params.get(PARAM_RIGHT);
		String paramBottom = params.get(PARAM_BOTTOM);


		// Get JSON
		JSONArray json = this.requestPlaces(paramName, paramLeft, paramTop, paramRight, paramBottom);
		
		if (json != null){
			// Decrypt JSON
			try {
				for(int i = 0; i < json.length(); i++){
					JSONObject r = json.getJSONObject(i);
	
					String name = r.getString(TAG_NAME);
					double lat = Double.parseDouble(r.getString(TAG_LATITUDE));
					double lon = Double.parseDouble(r.getString(TAG_LONGITUDE));
	
					POI point = new POI(name, lat, lon);
					pois.add(point);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		return pois;
	}
}
