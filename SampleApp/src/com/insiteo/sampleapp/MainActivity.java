package com.insiteo.sampleapp;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.insiteo.common.InsiteoError;
import com.insiteo.common.utils.threading.ICancelable;
import com.insiteo.init.EInitResult;
import com.insiteo.init.EPackageType;
import com.insiteo.init.IInitListener;
import com.insiteo.init.InitProvider;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class MainActivity extends Activity implements IInitListener {
	
	public final static String TAG = "SampleApp";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
				
		initAPI();
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
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

	private ICancelable mInitTask = null;

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

		final InitProvider initProvider = InitProvider.getInstance(); 

		// In version >= 3.x of Insiteo API you will need to provide your API key BEFORE calling the startAPI.  
		initProvider.setAPIKey(InsiteoConf.API_KEY);

		// Start Insiteo API initialization (asynchronous), and get the running initialization task
		mInitTask = initProvider.startAPI(MainActivity.this, MainActivity.this, InsiteoConf.SERVER_URL, InsiteoConf.SITE_ID, 
				InsiteoConf.VERSION, InsiteoConf.LANG, InsiteoConf.SERVER, InsiteoConf.ANALYTICS_ENABLED);

		mInitStatusView.setVisibility(View.VISIBLE);


	}


	/**
	 * Callback received on API server initialization
	 */
	@Override
	public void onInitDone(final EInitResult aInitRes, final InsiteoError aError) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {

				mInitStatusView.setVisibility(View.GONE);


				switch (aInitRes) {

				case FAIL:
					/**
					 * Initialization failed. Some services such as Meetme won't be available as their request server interaction. Other service will work as expected under condition that appropriate packages habe already been downloaded
					 */
					//init failed : display an error message, then launch next activity
					String msgFail = getString(R.string.launcher_api_init_fail) + InitProvider.getInstance().getError().getMessage();
					Crouton.makeText(MainActivity.this, msgFail, Style.ALERT).show();
					startDashboard();
					break;

				case SUCCESS:
					/**
					 * Initialization succeeded and no new data are required to be download. The Application can use all the LBS services normally.
					 */
					startDashboard();
					break;

				case SUCCESS_WITH_NEW_DATA:
					/**
					 * Initialization succeeded and new data are available. If the application is launched for the first time the data will be required for the application to work (for example to download the map tiles) 
					 * so the download process must be triggered. Otherwise those data are optional.
					 */					
					proposePackageUpdate();
					break;					
				}
			}
		});
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
		
		mUpdateActionBtn.setVisibility(View.GONE);
		mSkipActionBtn.setVisibility(View.GONE);
		
		mUpdateProgressBar.setVisibility(View.VISIBLE);
		mUpdateStepView.setVisibility(View.VISIBLE);
		mUpdateValueView.setVisibility(View.VISIBLE);

		mInitTask = InitProvider.getInstance().updatePackages(MainActivity.this, MainActivity.this, false);
	}

	/**
	 * Callback received when the download of new data is processing. Those data are user to update the UI.
	 */
	@Override
	public void onDownloadProgress(long aDownloadedBytes, long aTotalBytes) {
		int percent = (int) (aDownloadedBytes * 100 / aTotalBytes);
		String value = String.valueOf(percent) + "%";
		
		mUpdateStepView.setText(R.string.launcher_downloading);
		mUpdateValueView.setText(value);
		
		mUpdateProgressBar.setIndeterminate(false);
		mUpdateProgressBar.setMax((int)(aTotalBytes/1024));
		mUpdateProgressBar.setProgress((int)(aDownloadedBytes/1024));
	}

	/**
	 * Callback received when a new package is starting being downloaded
	 */
	@Override
	public void onPackageDowloadBegin(EPackageType aPkgType) {
	}

	/**
	 * Callback received when the installation of new data is processing. Those data are user to update the UI.
	 */
	@Override
	public void onInstallProgress(long aCurrentFile, long aTotalFiles) {
		int percent = (int) (aCurrentFile * 100 / aTotalFiles);
		String value = String.valueOf(percent) + "%";

		mUpdateStepView.setText(R.string.launcher_installing);
		mUpdateValueView.setText(value);

		mUpdateProgressBar.setIndeterminate(false);
		mUpdateProgressBar.setMax((int)(aTotalFiles));
		mUpdateProgressBar.setProgress((int)(aCurrentFile));
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
	 * Launches the Dashboard activity
	 */
	private void startDashboard() {
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
