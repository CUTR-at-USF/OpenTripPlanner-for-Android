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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.CalendarContract;
import android.support.v4.widget.PopupMenuCompat;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.opentripplanner.api.model.Itinerary;
import org.opentripplanner.api.model.Leg;
import org.opentripplanner.routing.core.TraverseMode;

import java.util.ArrayList;
import java.util.List;

import edu.usf.cutr.opentripplanner.android.AlarmActivity;
import edu.usf.cutr.opentripplanner.android.NotificationService;
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

    Spinner tripList;

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
        ImageButton btnShareDirections = (ImageButton) header.findViewById(R.id.btnShareDirections);
        ImageButton btnAlarmDirections = (ImageButton) header.findViewById(R.id.btnAlarmDirections);
        final OtpFragment ofl = this.getFragmentListener();
        final DirectionListFragment dlf = this;
        OnClickListener oclDisplayDirection = new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                ofl.onSwitchedToMainFragment(dlf);
            }
        };
        btnDisplayMap.setOnClickListener(oclDisplayDirection);
        btnShareDirections.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {

                // create popup menu
                View menuItemView = getView().findViewById(R.id.btnShareDirections);
                PopupMenu popup = new PopupMenu(getActivity(),menuItemView);
                popup.getMenuInflater().inflate(R.menu.share_menu, popup.getMenu());
                menuItemView.setOnTouchListener(PopupMenuCompat.getDragToOpenListener(popup));

                //registering popup with OnMenuItemClickListener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {

                        Intent itn = new Intent();
                        itn.setAction(Intent.ACTION_SEND);
                        itn.setType("text/plain");

                        // fill intend content based on chosen menu item
                        switch (item.getItemId()) {
                            case R.id.btnShareDirectionsShort:
                                itn.putExtra(Intent.EXTRA_TEXT, getDepartureArrivalHeaders(false));
                                break;
                            case R.id.btnShareDirectionsDetailed:
                                itn.putExtra(Intent.EXTRA_TEXT, getDepartureArrivalHeaders(true));
                                break;
                            default:
                                break;
                        }
                        startActivity(Intent.createChooser(itn, "Share via"));
                        return true;
                    }
                });

                popup.show();
            }
        });
        btnAlarmDirections.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {

                // create popup menu
                View menuItemView = getView().findViewById(R.id.btnAlarmDirections);
                PopupMenu popup = new PopupMenu(getActivity(),menuItemView);
                popup.getMenuInflater().inflate(R.menu.alarm_menu, popup.getMenu());
                menuItemView.setOnTouchListener(PopupMenuCompat.getDragToOpenListener(popup));

                //registering popup with OnMenuItemClickListener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {

                        switch (item.getItemId()) {
                            case R.id.btnAlarmDirectionsAlarm:
                                setAlarmItinerary();
                                break;
                            case R.id.btnAlarmDirectionsNotifications:
                                setNotificationsItinerary();
                                break;
                            case R.id.btnAlarmDirectionsCalendar:
                                setCalendarItinerary();
                                break;
                            default:
                                break;
                        }
                        return true;
                    }
                });

                popup.show();
            }
        });

        fromHeader = (TextView) header.findViewById(R.id.fromHeader);
        toHeader = (TextView) header.findViewById(R.id.toHeader);
        departureTimeHeader = (TextView) header.findViewById(R.id.departureTimeHeader);
        arrivalTimeHeader = (TextView) header.findViewById(R.id.arrivalTimeHeader);
        tripList = (Spinner) header.findViewById(R.id.itinerarySelection);

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
            long tripDuration;
            if (PreferenceManager.getDefaultSharedPreferences(
                    getActivity().getApplicationContext())
                    .getInt(OTPApp.PREFERENCE_KEY_API_VERSION, OTPApp.API_VERSION_V1)
                    == OTPApp.API_VERSION_V1){
                tripDuration = it.duration;
            }
            else{
                tripDuration = it.duration / 1000;
            }
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
        tripList.setAdapter(itineraryAdapter);

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
        tripList.setSelection(currentItineraryIndex);
        tripList.setOnItemSelectedListener(itinerarySpinnerListener);

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

    private String getDepartureArrivalHeaders(Boolean detailed) {

        if (detailed) {
            Toast.makeText(getActivity(), "..", Toast.LENGTH_SHORT).show();

            return "From: " + fromHeader.getText().toString()
              + "\n At:   " + departureTimeHeader.getText().toString()
              + "\n To:   " + toHeader.getText().toString()
              + "\n At:   " + arrivalTimeHeader.getText().toString();
        } else {
            return "From: " + fromHeader.getText().toString() + " - " + departureTimeHeader.getText().toString()
              + "\n To:   " + toHeader.getText().toString() + " - " + arrivalTimeHeader.getText().toString();
        }
    }

    private void setAlarmItinerary() {
        Intent intent = new Intent(getActivity().getApplicationContext(), AlarmActivity.class);
        intent.putExtra(OTPApp.BUNDLE_KEY_RESULT_TRIP_START_LOCATION, fromHeader.getText().toString());
        intent.putExtra(OTPApp.BUNDLE_KEY_RESULT_TRIP_END_LOCATION, toHeader.getText().toString());
        startActivity(intent);
    }

    private void setNotificationsItinerary() {
        // Notification title: short trip description
        String departure = (fromHeader.getText().toString().split(","))[0];
        String arrival = (toHeader.getText().toString().split(","))[0];
        String tripDescription = departure + " → " + arrival;

        // Notification content: time remaining before departure; the text contains hour and minutes separated by a :
        // The minutes have a space at the end that must be removed in order to keep the app alive
        int tripHour = Integer.parseInt((departureTimeHeader.getText().toString()).split(":")[0]);
        int tripMinutes = Integer.parseInt(((departureTimeHeader.getText().toString()).split(":")[1]).substring(0,2));

        Intent intent = new Intent(getActivity().getApplicationContext(), NotificationService.class);
        intent.putExtra(OTPApp.TRIP_DESCRIPTION, tripDescription);
        intent.putExtra(OTPApp.BUNDLE_KEY_TRIP_TIME, departureTimeHeader.getText().toString().substring(0,5));
        intent.putExtra(OTPApp.BUNDLE_KEY_TRIP_HOUR, tripHour);
        intent.putExtra(OTPApp.BUNDLE_KEY_TRIP_MINUTES, tripMinutes);
        getActivity().startService(intent);
    }

    private void setCalendarItinerary() {
        // Obtaining of the selected itinerary, along with its relevant parameters
        int selecItinID = (int)tripList.getSelectedItemId();
        Itinerary itinerary = otpBundle.getItineraryList().get(selecItinID);
        long startTime = Long.parseLong(itinerary.startTime);
        long endTime = Long.parseLong(itinerary.endTime);
        String departure = (fromHeader.getText().toString().split(","))[0];
        String arrival = (toHeader.getText().toString().split(","))[0];
        String tripDescription = departure + " → " + arrival;

        Intent intent = new Intent(Intent.ACTION_INSERT);
        intent.setData(CalendarContract.Events.CONTENT_URI);
        intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startTime);
        intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime);
        intent.putExtra(CalendarContract.Events.TITLE, tripDescription);
        intent.putExtra(CalendarContract.Events.EVENT_LOCATION, departure);
        startActivity(intent);
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
