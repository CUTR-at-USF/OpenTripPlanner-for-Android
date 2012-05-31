package edu.usf.cutr.opentripplanner.android.contacts;

import java.util.ArrayList;

public class ContactList {

	private ArrayList<Contact> contacts = new ArrayList<Contact>();

	public ArrayList<Contact> getContacts() {
		return contacts;
	}

	public void setContacts(ArrayList<Contact> contacts) {
		this.contacts = contacts;
	}
	
	public void addContact(Contact contact) {
		this.contacts.add(contact);
	}
 	
	public ContactList() {
		
	}
	
}
