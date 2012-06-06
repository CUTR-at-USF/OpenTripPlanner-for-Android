package edu.usf.cutr.opentripplanner.android.listeners;

import org.osmdroid.util.GeoPoint;

import edu.usf.cutr.opentripplanner.android.model.Server;

public interface ServerSelectorCompleteListener {
	public void onServerSelectorComplete(GeoPoint point, Server server);
}
