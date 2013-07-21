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

package edu.usf.cutr.opentripplanner.android;

import java.util.ArrayList;
import java.util.List;

import org.opentripplanner.v092snapshot.api.model.Itinerary;
import org.opentripplanner.v092snapshot.api.model.Leg;
import org.osmdroid.util.GeoPoint;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import edu.usf.cutr.opentripplanner.android.fragments.DirectionListFragment;
import edu.usf.cutr.opentripplanner.android.fragments.MainFragment;
import edu.usf.cutr.opentripplanner.android.listeners.OnFragmentListener;
import edu.usf.cutr.opentripplanner.android.model.OTPBundle;
import edu.usf.cutr.opentripplanner.android.sqlite.ServersDataSource;

/**
 * Main Activity for the OTP for Android app
 * 
 * @author Khoa Tran
 * @author Sean Barbeau (conversion to Jackson)
 * 
 */

public class MyActivity extends FragmentActivity implements OnFragmentListener{

	private List<Leg> currentItinerary = new ArrayList<Leg>();
	
	private List<Itinerary> currentItineraryList = new ArrayList<Itinerary>();
	
	private int currentItineraryIndex = -1;

	private OTPBundle bundle = null;

	private MainFragment mainFragment;

	private ServersDataSource datasource;

	private String TAG = "OTP";
	
	private String currentRequestString="";
	
	private boolean isButtonStartLocation = false;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		bundle = (OTPBundle)getLastCustomNonConfigurationInstance();
		setContentView(R.layout.activity);

		if (savedInstanceState != null) {
			mainFragment = (MainFragment) getSupportFragmentManager().findFragmentByTag("mainFragmentTag");//recuperar o tag adecuado e pillar ese fragment
	    }
		
		if(savedInstanceState==null){
			FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
			mainFragment = new MainFragment();
			fragmentTransaction.replace(R.id.mainFragment, mainFragment, "mainFragmentTag");
			fragmentTransaction.commit();
		}
		
		setDatasource(new ServersDataSource(this));
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		switch(requestCode) {
		case OTPApp.REFRESH_SERVER_LIST_REQUEST_CODE: 
			if (resultCode == RESULT_OK) {
				boolean shouldRefresh = data.getBooleanExtra(OTPApp.REFRESH_SERVER_RETURN_KEY, false);
				//				Toast.makeText(this, "Should server list refresh? " + shouldRefresh, Toast.LENGTH_LONG).show();
				if(shouldRefresh){
					mainFragment.processServerSelector(true);
				}
				break;
			}
		case OTPApp.CHOOSE_CONTACT_REQUEST_CODE:
			if(resultCode == RESULT_OK){
//				Log.v(TAG, "CHOOSE CONTACT RESULT OK");
				
				Uri contactData = data.getData();
				Cursor c =  managedQuery(contactData, null, null, null, null);
				if (c.moveToFirst()) {
				    String address = c.getString(c.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS));
				    mainFragment.setTextBoxLocation(address, isButtonStartLocation);
				    mainFragment.processAddress(isButtonStartLocation, address);
				}
				
				break;
			}
		}
	}
	
	@Override
	protected void onDestroy() {		
		
		mainFragment = null;
		
		Log.d(TAG, "Released mainFragment with map in MyActivity.onDestroy()");
		
		super.onDestroy();
	}
	
	@Override
	public void onItinerariesLoaded(List<Itinerary> itineraries) {
		// TODO Auto-generated method stub
		currentItineraryList.clear();
		currentItineraryList.addAll(itineraries);
	}

	@Override
	public void onItinerarySelected(int i) {
		// TODO Auto-generated method stub
		if(i >= currentItineraryList.size()) return;
		
		currentItineraryIndex = i;
		currentItinerary.clear();
		currentItinerary.addAll(currentItineraryList.get(i).legs);
	}

	@Override
	public List<Leg> getCurrentItinerary() {
		// TODO Auto-generated method stub
		return currentItinerary;
	}

	@Override
	public void onSwitchedToDirectionFragment() {
		// TODO Auto-generated method stub
		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction transaction = fm.beginTransaction();

		Fragment directionFragment = new DirectionListFragment();
		transaction.add(R.id.mainFragment, directionFragment);
		transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		transaction.addToBackStack(null);

		transaction.commit();
	}

	@Override
	public OTPBundle getOTPBundle() {
		// TODO Auto-generated method stub
		return bundle;
	}

	@Override
	public void setOTPBundle(OTPBundle b) {
		// TODO Auto-generated method stub
		this.bundle = b;
		this.bundle.setCurrentItineraryIndex(currentItineraryIndex);
		this.bundle.setItineraryList(currentItineraryList);
	}

	@Override
	public void onSwitchedToMainFragment(Fragment f) {
		// TODO Auto-generated method stub
		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction transaction = fm.beginTransaction();
		transaction.remove(f);
		transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		fm.popBackStack();
		transaction.commit();
		
		mainFragment.showRouteOnMap(currentItinerary);
	}

	/**
	 * @return the datasource
	 */
	public ServersDataSource getDatasource() {
		return datasource;
	}

	/**
	 * @param datasource the datasource to set
	 */
	public void setDatasource(ServersDataSource datasource) {
		this.datasource = datasource;
	}

	@Override
	public void setCurrentRequestString(String url) {
		currentRequestString = url;
	}

	@Override
	public String getCurrentRequestString() {
		return currentRequestString;
	}
	
	@Override
	public void zoomToLocation(GeoPoint location){
		mainFragment.zoomToLocation(location);
	}
	
	@Override
	public void setMarker(GeoPoint location, boolean isStartMarker){
		mainFragment.setMarker(location, isStartMarker);
	}

	@Override
	public List<Itinerary> getCurrentItineraryList() {
		// TODO Auto-generated method stub
		return currentItineraryList;
	}

	@Override
	public int getCurrentItineraryIndex() {
		// TODO Auto-generated method stub
		return currentItineraryIndex;
	}

	/**
	 * @return the isButtonStartLocation
	 */
	public boolean isButtonStartLocation() {
		return isButtonStartLocation;
	}

	/**
	 * @param isButtonStartLocation the isButtonStartLocation to set
	 */
	public void setButtonStartLocation(boolean isButtonStartLocation) {
		this.isButtonStartLocation = isButtonStartLocation;
	}
}
