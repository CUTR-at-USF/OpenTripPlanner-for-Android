package edu.usf.cutr.opentripplanner.android.test;

import junit.framework.Assert;
import android.test.AndroidTestCase;
import edu.usf.cutr.opentripplanner.android.util.LocationUtil;
import edu.usf.cutr.opentripplanner.android.model.Server;

import com.google.android.gms.maps.model.LatLng;

/**
 * Evaluates the LocationUtils.checkPointInBoundingBox() method with various bounds
 * and locations
 * 
 * @author barbeau
 *
 */
public class BoundsTest extends AndroidTestCase {
	
	public static final String TAG = "BoundsTest";
    public static final int ACCEPTABLE_ERROR = 10; // meters
	Server serverTampa = new Server();
	
	@Override
	protected void setUp() throws Exception {
		// Tampa server
		serverTampa.setRegion("Tampa");
		serverTampa.setBaseURL("http://opentripplanner.usf.edu/opentripplanner-api-webapp/ws");
		serverTampa.setBounds("27.6236434,-82.8511308,28.3251809,-82.0559399");
		serverTampa.setLanguage("en_US");
		serverTampa.setContactName("Sean Barbeau");
		serverTampa.setContactEmail("opentripplanner@cutr.usf.edu");
				
		super.setUp();
	}
	
	public void testBoundsEvaluation(){
		// Tampa server
		LatLng pointInTampa1 = new LatLng(27.9710, -82.4650);		
		Assert.assertTrue(LocationUtil.checkPointInBoundingBox(pointInTampa1, serverTampa, ACCEPTABLE_ERROR));
		
		LatLng pointOutOfTampa1 = new LatLng(45.416, -122.839);		
		Assert.assertFalse(LocationUtil.checkPointInBoundingBox(pointOutOfTampa1, serverTampa, ACCEPTABLE_ERROR));
	}	
}
