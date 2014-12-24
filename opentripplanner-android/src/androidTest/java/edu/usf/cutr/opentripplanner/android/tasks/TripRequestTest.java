package edu.usf.cutr.opentripplanner.android.tasks;

import android.content.Context;
import android.content.res.Resources;
import android.test.ActivityTestCase;
import android.text.format.DateFormat;

import com.fasterxml.jackson.databind.ObjectMapper;

import junit.framework.Assert;

import org.opentripplanner.api.model.Leg;
import org.opentripplanner.api.model.Place;
import org.opentripplanner.api.model.TripPlan;
import org.opentripplanner.api.model.WalkStep;
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
import java.util.TimeZone;

import edu.usf.cutr.opentripplanner.android.OTPApp;
import edu.usf.cutr.opentripplanner.android.R;
import edu.usf.cutr.opentripplanner.android.util.JacksonConfig;

public class TripRequestTest extends ActivityTestCase {

    public void testGetErrorMessage() throws Exception {

    }
//TODO make test case less exhaustive and run with all servers
//    public void testRequestPlan() throws Exception {
//        Context context = getInstrumentation().getTargetContext();
//        Resources resources = context.getResources();
//        InputStream inputStream = resources.openRawResource(R.raw.test_trip_response_corunha);
//        Response savedResponse = JacksonConfig.getObjectReaderInstance().readValue(inputStream);
//        String baseURL = "http://galadriel.dc.fi.udc.es:8080/otp";
//        Request request = new Request();
//        double startLatitude = 43.3527384041191, startLlongitude = -8.409261703491211;
//        double endLatitude = 43.33304453039103, endLongitude = -8.411107063293457;
//        boolean arriveBy = false;
//        TraverseModeSet modes = new TraverseModeSet(TraverseMode.RAIL, TraverseMode.BUS, TraverseMode.WALK);
//        OptimizeType optimization = OptimizeType.QUICK;
//        String startLocation = startLatitude + "," + startLlongitude;
//        String endLocation = endLatitude + "," + endLongitude;
//
//        try {
//            request.setFrom(URLEncoder.encode(startLocation, OTPApp.URL_ENCODING));
//            request.setTo(URLEncoder.encode(endLocation, OTPApp.URL_ENCODING));
//        } catch (UnsupportedEncodingException e1) {
//            e1.printStackTrace();
//        }
//        request.setOptimize(optimization);
//        request.setModes(modes);
//        Date requestTripDate;
//        Calendar calendar = Calendar.getInstance();
//        calendar.set(2014, Calendar.SEPTEMBER, 4, 16, 49);
//        requestTripDate = calendar.getTime();
//        request.setDateTime(
//                DateFormat.format(OTPApp.FORMAT_OTP_SERVER_DATE_QUERY,
//                        requestTripDate.getTime()).toString(),
//                DateFormat
//                        .format(OTPApp.FORMAT_OTP_SERVER_TIME_QUERY, requestTripDate.getTime())
//                        .toString());
//        TripRequest tripRequest = new TripRequest(null, getActivity(), resources, null, null);
//        Response newResponse = tripRequest.requestPlan(request, OTPApp.FOLDER_STRUCTURE_PREFIX_NEW, baseURL);
//        TripPlan oldTripPlan = savedResponse.getPlan();
//        TripPlan newTripPlan = newResponse.getPlan();
//        ObjectMapper mapper = new ObjectMapper();
//        File file = new File(context.getFilesDir(), "test_trip_request_corunha.json");
//        mapper.writeValue(file, request);
//        if (newTripPlan != null && newTripPlan.getItinerary() != null){
//            Assert.assertEquals(oldTripPlan.getItinerary().size(),newTripPlan.getItinerary().size());
//            for (int i = 0; i < newTripPlan.getItinerary().size(); i++){
//                Itinerary oldItinerary = oldTripPlan.getItinerary().get(i);
//                Itinerary newItinerary = newTripPlan.getItinerary().get(i);
//                Assert.assertEquals(newItinerary.legs.size(), oldItinerary.legs.size());
//                for (int j = 0; j < newItinerary.legs.size(); j++){
//                    Leg oldLeg = oldItinerary.legs.get(j);
//                    Leg newLeg = newItinerary.legs.get(j);
//                    Assert.assertEquals(newLeg.from.getLat(), oldLeg.from.getLat());
//                    Assert.assertEquals(newLeg.from.getLon(), oldLeg.from.getLon());
//                    Assert.assertEquals(newLeg.to.getLat(), oldLeg.to.getLat());
//                    Assert.assertEquals(newLeg.to.getLon(), oldLeg.to.getLon());
//                    if (oldLeg.getSteps() != null && newLeg.getSteps() != null){
//                        Assert.assertEquals(newLeg.getSteps().size(), oldLeg.getSteps().size());
//                        for (int k = 0; k < newLeg.getSteps().size(); k++){
//                            WalkStep oldWalkStep = oldLeg.getSteps().get(k);
//                            WalkStep newWalkStep = newLeg.getSteps().get(k);
//                            Assert.assertEquals(oldWalkStep.getLat(),newWalkStep.getLat());
//                            Assert.assertEquals(oldWalkStep.getLon(),newWalkStep.getLon());
//                        }
//                    }
//                    else if (!(oldLeg.getSteps() == null && newLeg.getSteps() == null)){
//                        fail();
//                    }
//                    if (oldLeg.getIntermediateStops() != null && newLeg.getIntermediateStops() != null){
//                        Assert.assertEquals(newLeg.getIntermediateStops().size(), oldLeg.getIntermediateStops().size());
//                        for (int k = 0; k < newLeg.getSteps().size(); k++){
//                            Place oldPlace = oldLeg.getIntermediateStops().get(k);
//                            Place newPlace = newLeg.getIntermediateStops().get(k);
//                            Assert.assertEquals(oldPlace.getLat(),newPlace.getLat());
//                            Assert.assertEquals(oldPlace.getLon(),newPlace.getLon());
//                        }
//                    }
//                    else if (!(oldLeg.getIntermediateStops() == null && newLeg.getIntermediateStops() == null)){
//                        fail();
//                    }
//
//                }
//            }
//        }
//        else{
//            fail("Empty trip plan or itineraries");
//        }
//    }
}