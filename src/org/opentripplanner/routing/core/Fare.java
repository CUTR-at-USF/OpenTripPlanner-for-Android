/* This program is free software: you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public License
 as published by the Free Software Foundation, either version 3 of
 the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>. */

package org.opentripplanner.routing.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;

import android.util.Log;

/**
 * <p><strong>Fare support has not yet been implemented.</strong>
 * </p><p>
 * Fare is a set of fares for different classes of users.</p>
 */
public class Fare {
    protected static final Logger LOGGER = Logger.getLogger(Fare.class.getCanonicalName());
    
    private static final String TAG = "OTP";

    public static enum FareType {
        regular, student, senior, tram, special
    }
    
    public static class Entry{
    	@Element
    	public FareType key = null;
    	@Element
    	public Money value = null;
    	
    	public Entry(){
    	}
    	
    	public Entry(FareType ft, Money m){
    		key = ft;
    		value = m;
    	}
    }
    
    /**
     * A mapping from {@link FareType} to {@link Money}.
     */
    @ElementList(required=false)
//    public HashMap<FareType, Money> fare;
    public ArrayList<Entry> fare;
    
//    @Element(required=false)
//    public Entry entry = new Entry();
    
//    @Element(required=false)
//    public void setEntry(Entry e){
//    	Log.v(TAG, "setEntry method");
//    	entry = e;
//    	fare.put(e.key, e.value);
//    }
    
//    @Element(required=false)
//    public Entry getEntry(){
//    	Log.v(TAG, "getEntry method");
//    	return entry;
//    }

    public Fare() {
    	Log.v(TAG, "Fare constructor");
//        fare = new HashMap<FareType, Money>();
    	fare = new ArrayList<Entry>();
    }

//    public void addFare(FareType fareType, WrappedCurrency currency, int cents) {
//    	Log.v(TAG, "add Fare");
//        fare.put(fareType, new Money(currency, cents));
//    }
    
    public void addFare(FareType fareType, WrappedCurrency currency, int cents) {
    	Log.v(TAG, "add Fare");
        fare.add(new Entry(fareType, new Money(currency, cents)));
    }
    
    public void addFare(Entry entry) {
    	Log.v(TAG, "add Fare");
    	fare.add(entry);
    }
    
    public Money getFare(FareType type) {
    	Log.v(TAG, "get Fare");
    	for(int i=0; i<fare.size(); i++){
    		Entry e = fare.get(i);
    		if(e.key.equals(type)) {
    			return e.value;
    		}
    	}
        return null;
    }
    
    public String toString() {
    	StringBuffer buffer = new StringBuffer("Fare(");
    	for (Entry en : fare) {
    		Money cost = en.value;
    		buffer.append("[");
    		buffer.append(en.key);
    		buffer.append(":");
    		buffer.append(cost.toString());
    		buffer.append("], ");
    	}
    	buffer.append(")");
    	return buffer.toString();
    }
    
//    public String toString() {
//        StringBuffer buffer = new StringBuffer("Fare(");
//        for (FareType type : fare.keySet()) {
//            Money cost = fare.get(type);
//            buffer.append("[");
//            buffer.append(type.toString());
//            buffer.append(":");
//            buffer.append(cost.toString());
//            buffer.append("], ");
//        }
//        buffer.append(")");
//        return buffer.toString();
//    }
}
