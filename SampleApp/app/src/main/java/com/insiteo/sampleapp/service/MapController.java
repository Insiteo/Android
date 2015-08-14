package com.insiteo.sampleapp.service;

import android.content.DialogInterface;
import android.util.Log;

import com.insiteo.lbs.common.utils.geometry.ISPosition;
import com.insiteo.lbs.map.ISIMapListener;
import com.insiteo.lbs.map.database.ISMapDBHelper;
import com.insiteo.lbs.map.entities.ISMap;
import com.insiteo.lbs.map.entities.ISZone;
import com.insiteo.lbs.map.entities.ISZonePoi;
import com.insiteo.lbs.map.render.ISEZoneAction;
import com.insiteo.sampleapp.MapViewController;
import com.insiteo.sampleapp.render.GfxRto;

import java.util.List;

public class MapController implements ISIMapListener {

	public final static String TAG = MapController.class.getSimpleName();
	private RTOController rtoController;
	private ISMap mCurrentMap;

	List<ISMap> mMaps;
	private MapViewController mapViewController;
	private LocationController locationController;

	public MapController(MapViewController mapViewController, LocationController locationController) {
		this.locationController = locationController;
		this.mapViewController = mapViewController;
		this.rtoController = new RTOController();
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
		mapViewController.updateActionBar(mapNames);
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
				mapViewController.selectActionBarItem(i);
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
		mapViewController.initRTO(rtoController);
		mapViewController.addRenderers();
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
		mapViewController.clearRenderers();

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
			mapViewController.addRTOInZone(zone.getId(), rto);
		}
		// Center the map on this zone with animation.
		mapViewController.centerMap(zone.getId());
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

	public void rotateMap(int mLastLocMapID, float aAzimuth) {
		float angle = aAzimuth;
		if (mCurrentMap != null && mLastLocMapID == mCurrentMap.getId()) {
			boolean isMapOriented = mCurrentMap.isOriented();
			if (isMapOriented) {
				float mapAzimuth = mCurrentMap.getAzimuth();
				angle = mapAzimuth - aAzimuth;
			}
		}
		mapViewController.rotate(angle);
	}

	public ISMap getCurrentMap() {
		return mCurrentMap;
	}

	public List<ISMap> getMaps() {
		return mMaps;
	}

	public ISMap getMap(int position) {
		if (mMaps != null && mMaps.size() > position) {
			return mMaps.get(position);
		}
		throw new IllegalArgumentException("Cannot find map for position" + position);
	}
}
