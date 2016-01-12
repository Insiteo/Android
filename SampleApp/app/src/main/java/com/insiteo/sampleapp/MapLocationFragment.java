package com.insiteo.sampleapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.insiteo.lbs.common.ISError;
import com.insiteo.lbs.common.utils.ISLog;
import com.insiteo.lbs.location.ISILocationListener;
import com.insiteo.lbs.location.ISLocation;
import com.insiteo.lbs.location.ISLocationProvider;
import com.insiteo.lbs.location.ISLocationRenderer;

/**
 * Created by Cyril on 15/12/2015.
 */
public class MapLocationFragment extends MapFragment implements ISILocationListener, View.OnClickListener{
    private ImageButton mLocationButton;
    private ISLocationRenderer mLocationrenderer;
    private boolean mCenterOnPosition = true;

    private ISLocation mLastLocation;
    private int mLastLocMapID = -1;
    private boolean mRotateMap = true;

    public MapLocationFragment() {

    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment MapLocationFragment.
     */
    public static MapLocationFragment newInstance() {
        MapLocationFragment fragment = new MapLocationFragment();
        return fragment;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public void onMapViewReady(final int aMapID, final String aMapName) {
        super.onMapViewReady(aMapID, aMapName);
        initializeLocationService();
    }

    private void initializeLocationService() {
        ISLog.e(TAG, "initializeLocationService: " );
        mLocationButton = (ImageButton) getView().findViewById(R.id.btn_loc);
        mLocationButton.setOnClickListener(this);

        /**
         * Get the location renderer that automatically created by the location provider. We then set its drawing priority and
         * add it to the MapViewController that will handle the drawing process.
         * The location rendering object uses 3 different colors:
         * - one for the location ie the color of the dot that represents your location
         * - one specific color that is used when the LocationProvider failed computing your position (locationLost).
         * - one to draw an accuracy circle around your position.
         * They can all be override whether from the resources (use insiteo_location_userlocation, insiteo_location_userlocationlost,
         * insiteo_location_accuracy_color color resources) or by calling the appropriate method on the LocationRenderer
         */
        mLocationrenderer = (ISLocationRenderer) ISLocationProvider.getInstance().getRenderer(getResources());
        mLocationrenderer.setPriority(11);
    }

    @Override
    public void onPause() {
        super.onPause();
        stopLocation();
    }

    @Override
    public void initMap() {
        super.initMap();
    }

    @Override
    public void onClick(View v) {
        ISLog.e(TAG, "onClick: MapLocationFragment" );
        switch (v.getId()) {
            case R.id.btn_loc:
                locationHandler();
                break;
        }
    }

    private void locationHandler() {
        if (!ISLocationProvider.getInstance().isStarted()) {
            startLocation();
        } else if(ISLocationProvider.getInstance().isStarted() && !mRotateMap){
            mRotateMap = true;
        } else {
            stopLocation();
            mRotateMap = false;
        }
    }

    /**
     * Starts the location computing process using the flags that were defined in the LauncherActivity
     */
    public void startLocation() {
        mLocationButton.setImageResource(R.drawable.localization_button);
        ((AnimationDrawable) mLocationButton.getDrawable()).start();

        ISLocationProvider.getInstance().start(this);
    }

    /**
     * Stops the location computing process.
     */
    public void stopLocation(){
        ISLocationProvider.getInstance().stop();
        mLocationButton.setImageResource(R.drawable.ic_location_off);
        mLastLocMapID = -1;
        mMapView.rotate(0f, false);

        mLastLocation = null;
    }

    @Override
    public void onLocationInitDone(ISError isError) {
        mMapView.addRenderer(mLocationrenderer);
    }

    @Override
    public void onLocationReceived(final ISLocation aLocation) {
        mLocationButton.setImageResource(R.drawable.ic_location_on);

        mLastLocation = aLocation;

        /**
         * this method animate the map to be centered on the given position, here the user's position
         */
        if(mCenterOnPosition) mMapView.centerMap(aLocation.getPosition(), true);

        mLastLocMapID = aLocation.getMapID();
    }

    @Override
    public void onAzimuthReceived(final float aAzimuth) {
        if (mRotateMap && mLastLocation != null) {
            float angle = aAzimuth;
            if (mCurrentMap != null && mLastLocMapID == mCurrentMap.getId()) {
                boolean isMapOriented = mCurrentMap.isOriented();
                if (isMapOriented) {
                    float mapAzimuth =  mCurrentMap.getAzimuth();
                    angle = mapAzimuth - aAzimuth;
                }
            }
            mMapView.rotate(angle, false);
        }
    }

    @Override
    public void onCompassAccuracyTooLow() {
    }

    @Override
    public void onNeedToActivateGPS() {
        Toast.makeText(getActivity(), "GPS Needed, please activate", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLocationLost(ISLocation isLocation) {
        Toast.makeText(getActivity(), "Location Lost", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void noRegisteredBeaconDetected() {
        Toast.makeText(getActivity(), "No Registered Beacon Detected", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onWifiActivationRequired() {
        final AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        alert.setTitle(R.string.loc_activation_required);
        alert.setMessage(R.string.loc_wifi_activation_required);
        alert.setCancelable(false);
        alert.setPositiveButton(getString(R.string.loc_activate), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                ISLocationProvider.getInstance().activateWifi();
            }
        });
        alert.setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                stopLocation();
            }
        });
        alert.show();
    }

    @Override
    public void onBleActivationRequired() {
        final AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        alert.setTitle(R.string.loc_activation_required);
        alert.setMessage(R.string.loc_ble_activation_required);
        alert.setCancelable(false);
        alert.setPositiveButton(getString(R.string.loc_activate), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                ISLocationProvider.getInstance().activateBle();
            }
        });
        alert.setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                stopLocation();
            }
        });
        alert.show();
    }
}
