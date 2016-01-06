package com.insiteo.sampleapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.insiteo.lbs.Insiteo;
import com.insiteo.lbs.common.ISError;
import com.insiteo.lbs.common.auth.entities.ISUserSite;
import com.insiteo.lbs.common.init.ISEPackageType;
import com.insiteo.lbs.common.init.ISPackage;
import com.insiteo.lbs.common.utils.ISLog;
import com.insiteo.lbs.location.ISLocationConstants;
import com.insiteo.lbs.map.ISMapConstants;
import com.insiteo.sampleapp.beacon.BeaconMonitoringFragment;
import com.insiteo.sampleapp.initialization.ISInitializationTaskFragment;
import com.insiteo.sampleapp.settings.SettingsActivity;

import java.util.Stack;

import de.keyboardsurfer.android.widget.crouton.Crouton;

public class MainActivity extends ActionBarActivity implements ISInitializationTaskFragment.Callback {

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

        if (error == null) {
            mInitializationFragment.startSite(suggestedSite);
        } else {
            ISLog.e(TAG, "onInitDone: " + error);
        }
    }

    @Override
    public void onStartDone(ISError error, Stack<ISPackage> packageToUpdate) {
        if (error == null) {
            if (packageToUpdate.isEmpty()) {
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
        }
    }
}
