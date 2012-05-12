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

package org.opentripplanner.android.pois;

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
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

/**
 * @author Khoa Tran
 *
 */

public class Nominatim implements Places{
	private String request = "http://open.mapquestapi.com/nominatim/v1/search?format=json";

	private static String TAG = "OTP";

	private double left, top, right, bottom;

	public static final String PARAM_NAME = "q";

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

	public Nominatim(double left, double top, double right, double bottom){
		this.left = left;
		this.top = top;
		this.right = right;
		this.bottom = bottom;
	}

	//	http://open.mapquestapi.com/nominatim/v1/search?format=json&q=Walmart&viewbox=-82.8511308,27.6236434,-82.0559399,28.3251809&bounded=1

	public JSONArray requestPlaces(String paramName){
		StringBuilder builder = new StringBuilder();
		HttpClient client = new DefaultHttpClient();
		
		String encodedParamName = "";
		try {
			encodedParamName = URLEncoder.encode(paramName, "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return null;
		}
		
		request += "&q=" + encodedParamName;
		request += "&viewbox=" + Double.toString(left)
				+ "," + Double.toString(top)
				+ "," + Double.toString(right)
				+ "," + Double.toString(bottom);
		request += "&bounded=1";

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

		// Get JSON
		JSONArray json = this.requestPlaces(paramName);

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

		return pois;
	}
}
