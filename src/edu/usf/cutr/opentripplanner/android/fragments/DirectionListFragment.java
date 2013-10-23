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

package edu.usf.cutr.opentripplanner.android.fragments;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import org.opentripplanner.routing.core.TraverseMode;
import org.opentripplanner.v092snapshot.api.model.Itinerary;
import org.opentripplanner.v092snapshot.api.model.Leg;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import edu.usf.cutr.opentripplanner.android.OTPApp;
import edu.usf.cutr.opentripplanner.android.R;
import edu.usf.cutr.opentripplanner.android.listeners.OnFragmentListener;
import edu.usf.cutr.opentripplanner.android.model.Direction;
import edu.usf.cutr.opentripplanner.android.model.OTPBundle;
import edu.usf.cutr.opentripplanner.android.util.DateTimeConversion;
import edu.usf.cutr.opentripplanner.android.util.DirectionExpandableListAdapter;
import edu.usf.cutr.opentripplanner.android.util.ExpandableListFragment;
import edu.usf.cutr.opentripplanner.android.util.ItineraryDecrypt;

/**
 * This fragment shows the list of step-by-step directions for a planned trip
 * 
 * @author Khoa Tran
 *
 */

public class DirectionListFragment extends ExpandableListFragment {

	View header = null;

	private OnFragmentListener fragmentListener;
		
	private ExpandableListView elv;
	
	private boolean isFragmentFirstLoad = true;
	
	TextView fromHeader;
	TextView toHeader;
	TextView departureTimeHeader;
	TextView arrivalTimeHeader;
	
	OTPBundle otpBundle;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			setFragmentListener((OnFragmentListener) activity);
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement OnFragmentListener");
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View mainView = inflater.inflate(R.layout.direction, container, false);

		header = inflater.inflate(R.layout.list_direction_header, null);

		return mainView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
				
		ImageButton btnDisplayMap = (ImageButton)header.findViewById(R.id.btnDisplayMap);
		final OnFragmentListener ofl = this.getFragmentListener();
		final DirectionListFragment dlf = this;
		OnClickListener oclDisplayDirection = new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				ofl.onSwitchedToMainFragment(dlf);
			}
		};
		btnDisplayMap.setOnClickListener(oclDisplayDirection);
		

		fromHeader = (TextView)header.findViewById(R.id.fromHeader);
		toHeader = (TextView)header.findViewById(R.id.toHeader);
		departureTimeHeader = (TextView)header.findViewById(R.id.departureTimeHeader);
		arrivalTimeHeader = (TextView)header.findViewById(R.id.arrivalTimeHeader);
		
		if (savedInstanceState != null){
			otpBundle = (OTPBundle) savedInstanceState.getSerializable(OTPApp.BUNDLE_KEY_OTP_BUNDLE);
			fragmentListener.setOTPBundle(otpBundle);
		}
		else{
			otpBundle = fragmentListener.getOTPBundle();
		}
		
	 	fromHeader.setText(otpBundle.getFromText());
	 	toHeader.setText(otpBundle.getToText());
		setDepartureArrivalHeaders();

		ArrayList<Leg> currentItinerary = new ArrayList<Leg>();
		currentItinerary.addAll(fragmentListener.getCurrentItinerary());
		ArrayList<Itinerary> itineraryList = new ArrayList<Itinerary>();
		itineraryList.addAll(fragmentListener.getCurrentItineraryList());
		int currentItineraryIndex = fragmentListener.getCurrentItineraryIndex();
		
		ArrayList<Direction> directions = new ArrayList<Direction>();
		ItineraryDecrypt itDecrypt = new ItineraryDecrypt(currentItinerary, getActivity().getApplicationContext());
		ArrayList<Direction> tempDirections = itDecrypt.getDirections();
		if(tempDirections!=null && !tempDirections.isEmpty()){
			directions.addAll(tempDirections);
		}
		
		
		final Activity activity = this.getActivity();
		Spinner itinerarySelectionSpinner = (Spinner) header.findViewById(R.id.itinerarySelection);
		
		String[] itinerarySummaryList = new String[itineraryList.size()];
		for(int i=0; i<itinerarySummaryList.length; i++){
			Itinerary it = itineraryList.get(i);
			itinerarySummaryList[i] = Integer.toString(i+1) + ".   ";//Shown index is i + 1, to use 1-based indexes for the UI instead of 0-based
			itinerarySummaryList[i] += getString(R.string.total_duration) + " " + DateTimeConversion.getFormattedDurationText(it.duration/1000, getActivity().getApplicationContext());
			itinerarySummaryList[i] +=  "   " + getString(R.string.walking_duration) + " " + DateTimeConversion.getFormattedDurationText(it.walkTime, getActivity().getApplicationContext());
		}
		
		ArrayAdapter<String> itineraryAdapter = new ArrayAdapter<String>(activity, android.R.layout.simple_spinner_item, itinerarySummaryList);

		itineraryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		itinerarySelectionSpinner.setAdapter(itineraryAdapter);
		
		AdapterView.OnItemSelectedListener itinerarySpinnerListener = new AdapterView.OnItemSelectedListener() {
		    @Override
		    public void  onItemSelected (AdapterView<?> parent, View view, int position, long id){
//		    	Toast.makeText(parent.getContext(), 
//		    				   Long.toString(id) + " chosen " +
//		    				   parent.getItemAtPosition(position).toString(), 
//		    				   Toast.LENGTH_SHORT).show();
		    	fragmentListener.onItinerarySelected(position);
		    	
		    	setDepartureArrivalHeaders();

		    	if(!isFragmentFirstLoad){
					ArrayList<Direction> directions = new ArrayList<Direction>();
					ItineraryDecrypt itDecrypt = new ItineraryDecrypt(fragmentListener.getCurrentItinerary(), getActivity().getApplicationContext());
					ArrayList<Direction> tempDirections = itDecrypt.getDirections();
					if(tempDirections!=null && !tempDirections.isEmpty()){
						directions.addAll(tempDirections);
					}
					

					Direction direction_data[] = directions.toArray(new Direction[directions.size()]);
					
					DirectionExpandableListAdapter adapter = new DirectionExpandableListAdapter(DirectionListFragment.this.getActivity(), 
							R.layout.list_direction_item, direction_data);


					elv.setAdapter(adapter);
					
		    	}
		    	openIfNonTransit();
		    	
				isFragmentFirstLoad = false;
		    	
		    }

		    @Override
		    public void onNothingSelected (AdapterView<?> parent) {
		    	
		    }
		};
		itinerarySelectionSpinner.setSelection(currentItineraryIndex);
		itinerarySelectionSpinner.setOnItemSelectedListener(itinerarySpinnerListener);
		
//		TextView totalDistanceHeader = (TextView)header.findViewById(R.id.totalDistanceHeader);
//		totalDistanceHeader.setText(Double.toString(itDecrypt.getTotalDistance()));
//		TextView timeTraveledHeader = (TextView)header.findViewById(R.id.timeTraveledHeader);
//		Double d = itDecrypt.getTotalTimeTraveled();
//		timeTraveledHeader.setText(getFormattedDuration(d.intValue()));
		
		// Populate list with our static array of titles.
		elv = getExpandableListView();

		Direction direction_data[] = directions.toArray(new Direction[directions.size()]);
		
		DirectionExpandableListAdapter adapter = new DirectionExpandableListAdapter(this.getActivity(), 
				R.layout.list_direction_item, direction_data);

		elv.addHeaderView(header);

		elv.setAdapter(adapter);
		
		elv.setGroupIndicator(null); // Get rid of the down arrow
		
		openIfNonTransit();
		
		if (savedInstanceState == null){
			if (otpBundle.isFromInfoWindow()){
				elv.expandGroup(otpBundle.getCurrentStepIndex());
				elv.setSelectedGroup(otpBundle.getCurrentStepIndex());
				otpBundle.setFromInfoWindow(false);
			}
		}
	}
	
	private void openIfNonTransit(){
		List<Leg> legsList = fragmentListener.getCurrentItinerary();
		
		if (legsList.size() == 1){
			Leg firstLeg = legsList.get(0);
			TraverseMode traverseMode = TraverseMode.valueOf((String) firstLeg.mode);
			if (!traverseMode.isTransit()){
				elv.expandGroup(0);
			}
		}
	}
	
	private void setDepartureArrivalHeaders(){
	 	Itinerary firstItinerary = fragmentListener.getCurrentItineraryList().get(fragmentListener.getCurrentItineraryIndex());
		
		int agencyTimeZoneOffset = 0;
		long startTimeInSeconds = -1;
		long endTimeInSeconds = -1;
		
		for (Leg leg : firstItinerary.legs){
			if (startTimeInSeconds == -1){
				startTimeInSeconds = Long.parseLong(leg.getStartTime());
			}
			if (leg.getAgencyTimeZoneOffset() != 0){
				agencyTimeZoneOffset = leg.getAgencyTimeZoneOffset();
			}
			endTimeInSeconds = Long.parseLong(leg.getEndTime());
		}
		
		
		departureTimeHeader.setText(DateTimeConversion.getTimeWithContext(getActivity().getApplicationContext(), agencyTimeZoneOffset, startTimeInSeconds, false));
		
		arrivalTimeHeader.setText(DateTimeConversion.getTimeWithContext(getActivity().getApplicationContext(), agencyTimeZoneOffset, endTimeInSeconds, false));
	}

	@Override
	public void onSaveInstanceState(Bundle bundle) {
		super.onSaveInstanceState(bundle);

		bundle.putSerializable(OTPApp.BUNDLE_KEY_OTP_BUNDLE, otpBundle);
	}

	@Override
	public void onListItemClick(ExpandableListView l, View v, int pos, long id) {
		showDetails(pos);
	}

	/**
	 * Helper function to show the details of a selected item, either by
	 * displaying a fragment in-place in the current UI, or starting a
	 * whole new activity in which it is displayed.
	 */
	void showDetails(int index) {
	}

	/**
	 * @return the fragmentListener
	 */
	public OnFragmentListener getFragmentListener() {
		return fragmentListener;
	}

	/**
	 * @param fragmentListener the fragmentListener to set
	 */
	public void setFragmentListener(OnFragmentListener fragmentListener) {
		this.fragmentListener = fragmentListener;
	}
}
