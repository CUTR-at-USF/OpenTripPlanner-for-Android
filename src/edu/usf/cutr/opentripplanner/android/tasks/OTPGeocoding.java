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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources.NotFoundException;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import edu.usf.cutr.opentripplanner.android.OTPApp;
import edu.usf.cutr.opentripplanner.android.R;
import edu.usf.cutr.opentripplanner.android.listeners.OTPGeocodingListener;
import edu.usf.cutr.opentripplanner.android.model.Server;
import edu.usf.cutr.opentripplanner.android.pois.GooglePlaces;
import edu.usf.cutr.opentripplanner.android.pois.Nominatim;
import edu.usf.cutr.opentripplanner.android.pois.POI;
import edu.usf.cutr.opentripplanner.android.pois.Places;

/**
 * @author Khoa Tran
 *
 */

public class OTPGeocoding extends AsyncTask<String, Integer, Long> {
	private static final String TAG = "OTP";
	private ProgressDialog progressDialog;
	private WeakReference<Activity> activity;
	private Context context;
	private boolean isStartTextbox;
	private OTPGeocodingListener callback;
	private String placesService;

	private ArrayList<Address> addressesReturn = new ArrayList<Address>();

	private Server selectedServer;

	public OTPGeocoding(WeakReference<Activity> activity, Context context, boolean isStartTextbox, Server selectedServer, String placesService, OTPGeocodingListener callback) {
		this.context = context;
		this.activity = activity;
		this.isStartTextbox = isStartTextbox;
		this.callback = callback;
		this.selectedServer = selectedServer;
		this.placesService = placesService;
		if (activity.get() != null){
			progressDialog = new ProgressDialog(activity.get());
		}
	}

	protected void onPreExecute() {
		if (activity.get() != null){
			progressDialog.setIndeterminate(true);
	        progressDialog.setCancelable(true);
			progressDialog = ProgressDialog.show(activity.get(), "",
					"Processing geocoding. Please wait... ", true);
		}
	}

	protected Long doInBackground(String... reqs) {
		long count = reqs.length;

		String address = reqs[0];
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);


		if(address==null || address.equalsIgnoreCase("")) {
			return count;
		}

		if(address.equalsIgnoreCase(context.getString(R.string.my_location))) {
			String currentLat = reqs[1];
			String currentLng = reqs[2];
			LatLng latLng = new LatLng(Double.parseDouble(currentLat), Double.parseDouble(currentLng));
			
			Address addressReturn = new Address(Locale.US);
			addressReturn.setLatitude(latLng.latitude);
			addressReturn.setLongitude(latLng.longitude);
			addressReturn.setAddressLine(addressReturn.getMaxAddressLineIndex()+1, context.getString(R.string.my_location));

			addressesReturn.add(addressReturn);

			return count;
		}
		
		ArrayList<Address> addresses = null;
		
		if (prefs.getBoolean(OTPApp.PREFERENCE_KEY_USE_ANDROID_GEOCODER, true)){
			Geocoder gc = new Geocoder(context);
			try {
				if (selectedServer != null){
					addresses = (ArrayList<Address>)gc.getFromLocationName(address, 
							context.getResources().getInteger(R.integer.geocoder_max_results), 
							selectedServer.getLowerLeftLatitude(), 
							selectedServer.getLowerLeftLongitude(), 
							selectedServer.getUpperRightLatitude(), 
							selectedServer.getUpperRightLongitude());
				}
				else{
					addresses = (ArrayList<Address>)gc.getFromLocationName(address, 
							context.getResources().getInteger(R.integer.geocoder_max_results));
				}
				for(int i=0; i<addresses.size(); i++){
					Address addr = addresses.get(i);
					String addressLine = "";
					addressLine += addr.getAddressLine(0)!=null ? addr.getAddressLine(0) : "no-name";
					addressLine += addr.getAddressLine(1)!=null ? "\n" + addr.getAddressLine(1) : "";
					addressLine += addr.getAddressLine(2)!=null ? ", " + addr.getAddressLine(2) : "";
					addr.setAddressLine(addr.getMaxAddressLineIndex()+1, addressLine);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		if ((addresses == null) || addresses.isEmpty()){
			addresses = searchPlaces(address);
		}

		addressesReturn.addAll(addresses);

		return count;
	}

	/**
	 * Try to grab the developer key from an unversioned resource file, if it exists
	 * @return the developer key from an unversioned resource file, or empty string if it doesn't exist
	 */
	private String getKeyFromResource(){
		String strKey = new String("");

		try {
			InputStream in = context.getResources().openRawResource(R.raw.googleplaceskey);
			BufferedReader r = new BufferedReader(new InputStreamReader(in));
			StringBuilder total = new StringBuilder();

			while ((strKey = r.readLine()) != null) {
				total.append(strKey);
			}

			strKey = total.toString();

			strKey.trim(); //Remove any whitespace

		} catch (NotFoundException e) {
			Log.w(TAG, "Warning - didn't find the google places key file:" + e);
		} catch (IOException e) {
			Log.w(TAG, "Error reading the developer key file:" + e);
		}

		return strKey;
	}

	private ArrayList<Address> searchPlaces(String name){
		HashMap<String, String> params = new HashMap<String, String>();
		Places p;

		if(placesService.equals(context.getResources().getString(R.string.geocoder_google_places))){
			params.put(GooglePlaces.PARAM_NAME, name);
			if (selectedServer != null){
				params.put(GooglePlaces.PARAM_LOCATION, Double.toString(selectedServer.getCenterLatitude()) + "," + Double.toString(selectedServer.getCenterLongitude()));
				params.put(GooglePlaces.PARAM_RADIUS, Double.toString(selectedServer.getRadius()));
			}
			p = new GooglePlaces(getKeyFromResource());

			Log.v(TAG, "Using Google Places!");
		} else {
			params.put(Nominatim.PARAM_NAME, name);
			if (selectedServer != null){
				params.put(Nominatim.PARAM_LEFT, Double.toString(selectedServer.getLowerLeftLongitude()));
				params.put(Nominatim.PARAM_TOP, Double.toString(selectedServer.getLowerLeftLatitude()));
				params.put(Nominatim.PARAM_RIGHT, Double.toString(selectedServer.getUpperRightLongitude()));
				params.put(Nominatim.PARAM_BOTTOM, Double.toString(selectedServer.getUpperRightLatitude()));
			}

			p = new Nominatim();

			Log.v(TAG, "Using Nominatim!");
		}

		ArrayList<POI> pois = new ArrayList<POI>();
		pois.addAll(p.getPlaces(params));

		ArrayList<Address> addresses = new ArrayList<Address>();

		for(int i=0; i<pois.size(); i++){
			POI poi = pois.get(i);
			Log.v(TAG, poi.getName() + " " + poi.getLatitude() + "," + poi.getLongitude());
			Address addr = new Address(Locale.US);
			addr.setLatitude(poi.getLatitude());
			addr.setLongitude(poi.getLongitude());
			String addressLine = poi.getAddress()==null ? poi.getName() : (poi.getName() + ", " + poi.getAddress());
			addr.setAddressLine(addr.getMaxAddressLineIndex()+1,addressLine); 
			addresses.add(addr);
		}

		return addresses;
	}
	
	protected void  onCancelled(Long result){
		if (activity.get() != null){
			try{		
				if (progressDialog != null && progressDialog.isShowing()) {
					progressDialog.dismiss();
				}
			}catch(Exception e){
				Log.e(TAG, "Error in Geocoding Cancelled dismissing dialog: " + e);
			}
		}
		if (activity.get() != null){
			AlertDialog.Builder geocoderAlert = new AlertDialog.Builder(activity.get());
			geocoderAlert.setTitle(R.string.geocoder_results_title)
					.setMessage(R.string.geocoder_no_results_message)
					.setCancelable(false)
					.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
						}
					});

			AlertDialog alert = geocoderAlert.create();
			alert.show();
		}

				
		Log.e(TAG, "No geocoding processed!");
	}

	protected void onPostExecute(Long result) {
		if (activity.get() != null){
			try{		
				if (progressDialog != null && progressDialog.isShowing()) {
					progressDialog.dismiss();
				}
			}catch(Exception e){
				Log.e(TAG, "Error in Geocoding PostExecute dismissing dialog: " + e);
			}
		}
		
		callback.onOTPGeocodingComplete(isStartTextbox, addressesReturn);
	}
}