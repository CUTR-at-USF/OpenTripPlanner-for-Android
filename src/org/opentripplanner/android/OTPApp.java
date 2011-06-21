package org.opentripplanner.android;

import android.app.Application;

public class OTPApp extends Application {
	
	private String serverURL;

	/**
	 * @param serverURL the serverURL to set
	 */
	public void setServerURL(String serverURL) {
		this.serverURL = serverURL;
	}

	/**
	 * @return the serverURL
	 */
	public String getServerURL() {
		return serverURL;
	}
	
}
