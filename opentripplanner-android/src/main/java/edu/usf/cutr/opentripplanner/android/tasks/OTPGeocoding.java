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

package edu.usf.cutr.opentripplanner.android.tasks;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.ArrayList;

import edu.usf.cutr.opentripplanner.android.OTPApp;
import edu.usf.cutr.opentripplanner.android.R;
import edu.usf.cutr.opentripplanner.android.listeners.OTPGeocodingListener;
import edu.usf.cutr.opentripplanner.android.model.Server;
import edu.usf.cutr.opentripplanner.android.util.LocationUtil;
import edu.usf.cutr.opentripplanner.android.util.CustomAddress;

/**
 * @author Khoa Tran
 */

public class OTPGeocoding extends IntentService {
    Server selectedServer;
    boolean geocodingForMarker;
    ArrayList<CustomAddress> addressesReturn;

    public OTPGeocoding() {
        super("OTPGeocoding");
    }

    public void onHandleIntent(Intent intent) {
        this.selectedServer = (Server) intent.getSerializableExtra("selectedServer");
        this.geocodingForMarker = intent.getBooleanExtra("geocodingForMarker", false);
        String[] reqs = intent.getStringArrayExtra("params");
        long count = reqs.length;
        addressesReturn = LocationUtil.processGeocoding(this, selectedServer, geocodingForMarker, reqs);
        Intent resultIntent = new Intent(intent.getStringExtra("FILTER"));
        resultIntent.putExtra("count", count);
        resultIntent.putExtra("addressesReturn", addressesReturn);
        LocalBroadcastManager.getInstance(this).sendBroadcast(resultIntent);
    }

    public static class OTPGeocodingReceiver extends BroadcastReceiver {

        private boolean isStartTextbox;

        private OTPGeocodingListener callback;

        private boolean geocodingForMarker;

        private ArrayList<CustomAddress> addressesReturn;

        public OTPGeocodingReceiver(boolean isStartTextbox, boolean geocodingForMarker, 
                OTPGeocodingListener callback) {
            this.isStartTextbox = isStartTextbox;
            this.callback = callback;
            this.geocodingForMarker = geocodingForMarker;
        }

        public void onReceive(Context receiverContext, Intent receiverIntent) {
            Long result = (Long) receiverIntent.getSerializableExtra("count");
            addressesReturn = (ArrayList<CustomAddress>) receiverIntent.getSerializableExtra("addressesReturn");
            callback.onOTPGeocodingComplete(isStartTextbox, addressesReturn, geocodingForMarker);
        }
    }
}
