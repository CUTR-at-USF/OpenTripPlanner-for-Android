package org.opentripplanner.android;

import java.util.List;

import org.opentripplanner.android.fragments.DirectionListFragment;
import org.opentripplanner.android.fragments.MainFragment;
import org.opentripplanner.android.model.OTPBundle;
import org.opentripplanner.api.model.Leg;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

public class MyActivity extends FragmentActivity implements OnFragmentListener{

	private List<Leg> currentItinerary = null;
	
	private OTPBundle bundle;
	
	private static Fragment mainFragment = new MainFragment();
	private static Fragment directionFragment = new DirectionListFragment();

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		bundle = (OTPBundle)getLastCustomNonConfigurationInstance();
		
		if(savedInstanceState==null){
			setContentView(R.layout.activity);
			FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
			fragmentTransaction.replace(R.id.mainFragment, mainFragment);
			fragmentTransaction.commit();
		}
	}

	@Override
	public void onItinerarySelected(List<Leg> l) {
		// TODO Auto-generated method stub
		currentItinerary = l;
	}
	
	@Override
	public List<Leg> getCurrentItinerary() {
		// TODO Auto-generated method stub
		return currentItinerary;
	}

	@Override
	public void onDirectionFragmentSwitched() {
		// TODO Auto-generated method stub
		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction transaction = fm.beginTransaction();
		transaction.replace(R.id.mainFragment, directionFragment);
		transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
//		transaction.addToBackStack(null);
		transaction.commit();
	}

	@Override
	public OTPBundle getOTPBundle() {
		// TODO Auto-generated method stub
		return bundle;
	}

	@Override
	public void setOTPBundle(OTPBundle b) {
		// TODO Auto-generated method stub
		this.bundle = b;
		this.bundle.setCurrentItinerary(currentItinerary);
	}

	@Override
	public void onMainFragmentSwitched() {
		// TODO Auto-generated method stub
		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction transaction = fm.beginTransaction();
		transaction.replace(R.id.mainFragment, mainFragment);
		transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		transaction.commit();
	}
	
//	@Override
//	public Object onRetainCustomNonConfigurationInstance (){
//		return bundle;
//	}
}
