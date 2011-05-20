package org.opentripplanner.android;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MyLocationOverlay;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import au.com.bytecode.opencsv.CSVReader;

/*import com.google.wireless.gdata.client.GDataParserFactory;
import com.google.wireless.gdata.data.Entry;
import com.google.wireless.gdata.docs.SpreadsheetsClient;
import com.google.wireless.gdata.docs.SpreadsheetsClient.SpreadsheetEntry;
import com.google.wireless.gdata.parser.GDataParser;
import com.google.wireless.gdata.parser.ParseException;
import com.google.wireless.gdata.serializer.GDataSerializer;
import com.google.wireless.gdata2.client.GDataClient;
import com.google.wireless.gdata2.data.*;*/


import de.mastacode.http.Http;

public class MainActivity extends Activity {

	private MapView mv;
	private MapController mc;
	private MyLocationOverlay mlo;
	private MenuItem mGPS;
	private MenuItem mSettings;
	private MenuItem mExit;
	
	private ImageButton btnStartLocation;
	private ImageButton btnEndLocation;
	private Button btnPlanTrip;

	private static LocationManager locationManager;
	private static final int EXIT_ID = 1;
	private static final int GPS_ID = 2;
	private static final int SETTINGS_ID = 3;
	
	private static final String TAG = "MainActivity";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

		btnStartLocation = (ImageButton) findViewById(R.id.btnStartLocation);
		btnEndLocation = (ImageButton) findViewById(R.id.btnEndLocation);
		btnPlanTrip = (Button) findViewById(R.id.btnPlanTrip);
		
		btnStartLocation.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "Clicked!");
				final CharSequence[] items = {"Current Location", "Contact Address", "Point on Map"};

				AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
				builder.setTitle("Choose Start Location");
				builder.setItems(items, new DialogInterface.OnClickListener() {
				    public void onClick(DialogInterface dialog, int item) {
				        Toast.makeText(getApplicationContext(), items[item], Toast.LENGTH_SHORT).show();
				    }
				});
				AlertDialog alert = builder.create();
				alert.show();
			}
		});
		
		mv = (MapView) findViewById(R.id.mapview);
		mv.setBuiltInZoomControls(true);
		mv.setMultiTouchControls(true);

		mc = mv.getController();
		mc.setZoom(20);
		mc.setCenter(getLastLocation());

		// ---Add a location marker---
		// MapOverlay mapOverlay = new MapOverlay(this, getLastLocation());
		// List<Overlay> listOfOverlays = mv.getOverlays();
		// listOfOverlays.clear();
		// listOfOverlays.add(mapOverlay);
		// mv.invalidate();

		mlo = new MyLocationOverlay(this, mv);
		// mlo.enableCompass();
		mv.getOverlays().add(mlo);

		// GeoPoint point = getLastLocation();
		// OverlayItem overlayitem = new OverlayItem("Title!", "Description!",
		// point);
		// itemizedoverlay.addOverlay(overlayitem);
		// mapOverlays.add(itemizedoverlay);

		String Url = "https://spreadsheets.google.com/ccc?key=0AgWy8ujaGosCdDhxTC04cUZNeHo0eGFBQTBpU2dxN0E&hl=en&authkey=CK-H__IP";
		Url = "https://spreadsheets.google.com/pub?hl=en&hl=en&key=0AgWy8ujaGosCdDhxTC04cUZNeHo0eGFBQTBpU2dxN0E&single=true&gid=0&output=csv";
		/*GDataClient client = new com.google.android.common.gdata2.AndroidGDataClient(this);
		GDataParserFactory spreadsheetFactory = new 
		SpreadsheetsClient s = new SpreadsheetsClient(client, spreadsheetFactory));
		s.*/
		
		HttpClient client = new DefaultHttpClient();
		String result = "";
		try {
			result = Http.get(Url).use(client).charset("UTF-8").followRedirects(true).asString();
			Log.d(TAG, "Spreadsheet: " + result);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		List<Server> knownServers = new ArrayList<Server>();
		
		CSVReader reader = new CSVReader(new StringReader(result));
		try {
			List<String[]> entries = reader.readAll();
			for (String[] e : entries) {
				if(e[0] == "Region") {
					continue; //Ignore the first line of the file
				}
				Server s = new Server(e[0], e[1], e[2], e[3], e[4], e[5]);
				knownServers.add(s);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		//GDataClient client = new SpreadsheetGDataClient("test", "http", "http://www.google.com");
		//client.getMediaEntryAsStream(new URL(Url), arg1, arg2)
		
		//ServiceDataClient client = new ServiceDataC
		//GDataParserFactory spreadSheetFactory = new XmlSpreadsheetsGDataParserFactory(new XmlParserFactory)
		//SpreadsheetsClient c = new SpreadsheetsClient(client, spreadsheetFactory);
		
		Log.d(TAG, "Servers: " + knownServers.size());
	}

	@Override
	protected void onResume() {
		super.onResume();
		mlo.enableMyLocation();
		// mlo.enableFollowLocation();
		mlo.enableCompass();
	}

	@Override
	protected void onPause() {
		super.onPause();
		mlo.disableMyLocation();
		// mlo.disableFollowLocation();
		mlo.disableCompass();
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu pMenu) {
		mGPS = pMenu.add(0, GPS_ID, Menu.NONE, R.string.enable_gps);
		mSettings = pMenu.add(0, SETTINGS_ID, Menu.NONE, R.string.settings);
		mExit = pMenu.add(0, EXIT_ID, Menu.NONE, R.string.exit);
		mGPS.setIcon(android.R.drawable.ic_menu_compass);
		mSettings.setIcon(android.R.drawable.ic_menu_preferences);
		mExit.setIcon(android.R.drawable.ic_menu_close_clear_cancel);
		return true;
	}

	public boolean onPrepareOptionsMenu(final Menu pMenu) {
		if (isGPSEnabled()) {
			mGPS.setTitle(R.string.disable_gps);
		} else {
			mGPS.setTitle(R.string.enable_gps);
		}
		return super.onPrepareOptionsMenu(pMenu);
	}

	public boolean onOptionsItemSelected(final MenuItem pItem) {
		if (pItem == mExit) {
			this.finish();
			return true;
		} else if (pItem == mGPS) {
			Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
			startActivity(myIntent);
		} else if (pItem == mSettings) {
			//TODO - settings activity
		}

		return false;
	}

	/*
	 * Get the last location the phone was at Based off example at
	 * http://www.androidsnippets
	 * .com/get-the-phones-last-known-location-using-locationmanager
	 */
	private GeoPoint getLastLocation() {
		LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		List<String> providers = lm.getProviders(true);
		Location l = null;

		for (int i = providers.size() - 1; i >= 0; i--) {
			l = lm.getLastKnownLocation(providers.get(i));
			if (l != null)
				break;
		}

		if (l == null) {
			return new GeoPoint(0, 0);
		}

		return new GeoPoint(l);
	}

	private Boolean isGPSEnabled() {
		return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
	}

	class MapOverlay extends org.osmdroid.views.overlay.Overlay {

		public GeoPoint p;

		public MapOverlay(Context ctx) {
			super(ctx);
			// TODO Auto-generated constructor stub
		}

		public MapOverlay(MainActivity mainActivity, GeoPoint lastLocation) {
			super(mainActivity);
			p = lastLocation;
		}

		@Override
		protected void draw(Canvas c, MapView osmv, boolean shadow) {
			// super.draw(c, osmv, shadow);

			Point screenPts = new Point();
			osmv.getProjection().toPixels(p, screenPts);

			Bitmap bmp = BitmapFactory.decodeResource(getResources(),
					R.drawable.arrow);
			c.drawBitmap(bmp, screenPts.x, screenPts.y - 50, null);
			return;
		}

	}
}