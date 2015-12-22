## Map

> **Packages dependencies** Be aware that you need to have installed at least the following packages:
- A `MAPDATA` package, which contains maps information such as zoom levels and scales.
- **2D only** A `TILES` package, which contains <code>.3cm</code> files that will be displayed.
- **3D only** A `MAP3DPACKAGE` package, which contains all 3D files.

> You can easily check if the package is available on the device with the following method: <a href="http://dev.insiteo.com/api/doc/android/3.4/reference/com/insiteo/lbs/common/auth/entities/ISSite.html#hasPackage(com.insiteo.lbs.common.init.ISEPackageType)" target="_blank">`ISSite#hasPackage(EPackageType.MAP_DATA);`</a>.


### Display a MapView

A <a href="http://dev.insiteo.com/api/doc/android/3.4/reference/com/insiteo/lbs/map/ISMapView.html" target="_blank">`ISMapView`</a> is provided and can be used to display a 2D/3D map component with specific interactive areas. The API also provides advanced features such as additional rendering layouts management (promotional, special areas etc &#8230;) or specific events handling. To use this component you have to make sure that you have downloaded all the required packages. 
		
The <a href="http://dev.insiteo.com/api/doc/android/3.4/reference/com/insiteo/lbs/map/ISMapView.html" target="_blank">`ISMapView`</a> will display a `2D` tiled map or `3D` plane of the site (depending on your <a href="http://dev.insiteo.com/api/doc/android/3.4/reference/com/insiteo/lbs/map/render/ISERenderMode.html" target="_blank">`ISERenderMode`</a>), and provide common map interaction (such as move, center, and pinch to zoom), animation and more. It also handle the <a href="http://dev.insiteo.com/api/doc/android/3.4/reference/com/insiteo/lbs/map/entities/ISZone.html" target="_blank">`ISZone`</a> rendering and touch behavior.
	
The <a href="http://dev.insiteo.com/api/doc/android/3.4/reference/com/insiteo/lbs/map/ISMap3DView.html" target="_blank">`ISMap3DView`</a> uses the <a href="http://www.jpct.net/jpct-ae/index.html" target="_blank">`JPCT`</a> Android engine and you can refer to its documentation <a href="http://www.jpct.net/jpct-ae/doc/" target="_blank">here</a>.


In order to use our `MapAPI`, you will need to instanciate a <a href="http://dev.insiteo.com/api/doc/android/3.4/reference/com/insiteo/lbs/map/ISMap2DView.html" target="_blank">`ISMap2DView`</a> or a <a href="http://dev.insiteo.com/api/doc/android/3.4/reference/com/insiteo/lbs/map/ISMap3DView.html" target="_blank">`ISMap3DView`</a> (you can not use both instance simultaneously):

```java
// ISMap2DView Java instanciation
ISMap2DView mMapView = new ISMap2DView(getActivity()); 
```

```xml
<!-- ISMap2DView instanciation via xml layout -->
<com.insiteo.lbs.map.ISMap2DView
    android:id="@+id/map"
    android:layout_width="fill_parent"
    android:layout_height="fill_parent"/>
```

> **Prerequisites** You will need to initialize <a href="http://dev.insiteo.com/api/doc/android/3.4/reference/com/insiteo/lbs/common/init/Insiteo.html" target="_blank">`Insiteo`</a> before instantiating <a href="http://dev.insiteo.com/api/doc/android/3.4/reference/com/insiteo/lbs/map/ISMapView.html" target="_blank">`ISMapView`</a>. <a href="http://dev.insiteo.com/api/doc/android/3.4/reference/com/insiteo/lbs/map/ISMapView.html" target="_blank">`ISMapView`</a> events will be notified via the <a href="http://dev.insiteo.com/api/doc/android/3.4/reference/com/insiteo/lbs/map/ISIMapListener.html" target="_blank">`ISIMapListener`</a> interface. The single <a href="http://dev.insiteo.com/api/doc/android/3.4/reference/com/insiteo/lbs/map/ISIMapListener.html" target="_blank">`ISIMapListener`</a> associated to the map is by default the `Context` that created it (the `Activity` in most cases) but can be change with the <a href="http://dev.insiteo.com/api/doc/android/3.4/reference/com/insiteo/lbs/map/ISMapView.html#setListener(com.insiteo.lbs.map.ISIMapListener)" target="_blank">`ISMapView#setListener(ISIMapListener listener)`</a> method (useful when your using the <a href="http://dev.insiteo.com/api/doc/android/3.4/reference/com/insiteo/lbs/map/ISMapView.html" target="_blank">`ISMapView`</a> in a `Fragment`).

### Adding graphical objects on map

The <a href="http://dev.insiteo.com/api/doc/android/3.4/reference/com/insiteo/lbs/map/ISMapView.html" target="_blank">`ISMapView`</a> also allows you to display custom interactive objects. This can be done by implementing the renderer interface <a href="http://dev.insiteo.com/api/doc/android/3.4/reference/com/insiteo/lbs/map/render/ISIRenderer.html" target="_blank">`ISIRenderer`</a> and the Render Touch Object interface <a href="http://dev.insiteo.com/api/doc/android/3.4/reference/com/insiteo/lbs/map/render/ISIRTO.html" target="_blank">`ISIRTO`</a> or by simply extending the <a href="http://dev.insiteo.com/api/doc/android/3.4/reference/com/insiteo/lbs/map/render/ISGenericRenderer.html" target="_blank">`ISGenericRenderer`</a> and <a href="http://dev.insiteo.com/api/doc/android/3.4/reference/com/insiteo/lbs/map/render/ISGenericRTO.html" target="_blank">`ISGenericRTO`</a> that already provide common behavior (icon and label rendering, touch handling and so on, you can check their behavior in our SampleApp).

The <a href="http://dev.insiteo.com/api/doc/android/3.4/reference/com/insiteo/lbs/map/ISMapView.html" target="_blank">`ISMapView`</a> will also detect touches, and dispatch them to all <a href="http://dev.insiteo.com/api/doc/android/3.4/reference/com/insiteo/lbs/map/render/ISIRTO.html" target="_blank">`ISIRTO`</a>. A listener can be set on the map view, to be notified of clicks on specific <a href="http://dev.insiteo.com/api/doc/android/3.4/reference/com/insiteo/lbs/map/render/ISIRTO.html" target="_blank">`ISIRTO`</a> class (see <a href="http://dev.insiteo.com/api/doc/android/3.4/reference/com/insiteo/lbs/map/ISMapView.html" target="_blank">`ISMapView`</a> class documentation).


Adding, removing <a href="http://api.insiteo.com/apidocs/android/v3.3/reference/com/insiteo/lbs/map/render/ISGenericRTO.html" target="_blank">`ISGenericRTO`</a> to the <a href="http://dev.insiteo.com/api/doc/android/3.4/reference/com/insiteo/lbs/map/ISMapView.html" target="_blank">`ISMapView`</a> and listening for their events:

```java
/* This method will add the rto at the map center */
ISGenericRto genericRTO = new ISGenericRTO(mapView.getScreenCenter(), "MyRTO");
mapView.addRTO(genericRTO);

/* And to remove it */
mapView.removeRTO(genericRTO);

/* This method will add the rto to the given zone */
ISGenericRto genericRTO = new ISGenericRTO(mapView.getScreenCenter(), "MyRTO");
genericRTO.setLabel("MyRTO");
mapView.addRTOInZone(zoneId, genericRTO);

/* And to remove it */
mapView.removeRTOFromZone(zoneId, genericRTO);

/* Add a listener for this type of IRTO */
mMapView.setRTOListener(listener, GfxRto.class);
```

### Create your own ISRenderer

A renderer is a class that defines drawing and touch behavior for a certain type of <a href="http://api.insiteo.com/apidocs/android/v3.3/reference/com/insiteo/lbs/common/rendertouch/IRTO.html" target="_blank">`IRTO`</a>. Once added to the <a href="http://dev.insiteo.com/api/doc/android/3.4/reference/com/insiteo/lbs/map/ISMapView.html" target="_blank">`ISMapView`</a> a renderer will have its `render2D` or `render3D` method call by the map rendering loop to enable to do its rendering operation and it will also be notify when to handle touch events (`onTouchDown`, `onTouchPointerDown`, `onTouchMove`, `onTouchPointerUp`, `onTouchUp`). If you want to use your own customized renderer, you will need to create a class that implements the <a href="http://api.insiteo.com/apidocs/android/v3.3/reference/com/insiteo/lbs/common/rendertouch/IRenderer.html" target="_blank">`IRenderer`</a> interface. Then you will be able to specify your own renderering and touch behavior. 

`IRenderer` uses a priority value that will define it's 2D rendering and touch order. Highest priority renderered last (so on the top) and notify by touch first.

To register a new renderer as a map's renderer, simply do like this:

```java
/* How to add a custom renderer */
mapView.addRenderer(myCustomRenderer);
```

### Create your own ISIRTO

To draw a customized rendering object on the map, you will need to create a class that implements the <a href="http://dev.insiteo.com/api/doc/android/3.4/reference/com/insiteo/lbs/common/rendertouch/ISIRTO.html" target="_blank">`ISIRTO`</a> interface. Then you will be able to specify your object's behavior through methods like:

```java
/* The method you will need to override in order to manually manage your object 2D rendering */
public void render2D(Canvas aCanvas, double aRatio, Point aOffset, float aAngle) {
}

/* The method you will need to override in order to manually manage your object 3D rendering */
public void render3D(ISWorld world, FrameBuffer frameBuffer, Map map, double ratio, float angle) {
}

/* Because once added to the world a 3D object will always be drawn it is up to you to remove the object from the world when required */
public void remove3DObject(ISWorld world) {
}

/* Method that gets called when the IRTO have to handle a touch down event */
public ETouchObjectResult onTouchDown(Touch aTouch) {
```

#### Where to find my ISRTO?

All <a href="http://dev.insiteo.com/api/doc/android/3.4/reference/com/insiteo/lbs/map/render/ISIRTO.html" target="_blank">`ISIRTO`</a> of class corresponding to the custom renderer class, when added via <a href="http://dev.insiteo.com/api/doc/android/3.4/reference/com/insiteo/lbs/map/ISMapView.html" target="_blank">`ISMapView`</a>, will be put in custom renderer. If you add an <a href="http://dev.insiteo.com/api/doc/android/3.4/reference/com/insiteo/lbs/common/rendertouch/ISIRTO.html" target="_blank">`ISIRTO`</a> and there are no <a href="http://dev.insiteo.com/api/doc/android/3.4/reference/com/insiteo/lbs/map/render/ISIRenderer.html" target="_blank">`ISIrenderer`</a> defined for that specific class, the <a href="http://dev.insiteo.com/api/doc/android/3.4/reference/com/insiteo/lbs/map/ISMapView.html" target="_blank">`ISMapView`</a> will automatically create a <a href="http://dev.insiteo.com/api/doc/android/3.4/reference/com/insiteo/lbs/map/render/ISGenericRenderer.html" target="_blank">`ISGenericRenderer`</a> to handle it. So creating your own <a href="http://dev.insiteo.com/api/doc/android/3.4/reference/com/insiteo/lbs/common/rendertouch/ISIRTO.html" target="_blank">`ISIRTO`</a> does not mean that you necessarily have to create you own <a href="http://dev.insiteo.com/api/doc/android/3.4/reference/com/insiteo/lbs/map/render/ISIRenderer.html" target="_blank">`ISIrenderer`</a>.

#### Zone offsets in 3D

In 3D, you can specify an offset through the z axis still by using the following method <a href="http://dev.insiteo.com/api/doc/android/3.4/reference/com/insiteo/lbs/map/ISMapView.html#addRTOInZone(int, com.insiteo.lbs.map.render.ISIRTO, SimpleVector)" target="_blank">`mapView.addRTOInZone(zoneId, rto, zoneOffset)`</a>.

#### Zone/POI

With the Insiteo API, you can link your content to our zone based system. To do that you will have to register this content through our back office. For example you can link a shop to one of our zone and then, get back this content in your application, simply by requesting our API.

### Link with external content

With the Insiteo framework, you can link your content to our zone based system. To do that you will have to register this content through our back office. For example you can link a shop to one of our zone and then, get back this content in your application, simply by requesting our framework.

<a href="https://www.youtube.com/watch?v=CLvNfQuzyUw" target="_blank">![alt tag](http://img.youtube.com/vi/CLvNfQuzyUw/0.jpg)</a>

<a href="https://www.youtube.com/watch?v=CLvNfQuzyUw" target="_blank">Insiteo Interactive maps - 2 minutes tutorial</a>

To get all related Insiteo zones for a given external POI, you can use the <a href="http://dev.insiteo.com/api/doc/android/3.4/reference/com/insiteo/lbs/map/database/ISMapDBHelper.html" target="_blank">`ISMapDBHelper`</a> class like so:

```java
// Get all Zone/POI assocations for a given external identifier
List<ZonePoi> zonePois = ISMapDBHelper.getZoneAssocFromExtPoi(extPoiID);
```

> **Note:** A list is returned, because you can link a POI to several zones and a zone can contains several POIs.


To get all POIs related to a given Insiteo zone, you can use the <a href="http://dev.insiteo.com/api/doc/android/3.4/reference/com/insiteo/lbs/map/database/ISMapDBHelper.html" target="_blank">`ISMapDBHelper`</a> class like so:

```java
//Get all external Zone/POI assocations for a given zone identifier
public static List<ISZonePoi> getPoiAssocFromZone(int aZoneId, boolean aExternal);
```

Each method returns an <a href="http://dev.insiteo.com/api/doc/android/3.4/reference/com/insiteo/lbs/map/entities/ISZonePoi.html" target="_blank">`ISZonePoi`</a> object which contains a position in meters and an offset (if specified) in order to place your on <a href="http://dev.insiteo.com/api/doc/android/3.4/reference/com/insiteo/lbs/map/render/ISIRTO.html" target="_blank">`ISIRTO`</a> on our map.

#### Zone/Poi associations offsets

If you want an offset to be used when drawing an <a href="http://dev.insiteo.com/api/doc/android/3.4/reference/com/insiteo/lbs/map/render/ISIRTO.html" target="_blank">`ISIRTO`</a> in a <a href="http://dev.insiteo.com/api/doc/android/3.4/reference/com/insiteo/lbs/map/entities/ISZone.html" target="_blank">`ISZone`</a> you have to explicitly set it we adding the <a href="http://dev.insiteo.com/api/doc/android/3.4/reference/com/insiteo/lbs/map/render/ISIRTO.html" target="_blank">`ISIRTO`</a> to the <a href="http://dev.insiteo.com/api/doc/android/3.4/reference/com/insiteo/lbs/map/ISMapView.html" target="_blank">`ISMapView`</a>.


### Best practices

- **Map rendering** It is best practice to call the <a href="http://dev.insiteo.com/api/doc/android/3.4/reference/com/insiteo/lbs/map/ISMapView.html" target="_blank">`ISMapView`</a> <a href="http://dev.insiteo.com/api/doc/android/3.4/reference/com/insiteo/lbs/map/ISMapView.html#onPause()" target="_blank">`onPause`</a> and <a href="http://dev.insiteo.com/api/doc/android/3.4/reference/com/insiteo/lbs/map/ISMapView.html#onResume()" target="_blank">`onResume`</a> methods in your `Activity` or `Fragment` respective methods.

- **Handling loads of POI** if your application needs to handle a large amount of graphical object it can be best to filter what should be rendered depending on the zoomLevel. Use the `onZoomEnd(int newZoomLevel)` method to remove old `ISIRTO` from the `ISMapView` and add the new ones.

## Where to go from there?

- [Get your first location](location.md).
- [Configure your iBeacons](beacon.md).
- [Compute your first itinerary](itinerary.md).
- [Setup your first geofencing zone](geofence.md).
- [Enable analytics](analytics.md).

## You missed a thing?

- [Project setup](../README.md).
