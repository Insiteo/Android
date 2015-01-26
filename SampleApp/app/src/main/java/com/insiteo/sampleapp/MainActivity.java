package com.insiteo.sampleapp;

import java.util.Stack;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.insiteo.lbs.common.CommonConstants;
import com.insiteo.lbs.common.InsiteoError;
import com.insiteo.lbs.common.init.EPackageType;
import com.insiteo.lbs.common.init.IInitListener;
import com.insiteo.lbs.common.init.InitProvider;
import com.insiteo.lbs.common.init.Package;
import com.insiteo.lbs.location.LocationConstants;
import com.insiteo.lbs.map.MapConstants;
import com.insiteo.lbs.map.render.ERenderMode;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class MainActivity extends Activity implements IInitListener {

	public final static String TAG = "SampleApp";

	private final static boolean AUTOMATIC_DOWNLOAD = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		CommonConstants.DEBUG = InsiteoConf.LOG_ENABLED;
		LocationConstants.DEBUG_MODE = InsiteoConf.EMBEDDED_LOG_ENABLED;


        MapConstants.USE_ZONE_3D_OPTIMIZATION = false;


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

		// Init UI components
		mInitStatusView = findViewById(R.id.init_status);
		mPackageStatusView = findViewById(R.id.package_status);
		mUpdateProgressBar = (ProgressBar) findViewById(R.id.update_progress);
		mUpdateActionBtn = (Button) findViewById(R.id.download_packages_btn);
		mSkipActionBtn = (Button) findViewById(R.id.skip_btn);
		mUpdateStepView = (TextView) findViewById(R.id.update_step);
		mUpdateValueView = (TextView) findViewById(R.id.update_value);
		mUpdateTitleView = (TextView) findViewById(R.id.update_title);

		final InitProvider initProvider = InitProvider.getInstance(); 

		// Make sure to provide your API key before initializing our API 
		initProvider.setAPIKey(InsiteoConf.API_KEY);

		// Start Insiteo API initialization (asynchronous), and get the running initialization task
		initProvider.start(this, InsiteoConf.SERVER, InsiteoConf.SITE_ID, InsiteoConf.LANG, this, InsiteoConf.RENDER_MODE);

		mInitStatusView.setVisibility(View.VISIBLE);

	}

	/**
	 * Callback received on API server initialization
	 */
	@Override
	public Stack<Package> onInitDone(final InsiteoError aError, final Stack<Package> packageToUpdate) {
		
		/* Hide the Loader. NB this methods is not called on the UI Thread ! */
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				mInitStatusView.setVisibility(View.GONE);
			}
		});

		/**
		 * If the error is different from <code>null</code> then it means that the initialization has failed. Most likely this comes from 
		 * a wrong API key or no network access. A failure in the API initialization does not mean that our services are unavailable. As we work with data package 
		 * this only mean that you might not be using the last version of the server data. Nevertheless if you have old data you can still take advantage of our services
		 * (only some services such as Meetme won't be available as they request server interaction).
		 * <b>Caution:</b> Be sure to check that you have the required package data before using our services.
		 */
		if(aError != null) {
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					String msgFail = getString(R.string.launcher_api_init_fail) + " " + aError.getMessage();
					Crouton.makeText(MainActivity.this, msgFail, Style.ALERT).show();
					startDashboard();
				}
			});
			return null;
		} 
		
		/**
		 * The error is <code>null</code> and the stack of packages to update is empty, this means that your application is already using the most up to date set of data.
		 * You can use all the services normally.
		 */
		else if(packageToUpdate.isEmpty()) {			
			startDashboard();
			return null;
		} 
		/**
		 * Initialization succeeded and new data are available. If the application is launched for the first time the data will be required for the application to work 
		 * (for example to download the map tiles, the location data and so on) so the download process must be triggered. Otherwise those data are optional and can still 
		 * work with old values. If you want to automatically start the download process you can return a stack of package to download (it is up to you to filter the list at 
		 * your convenience), otherwise you can trigger the download later, after a specific user interaction for example.
		 */	
		else {
			if(AUTOMATIC_DOWNLOAD) {		
				displayUpdateView();
				return packageToUpdate;
			} else {
				proposePackageUpdate();
				return null;
			}
		}
	}



	//******************************************************************************************************************
	// DATA DOWNLOAD
	// *****************************************************************************************************************

	/**
	 * Displays a window to propose data package update
	 */
	private void proposePackageUpdate() {

		mPackageStatusView.setVisibility(View.VISIBLE);

		mUpdateActionBtn.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				updatePackages();
			}
		});

		mSkipActionBtn.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				startDashboard();
			}
		});
	}

	/**
	 * Triggers the download of the new data that are available. This process is done asynchronously so that the UI can't still handle user interaction.
	 * If the task is cancelled the download will be interrupted.
	 */
	private void updatePackages() {

		displayUpdateView();

		InitProvider.getInstance().update(MainActivity.this, false);
	}
	
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

	@Override
	public void onPackageUpdateProgress(EPackageType packageType, boolean download, long progress, long total) {
		
		
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


	/**
	 * Callback received when the entire process of data update is done
	 */
	@Override
	public void onDataUpdateDone(final boolean aSuccess, final InsiteoError aError) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {

				mPackageStatusView.setVisibility(View.GONE);

				startDashboard();
			}
		});		
	}

	/**
	 * Launches the {@link MapFragment}
	 */
	private void startDashboard() {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				/** In our case we consider that the application can only work if it has at least the required package for map and the location. 
				 * Here we are only checking for the Map2D packages, for the 3D check <code>MAP3DPACKAGE</code> instead of <code>TILES</code>*/
				
				if(InitProvider.getInstance().getRenderMode() == ERenderMode.RENDER_MODE_3D) {
					if (InitProvider.getInstance().hasPackage(EPackageType.MAPDATA)
							&& InitProvider.getInstance().hasPackage(EPackageType.MAP3DPACKAGE)
							&& InitProvider.getInstance().hasPackage(EPackageType.LOCATION)) {

						getFragmentManager()
						.beginTransaction()
						.replace(R.id.container,
								new MapFragment()).commit();
					} else {
						Crouton.makeText(MainActivity.this, R.string.error_missing_required_package, Style.ALERT).show();
					}
				} else {
					if (InitProvider.getInstance().hasPackage(EPackageType.MAPDATA)
							&& InitProvider.getInstance().hasPackage(EPackageType.TILES)
							&& InitProvider.getInstance().hasPackage(EPackageType.LOCATION)) {

						getFragmentManager()
						.beginTransaction()
						.replace(R.id.container,
								new MapFragment()).commit();
					} else {
						Crouton.makeText(MainActivity.this, R.string.error_missing_required_package, Style.ALERT).show();
					}
				}
				
			}
		});


	}



}
