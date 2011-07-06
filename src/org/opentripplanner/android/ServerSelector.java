package org.opentripplanner.android;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.osmdroid.util.GeoPoint;
import de.mastacode.http.Http;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import au.com.bytecode.opencsv.CSVReader;

public class ServerSelector extends AsyncTask<GeoPoint, Integer, Long> {
		private Server selectedServer;
		private static final String TAG = "OTP";
		private ProgressDialog progressDialog;
		private MainActivity activity;

		public ServerSelector(MainActivity activity) {
			this.activity = activity;
			progressDialog = new ProgressDialog(activity);
		}

		protected void onPreExecute() {
			progressDialog = ProgressDialog.show(activity, "",
					"Determining optimal server. Please wait ... ", true);
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
				app.setSelectedServer(selectedServer);
				Log.v(TAG, "Selected server: " + selectedServer.getRegion());
			} else {
				// TODO - handle errors here - prompt for manual sever entry?
				Log.e(TAG, "No server selected!");
			}
		}
		 
		private List<Server> getServerList() {
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
				return null;
			}
			
			List<Server> knownServers = new ArrayList<Server>();
			
			CSVReader reader = new CSVReader(new StringReader(result));
			try {
				List<String[]> entries = reader.readAll();
				for (String[] e : entries) {
					if(e[0] == "Region") {
						continue; //Ignore the first line of the file
					}
					Server s = new Server(e[0], e[1], e[2], e[3], e[4], e[5]);
					knownServers.add(s);
				}
			} catch (IOException e) {
				Log.e(TAG, "Problem reading CSV server file: " + e.getMessage());
				return null;
			}
			
			Log.d(TAG, "Servers: " + knownServers.size());
			
			return knownServers;
		}
		
		private Server findOptimalSever(GeoPoint currentLocation) {
			List<Server> knownServers = getServerList();
			
			if (knownServers == null || knownServers.size() < 1) {
				return null;
			}
			
			for (Server server : knownServers) {
				//server.getBounds()
				//TODO - check bounds here to find server
				if (server.getRegion().equalsIgnoreCase("Tampa")) {
					return server;
				}
			}
			
			return null;
		}
	}
