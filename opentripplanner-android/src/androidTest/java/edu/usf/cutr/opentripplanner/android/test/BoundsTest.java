package edu.usf.cutr.opentripplanner.android.test;

import com.google.android.gms.maps.model.LatLng;

import junit.framework.Assert;

import android.test.AndroidTestCase;

import edu.usf.cutr.opentripplanner.android.model.Server;
import edu.usf.cutr.opentripplanner.android.util.LocationUtil;

/**
 * Evaluates the LocationUtil.checkPointInBoundingBox() method with various bounds
 * and locations.
 * <p>
 * This method also works for servers crossing IDL.
 *
 * @author barbeau
 * @author Vreixo Gonzalez
 */
public class BoundsTest extends AndroidTestCase {

    public static final String TAG = "BoundsTest";

    Server serverTampa = new Server();

    Server serverPortland = new Server();

    Server serverGreenville = new Server();

    Server serverCalgary = new Server();

    Server serverPorto = new Server();

    Server serverPortlandBigBounds = new Server();

    // This servers test the behavior when crossing the IDL, which is the main reason to use the distances to check point bounds
    Server serverFijiIDL = new Server();

    Server serverChukotkaPeninsulaIDL = new Server();

    Server serverUnitedStates = new Server();

    @Override
    protected void setUp() throws Exception {
        // Tampa server
        serverTampa.setRegion("Tampa");
        serverTampa.setBaseURL("http://opentripplanner.usf.edu/opentripplanner-api-webapp/ws");
        serverTampa.setBounds("27.6236434,-82.8511308,28.3251809,-82.0559399");
        serverTampa.setLanguage("en_US");
        serverTampa.setContactName("Sean Barbeau");
        serverTampa.setContactEmail("opentripplanner@cutr.usf.edu");

        // Portland server
        serverPortland.setRegion("Portland");
        serverPortland.setBaseURL("http://rtp.trimet.org/opentripplanner-api-webapp/ws");
        serverPortland.setBounds("45.416,-122.839,45.609,-122.537");
        serverPortland.setLanguage("en_US");
        serverPortland.setContactName("Tech Support");
        serverPortland.setContactEmail("maptripplanner@trimet.org");

        // Greenville, SC server
        serverGreenville.setRegion("Greenville, SC");
        serverGreenville
                .setBaseURL("http://trip.greenvilleopenmap.info/opentripplanner-api-webapp/ws");
        serverGreenville.setBounds("34.50005,-82.9999831,35.043754,-81.800363");
        serverGreenville.setLanguage("en_US");
        serverGreenville.setContactName("Mike Nice");
        serverGreenville.setContactEmail("bikeoid@greenvilleopenmap.info");

        // Calgary, Canada server
        serverCalgary.setRegion("Calgary, Canada");
        serverCalgary
                .setBaseURL("http://gisciencegroup.ucalgary.ca:8080/opentripplanner-api-webapp/ws");
        serverCalgary.setBounds("50.7100302,-114.6108674,51.352879,-113.4783241");
        serverCalgary.setLanguage("en_CA");
        serverCalgary.setContactName("Stefan Steiniger");
        serverCalgary.setContactEmail("sstein@geo.uzh.ch");

        // Porto, Portugal server
        serverPorto.setRegion("Porto, Portugal");
        serverPorto.setBaseURL(
                "http://env-8084044.jelastic.servint.net/opentripplanner-api-webapp/ws");
        serverPorto.setBounds("40.96826,-8.77817,41.40688,-8.27958");
        serverPorto.setLanguage("pt_PT");
        serverPorto.setContactName("Ricardo Reis da Silva");
        serverPorto.setContactEmail("info@transportespublicos.pt");

        // Portland server big bounds
        serverPortlandBigBounds.setRegion("Portland big bounds");
        serverPortlandBigBounds.setBaseURL("http://rtp.trimet.org/opentripplanner-api-webapp/ws");
        serverPortlandBigBounds.setBounds("44.761538,-123.5271731,45.901268,-121.121062");
        serverPortlandBigBounds.setLanguage("en_US");
        serverPortlandBigBounds.setContactName("Tech Support");
        serverPortlandBigBounds.setContactEmail("maptripplanner@trimet.org");

        // FijiIDL server
        // This server tests the behavior when crossing the IDL, which is the main reason to use the distances to check point bounds
        serverFijiIDL.setRegion("FijiIDL");
        serverFijiIDL.setBaseURL("http://newserver.com");
        serverFijiIDL.setBounds("-17.0074,179.8261,-16.407,-179.8141");
        serverFijiIDL.setLanguage("en_US");
        serverFijiIDL.setContactName("Unknown");
        serverFijiIDL.setContactEmail("Unknown");

        // Chukotka Peninsula server
        // This server tests the behavior when crossing the IDL, which is the main reason to use the distances to check point bounds
        serverChukotkaPeninsulaIDL.setRegion("ChukotkaPeninsulaIDL");
        serverChukotkaPeninsulaIDL.setBaseURL("http://newserver.com");
        serverChukotkaPeninsulaIDL.setBounds("66.1433,179.0001,67.9183,-179.0124");
        serverChukotkaPeninsulaIDL.setLanguage("en_US");
        serverChukotkaPeninsulaIDL.setContactName("Unknown");
        serverChukotkaPeninsulaIDL.setContactEmail("Unknown");

        // United States
        serverUnitedStates.setRegion("United States");
        serverUnitedStates.setBaseURL("http://us-server.com");
        serverUnitedStates.setBounds("24.5454892,-124.16933,49.273757,-69.965541");
        serverUnitedStates.setLanguage("en_US");
        serverUnitedStates.setContactName("Unknown");
        serverUnitedStates.setContactEmail("Unknown");

        super.setUp();
    }

    public void testBoundsEvaluation() {
        // Tampa server
        LatLng pointInTampa1 = new LatLng(27.9710, -82.4650);
        Assert.assertTrue(
                LocationUtil.checkPointInBoundingBox(pointInTampa1, serverTampa));

        LatLng pointOutOfTampa1 = new LatLng(45.416, -122.839);
        Assert.assertFalse(LocationUtil
                .checkPointInBoundingBox(pointOutOfTampa1, serverTampa));

        // Portland server
        LatLng pointInPortland1 = new LatLng(45.518, -122.6800);
        Assert.assertTrue(LocationUtil
                .checkPointInBoundingBox(pointInPortland1, serverPortland));

        LatLng pointOutOfPortland1 = new LatLng(45.416, -122.939);
        Assert.assertFalse(LocationUtil
                .checkPointInBoundingBox(pointOutOfPortland1, serverPortland));

        // Greenville server
        LatLng pointInGreenville1 = new LatLng(34.84733, -82.39693);
        Assert.assertTrue(LocationUtil
                .checkPointInBoundingBox(pointInGreenville1, serverGreenville));

        LatLng pointOutOfGreenville1 = new LatLng(45.416, -122.839);
        Assert.assertFalse(LocationUtil
                .checkPointInBoundingBox(pointOutOfGreenville1, serverGreenville));

        // Calgary server
        LatLng pointInCalgary1 = new LatLng(50.7100302, -114.6108674);
        Assert.assertTrue(LocationUtil
                .checkPointInBoundingBox(pointInCalgary1, serverCalgary));

        LatLng pointOutOfCalgary1 = new LatLng(45.416, -122.839);
        Assert.assertFalse(LocationUtil
                .checkPointInBoundingBox(pointOutOfCalgary1, serverCalgary));

        // Porto server
        LatLng pointInPorto1 = new LatLng(41.1554, -8.6078);
        Assert.assertTrue(
                LocationUtil.checkPointInBoundingBox(pointInPorto1, serverPorto));

        LatLng pointOutOfPorto1 = new LatLng(45.416, -122.839);
        Assert.assertFalse(LocationUtil
                .checkPointInBoundingBox(pointOutOfPorto1, serverPorto));

        // Portland big bounds server
        LatLng pointInPortlandBigBounds1 = new LatLng(45.518, -122.6800);
        Assert.assertTrue(LocationUtil
                .checkPointInBoundingBox(pointInPortlandBigBounds1, serverPortlandBigBounds));

        LatLng pointOutOfPortlandBigBounds1 = new LatLng(27.9710, -82.4650);
        Assert.assertFalse(LocationUtil
                .checkPointInBoundingBox(pointOutOfPortlandBigBounds1, serverPortlandBigBounds));

        // FijiIDL server
        LatLng pointInFijiIDLPositive = new LatLng(-16.407, -179.8141);
        Assert.assertTrue(LocationUtil
                .checkPointInBoundingBox(pointInFijiIDLPositive, serverFijiIDL));

        LatLng pointOutOfFijiIDLPositive = new LatLng(-17.6995, 175.6581);
        Assert.assertFalse(LocationUtil
                .checkPointInBoundingBox(pointOutOfFijiIDLPositive, serverFijiIDL));

        LatLng pointInFijiIDLNegative = new LatLng(-16.9089, -179.8151);
        Assert.assertTrue(LocationUtil
                .checkPointInBoundingBox(pointInFijiIDLNegative, serverFijiIDL));

        LatLng pointOutOfFijiIDLNegative = new LatLng(-16.5907, -178.8570);
        Assert.assertFalse(LocationUtil
                .checkPointInBoundingBox(pointOutOfFijiIDLNegative, serverFijiIDL));

        // ChukotkaPeninsulaIDL server
        LatLng pointInChukotkaPeninsulaIDLPositive = new LatLng(66.8878, 179.1520);
        Assert.assertTrue(LocationUtil.checkPointInBoundingBox(pointInChukotkaPeninsulaIDLPositive,
                serverChukotkaPeninsulaIDL));

        LatLng pointOutOfChukotkaPeninsulaIDLPositive = new LatLng(-17.6995, 175.6581);
        Assert.assertFalse(LocationUtil
                .checkPointInBoundingBox(pointOutOfChukotkaPeninsulaIDLPositive,
                        serverChukotkaPeninsulaIDL));

        LatLng pointInChukotkaPeninsulaIDLNegative = new LatLng(67.2878, -179.7276);
        Assert.assertTrue(LocationUtil.checkPointInBoundingBox(pointInChukotkaPeninsulaIDLNegative,
                serverChukotkaPeninsulaIDL));

        LatLng pointOutOfChukotkaPeninsulaIDLNegative = new LatLng(67.2878, -178.7276);
        Assert.assertFalse(LocationUtil
                .checkPointInBoundingBox(pointOutOfChukotkaPeninsulaIDLNegative,
                        serverChukotkaPeninsulaIDL));

        // USA server
        LatLng pointInUsaPositive = new LatLng(28.05866283764341, -82.41802878677845);
        Assert.assertTrue(LocationUtil.checkPointInBoundingBox(pointInUsaPositive,
                serverUnitedStates));

        LatLng pointOutOfUsaPositive = new LatLng(67.2878, -178.7276);
        Assert.assertFalse(LocationUtil
                .checkPointInBoundingBox(pointOutOfUsaPositive, serverUnitedStates));
    }
}
