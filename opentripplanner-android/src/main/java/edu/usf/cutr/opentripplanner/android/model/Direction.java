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

package edu.usf.cutr.opentripplanner.android.model;

import java.util.ArrayList;

/**
 * @author Khoa Tran
 *
 */

public class Direction {
	
	private int icon;
	private int directionIndex;
    private String directionText;
    private ArrayList<Direction> subDirections = null;
    private double distanceTraveled;
    
    public Direction(){
        super();
    }
    
    public Direction(int icon, String directionText) {
        super();
        this.setIcon(icon);
        this.setDirectionText(directionText);
    }

	/**
	 * @return the icon
	 */
	public int getIcon() {
		return icon;
	}

	/**
	 * @param icon the icon to set
	 */
	public void setIcon(int icon) {
		this.icon = icon;
	}

	/**
	 * @return the directionText
	 */
	public String getDirectionText() {
		return directionText;
	}

	/**
	 * @param directionText the directionText to set
	 */
	public void setDirectionText(String directionText) {
		this.directionText = directionText;
	}


	/**
	 * @return the distanceTraveled
	 */
	public double getDistanceTraveled() {
		return distanceTraveled;
	}

	/**
	 * @param distanceTraveled the distanceTraveled to set
	 */
	public void setDistanceTraveled(double distanceTraveled) {
		this.distanceTraveled = distanceTraveled;
	}

	/**
	 * @return the subDirections
	 */
	public ArrayList<Direction> getSubDirections() {
		return subDirections;
	}

	/**
	 * @param subDirections the subDirections to set
	 */
	public void setSubDirections(ArrayList<Direction> subDirections) {
		this.subDirections = subDirections;
	}

	public int getDirectionIndex() {
		return directionIndex;
	}

	public void setDirectionIndex(int directionIndex) {
		this.directionIndex = directionIndex;
	}

}
