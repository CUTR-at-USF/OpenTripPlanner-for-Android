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

package org.opentripplanner.android.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

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
}
