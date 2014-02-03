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

import com.google.android.gms.maps.model.LatLng;

import edu.usf.cutr.opentripplanner.android.R;
import android.content.Context;
import android.location.Location;

/**
 * Modified by Khoa Tran
 *
 */

public class Server {

	private long id;
	private Long date;
	private String region;
	private String baseURL;
	private String bounds;
	private String center;
	private String zoom;
	private String language;
	private String contactName;
	private String contactEmail;

	private double lowerLeftLatitude, lowerLeftLongitude, upperRightLatitude, upperRightLongitude;

	private double geometricalCenterLatitude, geometricalCenterLongitude;  //for Google Places
	
	private double centerLatitude, centerLongitude;  
	
	private float initialZoom;   
	
	private boolean boundsSet = false;

	public boolean areBoundsSet() {
		return boundsSet;
	}
	
	private boolean centerSet = false;
	
	public boolean isCenterSet(){
		return centerSet;
	}

	private boolean zoomSet = false;

	public boolean isZoomSet() {
		return zoomSet;
	} 

	public Server() {
		super();
	}
	
	public Server(Server s) {
		super();
		setId(s.getId());
		setDate(s.getDate());
		setRegion(s.getRegion());
		setBaseURL(s.getBaseURL());
		setBounds(s.getBounds());  // do extra string processing to set lowerleft and upperright
		setCenter(s.getCenter());
		setZoom(s.getZoom());
		setLanguage(s.getLanguage());
		setContactName(s.getContactName());
		setContactEmail(s.getContactEmail());
	}

	public Server(Long d, String region, String baseURL, String bounds,
			String language, String contactName, String contactEmail, String center, String zoom) {
		super();
		setDate(d);
		setRegion(region);
		setBaseURL(baseURL);
		setBounds(bounds);  // do extra string processing to set lowerleft and upperright
		setCenter(center); 
		setZoom(zoom);
		setLanguage(language);
		setContactName(contactName);
		setContactEmail(contactEmail);
	}
	
	public Server(String region, String baseURL, String bounds,
			String language, String contactName, String contactEmail, String center, String zoom) {
		super();
		setRegion(region);
		setBaseURL(baseURL);
		setBounds(bounds);  // do extra string processing to set lowerleft and upperright
		setCenter(center);
		setZoom(zoom);
		setLanguage(language);
		setContactName(contactName);
		setContactEmail(contactEmail);
	}

	/*
	 * Constructor for server with a custom URL
	 */
	public Server(String baseURL, Context applicationContext) {
		super();
		this.baseURL = baseURL;
		this.region = applicationContext.getResources().getString(R.string.custom_server_unknown_region);
		this.bounds = applicationContext.getResources().getString(R.string.custom_server_unknown_bounds);
		this.language = applicationContext.getResources().getString(R.string.custom_server_unknown_language);
		this.contactName = applicationContext.getResources().getString(R.string.custom_server_unknown_name);
		this.contactEmail = applicationContext.getResources().getString(R.string.custom_server_unknown_email);
	}

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public String getBaseURL() {
		return baseURL;
	}

	public void setBaseURL(String baseURL) {
		this.baseURL = baseURL;
	}
	
	public String getZoom() {
		return zoom;
	}

	public void setZoom(String zoom) {
		zoomSet = true;
		this.zoom = zoom;

		setInitialZoom(Float.parseFloat(zoom));
	}

	public String getCenter() {
		return center;
	}

	public void setCenter(String center) {
		centerSet = true;
		this.center = center;
		String[] tokens = center.split(",");

		setCenterLatitude(Double.parseDouble(tokens[0]));
		setCenterLongitude(Double.parseDouble(tokens[1]));
	}

	public String getBounds() {
		return bounds;
	}

	public void setBounds(String bounds) {
		boundsSet = true;
		this.bounds = bounds;
		String[] tokens = bounds.split(",");

		setLowerLeftLatitude(Double.parseDouble(tokens[0]));
		setLowerLeftLongitude(Double.parseDouble(tokens[1]));
		setUpperRightLatitude(Double.parseDouble(tokens[2]));
		setUpperRightLongitude(Double.parseDouble(tokens[3]));
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getContactEmail() {
		return contactEmail;
	}

	public void setContactEmail(String contactEmail) {
		this.contactEmail = contactEmail;
	}

	public String toString() {
		return region;
	}

	/**
	 * @return the id
	 */
	public long getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(long id) {
		this.id = id;
	}

	/**
	 * @return the date
	 */
	public Long getDate() {
		return date;
	}

	/**
	 * @param date the date to set
	 */
	public void setDate(Long date) {
		this.date = date;
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

	/**
	 * @return the radius
	 */
	public double getRadius() {
		float[] results = new float[3];
		Location.distanceBetween(geometricalCenterLatitude, geometricalCenterLongitude, lowerLeftLatitude, lowerLeftLongitude, results);
		return results[0];
	}

	/**
	 * @return the geometricalCenterLatitude
	 */
	public double getGeometricalCenterLatitude() {
		geometricalCenterLatitude = (lowerLeftLatitude + upperRightLatitude)/2;
		return geometricalCenterLatitude;
	}

	/**
	 * @return the geometricalCenterLongitude
	 */
	public double getGeometricalCenterLongitude() {
		geometricalCenterLongitude = (lowerLeftLongitude + upperRightLongitude)/2;
		return geometricalCenterLongitude;
	}
	
	/**
	 * @return the geometricalCenterLatitude
	 */
	public double getCenterLatitude() {
		return centerLatitude;
	}

	/**
	 * @return the geometricalCenterLongitude
	 */
	public double getCenterLongitude() {
		return centerLongitude;
	}

	/**
	 * @return the contactName
	 */
	public String getContactName() {
		return contactName;
	}

	/**
	 * @param contactName the contactName to set
	 */
	public void setContactName(String contactName) {
		this.contactName = contactName;
	}

	public void setCenterLatitude(double centerLatitude) {
		this.centerLatitude = centerLatitude;
	}

	public void setCenterLongitude(double centerLongitude) {
		this.centerLongitude = centerLongitude;
	}

	public float getInitialZoom() {
		return initialZoom;
	}

	public void setInitialZoom(float initialZoom) {
		this.initialZoom = initialZoom;
	}
	
}
