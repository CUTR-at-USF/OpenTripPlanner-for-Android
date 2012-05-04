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

package org.opentripplanner.android.model;

import java.io.Serializable;
import java.util.List;

import org.opentripplanner.api.model.Leg;
import org.osmdroid.util.GeoPoint;

/**
 * @author Khoa Tran
 *
 */

public class OTPBundle implements Serializable{
	private String toText, fromText;
	private int optimization, travelMode;
	private GeoPoint startLocation, endLocation;
	private List<Leg> currentItinerary;
	public OTPBundle(){
		
	}
	/**
	 * @return the toText
	 */
	public String getToText() {
		return toText;
	}
	/**
	 * @param toText the toText to set
	 */
	public void setToText(String toText) {
		this.toText = toText;
	}
	/**
	 * @return the fromText
	 */
	public String getFromText() {
		return fromText;
	}
	/**
	 * @param fromText the fromText to set
	 */
	public void setFromText(String fromText) {
		this.fromText = fromText;
	}
	/**
	 * @return the optimization
	 */
	public int getOptimization() {
		return optimization;
	}
	/**
	 * @param optimization the optimization to set
	 */
	public void setOptimization(int optimization) {
		this.optimization = optimization;
	}
	/**
	 * @return the travelMode
	 */
	public int getTravelMode() {
		return travelMode;
	}
	/**
	 * @param travelMode the travelMode to set
	 */
	public void setTravelMode(int travelMode) {
		this.travelMode = travelMode;
	}
	/**
	 * @return the startLocation
	 */
	public GeoPoint getStartLocation() {
		return startLocation;
	}
	/**
	 * @param startLocation the startLocation to set
	 */
	public void setStartLocation(GeoPoint startLocation) {
		this.startLocation = startLocation;
	}
	/**
	 * @return the endLocation
	 */
	public GeoPoint getEndLocation() {
		return endLocation;
	}
	/**
	 * @param endLocation the endLocation to set
	 */
	public void setEndLocation(GeoPoint endLocation) {
		this.endLocation = endLocation;
	}
	/**
	 * @return the currentItinerary
	 */
	public List<Leg> getCurrentItinerary() {
		return currentItinerary;
	}
	/**
	 * @param currentItinerary the currentItinerary to set
	 */
	public void setCurrentItinerary(List<Leg> currentItinerary) {
		this.currentItinerary = currentItinerary;
	}
}
