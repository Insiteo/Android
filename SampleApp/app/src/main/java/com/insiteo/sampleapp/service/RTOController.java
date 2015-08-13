package com.insiteo.sampleapp.service;

import com.insiteo.lbs.map.entities.ISZone;
import com.insiteo.lbs.map.render.ISGenericRTO;
import com.insiteo.lbs.map.render.ISIRTO;
import com.insiteo.lbs.map.render.ISIRTOListener;

public class RTOController implements ISIRTOListener{

	/**
	 * Called when a RTO is clicked
	 *
	 * @param rto  the RTO that was clicked
	 * @param zone the zone containing this RTO (if RTO was put in a zone), or null if RTO was not set in a zone
	 */
	@Override
	public void onRTOClicked(ISIRTO rto, ISZone zone) {
		if (rto instanceof ISGenericRTO) {
			ISGenericRTO genericRto = (ISGenericRTO) rto;

			if (genericRto.isAnnotationClicked()) {
				genericRto.setActionDisplayed(!genericRto.isActionDisplayed());
			}

			if (genericRto.isActionClicked()) {
				genericRto.setIndicatorDisplayed(!genericRto.isIndicatorDisplayed());
			}
		}
	}

	/**
	 * Called when a RTO is moved
	 *
	 * @param rto  the RTO that was moved
	 * @param zone the zone containing this RTO (if RTO was put in a zone), or null if RTO was not set in a zone
	 */
	@Override
	public void onRTOMoved(ISIRTO rto, ISZone zone) {
	}

	/**
	 * Called when a RTO is released
	 *
	 * @param rto  the RTO that was released
	 * @param zone the zone containing this RTO (if RTO was put in a zone), or null if RTO was not set in a zone
	 */
	@Override
	public void onRTOReleased(ISIRTO rto, ISZone zone) {
	}

	/**
	 * Called when a RTO is selected
	 *
	 * @param rto  the RTO that was selected
	 * @param zone the zone containing this RTO (if RTO was put in a zone), or null if RTO was not set in a zone
	 */
	@Override
	public void onRTOSelected(ISIRTO rto, ISZone zone) {
	}
}
