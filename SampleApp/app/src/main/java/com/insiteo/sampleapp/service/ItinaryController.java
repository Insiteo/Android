package com.insiteo.sampleapp.service;

import android.content.res.Resources;

import com.insiteo.lbs.common.ISError;
import com.insiteo.lbs.common.utils.geometry.ISPosition;
import com.insiteo.lbs.itinerary.ISIItineraryRendererListener;
import com.insiteo.lbs.itinerary.ISIItineraryRequestListener;
import com.insiteo.lbs.itinerary.ISItineraryProvider;
import com.insiteo.lbs.itinerary.ISItineraryRenderer;
import com.insiteo.lbs.itinerary.entities.ISItinerary;
import com.insiteo.lbs.itinerary.entities.ISItinerarySection;
import com.insiteo.lbs.location.ISELocationModule;
import com.insiteo.lbs.location.ISLocationProvider;
import com.insiteo.lbs.map.ISMapView;
import com.insiteo.lbs.map.entities.ISMap;
import com.insiteo.sampleapp.MapViewController;

import java.util.ArrayList;
import java.util.List;

public class ItinaryController implements ISIItineraryRendererListener, ISIItineraryRequestListener {

	private final static boolean ITINERARY_RECOMPUTE_ACTIVATED = true;
	private final static float MAX_RECOMPUTATION_DISTANCE = 5;
	private ISItineraryProvider mItineraryProvider;
	private ISItineraryRenderer mItineraryRenderer;
	private final static boolean PMR_ENABLED = false;
	private MapViewController mapViewController;

	public ItinaryController(MapViewController mapViewController) {
		this.mapViewController = mapViewController;
	}

	public void clear() {
		mItineraryRenderer.clear();
	}

	public ISItineraryRenderer getItineraryRenderer() {
		return mItineraryRenderer;
	}

	public void init(Resources resources) {
		// create an itinerary provider, as we don't use location.
		// When using location, it is easier to get itinerary provider from location provider)
		mItineraryProvider = (ISItineraryProvider) ISLocationProvider.getInstance().getModule(ISELocationModule.ITINERARY);
		mItineraryProvider.setDynamicMode(true);

		//get itinerary renderer linked to provider
		mItineraryRenderer = mItineraryProvider.getRenderer(resources);
		mItineraryRenderer.setPriority(10);
		mItineraryRenderer.setLinkToEnds(true, true);
		mItineraryRenderer.setListener(this);
	}

	/**
	 * Callback fired when the itinerary of the last request changed (when location is updated for example).
	 * This method can be used to ask for a new itinerary if the user is now too far from the itinerary (we usually recompute
	 * the itinerary over a distance of 5 meters).
	 *
	 * @param aRequest       the related request
	 * @param aDistanceToIti the distance between the user location and the itinerary (in meter)
	 */
	@Override
	public void onItineraryChanged(ISItineraryProvider.ISBaseRequest aRequest, float aDistanceToIti) {
		if (aRequest instanceof ISItineraryProvider.ISItineraryRequest && ISLocationProvider.getInstance().isStarted()) {
			if (aDistanceToIti > MAX_RECOMPUTATION_DISTANCE || aDistanceToIti == -1) {
				aRequest.recompute();
			}
		}
	}

	/**
	 * Callback for itinerary request completion
	 *
	 * @param aSuccess true if the request was successful
	 * @param aRequest the request object containing itinerary data
	 * @param error    an error object containing a code and a message, null if request was successful
	 */
	@Override
	public void onItineraryRequestDone(boolean aSuccess, ISItineraryProvider.ISBaseRequest aRequest, final ISError error) {
		if (aSuccess) {
			mItineraryRenderer.setDisplayEnabled(true);
		} else {
			mapViewController.showItinaryAlert(error);
		}
	}

	/**
	 * Callback fired when an instruction (a list of sections linked by edges) is touched
	 *
	 * @param aItinerary        the itinerary that contains the instruction
	 * @param aInstructionIndex the index of the instruction
	 */
	@Override
	public void onInstructionClicked(ISItinerary aItinerary, int aInstructionIndex) {
	}

	/**
	 * Callback fired when a waypoint corresponding to a map change is clicked
	 * (ie : the last waypoint of the displayed map, if this waypoint is not the end of itinerary)
	 *
	 * @param aNextPosition the position of the next itinerary section
	 */
	@Override
	public void onMapSwitcherClicked(ISPosition aNextPosition) {
		if (aNextPosition != null) {
			mapViewController.centerMap(aNextPosition);
		}
	}

	/**
	 * Callback fired when a waypoint (ie : a graphical point) is touched
	 *
	 * @param aItinerary        the itinerary that contains the section
	 * @param aInstructionIndex the index of the instruction that contains the section
	 * @param aSection          the section corresponding to the touched waypoint
	 */
	@Override
	public void onWaypointClicked(ISItinerary aItinerary, int aInstructionIndex,
	                              ISItinerarySection aSection) {
	}

	/**
	 * This method shows how to compute an itinerary between two points
	 *
	 * @param mMapView
	 * @param locationController
	 */
	public void computeItinerary(ISMapView mMapView, LocationController locationController) {
		//hide the old itinerary if it exists
		mItineraryRenderer.setDisplayEnabled(false);

		ISPosition arrival = new ISPosition(mMapView.getMapId(), 20, 20);
		mItineraryProvider.requestItineraryFromCurrentLocation(arrival, true, this, PMR_ENABLED);

		// If the location is started will request an itinerary from our current position
		if (!ISLocationProvider.getInstance().isStarted()) {
			mapViewController.showLocationButtonResourceLocalizationWithAnimation();
			locationController.startLocation();
		}
	}

	/**
	 * This method shows how to compute a route between different positions
	 *
	 * @param mMaps
	 * @param mCurrentMap
	 */
	public void computeOptimizedItinerary(List<ISMap> mMaps, ISMap mCurrentMap) {
		int positionNbr = 4;
		ArrayList<ISPosition> pos = new ArrayList<ISPosition>();
		for (int i = 0; i < positionNbr; i++) {
			double x = Math.random() * 50;
			double y = Math.random() * 50;
			if (mMaps.size() > 1 && i > positionNbr / 2) {
				ISMap otherMap = null;
				for (ISMap map : mMaps) {
					if (map.getId() != mCurrentMap.getId()) {
						otherMap = map;
						break;
					}
				}
				if (otherMap != null) {
					ISPosition p = new ISPosition(otherMap.getId(), x, y);
					pos.add(p);
				}
			} else {
				ISPosition p = new ISPosition(mCurrentMap.getId(), x, y);
				pos.add(p);
			}

		}

		mItineraryProvider.requestOptimizedItinerary(pos, ISItineraryProvider.ISEOptimizationMode.NearestNeighbourShortestPath, true, false, this, false);

	}
}
