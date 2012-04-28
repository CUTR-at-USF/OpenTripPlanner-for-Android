///*
// * Copyright 2011 Marcy Gordon
// * 
// * Licensed under the Apache License, Version 2.0 (the "License"); you may not
// * use this file except in compliance with the License. You may obtain a copy of
// * the License at
// * 
// * http://www.apache.org/licenses/LICENSE-2.0
// * 
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
// * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
// * License for the specific language governing permissions and limitations under
// * the License.
// */
//
//package org.opentripplanner.android;
//
//import android.app.Notification;
//import android.app.NotificationManager;
//import android.app.PendingIntent;
//import android.app.Service;
//import android.content.Intent;
//import android.os.Binder;
//import android.os.IBinder;
//import android.util.Log;
//import android.widget.Toast;
//
//public class NavigationService extends Service {
//    private NotificationManager mNM;
//    private Notification notification;
//
//    private int NOTIFICATION = 123; //R.string.local_service_started;
//
//    public class LocalBinder extends Binder {
//        NavigationService getService() {
//            return NavigationService.this;
//        }
//    }
//
//    @Override
//    public void onCreate() {
//        mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
//
//        showNotification();
//    }
//
//   /* @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        Log.i("LocalService", "Received start id " + startId + ": " + intent);
//        // We want this service to continue running until it is explicitly
//        // stopped, so return sticky.
//        return START_STICKY;
//    }*/
//
//    @Override
//    public void onDestroy() {
//        mNM.cancel(NOTIFICATION);
//
//        Toast.makeText(this, "Stopping service bro", Toast.LENGTH_SHORT).show();
//    }
//
//    @Override
//    public IBinder onBind(Intent intent) {
//        return mBinder;
//    }
//
//    private final IBinder mBinder = new LocalBinder();
//
//    /**
//     * Show a notification while this service is running.
//     */
//    private void showNotification() {
//        CharSequence text ="Starting service";
//
//        notification = new Notification(R.drawable.arrow, text, System.currentTimeMillis());
//
//        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);
//
//        notification.setLatestEventInfo(this, "Large heading", "extra info here", contentIntent);
//
//        notification.flags |= Notification.FLAG_ONGOING_EVENT;
//        
//        mNM.notify(NOTIFICATION, notification);
//    }
//    
//    public void updateNotification() {
//    	PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
//                new Intent(this, MainActivity.class), 0);
//    	notification.setLatestEventInfo(this, "Test heading", "small text", contentIntent);
//    	mNM.notify(NOTIFICATION, notification);
//    }
//}