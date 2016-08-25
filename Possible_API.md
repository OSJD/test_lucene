#Possible API Functions

For multi dimensional space search Lucene 6.1 provides following Fields to index locations in multidimensional spaces.

* Basic Space Points
    * IntPoint
    * FloatPoint
    * DoublePoint
    
* LatLonPoint
* Geo3DPoint


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

####LatLonPoint
Used to Query on the surface of a Planet.

An indexed location field. Finding all documents within a range at search time is efficient. Multiple values for the same field in one document is allowed.

Accept 2 double values.
```
LatLonPoint(String name, double latitude, double longitude)
```

####Geo3DPoint
Similar to LatLonPoint. If this has no use over LatLon this can be neglected but It has shape query which supports paths.



##Querying

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
* Polygon Query(an Array of Polygon )
* Box Query (MinLat, MaxLat, MinLon, MaxLon)
* K-Nearest(Location, K-Number of Points) => Return the K-nearest points.


####Geo3DPoint
* Other than the existing queries, this support geoShape query. 
    GeoShape can be used to query, 
    * Polygons with holes
    * Paths with a given width