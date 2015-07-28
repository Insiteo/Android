package com.insiteo.sampleapp.bus;

import com.insiteo.sampleapp.beacon.StateBeaconRegion;

/**
 * Created by MMO on 09/07/2015.
 */
public class BeaconRegionEvent {
    StateBeaconRegion region;

    public BeaconRegionEvent(StateBeaconRegion region){
        this.region = region;
    }

    public StateBeaconRegion getRegion(){
        return region;
    }
}
