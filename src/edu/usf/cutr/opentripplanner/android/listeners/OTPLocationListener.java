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

package edu.usf.cutr.opentripplanner.android.listeners;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

/**
 * 
 * @author Khoa Tran
 *
 */

public class OTPLocationListener implements LocationListener {
	
	private static double currentLat=0.0, currentLon=0.0;

    @Override
    public void onLocationChanged(Location location) {

        int lat = (int) location.getLatitude(); // * 1E6);
        int log = (int) location.getLongitude(); // * 1E6);
        int acc = (int) (location.getAccuracy());

        String info = location.getProvider();
        try {

            setCurrentLat(location.getLatitude());
            setCurrentLon(location.getLongitude());

        } catch (Exception e) {
        }

    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.i("OnProviderDisabled", "OnProviderDisabled");
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.i("onProviderEnabled", "onProviderEnabled");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.i("onStatusChanged", "onStatusChanged");
    }

	/**
	 * @return the currentLat
	 */
	public double getCurrentLat() {
		return currentLat;
	}

	/**
	 * @param currentLat the currentLat to set
	 */
	public void setCurrentLat(double cLat) {
		currentLat = cLat;
	}

	/**
	 * @return the currentLon
	 */
	public double getCurrentLon() {
		return currentLon;
	}

	/**
	 * @param currentLon the currentLon to set
	 */
	public void setCurrentLon(double cLon) {
		currentLon = cLon;
	}

}