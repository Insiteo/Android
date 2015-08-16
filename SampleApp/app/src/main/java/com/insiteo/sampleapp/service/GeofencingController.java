package com.insiteo.sampleapp.service;

import android.util.Log;

import com.insiteo.lbs.geofence.ISGeofenceArea;
import com.insiteo.lbs.geofence.ISGeofenceProvider;
import com.insiteo.lbs.geofence.ISIGeofenceListener;
import com.insiteo.lbs.location.ISELocationModule;
import com.insiteo.lbs.location.ISLocationProvider;
import com.insiteo.sampleapp.MapViewController;

import java.util.List;

public class GeofencingController implements ISIGeofenceListener {
	private MapViewController mapViewController;

	public GeofencingController(MapViewController mapViewController) {
		this.mapViewController = mapViewController;
	}

	public void init() {
		ISGeofenceProvider mGeofenceProvider = (ISGeofenceProvider) ISLocationProvider.getInstance().getModule(ISELocationModule.GEOFENCING);
		mGeofenceProvider.setListener(this);
	}

	/**
	 * Called when geofencing module has new data available.
	 *
	 * @param aEnteredZones list of zones that location has just entered
	 * @param aStayedZones  list of zones where location has stayed for a certain amount of time
	 * @param aLeftZones    list of zones that location has just left
	 */
	@Override
	public void onGeofenceUpdate(List<ISGeofenceArea> aEnteredZones, List<ISGeofenceArea> aStayedZones, List<ISGeofenceArea> aLeftZones) {
		Log.d("Geofencing", "onGeofenceUpdate " + aEnteredZones.size() + " " + aStayedZones.size() + " " + aLeftZones.size());

		for (ISGeofenceArea z : aEnteredZones) {
			final String extra1 = z.getExtra1();
			if (extra1 != null) {
				mapViewController.showGeofenceToast(extra1);
			}
		}
	}

	/**
	 * Called when geofencing data is reset. This happens when no location was received for a long duration, thus zone detection is not valid anymore.
	 */
	@Override
	public void onGeofenceDataCleared() {

	}
}
