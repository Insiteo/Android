package com.insiteo.sampleapp;

import com.insiteo.lbs.common.init.InitProvider.EServerType;
import com.insiteo.lbs.map.render.ERenderMode;

public class InsiteoConf {

	// This set of values represent all the required data to identify a site on a given server
	public static final int SITE_ID = 364;
	public static final String LANG = "fr";

	// Insiteo API uses a server value to properly store the data on the device. You have to make sure to set the right Server.X value according to the server url that you are using.
	// For example here the url targets our test server this is why the server is Server.TEST
	// Server.DEV will store the data on the SD card under insiteo/sites/dev/{SITE_ID}/{LANG}/{VERSION}
	// Server.TEST will store the data on the SD card under insiteo/sites/test/{SITE_ID}/{LANG}/{VERSION}
	// Server.PROD will store the data on the SD card under insiteo/sites/release/{SITE_ID}/{LANG}/{VERSION}
	public static final EServerType SERVER = EServerType.TEST;
	

	/** The ERenderMode that should be used. */
	public static final ERenderMode RENDER_MODE    = ERenderMode.RENDER_MODE_2D;

	// This key that Insiteo will provide you is mandatory to use our API.
	public static final String API_KEY = "5a30df2a-6038-698f-e387-0691ff1239d6";

	/** Enables the log for the Java level code */
	public static final boolean LOG_ENABLED    			= true;

	/** Enables the log for the embedded code (native library) */
	public static final boolean EMBEDDED_LOG_ENABLED    = false;

}
