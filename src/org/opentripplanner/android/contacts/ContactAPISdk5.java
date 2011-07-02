package org.opentripplanner.android.contacts;

import java.util.ArrayList;
import android.content.Intent;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.content.ContentResolver;

public class ContactAPISdk5 extends ContactAPI {

	private Cursor cur;
	private ContentResolver cr;
	
	public Cursor getCur() {
		return cur;
	}

	public void setCur(Cursor cur) {
		this.cur = cur;
	}

	public ContentResolver getCr() {
		return cr;
	}

	public void setCr(ContentResolver cr) {
		this.cr = cr;
	}

	public Intent getContactIntent() {
		return(new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI));
	}
	
	public ContactList newContactList() {
		ContactList contacts = new ContactList();
		String id;
		
		this.cur = this.cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);
		if (this.cur.getCount() > 0) {
			while (cur.moveToNext()) {
				Contact c = new Contact();
				id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
				c.setId(id);
				c.setDisplayName(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)));
//				if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
//					c.setPhone(this.getPhoneNumbers(id));
//				}
//				c.setEmail(this.getEmailAddresses(id));
//				c.setNotes(this.getContactNotes(id));
				ArrayList<Address> addresses = this.getContactAddresses(id);
				if(addresses.isEmpty()) {
					continue;
				}
				c.setAddresses(addresses);
//				c.setImAddresses(this.getIM(id));
//				c.setOrganization(this.getContactOrg(id));
				contacts.addContact(c);
			}
		}
		return(contacts);
	}
	
//	public ArrayList<Phone> getPhoneNumbers(String id) {
//		ArrayList<Phone> phones = new ArrayList<Phone>();
//		
//		Cursor pCur = this.cr.query(
//				ContactsContract.CommonDataKinds.Phone.CONTENT_URI, 
//				null, 
//				ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?", 
//				new String[]{id}, null);
//		while (pCur.moveToNext()) {
//			phones.add(new Phone(
//					pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
//					, pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE))
//			));
//
//		} 
//		pCur.close();
//		return(phones);
//	}
	
//	public ArrayList<Email> getEmailAddresses(String id) {
//		ArrayList<Email> emails = new ArrayList<Email>();
//		
//		Cursor emailCur = this.cr.query( 
//				ContactsContract.CommonDataKinds.Email.CONTENT_URI, 
//				null,
//				ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?", 
//				new String[]{id}, null); 
//		while (emailCur.moveToNext()) { 
//		    // This would allow you get several email addresses
//			Email e = new Email(emailCur.getString(emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA))
//					,emailCur.getString(emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE))  
//					);
//			emails.add(e);
//		} 
//		emailCur.close();
//		return(emails);
//	}
	
	public ArrayList<String> getContactNotes(String id) {
		ArrayList<String> notes = new ArrayList<String>();
		String where = ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?"; 
		String[] whereParameters = new String[]{id, 
			ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE}; 
		Cursor noteCur = this.cr.query(ContactsContract.Data.CONTENT_URI, null, where, whereParameters, null); 
		if (noteCur.moveToFirst()) { 
			String note = noteCur.getString(noteCur.getColumnIndex(ContactsContract.CommonDataKinds.Note.NOTE));
			if (note.length() > 0) {
				notes.add(note);
			}
		} 
		noteCur.close();
		return(notes);
	}
	
	public ArrayList<Address> getContactAddresses(String id) {
		ArrayList<Address> addrList = new ArrayList<Address>();
		
		String where = ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?"; 
		String[] whereParameters = new String[]{id, 
				ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE}; 
		
		Cursor addrCur = this.cr.query(ContactsContract.Data.CONTENT_URI, null, where, whereParameters, null); 
		while(addrCur.moveToNext()) {
			String poBox = addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.POBOX));
			String street = addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.STREET));
			String city = addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.CITY));
			String state = addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.REGION));
			String postalCode = addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE));
			String country = addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY));
			String type = addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.TYPE));
			Address a = new Address(poBox, street, city, state, postalCode, country, type);
			addrList.add(a);
		} 
		addrCur.close();
		return(addrList);
	}
	
//	public ArrayList<IM> getIM(String id) {
//		ArrayList<IM> imList = new ArrayList<IM>();
//		String where = ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?"; 
//		String[] whereParameters = new String[]{id, 
//				ContactsContract.CommonDataKinds.Im.CONTENT_ITEM_TYPE}; 
//		
//		Cursor imCur = this.cr.query(ContactsContract.Data.CONTENT_URI, null, where, whereParameters, null); 
//		if (imCur.moveToFirst()) { 
//			String imName = imCur.getString(imCur.getColumnIndex(ContactsContract.CommonDataKinds.Im.DATA));
//			String imType;
//			imType = imCur.getString(imCur.getColumnIndex(ContactsContract.CommonDataKinds.Im.TYPE));
//			if (imName.length() > 0) {
//				IM im = new IM(imName, imType);
//				imList.add(im);
//			}
//		} 
//		imCur.close();
//		return(imList);
//	}
	
//	public Organization getContactOrg(String id) {
//		Organization org = new Organization();
//		String where = ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?"; 
//		String[] whereParameters = new String[]{id, 
//				ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE}; 
//		
//		Cursor orgCur = this.cr.query(ContactsContract.Data.CONTENT_URI, null, where, whereParameters, null);
//
//		if (orgCur.moveToFirst()) { 
//			String orgName = orgCur.getString(orgCur.getColumnIndex(ContactsContract.CommonDataKinds.Organization.DATA));
//			String title = orgCur.getString(orgCur.getColumnIndex(ContactsContract.CommonDataKinds.Organization.TITLE));
//			if (orgName.length() > 0) {
//				org.setOrganization(orgName);
//				org.setTitle(title);
//			}
//		} 
//		orgCur.close();
//		return(org);
//	}
	
}
