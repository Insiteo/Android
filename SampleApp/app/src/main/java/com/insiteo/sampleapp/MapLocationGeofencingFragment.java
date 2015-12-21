package com.insiteo.sampleapp;

import android.content.res.Resources;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.insiteo.lbs.common.ISError;
import com.insiteo.lbs.common.init.ISPackage;
import com.insiteo.lbs.geofence.ISGeofenceArea;
import com.insiteo.lbs.geofence.ISGeofenceProvider;
import com.insiteo.lbs.geofence.ISIGeofenceListener;
import com.insiteo.lbs.location.ISELocationModule;
import com.insiteo.lbs.location.ISLocationProvider;

import java.util.List;
import java.util.Stack;

/**
 * Created by Cyril on 15/12/2015.
 */
public class MapLocationGeofencingFragment extends MapLocationFragment implements ISIGeofenceListener {
    private static final String TAG = "MapLocationGeofencing";

    private ISGeofenceProvider mGeofenceProvider;
    private View mGeofenceToastView = null;
    private TextView mGeofenceToastText = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public MapLocationGeofencingFragment() {

    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment MapLocationFragment.
     */
    public static MapLocationGeofencingFragment newInstance() {
        MapLocationGeofencingFragment fragment = new MapLocationGeofencingFragment();
        return fragment;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onStartDone(ISError error, Stack<ISPackage> packageToUpdate) {
        super.onStartDone(error, packageToUpdate);
        initializeGeofencingService();
    }

    private void initializeGeofencingService(){
        mGeofenceProvider = (ISGeofenceProvider) ISLocationProvider.getInstance().getModule(ISELocationModule.GEOFENCING);
        mGeofenceProvider.setListener(this);
        mGeofenceToastView = LayoutInflater.from(getActivity()).inflate(R.layout.geofencing_toast, null);
        mGeofenceToastText = (TextView)mGeofenceToastView.findViewById(R.id.geofence_text);
    }

    /**
     * Called when geofencing module has new data available.
     * @param aEnteredZones list of zones that location has just entered
     * @param aStayedZones list of zones where location has stayed for a certain amount of time
     * @param aLeftZones list of zones that location has just left
     */
    @Override
    public void onGeofenceUpdate(List<ISGeofenceArea> aEnteredZones, List<ISGeofenceArea> aStayedZones, List<ISGeofenceArea> aLeftZones) {
        final List<ISGeofenceArea> zones = aEnteredZones;

        for (ISGeofenceArea z : zones) {
            final String extra1 = z.getExtra1();
            if (extra1 != null) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override public void run() {
                        mGeofenceToastText.setText(extra1);
                        final Toast t = new Toast(getActivity());

                        t.setDuration(Toast.LENGTH_LONG);
                        Resources r = getResources();
                        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, r.getDisplayMetrics());
                        t.setGravity(Gravity.TOP, 0, (int) px);
                        t.setView(mGeofenceToastView);
                        t.show();
                    }
                });
            }
        }
    }

    /**
     * Called when geofencing data is reset. This happens when no location was received for a long duration, thus zone detection is not valid anymore.
     */
    @Override
    public void onGeofenceDataCleared() {

    }
}
