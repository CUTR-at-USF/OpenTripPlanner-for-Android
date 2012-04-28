package org.opentripplanner.android.fragments;

import java.util.ArrayList;

import org.opentripplanner.android.OnFragmentListener;
import org.opentripplanner.android.R;
import org.opentripplanner.android.model.Direction;
import org.opentripplanner.android.model.OTPBundle;
import org.opentripplanner.android.util.DirectionListAdapter;
import org.opentripplanner.android.util.ItineraryDecrypt;
import org.opentripplanner.api.model.Leg;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

public class DirectionListFragment extends ListFragment {

	View header = null;

	private OnFragmentListener fragmentListener;

	private static final String TAG = "OTP";

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
		OnClickListener oclDisplayDirection = new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				ofl.onMainFragmentSwitched();
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
		ListView listView1 = getListView();

		Direction direction_data[] = directions.toArray(new Direction[directions.size()]);

		DirectionListAdapter adapter = new DirectionListAdapter(this.getActivity(), 
				R.layout.list_direction_item, direction_data);

		listView1.addHeaderView(header);

		listView1.setAdapter(adapter);
	}

	@Override
	public void onListItemClick(ListView l, View v, int pos, long id) {
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
