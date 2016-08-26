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

1. Points - K-Nearest `(Double lat, Double lon, Double radius, int k)`
2. Points - Sorted by Distance from a given location `(Double lat, Double lon, int limit)`
3. Points - within a given radius (from a given location)
4. Points - inside the polygon - Polygon Query -  `(IPolygon compositePolygon, int limit)`
    * Supports composite polygons and polygons with holes.
    * So there should be a set of __Public Polygon Generation Factory Methods__
5. Points - for each Polygon Bucket - Bucket Query `(List<IPolygonBucket> polygonBuckets)`
    * `PolygonBucket` contains bucket_label and polygon. Similar to [AnalyticsDrillDownRange](https://github.com/janakact/carbon-analytics/blob/master/components/analytics-core/org.wso2.carbon.analytics.dataservice.commons/src/main/java/org/wso2/carbon/analytics/dataservice/commons/AnalyticsDrillDownRange.java).


Following can be reduced to polygon query to simplyfy the interface
6. Points - inside the Box Query or rage query for a given Minimum Latitude, Maximum Latitude, Minimum Longitude, Maximum Longitude

#####Counts
7. Point Count - within a given radius
8. Point Count - inside a polygon 
9. Point Count - for each bucket for a list of polygons.
10. Point Count - inside a given box

####Sorting and Composition

Lucene support sorting as a seperate functionality for all query results. Sorts can be aplied only on sortable fields.
* K-Nearest, Polygon, PolygoBucket can be sorted by a given sortable field. 
* There should be a mechanism to make composite queries using a set of queries.
    * There are two options for now. 
        1. Implement a QueryRequest class and composistion factory methods for that.
        2. Expose Lucenes Query class and factory methods.
        > 1st one can be prefered because interface of lucene may change by time, there for we need a connector to keep our interfaces consistant. 

* Further this sorting and filtering composition can be from text search too. Therefore text search should also support this query composition.

##VectorSpace Field
1. Exact Query
2. Range Query `(int[] lowMargin, int[] highMargin) or (double[] lowMargin, double[] hightMargin)`
3. Rage Bucke Query 

#####Counts
4. Point Count - Exact Query
5. Point Count - Range Query
6. Point Count - for each range bucket