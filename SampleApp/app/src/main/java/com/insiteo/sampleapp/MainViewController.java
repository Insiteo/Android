package com.insiteo.sampleapp;

public class MainViewController {
	private final InsiteoController insiteoController;
	private MainActivity mainActivity;

	public MainViewController(MainActivity mainActivity, InsiteoController insiteoController) {
		this.mainActivity = mainActivity;
		this.insiteoController = insiteoController;
	}

	public void hideInitStatusView() {
		mainActivity.hideInitStatusView();
	}

	public void showProgressBarIfHidden() {
		mainActivity.showProgressBarIfHidden();
	}

	public void showUpdateStepViewIfHidden() {
		mainActivity.showUpdateStepViewIfHidden();
	}

	public void showUpdateValueViewIfHidden() {
		mainActivity.showUpdateValueViewIfHidden();
	}

	public void updateDownloadStatus(String value, long progress, long total) {
		mainActivity.updateDownloadStatus(value, progress, total);
	}

	public void updateInstallStatus(String value, long progress, long total) {
		mainActivity.updateInstallStatus(value, progress, total);
	}

	public void hidePackageStatusView() {
		mainActivity.hidePackageStatusView();
	}

	public void startDashboard() {
		if (insiteoController.isReady()) {
			mainActivity.showMapFragment();
		} else {
			mainActivity.showAlert(R.string.error_missing_required_package);
		}
	}
}
