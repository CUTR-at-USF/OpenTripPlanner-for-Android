/*
 * Copyright 2011 Marcy Gordon
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package edu.usf.cutr.opentripplanner.android.model;

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
