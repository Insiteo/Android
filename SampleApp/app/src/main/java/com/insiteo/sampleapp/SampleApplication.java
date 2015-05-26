package com.insiteo.sampleapp;

import android.widget.Toast;

import com.insiteo.lbs.beacon.ISBeaconApplication;
import com.insiteo.lbs.common.auth.entities.ISBeaconRegion;

/**
 * Created by MMO on 20/05/2015.
 */
public class SampleApplication extends ISBeaconApplication {

    @Override
    public boolean shouldSendNotification(ISBeaconRegion region) {
        return true;
    }

    @Override
    public void onEnteredBeaconRegion(ISBeaconRegion region) {
        Toast.makeText(this, "Entered " + region, Toast.LENGTH_SHORT).show();
        super.onEnteredBeaconRegion(region);
    }
}
