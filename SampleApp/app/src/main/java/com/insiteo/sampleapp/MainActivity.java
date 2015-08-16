package com.insiteo.sampleapp;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseArray;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.insiteo.lbs.common.auth.entities.ISUserSite;
import com.insiteo.lbs.map.ISMapConstants;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class MainActivity extends AppCompatActivity {

	private MapFragment mMapFragment;

	private View mInitStatusView;
	private View mPackageStatusView;

	private ProgressBar mUpdateProgressBar;
	private TextView mUpdateStepView;
	private TextView mUpdateValueView;
	private InsiteoController insiteoController;
	private MainISIInitListener mInitListener;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		insiteoController = new InsiteoController();
		MainViewController mainViewController = new MainViewController(this, insiteoController);
		mInitListener = new MainISIInitListener(mainViewController);
		ISMapConstants.USE_ZONE_3D_OPTIMIZATION = false;
		mInitStatusView = findViewById(R.id.init_status);
		mPackageStatusView = findViewById(R.id.package_status);
		mUpdateProgressBar = (ProgressBar) findViewById(R.id.update_progress);
		mUpdateStepView = (TextView) findViewById(R.id.update_step);
		mUpdateValueView = (TextView) findViewById(R.id.update_value);
		mInitStatusView.setVisibility(View.VISIBLE);

		insiteoController.initAPI(this, mInitListener);
	}

	@Override
	protected void onDestroy() {
		Crouton.cancelAllCroutons();
		super.onDestroy();
	}

	public void showAlert(int messageRes) {
		Crouton.makeText(this, messageRes, Style.ALERT).show();
	}

	public void showMapFragment() {
		mMapFragment = new MapFragment();
		getSupportFragmentManager()
				.beginTransaction()
				.replace(R.id.container, mMapFragment)
				.commit();
	}

	public void switchSite() {
		SparseArray<ISUserSite> availablesSites = insiteoController.getAvailablesSites();
		if (availablesSites.size() <= 1) {
			showAlert(R.string.one_site_available);
			return;
		}

		for (int i = 0; i < availablesSites.size(); i++) {
			ISUserSite userSite = availablesSites.valueAt(i);
			if (!insiteoController.isSameSite(userSite)) {
				Crouton.makeText(this, "Switching to site " + userSite.getLabel(), Style.INFO).show();
				getSupportFragmentManager().beginTransaction().remove(mMapFragment).commit();
				mMapFragment = null;

				getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);

				mInitStatusView.setVisibility(View.VISIBLE);

				mUpdateProgressBar.setVisibility(View.GONE);
				mUpdateStepView.setVisibility(View.GONE);
				mUpdateValueView.setVisibility(View.GONE);

				insiteoController.startAndUpdate(userSite, mInitListener);

			}
		}
	}

	public void hideInitStatusView() {
		mInitStatusView.setVisibility(View.GONE);
	}

	public void showProgressBarIfHidden() {
		showViewIfHidden(mUpdateProgressBar);
	}

	public void showUpdateStepViewIfHidden() {
		showViewIfHidden(mUpdateStepView);
	}

	public void showUpdateValueViewIfHidden() {
		showViewIfHidden(mUpdateValueView);
	}

	private void showViewIfHidden(View view) {
		if (view.getVisibility() == View.GONE)
			view.setVisibility(View.VISIBLE);
	}

	public void updateDownloadStatus(String value, long progress, long total) {
		mUpdateStepView.setText(R.string.launcher_downloading);
		mUpdateValueView.setText(value);
		mUpdateProgressBar.setIndeterminate(false);
		mUpdateProgressBar.setMax((int) (total / 1024));
		mUpdateProgressBar.setProgress((int) (progress / 1024));
	}

	public void updateInstallStatus(String value, long progress, long total) {
		mUpdateStepView.setText(R.string.launcher_installing);
		mUpdateValueView.setText(value);
		mUpdateProgressBar.setIndeterminate(false);
		mUpdateProgressBar.setMax((int) (total));
		mUpdateProgressBar.setProgress((int) (progress));
	}

	public void hidePackageStatusView() {
		mPackageStatusView.setVisibility(View.GONE);
	}
}
