package edu.usf.cutr.opentripplanner.android;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.GooglePlayServicesUtil;


public class AboutActivity extends FragmentActivity {

    public AboutActivity() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);
        TextView description = (TextView) findViewById(R.id.appDescription);
        TextView acknowledgements = (TextView) findViewById(R.id.acknowledgements);
        TextView license = (TextView) findViewById(R.id.license);
        Button playServicesButton = (Button) findViewById(R.id.playServicesButton);
        description.setMovementMethod(LinkMovementMethod.getInstance());
        acknowledgements.setMovementMethod(LinkMovementMethod.getInstance());
        license.setMovementMethod(LinkMovementMethod.getInstance());
        playServicesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AboutActivity.this, AboutPlayServicesActivity.class);
                startActivity(intent);
            }
        });
    }

}
