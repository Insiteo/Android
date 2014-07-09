package com.insiteo.sampleapp;

import com.insiteo.lbs.common.CommonConstants;
import com.insiteo.lbs.common.init.InitProvider.Server;
import com.insiteo.lbs.location.LocationProvider;

public class InsiteoConf {

	// Url of the server where the site is configured.
	public static final String SERVER_URL = "https://services.test.insiteo.com/v" + 
			CommonConstants.URL_VERSION + "/Insiteo.Dispatch.test/Insiteo.Dispatch.svc";

	// This set of values represent all the required data to identify a site on a given server
	public static final int SITE_ID = 300;
	public static final int VERSION = 1;
	public static final String LANG = "fr";

	// Insiteo API uses a server value to properly store the data on the device. You have to make sure to set the right Server.X value according to the server url that you are using.
	// For example here the url targets our test server this is why the server is Server.TEST
	// Server.DEV will store the data on the SD card under insiteo/dev/{SITE_ID}/{LANG}/{VERSION}
	// Server.TEST will store the data on the SD card under insiteo/test/{SITE_ID}/{LANG}/{VERSION}
	// Server.PROD will store the data on the SD card under insiteo/release/{SITE_ID}/{LANG}/{VERSION}
	public static final Server SERVER = Server.TEST;

	// This key that Insiteo will provide you is mandatory to use our API.
	public static final String API_KEY = "5a30df2a-6038-698f-e387-0691ff1239d6";

	// This indicates to the api if it should starts the analytics engine
	public static final boolean ANALYTICS_ENABLED    = false;

	// Those flags represent the technologies that will be used to process the user's location
	// This application uses fake location so those flags won't really matter
	// The available flags are: COMPASS, GPS, WIFI, BLE and MEMS.
	// BLE and WIFI should not be used simultaneously
	public static int LOCATION_FLAGS = LocationProvider.COMPASS|LocationProvider.GPS|/*LocationProvider.WIFI|*/LocationProvider.MEMS;


	public static final boolean LOG_ENABLED    			= true;
	public static final boolean EMBEDDED_LOG_ENABLED    = false;

}
