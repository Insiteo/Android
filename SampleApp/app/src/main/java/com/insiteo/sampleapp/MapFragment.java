package com.insiteo.sampleapp;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.insiteo.lbs.Insiteo;
import com.insiteo.lbs.common.utils.geometry.ISPosition;
import com.insiteo.lbs.itinerary.ISItineraryProvider;
import com.insiteo.lbs.location.ISLocationProvider;
import com.insiteo.lbs.map.ISIMapListener;
import com.insiteo.lbs.map.ISMapView;
import com.insiteo.lbs.map.database.ISMapDBHelper;
import com.insiteo.lbs.map.entities.ISMap;
import com.insiteo.lbs.map.entities.ISZone;
import com.insiteo.lbs.map.render.ISERenderMode;
import com.insiteo.lbs.map.render.ISEZoneAction;

import java.util.List;

public class MapFragment extends Fragment implements ISIMapListener, OnClickListener, ActionBar.OnNavigationListener {

	public final static String TAG = MapFragment.class.getSimpleName();

	public ISMapView mMapView = null;

	private ISMap mCurrentMap;
	List<ISMap> mMaps;

    Callback mapCallback = null;

    public void setCallbackListerner(Callback c) {
        mapCallback = c;
    }

    public static MapFragment newInstance() {
        Bundle args = new Bundle();
        
        MapFragment fragment = new MapFragment();
        fragment.setArguments(args);
        return fragment;
    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

		View rootView = inflater.inflate((Insiteo.getCurrentUser().getRenderMode() == ISERenderMode.MODE_2D) ? R.layout.fragment_map_2d : R.layout.fragment_map_3d, container, false);

		setHasOptionsMenu(true);
		return rootView;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		initializeMapService();
		super.onViewCreated(view, savedInstanceState);
	}

	@Override
	public void onResume() {
		/* It is best practice to pause the MapView component on its parent Fragment (or Activity) respective method */ 
		mMapView.onResume();
		super.onResume();
	}


	@Override
	public void onPause() {
		/* It is best practice to pause the MapView component on its parent Fragment (or Activity) respective method */ 
		mMapView.onPause();
		super.onPause();
	}
	//******************************************************************************************************************
	// 	UI CALLBACKS
	// *****************************************************************************************************************

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_loc:

			break;
		}
	}

	@Override
	public boolean onNavigationItemSelected(int position, long id) {
		if(mMaps != null && mMaps.size() > position){
			ISMap map = mMaps.get(position);
			mMapView.changeMap(map.getId(), true, true, true);
		}
		return true;
	}


    //**********************************************************************************************
    // 	UI
    // *********************************************************************************************

    private void displayUIInformation() {
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());

        View informationView = LayoutInflater.from(getActivity()).inflate(R.layout
                .information_layout, null);

        TextView apiVersion, locationVersion, itineraryVersion;

        apiVersion = (TextView) informationView.findViewById(R.id.api_version_value);
        locationVersion = (TextView) informationView.findViewById(R.id.loc_version_value);
        itineraryVersion = (TextView) informationView.findViewById(R.id.iti_version_value);

        apiVersion.setText(Insiteo.getAPIVersion());
        locationVersion.setText(ISLocationProvider.getVersion());
        itineraryVersion.setText(ISItineraryProvider.getVersion());

        alert.setView(informationView);

        alert.create().show();
    }

	//**********************************************************************************************
	// 	MAP SERVICE
	// *********************************************************************************************

	private void initializeMapService() {
		mMapView = (ISMapView) getView().findViewById(R.id.map);

		// The MapView listener. By default the listener is set to the context (if it implements IMapListener) that created the View.
		// In the case of fragment we have to explicitly set it.
		mMapView.setListener(this);
	}

	private void setMapNavigationList() {

		// Retrieve the list MapData associated to the current site.
		mMaps = ISMapDBHelper.getMaps(false);
		final String[] mapNames = new String[mMaps.size()];

		int i = 0;
		for (ISMap map : mMaps) {
			mapNames[i] = map.getName();
			i++;
		}

		ActionBar actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();

		if (actionBar != null) {
			actionBar.setDisplayShowTitleEnabled(false);
			actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

			// Set up the dropdown list navigation in the action bar.
			actionBar.setListNavigationCallbacks(
					// Specify a SpinnerAdapter to populate the dropdown list.
					new ArrayAdapter<String>(actionBar.getThemedContext(),
							android.R.layout.simple_list_item_1,
							android.R.id.text1, mapNames), MapFragment.this);
		}
	}

	/**
	 * Called when the map displayed by the MapView changes.
	 * @param mapId the new map's ID
	 * @param mapName the new map's name
	 */
	@Override
	public void onMapChanged(final int mapId, final String mapName) {	
		Log.d(TAG, "onMapChanged");
		for(int i = 0; i < mMaps.size(); i++){
			if(mMaps.get(i).getId() == mapId) {
				mCurrentMap = mMaps.get(i);
				((ActionBarActivity) getActivity()).getSupportActionBar().setSelectedNavigationItem(i);
			}
		}
	}

	/**
	 * Called when the MapView could not be initialized
	 * @param aReason the reason of the failure
	 */
	@Override
	public void onMapInitFailed(String aReason) {
	}

	/**
	 * Called when the MapView is ready (ie : maps data is initialized)
	 * @param aMapID the ID of the current map
	 * @param aMapName the name of the current map
	 */
	@Override
	public void onMapViewReady(final int aMapID, final String aMapName) {
        setMapNavigationList();

        mCurrentMap = ISMapDBHelper.getMap(aMapID);
        mapCallback.onMapInitDone();
	}

    public interface Callback {
        void onMapInitDone();
		void onMapZoneClicked(ISZone zone, ISEZoneAction actionType, String actionParam);
    }

	/**
	 * This event is triggered when a Zone object was clicked, and its associated action is custom,
	 * so it can't be handled by MapController.
	 * @param zone the clicked Zone.
	 * @param actionType the type of action contained in this zone.
	 * @param actionParam the action parameter.
	 */
	@Override
	public void onZoneClicked(ISZone zone, ISEZoneAction actionType, String actionParam) {
		mapCallback.onMapZoneClicked(zone, actionType, actionParam);
	}

	/**
	 * Called when the MapView is clicked
	 */
	@Override
	public void onMapClicked(ISPosition clickedPosition) {

	}

	/**
	 * Called when the MapView is moved
	 */
	@Override
	public void onMapMoved() {

	}

	/**
	 * Called when the zoomlevel has changed	
	 * @param newZoomLevel the new zoomlevel
	 */
	@Override
	public void onZoomEnd(int newZoomLevel) {

	}

	/**
	 * Called the map has stopped moving
	 */
	@Override
	public void onMapReleased() {

	}
}
