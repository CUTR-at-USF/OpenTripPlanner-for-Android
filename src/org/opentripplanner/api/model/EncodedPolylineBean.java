package org.opentripplanner.api.model;

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


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.osmdroid.util.GeoPoint;
import org.simpleframework.xml.Element;

/**
* A list of coordinates encoded as a string.
* 
* See <a href="http://code.google.com/apis/maps/documentation/polylinealgorithm.html">Encoded
* polyline algorithm format</a>
*/

public class EncodedPolylineBean implements Serializable {

   private static final long serialVersionUID = 1L;

   @Element
   private String points;

   private String levels;

   @Element
   private int length;

   public EncodedPolylineBean() {

   }

   public EncodedPolylineBean(String points, String levels, int length) {
       this.points = points;
       this.levels = levels;
       this.length = length;
   }

   /**
    * The encoded points of the polyline.
    */
   public String getPoints() {
       return points;
   }

   public void setPoints(String points) {
       this.points = points;
   }

   /**
    * Levels describes which points should be shown at various zoom levels. Presently, we show all
    * points at all zoom levels.
   */
   public String getLevels() {
       return levels;
   }

   public String getLevels(int defaultLevel) {
       if (levels == null) {
           StringBuilder b = new StringBuilder();
           String l = encodeNumber(defaultLevel);
           for (int i = 0; i < length; i++)
               b.append(l);
           return b.toString();
       }
       return levels;
   }

   public void setLevels(String levels) {
       this.levels = levels;
   }

   /**
    * The number of points in the string
    */
   public int getLength() {
       return length;
   }

   public void setLength(int length) {
       this.length = length;
   }

   private static String encodeNumber(int num) {

       StringBuffer encodeString = new StringBuffer();

       while (num >= 0x20) {
           int nextValue = (0x20 | (num & 0x1f)) + 63;
           encodeString.append((char) (nextValue));
           num >>= 5;
       }

       num += 63;
       encodeString.append((char) (num));

       return encodeString.toString();
   }
   
   //Borrowed from http://jeffreysambells.com/posts/2010/05/27/decoding-polylines-from-google-maps-direction-api-with-java/
   public static List<GeoPoint> decodePoly(String encoded) {

	    List<GeoPoint> poly = new ArrayList<GeoPoint>();
	    int index = 0, len = encoded.length();
	    int lat = 0, lng = 0;

	    while (index < len) {
	        int b, shift = 0, result = 0;
	        do {
	            b = encoded.charAt(index++) - 63;
	            result |= (b & 0x1f) << shift;
	            shift += 5;
	        } while (b >= 0x20);
	        int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
	        lat += dlat;

	        shift = 0;
	        result = 0;
	        do {
	            b = encoded.charAt(index++) - 63;
	            result |= (b & 0x1f) << shift;
	            shift += 5;
	        } while (b >= 0x20);
	        int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
	        lng += dlng;

	        GeoPoint p = new GeoPoint((int) (((double) lat / 1E5) * 1E6),
	             (int) (((double) lng / 1E5) * 1E6));
	        poly.add(p);
	    }

	    return poly;
	}

}