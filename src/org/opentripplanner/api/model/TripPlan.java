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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.opentripplanner.util.DateConstants;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;

import android.util.Log;

import com.google.gson.annotations.SerializedName;

//import javax.xml.bind.annotation.XmlElementWrapper;

/**
 * A TripPlan is a set of ways to get from point A to point B at time T.
 */
public class TripPlan {
	
	private static final String TAG = "OTP";
	
    /** 
     * The time and date of travel 
     */
	//TODO - fix datedeserializer
//	@Element
//    public String date = null;
	public String date;
	public Date formatted_date;
    @Element
    public void setDate(String d){
    	SimpleDateFormat parser = 
    			new SimpleDateFormat(DateConstants.ISO_DATETIME_TIME_ZONE_FORMAT);
    	Log.v(TAG, "setDate TripPlan");
    	try {
    		Log.v(TAG, d);
    		formatted_date = parser.parse(d);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
    }
    @Element
    public String getDate(){
    	Log.v(TAG, "getDate TripPlan");
    	return date;
    }
    /**
     * The origin
     */
	@Element
    public Place from = null;
    /**
     * The destination
     */
	@Element
    public Place to = null;

    /** 
     * A list of possible itineraries. 
     */
    //@XmlElementWrapper(name="itineraries")
    //@SerializedName("itineraries")
    @ElementList(name="itineraries")
    public List<Itinerary> itinerary = new ArrayList<Itinerary>();

    public TripPlan() {}
    
    
    public void addItinerary(Itinerary itinerary) {
        this.itinerary.add(itinerary);
    }
}
