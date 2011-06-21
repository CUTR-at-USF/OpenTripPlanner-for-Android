/* This program is free software: you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public License
 as published by the Free Software Foundation, either version 3 of
 the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>. */

package org.opentripplanner.api.model; 

import java.util.logging.Logger; 

import org.opentripplanner.util.Constants;
import org.simpleframework.xml.Element;

//import org.onebusaway.gtfs.model.AgencyAndId;
//import org.opentripplanner.util.Constants; 

/** 
* A Place is where a journey starts or ends, or a transit stop along the way.
*/ 
public class Place {
    protected static final Logger LOGGER = Logger.getLogger(Place.class.getCanonicalName());

    /** 
     * For transit stops, the name of the stop.  For points of interest, the name of the POI.
     */
    @Element(required=false)
    public String name = null;

    /** 
     * The ID of the stop.  Depending on the transit agency, this may or may not be something that
     * users care about.
     */
    @Element(required=false)
    public AgencyAndId stopId = null;

    /**
     * The longitude of the place.
     */
    @Element(required=false)
    public Double lon = null;
    
    /**
     * The latitude of the place.
     */
    @Element(required=false)
    public Double lat = null;

    /**
     * Returns the geometry in GeoJSON format
     * @return
     */
    //@XmlElement
    String getGeometry() {

        return Constants.GEO_JSON_POINT + lon + "," + lat + Constants.GEO_JSON_TAIL;
    }
    
    //TODO remove or fix!
    @Element(required=false)
    public String geometry = "";

    public Place() {
    }

    public Place(Double lon, Double lat, String name) {
        this.lon = lon;
        this.lat = lat;
        this.name = name;
    }

    public Place(Double lon, Double lat, String name, AgencyAndId stopId) {
        this(lon, lat, name);
        this.stopId = stopId;
    }

}
