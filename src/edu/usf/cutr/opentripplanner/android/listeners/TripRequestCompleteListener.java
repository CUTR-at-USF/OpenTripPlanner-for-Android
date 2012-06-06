package edu.usf.cutr.opentripplanner.android.listeners;

import java.util.List;

import org.opentripplanner.api.model.Itinerary;

public interface TripRequestCompleteListener {
	public void onTripRequestComplete(List<Itinerary> itineraries, String currentRequestString);
}
