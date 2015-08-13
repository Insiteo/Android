package com.insiteo.sampleapp;

import android.graphics.drawable.AnimationDrawable;

import com.insiteo.lbs.common.ISError;
import com.insiteo.lbs.common.utils.geometry.ISPosition;

public class MapViewController {
	private MapFragment mapFragment;

	public MapViewController(MapFragment mapFragment) {
		this.mapFragment = mapFragment;
	}

	public void showGeofenceToast(String extra1) {
		mapFragment.showGeofenceToast(extra1);
	}

	public void stopLocation() {
		mapFragment.stopLocation();
	}

	public void showLocationButtonResourceOn() {
		mapFragment.showLocationButtonResourceOn();
	}

	public void centerMap(ISPosition position) {
		mapFragment.centerMap(position);
	}

	public void rotateMap(int mLastLocMapID, float aAzimuth) {
		mapFragment.rotateMap(mLastLocMapID, aAzimuth);
	}

	public void showWifiActivitionRequiredDialog() {
		mapFragment.showWifiActivitionRequiredDialog();
	}

	public void showBLEActivitionRequiredDialog() {
		mapFragment.showBLEActivitionRequiredDialog();
	}

	public void showItinaryAlert(ISError error) {
		mapFragment.showItinaryAlert(error);
	}

	public void showLocationButtonResourceLocalizationWithAnimation() {
		mapFragment.showLocationButtonResourceLocalizationWithAnimation();
	}
}
