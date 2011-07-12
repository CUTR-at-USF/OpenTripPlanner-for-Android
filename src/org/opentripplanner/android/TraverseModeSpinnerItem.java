package org.opentripplanner.android;

import org.opentripplanner.routing.core.TraverseModeSet;

public class TraverseModeSpinnerItem {

	private String displayName;
	private TraverseModeSet traverseModeSet;
	
	public TraverseModeSpinnerItem() {
	}

	public TraverseModeSpinnerItem(String displayName,
			TraverseModeSet traverseModeSet) {
		super();
		this.displayName = displayName;
		this.traverseModeSet = traverseModeSet;
	}
	
	public String toString() {
		return displayName;
	}
	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public TraverseModeSet getTraverseModeSet() {
		return traverseModeSet;
	}

	public void setTraverseModeSet(TraverseModeSet traverseModeSet) {
		this.traverseModeSet = traverseModeSet;
	}

}
