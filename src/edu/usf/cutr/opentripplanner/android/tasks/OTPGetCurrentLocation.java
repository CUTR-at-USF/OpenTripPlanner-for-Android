package edu.usf.cutr.opentripplanner.android.tasks;

import java.util.ArrayList;
import java.util.List;

import org.osmdroid.util.GeoPoint;

import edu.usf.cutr.opentripplanner.android.MyActivity;
import edu.usf.cutr.opentripplanner.android.listeners.OTPLocationListener;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class OTPGetCurrentLocation extends AsyncTask<String, Integer, String> {
    private ProgressDialog pDialog = null;
    private MyActivity activity;

    public double currentLat = 0.0;
    public double currentLon = 0.0;

    public LocationManager lm;
    public ArrayList<OTPLocationListener> otpLocationListenerList;
    
    private List<String> providers = new ArrayList<String>();
    
    public OTPGetCurrentLocation(MyActivity activity) {
		this.activity = activity;
	}

    @Override
    protected void onPreExecute() {
        otpLocationListenerList = new ArrayList<OTPLocationListener>();
        
        lm = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);

        pDialog = new ProgressDialog(activity);
        pDialog.setMessage("Loading...");
        pDialog.setIndeterminate(true);
        pDialog.setCancelable(true);
        pDialog.show();
        
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
        pDialog.dismiss();

//        Toast.makeText(activity,
//                "LATITUDE :" + currentLat + " LONGITUDE :" + currentLon,
//                Toast.LENGTH_LONG).show();
//        
        GeoPoint gp = new GeoPoint((int)(currentLat*1E6), (int)(currentLon*1E6));
//        activity.set.setScreenCenterTo(gp);
        
        for (int i=0; i<providers.size(); i++) {
			OTPLocationListener otpLocationListener = otpLocationListenerList.get(i);
			otpLocationListener = null;
		}
        
//        activity.setLookingForCurrentLocation(false, gp);
    }

    @Override
    protected String doInBackground(String... params) {
        // TODO Auto-generated method stub

        while (currentLat == 0.0) {
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