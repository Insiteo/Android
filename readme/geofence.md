## Geofencing

> **Packages dependencies** If you intend to use the this service you have to make sure that the `geofencing` package have been properly downloaded. You can easily check if the package is available on the device with the following method: <a href="http://dev.insiteo.com/api/doc/android/3.4/reference/com/insiteo/lbs/common/auth/entities/ISSite.html#hasPackage(com.insiteo.lbs.common.init.ISEPackageType)" target="_blank">`ISSite#hasPackage(EPackageType.GEOFENCING);`</a>.

### Start the Geofencing module

To start the `Geofencing` module, simply do like so:

```java
// Retrieve the geofencing module
geofenceProvider = (ISGeofenceProvider) ISLocationProvider.getInstance().getModule(ISELocationModule.GEOFENCING);

// Register as a IGeofenceListener
geofenceProvider.setListener(listener);
```

### Understand geonotifications

After starting the module, your delegate will be notified with 3 arrays of <a href="http://dev.insiteo.com/api/doc/android/3.4/reference/com/insiteo/lbs/geofence/ISGeofenceArea.html" target="_blank">`ISGeoFenceArea`</a>.

- The first one contains all zones the user just entered.
- The second one contains all zones where the user still is and has spent a certain time.
- The third one contains all zones the user just left.

```java
/**
 * Called when geofencing module has new data available.
 * @param enteredAreas list of areas that location has just entered  
 * @param stayedAreas list of areas where location has stayed for a certain amount of time
 * @param leftAreas list of areas that location has just left
 */
@Override
public void onGeofenceUpdate(List enteredAreas, List stayedAreas, List leftAreas) {
	Log.d("Geofencing", " onGeofenceUpdate: " + enteredAreas.size() + ", " 
 + stayedAreas.size() + ", " + leftAreas.size()); 
}
```

### Dynamic geofencing

In the last version of our API, geopush content can be added to the <a href="http://dev.insiteo.com/api/doc/android/3.4/reference/com/insiteo/lbs/geofence/ISGeofenceProvider.html" target="_blank">`ISGeofenceProvider`</a> directly from your application in addition to the one fetched from the server. This enables, for example, your content to be more accurate to a specific user's behaviour or using context.

- The created <a href="http://dev.insiteo.com/api/doc/android/3.4/reference/com/insiteo/lbs/geofence/ISGeofenceArea.html" target="_blank">`ISGeofenceArea`</a>'s polygon will be based on the specific <a href="http://dev.insiteo.com/api/doc/android/3.4/reference/com/insiteo/lbs/map/entities/ISZone.html" target="_blank">`ISZone`</a> parameters that have to be provided in the back office.
- If the <a href="http://dev.insiteo.com/api/doc/android/3.4/reference/com/insiteo/lbs/geofence/ISGeofenceArea.html" target="_blank">`ISGeofenceArea`</a> parameters (ie width, enteredTime, enteredEnabled ... ) are not set they will be fetched from the configuration file. This configuration file defines those parameters by <a href="http://dev.insiteo.com/api/doc/android/3.4/reference/com/insiteo/lbs/map/entities/ISMap.html" target="_blank">`ISMap`</a> and not by <a href="http://dev.insiteo.com/api/doc/android/3.4/reference/com/insiteo/lbs/map/entities/ISZone.html" target="_blank">`ISZone`</a>.
- If the creation succeeded the <a href="http://dev.insiteo.com/api/doc/android/3.4/reference/com/insiteo/lbs/geofence/ISGeofenceArea.html" target="_blank">`ISGeofenceArea`</a> will be automatically added to the <a href="http://dev.insiteo.com/api/doc/android/3.4/reference/com/insiteo/lbs/geofence/ISGeofenceProvider.html" target="_blank">`ISGeofenceProvider`</code></a>.

#### Adding content to a specific zone or for a specific zone/poi association

To add a geopush content to a specific <a href="http://dev.insiteo.com/api/doc/android/3.4/reference/com/insiteo/lbs/map/entities/ISZone.html" target="_blank">`ISZone`</a> or <a href="http://dev.insiteo.com/api/doc/android/3.4/reference/com/insiteo/lbs/map/entities/ISZonePoi.html" target="_blank">`ISZonePoi`</a>, you can use the methods shown below.

A polygon based on the ISZone parameters and the provided <a href="http://api.insiteo.com/apidocs/ios/v3.4/Classes/ISGeofenceArea.html" target="_blank">`ISGeofenceArea`</a> width will be created and this <a href="http://dev.insiteo.com/api/doc/android/3.4/reference/com/insiteo/lbs/geofence/ISGeofenceArea.html" target="_blank">`ISGeofenceArea`</a> will be automatically added to the <a href="http://dev.insiteo.com/api/doc/android/3.4/reference/com/insiteo/lbs/geofence/ISGeofenceProvider.html" target="_blank">`ISGeofenceProvider`</a>.

```java
// For a Zone
public ISGeofenceArea addGeofenceArea(int zoneId, String content);

public ISGeofenceArea addGeofenceArea(int zoneId, String content, long eventTime);

public ISGeofenceArea addGeofenceArea(int zoneId, String content, boolean enteredEnabled, long enteredTime, boolean stayedEnabled, long stayedTime, boolean leaveEnabled, long leaveTime, float width);

// For a ZonePoi association

public ISGeofenceArea addGeofenceArea(ISZonePoi zonePoi, String content);

public ISGeofenceArea addGeofenceArea(ISZonePoi zonePoi, String content, long eventTime);

public ISGeofenceArea addGeofenceArea(ISZonePoi zonePoi, String content, boolean enteredEnabled, long enteredTime, boolean stayedEnabled, long stayedTime, boolean leaveEnabled, long leaveTime, float width);
```

#### Adding content for a given position

To add a geopush content at a specific <a href="http://dev.insiteo.com/api/doc/android/3.4/reference/com/insiteo/lbs/common/utils/geometry/ISPosition.html" target="_blank">`ISPosition`</a>, you can use the methods shown below.

A square of size on the given parameter (or by default 4 time the size defined in the configuration file) and center on the given position will be created.

```java
public ISGeofenceArea addGeofenceArea(String guid, ISPosition center, String content);

public ISGeofenceArea addGeofenceArea(String guid, ISPosition center, String content, long eventTime);

public ISGeofenceArea addGeofenceArea(String guid, ISPosition center, String content, boolean enteredEnabled, long enteredTime, boolean stayedEnabled, long stayedTime, boolean leaveEnabled, long leaveTime);

public ISGeofenceArea addGeofenceArea(String guid, ISPosition center, float size, String content);

public ISGeofenceArea addGeofenceArea(String guid, ISPosition center, float size, String content, long eventTime);

public ISGeofenceArea addGeofenceArea(String guid, ISPosition center, float size, String content, boolean enteredEnabled, long enteredTime, boolean stayedEnabled, long stayedTime, boolean leaveEnabled, long leaveTime);
```

#### Removing a dynamic geofence area

To remove a <a href="http://dev.insiteo.com/api/doc/android/3.4/reference/com/insiteo/lbs/geofence/ISGeofenceArea.html" target="_blank">`ISGeofenceArea`</a> from the <a href="http://dev.insiteo.com/api/doc/android/3.4/reference/com/insiteo/lbs/geofence/ISGeofenceProvider.html" target="_blank">`ISGeofenceProvider`</a> call the appropriate remove method based on how it was added.

```java
public void removeGeofenceArea(String guid);

public void removeGeofenceArea(int zoneId);

public void removeGeofenceArea(ISZonePoi zonePoi);

public void removeGeofenceArea(ISGeofenceArea area);
```

### Geofencing rendering

You can now view your <a href="http://dev.insiteo.com/api/doc/android/3.4/reference/com/insiteo/lbs/geofence/ISGeofenceArea.html" target="_blank">`ISGeofenceArea`</a> on your <a href="http://dev.insiteo.com/api/doc/android/3.4/reference/com/insiteo/lbs/map/ISMapView.html" target="_blank">`ISMapView`</a>. Like all other LBS services, you will have to retrieve its <a href="http://dev.insiteo.com/api/doc/android/3.4/reference/com/insiteo/lbs/map/render/ISIRenderer.html" target="_blank">`ISRenderer`</a> and pass it to the <a href="http://dev.insiteo.com/api/doc/android/3.4/reference/com/insiteo/lbs/map/ISMapView.html" target="_blank">`ISMapView`</a>. All the geofencing zone will be displayed ie the one define on the back office that one created dynamically.

> **Note:** 3D rendering is not available for this module.

## Where to go from there?

- [Enable analytics](analytics.md).

## You missed a thing?

- [Project setup](../README.md).
- [Display your first map](map.md).
- [Get your first location](location.md).
- [Configure your iBeacons](beacon.md).
- [Compute your first itinerary](itinerary.md).
