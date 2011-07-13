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

import android.app.Application;

public class OTPApp extends Application {
	
	private Server selectedServer;

	/**
	 * @param selectedServer the selected OTP server
	 */
	public void setSelectedServer(Server selectedServer) {
		this.selectedServer = selectedServer;
	}

	/**
	 * @return the selected OTP server
	 */
	public Server getSelectedServer() {
		return selectedServer;
	}
	
}
