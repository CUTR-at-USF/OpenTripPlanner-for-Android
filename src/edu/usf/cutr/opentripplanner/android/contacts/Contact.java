package edu.usf.cutr.opentripplanner.android.contacts;

import java.util.ArrayList;

public class Contact {
	private String id;
	private String displayName;
//	private ArrayList<Phone> phone;
//	private ArrayList<Email> email;
	private ArrayList<String> notes;
	private ArrayList<Address> addresses = new ArrayList<Address>();
//	private ArrayList<IM> imAddresses;
//	private Organization organization;
 	
	
//	public Organization getOrganization() {
//		return organization;
//	}
//	public void setOrganization(Organization organization) {
//		this.organization = organization;
//	}
//	public ArrayList<IM> getImAddresses() {
//		return imAddresses;
//	}
//	public void setImAddresses(ArrayList<IM> imAddresses) {
//		this.imAddresses = imAddresses;
// 	}
//	public void addImAddresses(IM imAddr) {
//		this.imAddresses.add(imAddr);
//	}
	public ArrayList<String> getNotes() {
		return notes;
	}
	public void setNotes(ArrayList<String> notes) {
		this.notes = notes;
	}
	public void addNote(String note) {
		this.notes.add(note);
	}
	public ArrayList<Address> getAddresses() {
		return addresses;
	}
	public void setAddresses(ArrayList<Address> addresses) {
		this.addresses = addresses;
	}
	public void addAddress(Address address) {
		this.addresses.add(address);
	}
//	public ArrayList<Email> getEmail() {
//		return email;
//	}
//	public void setEmail(ArrayList<Email> email) {
//		this.email = email;
//	}
//	public void addEmail(Email e) {
//		this.email.add(e);
//	}	
	public String getId() {
		return id;
	}
	public void setId(String id) {
 		this.id = id;
	}
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String dName) {
		this.displayName = dName;
	}
//	public ArrayList<Phone> getPhone() {
//		return phone;
//	}
//	public void setPhone(ArrayList<Phone> phone) {
//		this.phone = phone;
//	}
//	public void addPhone(Phone phone) {
//		this.phone.add(phone);
//	}
}
