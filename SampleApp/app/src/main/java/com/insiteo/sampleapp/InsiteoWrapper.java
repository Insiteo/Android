package com.insiteo.sampleapp;

import android.content.Context;

import com.insiteo.lbs.Insiteo;
import com.insiteo.lbs.common.auth.entities.ISSite;
import com.insiteo.lbs.common.init.ISEPackageType;
import com.insiteo.lbs.common.init.listener.ISIInitListener;
import com.insiteo.lbs.map.render.ISERenderMode;

public class InsiteoWrapper {

	public InsiteoWrapper() {
		Insiteo.setDebug(InsiteoConf.LOG_ENABLED);
	}

	public void launch(Context context, ISIInitListener mInitListener) {
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
}
