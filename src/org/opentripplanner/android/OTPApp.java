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
