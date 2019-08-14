package com.insiteo.sampleapp;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.insiteo.lbs.Insiteo;
import com.insiteo.lbs.common.ISError;
import com.insiteo.lbs.common.auth.entities.ISSite;
import com.insiteo.lbs.common.auth.entities.ISUserSite;
import com.insiteo.lbs.common.init.ISEPackageType;
import com.insiteo.lbs.common.init.ISEServerType;
import com.insiteo.lbs.common.init.ISPackage;
import com.insiteo.lbs.common.utils.ISLog;
import com.insiteo.lbs.itinerary.GfxInstruction;
import com.insiteo.lbs.location.ISLocationConstants;
import com.insiteo.lbs.map.ISMapConstants;
import com.insiteo.sampleapp.beacon.BeaconMonitoringFragment;
import com.insiteo.sampleapp.initialization.ISInitializationTaskFragment;
import com.insiteo.sampleapp.settings.SettingsActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import de.keyboardsurfer.android.widget.crouton.Crouton;

public class MainActivity extends AppCompatActivity implements ISInitializationTaskFragment.Callback {

	public final static String TAG = "SampleApp";

    private final static int NO_RESOURCE = -1;

    private ISInitializationTaskFragment mInitializationFragment;

    private Toolbar mToolbar;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;

    private ProgressBar mLoaderView;
    private TextView mUpdateProgress;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        // Set the UI
        setContentView(R.layout.activity_main);
        initToolbar();
        initNavigationDrawer();
        initElements();

        FragmentManager fm = getSupportFragmentManager();
        mInitializationFragment = (ISInitializationTaskFragment) fm.findFragmentByTag(TAG);

        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (mInitializationFragment == null) {
            launch(mInitializationFragment = ISInitializationTaskFragment.newInstance(), NO_RESOURCE);
        }
        boolean permRequired = checkPermissionStatus();



        if(!permRequired)
            init();

        GfxInstruction.SECTION_WIDTH_IN_METERS = 10;
	}

    public void init() {


        Insiteo.setDebug(InsiteoConf.LOG_ENABLED);
        ISLocationConstants.DEBUG_MODE = InsiteoConf.EMBEDDED_LOG_ENABLED;


        ISMapConstants.USE_ZONE_3D_OPTIMIZATION = false;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(mInitializationFragment != null) {

            switch (mInitializationFragment.getCurrentState()){
                case UNKNOWN:
                    /**
                     * Forces the API initialization
                     */
                    if(!Insiteo.getInstance().isAuthenticated()) {
                        mInitializationFragment.initializeAPI();
                        displayLoaderView(true);
                    }
                    break;

                case INITIALIZING:
                    displayLoaderView(true);
                    break;
            }
        }
    }

    @Override
	protected void onDestroy() {
		Crouton.cancelAllCroutons();
		super.onDestroy();
	}

    private void launch(Fragment frag, int subtitleRes) {

        if(subtitleRes != NO_RESOURCE) mToolbar.setSubtitle(subtitleRes);

        FragmentManager fragmentManager = getSupportFragmentManager();

        fragmentManager.beginTransaction()
                .replace(R.id.container, frag)
                .commit();
    }

    private void launchBeacon() {
        launch(BeaconMonitoringFragment.newInstance(), R.string.beacon_monitoring_title);
    }

    private void launchMap() {
        launch(MapFragment.newInstance(), R.string.map_title);
    }

    //**********************************************************************************************
    // 	OptionMenu
    // *********************************************************************************************

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: mDrawerLayout.openDrawer(GravityCompat.START); return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //**********************************************************************************************
    // 	UI Elements
    // *********************************************************************************************

    private void initToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
    }

    private void initNavigationDrawer() {
        NavigationView navView = (NavigationView) findViewById(R.id.navigation);
        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.beacon_monitoring:
                        mDrawerLayout.closeDrawers();
                        BeaconMonitoringFragment frag = BeaconMonitoringFragment.newInstance();
                        launch(frag, R.string.beacon_monitoring_title);
                        return true;
                    case R.id.menu_settings:
                        mDrawerLayout.closeDrawers();
                        startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                        return true;
                }
                return false;
            }
        });

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);


        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                supportInvalidateOptionsMenu();
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                supportInvalidateOptionsMenu();
            }
        };

        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });

        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    private void initElements() {
        mLoaderView = (ProgressBar) findViewById(R.id.initialization_progress);
        mUpdateProgress = (TextView) findViewById(R.id.update_progress);
    }

    private void displayLoaderView(boolean visible) {
        mLoaderView.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    //**********************************************************************************************
    // 	ISInitializationTaskFragment#Callback
    // *********************************************************************************************

    @Override
    public void onInitDone(ISError error, ISUserSite suggestedSite, boolean fromLocalCache) {
        displayLoaderView(false);

        /**
         * The sdk will return an error if no internet access is available but this does not
         * mean that the sdk can not be used
         */
        if (error == null || error.getReason() == ISError.ReasonConnectivity) {
            mInitializationFragment.startSite(suggestedSite);
        } else {
            ISLog.e(TAG, "onInitDone: " + error);
        }
    }

    @Override
    public void onStartDone(ISError error, Stack<ISPackage> packageToUpdate) {
        /**
         * The sdk will return an error if no internet access is available but this does not
         * mean that the sdk can not be used. Indeed if all the package are available locally
         * it should be ok
         */
        if (error == null || error.getReason() == ISError.ReasonConnectivity) {
            if (packageToUpdate == null || packageToUpdate.isEmpty()) {
                launchMap();
            }
        } else {
            ISLog.e(TAG, "onStartDone: " + error);
        }
    }

    @Override
    public void onPackageUpdateProgress(ISEPackageType packageType, boolean download, long progress, long total) {
        ISLog.d(TAG, "onPackageUpdateProgress() called with: " + "packageType = [" + packageType + "], download = [" + download + "], progress = [" + progress + "], total = [" + total + "]");
        StringBuilder sb = new StringBuilder();
        if (download) sb.append("downloading => "); else sb.append("installing => ");

        sb.append('[').append(packageType).append(']').append(' ');
        sb.append(' ').append(progress).append('/').append(total);

        mUpdateProgress.setText(sb.toString());
    }

    @Override
    public void onDataUpdateDone(ISError error) {
        if (error == null) {
            launchMap();
        } else {
            ISLog.e(TAG, "onDataUpdateDone: " + error);
            ISSite site = Insiteo.getCurrentSite();
            if (site != null && site.hasPackage(ISEPackageType.MAPDATA)
                    && site.hasPackage(ISEPackageType.TILES)) {
                launchMap();
            }
        }
    }

    //**********************************************************************************************
    // Android M permissions request
    //**********************************************************************************************

    //**********************************************************************************************
    // Android M permissions request
    //**********************************************************************************************

    private static final int PERMISSION_REQUEST = 0;

    private boolean checkPermissionStatus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            final List<String> permissionsList = new ArrayList<String>();

            StringBuilder message = new StringBuilder();
            message.append("The application requires the following permissions: \n ");

            if (!isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)) {
                permissionsList.add(Manifest.permission.ACCESS_FINE_LOCATION);
                message.append("\n - ACCESS_COARSE_LOCATION in order to scan BLE and WIFI signals\n");

            }

            if (!isPermissionGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                permissionsList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                message.append("\n - WRITE_EXTERNAL_STORAGE in order to write temporary files on the SD card \n");

            }

            if (!isPermissionGranted(Manifest.permission.READ_PHONE_STATE)) {
                permissionsList.add(Manifest.permission.READ_PHONE_STATE);
                message.append("\n - READ_PHONE_STATE to pause scans on phone calls \n");

            }

            if (!permissionsList.isEmpty()) {
                displayPermissionsAlert("Permission required", message.toString(),
                        permissionsList.toArray(new String[permissionsList.size()]));
                return true;
            }
        }
        return false;
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void displayPermissionsAlert(String title, String message, final String[] permissions) {
        TextView tv = new TextView(this);
        tv.setText(message);
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setView(tv);
        builder.setPositiveButton(android.R.string.ok, null);
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface dialog) {
                requestPermissions(permissions, PERMISSION_REQUEST);
            }
        });
        builder.show();
    }



    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST: {

                for (int i = 0; i < permissions.length; i++) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        init();
                        Log.d(TAG, permissions[i] + " granted");
                    } else {
                        Toast.makeText(MainActivity.this, "All permissions are required", Toast.LENGTH_SHORT).show();
                        Log.w(TAG, permissions[i] + " refused");
                        finish();
                    }
                }

                return;
            }
        }
    }

    private boolean isPermissionGranted(String permission) {
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                permission);
        return permissionCheck == PackageManager.PERMISSION_GRANTED;
    }
}
