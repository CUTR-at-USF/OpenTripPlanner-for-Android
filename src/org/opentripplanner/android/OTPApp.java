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

package org.opentripplanner.android;

import org.opentripplanner.android.model.Server;
import org.opentripplanner.android.tasks.MetadataRequest;

import android.app.Application;

public class OTPApp extends Application {
	
	private Server selectedServer;
	
	private double lowerLeftLatitude, lowerLeftLongitude, upperRightLatitude, upperRightLongitude;

	/**
	 * @param selectedServer the selected OTP server
	 */
	public void setSelectedServer(Server selectedServer, MyActivity activity) {
		this.selectedServer = selectedServer;
		new MetadataRequest(activity).execute("");
	}

	/**
	 * @return the selected OTP server
	 */
	public Server getSelectedServer() {
		return selectedServer;
	}

	/**
	 * @return the lowerLeftLatitude
	 */
	public double getLowerLeftLatitude() {
		return lowerLeftLatitude;
	}

	/**
	 * @param lowerLeftLatitude the lowerLeftLatitude to set
	 */
	public void setLowerLeftLatitude(double lowerLeftLatitude) {
		this.lowerLeftLatitude = lowerLeftLatitude;
	}

	/**
	 * @return the lowerLeftLongitude
	 */
	public double getLowerLeftLongitude() {
		return lowerLeftLongitude;
	}

	/**
	 * @param lowerLeftLongitude the lowerLeftLongitude to set
	 */
	public void setLowerLeftLongitude(double lowerLeftLongitude) {
		this.lowerLeftLongitude = lowerLeftLongitude;
	}

	/**
	 * @return the upperRightLatitude
	 */
	public double getUpperRightLatitude() {
		return upperRightLatitude;
	}

	/**
	 * @param upperRightLatitude the upperRightLatitude to set
	 */
	public void setUpperRightLatitude(double upperRightLatitude) {
		this.upperRightLatitude = upperRightLatitude;
	}

	/**
	 * @return the upperRightLongitude
	 */
	public double getUpperRightLongitude() {
		return upperRightLongitude;
	}

	/**
	 * @param upperRightLongitude the upperRightLongitude to set
	 */
	public void setUpperRightLongitude(double upperRightLongitude) {
		this.upperRightLongitude = upperRightLongitude;
	}
	
}
