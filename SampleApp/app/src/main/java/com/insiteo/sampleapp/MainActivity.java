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
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.insiteo.lbs.Insiteo;
import com.insiteo.lbs.location.ISLocationConstants;
import com.insiteo.sampleapp.beacon.BeaconMonitoringFragment;
import com.insiteo.sampleapp.settings.SettingsActivity;

import de.keyboardsurfer.android.widget.crouton.Crouton;

public class MainActivity extends ActionBarActivity {

	public final static String TAG = "SampleApp";

    private final static int NO_RESOURCE = -1;



    private Toolbar mToolbar;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;

    private ProgressBar mLoaderView;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        // Set the UI
        setContentView(R.layout.activity_main);
        initToolbar();
        initNavigationDrawer();
        initElements();

		Insiteo.setDebug(InsiteoConf.LOG_ENABLED);
		ISLocationConstants.DEBUG_MODE = InsiteoConf.EMBEDDED_LOG_ENABLED;

	}

    @Override
	protected void onDestroy() {
		Crouton.cancelAllCroutons();
		super.onDestroy();
	}

    private void launch(Fragment frag, int subtitleRes) {
        Log.e(TAG, "launch: " + frag);
        if(subtitleRes != NO_RESOURCE) mToolbar.setSubtitle(subtitleRes);

        FragmentManager fragmentManager = getSupportFragmentManager();

        fragmentManager.beginTransaction()
                .replace(R.id.container, frag)
                .commit();
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
                    case R.id.init_all_in_one:
                        mDrawerLayout.closeDrawers();
                        MapFragment map_frag = MapFragment.newInstance();
                        launch(map_frag, R.string.beacon_monitoring_title);
                        return true;
                    case R.id.map_rto:
                        mDrawerLayout.closeDrawers();
                        MapRTOFragment map_rto_frag = MapRTOFragment.newInstance();
                        launch(map_rto_frag, R.string.map_rto_title);
                        return true;
                    case R.id.map_location:
                        mDrawerLayout.closeDrawers();
                        MapLocationFragment map_loc_frag = MapLocationFragment.newInstance();
                        launch(map_loc_frag, R.string.map_location_title);
                        return true;
                    case R.id.map_location_geofencing:
                        mDrawerLayout.closeDrawers();
                        MapLocationGeofencingFragment map_geofencing_frag = MapLocationGeofencingFragment.newInstance();
                        launch(map_geofencing_frag, R.string.map_location_geofencing_title);
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
    }
}
