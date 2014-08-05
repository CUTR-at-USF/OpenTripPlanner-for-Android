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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import edu.usf.cutr.opentripplanner.android.OTPApp;
import edu.usf.cutr.opentripplanner.android.model.Server;

/**
 * Local data storage for servers from the OTP Server Directory
 * https://docs.google.com/spreadsheet/ccc?key=0AgWy8ujaGosCdDhxTC04cUZNeHo0eGFBQTBpU2dxN0E&usp=sharing&authkey=CK-H__IP
 *
 * @author Khoa Tran
 */

public class ServersDataSource {

    // Database fields
    private SQLiteDatabase database;

    private OtpSQLiteHelper dbHelper;

    private static ServersDataSource mInstance = null;

    private String[] allColumns = {OtpSQLiteHelper.COLUMN_ID,
            OtpSQLiteHelper.COLUMN_DATE,
            OtpSQLiteHelper.COLUMN_REGION,
            OtpSQLiteHelper.COLUMN_BASEURL,
            OtpSQLiteHelper.COLUMN_BOUNDS,
            OtpSQLiteHelper.COLUMN_LANGUAGE,
            OtpSQLiteHelper.COLUMN_CONTACT_NAME,
            OtpSQLiteHelper.COLUMN_CONTACT_EMAIL,
            OtpSQLiteHelper.COLUMN_CENTER,
            OtpSQLiteHelper.COLUMN_ZOOM,
            OtpSQLiteHelper.COLUMN_OFFERS_BIKE_RENTAL};

    private ServersDataSource(Context context) {
        dbHelper = OtpSQLiteHelper.getInstance(context);
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

        if ((s.getRegion() != null) && (s.getBaseURL() != null) && (s.getBounds() != null)
                && (s.getCenter() != null) && (s.getZoom() != null) && (s.getLanguage() != null)
                && (s.getContactName() != null) && (s.getContactEmail() != null)
                && (s.getOffersBikeRental() != null)) {
            if (s.getDate() != null) {
                values.put(OtpSQLiteHelper.COLUMN_DATE, s.getDate());
                Log.d(OTPApp.TAG,
                        "Wrote '" + s.getRegion() + "' server date to SQLLite - " + s.getDate());
            }
            values.put(OtpSQLiteHelper.COLUMN_REGION, s.getRegion());
            values.put(OtpSQLiteHelper.COLUMN_BASEURL, s.getBaseURL());
            values.put(OtpSQLiteHelper.COLUMN_BOUNDS, s.getBounds());
            values.put(OtpSQLiteHelper.COLUMN_CENTER, s.getCenter());
            values.put(OtpSQLiteHelper.COLUMN_ZOOM, s.getZoom());
            values.put(OtpSQLiteHelper.COLUMN_LANGUAGE, s.getLanguage());
            values.put(OtpSQLiteHelper.COLUMN_CONTACT_NAME, s.getContactName());
            values.put(OtpSQLiteHelper.COLUMN_CONTACT_EMAIL, s.getContactEmail());
            values.put(OtpSQLiteHelper.COLUMN_OFFERS_BIKE_RENTAL, s.getOffersBikeRental());

            long insertId = database.insert(OtpSQLiteHelper.TABLE_SERVERS, null,
                    values);
            Cursor cursor = database.query(OtpSQLiteHelper.TABLE_SERVERS,
                    allColumns, OtpSQLiteHelper.COLUMN_ID + " = " + insertId, null,
                    null, null, null);
            cursor.moveToFirst();
            Server newServer = cursorToServer(cursor);
            cursor.close();
            return newServer;
        } else {
            return null;
        }
    }

    public void deleteServer(Server server) {
        long id = server.getId();
        Log.d(OTPApp.TAG, "Server deleted with id: " + id);
        database.delete(OtpSQLiteHelper.TABLE_SERVERS, OtpSQLiteHelper.COLUMN_ID
                + " = " + id, null);
    }

    public Server getServer(Long id) {
        Server newServer = null;

        Cursor cursor = database.query(OtpSQLiteHelper.TABLE_SERVERS, allColumns,
                OtpSQLiteHelper.COLUMN_ID + " = " + id, null, null, null, null);
        if (cursor.moveToFirst()) {
            newServer = cursorToServer(cursor);
        }
        cursor.close();

        if (newServer != null) {
            Log.d(OTPApp.TAG, "Found server with id: " + id);
        } else {
            Log.d(OTPApp.TAG, "Server with id " + id + " does not exist in database");
        }

        return newServer;
    }

    public List<Server> getAllServers() {
        List<Server> servers = new ArrayList<Server>();

        Cursor cursor = database.query(OtpSQLiteHelper.TABLE_SERVERS,
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

        String whereClause = OtpSQLiteHelper.COLUMN_DATE + " = (SELECT max(" +
                OtpSQLiteHelper.COLUMN_DATE + ") FROM " + OtpSQLiteHelper.TABLE_SERVERS + ")";

        Cursor cursor = database.query(OtpSQLiteHelper.TABLE_SERVERS,
                allColumns, whereClause, null, null, null, null);

        while (cursor.moveToNext()) {
            Server s = cursorToServer(cursor);
            servers.add(s);
        }

        cursor.close();
        return servers;
    }

    public Long getMostRecentDate() {
        String whereClause = OtpSQLiteHelper.COLUMN_DATE + " = (SELECT max(" +
                OtpSQLiteHelper.COLUMN_DATE + ") FROM " + OtpSQLiteHelper.TABLE_SERVERS + ")";

        Cursor cursor = database.query(OtpSQLiteHelper.TABLE_SERVERS,
                allColumns, whereClause, null, null, null, null);

        if (cursor.moveToFirst()) {
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
        server.setOffersBikeRental(cursor.getInt(10)>0);

        Log.d(OTPApp.TAG,
                "Retrieved '" + server.getRegion() + "' server date from SQLLite - " + addedOn);

        return server;
    }
}
