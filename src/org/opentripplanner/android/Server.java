package org.opentripplanner.android;

public class Server {

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
	
	public Server(String baseURL) {
		super();
		this.baseURL = baseURL;

		this.region = "Custom Region";
		this.bounds = "Uknown bounds";
		this.language = "Custom Language";
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
}
