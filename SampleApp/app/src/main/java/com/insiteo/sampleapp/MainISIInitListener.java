package com.insiteo.sampleapp;

import android.location.Location;
import android.support.annotation.NonNull;

import com.insiteo.lbs.common.ISError;
import com.insiteo.lbs.common.auth.entities.ISSite;
import com.insiteo.lbs.common.auth.entities.ISUserSite;
import com.insiteo.lbs.common.init.ISEPackageType;
import com.insiteo.lbs.common.init.ISPackage;
import com.insiteo.lbs.common.init.listener.ISIInitListener;

import java.util.Stack;

public class MainISIInitListener implements ISIInitListener {

	private MainViewController mainViewController;

	public MainISIInitListener(MainViewController mainViewController) {
		this.mainViewController = mainViewController;
	}

	@Override
	public void onInitDone(ISError error, ISUserSite suggestedSite, boolean fromLocalCache) {
	}

	@Override
	public void onStartDone(ISError isInsiteoError, Stack<ISPackage> stack) {
		mainViewController.hideInitStatusView();
	}

	@Override
	public void onPackageUpdateProgress(ISEPackageType isePackageType, boolean download, long progress, long total) {
		mainViewController.showProgressBarIfHidden();
		mainViewController.showUpdateStepViewIfHidden();
		mainViewController.showUpdateValueViewIfHidden();
		String value = calculateProgress(progress, total);
		if (download) {
			/** For the download process the progress is given in bytes */
			mainViewController.updateDownloadStatus(value, progress, total);
		} else {
			/** For the install process the progress is given in number of files */
			mainViewController.updateInstallStatus(value, progress, total);
		}
	}

	@NonNull
	private String calculateProgress(long progress, long total) {
		int percent = (int) (progress * 100 / total);
		return String.valueOf(percent) + "%";
	}

	@Override
	public void onDataUpdateDone(ISError error) {
		mainViewController.hidePackageStatusView();
		mainViewController.startDashboard();
	}

	/**
	 * This callback will be used in order to select the most suitable {@link ISSite} that will be returned in
	 * by {@link #onInitDone(ISError, ISUserSite, boolean)}. Most of the time this should be used to return the user's location. If no {@link Location}
	 * (ie <code>null</code>) is returned  then the suggested {@link ISSite} will simply be the first one returned by the server.
	 *
	 * @return the {@link Location} to find the most suitable {@link ISSite} or <code>null</code>
	 */
	@Override
	public Location selectClosestToLocation() {
		return null;
	}
}
