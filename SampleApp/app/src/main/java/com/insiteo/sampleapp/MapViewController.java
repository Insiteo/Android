package com.insiteo.sampleapp;

public class MapViewController {
	private MapFragment mapFragment;

	public MapViewController(MapFragment mapFragment) {
		this.mapFragment = mapFragment;
	}

	public void showGeofenceToast(String extra1) {
		mapFragment.showGeofenceToast(extra1);
	}
}
