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

import org.opentripplanner.routing.core.TraverseMode;
import org.opentripplanner.api.model.Itinerary;
import org.opentripplanner.api.model.Leg;

import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceManager;
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

import java.util.ArrayList;
import java.util.List;

import edu.usf.cutr.opentripplanner.android.OTPApp;
import edu.usf.cutr.opentripplanner.android.R;
import edu.usf.cutr.opentripplanner.android.listeners.OtpFragment;
import edu.usf.cutr.opentripplanner.android.model.Direction;
import edu.usf.cutr.opentripplanner.android.model.OTPBundle;
import edu.usf.cutr.opentripplanner.android.util.ConversionUtils;
import edu.usf.cutr.opentripplanner.android.util.DirectionExpandableListAdapter;
import edu.usf.cutr.opentripplanner.android.util.DirectionsGenerator;
import edu.usf.cutr.opentripplanner.android.util.ExpandableListFragment;

/**
 * This fragment shows the list of step-by-step directions for a planned trip
 *
 * @author Khoa Tran
 */

public class DirectionListFragment extends ExpandableListFragment {

    View header = null;

    private OtpFragment fragmentListener;

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
            setFragmentListener((OtpFragment) activity);
        } catch (ClassCastException e) {
            throw new ClassCastException(
                    activity.toString() + " must implement OtpFragment");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        View mainView = inflater.inflate(R.layout.direction, container, false);

        header = inflater.inflate(R.layout.list_direction_header, null);

        return mainView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ImageButton btnDisplayMap = (ImageButton) header.findViewById(R.id.btnDisplayMap);
        final OtpFragment ofl = this.getFragmentListener();
        final DirectionListFragment dlf = this;
        OnClickListener oclDisplayDirection = new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                ofl.onSwitchedToMainFragment(dlf);
            }
        };
        btnDisplayMap.setOnClickListener(oclDisplayDirection);

        fromHeader = (TextView) header.findViewById(R.id.fromHeader);
        toHeader = (TextView) header.findViewById(R.id.toHeader);
        departureTimeHeader = (TextView) header.findViewById(R.id.departureTimeHeader);
        arrivalTimeHeader = (TextView) header.findViewById(R.id.arrivalTimeHeader);

        if (savedInstanceState != null) {
            otpBundle = (OTPBundle) savedInstanceState
                    .getSerializable(OTPApp.BUNDLE_KEY_OTP_BUNDLE);
            fragmentListener.setOTPBundle(otpBundle);
        } else {
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
        DirectionsGenerator dirGen = new DirectionsGenerator(currentItinerary,
                getActivity().getApplicationContext());
        ArrayList<Direction> tempDirections = dirGen.getDirections();
        if (tempDirections != null && !tempDirections.isEmpty()) {
            directions.addAll(tempDirections);
        }

        final Activity activity = this.getActivity();
        Spinner itinerarySelectionSpinner = (Spinner) header.findViewById(R.id.itinerarySelection);

        String[] itinerarySummaryList = new String[itineraryList.size()];

        boolean isTransitIsTagSet = false;
        for (int i = 0; i < itinerarySummaryList.length; i++) {
            isTransitIsTagSet = false;
            Itinerary it = itineraryList.get(i);
            for (Leg leg : it.legs) {
                TraverseMode traverseMode = TraverseMode.valueOf(leg.mode);
                if (traverseMode.isTransit()) {
                    itinerarySummaryList[i] = ConversionUtils
                            .getRouteShortNameSafe(leg.routeShortName,
                                    leg.routeLongName,
                                    getActivity().getApplicationContext()) + ". ";
                    isTransitIsTagSet = true;
                    break;
                }
            }
            if (!isTransitIsTagSet) {
                itinerarySummaryList[i] = Integer.toString(i + 1)
                        + ".   ";//Shown index is i + 1, to use 1-based indexes for the UI instead of 0-based
            }
        }

        for (int i = 0; i < itinerarySummaryList.length; i++) {
            Itinerary it = itineraryList.get(i);
            long tripDuration = ConversionUtils.normalizeDuration(it.duration, getActivity().getApplicationContext());
            itinerarySummaryList[i] += getString(R.string.step_by_step_total_duration) + " " + ConversionUtils
                    .getFormattedDurationTextNoSeconds(tripDuration, false,
                            getActivity().getApplicationContext());
            if (isTransitIsTagSet) {
                itinerarySummaryList[i] += "   " + getString(R.string.step_by_step_walking_duration) + " "
                        + ConversionUtils.getFormattedDurationTextNoSeconds(it.walkTime, false,
                        getActivity().getApplicationContext());
            }
        }

        ArrayAdapter<String> itineraryAdapter = new ArrayAdapter<String>(activity,
                android.R.layout.simple_spinner_item, itinerarySummaryList);

        itineraryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        itinerarySelectionSpinner.setAdapter(itineraryAdapter);

        AdapterView.OnItemSelectedListener itinerarySpinnerListener
                = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (fragmentListener.getCurrentItineraryIndex() != position) {
                    fragmentListener.onItinerarySelected(position, 3);
                }

                setDepartureArrivalHeaders();

                if (!isFragmentFirstLoad) {
                    ArrayList<Direction> directions = new ArrayList<Direction>();
                    DirectionsGenerator dirGen = new DirectionsGenerator(
                            fragmentListener.getCurrentItinerary(),
                            getActivity().getApplicationContext());
                    ArrayList<Direction> tempDirections = dirGen.getDirections();
                    if (tempDirections != null && !tempDirections.isEmpty()) {
                        directions.addAll(tempDirections);
                    }

                    Direction direction_data[] = directions
                            .toArray(new Direction[directions.size()]);

                    DirectionExpandableListAdapter adapter = new DirectionExpandableListAdapter(
                            DirectionListFragment.this.getActivity(),
                            R.layout.list_direction_item, R.layout.list_subdirection_item,
                            direction_data);

                    elv.setAdapter(adapter);

                }
                openIfNonTransit();

                isFragmentFirstLoad = false;

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        };
        itinerarySelectionSpinner.setSelection(currentItineraryIndex);
        itinerarySelectionSpinner.setOnItemSelectedListener(itinerarySpinnerListener);

        // Populate list with our static array of titles.
        elv = getExpandableListView();

        Direction direction_data[] = directions.toArray(new Direction[directions.size()]);

        DirectionExpandableListAdapter adapter = new DirectionExpandableListAdapter(
                this.getActivity(),
                R.layout.list_direction_item, R.layout.list_subdirection_item, direction_data);

        elv.addHeaderView(header);

        elv.setAdapter(adapter);

        elv.setGroupIndicator(null); // Get rid of the down arrow

        openIfNonTransit();

        if (savedInstanceState == null) {
            if (otpBundle.isFromInfoWindow()) {
                elv.expandGroup(otpBundle.getCurrentStepIndex());
                elv.setSelectedGroup(otpBundle.getCurrentStepIndex());
                otpBundle.setFromInfoWindow(false);
            }
        }
    }

    private void openIfNonTransit() {
        List<Leg> legsList = fragmentListener.getCurrentItinerary();

        if (legsList.size() == 1) {
            Leg firstLeg = legsList.get(0);
            TraverseMode traverseMode = TraverseMode.valueOf(firstLeg.mode);
            if (!traverseMode.isTransit()) {
                elv.expandGroup(0);
            }
        }
    }

    private void setDepartureArrivalHeaders() {
        Itinerary actualItinerary = fragmentListener.getCurrentItineraryList()
                .get(fragmentListener.getCurrentItineraryIndex());

        if (!actualItinerary.legs.isEmpty()) {
            Leg firstLeg = actualItinerary.legs.get(0);
            Leg lastLeg = actualItinerary.legs.get((actualItinerary.legs.size() - 1));
            int agencyTimeZoneOffset = firstLeg.agencyTimeZoneOffset;
            long startTimeInSeconds = Long.parseLong(firstLeg.startTime);
            long endTimeInSeconds = Long.parseLong(lastLeg.endTime);

            departureTimeHeader.setText(ConversionUtils
                    .getTimeWithContext(getActivity().getApplicationContext(), agencyTimeZoneOffset,
                            startTimeInSeconds, false));

            arrivalTimeHeader.setText(ConversionUtils
                    .getTimeWithContext(getActivity().getApplicationContext(), agencyTimeZoneOffset,
                            endTimeInSeconds, false));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);

        bundle.putSerializable(OTPApp.BUNDLE_KEY_OTP_BUNDLE, otpBundle);
    }

    /**
     * @return the fragmentListener
     */
    public OtpFragment getFragmentListener() {
        return fragmentListener;
    }

    /**
     * @param fragmentListener the fragmentListener to set
     */
    public void setFragmentListener(OtpFragment fragmentListener) {
        this.fragmentListener = fragmentListener;
    }
}
