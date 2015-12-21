package com.insiteo.sampleapp;

import android.os.Bundle;
import android.view.View;

import com.insiteo.lbs.common.ISError;
import com.insiteo.lbs.common.init.ISPackage;
import com.insiteo.lbs.map.database.ISMapDBHelper;
import com.insiteo.lbs.map.entities.ISZone;
import com.insiteo.lbs.map.entities.ISZonePoi;
import com.insiteo.lbs.map.render.ISEZoneAction;
import com.insiteo.lbs.map.render.ISGenericRTO;
import com.insiteo.lbs.map.render.ISIRTO;
import com.insiteo.lbs.map.render.ISIRTOListener;
import com.insiteo.sampleapp.render.GfxRto;

import java.util.List;
import java.util.Stack;

/**
 * Created by Cyril on 15/12/2015.
 */
public class MapRTOFragment extends MapLocationFragment implements ISIRTOListener{

    private static final String TAG = "MapRTOFragment";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public MapRTOFragment() {

    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment MapLocationFragment.
     */
    public static MapRTOFragment newInstance() {
        MapRTOFragment fragment = new MapRTOFragment();
        return fragment;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onStartDone(ISError error, Stack<ISPackage> packageToUpdate) {
        super.onStartDone(error, packageToUpdate);

    }

    @Override
    public void onLocationInitDone(ISError isError) {
        super.onLocationInitDone(isError);
    }

    @Override
    public void onMapInitDone() {
        initializationMapRTO();
    }

    private void initializationMapRTO() {
        mMapFrag.mMapView.setPriority(GfxRto.class, 14);
        mMapFrag.mMapView.setRTOListener(this, GfxRto.class);
    }

    /**
     * Called when a RTO is clicked
     * @param rto the RTO that was clicked
     * @param zone the zone containing this RTO (if RTO was put in a zone), or null if RTO was not set in a zone
     */
    @Override
    public void onRTOClicked(ISIRTO rto, ISZone zone) {
        if(rto instanceof ISGenericRTO) {
            ISGenericRTO genericRto = (ISGenericRTO) rto;

            if(genericRto.isAnnotationClicked()) {
                genericRto.setActionDisplayed(!genericRto.isActionDisplayed());
            }

            if(genericRto.isActionClicked()) {
                genericRto.setIndicatorDisplayed(!genericRto.isIndicatorDisplayed());
            }
        }
    }

    /**
     * Called when a RTO is moved
     * @param rto the RTO that was moved
     * @param zone the zone containing this RTO (if RTO was put in a zone), or null if RTO was not set in a zone
     */
    @Override
    public void onRTOMoved(ISIRTO rto, ISZone zone) {
    }

    /**
     * Called when a RTO is released
     * @param rto the RTO that was released
     * @param zone the zone containing this RTO (if RTO was put in a zone), or null if RTO was not set in a zone
     */
    @Override
    public void onRTOReleased(ISIRTO rto, ISZone zone) {
    }

    /**
     * Called when a RTO is selected
     * @param rto the RTO that was selected
     * @param zone the zone containing this RTO (if RTO was put in a zone), or null if RTO was not set in a zone
     */
    @Override
    public void onRTOSelected(ISIRTO rto, ISZone zone) {

    }

    /**
     * This event is triggered when a Zone object was clicked, and its associated action is custom,
     * so it can't be handled by MapController.
     * @param zone the clicked Zone.
     * @param actionType the type of action contained in this zone.
     * @param actionParam the action parameter.
     */
    @Override
    public void onMapZoneClicked(ISZone zone, ISEZoneAction actionType, String actionParam) {

        // Clear the MapView from all the GfxRto previously rendered.
        mMapFrag.mMapView.clearRenderer(GfxRto.class);

        // Get all the external POI that are associated to this zone and add them on the map.
        List<ISZonePoi> zpas = ISMapDBHelper.getPoiAssocFromZone(zone.getId(), true);

        for(int i = 0; i < zpas.size(); i++) {
            String poiExtId = zpas.get(i).getExternalPoiId();
            GfxRto rto = new GfxRto();

            rto.setLabel(poiExtId);
            rto.setLabelDisplayed(true);

            rto.setAnnotationLabel(poiExtId);

            // Apply a particular offset to this rto. This is used to have multiple rto in the same zone without superposition.
            rto.setZoneOffset(zpas.get(i).getOffset());

            mMapFrag.mMapView.addRTOInZone(zone.getId(), rto);
        }

        // Center the map on this zone with animation.
        mMapFrag.mMapView.centerMap(zone.getId(), true);
    }

}
