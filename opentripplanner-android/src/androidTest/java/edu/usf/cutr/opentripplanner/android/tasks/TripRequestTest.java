package edu.usf.cutr.opentripplanner.android.tasks;

import android.content.Context;
import android.content.res.Resources;
import android.test.ActivityTestCase;
import android.text.format.DateFormat;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import junit.framework.Assert;

import org.opentripplanner.api.model.TripPlan;
import org.opentripplanner.api.ws.Request;
import org.opentripplanner.api.model.Itinerary;
import org.opentripplanner.api.ws.Response;
import org.opentripplanner.routing.core.OptimizeType;
import org.opentripplanner.routing.core.TraverseMode;
import org.opentripplanner.routing.core.TraverseModeSet;

import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Date;

import edu.usf.cutr.opentripplanner.android.OTPApp;
import edu.usf.cutr.opentripplanner.android.R;
import edu.usf.cutr.opentripplanner.android.util.JacksonConfig;

public class TripRequestTest extends ActivityTestCase {

    public void testGetErrorMessage() throws Exception {

    }

    public void testRequestPlan() throws Exception {
        Context context = getInstrumentation().getTargetContext();
        Resources resources = context.getResources();

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        InputStream inputStreamSavedResponse = resources.openRawResource(R.raw.test_trip_response_corunha);
        InputStream inputStreamSavedRequest = resources.openRawResource(R.raw.test_trip_request_corunha);

        Response savedResponse = mapper.readValue(inputStreamSavedResponse, Response.class);
        Request savedRequest = mapper.readValue(inputStreamSavedRequest, Request.class);

        String baseURL = "http://galadriel.dc.fi.udc.es:8080/otp";

        Request newRequest = new Request();
        newRequest.setFrom(savedRequest.getFrom());
        newRequest.setTo(savedRequest.getTo());
        newRequest.setOptimize(savedRequest.getOptimize());
        newRequest.setModes(savedRequest.getModes());
        newRequest.setDateTime(savedRequest.getDateTime());

        TripRequest tripRequest = new TripRequest(null, context, resources, null, null);
        Response newResponse = tripRequest.requestPlan(newRequest, OTPApp.FOLDER_STRUCTURE_PREFIX_NEW, baseURL);

        if (newResponse.getPlan() != null && newResponse.getPlan().getItinerary() != null && newResponse.getPlan().getItinerary().get(0) != null){
            TripPlan tripPlan = newResponse.getPlan();
            Itinerary itinerary = tripPlan.getItinerary().get(0);
            Assert.assertEquals(itinerary.legs.get(0).getSteps().get(0).getLat(), savedResponse.getPlan().getItinerary().get(0).legs.get(0).getSteps().get(0).getLat());
        }
        else{
            fail("Empty trip plan or itineraries");
        }
    }
}