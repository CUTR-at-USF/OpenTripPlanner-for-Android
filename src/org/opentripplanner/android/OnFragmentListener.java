package org.opentripplanner.android;

import java.util.List;

import org.opentripplanner.android.model.OTPBundle;
import org.opentripplanner.api.model.Leg;

public interface OnFragmentListener {
	public void onItinerarySelected(List<Leg> l);
	
	public List<Leg> getCurrentItinerary();
	
	public void onDirectionFragmentSwitched();
	
	public void onMainFragmentSwitched();
	
	public OTPBundle getOTPBundle();
	
	public void setOTPBundle(OTPBundle b);
	
}
