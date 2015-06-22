package com.insiteo.sampleapp;

import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.insiteo.lbs.Insiteo;
import com.insiteo.lbs.common.ISError;
import com.insiteo.lbs.common.auth.entities.ISSite;
import com.insiteo.lbs.common.auth.entities.ISUserSite;
import com.insiteo.lbs.common.init.ISEPackageType;
import com.insiteo.lbs.common.init.ISPackage;
import com.insiteo.lbs.common.init.listener.ISIInitListener;
import com.insiteo.lbs.location.ISLocationConstants;
import com.insiteo.lbs.map.ISMapConstants;
import com.insiteo.lbs.map.render.ISERenderMode;

import java.util.Stack;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class MainActivity extends ActionBarActivity {

	public final static String TAG = "SampleApp";

	private final static boolean AUTOMATIC_DOWNLOAD = true;

    private MapFragment mMapFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Insiteo.setDebug(InsiteoConf.LOG_ENABLED);
		ISLocationConstants.DEBUG_MODE = InsiteoConf.EMBEDDED_LOG_ENABLED;


        ISMapConstants.USE_ZONE_3D_OPTIMIZATION = false;

		// Init UI components
		mInitStatusView = findViewById(R.id.init_status);
		mPackageStatusView = findViewById(R.id.package_status);
		mUpdateProgressBar = (ProgressBar) findViewById(R.id.update_progress);
		//mUpdateActionBtn = (Button) findViewById(R.id.download_packages_btn);
		//mSkipActionBtn = (Button) findViewById(R.id.skip_btn);
		mUpdateStepView = (TextView) findViewById(R.id.update_step);
		mUpdateValueView = (TextView) findViewById(R.id.update_value);
		//mUpdateTitleView = (TextView) findViewById(R.id.update_title);

		initAPI();

	}

	@Override
	protected void onDestroy() {
		Crouton.cancelAllCroutons();
		super.onDestroy();
	}

	//******************************************************************************************************************
	// API INITIALIZATION
	// *****************************************************************************************************************

	// UI Components
	private View mInitStatusView;
	private View mPackageStatusView;

	private ProgressBar mUpdateProgressBar;
	private Button mUpdateActionBtn;
	private Button mSkipActionBtn;
	private TextView mUpdateStepView;
	private TextView mUpdateValueView;
	private TextView mUpdateTitleView;

	/**
	 * This method initializes the InsiteoAPI in order to be able to use all services 
	 */
	private void initAPI() {
		Insiteo.getInstance().launch(this, mInitListener);
		mInitStatusView.setVisibility(View.VISIBLE);
	}



	//******************************************************************************************************************
	// DATA DOWNLOAD
	// *****************************************************************************************************************


	
	/*private void displayUpdateView() {
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				mPackageStatusView.setVisibility(View.VISIBLE);
				
				//mUpdateTitleView.setVisibility(View.GONE);
				
				//mUpdateActionBtn.setVisibility(View.GONE);
				//mSkipActionBtn.setVisibility(View.GONE);

				mUpdateProgressBar.setVisibility(View.VISIBLE);
				mUpdateStepView.setVisibility(View.VISIBLE);
				mUpdateValueView.setVisibility(View.VISIBLE);
			}
		});		
	}*/



	/**
	 * Launches the {@link MapFragment}
	 */
	private void startDashboard() {
		/** In our case we consider that the application can only work if it has at least the required package for map and the location.
		 * Here we are only checking for the Map2D packages, for the 3D check <code>MAP3DPACKAGE</code> instead of <code>TILES</code>*/

		if (Insiteo.getCurrentUser().getRenderMode() == ISERenderMode.MODE_3D) {
			if (Insiteo.getCurrentSite().hasPackage(ISEPackageType.MAPDATA)
					&& Insiteo.getCurrentSite().hasPackage(ISEPackageType.MAP3DPACKAGE)
					&& Insiteo.getCurrentSite().hasPackage(ISEPackageType.LOCATION)) {




                getSupportFragmentManager()
						.beginTransaction()
						.replace(R.id.container, mMapFragment).commit();
			} else {
				Crouton.makeText(MainActivity.this, R.string.error_missing_required_package, Style.ALERT).show();
			}
		} else {
			if (Insiteo.getCurrentSite().hasPackage(ISEPackageType.MAPDATA)
					&& Insiteo.getCurrentSite().hasPackage(ISEPackageType.TILES)
					&& Insiteo.getCurrentSite().hasPackage(ISEPackageType.LOCATION)) {

                mMapFragment = new MapFragment();

				getSupportFragmentManager()
						.beginTransaction()
						.replace(R.id.container,
                                mMapFragment).commit();
			} else {
				Crouton.makeText(MainActivity.this, R.string.error_missing_required_package, Style.ALERT).show();
			}
		}
	}



	private final ISIInitListener mInitListener = new ISIInitListener() {
		@Override
		public void onInitDone(ISError error, ISUserSite suggestedSite, boolean fromLocalCache) {

		}

		@Override
		public void onStartDone(ISError isInsiteoError, Stack<ISPackage> stack) {
			mInitStatusView.setVisibility(View.GONE);
		}

		@Override
		public void onPackageUpdateProgress(ISEPackageType isePackageType, boolean download, long progress, long total) {
            if (mUpdateProgressBar.getVisibility() == View.GONE) mUpdateProgressBar.setVisibility(View.VISIBLE);
            if (mUpdateStepView.getVisibility() == View.GONE) mUpdateStepView.setVisibility(View.VISIBLE);
            if (mUpdateValueView.getVisibility() == View.GONE) mUpdateValueView.setVisibility(View.VISIBLE);

			if(download) {
				/** For the download process the progress is given in bytes */
				int percent = (int) (progress * 100 / total);
				String value = String.valueOf(percent) + "%";

				mUpdateStepView.setText(R.string.launcher_downloading);
				mUpdateValueView.setText(value);

				mUpdateProgressBar.setIndeterminate(false);
				mUpdateProgressBar.setMax((int)(total/1024));
				mUpdateProgressBar.setProgress((int)(progress/1024));
			} else {
				/** For the install process the progress is given in number of files */
				int percent = (int) (progress * 100 / total);
				String value = String.valueOf(percent) + "%";

				mUpdateStepView.setText(R.string.launcher_installing);
				mUpdateValueView.setText(value);

				mUpdateProgressBar.setIndeterminate(false);
				mUpdateProgressBar.setMax((int)(total));
				mUpdateProgressBar.setProgress((int)(progress));
			}
		}

		@Override
		public void onDataUpdateDone(ISError error) {
			mPackageStatusView.setVisibility(View.GONE);
			startDashboard();
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
    };




	public void switchSite() {
		SparseArray<ISUserSite> availablesSites = Insiteo.getCurrentUser().getSites();

		if(availablesSites.size() <= 1) { Crouton.makeText(this, "Only one site available", Style.ALERT).show(); return; }


		for (int i = 0; i < availablesSites.size(); i++) {
			if (availablesSites.valueAt(i).getSiteId() != Insiteo.getCurrentSite().getSiteId()) {
				Crouton.makeText(this, "Switching to site " + availablesSites.valueAt(i).getLabel(), Style.INFO).show();
                getSupportFragmentManager().beginTransaction().remove(mMapFragment).commit();
                mMapFragment = null;


                getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);

                mInitStatusView.setVisibility(View.VISIBLE);

                mUpdateProgressBar.setVisibility(View.GONE);
                mUpdateStepView.setVisibility(View.GONE);
                mUpdateValueView.setVisibility(View.GONE);


                Insiteo.getInstance().startAndUpdate(availablesSites.valueAt(i), mInitListener);

			}
		}
	}
}
