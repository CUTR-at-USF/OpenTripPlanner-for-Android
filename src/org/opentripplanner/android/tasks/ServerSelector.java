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

package org.opentripplanner.android.tasks;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.opentripplanner.android.MyActivity;
import org.opentripplanner.android.OTPApp;
import org.opentripplanner.android.OnFragmentListener;
import org.opentripplanner.android.model.Server;
import org.opentripplanner.android.sqlite.ServersDataSource;
import org.osmdroid.util.GeoPoint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.EditText;
import au.com.bytecode.opencsv.CSVReader;
import de.mastacode.http.Http;

/**
 * Modified by Khoa Tran
 *
 */

public class ServerSelector extends AsyncTask<GeoPoint, Integer, Long> {
		private Server selectedServer;
		private static List<Server> knownServers;
		private static final String TAG = "OTP";
		private ProgressDialog progressDialog;
		private MyActivity activity;

		public ServerSelector(MyActivity activity) {
			this.activity = activity;
			progressDialog = new ProgressDialog(activity);
		}

		protected void onPreExecute() {
			progressDialog = ProgressDialog.show(activity, "",
					"Detecting optimal server. Please wait...", true);
		}

		protected Long doInBackground(GeoPoint... currentLocation) {
			int count = currentLocation.length;
			long totalSize = 0;
			for (int i = 0; i < count; i++) {
				// totalSize += Downloader.downloadFile(reqs[i]);
				selectedServer = findOptimalSever(currentLocation[i]);
				// publishProgress((int) ((i / (float) count) * 100));
			}
			return totalSize;
		}

//		     protected void onProgressUpdate(Integer... progress) {
//		       //  setProgressPercent(progress[0]);
//		    	 setProgress(progress[0]);
//		    	 //TODO - fix tag
//		    	 Log.v(TAG, "Progress: " + progress[0]);
//		     }

		protected void onPostExecute(Long result) {
			if (progressDialog.isShowing()) {
				progressDialog.dismiss();
			}

			if (selectedServer != null) {
				OTPApp app = ((OTPApp) activity.getApplication());
				app.setSelectedServer(selectedServer, activity);
				Log.v(TAG, "Automatically selected server: " + selectedServer.getRegion());
			} else if (knownServers != null && knownServers.size() > 1){
				Log.w(TAG, "No server automatically selected!");
				
//				List<String> serverNames = new ArrayList<String>();
//				for (Server server : knownServers) {
//					serverNames.add(server.getRegion());
//				}
//				serverNames.add("Custom Server");
//				
//				final CharSequence[] items = serverNames.toArray(new CharSequence[serverNames.size()]);
//				
//				AlertDialog.Builder builder = new AlertDialog.Builder(activity);
//				builder.setTitle("Choose OpenTripPlanner Server");
//				builder.setItems(items, new DialogInterface.OnClickListener() {
//				    
//					public void onClick(DialogInterface dialog, int item) {
//				        
//				        if(items[item].equals("Custom Server")) {
//				        	final EditText tbBaseURL = new EditText(activity);
//
//				        	AlertDialog.Builder urlAlert = new AlertDialog.Builder(activity);
//				        	urlAlert.setTitle("Enter a custom OTP server domain");
//				        	urlAlert.setView(tbBaseURL);
//				        	urlAlert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
//				        		public void onClick(DialogInterface dialog, int whichButton) {
//				        			String value = tbBaseURL.getText().toString().trim();
//				        			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
//				        			Editor e = prefs.edit();
//				        			e.putBoolean("auto_detect_server", false);
//				        			e.putString("custom_server_url", value);
//				        			e.commit();
//				        		}
//				        	});
//				        	urlAlert.create().show();
//
//				        } else { 
//				        	//TODO - set server URL here - app wise as well?
//					        for (Server server : knownServers) {
//								if (server.getRegion().equals(items[item])) {
//									selectedServer = server;
//									OTPApp app = ((OTPApp) activity.getApplication());
//									app.setSelectedServer(selectedServer, activity);
//									break;
//								}
//							}
//					        //TODO - clear custom url pref here?
//				        }
//				        Log.v(TAG, "Chosen: " + items[item]);
//				    }
//				});
//				AlertDialog alert = builder.create();
//				alert.show();
				
			} else {
				//TODO - handle error here that server list cannot be loaded
				Log.e(TAG, "Server list could not be downloaded!!");
			}
		}
		 
		private void getServerList() {
			if(knownServers != null && knownServers.size() > 1) {
				Log.v(TAG, "KnownServers was already populated previously.");
				return;
			}
			//TODO - check if servers are stored in SQLite?
			
			//if severs are not stored, download list from the Google Spreadsheet
			
			//String Url = "https://spreadsheets.google.com/ccc?key=0AgWy8ujaGosCdDhxTC04cUZNeHo0eGFBQTBpU2dxN0E&hl=en&authkey=CK-H__IP";
			//Url = "https://spreadsheets.google.com/pub?hl=en&hl=en&key=0AgWy8ujaGosCdDhxTC04cUZNeHo0eGFBQTBpU2dxN0E&single=true&gid=0&output=csv";
			
			// NEW
			//https://spreadsheets.google.com/spreadsheet/pub?hl=en_US&hl=en_US&key=0AgWy8ujaGosCdDhxTC04cUZNeHo0eGFBQTBpU2dxN0E&single=true&gid=0&output=csv
			
			String Url = "https://spreadsheets.google.com/spreadsheet/pub?hl=en_US&hl=en_US&key=0AgWy8ujaGosCdDhxTC04cUZNeHo0eGFBQTBpU2dxN0E&single=true&gid=0&output=csv";

			HttpClient client = new DefaultHttpClient();
			String result = "";
			try {
				result = Http.get(Url).use(client).charset("UTF-8").followRedirects(true).asString();
				Log.d(TAG, "Spreadsheet: " + result);
			} catch (IOException e) {
				Log.e(TAG, "Unable to download spreadsheet with server list: " + e.getMessage());
				return;
			}
			
			List<Server> serverList = new ArrayList<Server>();
			
			CSVReader reader = new CSVReader(new StringReader(result));
			try {
				List<String[]> entries = reader.readAll();
				for (String[] e : entries) {
					if(e[0].equalsIgnoreCase("Region")) {
						continue; //Ignore the first line of the file
					}
					Server s = new Server(e[0], e[1], e[2], e[3], e[4], e[5]);
					serverList.add(s);
				}
			} catch (IOException e) {
				Log.e(TAG, "Problem reading CSV server file: " + e.getMessage());
				return;
			}
			
			Log.d(TAG, "Servers: " + serverList.size());
			
			if (knownServers != null) {
				knownServers.clear();
				knownServers.addAll(serverList);
			} else {
				knownServers = serverList;
			}
			
			ServersDataSource datasource = activity.getDatasource();
			for(int i=0; i<knownServers.size(); i++){
				datasource.createServer(knownServers.get(i));
			}
			
			return;
		}
		
		private Server findOptimalSever(GeoPoint currentLocation) {
			if(selectedServer != null) {
				return selectedServer;
			}
			
			getServerList();
			
			if (knownServers == null || knownServers.size() < 1) {
				return null;
			}
			
			for (Server server : knownServers) {
				//server.getBounds()
				//TODO - check bounds here to find server
				if (server.getRegion().equalsIgnoreCase("Tampa1")) {
					return server;
				}
			}
			
			return null;
		}
	}
