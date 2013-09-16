/*
 * Copyright 2012 University of South Florida
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

package edu.usf.cutr.opentripplanner.android.util;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import edu.usf.cutr.opentripplanner.android.OTPApp;
import edu.usf.cutr.opentripplanner.android.R;

import org.opentripplanner.api.model.AgencyAndId;
import org.opentripplanner.api.model.RelativeDirection;
import org.opentripplanner.routing.core.TraverseMode;
import org.opentripplanner.routing.patch.Alerts;
import org.opentripplanner.v092snapshot.api.model.AbsoluteDirection;
import org.opentripplanner.v092snapshot.api.model.Elevation;
import org.opentripplanner.v092snapshot.api.model.Leg;
import org.opentripplanner.v092snapshot.api.model.Place;
import org.opentripplanner.v092snapshot.api.model.WalkStep;

import android.content.Context;
import android.text.format.DateFormat;

import edu.usf.cutr.opentripplanner.android.model.Direction;

/**
 * @author Khoa Tran
 *
 */

public class ItineraryDecrypt {
	private List<Leg> legs = new ArrayList<Leg>();

	private ArrayList<Direction> directions = new ArrayList<Direction>();

	private double totalDistance = 0;

	private double totalTimeTraveled = 0;
	
	private Context applicationContext;
	
	int agencyTimeZoneOffset = 0;

	public ItineraryDecrypt(List<Leg> legs, Context applicationContext){
		this.legs.addAll(legs);
		this.applicationContext = applicationContext;
		
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

			TraverseMode traverseMode = TraverseMode.valueOf((String) leg.mode);
			if(traverseMode.isOnStreetNonTransit()){
				Direction dir = decryptNonTransit(leg);
				if(dir == null){
					continue;
				}
				addDirection(dir);
			} else{
				ArrayList<Direction> directions = decryptTransit(leg);
				if(directions == null){
					continue;
				}

				if(directions.get(0)!=null){
					addDirection(directions.get(0));
				}

				if(directions.get(1)!=null){
					addDirection(directions.get(1));
				}
			}

			//			directionText+=leg.mode+"\n";
		}
	}

	private Direction decryptNonTransit(Leg leg){
		Direction direction = new Direction();

		//		http://opentripplanner.usf.edu/opentripplanner-api-webapp/ws/plan?optimize=QUICK&time=09:24pm&arriveBy=false&wheelchair=false&maxWalkDistance=7600.0&fromPlace=28.033389%2C+-82.521034&toPlace=28.064709%2C+-82.471618&date=03/07/12&mode=WALK,TRAM,SUBWAY,RAIL,BUS,FERRY,CABLE_CAR,GONDOLA,FUNICULAR,TRANSIT,TRAINISH,BUSISH
		
		if (leg.getAgencyTimeZoneOffset() != 0){
			this.agencyTimeZoneOffset = leg.getAgencyTimeZoneOffset();
		}
		
		// Get appropriate action and icon
		String action = applicationContext.getResources().getString(R.string.mode_walk_action);
		int icon = R.drawable.mode_walk;
		TraverseMode mode = TraverseMode.valueOf((String) leg.mode);
		if(mode.compareTo(TraverseMode.BICYCLE)==0){
			action = applicationContext.getResources().getString(R.string.mode_bicycle_action);
			icon = R.drawable.mode_bicycle;
		} else if(mode.compareTo(TraverseMode.CAR)==0){
			action = applicationContext.getResources().getString(R.string.mode_car_action);
			icon = R.drawable.icon;
		}

		direction.setIcon(icon);

		//		Main direction
		Place fromPlace = leg.from;
		Place toPlace = leg.to;
		String mainDirectionText = action;
		mainDirectionText += fromPlace.name==null ? "" : " " + applicationContext.getResources().getString(R.string.step_by_step_from) + " " + fromPlace.name;
		mainDirectionText += toPlace.name==null ? "" : " " + applicationContext.getResources().getString(R.string.step_by_step_to) + " " + toPlace.name;
		mainDirectionText += toPlace.stopId==null ? "" : " (" + toPlace.stopId.getAgencyId() + " " + toPlace.stopId.getId() + ")";
//		double duration = DateTimeConversion.getDuration(leg.startTime, leg.endTime);
		double totalDistance = leg.distance;
		mainDirectionText += "\n[" + String.format(OTPApp.FORMAT_DISTANCE_METERS_FULL, totalDistance) + applicationContext.getResources().getString(R.string.distance_unit) +" ]";// Double.toString(duration);

		direction.setDirectionText(mainDirectionText);

		//		Sub-direction
		List<WalkStep> walkSteps = leg.getWalkSteps();

		if(walkSteps==null) return direction;

		ArrayList<Direction> subDirections = new ArrayList<Direction>(walkSteps.size());

		for(WalkStep step: walkSteps){
			Direction dir = new Direction();
			String subDirectionText = "";

			double distance = step.distance;
			// Distance traveled [distance]
	//		distance = Double.valueOf(twoDForm.format(distance)); -->VREIXO
			dir.setDistanceTraveled(distance);

			RelativeDirection relativeDir = step.relativeDirection;
			String streetName = step.streetName;
			AbsoluteDirection absoluteDir = step.absoluteDirection;
			String exit = step.exit;
			boolean isStayOn = (step.stayOn==null ? false : step.stayOn);
			boolean isBogusName = (step.bogusName==null ? false : step.bogusName);
			double lon = step.lon;
			double lat = step.lat;
			//Elevation[] elevation = step.getElevation();  //Removed elevation for now, since we're not doing anything with it and it causes version issues between OTP server APIs v0.9.1-SNAPSHOT and v0.9.2-SNAPSHOT
			List<Alerts> alert = step.alerts;

			// Walk East
			if(relativeDir==null){
				subDirectionText += action + " " + applicationContext.getResources().getString(R.string.step_by_step_heading) + " ";
				subDirectionText += absoluteDir.name() + " ";
			}
			// (Turn left)/(Continue) 
			else {
				if(!isStayOn) {
					RelativeDirection rDir = RelativeDirection.valueOf(relativeDir.name());

					// Do not need TURN Continue
					if( rDir.compareTo(RelativeDirection.CONTINUE) != 0 &&
							rDir.compareTo(RelativeDirection.CIRCLE_CLOCKWISE) !=0 &&
							rDir.compareTo(RelativeDirection.CIRCLE_COUNTERCLOCKWISE) != 0){
						subDirectionText += applicationContext.getResources().getString(R.string.step_by_step_turn) + " ";
					}

					subDirectionText += relativeDir.name() + " ";
				} else {
					subDirectionText += relativeDir.name() + " ";
				}
			}

			// (on ABC)
			//			if(!isBogusName) {
			//				subDirectionText += "on "+ streetName + " ";
			//			}

			subDirectionText += applicationContext.getResources().getString(R.string.step_by_step_connector_street_name) + " "+ streetName + " ";

			subDirectionText += "\n[" + String.format(OTPApp.FORMAT_DISTANCE_METERS_FULL, distance) + applicationContext.getResources().getString(R.string.distance_unit) + " ]";

			dir.setDirectionText(subDirectionText);

			dir.setIcon(icon);

			// Add new sub-direction
			subDirections.add(dir);
		}

		direction.setSubDirections(subDirections);

		return direction;
	}

	private ArrayList<Direction> decryptTransit(Leg leg){
		ArrayList<Direction> directions = new ArrayList<Direction>(2);
		Direction onDirection = new Direction();
		Direction offDirection = new Direction(); 
		
		if (leg.getAgencyTimeZoneOffset() != 0){
			this.agencyTimeZoneOffset = leg.getAgencyTimeZoneOffset();
		}

		//		set icon
		TraverseMode mode = TraverseMode.valueOf((String) leg.mode);
		int icon = R.drawable.mode_bus;
		if(mode.compareTo(TraverseMode.RAIL)==0){
			icon = R.drawable.mode_rail;
		} else if(mode.compareTo(TraverseMode.FERRY)==0){
			icon = R.drawable.mode_ferry;
		} else if(mode.compareTo(TraverseMode.GONDOLA)==0){
			icon = R.drawable.mode_gondola;
		} else if(mode.compareTo(TraverseMode.SUBWAY)==0){
			icon = R.drawable.mode_subway;
		} else if(mode.compareTo(TraverseMode.TRAM)==0){
			icon = R.drawable.mode_tram;
		}

		onDirection.setIcon(icon);

		//		set direction text
		String onDirectionText = "";
		String offDirectionText = "";

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
		String startTime = leg.startTime;

		ArrayList<Place> stopsInBetween = new ArrayList<Place>();
		if(leg.getStop()!=null)
			stopsInBetween.addAll(leg.getStop());

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

		offDirectionText += applicationContext.getResources().getString(R.string.step_by_step_transit_get_off) + " " + serviceName + " " + mode + " " + route + "\n";
		offDirectionText += applicationContext.getResources().getString(R.string.step_by_step_transit_connector_stop_name) + " " + to.name + " (" + agencyAndIdTo.getAgencyId() + " " + agencyAndIdTo.getId() + ")";
		offDirection.setDirectionText(offDirectionText);
		offDirection.setIcon(icon);

		// Only onDirection has subdirection (list of stops in between)
		
		onDirectionText += applicationContext.getResources().getString(R.string.step_by_step_transit_get_on) + " " + serviceName + " " + mode + " " + route + DateTimeConversion.getTimeWithContext(applicationContext, agencyTimeZoneOffset, Long.parseLong(leg.getStartTime()), true) + "\n";
		onDirectionText += applicationContext.getResources().getString(R.string.step_by_step_transit_connector_stop_name) + " " + from.name + " (" + agencyAndIdFrom.getAgencyId() + " " + agencyAndIdFrom.getId() + ")\n";
		onDirectionText += stopsInBetween.size() + " " + applicationContext.getResources().getString(R.string.step_by_step_transit_stops_in_between);
		onDirection.setDirectionText(onDirectionText);
		onDirection.setIcon(icon);

		// sub-direction
		ArrayList<Direction> subDirections = new ArrayList<Direction>();
		for(int i=0; i<stopsInBetween.size(); i++){
			Direction subDirection = new Direction();

			Place stop = stopsInBetween.get(i);
			AgencyAndId agencyAndIdStop = stop.stopId;
			String subDirectionText = 	Integer.toString(i) + ". " +stop.name + " (" + 
					agencyAndIdStop.getAgencyId() + " " + 
					agencyAndIdStop.getId() + ")";

			subDirection.setDirectionText(subDirectionText);
			subDirection.setIcon(icon);

			subDirections.add(subDirection);
		}
		onDirection.setSubDirections(subDirections);

		// Distance traveled [distance]
	//	distance = Double.valueOf(twoDForm.format(distance)); -->VREIXO
		onDirection.setDistanceTraveled(distance);

		directions.add(onDirection);
		directions.add(offDirection);

		return directions;
	}

	/**
	 * @return the totalDistance
	 */
	public double getTotalDistance() {
	//	totalDistance = Double.valueOf(twoDForm.format(totalDistance));-->VREIXO
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

		totalTimeTraveled = DateTimeConversion.getDuration(startTimeText, endTimeText);

		return totalTimeTraveled;
	}

}
