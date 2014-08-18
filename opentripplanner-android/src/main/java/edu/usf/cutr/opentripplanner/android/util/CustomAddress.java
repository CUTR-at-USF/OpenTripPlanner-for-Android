package edu.usf.cutr.opentripplanner.android.util;

import android.location.Address;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Created by foucelhas on 18/08/14.
 */
public class CustomAddress extends Address {

    public CustomAddress(Locale locale) {
        super(locale);
    }

    public CustomAddress(Address address) {
        super(address.getLocale());
        for (int i = 0; i <= address.getMaxAddressLineIndex(); i++){
            super.setAddressLine(i, address.getAddressLine(i));
        }
		super.setFeatureName(address.getFeatureName());
		super.setAdminArea(address.getAdminArea());
		super.setSubAdminArea(address.getSubAdminArea());
		super.setLocality(address.getLocality());
		super.setSubLocality(address.getSubLocality());
		super.setThoroughfare(address.getThoroughfare());
		super.setSubThoroughfare(address.getSubThoroughfare());
		super.setPostalCode(address.getPostalCode());
		super.setCountryCode(address.getCountryCode());
		super.setCountryName(address.getCountryName());
		super.setLatitude(address.getLatitude());
		super.setLongitude(address.getLongitude());
		super.setPhone(address.getPhone());
		super.setUrl(address.getUrl());
		super.setExtras(address.getExtras());
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append(getFeatureName());
        sb.append(getAdminArea());
        sb.append(getSubAdminArea());
        sb.append(getLocality());
        sb.append(getThoroughfare());
        return sb.toString();
    }
}
