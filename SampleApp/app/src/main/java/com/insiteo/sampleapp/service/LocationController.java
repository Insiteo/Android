package com.insiteo.sampleapp.service;

import android.content.res.Resources;

import com.insiteo.lbs.common.ISError;
import com.insiteo.lbs.location.ISILocationListener;
import com.insiteo.lbs.location.ISLocation;
import com.insiteo.lbs.location.ISLocationProvider;
import com.insiteo.lbs.location.ISLocationRenderer;
import com.insiteo.lbs.map.render.ISIRenderer;
import com.insiteo.sampleapp.MapViewController;

public class LocationController implements ISILocationListener {
	private ISLocationRenderer mLocationRenderer;
	private ISLocation mLastLocation;
	private boolean mCenterOnPosition = true;
	private MapViewController mapViewController;
	private int mLastLocMapID = -1;
	private boolean mRotateMap = false;

	public LocationController(MapViewController mapViewController) {
		this.mapViewController = mapViewController;
	}

	public void init(Resources renderer) {
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
		mLocationRenderer = (ISLocationRenderer) ISLocationProvider.getInstance().getRenderer(renderer);
		mLocationRenderer.setPriority(11);
	}

	public void startLocation() {
		/**
		 * Starts the location computing process using the flags that were defined in the LauncherActivity
		 */
		ISLocationProvider.getInstance().start(this);
	}

	/**
	 * Stops the location computing process.
	 */
	public void stopLocation() {
		ISLocationProvider.getInstance().stop();
		mapViewController.stopLocation();
		mLastLocMapID = -1;
		mLastLocation = null;
	}

	/**
	 * Called when the location computing process has been started.
	 *
	 * @param aError the error returned in case of failure
	 */
	@Override
	public void onLocationInitDone(final ISError aError) {
	}

	/**
	 * This listener gets triggered each time the location process has been able to compute a new position
	 *
	 * @param aLocation the last calculated position
	 */
	@Override
	public void onLocationReceived(final ISLocation aLocation) {
		mapViewController.showLocationButtonResourceOn();
		mLastLocation = aLocation;
		/**
		 * this method animate the map to be centered on the given position, here the user's position
		 */
		if (mCenterOnPosition) {
			mapViewController.centerMap(aLocation.getPosition());
		}
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
			mapViewController.rotateMap(mLastLocMapID, aAzimuth);
		}
	}

	/**
	 * This gets called when the LocationProvider has lost your position.
	 *
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
		mapViewController.showWifiActivitionRequiredDialog();
	}

	/**
	 * This callback gets triggered when the LocationService requires the BLE to be switch on. It enable to ask the user for this permission.
	 */
	@Override
	public void onBleActivationRequired() {
		mapViewController.showBLEActivitionRequiredDialog();
	}

	public ISIRenderer getLocationRenderer() {
		return mLocationRenderer;
	}

	public void disableCenterOnPosition() {
		mCenterOnPosition = false;
	}

	public void locationHandler() {
		if (!ISLocationProvider.getInstance().isStarted()) {
			startLocation();
		} else if (ISLocationProvider.getInstance().isStarted() && !mRotateMap) {
			mRotateMap = true;
		} else {
			stopLocation();
			mRotateMap = false;
		}
	}
}
