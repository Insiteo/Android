package com.insiteo.sampleapp;

import android.content.Context;
import android.util.SparseArray;

import com.insiteo.lbs.Insiteo;
import com.insiteo.lbs.common.auth.entities.ISSite;
import com.insiteo.lbs.common.auth.entities.ISUserSite;
import com.insiteo.lbs.common.init.ISEPackageType;
import com.insiteo.lbs.map.render.ISERenderMode;

public class InsiteoController {


	public InsiteoController() {
		Insiteo.setDebug(InsiteoConf.LOG_ENABLED);
	}

	public void initAPI(Context context, MainISIInitListener mInitListener) {
		Insiteo.getInstance().launch(context, mInitListener);
	}

	public boolean isRenderMode3D() {
		return Insiteo.getCurrentUser().getRenderMode() == ISERenderMode.MODE_3D;
	}

	public boolean has3DPackages() {
		ISSite currentSite = Insiteo.getCurrentSite();
		return currentSite.hasPackage(ISEPackageType.MAPDATA)
				&& currentSite.hasPackage(ISEPackageType.MAP3DPACKAGE)
				&& currentSite.hasPackage(ISEPackageType.LOCATION);
	}

	public boolean has2DPackages() {
		return Insiteo.getCurrentSite().hasPackage(ISEPackageType.MAPDATA)
				&& Insiteo.getCurrentSite().hasPackage(ISEPackageType.TILES)
				&& Insiteo.getCurrentSite().hasPackage(ISEPackageType.LOCATION);
	}

	public boolean is3DModeReady() {
		return isRenderMode3D() && has3DPackages();
	}

	public SparseArray<ISUserSite> getAvailablesSites() {
		return Insiteo.getCurrentUser().getSites();
	}

	public boolean isSameSite(ISUserSite userSite) {
		return userSite.getSiteId() == Insiteo.getCurrentSite().getSiteId();
	}

	public void startAndUpdate(ISUserSite userSite, MainISIInitListener mInitListener) {
		Insiteo.getInstance().startAndUpdate(userSite, mInitListener);
	}

	public boolean isReady() {
		return is3DModeReady() || has2DPackages();
	}
}
