/*
 * Copyright 2012 University of South Florida
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

package edu.usf.cutr.opentripplanner.android.sqlite;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import edu.usf.cutr.opentripplanner.android.OTPApp;
import edu.usf.cutr.opentripplanner.android.model.Server;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * @author Khoa Tran
 *
 */

public class ServersDataSource {

	// Database fields
	private SQLiteDatabase database;
	private MySQLiteHelper dbHelper;
	private static ServersDataSource mInstance = null;
	private String[] allColumns = { MySQLiteHelper.COLUMN_ID,
			MySQLiteHelper.COLUMN_DATE, 
			MySQLiteHelper.COLUMN_REGION,
			MySQLiteHelper.COLUMN_BASEURL,
			MySQLiteHelper.COLUMN_BOUNDS,
			MySQLiteHelper.COLUMN_LANGUAGE,
			MySQLiteHelper.COLUMN_CONTACT_NAME,
			MySQLiteHelper.COLUMN_CONTACT_EMAIL,
			MySQLiteHelper.COLUMN_CENTER,
			MySQLiteHelper.COLUMN_ZOOM};

	private static final String TAG = "OTP";
	
	private SimpleDateFormat dateFormat = (SimpleDateFormat) SimpleDateFormat.getDateTimeInstance();

	private ServersDataSource(Context context) {
		dbHelper = MySQLiteHelper.getInstance(context);
	}
	
    public static ServersDataSource getInstance(Context ctx) {
    	if (mInstance == null) {
    		mInstance = new ServersDataSource(ctx);
    	}
    	return mInstance;
    }

	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}

	public void close() {
		dbHelper.close();
	}

	public Server createServer(Server s) {
		ContentValues values = new ContentValues();
				
		if(s.getDate()!=null){			
			values.put(MySQLiteHelper.COLUMN_DATE, s.getDate());
			Log.d(OTPApp.TAG, "Wrote '" + s.getRegion() + "' server date to SQLLite - " + s.getDate());
		}
		values.put(MySQLiteHelper.COLUMN_REGION, s.getRegion());
		values.put(MySQLiteHelper.COLUMN_BASEURL, s.getBaseURL());
		values.put(MySQLiteHelper.COLUMN_BOUNDS, s.getBounds());
		values.put(MySQLiteHelper.COLUMN_CENTER, s.getCenter());
		values.put(MySQLiteHelper.COLUMN_ZOOM, s.getZoom());
		values.put(MySQLiteHelper.COLUMN_LANGUAGE, s.getLanguage());
		values.put(MySQLiteHelper.COLUMN_CONTACT_NAME, s.getContactName());
		values.put(MySQLiteHelper.COLUMN_CONTACT_EMAIL, s.getContactEmail());

		long insertId = database.insert(MySQLiteHelper.TABLE_SERVERS, null,
				values);
		Cursor cursor = database.query(MySQLiteHelper.TABLE_SERVERS,
				allColumns, MySQLiteHelper.COLUMN_ID + " = " + insertId, null,
				null, null, null);
		cursor.moveToFirst();
		Server newServer = cursorToServer(cursor);
		cursor.close();
		return newServer;
	}

	public void deleteServer(Server server) {
		long id = server.getId();
		Log.v(TAG, "Server deleted with id: " + id);
		database.delete(MySQLiteHelper.TABLE_SERVERS, MySQLiteHelper.COLUMN_ID
				+ " = " + id, null);
	}
	
	public Server getServer(Long id) {
		Log.v(TAG, "Server deleted with id: " + id);
		Server newServer = null;
		
		Cursor cursor = database.query(MySQLiteHelper.TABLE_SERVERS, allColumns, MySQLiteHelper.COLUMN_ID + " = " + id, null, null, null, null);
		if (cursor.moveToFirst()){
			newServer = cursorToServer(cursor);
		}
		cursor.close();
		
		return newServer;
	}

	public List<Server> getAllServers() {
		List<Server> servers = new ArrayList<Server>();

		Cursor cursor = database.query(MySQLiteHelper.TABLE_SERVERS,
				allColumns, null, null, null, null, null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			Server comment = cursorToServer(cursor);
			servers.add(comment);
			cursor.moveToNext();
		}
		cursor.close();
		return servers;
	}

	public List<Server> getMostRecentServers() {
		List<Server> servers = new ArrayList<Server>();

		String whereClause = MySQLiteHelper.COLUMN_DATE + " = (SELECT max(" + 
				MySQLiteHelper.COLUMN_DATE +") FROM "+MySQLiteHelper.TABLE_SERVERS+")";

		Cursor cursor = database.query(MySQLiteHelper.TABLE_SERVERS,
				allColumns, whereClause, null, null, null, null);

/*		This code creates problems dealing with empty cursors, changed for a simplier version	
 		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			Server s = cursorToServer(cursor);
			servers.add(s);
			cursor.moveToNext();
		}
*/
		while(cursor.moveToNext()){
			Server s = cursorToServer(cursor);
			servers.add(s);
		}
		
		cursor.close();
		return servers;
	}
	
	public Long getMostRecentDate() {
		String whereClause = MySQLiteHelper.COLUMN_DATE + " = (SELECT max(" + 
				MySQLiteHelper.COLUMN_DATE +") FROM "+MySQLiteHelper.TABLE_SERVERS+")";

		Cursor cursor = database.query(MySQLiteHelper.TABLE_SERVERS,
				allColumns, whereClause, null, null, null, null);

		if (cursor.moveToFirst()){
			Server s = cursorToServer(cursor);
			
			// Make sure to close the cursor
			cursor.close();
			return s.getDate();
		}
		
		return null;
	}

	private Server cursorToServer(Cursor cursor) {
		Server server = new Server();
		server.setId(cursor.getLong(0));

		
		Long addedOn = cursor.getLong(1);
		
		server.setDate(addedOn);
		server.setRegion(cursor.getString(2));
		server.setBaseURL(cursor.getString(3));
		server.setBounds(cursor.getString(4));
		server.setLanguage(cursor.getString(5));
		server.setContactName(cursor.getString(6));
		server.setContactEmail(cursor.getString(7));
		server.setCenter(cursor.getString(8));
		server.setZoom(cursor.getString(9));
		
		Log.d(OTPApp.TAG, "Retrieved '" + server.getRegion() + "' server date from SQLLite - " + addedOn);
		
		return server;
	}
}
