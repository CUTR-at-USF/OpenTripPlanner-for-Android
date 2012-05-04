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

/**
 * @author Khoa Tran
 *
 */

public class MyActivity extends FragmentActivity implements OnFragmentListener{

	private List<Leg> currentItinerary = null;
	
	private OTPBundle bundle;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		bundle = (OTPBundle)getLastCustomNonConfigurationInstance();
		
		if(savedInstanceState==null){
			setContentView(R.layout.activity);
			FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
			Fragment mainFragment = new MainFragment();
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
		Fragment directionFragment = new DirectionListFragment();
		transaction.replace(R.id.mainFragment, directionFragment);
		transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		transaction.addToBackStack(null);
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
		fm.popBackStack();
		Fragment mainFragment = new MainFragment();
		transaction.add(R.id.mainFragment, mainFragment);
		transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		transaction.commit();
	}
	
//	@Override
//	public Object onRetainCustomNonConfigurationInstance (){
//		return bundle;
//	}
}
