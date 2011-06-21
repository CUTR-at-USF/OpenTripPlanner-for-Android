package org.opentripplanner.api.ws;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

public class GraphMetadata {

	/**
     * The bounding box of the graph, in decimal degrees.
     */
	@Element
    private double minLatitude, minLongitude, maxLatitude, maxLongitude;

    @Root
    public GraphMetadata() {
    }

	public double getMinLatitude() {
		return minLatitude;
	}

	public void setMinLatitude(double minLatitude) {
		this.minLatitude = minLatitude;
	}

	public double getMinLongitude() {
		return minLongitude;
	}

	public void setMinLongitude(double minLongitude) {
		this.minLongitude = minLongitude;
	}

	public double getMaxLatitude() {
		return maxLatitude;
	}

	public void setMaxLatitude(double maxLatitude) {
		this.maxLatitude = maxLatitude;
	}

	public double getMaxLongitude() {
		return maxLongitude;
	}

	public void setMaxLongitude(double maxLongitude) {
		this.maxLongitude = maxLongitude;
	}
}
