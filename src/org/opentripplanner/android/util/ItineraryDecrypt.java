package org.opentripplanner.android.util;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.opentripplanner.android.R;
import org.opentripplanner.android.model.Direction;
import org.opentripplanner.api.model.AbsoluteDirection;
import org.opentripplanner.api.model.AgencyAndId;
import org.opentripplanner.api.model.Leg;
import org.opentripplanner.api.model.Place;
import org.opentripplanner.api.model.RelativeDirection;
import org.opentripplanner.api.model.WalkStep;
import org.opentripplanner.routing.core.TraverseMode;
import org.opentripplanner.routing.patch.Alerts;

public class ItineraryDecrypt {
	private List<Leg> legs = new ArrayList<Leg>();
	
	private ArrayList<Direction> directions = new ArrayList<Direction>();
	
	private double totalDistance = 0;
	
	private double totalTimeTraveled = 0;
	
	public ItineraryDecrypt(List<Leg> legs){
		this.legs.addAll(legs);
		
		convertToDirectionList();
	}

	/**
	 * @return the directions
	 */
	public ArrayList<Direction> getDirections() {
		return directions;
	}

	/**
	 * @param directions the directions to set
	 */
	public void setDirections(ArrayList<Direction> directions) {
		this.directions = directions;
	}
	
	public void addDirection(Direction dir){
		if(directions==null){
			directions = new ArrayList<Direction>();
		}
		
		directions.add(dir);
	}
	
	private void convertToDirectionList(){
		for(Leg leg: legs){
			setTotalDistance(getTotalDistance() + leg.distance);
			Direction dir;
			
			TraverseMode traverseMode = TraverseMode.valueOf((String) leg.mode);
			if(traverseMode.isOnStreetNonTransit()){
				dir = decryptNonTransit(leg);
			} else{
				dir = decryptTransit(leg);
			}
			
			if(dir == null){
				continue;
			}
			
			addDirection(dir);
//			directionText+=leg.mode+"\n";
		}
	}
	
	private Direction decryptNonTransit(Leg leg){
		Direction direction = new Direction();
		
//		http://opentripplanner.usf.edu/opentripplanner-api-webapp/ws/plan?optimize=QUICK&time=09:24pm&arriveBy=false&wheelchair=false&maxWalkDistance=7600.0&fromPlace=28.033389%2C+-82.521034&toPlace=28.064709%2C+-82.471618&date=03/07/12&mode=WALK,TRAM,SUBWAY,RAIL,BUS,FERRY,CABLE_CAR,GONDOLA,FUNICULAR,TRANSIT,TRAINISH,BUSISH
		
		// Get appropriate action and icon
		String action = null;
		TraverseMode mode = TraverseMode.valueOf((String) leg.mode);
		if(mode.compareTo(TraverseMode.WALK)==0){
			action = "Walk";
			direction.setIcon(R.drawable.mode_walk);
		} else if(mode.compareTo(TraverseMode.BICYCLE)==0){
			action = "Bike";
			direction.setIcon(R.drawable.mode_bicycle);
		} else if(mode.compareTo(TraverseMode.CAR)==0){
			action = "Drive";
			direction.setIcon(R.drawable.icon);
		}
		
//		Main direction
		Place fromPlace = leg.from;
		Place toPlace = leg.to;
		String mainDirectionText = action;
		mainDirectionText += fromPlace.name==null ? "" : " from " + fromPlace.name;
		mainDirectionText += toPlace.name==null ? "" : " to " + toPlace.name;
		mainDirectionText += toPlace.stopId==null ? "" : " (" + toPlace.stopId.getAgencyId() + " " + toPlace.stopId.getId() + ")";
		
		direction.setDirectionText(mainDirectionText);
		
//		Sub-direction
		List<WalkStep> walkSteps = leg.walkSteps;
		
		if(walkSteps==null) return direction;
		
		ArrayList<Direction> subDirections = new ArrayList<Direction>(walkSteps.size());
		
		for(WalkStep step: walkSteps){
			Direction dir = new Direction();
			String subDirectionText = "";
			
			double distance = step.distance;
			RelativeDirection relativeDir = step.relativeDirection;
			String streetName = step.streetName;
			AbsoluteDirection absoluteDir = step.absoluteDirection;
			String exit = step.exit;
			boolean isStayOn = (step.stayOn==null ? false : step.stayOn);
			boolean isBogusName = (step.bogusName==null ? false : step.bogusName);
			double lon = step.lon;
			double lat = step.lat;
			String elevation = step.elevation;
			List<Alerts> alert = step.alerts;
			
			// Walk East
			if(relativeDir==null){
				subDirectionText += action + " ";
				subDirectionText += absoluteDir.name() + " ";
			}
			// (Turn left)/(Continue) 
			else {
				if(!isStayOn) {
					subDirectionText += "Turn " + relativeDir.name() + " ";
				} else {
					subDirectionText += relativeDir.name() + " ";
				}
			}
			
			// (on ABC)
			if(!isBogusName) {
				subDirectionText += "on "+ streetName + " ";
			}
			
			// Distance traveled [distance]
			DecimalFormat twoDForm = new DecimalFormat("#.##");
	        distance = Double.valueOf(twoDForm.format(distance));
	        dir.setDistanceTraveled(distance);
			
			// Add new sub-direction
			subDirections.add(dir);
		}
		
		direction.setNonTransitSubDirection(subDirections);
		
		return direction;
	}
	
	private Direction decryptTransit(Leg leg){
		Direction direction = new Direction();
		
//		set icon
		TraverseMode mode = TraverseMode.valueOf((String) leg.mode);
		if(mode.compareTo(TraverseMode.BUS)==0){
			direction.setIcon(R.drawable.mode_bus);
		} else if(mode.compareTo(TraverseMode.RAIL)==0){
			direction.setIcon(R.drawable.mode_rail);
		} else if(mode.compareTo(TraverseMode.FERRY)==0){
			direction.setIcon(R.drawable.mode_ferry);
		} else if(mode.compareTo(TraverseMode.GONDOLA)==0){
			direction.setIcon(R.drawable.mode_gondola);
		} else if(mode.compareTo(TraverseMode.SUBWAY)==0){
			direction.setIcon(R.drawable.mode_subway);
		} else if(mode.compareTo(TraverseMode.TRAM)==0){
			direction.setIcon(R.drawable.mode_tram);
		} else {
			direction.setIcon(R.drawable.icon);
		}
		
//		set direction text
		String mainDirectionText = "";
		
		String route = leg.route;
		String agencyName = leg.agencyName;
		String agencyUrl = leg.agencyId;
		String routeColor = leg.routeColor;
		String routeTextColor = leg.routeTextColor;
		boolean isInterlineWithPreviousLeg = (leg.interlineWithPreviousLeg==null?false:leg.interlineWithPreviousLeg);
		String tripShortName = leg.tripShortName;
		String headsign = leg.headsign;
		String agencyId = leg.agencyId;
		String routeShortName = leg.routeShortName;
		String routeLongName = leg.routeLongName;
		String boardRule = leg.boardRule;
		String alignRule = leg.alightRule;
		
		double distance = leg.distance;
		Place from = leg.from;
		AgencyAndId agencyAndIdFrom = from.stopId;
		Place to = leg.to;
		AgencyAndId agencyAndIdTo = to.stopId;
		long duration = leg.duration;
		
		// Get on HART BUS 6
		String serviceName = agencyName;
		if(serviceName==null)
			serviceName = agencyId;

		mainDirectionText += "At " + from.name + " (" + agencyAndIdFrom.getAgencyId() + " " + agencyAndIdFrom.getId() + "), ";
		mainDirectionText += "Get on " + serviceName + " " + mode + " " + route + "\n\n";

		mainDirectionText += "At " + to.name + " (" + agencyAndIdTo.getAgencyId() + " " + agencyAndIdTo.getId() + "), ";
		mainDirectionText += "Get off " + serviceName + " " + mode + " " + route;
		
		direction.setDirectionText(mainDirectionText);

		// Distance traveled [distance]
		DecimalFormat twoDForm = new DecimalFormat("#.##");
		distance = Double.valueOf(twoDForm.format(distance));
		direction.setDistanceTraveled(distance);
		
		return direction;
	}

	/**
	 * @return the totalDistance
	 */
	public double getTotalDistance() {
		DecimalFormat twoDForm = new DecimalFormat("#.##");
		totalDistance = Double.valueOf(twoDForm.format(totalDistance));
		return totalDistance;
	}

	/**
	 * @param totalDistance the totalDistance to set
	 */
	public void setTotalDistance(double totalDistance) {
		this.totalDistance = totalDistance;
	}

	/**
	 * @return the totalTimeTraveled
	 */
	public double getTotalTimeTraveled() {
		if(legs.isEmpty()) return 0;
		
		Leg legStart = legs.get(0);
		String startTimeText = legStart.startTime;
		Leg legEnd = legs.get(legs.size()-1);
		String endTimeText = legEnd.endTime;
		DateFormat formatter ; 
		Date startTime=null, endTime=null; 
//		2012-03-09T22:46:00-05:00
		formatter = new SimpleDateFormat("yyyy-MM-dd\'T\'HH:mm:ssZZ");
		try {
			startTime = (Date)formatter.parse(startTimeText);
			endTime = (Date)formatter.parse(endTimeText);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		totalTimeTraveled = 0;
		if (startTime!=null && endTime!=null) {
			totalTimeTraveled = (startTime.getTime() - endTime.getTime()) / 1000;
		}
		
		return totalTimeTraveled;
	}
	
}
