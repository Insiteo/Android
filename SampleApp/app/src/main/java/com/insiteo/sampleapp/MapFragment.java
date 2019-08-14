package com.insiteo.sampleapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
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
import android.view.MotionEvent;
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
import com.insiteo.lbs.geofence.ISGeofenceArea;
import com.insiteo.lbs.geofence.ISGeofenceProvider;
import com.insiteo.lbs.geofence.ISIGeofenceListener;
import com.insiteo.lbs.itinerary.ISIItineraryRendererListener;
import com.insiteo.lbs.itinerary.ISIItineraryRequestListener;
import com.insiteo.lbs.itinerary.ISItineraryProvider;
import com.insiteo.lbs.itinerary.ISItineraryRenderer;
import com.insiteo.lbs.itinerary.entities.ISItinerary;
import com.insiteo.lbs.itinerary.entities.ISItinerarySection;
import com.insiteo.lbs.location.ISELocationModule;
import com.insiteo.lbs.location.ISILocationListener;
import com.insiteo.lbs.location.ISLocation;
import com.insiteo.lbs.location.ISLocationProvider;
import com.insiteo.lbs.location.ISLocationRenderer;
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

import java.util.ArrayList;
import java.util.List;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class MapFragment extends Fragment implements ISIMapListener, ISIRTOListener, OnClickListener,
		ISIItineraryRendererListener, ISIItineraryRequestListener, ISILocationListener, ISIGeofenceListener, ActionBar.OnNavigationListener {

	public final static String TAG = MapFragment.class.getSimpleName();


	private final static boolean PMR_ENABLED = false;

	// MAP
	private ISMapView mMapView = null;

	private ISMap mCurrentMap;
	private boolean mRotateMap = false;
	List<ISMap> mMaps;

	// ITINERARY
	private final static boolean ITINERARY_RECOMPUTE_ACTIVATED = true;
	private final static float MAX_RECOMPUTATION_DISTANCE = 5;
	private ISItineraryProvider mItineraryProvider;
	private ISItineraryRenderer mItineraryRenderer;

	// LOCATION
	private ISLocationRenderer mLocationrenderer;
	private ImageButton mLocationButton;
	private ImageButton mRenderButton;
	private ISLocation mLastLocation;
	private boolean mCenterOnPosition = true;

	// GEOFENCING
	private ISGeofenceProvider mGeofenceProvider;
	private View mGeofenceToastView = null;
	private TextView mGeofenceToastText = null;

	private int mLastLocMapID = -1;


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
		initializeLocationService();
		initializeItineraryService();
		initializeGeofencingService();
		super.onViewCreated(view, savedInstanceState);
	}
	
	@Override
	public void onResume() {
		/* It is best practice to pause the MapView component on its parent Fragment (or Activity) respective method */ 
		//mMapView.onResume();
		super.onResume();
	}


	@Override
	public void onPause() {
		stopLocation();

		/* It is best practice to pause the MapView component on its parent Fragment (or Activity) respective method */ 
		//mMapView.onPause();
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
		case R.id.btn_toggle_render:
			toggleRender();
			break;
		}
	}

	boolean isRendering = true;
	public void toggleRender() {
		if(isRendering) {
			isRendering = false;
			mMapView.stopRendering();
		} else {
			isRendering = true;
			mMapView.startRendering();
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
			if(mItineraryRenderer != null) {
				mItineraryRenderer.clear();
			}
			result = false;
			break;


		case R.id.action_clear_all:
			if(mItineraryRenderer != null) {
				mItineraryRenderer.clear();
			}
			mMapView.clearRenderer(GfxRto.class);
			result = false;
			break;

        case R.id.action_information:
                displayUIInformation();
                result = false;
                break;

            case R.id.action_switch_site:
                MainActivity act = (MainActivity) getActivity();
                //act.switchSite();
				result = false;
				break;

		}
		return result;
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

	private void initializeMapService(){
		mMapView = (ISMapView) getView().findViewById(R.id.map);

		// The MapView listener. By default the listener is set to the context (if it implements IMapListener) that created the View.
		// In the case of fragment we have to explicitly set it.
		mMapView.setListener(getContext(), this);
	}

	private void initializeRTO(){
		// 1 - The RTO class needs to be added to the MapViewController in order to be drawn by a GenericRenderer
		mMapView.setPriority(GfxRto.class, 14);

		// 2 - We tell the MapViewController to put a listener for touch events on this type of RTOs
		mMapView.setRTOListener(this, GfxRto.class);
	}

	private void setMapNavigationList(){

		// Retrieve the list MapData associated to the current site.
		mMaps = ISMapDBHelper.getMaps(false);
		final String[] mapNames = new String[mMaps.size()];

		int i = 0;
		for (ISMap map : mMaps) {
			mapNames[i] = map.getName();
			i++;
		}

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
				((AppCompatActivity) getActivity()).getSupportActionBar().setSelectedNavigationItem(i);
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
		Log.d(TAG, "onMapViewReady");
        setMapNavigationList();

        initializeRTO();

        // Add the location and itinerary renderers to the MapView to enable their display.
        mMapView.addRenderer(mLocationrenderer);
        mMapView.addRenderer(mItineraryRenderer);

        mCurrentMap = ISMapDBHelper.getMap(aMapID);
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
		Log.d(TAG, "onZoneClicked");

		// Clear the MapView from all the GfxRto previously rendered.
		mMapView.clearRenderer(GfxRto.class);

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
		//Log.d(TAG, "onMapMoved");
		mCenterOnPosition = false;
	}

	/**
	 * Called when the zoomlevel has changed	
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

	@Override
	public void onDoubleTap(MotionEvent e) {
	}

	@Override
	public void onLongPress(MotionEvent e) {
	}


	public void locateExtPoi(){
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

                for(int i = 0; i < zpas.size(); i++) {
                    String poiExtId = zpas.get(i).getExternalPoiId();
                    GfxRto rto = new GfxRto();
                    rto.setLabel(poiExtId);
                    rto.setLabelDisplayed(true);
                    mMapView.addRTOInZone(zpas.get(i).getZoneId(), rto, zpas.get(i).getOffset());
                }

                if(!zpas.isEmpty()) {
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

	//******************************************************************************************************************
	// 	ITINERARY SERVICE 
	// *****************************************************************************************************************

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

	/**
	 * This method shows how to compute an itinerary between two points
	 */
	private void computeItinerary() {
		if(mItineraryRenderer != null) {
			//hide the old itinerary if it exists
			mItineraryRenderer.setDisplayEnabled(false);
		}

		if(mItineraryProvider != null) {
			ISPosition arrival = new ISPosition(mMapView.getMapId(), 20, 20);
			mItineraryProvider.requestItineraryFromCurrentLocation(arrival, true, this, PMR_ENABLED);
		}
		// If the location is started will request an itinerary from our current position
		if (!ISLocationProvider.getInstance().isStarted()) {
			startLocation();
		} 

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
				
			} else {
                ISPosition p = new ISPosition(mCurrentMap.getId(), x, y);
				pos.add(p);
			}
			
		}

		if(mItineraryProvider != null) {
			mItineraryProvider.requestOptimizedItinerary(pos, ISItineraryProvider.ISEOptimizationMode.NearestNeighbourShortestPath, true, false, this, false);
		}
	}

	/**
	 * Callback fired when the itinerary of the last request changed (when location is updated for example). 
	 * This method can be used to ask for a new itinerary if the user is now too far from the itinerary (we usually recompute
	 * the itinerary over a distance of 5 meters).
	 * @param aRequest the related request
	 * @param aDistanceToIti the distance between the user location and the itinerary (in meter)
	 */
	@Override
	public void onItineraryChanged(ISItineraryProvider.ISBaseRequest aRequest, float aDistanceToIti) {
		if (aRequest instanceof ISItineraryProvider.ISItineraryRequest && ISLocationProvider.getInstance().isStarted() && ITINERARY_RECOMPUTE_ACTIVATED) {
			if (aDistanceToIti > MAX_RECOMPUTATION_DISTANCE || aDistanceToIti == -1) {
				aRequest.recompute();
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
	public void onItineraryRequestDone(boolean aSuccess, ISItineraryProvider.ISBaseRequest aRequest, final ISError error) {
		if(aSuccess) {
			if(mItineraryRenderer != null) {
				mItineraryRenderer.setDisplayEnabled(true);
			}
		}
		else {
            String message = getString(R.string.error_itinerary_computation_failed) + ": " + error;
            Crouton.makeText(getActivity(), message, Style.ALERT).show();
        }
	}

	/**
	 * Callback fired when an instruction (a list of sections linked by edges) is touched 
	 * @param aItinerary the itinerary that contains the instruction
	 * @param aInstructionIndex the index of the instruction
	 */
	@Override
	public void onInstructionClicked(ISItinerary aItinerary, int aInstructionIndex) {
	}

	/**
	 * Callback fired when a waypoint corresponding to a map change is clicked 
	 * (ie : the last waypoint of the displayed map, if this waypoint is not the end of itinerary) 	
	 * @param aNextPosition the position of the next itinerary section
	 */
	@Override
	public void onMapSwitcherClicked(ISPosition aNextPosition) {
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
	public void onWaypointClicked(ISItinerary aItinerary, int aInstructionIndex,
                                  ISItinerarySection aSection) {
	}

	//******************************************************************************************************************
	// GEOFENCING SERVICE 
	// *****************************************************************************************************************

	private void initializeGeofencingService(){
		// geofencing
		mGeofenceProvider = (ISGeofenceProvider) ISLocationProvider.getInstance().getModule(ISELocationModule.GEOFENCING);
		if (mGeofenceProvider != null) {
			mGeofenceProvider.setListener(this);
			mGeofenceToastView = LayoutInflater.from(getActivity()).inflate(R.layout.geofencing_toast, null);
			mGeofenceToastText = (TextView)mGeofenceToastView.findViewById(R.id.geofence_text);
		}

	}

	/**
	 * Called when geofencing module has new data available.
	 * @param aEnteredZones list of zones that location has just entered  
	 * @param aStayedZones list of zones where location has stayed for a certain amount of time
	 * @param aLeftZones list of zones that location has just left
	 */
	@Override
	public void onGeofenceUpdate(List<ISGeofenceArea> aEnteredZones, List<ISGeofenceArea> aStayedZones, List<ISGeofenceArea> aLeftZones) {
		Log.d("Geofencing", "onGeofenceUpdate " + aEnteredZones.size() + " " + aStayedZones.size() + " " + aLeftZones.size());	

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

	//******************************************************************************************************************
	// LOCATION SERVICE
	// *****************************************************************************************************************

	private void initializeLocationService(){

		mLocationButton = (ImageButton) getView().findViewById(R.id.btn_loc);
		mLocationButton.setOnClickListener(this);

		mRenderButton = (ImageButton) getView().findViewById(R.id.btn_toggle_render);
		mRenderButton.setOnClickListener(this);

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
		mLocationrenderer = (ISLocationRenderer) ISLocationProvider.getInstance().getRenderer(getResources());
		mLocationrenderer.setPriority(11);
	}

	private void locationHandler() {
		if (!ISLocationProvider.getInstance().isStarted()) {
			startLocation();
		} else if(ISLocationProvider.getInstance().isStarted() && !mRotateMap){
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
		ISLocationProvider.getInstance().stop();
		mLocationButton.setImageResource(R.drawable.ic_location_off);
		mLastLocMapID = -1;
		mMapView.rotate(0f, false);

		mLastLocation = null;
	}

	/**
	 * Starts the location computing process using the flags that were defined in the LauncherActivity
	 */
	private void startLocation() {
		mLocationButton.setImageResource(R.drawable.localization_button);
		((AnimationDrawable) mLocationButton.getDrawable()).start();

		ISLocationProvider.getInstance().start(21, this);
	}

	/**
	 * Called when the location computing process has been started.
	 * @param aError the error returned in case of failure
	 */
	@Override
	public void onLocationInitDone(final ISError aError) {
	}

	/**
	 * This listener gets triggered each time the location process has been able to compute a new position
	 * @param aLocation the last calculated position
	 */
	@Override
	public void onLocationReceived(final ISLocation aLocation) {
        mLocationButton.setImageResource(R.drawable.ic_location_on);

		mLastLocation = aLocation;

		/**
		 * this method animate the map to be centered on the given position, here the user's position
		 */
		if(mCenterOnPosition) mMapView.centerMap(aLocation.getPosition(), true);

		mLastLocMapID = aLocation.getMapID();

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
		if (mRotateMap && mLastLocation != null) {
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
	}

	/**
	 * This gets called when the LocationProvider has lost your position.
	 * @param aLastKnownLocation the last position that has been computed
	 */
	@Override
	public void onLocationLost(ISLocation aLastKnownLocation) {
	}

	/**
	 * This callback gets called if none of the known AP dedicated to the location process of this site were detected. This might means that the users is not on the site.
	 */
	@Override
	public void noRegisteredBeaconDetected() {
	}   

	/**
	 * This callback gets triggered when the LocationService requires the WIFI to be switch on. It enable to ask the user for this permission.
	 */
	@Override
	public void onWifiActivationRequired() {
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

	/**
	 * This callback gets triggered when the LocationService requires the BLE to be switch on. It enable to ask the user for this permission.
	 */
	@Override
	public void onBleActivationRequired() {
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


}
