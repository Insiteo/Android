package com.insiteo.sampleapp;

import android.content.Context;
import android.util.SparseArray;

import com.insiteo.lbs.Insiteo;
import com.insiteo.lbs.common.auth.entities.ISSite;
import com.insiteo.lbs.common.auth.entities.ISUser;
import com.insiteo.lbs.common.auth.entities.ISUserSite;
import com.insiteo.lbs.common.init.ISEPackageType;
import com.insiteo.lbs.map.render.ISERenderMode;
import com.insiteo.sampleapp.moduleinjection.GraphProvider;

import javax.inject.Inject;

import dagger.Lazy;

public class InsiteoController {

	@Inject
	Insiteo instance;
	@Inject
	Lazy<ISUser> lazyCurrentUser;
	@Inject
	Lazy<ISSite> lazyCurrentSite;

	public InsiteoController() {
		init();
		GraphProvider.injectApplicationGraph(this);
	}

	protected void init() {
		Insiteo.setDebug(InsiteoConf.LOG_ENABLED);
	}

	public void initAPI(Context context, MainISIInitListener mInitListener) {
		instance.launch(context, mInitListener);
	}

	public boolean isRenderMode3D() {
		return lazyCurrentUser.get().getRenderMode() == ISERenderMode.MODE_3D;
	}

	public boolean has3DPackages() {
		ISSite isSite = lazyCurrentSite.get();
		return isSite.hasPackage(ISEPackageType.MAPDATA)
				&& isSite.hasPackage(ISEPackageType.MAP3DPACKAGE)
				&& isSite.hasPackage(ISEPackageType.LOCATION);
	}

	public boolean has2DPackages() {
		ISSite isSite = lazyCurrentSite.get();
		return isSite.hasPackage(ISEPackageType.MAPDATA)
				&& isSite.hasPackage(ISEPackageType.TILES)
				&& isSite.hasPackage(ISEPackageType.LOCATION);
	}

	public boolean is3DModeReady() {
		return isRenderMode3D() && has3DPackages();
	}

	public SparseArray<ISUserSite> getAvailablesSites() {
		return lazyCurrentUser.get().getSites();
	}

	public boolean isSameSite(ISUserSite userSite) {
		return userSite.getSiteId() == lazyCurrentSite.get().getSiteId();
	}

	public void startAndUpdate(ISUserSite userSite, MainISIInitListener mInitListener) {
		instance.startAndUpdate(userSite, mInitListener);
	}

	public boolean isReady() {
		return is3DModeReady() || has2DPackages();
	}
}
