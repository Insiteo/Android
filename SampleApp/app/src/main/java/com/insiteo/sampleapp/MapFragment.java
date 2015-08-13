package com.insiteo.sampleapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.insiteo.lbs.Insiteo;
import com.insiteo.lbs.common.ISError;
import com.insiteo.lbs.common.utils.geometry.ISPosition;
import com.insiteo.lbs.itinerary.ISItineraryProvider;
import com.insiteo.lbs.location.ISLocationProvider;
import com.insiteo.lbs.map.ISIMapListener;
import com.insiteo.lbs.map.ISMapView;
import com.insiteo.lbs.map.database.ISMapDBHelper;
import com.insiteo.lbs.map.entities.ISMap;
import com.insiteo.lbs.map.entities.ISZone;
import com.insiteo.lbs.map.entities.ISZonePoi;
import com.insiteo.lbs.map.render.ISERenderMode;
import com.insiteo.lbs.map.render.ISEZoneAction;
import com.insiteo.lbs.map.render.ISGenericRTO;
import com.insiteo.lbs.map.render.ISIRTO;
import com.insiteo.lbs.map.render.ISIRTOListener;
import com.insiteo.sampleapp.render.GfxRto;
import com.insiteo.sampleapp.service.GeofencingController;
import com.insiteo.sampleapp.service.ItinaryController;
import com.insiteo.sampleapp.service.LocationController;

import java.util.List;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class MapFragment extends Fragment implements ISIMapListener, ISIRTOListener, OnClickListener,
		ActionBar.OnNavigationListener {

	public final static String TAG = MapFragment.class.getSimpleName();

	// MAP
	private ISMapView mMapView = null;

	private ISMap mCurrentMap;
	List<ISMap> mMaps;

	// ITINERARY
	private ItinaryController itinaryController;


	// LOCATION
	private LocationController locationController;
	private ImageButton mLocationButton;

	// GEOFENCING
	private GeofencingController geofencingController;
	private View mGeofenceToastView = null;
	private TextView mGeofenceToastText = null;

	public MapFragment() {
		MapViewController mapViewController = new MapViewController(this);
		this.locationController = new LocationController(mapViewController);
		this.geofencingController = new GeofencingController(mapViewController);
		this.itinaryController = new ItinaryController(mapViewController);
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
		mGeofenceToastView = LayoutInflater.from(getActivity()).inflate(R.layout.geofencing_toast, null);
		mGeofenceToastText = (TextView) mGeofenceToastView.findViewById(R.id.geofence_text);
		mLocationButton = (ImageButton) getView().findViewById(R.id.btn_loc);
		mLocationButton.setOnClickListener(this);
		initializeMapService();
		locationController.init(getResources());
		itinaryController.init(getResources());
		geofencingController.init();
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
		locationController.stopLocation();
		
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
				locationController.locationHandler();
				break;
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.main, menu);

		MenuItem versionItem = menu.findItem(R.id.action_version);
		versionItem.setTitle(Insiteo.getAPIVersion());

		MenuItem labelItem = menu.findItem(R.id.site_label);
		labelItem.setTitle(Insiteo.getCurrentSite().getLabel());

		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean result = true;

		switch (item.getItemId()) {

			case R.id.action_itinerary:
				itinaryController.computeItinerary(mMapView, locationController);
				result = false;
				break;

			case R.id.action_route:
				itinaryController.computeOptimizedItinerary(mMaps, mCurrentMap);
				result = false;
				break;

			case R.id.action_locate_ext_poi:
				locateExtPoi();
				result = false;
				break;

			case R.id.action_clear_itinerary:
				itinaryController.clear();
				result = false;
				break;


			case R.id.action_clear_all:
				itinaryController.clear();
				mMapView.clearRenderer(GfxRto.class);
				result = false;
				break;

			case R.id.action_information:
				displayUIInformation();
				result = false;
				break;

			case R.id.action_switch_site:
				MainActivity act = (MainActivity) getActivity();
				act.switchSite();
				result = false;
				break;

		}
		return result;
	}

	@Override
	public boolean onNavigationItemSelected(int position, long id) {
		if (mMaps != null && mMaps.size() > position) {
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

	private void initializeRTO() {
		// 1 - The RTO class needs to be added to the MapViewController in order to be drawn by a GenericRenderer
		mMapView.setPriority(GfxRto.class, 14);

		// 2 - We tell the MapViewController to put a listener for touch events on this type of RTOs
		mMapView.setRTOListener(this, GfxRto.class);
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
	 *
	 * @param mapId   the new map's ID
	 * @param mapName the new map's name
	 */
	@Override
	public void onMapChanged(final int mapId, final String mapName) {
		Log.d(TAG, "onMapChanged");
		for (int i = 0; i < mMaps.size(); i++) {
			if (mMaps.get(i).getId() == mapId) {
				mCurrentMap = mMaps.get(i);
				((ActionBarActivity) getActivity()).getSupportActionBar().setSelectedNavigationItem(i);
			}
		}
	}

	/**
	 * Called when the MapView could not be initialized
	 *
	 * @param aReason the reason of the failure
	 */
	@Override
	public void onMapInitFailed(String aReason) {
	}

	/**
	 * Called when the MapView is ready (ie : maps data is initialized)
	 *
	 * @param aMapID   the ID of the current map
	 * @param aMapName the name of the current map
	 */
	@Override
	public void onMapViewReady(final int aMapID, final String aMapName) {
		Log.d(TAG, "onMapViewReady");
		setMapNavigationList();

		initializeRTO();

		// Add the location and itinerary renderers to the MapView to enable their display.
		mMapView.addRenderer(locationController.getLocationRenderer());
		mMapView.addRenderer(itinaryController.getItineraryRenderer());

		mCurrentMap = ISMapDBHelper.getMap(aMapID);
	}

	/**
	 * This event is triggered when a Zone object was clicked, and its associated action is custom,
	 * so it can't be handled by MapController.
	 *
	 * @param zone        the clicked Zone.
	 * @param actionType  the type of action contained in this zone.
	 * @param actionParam the action parameter.
	 */
	@Override
	public void onZoneClicked(ISZone zone, ISEZoneAction actionType, String actionParam) {
		Log.d(TAG, "onZoneClicked");

		// Clear the MapView from all the GfxRto previously rendered.
		mMapView.clearRenderer(GfxRto.class);

		// Get all the external POI that are associated to this zone and add them on the map.
		List<ISZonePoi> zpas = ISMapDBHelper.getPoiAssocFromZone(zone.getId(), true);

		for (int i = 0; i < zpas.size(); i++) {
			String poiExtId = zpas.get(i).getExternalPoiId();
			GfxRto rto = new GfxRto();

			rto.setLabel(poiExtId);
			rto.setLabelDisplayed(true);

			rto.setAnnotationLabel(poiExtId);

			// Apply a particular offset to this rto. This is used to have multiple rto in the same zone without superposition.
			rto.setZoneOffset(zpas.get(i).getOffset());

			mMapView.addRTOInZone(zone.getId(), rto);

		}

		// Center the map on this zone with animation.
		mMapView.centerMap(zone.getId(), true);
	}

	/**
	 * Called when the MapView is clicked
	 */
	@Override
	public void onMapClicked(ISPosition clickedPosition) {
		Log.d(TAG, "onMapClicked");
	}

	/**
	 * Called when the MapView is moved
	 */
	@Override
	public void onMapMoved() {
		Log.d(TAG, "onMapMoved");
		locationController.disableCenterOnPosition();
	}

	/**
	 * Called when the zoomlevel has changed
	 *
	 * @param newZoomLevel the new zoomlevel
	 */
	@Override
	public void onZoomEnd(int newZoomLevel) {
		Log.d(TAG, "onZoomEnd");
	}

	/**
	 * Called the map has stopped moving
	 */
	@Override
	public void onMapReleased() {
		Log.d(TAG, "onMapReleased");
	}


	public void locateExtPoi() {
		final AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
		alert.setTitle(R.string.locate_ext_poi_title);
		final EditText input = new EditText(getActivity());
		input.setHint(R.string.locate_ext_poi_hint);
		alert.setView(input);
		alert.setCancelable(false);
		alert.setPositiveButton(R.string.action_locate, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				String value = input.getText().toString();

				// Clear the MapView from all the GfxRto previously rendered.
				mMapView.clearRenderer(GfxRto.class);

				List<ISZonePoi> zpas = ISMapDBHelper.getZoneAssocFromExtPoi(value);

				for (int i = 0; i < zpas.size(); i++) {
					String poiExtId = zpas.get(i).getExternalPoiId();
					GfxRto rto = new GfxRto();
					rto.setLabel(poiExtId);
					rto.setLabelDisplayed(true);
					mMapView.addRTOInZone(zpas.get(i).getZoneId(), rto, zpas.get(i).getOffset());
				}

				if (!zpas.isEmpty()) {
					mMapView.centerMap(zpas.get(0).getZoneId(), true);
				} else {
					Crouton.makeText(getActivity(), R.string.error_no_ext_poi_found, Style.ALERT).show();
				}
			}
		});
		alert.setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
			}
		});
		alert.show();
	}

	//******************************************************************************************************************
	// 	RTO SERVICE 
	// *****************************************************************************************************************

	/**
	 * Called when a RTO is clicked
	 *
	 * @param rto  the RTO that was clicked
	 * @param zone the zone containing this RTO (if RTO was put in a zone), or null if RTO was not set in a zone
	 */
	@Override
	public void onRTOClicked(ISIRTO rto, ISZone zone) {
		if (rto instanceof ISGenericRTO) {
			ISGenericRTO genericRto = (ISGenericRTO) rto;

			if (genericRto.isAnnotationClicked()) {
				genericRto.setActionDisplayed(!genericRto.isActionDisplayed());
			}

			if (genericRto.isActionClicked()) {
				genericRto.setIndicatorDisplayed(!genericRto.isIndicatorDisplayed());
			}
		}
	}

	/**
	 * Called when a RTO is moved
	 *
	 * @param rto  the RTO that was moved
	 * @param zone the zone containing this RTO (if RTO was put in a zone), or null if RTO was not set in a zone
	 */
	@Override
	public void onRTOMoved(ISIRTO rto, ISZone zone) {
	}

	/**
	 * Called when a RTO is released
	 *
	 * @param rto  the RTO that was released
	 * @param zone the zone containing this RTO (if RTO was put in a zone), or null if RTO was not set in a zone
	 */
	@Override
	public void onRTOReleased(ISIRTO rto, ISZone zone) {
	}

	/**
	 * Called when a RTO is selected
	 *
	 * @param rto  the RTO that was selected
	 * @param zone the zone containing this RTO (if RTO was put in a zone), or null if RTO was not set in a zone
	 */
	@Override
	public void onRTOSelected(ISIRTO rto, ISZone zone) {
	}

	//******************************************************************************************************************
	// 	ITINERARY SERVICE 
	// *****************************************************************************************************************






	public void showGeofenceToast(final String extra1) {
		getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
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

	public void stopLocation() {
		mMapView.rotate(0f, false);
		mLocationButton.setImageResource(R.drawable.ic_location_off);
	}

	public void showLocationButtonResourceOn() {
		mLocationButton.setImageResource(R.drawable.ic_location_on);
	}

	public void centerMap(ISPosition position) {
		mMapView.centerMap(position, true);
	}

	public void rotateMap(int mLastLocMapID, float aAzimuth) {
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

	public void showWifiActivitionRequiredDialog() {
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

	public void showBLEActivitionRequiredDialog() {
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

	public void showItinaryAlert(ISError error) {
		String message = getString(R.string.error_itinerary_computation_failed) + ": " + error;
		Crouton.makeText(getActivity(), message, Style.ALERT).show();
	}

	public void showLocationButtonResourceLocalizationWithAnimation() {
		mLocationButton.setImageResource(R.drawable.localization_button);
		((AnimationDrawable) mLocationButton.getDrawable()).start();
	}
}
