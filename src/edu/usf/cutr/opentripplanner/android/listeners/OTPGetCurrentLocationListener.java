package edu.usf.cutr.opentripplanner.android.listeners;

import org.osmdroid.util.GeoPoint;

public interface OTPGetCurrentLocationListener {
	public void onOTPGetCurrentLocationComplete(GeoPoint point);
}
