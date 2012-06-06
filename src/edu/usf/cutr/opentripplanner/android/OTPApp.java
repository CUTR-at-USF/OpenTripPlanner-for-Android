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

package edu.usf.cutr.opentripplanner.android;

import edu.usf.cutr.opentripplanner.android.model.Server;
import edu.usf.cutr.opentripplanner.android.tasks.MetadataRequest;

import android.app.Application;
import android.location.Location;

/*
 * Modified by Khoa Tran
 */

public class OTPApp extends Application {
	
	public static final int REFRESH_SERVER_LIST_REQUEST_CODE = 2;
	
	public static final int CHOOSE_CONTACT_REQUEST_CODE = 1;
	
	public static final String REFRESH_SERVER_RETURN_KEY = "RefreshServer";
	
	private static Server selectedServer;

	/**
	 * @param selectedServer the selected OTP server
	 */
	public void setSelectedServer(Server sServer) {
		selectedServer = sServer;
//		new MetadataRequest(activity).execute("");
	}

	/**
	 * @return the selected OTP server
	 */
	public Server getSelectedServer() {
		return selectedServer;
	}
	
}
