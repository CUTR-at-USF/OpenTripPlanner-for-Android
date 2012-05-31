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
import org.opentripplanner.api.model.Leg;

import edu.usf.cutr.opentripplanner.android.OnFragmentListener;
import edu.usf.cutr.opentripplanner.android.model.Direction;
import edu.usf.cutr.opentripplanner.android.model.OTPBundle;
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
import android.widget.ExpandableListView;
import android.widget.ImageButton;

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
				ofl.onMainFragmentSwitched(dlf);
			}
		};
		btnDisplayMap.setOnClickListener(oclDisplayDirection);
		
		OTPBundle otpBundle = fragmentListener.getOTPBundle();
		TextView fromHeader = (TextView)header.findViewById(R.id.fromHeader);
		if(fromHeader==null) Log.v(TAG, "null fromheader");
		if(otpBundle==null) Log.v(TAG, "null otpBundle");
		fromHeader.setText(otpBundle.getFromText());
		TextView toHeader = (TextView)header.findViewById(R.id.toHeader);
		toHeader.setText(otpBundle.getToText());

		ArrayList<Leg> currentItinerary = new ArrayList<Leg>();
		currentItinerary.addAll(fragmentListener.getCurrentItinerary());
		
		ArrayList<Direction> directions = new ArrayList<Direction>();
		ItineraryDecrypt itDecrypt = new ItineraryDecrypt(currentItinerary);
		ArrayList<Direction> tempDirections = itDecrypt.getDirections();
		if(tempDirections!=null && !tempDirections.isEmpty()){
			directions.addAll(tempDirections);
		}
		
		TextView totalDistanceHeader = (TextView)header.findViewById(R.id.totalDistanceHeader);
		totalDistanceHeader.setText(Double.toString(itDecrypt.getTotalDistance()));
		TextView timeTraveledHeader = (TextView)header.findViewById(R.id.timeTraveledHeader);
		Double d = itDecrypt.getTotalTimeTraveled();
		timeTraveledHeader.setText(getFormattedDuration(d.intValue()));
		
		// Populate list with our static array of titles.
		elv = getExpandableListView();

		Direction direction_data[] = directions.toArray(new Direction[directions.size()]);

//		DirectionListAdapter adapter = new DirectionListAdapter(this.getActivity(), 
//				R.layout.list_direction_item, direction_data);
		
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
	
	private String getFormattedDuration(int sec){
		String text = "";
		int h = sec/3600;
		if (h>=24)
			return null;
		int m = (sec%3600)/60;
		int s = (sec%3600)%60;
		text += Integer.toString(h) + "h" + " ";
		text += Integer.toString(m) + "m" + " ";
		text += Integer.toString(s) + "s" + " ";
		return text;
	}
}
