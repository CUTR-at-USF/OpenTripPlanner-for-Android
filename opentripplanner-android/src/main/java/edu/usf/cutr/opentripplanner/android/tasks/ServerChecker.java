/*
 * Copyright 2013 University of South Florida
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

package edu.usf.cutr.opentripplanner.android.tasks;

import java.io.IOException;
import java.lang.ref.WeakReference;

import org.apache.http.HttpStatus;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;
import de.mastacode.http.Http;
import edu.usf.cutr.opentripplanner.android.R;
import edu.usf.cutr.opentripplanner.android.listeners.ServerCheckerCompleteListener;
import edu.usf.cutr.opentripplanner.android.model.Server;

public class ServerChecker extends AsyncTask<Server, Long, String> {
	
	private static final String TAG = "OTP";
	private ProgressDialog progressDialog;
	private WeakReference<Activity> activity;
	private Context context;

	private ServerCheckerCompleteListener callback = null;
	
	private boolean showMessage;
	private boolean isWorking = false;
	
	/**
     * Constructs a new ServerChecker
     * @param 
     */
	public ServerChecker(WeakReference<Activity> activity, Context context, boolean showMessage) {
		this.activity = activity;
		this.context = context;
		this.showMessage = showMessage;
		if (activity.get() != null){
			progressDialog = new ProgressDialog(activity.get());
		}	
	}
	
	/**
     * Constructs a new ServerChecker
     * @param 
     */
	public ServerChecker(WeakReference<Activity> activity, Context context, ServerCheckerCompleteListener callback, boolean showMessage) {
		this.activity = activity;
		this.context = context;
		this.showMessage = showMessage;
		this.callback = callback;
		if (activity.get() != null){
			progressDialog = new ProgressDialog(activity.get());
		}	
	}
	
    @Override
	protected void onPreExecute() {
		if (activity.get() != null){
			progressDialog.setIndeterminate(true);
			progressDialog.setCancelable(true);
	    	progressDialog = ProgressDialog.show(activity.get(),"", context.getString(R.string.server_checker_progress), true);
		}
	}
	
    @Override
	protected String doInBackground(Server... params) {
		Server server = params[0];
		
		if (server == null) {
			Log.w(TAG,
					"Tried to get server info when no server was selected");
			cancel(true);
		}
		
		String message = context.getResources().getString(R.string.server_checker_info_dialog_region) + " "
				+ server.getRegion();
		message += "\n" + context.getResources().getString(R.string.server_checker_info_dialog_language) + " " + server.getLanguage();
		message += "\n" + context.getResources().getString(R.string.server_checker_info_dialog_contact) + " " + server.getContactName() + " ("
				+ server.getContactEmail() + ")";
		message += "\n" + context.getResources().getString(R.string.server_checker_info_dialog_url) + " " + server.getBaseURL();

		// TODO - fix server info bounds
		message += "\n" + context.getResources().getString(R.string.server_checker_info_dialog_bounds) + " " + server.getBounds();
		message += "\n" + context.getResources().getString(R.string.server_checker_info_dialog_reachable) + " ";

		int status = 0;
		try {
			HttpParams httpParameters = new BasicHttpParams();
			int timeoutConnection = context.getResources().getInteger(R.integer.connection_timeout);
			HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
			int timeoutSocket = context.getResources().getInteger(R.integer.socket_timeout);
			HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
			status = Http.get(server.getBaseURL() + "/plan")
					.use(new DefaultHttpClient(httpParameters)).asResponse()
					.getStatusLine().getStatusCode();
		} catch (IOException e) {
			Log.e(TAG, "Unable to reach server: " + e.getMessage());
			message = context.getResources().getString(R.string.server_checker_error_message) + " " + e.getMessage();
			return message;
		}

		if (status == HttpStatus.SC_OK) {
			message += context.getResources().getString(R.string.yes);
			isWorking = true;
		} else {
			message += context.getResources().getString(R.string.no);
		}

		return message;
	}

	@Override
	protected void onCancelled() {
		super.onCancelled();
		
		Toast.makeText(context, context.getResources().getString(R.string.info_server_error), Toast.LENGTH_SHORT).show();
	}

	@Override
	protected void onPostExecute(String result) {
		if (activity.get() != null){
			try{
				if (progressDialog != null && progressDialog.isShowing()) {
					progressDialog.dismiss();
				}
			}catch(Exception e){
				Log.e(TAG, "Error in Server Checker PostExecute dismissing dialog: " + e);
			}
		}

		if (showMessage){
			if (activity.get() != null){
				AlertDialog.Builder dialog = new AlertDialog.Builder(activity.get());
				dialog.setTitle(context.getResources().getString(R.string.server_checker_info_dialog_title));
				dialog.setMessage(result);
				dialog.setNeutralButton(context.getResources().getString(android.R.string.ok), null);
				dialog.create().show();
			}
		}
		else{
			if (isWorking){
				Toast.makeText(context, context.getResources().getString(R.string.server_checker_successful), Toast.LENGTH_SHORT).show();
			}
			else{
				Toast.makeText(context, context.getResources().getString(R.string.custom_server_error), Toast.LENGTH_SHORT).show();
			}
		}
		
		if (callback != null){
			callback.onServerCheckerComplete(result, isWorking);
		}
	}

}
