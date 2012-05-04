package org.opentripplanner.android.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

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
}
