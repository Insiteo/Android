package com.insiteo.sampleapp;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.insiteo.lbs.Insiteo;
import com.insiteo.lbs.common.ISError;
import com.insiteo.lbs.common.auth.entities.ISUser;
import com.insiteo.lbs.common.auth.entities.ISUserSite;
import com.insiteo.lbs.common.init.ISEPackageType;
import com.insiteo.lbs.common.init.ISPackage;
import com.insiteo.lbs.common.utils.ISLog;
import com.insiteo.lbs.map.entities.ISZone;
import com.insiteo.lbs.map.render.ISEZoneAction;
import com.insiteo.sampleapp.initialization.ISInitializationTaskFragment;

import java.util.Stack;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * Use the {@link MapLocationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class InitComponentFragment extends Fragment implements ISInitializationTaskFragment.Callback, MapFragment.Callback  {

    private static final String TAG = "InitComponentFragment";
    private final static int NO_RESOURCE = -1;

    private ISInitializationTaskFragment mInitializationFragment;
    FragmentManager fragmentManager = null;

    MapFragment mMapFrag;

    private ProgressBar mLoaderView;

    public InitComponentFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment MapLocationFragment.
     */
    public static InitComponentFragment newInstance() {
        InitComponentFragment fragment = new InitComponentFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.e(TAG, "onCreate: creating new " + TAG + " " + this);
        super.onCreate(savedInstanceState);
        fragmentManager = getFragmentManager();

    }

    @Override
    public void onStart() {
        super.onStart();
        if(mInitializationFragment != null) {

            switch (mInitializationFragment.getCurrentState()){
                case UNKNOWN:
                    /**
                     * Forces the API initialization
                     */
                    if(!Insiteo.getInstance().isAuthenticated()) {
                        mInitializationFragment.initializeAPI();
                        displayLoaderView(true);
                    }
                    else {
                        ISUser user = Insiteo.getInstance().getCurrentUser();
                        ISUserSite userSite = user.getSite(Insiteo.getInstance().getCurrentSite().getSiteId());
                        mInitializationFragment.startSite(userSite);
                    }
                    break;

                case INITIALIZING:
                    displayLoaderView(true);
                    break;
            }
        }
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy: destroying " + TAG + " " + this);
        super.onDestroy();
    }


    private void launch(Fragment frag, int subtitleRes) {
        fragmentManager.beginTransaction()
                .replace(R.id.mycontainer, frag)
                .commit();
    }

    private void launchMap() {
        mMapFrag = MapFragment.newInstance();
        launch(mMapFrag, R.string.map_title);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_map_location, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mInitializationFragment = (ISInitializationTaskFragment) fragmentManager.findFragmentByTag(TAG);

        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (mInitializationFragment == null) {
           launch(mInitializationFragment = ISInitializationTaskFragment.newInstance(), NO_RESOURCE);
        }
        mInitializationFragment.setContext(getActivity());
        mInitializationFragment.setParentFragment(this);

        initElements();
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


    //**********************************************************************************************
    // 	ISInitializationTaskFragment#Callback
    // *********************************************************************************************

    @Override
    public void onInitDone(ISError error, ISUserSite suggestedSite, boolean fromLocalCache) {
        displayLoaderView(false);

        if (error == null) {
            mInitializationFragment.startSite(suggestedSite);
        } else {
            ISLog.e(TAG, "onInitDone: " + error);
        }
    }

    public void startSite() {

    }

    @Override
    public void onStartDone(ISError error, Stack<ISPackage> packageToUpdate) {
        if (error == null) {
            if (packageToUpdate.isEmpty()) {
                launchMap();
                mMapFrag.setCallbackListerner((MapFragment.Callback) this);
            }
        } else {
            ISLog.e(TAG, "onStartDone: " + error);
        }
    }

    @Override
    public void onPackageUpdateProgress(ISEPackageType packageType, boolean download, long progress, long total) {

    }

    @Override
    public void onDataUpdateDone(ISError error) {
        if (error == null) {
            launchMap();
            mMapFrag.setCallbackListerner(this);
        } else {
            ISLog.e(TAG, "onDataUpdateDone: " + error);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    @Override
    public void onPause() {
        stopLocation();
        super.onPause();
    }

    /**
     * Stops the location computing process.
     */
    public void stopLocation(){
       // ISLocationProvider.getInstance().stop();
       // mLocationButton.setImageResource(R.drawable.ic_location_off);
       // mLastLocMapID = -1;

      //  mLastLocation = null;
    }



    private void initElements() {
        mLoaderView = (ProgressBar) getActivity().findViewById(R.id.initialization_progress);
    }

    private void displayLoaderView(boolean visible) {
        mLoaderView.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onMapInitDone() {

    }

    @Override
    public void onMapZoneClicked(ISZone zone, ISEZoneAction actionType, String actionParam) {

    }
}
