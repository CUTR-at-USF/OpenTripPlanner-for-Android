package org.opentripplanner.android;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

/*public class NavigationService extends Service {

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate(){
		Toast.makeText(getApplicationContext(), "Service started!!", Toast.LENGTH_SHORT).show();
	}
	
	@Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("LocalService", "Received start id " + startId + ": " + intent);
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

	
	@Override
	public void onDestroy(){
		Toast.makeText(getApplicationContext(), "Service stopped!", Toast.LENGTH_SHORT).show();
	}
	
}
*/

public class NavigationService extends Service {
    private NotificationManager mNM;
    private Notification notification;

    // Unique Identification Number for the Notification.
    // We use it on Notification start, and to cancel it.
    private int NOTIFICATION = 123; //R.string.local_service_started;

    /**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
    public class LocalBinder extends Binder {
        NavigationService getService() {
            return NavigationService.this;
        }
    }

    @Override
    public void onCreate() {
        mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        // Display a notification about us starting.  We put an icon in the status bar.
        showNotification();
    }

   /* @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("LocalService", "Received start id " + startId + ": " + intent);
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }*/

    @Override
    public void onDestroy() {
        // Cancel the persistent notification.
        mNM.cancel(NOTIFICATION);

        // Tell the user we stopped.
        Toast.makeText(this, "Stopping service bro", Toast.LENGTH_SHORT).show();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    // This is the object that receives interactions from clients.  See
    // RemoteService for a more complete example.
    private final IBinder mBinder = new LocalBinder();

    /**
     * Show a notification while this service is running.
     */
    private void showNotification() {
        // In this sample, we'll use the same text for the ticker and the expanded notification
        CharSequence text ="Starting service";

        // Set the icon, scrolling text and timestamp
        notification = new Notification(R.drawable.arrow, text,
                System.currentTimeMillis());

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);

        // Set the info for the views that show in the notification panel.
        notification.setLatestEventInfo(this, "Large heading",
                       "extra info here", contentIntent);

        // Send the notification.
        mNM.notify(NOTIFICATION, notification);
    }
    
    public void updateNotification() {
    	PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);
    	notification.setLatestEventInfo(this, "Test heading", "small text", contentIntent);
    	mNM.notify(NOTIFICATION, notification);
    }
}