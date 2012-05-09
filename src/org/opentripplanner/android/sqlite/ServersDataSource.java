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

package org.opentripplanner.android.sqlite;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import org.opentripplanner.android.model.Server;

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
	private String[] allColumns = { MySQLiteHelper.COLUMN_ID,
									MySQLiteHelper.COLUMN_DATE, 
									MySQLiteHelper.COLUMN_REGION,
									MySQLiteHelper.COLUMN_BASEURL,
									MySQLiteHelper.COLUMN_LANGUAGE,
									MySQLiteHelper.COLUMN_CONTACT};
	
	private static final String TAG = "OTP";

	public ServersDataSource(Context context) {
		dbHelper = new MySQLiteHelper(context);
	}

	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}

	public void close() {
		dbHelper.close();
	}

	public Server createServer(Server s) {
		ContentValues values = new ContentValues();
		values.put(MySQLiteHelper.COLUMN_REGION, s.getRegion());
		values.put(MySQLiteHelper.COLUMN_BASEURL, s.getBaseURL());
		values.put(MySQLiteHelper.COLUMN_LANGUAGE, s.getLanguage());
		values.put(MySQLiteHelper.COLUMN_CONTACT, s.getContact());
		
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
		// Make sure to close the cursor
		cursor.close();
		return servers;
	}

	private Server cursorToServer(Cursor cursor) {
		Server server = new Server();
		server.setId(cursor.getLong(0));
//		long millis = cursor.getLong(cursor.getColumnIndexOrThrow("added_on"));
		long millis = cursor.getLong(1);
		Date addedOn = new Date(millis);
		server.setDate(addedOn);
		server.setRegion(cursor.getString(2));
		server.setBaseURL(cursor.getString(3));
		server.setLanguage(cursor.getString(4));
		server.setContact(cursor.getString(5));
		return server;
	}
}
