package edu.usf.cutr.opentripplanner.android.listeners;

import java.util.ArrayList;

import android.location.Address;

public interface OTPGeocodingListener {
	public void onOTPGeocodingComplete(boolean isStartTextbox, ArrayList<Address> addressesReturn);
}
