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

package org.opentripplanner.android.model;

import java.sql.Date;

/**
 * Modified by Khoa Tran
 *
 */

public class Server {

	private long id;
	private Date date;
	private String region;
	private String baseURL;
	private String bounds;
	private String language;
	private String contact;
	private String contactEmail;
	
	public Server() {
		super();
	}

	public Server(String region, String baseURL, String bounds,
			String language, String contact, String contactEmail) {
		super();
		this.region = region;
		this.baseURL = baseURL;
		this.bounds = bounds;
		this.language = language;
		this.contact = contact;
		this.contactEmail = contactEmail;
	}
	
	/*
	 * Constructor for server with a custom URL
	 */
	public Server(String baseURL) {
		super();
		this.baseURL = baseURL;

		this.region = "Unknown Region";
		this.bounds = "Unknown Bounds";
		this.language = "Unknown Language";
		this.contact = "Unknown Contact";
		this.contactEmail = "Unknown Email";
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

	public String getBounds() {
		return bounds;
	}

	public void setBounds(String bounds) {
		this.bounds = bounds;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getContact() {
		return contact;
	}

	public void setContact(String contact) {
		this.contact = contact;
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
	public Date getDate() {
		return date;
	}

	/**
	 * @param date the date to set
	 */
	public void setDate(Date date) {
		this.date = date;
	}
}
