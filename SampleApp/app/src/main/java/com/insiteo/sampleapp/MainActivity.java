package com.insiteo.sampleapp;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.insiteo.lbs.common.ISInsiteoError;
import com.insiteo.lbs.common.auth.entities.ISUser;
import com.insiteo.lbs.common.auth.entities.ISUserSite;
import com.insiteo.lbs.common.init.ISEPackageType;
import com.insiteo.lbs.common.init.ISPackage;
import com.insiteo.lbs.common.init.Insiteo;
import com.insiteo.lbs.common.init.listener.ISIInitListener;
import com.insiteo.lbs.location.ISLocationConstants;
import com.insiteo.lbs.map.ISMapConstants;
import com.insiteo.lbs.map.render.ISERenderMode;

import java.util.Stack;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class MainActivity extends Activity {

	public final static String TAG = "SampleApp";

	private final static boolean AUTOMATIC_DOWNLOAD = true;

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
		mUpdateActionBtn = (Button) findViewById(R.id.download_packages_btn);
		mSkipActionBtn = (Button) findViewById(R.id.skip_btn);
		mUpdateStepView = (TextView) findViewById(R.id.update_step);
		mUpdateValueView = (TextView) findViewById(R.id.update_value);
		mUpdateTitleView = (TextView) findViewById(R.id.update_title);

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


	
	private void displayUpdateView() {
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				mPackageStatusView.setVisibility(View.VISIBLE);
				
				mUpdateTitleView.setVisibility(View.GONE);
				
				mUpdateActionBtn.setVisibility(View.GONE);
				mSkipActionBtn.setVisibility(View.GONE);

				mUpdateProgressBar.setVisibility(View.VISIBLE);
				mUpdateStepView.setVisibility(View.VISIBLE);
				mUpdateValueView.setVisibility(View.VISIBLE);
			}
		});		
	}



	/**
	 * Launches the {@link MapFragment}
	 */
	private void startDashboard() {
		/** In our case we consider that the application can only work if it has at least the required package for map and the location.
		 * Here we are only checking for the Map2D packages, for the 3D check <code>MAP3DPACKAGE</code> instead of <code>TILES</code>*/

		if (Insiteo.getCurrentUser().getRenderMode() == ISERenderMode.RENDER_MODE_3D) {
			if (Insiteo.getCurrentSite().hasPackage(ISEPackageType.MAPDATA)
					&& Insiteo.getCurrentSite().hasPackage(ISEPackageType.MAP3DPACKAGE)
					&& Insiteo.getCurrentSite().hasPackage(ISEPackageType.LOCATION)) {

				getFragmentManager()
						.beginTransaction()
						.replace(R.id.container,
								new MapFragment()).commit();
			} else {
				Crouton.makeText(MainActivity.this, R.string.error_missing_required_package, Style.ALERT).show();
			}
		} else {
			if (Insiteo.getCurrentSite().hasPackage(ISEPackageType.MAPDATA)
					&& Insiteo.getCurrentSite().hasPackage(ISEPackageType.TILES)
					&& Insiteo.getCurrentSite().hasPackage(ISEPackageType.LOCATION)) {

				getFragmentManager()
						.beginTransaction()
						.replace(R.id.container,
								new MapFragment()).commit();
			} else {
				Crouton.makeText(MainActivity.this, R.string.error_missing_required_package, Style.ALERT).show();
			}
		}
	}



	private final ISIInitListener mInitListener = new ISIInitListener() {
		@Override
		public void onInitDone(ISUser isUser, ISInsiteoError isInsiteoError, ISUserSite isUserSite) {
			mInitStatusView.setVisibility(View.GONE);
		}

		@Override
		public void onStartDone(ISInsiteoError isInsiteoError, Stack<ISPackage> stack) {
			displayUpdateView();
		}

		@Override
		public void onPackageUpdateProgress(ISEPackageType isePackageType, boolean download, long progress, long total) {
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
		public void onDataUpdateDone(boolean b, ISInsiteoError isInsiteoError) {
			mPackageStatusView.setVisibility(View.GONE);
			startDashboard();
		}
	};





}
