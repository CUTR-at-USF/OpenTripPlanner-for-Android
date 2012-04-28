package org.opentripplanner.android.model;

import java.util.ArrayList;

public class Direction {
	
	private int icon;
    private String directionText;
    private ArrayList<Direction> nonTransitSubDirection = null; //for non-transit direction
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
	 * @return the nonTransitSubDirection
	 */
	public ArrayList<Direction> getNonTransitSubDirection() {
		return nonTransitSubDirection;
	}

	/**
	 * @param nonTransitSubDirection the nonTransitSubDirection to set
	 */
	public void setNonTransitSubDirection(ArrayList<Direction> nonTransitSubDirection) {
		this.nonTransitSubDirection = nonTransitSubDirection;
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

}
