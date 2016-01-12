package com.insiteo.sampleapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.insiteo.lbs.Insiteo;
import com.insiteo.lbs.common.ISError;
import com.insiteo.lbs.common.auth.entities.ISUserSite;
import com.insiteo.lbs.itinerary.ISItineraryProvider;
import com.insiteo.lbs.location.ISLocationProvider;
import com.insiteo.sampleapp.initialization.ISInitializationTaskFragment;

/**
 * Created by Cyril on 04/01/2016.
 */
public class InitAllInOneFragment extends ISInitializationTaskFragment {

    public static InitAllInOneFragment newInstance() {
        Bundle args = new Bundle();

        InitAllInOneFragment fragment = new InitAllInOneFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.information_layout, container, false);
        return rootView;
    }
    TextView apiVersion, locationVersion, itineraryVersion, siteId;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        init();
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onInitDone(ISError error, ISUserSite suggestedSite, boolean fromLocalCache) {
        super.onInitDone(error, suggestedSite, fromLocalCache);

    }

    private void init() {
        apiVersion = (TextView) getView().findViewById(R.id.api_version_value);
        locationVersion = (TextView) getView().findViewById(R.id.loc_version_value);
        itineraryVersion = (TextView) getView().findViewById(R.id.iti_version_value);
        siteId = (TextView) getView().findViewById(R.id.site_id_value);
    }

    @Override
    public void initMap() {
        apiVersion.setText(Insiteo.getAPIVersion());
        locationVersion.setText(ISLocationProvider.getVersion());
        itineraryVersion.setText(ISItineraryProvider.getVersion());
        siteId.setText(" " + Insiteo.getCurrentSite().getSiteId());
    }
}
