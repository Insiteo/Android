## Analytics

### Starting the Analytics service

In order to use the analytics service you have to make sure it gets started during the API initialization. If you did not specify the analytics behavior in the API initialization method the service will be started automatically if the packages depedencies prerequisite is met. Otherwise this will depends on the value passed as the analyticsAutoStart as true will try to start it and false will keep it turned off and no data will be stored during the session.

### Location events

If you have decided to use the analytics service then the user's location will be automatically sent for datamining analysis. In order to avoid internet requests overload location will be aggregated according to a given frequency that can be set in the back office.

### Generic events

For any other type of events you would like to keep track of you can use the <a href="http://dev.insiteo.com/api/doc/android/3.4/reference/com/insiteo/lbs/analytics/entities/ISAnalyticsGenericEvent.html" target="_blank">`ISAnalyticsGenericEvent`</a>. This event enables you to add up to 2 NSString, 2 int, 2 double, 2 <a href="http://dev.insiteo.com/api/doc/android/3.4/reference/com/insiteo/lbs/common/utils/geometry/ISPosition.html" target="_blank">`ISPosition`</a> and a label to match most cases.

Adding a <a href="http://dev.insiteo.com/api/doc/android/3.4/reference/com/insiteo/lbs/analytics/entities/ISAnalyticsGenericEvent.html" target="_blank">`ISAnalyticsGenericEvent`</a> to the <a href="http://dev.insiteo.com/api/doc/android/3.4/reference/com/insiteo/lbs/analytics/ISAnalyticsManager.html" target="_blank">`ISAnalyticsManager`</a>.

```java
ISAnalyticsGenericEvent event = new ISAnalyticsGenericEvent("generic_event");
zoneEvent.setString1("product a");
zoneEvent.setPos1(location.getPosition());
ISAnalyticsManager.getInstance().addGenericEvent(event);
```

Insiteo's API already trace some basic events among them:
- Location start and stop
- Geofence entered, stayed and left
- Map changes, zone clicks and <a href="http://dev.insiteo.com/api/doc/android/3.4/reference/com/insiteo/lbs/map/render/ISIRTO.html" target="_blank">`ISRTO`</a> added with <a href="http://dev.insiteo.com/api/doc/android/3.4/reference/com/insiteo/lbs/map/entities/ISZonePoi.html" target="_blank">`ISZonePoi`</a>
- Itinerary requests

#### Tracking products displayed on map

If you want to trace when a product is added to the map you can use <a href="http://dev.insiteo.com/api/doc/android/3.4/reference/com/insiteo/lbs/map/ISMapView.html#addRTOInZone(com.insiteo.lbs.map.render.ISIRTO, com.insiteo.lbs.map.entities.ISZonePoi)" target="_blank">`ISMapView#addRTOInZone(ISIRTO, ISZonePoi)`</a>. This will generate a <a href="http://dev.insiteo.com/api/doc/android/3.4/reference/com/insiteo/lbs/analytics/entities/ISAnalyticsGenericEvent.html" target="_blank">`ISAnalyticsGenericEvent`</a> with the String1 set with the external id of the Poi defined in the <a href="http://dev.insiteo.com/api/doc/android/3.4/reference/com/insiteo/lbs/map/entities/ISZonePoi.html" target="_blank">`ISZonePoi`</a>.

#### Completing a generic event

It can be useful in some cases to add information to a <a href="http://dev.insiteo.com/api/doc/android/3.4/reference/com/insiteo/lbs/analytics/entities/ISAnalyticsGenericEvent.html" target="_blank">`ISAnalyticsGenericEvent`</a> for that you must set a <a href="http://dev.insiteo.com/api/doc/android/3.4/reference/com/insiteo/lbs/analytics/ISIAnalyticsListener.html" target="_blank">`ISAnalyticsListener`</a> that will be notified everytime a new generic event is added. Returning false in this method will dismiss the event.

## You missed a thing?

- [Project setup](../README.md).
- [Display your first map](map.md).
- [Get your first location](location.md).
- [Configure and launch iBeacon service](beacon.md).
- [Compute your first itinerary](itinerary.md).
- [Setup your first geofencing zone](geofence.md).
