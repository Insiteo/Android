# Using the Beacon Service

In order to use the Insiteo BeaconService in your application you must extend the <code>ISBeaconApplication class</code>. You can create your own Application objet. As soon as your Application Object is specified in <code>AndroidManifest.xml</code>, Android System and the Insiteo Library will handle the proper behavior of the Service.

```java
public class MyBeaconApplication extends ISBeaconApplication {
  ...
}
```

Don't forget to specify it in your <code>AndroidManifest.xml</code> application tag.


```xml
<application
  android:name=".MyBeaconApplication"
  android:icon="@drawable/ic_launcher"
  android:label="@string/app_name"
  android:launchMode="singleInstance">
</application>
```

## Monitoring & ranging 

The beacon service will work with the region that are configured on our back office and therefore the monitoring will only start if an API initialization has been done once.

Your application will be the entry point for all the beacon events. <code>ISBeaconApplication</code> already implements default behavior but you can override any of the following methods:


```java

/**
 * You should override this method if you don't want the service to be automatically started.
 * For instance if you want to start the service only if the user has agreed to the usage conditions.
 * @return <code>true</code> if the service should be started
 */
 protected boolean shouldAutoStartService() {
     return true;
 }

/**
 * Callback triggered when the user enters an ISBeaconRegion
 * @param region entered
 */
@Override
public void onEnteredBeaconRegion(ISBeaconRegion region) {
}

/**
 * Callback triggered when the user exits an ISBeaconRegion
 * @param region exited
 */
@Override
public void onExitBeaconRegion(ISBeaconRegion region) {
}

/**
 * Callback triggered when the a local notification should be displayed. 
 * @param region that should be notified
 * @return <code>false</code> if you want to override this behavior
 */
@Override
public boolean shouldSendNotification(ISBeaconRegion region) {
    return true;
}

/**
 * Callback triggered when the user entered an ISBeacon
 * @param beacon entered
 * @param region matching that beacon
 */
@Override
public void onEnterBeacon(ISBeacon beacon, ISBeaconRegion region) {
}

/**
 * Callback triggered when the user entered an ISBeacon
 * @param beacon exited
 * @param region matching that beacon
 */
@Override
public void onExitBeacon(ISBeacon beacon, ISBeaconRegion region) {
}

/**
 * Callback triggered when the user exits a ISBeaconRegion
 * @param beacons that were ranged
 * @param reachedProximityBeacons beacons that reached the region's proximity 
 * @param region that set off the ranging
 * @param unknownBeacons beacons that matched the region identifiers but were not registered for this region in our servers
 */
@Override
public void rangedBeacons(List<ISBeacon> beacons, List<ISBeacon> reachedProximityBeacons, ISBeaconRegion region, List<Beacon> unknownBeacons) {
}
```

By default the <code>ISBeaconApplication</code> register itself as the <code>ISBeaconListener</code> on the <code>BeaconProvider</code>. This is the best practice behavior, nevertheless you can change the listener at your own convenience.

You have to be aware that the service will only work if the BLE is activated on the device. The SDK will not force its activation.

## Customizing your local notification

Insiteo's SDK will automatically handle OS notification (unless you have overriden the default bahavior in the <code>shouldSendNotification()</code> method). Nevertheless you can fully customize those notifications by setting your own style.

```java
/**
 * This is where you should set all the style information for the notification. More information
 * on notification attribute can be found here 
 * @see <a href="http://developer.android.com/reference/android/support/v4/app/NotificationCompat.Builder.html">NotificationCompat.Builder</a>
 * @param builder used for the notification
 * @param region region to be notified
 * @param entered <code>true</code> if the user entered the region (ow exited)
 * @return the notification builder
 */
protected NotificationCompat.Builder setNotificationStyle(NotificationCompat.Builder builder,
                                                              ISBeaconRegion region, boolean entered) {
}

/**
 * Called to set a specific {@link android.content.Intent} for this notification
 * @param builder used for the notification
 * @param region to be notified
 * @return the notifcation builder
 */
protected NotificationCompat.Builder setNotificationIntent(NotificationCompat.Builder builder,
                                                               ISBeaconRegion region) {
}
```

## Required permissions

In order to work the services requires the following permissions

```xml
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />

<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />

<uses-permission android:name="android.permission.WAKE_LOCK" />
```

Those permissions are already set in the library's manifest and will be automatically merged to your application's manifest (unless this setting has been disabled).


## Where to go from there?

- [Compute your first itinerary](itinerary.md).
- [Setup your first geofencing zone](geofence.md).
- [Enable analytics](analytics.md).

## You missed a thing?

- [Project setup](../README.md).
- [Display your first map](map.md).
- [Get your first location](location.md).
