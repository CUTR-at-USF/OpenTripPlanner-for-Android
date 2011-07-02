package org.opentripplanner.android.contacts;

import android.os.Build;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;

public abstract class ContactAPI {

	private static ContactAPI api;
	
	private Cursor cur;
	private ContentResolver cr;
	
	public static ContactAPI getAPI() {
		if (api == null) {
			String apiClass;
			if (Integer.parseInt(Build.VERSION.SDK) >= Build.VERSION_CODES.ECLAIR) {
				apiClass = "org.opentripplanner.android.contacts.ContactAPISdk5";
			} else {
				apiClass = "org.opentripplanner.android.contacts.ContactAPISdk3";
			}
			
			try {
				Class<? extends ContactAPI> realClass = Class.forName(apiClass).asSubclass(ContactAPI.class);
				api = realClass.newInstance();
			} catch (Exception e) {
				throw new IllegalStateException(e);
			}
			
		}
		return api;
	}
	
	public abstract Intent getContactIntent();
	
	public abstract ContactList newContactList();
	
	public abstract Cursor getCur();
	public abstract void setCur(Cursor cur);
	
	public abstract ContentResolver getCr();
	public abstract void setCr(ContentResolver cr);
	
}
