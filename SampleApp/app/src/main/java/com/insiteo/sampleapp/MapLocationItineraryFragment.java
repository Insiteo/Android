package com.insiteo.sampleapp;

import android.os.Bundle;

import com.insiteo.lbs.common.ISError;
import com.insiteo.lbs.common.init.ISPackage;
import com.insiteo.lbs.common.utils.geometry.ISPosition;
import com.insiteo.lbs.itinerary.ISIItineraryRendererListener;
import com.insiteo.lbs.itinerary.ISIItineraryRequestListener;
import com.insiteo.lbs.itinerary.ISItineraryProvider;
import com.insiteo.lbs.itinerary.ISItineraryRenderer;
import com.insiteo.lbs.itinerary.entities.ISItinerary;
import com.insiteo.lbs.itinerary.entities.ISItinerarySection;
import com.insiteo.lbs.location.ISELocationModule;
import com.insiteo.lbs.location.ISLocationProvider;
import com.insiteo.lbs.map.entities.ISMap;

import java.util.ArrayList;
import java.util.Stack;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

/**
 * Created by Cyril on 30/12/2015.
 */
public class MapLocationItineraryFragment extends MapLocationFragment implements ISIItineraryRendererListener, ISIItineraryRequestListener {
    private final static boolean PMR_ENABLED = false;
    private final static boolean ITINERARY_RECOMPUTE_ACTIVATED = true;
    private final static float MAX_RECOMPUTATION_DISTANCE = 5;
    private ISItineraryProvider mItineraryProvider;
    private ISItineraryRenderer mItineraryRenderer;

    public static MapLocationItineraryFragment newInstance() {

        Bundle args = new Bundle();

        MapLocationItineraryFragment fragment = new MapLocationItineraryFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onStartDone(ISError error, Stack<ISPackage> packageToUpdate) {
        super.onStartDone(error, packageToUpdate);

    }

    @Override
    public void initMap() {
        super.initMap();
        initializeItineraryService();
    }

    private void initializeItineraryService(){
        // create an itinerary provider, as we don't use location.
        // When using location, it is easier to get itinerary provider from location provider)
        mItineraryProvider = (ISItineraryProvider) ISLocationProvider.getInstance().getModule(ISELocationModule.ITINERARY);

        if (mItineraryProvider != null) {
            mItineraryProvider.setDynamicMode(true);

            //get itinerary renderer linked to provider
            mItineraryRenderer = (ISItineraryRenderer) mItineraryProvider.getRenderer(getResources());
            mItineraryRenderer.setPriority(10);
            mItineraryRenderer.setLinkToEnds(true, true);
            mItineraryRenderer.setListener(this);
        }
    }

    @Override
    public void onLocationInitDone(ISError isError) {
        super.onLocationInitDone(isError);
        mMapView.addRenderer(mItineraryRenderer);
    }


    /**
     * This method shows how to compute an itinerary between two points
     */
    private void computeItinerary() {
        //hide the old itinerary if it exists
        mItineraryRenderer.setDisplayEnabled(true);

        ISPosition arrival = new ISPosition(mMapView.getMapId(), 20, 20);
        mItineraryProvider.requestItineraryFromCurrentLocation(arrival, true, this, PMR_ENABLED);

        // If the location is started will request an itinerary from our current position
        if (!ISLocationProvider.getInstance().isStarted()) {
            startLocation();
        }
    }

    @Override
    public void startLocation() {
        super.startLocation();
        computeItinerary();
    }

    /**
     * This method shows how to compute a route between different positions
     */
    private void computeOptimizedItinerary(){

        int positionNbr = 4;

        ArrayList<ISPosition> pos = new ArrayList<ISPosition>();

        for (int i = 0; i < positionNbr; i++) {
            double x = Math.random() * 50;
            double y = Math.random() * 50;

            if(mMaps.size() > 1 && i > positionNbr / 2) {
                ISMap otherMap = null;
                for (ISMap map : mMaps) {
                    if(map.getId() != mCurrentMap.getId()) {
                        otherMap = map;
                        break;
                    }
                }

                if(otherMap != null) {
                    ISPosition p = new ISPosition(otherMap.getId(), x, y);
                    pos.add(p);
                }
            }
            else {
                ISPosition p = new ISPosition(mCurrentMap.getId(), x, y);
                pos.add(p);
            }
        }
        mItineraryProvider.requestOptimizedItinerary(pos, ISItineraryProvider.ISEOptimizationMode.NearestNeighbourShortestPath, true, false, this, false);
    }

    @Override
    public void onWaypointClicked(ISItinerary isItinerary, int i, ISItinerarySection isItinerarySection) {

    }

    @Override
    public void onInstructionClicked(ISItinerary isItinerary, int i) {

    }

    @Override
    public void onMapSwitcherClicked(ISPosition aNextPosition) {
        if(aNextPosition != null){
            mMapView.centerMap(aNextPosition, true);
        }
    }

    @Override
    public void onItineraryRequestDone(boolean aSuccess, ISItineraryProvider.ISBaseRequest aRequest, final ISError error) {
        if(aSuccess) {
            mItineraryRenderer.setDisplayEnabled(true);
        }
        else {
            String message = getString(R.string.error_itinerary_computation_failed) + ": " + error;
            Crouton.makeText(getActivity(), message, Style.ALERT).show();
        }
    }

    @Override
    public void onItineraryChanged(ISItineraryProvider.ISBaseRequest aRequest, float aDistanceToIti) {
        if (aRequest instanceof ISItineraryProvider.ISItineraryRequest && ISLocationProvider.getInstance().isStarted() && ITINERARY_RECOMPUTE_ACTIVATED) {
            if (aDistanceToIti > MAX_RECOMPUTATION_DISTANCE || aDistanceToIti == -1) {
                aRequest.recompute();
            }
        }
    }
}
