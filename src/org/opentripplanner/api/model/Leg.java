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

import org.opentripplanner.routing.core.TraverseMode;
import org.opentripplanner.routing.patch.Alerts;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;

//import org.opentripplanner.routing.core.TraverseMode;

//import javax.xml.bind.annotation.XmlAttribute;
//import javax.xml.bind.annotation.XmlElement;
//import javax.xml.bind.annotation.XmlElementWrapper;

//import org.opentripplanner.util.model.EncodedPolylineBean;

import android.util.Log;

import com.google.gson.annotations.SerializedName;

/**
 * One leg of a trip -- that is, a temporally continuous piece of the journey that takes place on a
 * particular vehicle (or on foot).
 */

public class Leg {
	
	private static final String TAG = "OTP";
	
	public Leg() {
	}

    /**
     * The date and time this leg begins.
     */
	//TODO - fix dates
	@Element(required=false)
    public String startTime = null;
//	public Date startTime = null;
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
     * The date and time this leg ends.
     */
	@Element(required=false)
    public String endTime = null;
//	public Date endTime = null;
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
     * The distance traveled while traversing the leg in meters.
     */
	@Element(required=false)
    public Double distance = null;

    /**
     * The mode (e.g., <code>Walk</code>) used when traversing this leg.
     */
    //@XmlAttribute
	//TODO - fix mode
    //public String mode = TraverseMode.WALK.toString();
	@Attribute(required=false)
	public String mode = "";
	
    /**
     * For transit legs, the route of the bus or train being used. For non-transit legs, the name of
     * the street being traversed.
     */
    //@XmlAttribute
    @Attribute(required=false)
    public String route = "";
    
    @Attribute(required=false)
    public String agencyName;
    
    @Attribute(required=false)
    public String agencyUrl;
    
    /**
     * For transit leg, the route's (background) color (if one exists). For non-transit legs, null.
     */
    @Attribute(required=false)
    public String routeColor = null;

    /**
     * For transit leg, the route's text color (if one exists). For non-transit legs, null.
     */
    @Attribute(required=false)
    public String routeTextColor = null;

    /**
     * For transit legs, if the rider should stay on the vehicle as it changes route names.
     */
    @Attribute(required=false)
    public Boolean interlineWithPreviousLeg;

    /**
     * For transit leg, the trip's short name (if one exists). For non-transit legs, null.
     */
    //@XmlAttribute
    @Attribute(required=false)
    public String tripShortName = null;

    /**
     * For transit legs, the headsign of the bus or train being used. For non-transit legs, null.
     */
    //@XmlAttribute
    @Attribute(required=false)
    public String headsign = null;

    /**
     * For transit legs, the ID of the transit agency that operates the service used for this leg.
     * For non-transit legs, null.
     */
    //@XmlAttribute
    @Attribute(required=false)
    public String agencyId = null;
    
    /**
     * The Place where the leg originates.
     */
    @Element(required=false)
    public Place from = null;
    
    /**
     * The Place where the leg begins.
     */
    @Element(required=false)
    public Place to = null;

    /**
     * For transit legs, intermediate stops between the Place where the leg originates and the Place where the leg ends.
     * For non-transit legs, null.
     * This field is optional i.e. it is always null unless "showIntermediateStops" parameter is set to "true" in the planner request.
     */
    //@XmlElementWrapper(name = "intermediateStops")
    //@SerializedName("intermediateStops")
    @ElementList(name="intermediateStops",required=false)
    public List<Place> stops;
    
    /**
     * The leg's geometry.
     */
    @Element(required=false)
    public EncodedPolylineBean legGeometry;
    
    /**
     * A series of turn by turn instructions used for walking, biking and driving. 
     */
    //@XmlElementWrapper(name = "steps")
    //@SerializedName("steps")
    @ElementList(name="steps",required=false)
    public List<WalkStep> walkSteps;

    /**
     * Deprecated field formerly used for notes -- will be removed.  See
     * alerts
     */
    @ElementList(inline=true, required=false)
    private ArrayList<Notes> notesList;

    @ElementList(inline=true, required=false)
    private ArrayList<Alerts> alertsList;

    @Attribute(required=false)
	public String routeShortName;

    @Attribute(required=false)
	public String routeLongName;

    @Attribute(required=false)
    public String boardRule;

    @Attribute(required=false)
    public String alightRule;

    /**
     * bogus walk/bike/car legs are those that have 0.0 distance, 
     * and just one instruction
     * 
     * @return boolean true if the leg is bogus 
     */
    public boolean isBogusNonTransitLeg() {
        boolean retVal = false;
        if( (TraverseMode.WALK.toString().equals(this.mode) ||
             TraverseMode.CAR.toString().equals(this.mode) ||
             TraverseMode.BICYCLE.toString().equals(this.mode)) &&
            (this.walkSteps == null || this.walkSteps.size() <= 1) && 
            this.distance == 0) {
            retVal = true;
        }
        return retVal;
    }
    
    /** 
     * The leg's duration in milliseconds
     */
//    @Element(required=false)
//    public long getDuration() {
//        return 0;//endTime.getTime() - startTime.getTime();
//    }
    @Element
    public long duration;
    
    public void addNote(Notes note) {
    	Log.v(TAG, note.text);
        if (notesList == null) {
            notesList = new ArrayList<Notes>();
        }
        if (alertsList == null) {
            alertsList = new ArrayList<Alerts>();
        }
        
        if (!notesList.contains(note)) {
            notesList.add(note);
        }
    }

    public void addAlert(Alerts alert) {
        if (notesList == null) {
            notesList = new ArrayList<Notes>();
        }
        if (alertsList == null) {
            alertsList = new ArrayList<Alerts>();
        }
        String text = alert.alertHeaderText.getSomeTranslation();
        if (text == null) {
            text = alert.alertDescriptionText.getSomeTranslation();
        }
        if (text == null) {
            text = alert.alertUrl.getSomeTranslation();
        }
        Notes note = new Notes(text);
        if (!notesList.contains(note)) {
            notesList.add(note);
        }
        if (!alertsList.contains(alert)) {
            alertsList.add(alert);
        }
    }
    
    
    /**
     * bogus walk legs are those that have 0.0 distance, and just one instruction 
     * @return boolean true if the leg is bogus 
     */
    public boolean isBogusWalkLeg() {
        boolean retVal = false;
//        if( TraverseMode.WALK.toString().equals(this.mode)         &&
//            (this.walkSteps == null || this.walkSteps.size() <= 1) && 
//            this.distance == 0) {
//            retVal = true;
//        }
        return retVal;
    }
}
