## Itinerary

> **Packages dependencies** If you intend to use the this service you have to make sure that the `itinerary` package have been properly downloaded. You can easily check if the package is available on the device with the following method: <a href="http://dev.insiteo.com/api/doc/android/3.4/reference/com/insiteo/lbs/common/auth/entities/ISSite.html#hasPackage(com.insiteo.lbs.common.init.ISEPackageType)" target="_blank">`ISSite#hasPackage(EPackageType.ITINERARY);`</a>.

### Enable itinerary rendering

To use location-based services, such as `Itinerary` or `GeoFencing` you have to get the module from the location provider. If you want it to be displayed on an INSITEO map, do as below:

```java
// Retrieve the itinerary module from the LocationProvider
itineraryProvider = (ISItineraryProvider) ISLocationProvider.getInstance().getModule(ISELocationModule.ITINERARY);

// get itinerary renderer linked to provider
itineraryRenderer = (ISItineraryRenderer) itineraryProvider.getRenderer(getResources());
itineraryRenderer.setPriority(10);

// Add this renderer to the map renderers list
mapView.addRenderer(itineraryRenderer);

// Register for itinerary rendering user interaction (such as clicks).
itineraryRenderer.setListener(listener);
```

> **Customizing the rendering**: if you want you can change the colors that are used for the itinerary, for that you simply need to override any of the following xml values:

```xml
<color name="insiteo_itinerary_section_color_normal">#ff226c79</color>
<color name="insiteo_itinerary_section_color_disabled">#ff666666</color>
<color name="insiteo_itinerary_section_color_selectable">#ff226c79</color>
<color name="insiteo_itinerary_section_color_selected">#ff00b7df</color>

<color name="insiteo_itinerary_section_background_color_normal">#ffffffff</color>
<color name="insiteo_itinerary_section_background_color_disabled">#ffcccccc</color>
<color name="insiteo_itinerary_section_background_color_selectable">#ffffffff</color>
<color name="insiteo_itinerary_section_background_color_selected">#ffffffff</color>

<color name="insiteo_itinerary_waypoint_color_normal">#ff226c79</color>
<color name="insiteo_itinerary_waypoint_color_mapchange_enter">#ffeeb052</color>
<color name="insiteo_itinerary_waypoint_color_mapchange_exit">#ff91e58f</color>
<color name="insiteo_itinerary_waypoint_color_first">#ff28ab25</color>
<color name="insiteo_itinerary_waypoint_color_last">#ffee0000</color>

<color name="insiteo_itinerary_waypoint_background_color">#ffffffff</color>

<color name="insiteo_itinerary_animation_point_color">#ffffffff</color>
``` 

### Request an itinerary between two points

To compute an itinerary between two points, simply do like this:

```java
// Request an itinerary between two points.
ISPosition departure = new ISPosition(startMapId, 40, 40);
ISPosition arrival = new ISPosition(destMapId, 168, 100);
itineraryProvider.requestItinerary(departure, arrival, listener, PMR_ENABLED);
```

> **Note:** An <a href="http://dev.insiteo.com/api/doc/android/3.4/reference/com/insiteo/lbs/itinerary/ISItineraryProvider.BaseRequest.html" target="_blank">`ISBaseRequest`</a> is returned in order to identify sender through callbacks.

### Request an itinerary from user location

To compute an itinerary from the user location, please do like this:

```java
// Request an itinerary from the user location to a point arrival.
// With USE_LAST_LOC, you can decide to use the last location or wait for a new one.
// With PMR_ENABLED, you can set the itinerary to use only path for disabled persons.
ISPosition arrival = new ISPosition(mapID, 168, 100);
itineraryProvider.requestItineraryFromCurrentLocation(arrival, USE_LAST_LOC, listener, PMR_ENABLED);
```

### Request an optimized route

If you want to compute an optimized route between several points, you can proceed as follow:

```java
// Request an optimized route between several positions (an OptimizeRequest is returned)
itineraryProvider.requestOptimizedItinerary(pos, ISEOptimizationMode.NearestNeighbourShortestPath, true, false, this, false);
```

> **Note:** You can specify if you want to keep the first position, last position, or both.

> **Note:** There are multiple optimization modes available but we highly recommend to keep `ISEOptimizationMode.NearestNeighbourShortestPath` as the default one.

### Recomputation

When using the itinerary service along with the location you can refresh the route according to the user's position in the following way:

```java
/**
 * Callback fired when the itinerary of the last request changed (when location is updated for example). 
 * This method can be used to ask for a new itinerary if the user is now too far from the itinerary (we usually recompute
 * the itinerary over a distance of 5 meters).
 * @param request the related request
 * @param aDistanceToIti the distance between the user location and the itinerary (in meter)
 */
@Override
public void onItineraryChanged(BaseRequest request, float distanceToIti) {
	if (request instanceof ItineraryRequest && LocationProvider.getInstance().isStarted()) {	
		if (distanceToIti > MAX_RECOMPUTATION_DISTANCE || distanceToIti == -1) {
			request.recompute();
		}
	}
}
```

> **Note:** We usually use `10.0` meters as `MAX_RECOMPUTATION_DISTANCE`.

## Where to go from there?

- [Setup your first geofencing zone](geofence.md).
- [Enable analytics](analytics.md).

## You missed a thing?

- [Project setup](../README.md).
- [Display your first map](map.md).
- [Get your first location](location.md).
- [Configure and launch iBeacon service](beacon.md).

