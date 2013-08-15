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

import org.opentripplanner.v092snapshot.api.model.Itinerary;
import org.opentripplanner.v092snapshot.api.model.Leg;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.CameraPosition;

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
import edu.usf.cutr.opentripplanner.android.MyActivity;
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
	
	private boolean isFragmentFirstLoaded = true;

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
		

		TextView fromHeader = (TextView)header.findViewById(R.id.fromHeader);
		TextView toHeader = (TextView)header.findViewById(R.id.toHeader);


		if (savedInstanceState != null){
			fromHeader.setText(savedInstanceState.getString(OTPApp.BUNDLE_KEY_TB_START_LOCATION));
			toHeader.setText(savedInstanceState.getString(OTPApp.BUNDLE_KEY_TB_END_LOCATION));
			OTPBundle otpBundleRestored = (OTPBundle) savedInstanceState.getSerializable(OTPApp.BUNDLE_KEY_OTP_BUNDLE);
			fragmentListener.onItinerariesLoaded(otpBundleRestored.getItineraryList());
			fragmentListener.onItinerarySelected(otpBundleRestored.getCurrentItineraryIndex());
		}
		else{
			OTPBundle otpBundle = fragmentListener.getOTPBundle();
			fromHeader.setText(otpBundle.getFromText());
			toHeader.setText(otpBundle.getToText());
		}
		
		
		ArrayList<Leg> currentItinerary = new ArrayList<Leg>();
		currentItinerary.addAll(fragmentListener.getCurrentItinerary());
		ArrayList<Itinerary> itineraryList = new ArrayList<Itinerary>();
		itineraryList.addAll(fragmentListener.getCurrentItineraryList());
		int currentItineraryIndex = fragmentListener.getCurrentItineraryIndex();
		
		ArrayList<Direction> directions = new ArrayList<Direction>();
		ItineraryDecrypt itDecrypt = new ItineraryDecrypt(currentItinerary);
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
			itinerarySummaryList[i] += getString(R.string.total_duration) + " " + DateTimeConversion.getFormattedDurationText(it.duration/1000);
		//	itinerarySummaryList[i] += "   " + Long.toString(it.walkTime) + " meters";-->VREIXO
			itinerarySummaryList[i] +=  "   " + getString(R.string.walking_duration) + " " + DateTimeConversion.getFormattedDurationText(it.walkTime);
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
		    	
		    	if(!isFragmentFirstLoaded){
		    		ofl.onSwitchedToMainFragment(dlf);
		    	}
		    	
		    	isFragmentFirstLoaded = false;
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
	}

	@Override
	public void onSaveInstanceState(Bundle bundle) {
		super.onSaveInstanceState(bundle);
		TextView tbStartLocation = (TextView)header.findViewById(R.id.fromHeader);
		TextView tbEndLocation = (TextView)header.findViewById(R.id.toHeader);
		bundle.putString(OTPApp.BUNDLE_KEY_TB_START_LOCATION, tbStartLocation.getText().toString());
		bundle.putString(OTPApp.BUNDLE_KEY_TB_END_LOCATION, tbEndLocation.getText().toString());
		OTPBundle otpBundle = new OTPBundle();
		otpBundle.setItineraryList(fragmentListener.getCurrentItineraryList());
		otpBundle.setCurrentItineraryIndex(fragmentListener.getCurrentItineraryIndex());
		otpBundle.setCurrentItinerary(fragmentListener.getCurrentItinerary());
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
