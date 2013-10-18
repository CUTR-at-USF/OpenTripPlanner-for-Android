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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * @author Khoa Tran
 *
 */

public class MySQLiteHelper extends SQLiteOpenHelper {

	public static final String TABLE_SERVERS = "Servers";
	public static final String COLUMN_ID = "ID";
	public static final String COLUMN_DATE = "DateAdded";
	public static final String COLUMN_REGION = "Region";
	public static final String COLUMN_BASEURL = "BaseURL";
	public static final String COLUMN_BOUNDS = "Bounds";
	public static final String COLUMN_CENTER = "Center";
	public static final String COLUMN_ZOOM = "Zoom";
	public static final String COLUMN_LANGUAGE = "Language";
	public static final String COLUMN_CONTACT_NAME = "ContactName";
	public static final String COLUMN_CONTACT_EMAIL = "ContactEmail";

	private static final String DATABASE_NAME = "OTPServers.db";
	private static final int DATABASE_VERSION = 2;
	
	private static MySQLiteHelper mInstance = null;

	// Database creation sql statement
	private static final String DATABASE_CREATE = "CREATE TABLE "
			+ TABLE_SERVERS + "( "
			+ COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ COLUMN_DATE + " INTEGER DEFAULT CURRENT_TIMESTAMP, "
			+ COLUMN_REGION + " TEXT NOT NULL, "
			+ COLUMN_BASEURL + " TEXT NOT NULL, "
			+ COLUMN_BOUNDS + " TEXT NOT NULL, "
			+ COLUMN_LANGUAGE + " TEXT NOT NULL, "
			+ COLUMN_CONTACT_NAME + " TEXT NOT NULL, "
			+ COLUMN_CONTACT_EMAIL + " TEXT NOT NULL, "
			+ COLUMN_CENTER + " TEXT NOT NULL, "
			+ COLUMN_ZOOM + " TEXT NOT NULL"
			+ ");";

	private MySQLiteHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	  
	 
    public static MySQLiteHelper getInstance(Context ctx) {
      
	    // Use the application context, which will ensure that you 
	    // don't accidentally leak an Activity's context.
	    // See this article for more information: http://bit.ly/6LRzfx
    	if (mInstance == null) {
    		mInstance = new MySQLiteHelper(ctx.getApplicationContext());
    	}
    	return mInstance;
    }


	@Override
	public void onCreate(SQLiteDatabase database) {
		database.execSQL(DATABASE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(MySQLiteHelper.class.getName(),
				"Upgrading database from version " + oldVersion + " to "
						+ newVersion + ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_SERVERS);
		onCreate(db);
	}

}