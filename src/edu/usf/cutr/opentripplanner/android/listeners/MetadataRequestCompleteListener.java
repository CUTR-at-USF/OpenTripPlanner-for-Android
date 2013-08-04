package edu.usf.cutr.opentripplanner.android.listeners;

import org.opentripplanner.api.ws.GraphMetadata;

public interface MetadataRequestCompleteListener {
	
	public void onMetadataRequestComplete(GraphMetadata metadata);

}
