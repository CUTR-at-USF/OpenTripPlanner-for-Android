package edu.usf.cutr.opentripplanner.android.listeners;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

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