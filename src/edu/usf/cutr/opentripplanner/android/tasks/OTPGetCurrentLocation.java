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

import java.util.ArrayList;
import java.util.List;

import org.osmdroid.util.GeoPoint;

import edu.usf.cutr.opentripplanner.android.MyActivity;
import edu.usf.cutr.opentripplanner.android.listeners.OTPGetCurrentLocationListener;
import edu.usf.cutr.opentripplanner.android.listeners.OTPLocationListener;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

/**
 * 
 * @author Khoa Tran
 *
 */

public class OTPGetCurrentLocation extends AsyncTask<String, Integer, String> {
    private ProgressDialog progressDialog = null;
//    private MyActivity activity;
    private Context context;

    public double currentLat = 0.0;
    public double currentLon = 0.0;

    public LocationManager lm;
    public ArrayList<OTPLocationListener> otpLocationListenerList;
    
    private List<String> providers = new ArrayList<String>();
    
    private OTPGetCurrentLocationListener callback;
    
    public OTPGetCurrentLocation(Context context, OTPGetCurrentLocationListener callback) {
		this.context = context;
		this.callback = callback;
	}

    @Override
    protected void onPreExecute() {
        otpLocationListenerList = new ArrayList<OTPLocationListener>();
        
        lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Loading...");
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(true);
        progressDialog.show();
        
		providers.addAll(lm.getProviders(true));

		for (int i=0; i<providers.size(); i++) {
			OTPLocationListener otpLocationListener = new OTPLocationListener();
			lm.requestLocationUpdates(providers.get(i), 
									  0, 
									  0,
									  otpLocationListener);
			otpLocationListenerList.add(otpLocationListener);
		}

    }

    protected void onPostExecute(String result) {
        progressDialog.dismiss();
        
        for (int i=0; i<providers.size(); i++) {
			otpLocationListenerList.set(i, null);
		}
        
        GeoPoint gp = new GeoPoint((int)(currentLat*1E6), (int)(currentLon*1E6));
        
        callback.onOTPGetCurrentLocationComplete(gp);
    }

    @Override
    protected String doInBackground(String... params) {
        long startMillis = SystemClock.currentThreadTimeMillis();
		long deltaMillis = startMillis;
        while (currentLat == 0.0 && deltaMillis < 5000) {
        	deltaMillis = SystemClock.currentThreadTimeMillis() - startMillis;
        	for(int i=0; i<providers.size(); i++){
        		currentLat = otpLocationListenerList.get(i).getCurrentLat();
        		currentLon = otpLocationListenerList.get(i).getCurrentLon();
        		if(currentLat!=0.0)
        			return null;
        	}
        }
        return null;
    }
}