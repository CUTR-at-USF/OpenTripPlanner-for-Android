package edu.usf.cutr.opentripplanner.android.model;

public class ArriveBySpinnerItem {

	private String displayName;
	private boolean arriveBy;
	
	public ArriveBySpinnerItem() {
	}

	public ArriveBySpinnerItem(String displayName, Boolean arriveBy) {
		super();
		this.displayName = displayName;
		this.arriveBy = arriveBy;
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

	public Boolean getValue() {
		return arriveBy;
	}

	public void setValue(Boolean arriveBy) {
		this.arriveBy = arriveBy;
	}

}
