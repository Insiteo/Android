package com.insiteo.sampleapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
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
import com.insiteo.lbs.map.ISMapView;
import com.insiteo.lbs.map.database.ISMapDBHelper;
import com.insiteo.lbs.map.entities.ISMap;
import com.insiteo.lbs.map.entities.ISZonePoi;
import com.insiteo.lbs.map.render.ISERenderMode;
import com.insiteo.sampleapp.render.GfxRto;
import com.insiteo.sampleapp.service.GeofencingController;
import com.insiteo.sampleapp.service.ItinaryController;
import com.insiteo.sampleapp.service.LocationController;
import com.insiteo.sampleapp.service.MapController;
import com.insiteo.sampleapp.service.RTOController;
import com.threed.jpct.SimpleVector;

import java.util.List;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class MapFragment extends Fragment implements OnClickListener, ActionBar.OnNavigationListener {

	private View mGeofenceToastView = null;
	private TextView mGeofenceToastText = null;
	private ImageButton mLocationButton;
	private ISMapView mMapView = null;

	private ItinaryController itinaryController;
	private LocationController locationController;
	private GeofencingController geofencingController;
	private MapController mapController;

	public MapFragment() {
		MapViewController mapViewController = new MapViewController(this);
		this.locationController = new LocationController(mapViewController);
		this.geofencingController = new GeofencingController(mapViewController);
		this.itinaryController = new ItinaryController(mapViewController);
		this.mapController = new MapController(mapViewController, locationController);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate((Insiteo.getCurrentUser().getRenderMode() == ISERenderMode.MODE_2D) ? R.layout.fragment_map_2d : R.layout.fragment_map_3d, container, false);
		setHasOptionsMenu(true);
		return rootView;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		mGeofenceToastView = LayoutInflater.from(getActivity()).inflate(R.layout.geofencing_toast, null);
		mGeofenceToastText = (TextView) mGeofenceToastView.findViewById(R.id.geofence_text);
		mLocationButton = (ImageButton) getView().findViewById(R.id.btn_loc);
		mMapView = (ISMapView) getView().findViewById(R.id.map);
		// The MapView listener. By default the listener is set to the context (if it implements IMapListener) that created the View.
		// In the case of fragment we have to explicitly set it.
		mMapView.setListener(mapController);
		mLocationButton.setOnClickListener(this);
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
		menu.findItem(R.id.action_version).setTitle(Insiteo.getAPIVersion());
		menu.findItem(R.id.site_label).setTitle(Insiteo.getCurrentSite().getLabel());
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_itinerary:
				itinaryController.computeItinerary(mMapView, locationController);
				return false;
			case R.id.action_route:
				itinaryController.computeOptimizedItinerary(mapController.getMaps(), mapController.getCurrentMap());
				return false;
			case R.id.action_locate_ext_poi:
				locateExtPoi();
				return false;
			case R.id.action_clear_itinerary:
				itinaryController.clear();
				return false;
			case R.id.action_clear_all:
				itinaryController.clear();
				mMapView.clearRenderer(GfxRto.class);
				return false;
			case R.id.action_information:
				displayUIInformation();
				return false;
			case R.id.action_switch_site:
				MainActivity act = (MainActivity) getActivity();
				act.switchSite();
				return false;
		}
		return true;
	}

	@Override
	public boolean onNavigationItemSelected(int position, long id) {
		try {
			ISMap map = mapController.getMap(position);
			mMapView.changeMap(map.getId(), true, true, true);
		} catch (IllegalArgumentException iae) {
			Log.e(getClass().getSimpleName(), iae.getMessage(), iae);
		}
		return true;
	}


	//**********************************************************************************************
	// 	UI
	// *********************************************************************************************

	private void displayUIInformation() {
		View informationView = buildInformationView();
		new AlertDialog.Builder(getActivity())
				.setView(informationView)
				.create()
				.show();
	}

	@NonNull
	private View buildInformationView() {
		View informationView = LayoutInflater.from(getActivity()).inflate(R.layout
				.information_layout, null);
		TextView apiVersion, locationVersion, itineraryVersion;

		apiVersion = (TextView) informationView.findViewById(R.id.api_version_value);
		locationVersion = (TextView) informationView.findViewById(R.id.loc_version_value);
		itineraryVersion = (TextView) informationView.findViewById(R.id.iti_version_value);

		apiVersion.setText(Insiteo.getAPIVersion());
		locationVersion.setText(ISLocationProvider.getVersion());
		itineraryVersion.setText(ISItineraryProvider.getVersion());
		return informationView;
	}

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

	public void showWifiActivitionRequiredDialog() {
		new AlertDialog.Builder(getActivity())
				.setTitle(R.string.loc_activation_required)
				.setMessage(R.string.loc_wifi_activation_required)
				.setCancelable(false)
				.setPositiveButton(getString(R.string.loc_activate), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						ISLocationProvider.getInstance().activateWifi();
					}
				})
				.setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						stopLocation();
					}
				})
				.show();
	}

	public void showBLEActivitionRequiredDialog() {
		new AlertDialog.Builder(getActivity())
				.setTitle(R.string.loc_activation_required)
				.setMessage(R.string.loc_ble_activation_required)
				.setCancelable(false)
				.setPositiveButton(getString(R.string.loc_activate), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						ISLocationProvider.getInstance().activateBle();
					}
				}).setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				stopLocation();
			}
		}).show();
	}

	public void showItinaryAlert(ISError error) {
		String message = getString(R.string.error_itinerary_computation_failed) + ": " + error;
		Crouton.makeText(getActivity(), message, Style.ALERT).show();
	}

	public void showLocationButtonResourceLocalizationWithAnimation() {
		mLocationButton.setImageResource(R.drawable.localization_button);
		((AnimationDrawable) mLocationButton.getDrawable()).start();
	}

	public void updateActionBar(String[] mapNames) {
		ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();

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

	public void selectActionBarItem(int i) {
		((AppCompatActivity) getActivity()).getSupportActionBar().setSelectedNavigationItem(i);
	}

	public void initRTO(RTOController rtoController) {
		// 1 - The RTO class needs to be added to the MapViewController in order to be drawn by a GenericRenderer
		mMapView.setPriority(GfxRto.class, 14);
		// 2 - We tell the MapViewController to put a listener for touch events on this type of RTOs
		mMapView.setRTOListener(rtoController, GfxRto.class);
	}

	public void addRenderers() {
		mMapView.addRenderer(locationController.getLocationRenderer());
		mMapView.addRenderer(itinaryController.getItineraryRenderer());
	}

	public void clearRenderers() {
		mMapView.clearRenderer(GfxRto.class);
	}

	public void centerMap(int zoneId) {
		mMapView.centerMap(zoneId, true);
	}

	public void addRTOInZone(int zoneId, GfxRto rto) {
		mMapView.addRTOInZone(zoneId, rto);
	}

	public void rotate(float angle) {
		mMapView.rotate(angle, false);
	}

	public void locateExtPoi() {
		final EditText input = new EditText(getActivity());
		input.setHint(R.string.locate_ext_poi_hint);
		DialogInterface.OnClickListener onPositiveClickListener = new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				clearRenderers();
				String value = input.getText().toString();
				List<ISZonePoi> zpas = ISMapDBHelper.getZoneAssocFromExtPoi(value);

				for (int i = 0; i < zpas.size(); i++) {
					String poiExtId = zpas.get(i).getExternalPoiId();
					GfxRto rto = new GfxRto();
					rto.setLabel(poiExtId);
					rto.setLabelDisplayed(true);
					addRTOInZone(zpas.get(i).getZoneId(), rto, zpas.get(i).getOffset());
				}

				if (zpas.isEmpty()) {
					showNoExtPoiAlert();
				} else {
					centerMap(zpas.get(0).getZoneId());
				}
			}
		};
		new AlertDialog.Builder(getActivity())
				.setTitle(R.string.locate_ext_poi_title)
				.setView(input)
				.setCancelable(false)
				.setPositiveButton(R.string.action_locate, onPositiveClickListener)
				.setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
					}
				})
				.show();
	}

	public void showNoExtPoiAlert() {
		Crouton.makeText(getActivity(), R.string.error_no_ext_poi_found, Style.ALERT).show();
	}

	public void addRTOInZone(int zoneId, GfxRto rto, SimpleVector offset) {
		mMapView.addRTOInZone(zoneId, rto, offset);
	}

	public void rotateMap(int mLastLocMapID, float aAzimuth) {
		mapController.rotateMap(mLastLocMapID, aAzimuth);
	}
}