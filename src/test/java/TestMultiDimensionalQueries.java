import org.antlr.v4.runtime.misc.Triple;
import org.apache.lucene.facet.range.DoubleRange;
import org.apache.lucene.spatial3d.geom.GeoPoint;
import org.apache.lucene.spatial3d.geom.PlanetModel;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wso2123 on 8/25/16.
 */
public class TestMultiDimensionalQueries {

  public static MultiDimensionalSearcher searcher;
  public static MultiDimensionalIndexer indexer;

  public static final String FIELDNAME_LATLON = "latlon";
  public static final String FIELDNAME_GEO3D= "geo3d";
  public static final String FIELDNAME_ALT = "alt";
  public static final String FIELDNAME_4D = "latLonAltTime";

  public static final double COLOMBO_LAT =  6.9270790;
  public static final double COLOMBO_LON =  79.8612430;
  public static final double COLOMBO_ALTITUDE =  22.0;


  //Sample Poylgon
  private static double[] polygonLat;
  private static double[] polygonLon;
  private static List<GeoPoint> polygonGeoPoints = new ArrayList<>();


  @BeforeClass
  public static void setup() throws Exception {

    System.out.println("\nLoading Data ----------------------------------------------------------------------");
    //Index Data
    indexer = MultiDimensionalIndexer.getInstance();

    //Index AirPort Locations
    try (BufferedReader br = new BufferedReader(new FileReader("./data/airports.dat"))) {
      String line;

      String[] fieldNames = new String[]{"Airport ID", "Name", "City", "Country", "IATA/FAA", "ICAO", "Latitude", "Longitude", "Altitude", "Timezone", "DST", "Tz database time zone"};
      while ((line = br.readLine()) != null) {
        // process the line.
        String[] tags = line.split(",");

        //Field array to be indexed
        //+1 is for LatLon
        DataField[] fields = new DataField[fieldNames.length+4];

        //Add all fieldsa as stored fields first
        for(int i=0; i<fieldNames.length;i++)
        {
          fields[i] = new DataField(fieldNames[i],tags[i], DataField.ColumnType.TEXT);
        }

        try {
          //Add LatLon LatLonPoint. data shoule be given as an Double array with length 2.
          double[] latLon = new double[2];
          latLon[0] = Double.parseDouble(tags[6]);
          latLon[1] = Double.parseDouble(tags[7]);

          double altitude = Double.parseDouble(tags[8]);
          double timeZone = Double.parseDouble(tags[9]);

          fields[fieldNames.length] = new DataField(FIELDNAME_LATLON, latLon, DataField.ColumnType.LATLON_POINT);
          fields[fieldNames.length+1] = new DataField(FIELDNAME_GEO3D, latLon, DataField.ColumnType.GEO3D_POINT);
          fields[fieldNames.length+2] = new DataField(FIELDNAME_ALT, altitude, DataField.ColumnType.DOUBLE_POINT_1D);

          double[] latLonAltTime = {latLon[0],latLon[1],altitude,timeZone};
          fields[fieldNames.length+3] = new DataField(FIELDNAME_4D, latLonAltTime, DataField.ColumnType.DOUBLE_POINT);

        }
        catch (Exception e)
        {
          System.out.println("Parse Error:"+tags[0]+"\t"+e);
          continue;
        }
        //Index all fields
        indexer.put(fields);
      }
    }

    //Create Searcher
    searcher = new MultiDimensionalSearcher(indexer.getWriter());


    //Load a sample Plygon for testing
    loadMap();


    System.out.println("Loading Data is finished ----------------------------------------------------------------------");
  }


  //This method load the polygon of Sri Lanka as a sample polygon
  public static void loadMap() throws FileNotFoundException {
    try (BufferedReader br = new BufferedReader(new FileReader("./data/Sri_Lanka"))) {
      String line = br.readLine();
      String[] rows = line.split(" ");
      polygonLat = new double[rows.length];
      polygonLon = new double[rows.length];

      int count = 0;
      for (String row : rows
              ) {
        String[] splitedRow = row.split(",");
        polygonLat[count] = Double.parseDouble(splitedRow[1]);
        polygonLon[count] = Double.parseDouble(splitedRow[0]);

        polygonGeoPoints.add(new GeoPoint(PlanetModel.WGS84, polygonLat[count] * Math.PI / 180, polygonLon[count] * Math.PI / 180));
        count++;
      }


    } catch (Exception e) {
      System.out.println(e);
    }
  }

  private void printResult(List<String> list)
  {
    System.out.println("Result:"+list.size());
    for(String item:list)
      System.out.println(item);
  }


  //Lat Lon queries

  //Within a given distance Example: Air Ports within the 500km from colombo
  @Test
  public void search_LatLon_Distance() throws IOException {
    System.out.println("\nLatLon - Distance Query Example------------------------------------------------------------------------------");
    List<String> result = searcher.search_LatLon_Distance(FIELDNAME_LATLON,COLOMBO_LAT,COLOMBO_LON,50*1000.0,100);
    printResult(result);
  }


  //Inside a given polygon.
  //TODO support Holes and Composite Polygons
  @Test
  public void search_LatLon_Polygon() throws IOException {
    System.out.println("\nLatLon - Polygon Query Example------------------------------------------------------------------------------");
    List<String> result = searcher.search_LatLon_Polygon(FIELDNAME_LATLON,polygonLat,polygonLon,100);
    printResult(result);
  }

  //Returns points within a lat log box
  @Test
  public void search_LatLon_Box() throws IOException {
    System.out.println("\nLatLon - Box Query Example------------------------------------------------------------------------------");
    List<String> result = searcher.search_LatLon_Box(FIELDNAME_LATLON,COLOMBO_LAT,COLOMBO_LAT+1, COLOMBO_LON, COLOMBO_LON+1,10000);
    printResult(result);
  }

  //Return the given number of nearest points
  @Test
  public void search_LatLon_Nearest() throws IOException {
    System.out.println("\nLatLon - K Nearest------------------------------------------------------------------------------");
    List<String> result = searcher.search_LatLon_Nearest(FIELDNAME_LATLON,COLOMBO_LAT, COLOMBO_LON,50);
    printResult(result);
  }



  //Test Codes for DoublePoint 1D Quetries

  //Exact value query for 1D only. Search for the airports with the exact altitude of Colombo Airport
  @Test
  public void search_Double_1D_Exact() throws IOException {
    System.out.println("\nDoublePoint 1D Point Exact------------------------------------------------------------------------------");
    List<String> result = searcher.search_Double1D_Exact(FIELDNAME_ALT,COLOMBO_ALTITUDE,500);
    printResult(result);
  }

  //Search for the airports which has the given altitude range.
  @Test
  public void search_Double_1D_range() throws IOException {
    System.out.println("\nDoublePoint 1D - Range------------------------------------------------------------------------------");
    List<String> result = searcher.search_Double1D_Range(FIELDNAME_ALT,800,805,50);
    printResult(result);
  }

  //Divide and give counts for each bucket, buckets are depend on the altitude ranges provided
  @Test
  public void search_Double_1D_range_bucket() throws IOException {
    System.out.println("\nDoublePoint 1D - Range Buckets -----------------------------------------------------------------------------");


    Triple<String,Double,Double>[] buckets = new Triple[10];

    //Create Buckets
    for(int i=0; i<10; i++)
    {
      buckets[i] = new Triple<>(String.valueOf(i),i*100.0,(i+1)*100.0);
    }

    List<String> result = searcher.search_Double1D_Range_Buckets_Simple(FIELDNAME_ALT,buckets);
    printResult(result);
  }

  //Give airports which has the given ranges for each value.
  // {Latitude, Longitude, Altitude, TimeZone}
  /*
  Latitude: 0-10
  Longitude: 0-10
  Altitude: 0-1
  TimeZone: 0-1

  The airport which match the given ranges will be displayed.
   */
  @Test
  public void search_Double_MiltiDimensional_Range() throws IOException {
    System.out.println("\nDoublePoint multi dimensional - Range------------------------------------------------------------------------------");
    List<String> result = searcher.search_Double_Range(FIELDNAME_4D, new double[]{0, 0, 0, 0},new double[]{10,10,1,1},50);
    printResult(result);
  }



  //Print airports with the given range Geo3D circle. Points within a given distance.
  //Colombo is the center. radius is 50km.
  @Test
  public void search_Geo3D_Distance() throws IOException {
    System.out.println("\nGeo3D Distance------------------------------------------------------------------------------");
    List<String> result = searcher.search_Geo3D_Distance(FIELDNAME_GEO3D,COLOMBO_LAT,COLOMBO_LON,50*1000.0,50);
    printResult(result);
  }



}
