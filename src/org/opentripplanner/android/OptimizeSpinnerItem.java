package org.opentripplanner.android;

import org.opentripplanner.routing.core.OptimizeType;

public class OptimizeSpinnerItem {

	private String displayName;
	private OptimizeType optimizeType;
	
	public OptimizeSpinnerItem () {
		
	}
	
	public OptimizeSpinnerItem(String displayName, OptimizeType optimizeType){
		this.displayName = displayName;
		this.optimizeType = optimizeType;
	}
	
	public String toString(){
		return displayName;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public OptimizeType getOptimizeType() {
		return optimizeType;
	}

	public void setOptimizeType(OptimizeType optimizeType) {
		this.optimizeType = optimizeType;
	}
}
