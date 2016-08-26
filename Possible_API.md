#Possible API Functions

For multi dimensional space search Lucene 6.1 provides following Fields to index locations in multidimensional spaces.

* Basic Space Points
    * IntPoint
    * FloatPoint
    * DoublePoint
    
* LatLonPoint and Geo3DPoint

Geo3DPoint similar to LatLonPoint. It is an internal implementation design, which is about the math used inside calculation. LatLonPoint uses Haversine distance while Geo3D ueses direct math. That's what I heard. ;)
It seems like that LatLonPoint has better performance. ([Benchmarks](http://home.apache.org/~mikemccand/geobench.html))

##Indexing

####Basic Space Points (IntPoint, FloatPoint, DoublePoint)
It accept any number of integers, double or float.

* 1 Dimensional - Single Value
* N Dimensional - an Array with length N
```
IntPoint(String name, int... point)
FloatPoint(String name, float... point)
DoublePoint(String name, double... point)
```

####LatLonPoint and Geo3DPoint
Used to Query on the surface of a Planet.

An indexed location field. Finding all documents within a range at search time is efficient. Multiple values for the same field in one document is allowed.

Accept 2 double values.
```
LatLonPoint(String name, double latitude, double longitude)
```


##Querying - Direct

####Basic Space Points

For API, 
* For 1 dimensional fields - there are 3 possible queries
```
    newExactQuery(String, double) for matching an exact 1D point.
    newSetQuery(String, double...) for matching a set of 1D values.
    newRangeQuery(String, double, double) for matching a 1D range.
```

* For N dimensional fields - 1 possible query
```
    newRangeQuery(String, double[], double[]) for matching points/ranges in n-dimensional space. 
```
    

####LatLonPoint
* Distance Query(Location, Radius)
```
public static Query newDistanceQuery(String field,
                                     double latitude,
                                     double longitude,
                                     double radiusMeters)
```

* Polygon Query(an Array of Polygon )
```
public static Query newPolygonQuery(String field,
                                    Polygon... polygons)
```
* Box Query (MinLat, MaxLat, MinLon, MaxLon)
```
public static Query newBoxQuery(String field,
                                double minLatitude,
                                double maxLatitude,
                                double minLongitude,
                                double maxLongitude)
```
* K-Nearest(Location, K-Number of Points) => Return the K-nearest points. This is not a query this is a search result. Therefore searcher shoul be provided. 
```
public static TopFieldDocs nearest(IndexSearcher searcher,
                                   String field,
                                   double latitude,
                                   double longitude,
                                   int n)
                            throws IOException
```


####Geo3DPoint
Other than the existing queries, this support geoShape query. Also Geo3DPoint do not support K-Nearest query. 
    GeoShape can be used to query, 
* Polygons with holes
```
    public static GeoPolygon makeGeoPolygon(PlanetModel planetModel,
                                            List<GeoPoint> pointList,
                                            List<GeoPolygon> holes)
```

* Paths with a given width. Width of the path should be provided as a angle(radian) from the center of the planet.
```
public static GeoPath makeGeoPath(PlanetModel planetModel,
                                  double maxCutoffAngle,
                                  GeoPoint[] pathPoints)
```

##Querying - Indirect

####Basic Space Points

####LatLon or Geo3D
* Area Buckets (Polygon... polygons) - Accepts a series of polygons (Areas marked on the planet surface).
 Those polygons can represent buckets. This will devide points in to each area, and put them into buckets and return.
 
 
 
 
#Public Interface

There are two indexable fields. 

* GeoPosition Field to search on the surface of planet.
    > Seeems like we have to index 
    > * LatLonPoint
    > * Geo3DPoint
    > * **LatLonDocValuesField** - If you also need per-document operations such as sort by distance, add a separate LatLonDocValuesField instance.

* VectorSpace Field - Multi Dimensional Space Field.
    > There is a problem whether to index using int, double, or float.. may be all
    > This can be used for scientific functionalities. It is hard to find much use cases here.
    

##GeoPosition Field

* K-Nearest
* 

    

##VectorSpace Field
* Range Query
* Exact Query