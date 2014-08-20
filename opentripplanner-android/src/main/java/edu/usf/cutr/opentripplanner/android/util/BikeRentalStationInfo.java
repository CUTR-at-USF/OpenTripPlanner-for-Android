package edu.usf.cutr.opentripplanner.android.util;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by foucelhas on 15/07/14.
 */
public class BikeRentalStationInfo {

    LatLng location;

    String name;

    public BikeRentalStationInfo(LatLng location, String name) {
        this.location = location;
        this.name = name;
    }

    public LatLng getLocation() {
        return location;
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
