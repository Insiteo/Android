package com.insiteo.sampleapp.beacon;

import com.insiteo.lbs.common.auth.entities.ISBeaconRegion;

/**
 * Created by MMO on 09/07/2015.
 */
public class StateBeaconRegion {

    private final ISBeaconRegion  region;
    private State state;

    public enum State {
        IN,
        OUT
    }

    public StateBeaconRegion(ISBeaconRegion region) {
        this.region = region;
    }

    //**********************************************************************************************
    // GETTERS & SETTERS
    // *********************************************************************************************

    public ISBeaconRegion getRegion() {
        return region;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public Long getExternalId() {
        return region.getExternalId();
    }

    public String getUuid() {
        return region.getUuid();
    }

    public Integer getMajor() {
        return region.getMajor();
    }

    public Integer getMinor() {
        return region.getMinor();
    }

    public String getLabel() {
        return region.getLabel();
    }

    public String getMessage() {
        return region.getMessage();
    }

    public boolean shouldForceNotification() {
        return region.isShouldForceNotification();
    }

    //**********************************************************************************************
    //
    // *********************************************************************************************

    @Override
    public int hashCode() {
        return (int) region.getExternalId();
    }

    @Override
    public boolean equals(Object o) {
        return o != null && o.getClass() == this.getClass() && ((StateBeaconRegion) o).region.equals(region);
    }

    @Override
    public String toString() {
        return region.getLabel()+ ": " + region.getUuid() + ";" + region.getMajor() + ";" + region.getMinor();
    }
}
