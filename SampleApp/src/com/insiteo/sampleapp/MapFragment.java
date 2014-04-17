package com.insiteo.sampleapp;

import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
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

import com.insiteo.common.CommonConstants;
import com.insiteo.common.IMapData;
import com.insiteo.common.IZone;
import com.insiteo.common.InsiteoError;
import com.insiteo.common.rendertouch.IRTO;
import com.insiteo.common.rendertouch.IRTOListener;
import com.insiteo.common.utils.Log;
import com.insiteo.common.utils.geom.Position;
import com.insiteo.geofence.GeofenceProvider;
import com.insiteo.geofence.GeofenceZone;
import com.insiteo.geofence.IGeofenceListener;
import com.insiteo.init.InitProvider;
import com.insiteo.itinerary.IItineraryRendererListener;
import com.insiteo.itinerary.IItineraryRequestListener;
import com.insiteo.itinerary.ItineraryProvider;
import com.insiteo.itinerary.ItineraryProvider.BaseRequest;
import com.insiteo.itinerary.ItineraryProvider.EOptimizationMode;
import com.insiteo.itinerary.ItineraryProvider.ItineraryRequest;
import com.insiteo.itinerary.ItineraryRenderer;
import com.insiteo.itinerary.entities.Itinerary;
import com.insiteo.itinerary.entities.Section;
import com.insiteo.location.ELocationModule;
import com.insiteo.location.ILocationListener;
import com.insiteo.location.InsLocation;
import com.insiteo.location.LocationConstants;
import com.insiteo.location.LocationProvider;
import com.insiteo.location.LocationRenderer;
import com.insiteo.map.IMapListener;
import com.insiteo.map.MapView;
import com.insiteo.map.database.MapDBHelper;
import com.insiteo.map.entities.MapData;
import com.insiteo.map.entities.ZonePoiAssoc;
import com.insiteo.map.render.EZoneAction;
import com.insiteo.sampleapp.render.GfxRto;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class MapFragment extends Fragment implements IMapListener, IRTOListener, OnClickListener, IItineraryRendererListener, IItineraryRequestListener, ILocationListener, IGeofenceListener, OnNavigationListener{


	public final static int MAP_3D_TAG = 1;
	public final static int MAP_2D_TAG = 2;

	private final static boolean PMR_ENABLED = false;
	// MAP
	private MapView mMapView = null;

	private IMapData mCurrentMap;
	private boolean mRotateMap = true;
	List<MapData> mMaps;

	// ITINERARY
	private final static boolean ITINERARY_RECOMPUTE_ACTIVATED = true;
	private final static float ITINERARY_RECOMPUTE_DISTANCE = 8;
	private ItineraryProvider mItineraryProvider;
	private ItineraryRenderer mItineraryRenderer;

	// LOCATION
	private LocationRenderer mLocationrenderer;
	private ImageButton mLocationButton;

	// GEOFENCING
	private GeofenceProvider mGeofenceProvider;
	private View mGeofenceToastView = null;
	private TextView mGeofenceToastText = null;

	private int mLastLocMapID = CommonConstants.NULL_ID;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_main, container,	false);

		CommonConstants.DEBUG = InsiteoConf.LOG_ENABLED;
		LocationConstants.DEBUG_MODE = InsiteoConf.EMBEDDED_LOG_ENABLED;



		setHasOptionsMenu(true);


		return rootView;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		initializeMapService();
		initializeLocationService();
		initializeItineraryService();
		initializeGeofencingService();
		super.onViewCreated(view, savedInstanceState);
	}

	
	@Override
	public void onPause() {
		stopLocation();
		super.onPause();
	}
	//******************************************************************************************************************
	// 	UI CALLBACKS
	// *****************************************************************************************************************

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_loc:
			locationHandler();
			break;
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.main, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean result = true;

		switch (item.getItemId()) {

		case R.id.action_itinerary: 
			computeItinerary();
			result = false;
			break;

		case R.id.action_route: 
			computeOptimizedItinerary();
			result = false;
			break;

		case R.id.action_locate_ext_poi: 
			locateExtPoi();
			result = false;
			break;

		case R.id.action_clear_itinerary: 
			mItineraryRenderer.clear();
			result = false;
			break;


		case R.id.action_clear_all: 
			mItineraryRenderer.clear();
			mMapView.clearRenderer(GfxRto.class);
			result = false;
			break;

		}


		return result;
	}

	@Override
	public boolean onNavigationItemSelected(int position, long id) {
		if(mMaps != null && mMaps.size() > position){
			MapData map = mMaps.get(position);
			mMapView.changeMap(map.getId(), false, false, false);
		}
		return true;
	}
	
	//******************************************************************************************************************
	// 	MAP SERVICE
	// *****************************************************************************************************************

	private void initializeMapService(){
		mMapView = (MapView) getView().findViewById(R.id.map);
		
		// The MapView listener. By default the listener is set to the context (if it implements IMapListener) that created the View.
		// In the case of fragment we have to explicitly set it.
		mMapView.setListener(this);
	}

	private void initializeRTO(){
		// 1 - The RTO class needs to be added to the MapViewController in order to be drawn by a GenericRenderer
		mMapView.setPriority(GfxRto.class, 14);

		// 2 - We tell the MapViewController to put a listener for touch events on this type of RTOs
		mMapView.setRTOListener(this, GfxRto.class);
	}
	
	

	private void setMapNavigationList(){
		
		// Retrieve the list MapData associated to the current site.
		mMaps = MapDBHelper.getMaps(false);
		final String[] mapNames = new String[mMaps.size()];

		int i = 0;
		for (MapData map : mMaps) {
			mapNames[i] = map.getName();
			i++;
		}

		ActionBar actionBar = getActivity().getActionBar();

		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

		// Set up the dropdown list navigation in the action bar.
		actionBar.setListNavigationCallbacks(
				// Specify a SpinnerAdapter to populate the dropdown list.
				new ArrayAdapter<String>(actionBar.getThemedContext(),
						android.R.layout.simple_list_item_1,
						android.R.id.text1, mapNames), MapFragment.this);
	}

	/**
	 * Called when the map displayed by the MapView changes.
	 * @param mapId the new map's ID
	 * @param mapName the new map's name
	 */
	@Override
	public void onMapChanged(final int mapId, final String mapName) {		
		getActivity().runOnUiThread(new Runnable() {

			@Override
			public void run() {
				//set current map data
				mCurrentMap = InitProvider.getInstance().getMap(mapId);

				for(int i = 0; i < mMaps.size(); i++){
					if(mMaps.get(i).getId() == mapId) getActivity().getActionBar().setSelectedNavigationItem(i);
				}
			}
		});
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
		getActivity().runOnUiThread(new Runnable() {

			@Override
			public void run() {
				setMapNavigationList();

				initializeRTO();

				// Add the location and itinerary renderers to the MapView to enable their display.
				mMapView.addRenderer(mLocationrenderer);
				mMapView.addRenderer(mItineraryRenderer);

				mCurrentMap = InitProvider.getInstance().getMap(aMapID);
			}
		});
	}


	/**
	 * This event is triggered when a Zone object was clicked, and its associated action is custom, 
	 * so it can't be handled by MapController.
	 * @param aZoneID the clicked zone's ID
	 * @param aActionType the type of action contained in this zone
	 * @param aActionParam the action parameter
	 */
	@Override
	public void onZoneClicked(int zoneID, EZoneAction aActionType, String aActionParam) {

		// Clear the MapView from all the GfxRto previously rendered.
		mMapView.clearRenderer(GfxRto.class);

		// Get all the external POI that are associated to this zone and add them on the map.
		List<ZonePoiAssoc> zpas = MapDBHelper.getPoiAssocFromZone(zoneID, true);

		for(int i = 0; i < zpas.size(); i++) {
			String poiExtId = zpas.get(i).getPoiExtID();			
			GfxRto rto = new GfxRto(i, null, poiExtId);
			
			// Apply a particular offset to this rto. This is used to have multiple rto in the same zone without superposition.
			rto.setZoneOffset(zpas.get(i).getOffset());
			
			mMapView.addRTOInZone(zoneID, rto);
		}

		// Center the map on this zone with animation.
		mMapView.centerMap(zoneID, true);
	}

	/**
	 * Called when the MapView is clicked
	 */
	@Override
	public void onMapClicked() {
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

	
	public void locateExtPoi(){
		getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				final AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
				alert.setTitle(R.string.locate_ext_poi_title);
				final EditText input = new EditText(getActivity());
				input.setHint(R.string.locate_ext_poi_hint);
				alert.setView(input);
				alert.setCancelable(false);
				alert.setPositiveButton(R.string.action_locate, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						String value = input.getText().toString();
						
						List<ZonePoiAssoc> zpas = MapDBHelper.getZoneAssocFromExtPoi(value);
						
						for(int i = 0; i < zpas.size(); i++) {
							String poiExtId = zpas.get(i).getPoiExtID();			
							GfxRto rto = new GfxRto(i, null, poiExtId);
							mMapView.addRTOInZone(zpas.get(i).getZoneID(), rto);
						}

						if(!zpas.isEmpty()) {
							mMapView.centerMap(zpas.get(0).getZoneID(), true);						
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
		});	
	}
	
	//******************************************************************************************************************
	// 	RTO SERVICE 
	// *****************************************************************************************************************

	/**
	 * Called when a RTO is clicked
	 * @param aObject the RTO that was clicked
	 * @param aZone the zone containing this RTO (if RTO was put in a zone), or null if RTO was not set in a zone
	 */
	@Override
	public void onRTOClicked(IRTO rto, IZone zone) {
	}

	/**
	 * Called when a RTO is moved
	 * @param aObject the RTO that was moved
	 * @param aZone the zone containing this RTO (if RTO was put in a zone), or null if RTO was not set in a zone
	 */
	@Override
	public void onRTOMoved(IRTO rto, IZone zone) {
	}

	/**
	 * Called when a RTO is released
	 * @param aObject the RTO that was released
	 * @param aZone the zone containing this RTO (if RTO was put in a zone), or null if RTO was not set in a zone
	 */
	@Override
	public void onRTOReleased(IRTO rto, IZone zone) {
	}

	/**
	 * Called when a RTO is selected
	 * @param aObject the RTO that was selected
	 * @param aZone the zone containing this RTO (if RTO was put in a zone), or null if RTO was not set in a zone
	 */
	@Override
	public void onRTOSelected(IRTO rto, IZone zone) {
	}	

	//******************************************************************************************************************
	// 	ITINERARY SERVICE 
	// *****************************************************************************************************************

	private void initializeItineraryService(){
		// create an itinerary provider, as we don't use location. 
		// When using location, it is easier to get itinerary provider from location provider)
		mItineraryProvider = (ItineraryProvider) LocationProvider.getInstance().getModule(ELocationModule.ITINERARY);
		mItineraryProvider.setDynamicMode(true);

		//get itinerary renderer linked to provider
		mItineraryRenderer = (ItineraryRenderer) mItineraryProvider.getRenderer(getResources());
		mItineraryRenderer.setPriority(10);
		mItineraryRenderer.setLinkToEnds(true, true);
		mItineraryRenderer.setListener(this);

	}

	/**
	 * This method shows how to compute an itinerary between two points
	 */
	private void computeItinerary() {
		//hide the old itinerary if it exists
		mItineraryRenderer.setDisplayEnabled(false);

		//departure is map's top left corner, arrival is bottom right corner.
		int mapID = mMapView.getMapId();            

		// If the location is started will request an itinerary from our current position
		if (LocationProvider.getInstance().isStarted()) {

			Position arrival = new Position(mapID, 168, 100);
			mItineraryProvider.requestItineraryFromCurrentLocation(arrival, true, this, PMR_ENABLED);
		} 
		// Otherwise it will request an itinerary from 2 fake positions
		else {
			int startMapId = mMaps.get(0).getId();
			int destMapId = mMaps.get(1).getId();

			Position departure = new Position(startMapId, 40, 40);
			Position arrival = new Position(destMapId, 168, 100);
			mItineraryProvider.requestItinerary(departure, arrival, this, PMR_ENABLED);
		}

	}

	/**
	 * This method shows how to compute a route between different positions
	 */
	private void computeOptimizedItinerary(){

		int positionNbr = 4;

		ArrayList<Position> pos = new ArrayList<Position>();

		for (int i = 0; i < positionNbr; i++) {
			double x = Math.random() * 300;
			double y = Math.random() * 300;

			Position p = new Position(1, x, y);
			pos.add(p);
		}

		for (int i = 0; i < positionNbr; i++) {
			double x = Math.random() * 300;
			double y = Math.random() * 300;

			Position p = new Position(2, x, y);
			pos.add(p);
		}

		mItineraryProvider.requestOptimizedItinerary(pos, EOptimizationMode.EOptimizationModeNearestNeighbourShortestPath, true, false, this, false);

	}

	/**
	 * Callback fired when the itinerary of the last Request changed (when location is updated for example) 
	 * @param aRequest the related request
	 * @param aDistanceToIti the distance between the user location and the itinerary (in meter)
	 */
	@Override
	public void onItineraryChanged(BaseRequest aRequest, float aDistanceToIti) {
		if (aRequest instanceof ItineraryRequest && LocationProvider.getInstance().isStarted() && ITINERARY_RECOMPUTE_ACTIVATED) {	
			if (aDistanceToIti > ITINERARY_RECOMPUTE_DISTANCE || aDistanceToIti == -1) {
				computeItinerary();
			}
		}
	}

	/**
	 * Callback for itinerary request completion
	 * @param aSuccess true if the request was successful
	 * @param aRequest the request object containing itinerary data
	 * @param error an error object containing a code and a message, null if request was successful 
	 */
	@Override
	public void onItineraryRequestDone(boolean aSuccess, BaseRequest aRequest, final InsiteoError error) {
		if(aSuccess) mItineraryRenderer.setDisplayEnabled(true);
	}

	/**
	 * Callback fired when an instruction (a list of sections linked by edges) is touched 
	 * @param aItinerary the itinerary that contains the instruction
	 * @param aInstructionIndex the index of the instruction
	 */
	@Override
	public void onInstructionClicked(Itinerary aItinerary, int aInstructionIndex) {
	}

	/**
	 * Callback fired when a waypoint corresponding to a map change is clicked 
	 * (ie : the last waypoint of the displayed map, if this waypoint is not the end of itinerary) 	
	 * @param aNextPosition the position of the next itinerary section
	 */
	@Override
	public void onMapSwitcherClicked(Position aNextPosition) {
		if(aNextPosition != null){
			mMapView.centerMap(aNextPosition, true);
		}
	}

	/**
	 * Callback fired when a waypoint (ie : a graphical point) is touched
	 * @param aItinerary the itinerary that contains the section
	 * @param aInstructionIndex the index of the instruction that contains the section 
	 * @param aSection the section corresponding to the touched waypoint
	 */
	@Override
	public void onWaypointClicked(Itinerary aItinerary, int aInstructionIndex, Section aSection) {
	}

	//******************************************************************************************************************
	// GEOFENCING SERVICE 
	// *****************************************************************************************************************

	private void initializeGeofencingService(){
		// geofencing
		mGeofenceProvider = (GeofenceProvider) LocationProvider.getInstance().getModule(ELocationModule.GEOFENCING);
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
	public void onGeofenceUpdate(List<GeofenceZone> aEnteredZones, List<GeofenceZone> aStayedZones, List<GeofenceZone> aLeftZones) {
		Log.d("Geofencing", "onGeofenceUpdate " + aEnteredZones.size() + " " + aStayedZones.size() + " " + aLeftZones.size());	

		final List<GeofenceZone> zones = aEnteredZones;

		for (GeofenceZone z : zones) {
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

	//******************************************************************************************************************
	// LOCATION SERVICE
	// *****************************************************************************************************************

	private void initializeLocationService(){

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
		mLocationrenderer = (LocationRenderer)LocationProvider.getInstance().getRenderer(getResources());
		mLocationrenderer.setPriority(11);
	}

	private void locationHandler() {
		if (!LocationProvider.getInstance().isStarted()) {
			mLocationButton.setImageResource(R.drawable.localization_button);
			((AnimationDrawable) mLocationButton.getDrawable()).start();
			startLocation();
		} else if(LocationProvider.getInstance().isStarted() && !mRotateMap){
			mRotateMap = true;
		} else {
			stopLocation();
			mRotateMap = false;
		}
	}

	/**
	 * Stops the location computing process.
	 */
	public void stopLocation(){
		LocationProvider.getInstance().stop();
		mLocationButton.setImageResource(R.drawable.ic_location_off);
		mLastLocMapID = CommonConstants.NULL_ID;
		mMapView.rotateAngle(0f, false);
	}

	/**
	 * Starts the location computing process using the flags that were defined in the LauncherActivity
	 */
	private void startLocation() {
		LocationProvider.getInstance().start(getActivity(), this, InsiteoConf.LOCATION_FLAGS); 
	}

	/**
	 * Called when the location computing process has been started.
	 * @param aSuccess the state of the process initialization
	 * @param aError the error returned in case of failure
	 */
	@Override
	public void onLocationInitDone(final boolean aSuccess, final InsiteoError aError) {

	}

	/**
	 * This listener gets triggered each time the location process has been able to compute a new position
	 * @param aLocation the last calculated position
	 */
	@Override
	public void onLocationReceived(final InsLocation aLocation) {
		getActivity().runOnUiThread(new Runnable() {			
			@Override
			public void run() {
				mLocationButton.setImageResource(R.drawable.ic_location_on);				
			}
		});

		if (aLocation != null) {
			/**
			 * this method animate the map to be centered on the given position, here the user's position
			 */
			mMapView.centerMap(aLocation.getPosition(), true);

			mLastLocMapID = aLocation.getMapID();
		}	
	}

	/**
	 * This callback gets triggered when the accuracy returned by the compass sensors is too low. This could lead to imprecise location.
	 */
	@Override
	public void onCompassAccuracyTooLow() {
	}

	/**
	 * This callback gets triggered if the GPS is required and the API did not managed to start it.
	 */
	@Override
	public void onNeedToActivateGPS() {
	}

	@Override
	public void onAzimuthReceived(final float aAzimuth) {
		if (mRotateMap) {
			float angle = aAzimuth;
			if (mCurrentMap != null && mLastLocMapID == mCurrentMap.getId()) {
				boolean isMapOriented = mCurrentMap.isOriented();
				if (isMapOriented) {
					float mapAzimuth =  mCurrentMap.getAzimuth();
					angle = mapAzimuth - aAzimuth;
				}
			}
			mMapView.rotateAngle(angle - mMapView.getScreenOrientation(), false);
		}
	}

	/**
	 * This gets called when the LocationProvider has lost your position.
	 * @param aLastKnownLocation the last position that has been computed
	 */
	@Override
	public void onLocationLost(InsLocation aLastKnownLocation) {
		// TODO Auto-generated method stub
	}

	/**
	 * This callback gets called if none of the known AP dedicated to the location process of this site were detected. This might means that the users is not on the site.
	 */
	@Override
	public void noRegisteredBeaconDetected() {
		// TODO Auto-generated method stub
	}   

	/**
	 * This callback gets triggered when the LocationService requires the WIFI to be switch on. It enable to ask the user for this permission.
	 */
	@Override
	public void onWifiActivationRequired() {
		getActivity().runOnUiThread(new Runnable() {			
			@Override
			public void run() {
				final AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
				alert.setMessage(R.string.loc_wifi_activation_required);
				alert.setCancelable(false);
				alert.setPositiveButton(getString(R.string.loc_activate), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						LocationProvider.getInstance().activateWifi();
					}
				});
				alert.setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						stopLocation();				
					}
				});
				alert.show();
			}
		});		
	}

	/**
	 * This callback gets triggered when the LocationService requires the BLE to be switch on. It enable to ask the user for this permission.
	 */
	@Override
	public void onBleActivationRequired() {
		getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				final AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
				alert.setMessage(R.string.loc_ble_activation_required);
				alert.setCancelable(false);
				alert.setPositiveButton(getString(R.string.loc_activate), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						LocationProvider.getInstance().activateBle();
					}
				});
				alert.setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						stopLocation();				
					}
				});
				alert.show();
			}
		});	
	}

}
