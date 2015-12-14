package com.insiteo.sampleapp.beacon;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.insiteo.sampleapp.R;
import com.insiteo.sampleapp.SampleApplication;
import com.insiteo.sampleapp.bus.BeaconRegionEvent;


/**
 * Created by MMO on 08/07/2015.
 */
public class BeaconMonitoringFragment extends Fragment {
    private RecyclerView mRecyclerView;
    private BeaconRegionAdapter mAdapter;

    public static BeaconMonitoringFragment newInstance() {
        return new BeaconMonitoringFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SampleApplication.bus.register(this);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_monitoring, container, false);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mAdapter = new BeaconRegionAdapter(getActivity());
        mRecyclerView.setAdapter(mAdapter);

        if(getActivity() != null) {
            SampleApplication app = (SampleApplication) getActivity().getApplication();

            synchronized (app.getRegions()) {
                mAdapter.setDataCollection(app.getRegions().values());
            }
        }

        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        SampleApplication.bus.unregister(this);
    }



    //**********************************************************************************************
    // EventBus
    // *********************************************************************************************

    public void onEvent(BeaconRegionEvent event) {
        int position = mAdapter.add(event.getRegion());
        mRecyclerView.scrollToPosition(position);
    }
}
