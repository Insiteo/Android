package com.insiteo.sampleapp.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.insiteo.lbs.beacon.ISBeaconProvider;
import com.insiteo.sampleapp.R;

public class SettingsActivity extends AppCompatActivity {

    private Toolbar mToolbar;

    // Beacon specific settings
    private Switch mBeaconSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initToolbar();
        initElements();
    }


    private void initToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
    }

    private void initElements() {
        mBeaconSwitch = (Switch) findViewById(R.id.beacon_switch);
        if(mBeaconSwitch != null) {
            mBeaconSwitch.setOnCheckedChangeListener(mBeaconSwitchListener);
            mBeaconSwitch.setChecked(ISBeaconProvider.getInstance(this).isStarted());
        }
    }

    //**********************************************************************************************
    // Beacon settings listener
    // *********************************************************************************************

    private CompoundButton.OnCheckedChangeListener mBeaconSwitchListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            ISBeaconProvider beaconProv = ISBeaconProvider.getInstance(getApplicationContext());

            SharedPreferences prefs = getSharedPreferences(SettingsPrefs.BEACON_PREFS, Context.MODE_PRIVATE);
            prefs.edit().putBoolean(SettingsPrefs.BEACON_MONITORING_ENABLED, isChecked).apply();

            if (isChecked) {
                if (beaconProv != null && !beaconProv.isStarted()) beaconProv.start();
            } else {
                if (beaconProv != null && beaconProv.isStarted()) beaconProv.stop();
            }
        }
    };

}
