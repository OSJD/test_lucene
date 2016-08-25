import org.apache.lucene.document.Document;
import org.apache.lucene.document.DoublePoint;
import org.apache.lucene.document.LatLonPoint;
import org.apache.lucene.geo.Polygon;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.search.FieldComparator;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wso2123 on 8/25/16.
 */
public class MultiDimensionalSearcher {

  private IndexSearcher searcher;

  public MultiDimensionalSearcher(IndexWriter writer) throws IOException {
    searcher = new IndexSearcher(DirectoryReader.open(writer));
  }


  //This just convert the results to a String Array. Easy to print
  private List<String> getRecordsAsStrings(TopDocs docs) throws IOException {

    List<String> returnList = new ArrayList<>();
    for (ScoreDoc scoreDoc : docs.scoreDocs
            ) {
      Document doc = searcher.doc(scoreDoc.doc);
      String str = "";
      for(IndexableField field:doc.getFields())
      {
        str+=field.name()+"="+field.stringValue()+"\t|\t";
      }
      returnList.add(str);
    }
    return returnList;
  }


  /****************************************************************
  Exposing the following API to the test class ;)
  ********************************************************************/

  //Possible Queries for a LatLon Field----------------------------------------------------------
  //Returns the points within the given radius from the given point - radius in meters
  public List<String> search_LatLon_Distance(String field, Double lat, Double lon, Double radius, int count) throws IOException {
    TopDocs docs = searcher.search(LatLonPoint.newDistanceQuery(field, lat,lon,radius), count);
    return getRecordsAsStrings(docs);
  }

  //Return points within the given polygon
  //TODO support Holes and Composite Polygons
  public List<String> search_LatLon_Polygon(String field, double[] lats, double[] lons, int count) throws IOException {
    TopDocs docs = searcher.search(LatLonPoint.newPolygonQuery(field,new Polygon(lats,lons)),count);
    return getRecordsAsStrings(docs);
  }

  //Return points within the given box. it's a rage query actually.
  public List<String> search_LatLon_Box(String field, double minLat, double maxLat, double minLon, double maxLon, int count) throws IOException {
    TopDocs docs = searcher.search(LatLonPoint.newBoxQuery(field,minLat,maxLat, minLon, maxLon),count);
    return getRecordsAsStrings(docs);
  }

  //Return the nearest points
  public List<String> search_LatLon_Nearest(String field, double lat, double lon, int count) throws IOException {
    TopDocs docs = LatLonPoint.nearest(searcher,field,lat,lon,count);
    return getRecordsAsStrings(docs);
  }





  //Possible Queries for a Double 1D Field----------------------------------------------------------
  //Double Point
  //Exact Query. gives values with the exact value
  public List<String> search_Double1D_Exact(String field, double value, int count) throws IOException {
    TopDocs docs = searcher.search(DoublePoint.newExactQuery(field,value), count);
    return getRecordsAsStrings(docs);
  }


  //Search double range 1D
  public List<String> search_Double1D_Range(String field, double lower, double upper, int count) throws IOException {
    TopDocs docs = searcher.search(DoublePoint.newRangeQuery(field,lower,upper),count);
    return  getRecordsAsStrings(docs);
  }




  //Possible Queries for a Double Field----------------------------------------------------------
  //Search double range multi dimensional
  public List<String> search_Double_Range(String field, double[] lower, double[] upper, int count) throws IOException {
    TopDocs docs = searcher.search(DoublePoint.newRangeQuery(field,lower,upper),count);
    return  getRecordsAsStrings(docs);
  }

}
