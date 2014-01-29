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

package edu.usf.cutr.opentripplanner.android.util;

import java.util.Calendar;
import java.util.Date;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TimePicker;
import edu.usf.cutr.opentripplanner.android.MyActivity;
import edu.usf.cutr.opentripplanner.android.OTPApp;
import edu.usf.cutr.opentripplanner.android.R;
import edu.usf.cutr.opentripplanner.android.model.ArriveBySpinnerItem;

public class DateTimeDialog extends DialogFragment /* implements DatePickerCompleteListener, TimePickerCompleteListener*/{

	private Spinner spinScheduleType;
	private TimePicker pickerTime;
	private DatePicker pickerDate;
	private Button btnOk;
	private Button btnCancel;

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	
        View view = inflater.inflate(R.layout.date_time_dialog, container);
		
        spinScheduleType = (Spinner) view.findViewById(R.id.spinScheduleType);
        pickerTime = (TimePicker) view.findViewById(R.id.timePicker1);
		pickerDate = (DatePicker) view.findViewById(R.id.datePicker1);
		btnOk = (Button) view.findViewById(R.id.btnOk);
		btnCancel = (Button) view.findViewById(R.id.btnCancel);
				
        getDialog().setTitle(getResources().getString(R.string.date_time_title));
		getDialog().setCanceledOnTouchOutside(true);
        
		pickerTime.setIs24HourView(DateFormat.is24HourFormat(getActivity()));
		
		//TimePicker state needs to be saved manually because of this bug in Android that affects at least ICS: http://code.google.com/p/android/issues/detail?id=22754
	    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
	        pickerTime.setSaveFromParentEnabled(false);
	    }			
		
		OnClickListener oclOk = new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Calendar cal = Calendar.getInstance(); 

				cal.set(pickerDate.getYear(), pickerDate.getMonth(), pickerDate.getDayOfMonth(), pickerTime.getCurrentHour(), pickerTime.getCurrentMinute());
				ArriveBySpinnerItem selectedSscheduleType = (ArriveBySpinnerItem) spinScheduleType.getSelectedItem();
				((MyActivity) getActivity()).onDateComplete(cal.getTime(), selectedSscheduleType.getValue());
				dismiss();
			}
		};
		btnOk.setOnClickListener(oclOk);
		
		OnClickListener oclCancel = new OnClickListener() {
			
			@Override
			public void onClick(View v) {
			    dismiss();			
			}
		};
		btnCancel.setOnClickListener(oclCancel);
		
		ArrayAdapter<ArriveBySpinnerItem> arriveByTypeAdapter = new ArrayAdapter<ArriveBySpinnerItem>(
				getActivity(),
				android.R.layout.simple_spinner_dropdown_item,
				new ArriveBySpinnerItem[] {new ArriveBySpinnerItem(getResources().getString(R.string.date_time_spinner_depart), false), new ArriveBySpinnerItem(getResources().getString(R.string.date_time_spinner_arrive), true)});
		spinScheduleType.setAdapter(arriveByTypeAdapter);

        return view;
    }
    
    
    
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
    public void onActivityCreated(Bundle savedInstanceState){
    	super.onActivityCreated(savedInstanceState);
    	
    	Bundle bundle;

    	if (savedInstanceState == null){
    		bundle = this.getArguments();
    		Date tripDate = (Date) bundle.getSerializable(OTPApp.BUNDLE_KEY_TRIP_DATE);
        	boolean arriveBy = bundle.getBoolean(OTPApp.BUNDLE_KEY_ARRIVE_BY);

        	Calendar cal = Calendar.getInstance();
    	    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
	        	if (cal.getTimeInMillis() < tripDate.getTime()){
	        		//min time should preceede setted time and setted time will be set with 0 seconds, so we aloud one minute less
	        		pickerDate.setMinDate(cal.getTimeInMillis() - 60000);
	        	}
	        	else{
	        		//min time should preceede setted time and setted time will be set with 0 seconds, so we aloud one minute less
	        		pickerDate.setMinDate(tripDate.getTime() - 60000);
	        	}
    	    }		

        	cal.setTime(tripDate);
        	
    		if (!arriveBy){
    			spinScheduleType.setSelection(0);
    		}
    		else{
    			spinScheduleType.setSelection(1);
    		}
    		pickerTime.setCurrentHour(cal.get(Calendar.HOUR_OF_DAY));
    		pickerTime.setCurrentMinute(cal.get(Calendar.MINUTE));
    		pickerDate.updateDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
    	}
    	else{
    		//TimePicker state needs to be saved manually because of this bug in Android that affects at least ICS: http://code.google.com/p/android/issues/detail?id=22754
    		bundle = savedInstanceState;
    		pickerTime.setCurrentHour(bundle.getInt(OTPApp.BUNDLE_KEY_TIMEPICKER_SAVED_HOUR));
    		pickerTime.setCurrentMinute(bundle.getInt(OTPApp.BUNDLE_KEY_TIMEPICKER_SAVED_MINUTE));
    	}
    	
		
    }
    
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		//TimePicker state needs to be saved manually because of this bug in Android that affects at least ICS: http://code.google.com/p/android/issues/detail?id=22754
		savedInstanceState.putInt(OTPApp.BUNDLE_KEY_TIMEPICKER_SAVED_HOUR, pickerTime.getCurrentHour());
		savedInstanceState.putInt(OTPApp.BUNDLE_KEY_TIMEPICKER_SAVED_MINUTE, pickerTime.getCurrentMinute());
	}


}
