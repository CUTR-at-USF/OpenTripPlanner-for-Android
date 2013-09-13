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

package edu.usf.cutr.opentripplanner.android.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import android.content.Context;

import edu.usf.cutr.opentripplanner.android.R;

/**
 * @author Khoa Tran
 *
 */

public class DateTimeConversion {
	public static double getDuration(String startTimeText, String endTimeText){
		double duration=0;
		DateFormat formatter ; 
		Date startTime=null, endTime=null; 
//		2012-03-09T22:46:00-05:00
		formatter = new SimpleDateFormat("yyyy-MM-dd\'T\'HH:mm:ssZZ");
		try {
			startTime = (Date)formatter.parse(startTimeText);
			endTime = (Date)formatter.parse(endTimeText);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		duration = 0;
		if (startTime!=null && endTime!=null) {
			duration = (endTime.getTime() - startTime.getTime()) / 1000;
		}
		return duration;
	}
	
	/**
	 * 
	 * @param sec
	 * @return
	 */
	public static String getFormattedDurationText(long sec){
		String text = "";
		long h = sec/3600;
		if (h>=24)
			return null;
		long m = (sec%3600)/60;
		long s = (sec%3600)%60;
		if (h > 0){
			text += Long.toString(h) + "h" + " ";
		}
		text += Long.toString(m) + "m" + " ";
		text += Long.toString(s) + "s" + " ";
		return text;
	}
	
	/**
	 * 
	 * @param sec
	 * @return
	 */
	public static String getFormattedDurationTextNoSeconds(long sec){
		String text = "";
		long h = sec/3600;
		if (h>=24)
			return null;
		long m = (sec%3600)/60;
		if (h > 0){
			text += Long.toString(h) + "h" + " ";
			text += Long.toString(m) + "m" + " ";
		}
		else{
			text += Long.toString(m) + "min" + " ";
		}
		return text;
	}
	
	public static String getTimeWithContext(Context applicationContext, int offsetGMT, long time, boolean inLine){
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(applicationContext);
		DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(applicationContext);
		timeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		
		cal.setTimeInMillis(time);
		cal.add(Calendar.MILLISECOND, offsetGMT);
		
		if (inLine){
			if (DateTimeConversion.isToday(cal)){
				return (" " + applicationContext.getResources().getString(R.string.connector_time) + " " + timeFormat.format(cal.getTime()));	
			}
			else if (DateTimeConversion.isTomorrow(cal)){
				return (" " + "tomorrow" + " " + applicationContext.getResources().getString(R.string.connector_time) + " " + timeFormat.format(cal.getTime()));
			}
			else{
				return (" " + "on" + " " + dateFormat.format(cal.getTime()) +" " + applicationContext.getResources().getString(R.string.connector_time) + " " + timeFormat.format(cal.getTime()));
			}
		}
		else{
			if (DateTimeConversion.isToday(cal)){
				return (timeFormat.format(cal.getTime()));	
			}
			else{
				return (timeFormat.format(cal.getTime()) + " " + dateFormat.format(cal.getTime()));
			}
		}

	}
	
    public static boolean isToday(Calendar cal) {
    	Calendar actualTime = Calendar.getInstance();
    	
        return (actualTime.get(Calendar.ERA) == cal.get(Calendar.ERA) &&
        		actualTime.get(Calendar.YEAR) == cal.get(Calendar.YEAR) &&
				actualTime.get(Calendar.DAY_OF_YEAR) == cal.get(Calendar.DAY_OF_YEAR));
    }
    
    public static boolean isTomorrow(Calendar cal) {
    	Calendar tomorrowTime = Calendar.getInstance();
    	tomorrowTime.add(Calendar.DAY_OF_YEAR, 1);
    	
        return (tomorrowTime.get(Calendar.ERA) == cal.get(Calendar.ERA) &&
        		tomorrowTime.get(Calendar.YEAR) == cal.get(Calendar.YEAR) &&
				tomorrowTime.get(Calendar.DAY_OF_YEAR) == cal.get(Calendar.DAY_OF_YEAR));
    }
    
}
