package com.insiteo.sampleapp;

import android.content.SharedPreferences;

import com.insiteo.lbs.Insiteo;
import com.insiteo.lbs.beacon.ISBeaconApplication;
import com.insiteo.lbs.common.auth.entities.ISBeaconRegion;
import com.insiteo.lbs.common.utils.ISLog;
import com.insiteo.sampleapp.beacon.StateBeaconRegion;
import com.insiteo.sampleapp.bus.BeaconRegionEvent;
import com.insiteo.sampleapp.settings.SettingsPrefs;
import com.insiteo.sampleapp.utils.Utils;

import java.util.HashMap;
import java.util.Map;

import de.greenrobot.event.EventBus;

/**
 * Created by MMO on 20/05/2015.
 */
public class SampleApplication extends ISBeaconApplication {

    private final static String TAG = SampleApplication.class.getSimpleName();

    /**
     * If set to <code>true</code> beacon region notification will always
     * be trigger (if properly on back office), otherwise they will only be trigger when
     * the application is in background
     */
    private final static boolean FORCE_NOTIFICATION  = true;

    private Map<Long, StateBeaconRegion> beaconRegions;
    public static EventBus bus;


    @Override
    public void onCreate() {
        //  Sets the debug mode for both the ISBeaconService and the application process
        Insiteo.setDebug(BuildConfig.DEBUG);
        super.onCreate();

        if (!runsOnServiceProcess()) {
            bus = EventBus.getDefault();
            beaconRegions = new HashMap<>();
        }

    }

    @Override
    protected boolean shouldAutoStartService() {
        SharedPreferences prefs = getSharedPreferences(SettingsPrefs.BEACON_PREFS, MODE_PRIVATE);
        boolean autoStart = prefs.getBoolean(SettingsPrefs.BEACON_MONITORING_ENABLED, SettingsPrefs.BEACON_MONITORING_ENABLED_DEFAULT);
        return autoStart;
    }


    public Map<Long, StateBeaconRegion> getRegions() {
        return beaconRegions;
    }

    @Override
    public boolean shouldSendNotification(ISBeaconRegion region) {
        return FORCE_NOTIFICATION || super.shouldSendNotification(region);
    }

    @Override
    public void onEnteredBeaconRegion(ISBeaconRegion region) {
        ISLog.d(TAG, "onEnteredBeaconRegion " + region);

        Utils.vibrate(SampleApplication.this, 200);

        StateBeaconRegion stateRegion = null;

        synchronized (beaconRegions) {
            stateRegion = beaconRegions.get(region.getExternalId());

            if (stateRegion == null) {
                stateRegion = new StateBeaconRegion(region);
                stateRegion.setState(StateBeaconRegion.State.IN);
                beaconRegions.put(region.getExternalId(), stateRegion);
            } else {
                stateRegion.setState(StateBeaconRegion.State.IN);
            }
        }

        bus.post(new BeaconRegionEvent(stateRegion));
    }

    @Override
    public void onExitBeaconRegion(ISBeaconRegion region) {
        ISLog.d(TAG, "onExitBeaconRegion " + region);

        Utils.vibrate(SampleApplication.this, 200);

        StateBeaconRegion stateRegion = null;

        synchronized (beaconRegions) {
            stateRegion = beaconRegions.get(region.getExternalId());

            if (stateRegion == null) {
                stateRegion = new StateBeaconRegion(region);
                stateRegion.setState(StateBeaconRegion.State.OUT);
                beaconRegions.put(region.getExternalId(), stateRegion);
            } else {
                stateRegion.setState(StateBeaconRegion.State.OUT);
            }
        }

        bus.post(new BeaconRegionEvent(stateRegion));

    }

}
