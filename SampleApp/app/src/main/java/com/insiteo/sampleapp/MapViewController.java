package com.insiteo.sampleapp;

import com.insiteo.lbs.common.ISError;
import com.insiteo.lbs.common.utils.geometry.ISPosition;
import com.insiteo.sampleapp.render.GfxRto;
import com.insiteo.sampleapp.service.RTOController;

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

	public void updateActionBar(String[] mapNames) {
		mapFragment.updateActionBar(mapNames);
	}

	public void selectActionBarItem(int i) {
		mapFragment.selectActionBarItem(i);
	}

	public void initRTO(RTOController rtoController) {
		mapFragment.initRTO(rtoController);
	}

	public void addRenderers() {
		mapFragment.addRenderers();
	}

	public void clearRenderers() {
		mapFragment.clearRenderers();
	}

	public void centerMap(int zoneId) {
		mapFragment.centerMap(zoneId);
	}

	public void addRTOInZone(int id, GfxRto rto) {
		mapFragment.addRTOInZone(id, rto);
	}

	public void rotate(float angle) {
		mapFragment.rotate(angle);
	}
}
