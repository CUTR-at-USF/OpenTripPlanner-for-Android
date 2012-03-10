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
import java.util.Iterator;
import java.util.List;

import org.opentripplanner.routing.core.Fare;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;

import com.google.gson.annotations.SerializedName;

//import javax.xml.bind.annotation.XmlElement;
//import javax.xml.bind.annotation.XmlElementWrapper;

//import org.opentripplanner.routing.core.Fare;

/**
 * An Itinerary is one complete way of getting from the start location to the end location.
 */
public class Itinerary {
	
	public Itinerary(){
		
	}

    /**
     * Duration of the trip on this itinerary, in milliseconds.
     */
	@Element
    public long duration = 0;

    /**
     * Time that the trip departs.
     */
	//TODO - fix date parsing
	@Element(required=false)
	public String startTime = null;
//    public Date startTime = null;
//    @Element(required=false)
//    public void setStartTime(Date entry){
//    	SimpleDateFormat parser = 
//    			new SimpleDateFormat("yyyy-MM-d'T'HH:mm:ssZ");
//    	try {
//    		startTime = parser.parse(entry.toString());
//		} catch (ParseException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}  
//    }
//    @Element(required=false)
//    public Date getStartTime(){
//    	return startTime;
//    }
    /**
     * Time that the trip arrives.
     */
	@Element(required=false)
    public String endTime = null;
//    public Date endTime = null;
//    @Element(required=false)
//    public void setEndTime(Date entry){
//    	SimpleDateFormat parser = 
//    			new SimpleDateFormat("yyyy-MM-d'T'HH:mm:ssZ");
//    	try {
//    		endTime = parser.parse(entry.toString());
//		} catch (ParseException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}  
//    }
//    @Element(required=false)
//    public Date getEndTime(){
//    	return endTime;
//    }

    /**
     * How much time is spent walking, in milliseconds.
     */
	@Element
    public long walkTime = 0;
    /**
     * How much time is spent on transit, in milliseconds.
     */
	@Element
    public long transitTime = 0;
    /**
     * How much time is spent waiting for transit to arrive, in milliseconds.
     */
	@Element
    public long waitingTime = 0;

    /**
     * How far the user has to walk, in meters.
     */
	@Element(required=false)
    public Double walkDistance = 0.0;

    /**
     * How much elevation is lost, in total, over the course of the trip, in meters. As an example,
     * a trip that went from the top of Mount Everest straight down to sea level, then back up K2,
     * then back down again would have an elevationLost of Everest + K2.
     */
	@Element(required=false)
    public Double elevationLost = 0.0;
    /**
     * How much elevation is gained, in total, over the course of the trip, in meters. See
     * elevationLost.
     */
	@Element(required=false)
    public Double elevationGained = 0.0;

    /**
     * The number of transfers this trip has.
     */
	@Element(required=false)
    public Integer transfers = 0;

    /**
     * The cost of this trip
     */
	@Element(required=false)
    public Fare fare = new Fare();

    /**
     * A list of Legs. Each Leg is either a walking (cycling, car) portion of the trip, or a transit
     * trip on a particular vehicle. So a trip where the use walks to the Q train, transfers to the
     * 6, then walks to their destination, has four legs.
     */
    //@XmlElementWrapper(name = "legs")
    //@XmlElement(name = "leg")
    //@SerializedName("legs")
    @ElementList(name="legs",required=false)
    public List<Leg> legs = new ArrayList<Leg>();

    /**
     * This itinerary has a greater slope than the user requested (but there are no possible 
     * itineraries with a good slope). 
     */
    @Element
    public boolean tooSloped = false;

    /** 
     * adds leg to array list
     * @param leg
     */
    public void addLeg(Leg leg) {
        if(leg != null)
            legs.add(leg);
    }

    /** 
     * remove the leg from the list of legs 
     * @param leg object to be removed
     */
    public void removeLeg(Leg leg) {
        if(leg != null) {
            legs.remove(leg);
        }
    }
    
    public void removeBogusLegs() {
        Iterator<Leg> it = legs.iterator();
        while (it.hasNext()) {
            Leg leg = it.next();
            if (leg.isBogusWalkLeg()) {
                it.remove();
            }
        }
    }
}
