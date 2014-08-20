package edu.usf.cutr.opentripplanner.android;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import com.google.android.gms.common.GooglePlayServicesUtil;


public class AboutPlayServicesActivity extends FragmentActivity {

    public AboutPlayServicesActivity() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_playservices);
        TextView playServicesAcknowledgement = (TextView) findViewById(R.id.playServicesAcknowledgement);
        playServicesAcknowledgement
                .setText(GooglePlayServicesUtil.getOpenSourceSoftwareLicenseInfo(this));
    }

}
