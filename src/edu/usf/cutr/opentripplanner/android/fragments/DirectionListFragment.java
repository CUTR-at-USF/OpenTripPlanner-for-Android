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

import edu.usf.cutr.opentripplanner.android.R;

import org.opentripplanner.api.model.Itinerary;
import org.opentripplanner.api.model.Leg;
import org.opentripplanner.routing.core.OptimizeType;

import edu.usf.cutr.opentripplanner.android.listeners.OnFragmentListener;
import edu.usf.cutr.opentripplanner.android.model.Direction;
import edu.usf.cutr.opentripplanner.android.model.OTPBundle;
import edu.usf.cutr.opentripplanner.android.model.OptimizeSpinnerItem;
import edu.usf.cutr.opentripplanner.android.util.DateTimeConversion;
import edu.usf.cutr.opentripplanner.android.util.DirectionExpandableListAdapter;
import edu.usf.cutr.opentripplanner.android.util.ExpandableListFragment;
import edu.usf.cutr.opentripplanner.android.util.ItineraryDecrypt;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import android.widget.TextView;

/**
 * @author Khoa Tran
 *
 */

public class DirectionListFragment extends ExpandableListFragment {

	View header = null;

	private OnFragmentListener fragmentListener;

	private static final String TAG = "OTP";
	
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
	public void onActivityCreated(Bundle savedState) {
		super.onActivityCreated(savedState);
		
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
		
		OTPBundle otpBundle = fragmentListener.getOTPBundle();
		TextView fromHeader = (TextView)header.findViewById(R.id.fromHeader);
		fromHeader.setText(otpBundle.getFromText());
		TextView toHeader = (TextView)header.findViewById(R.id.toHeader);
		toHeader.setText(otpBundle.getToText());
		
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
			itinerarySummaryList[i] = Integer.toString(i) + ".   ";
			itinerarySummaryList[i] += DateTimeConversion.getFormattedDurationText(it.duration/1000);
			itinerarySummaryList[i] += "   " + Long.toString(it.walkTime) + " meters";
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
