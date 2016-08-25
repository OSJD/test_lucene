import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.LatLonPoint;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.geo.Polygon;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.spatial3d.Geo3DPoint;
import org.apache.lucene.spatial3d.geom.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wso2123 on 8/18/16.
 */
public class LatLonSample {


  private IndexSearcher searcher;
  private double[] mapLat;
  private double[] mapLon;
  private List<GeoPoint> mapGeoPoints = new ArrayList<>();

  public static void main(String[] args) throws IOException {
    LatLonSample sample = new LatLonSample();
    sample.generateTheIndex();
    sample.loadMap();
    sample.search();
  }

  private void generateTheIndex() throws IOException {
    Directory dir = FSDirectory.open(Paths.get("./index_airplane"));
    Analyzer analyzer = new StandardAnalyzer();
    IndexWriterConfig iwc = new IndexWriterConfig(analyzer);

    //
    iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);

    // Optional: for better indexing performance, if you
    // are indexing many documents, increase the RAM
    // buffer.  But if you do this, increase the max heap
    // size to the JVM (eg add -Xmx512m or -Xmx1g):
    //
    // iwc.setRAMBufferSizeMB(256.0);

    IndexWriter writer = new IndexWriter(dir, iwc);

    //load data from file and generate the index
    try (BufferedReader br = new BufferedReader(new FileReader("./data/airports.dat"))) {
      String line;

      String[] fieldNames = new String[]{"Airport ID", "Name", "City", "Country", "IATA/FAA", "ICAO", "Latitude", "Longitude", "Altitude", "Timezone", "DST", "Tz database time zone"};
      while ((line = br.readLine()) != null) {
        // process the line.
        String[] tags = line.split(",");
        // System.out.println(line);

        Document doc = new Document();
        for (int i = 0; i < fieldNames.length; i++)
          doc.add(new StoredField(fieldNames[i], tags[i]));

        //Add a latlon point to index
        try {
          doc.add(new LatLonPoint("latlon", Double.parseDouble(tags[6]), Double.parseDouble(tags[7])));
          Geo3DPoint point = new Geo3DPoint("geo3d", Double.parseDouble(tags[6]) , Double.parseDouble(tags[7]) );
          doc.add(point);

        } catch (Exception e) {
          System.out.println("Skipped: " + line);
        }
        writer.addDocument(doc);
      }
    }


    searcher = new IndexSearcher(DirectoryReader.open(writer));
  }

  public void search() throws IOException {
    System.out.println("\nLatLonQuery  Around Colombo 500km radius --------------------------------------");

    TopDocs docs = searcher.search(LatLonPoint.newDistanceQuery("latlon", 6.9270790, 79.8612430, 500 * 1000), 20);
    for (ScoreDoc scoreDoc : docs.scoreDocs
            ) {
      Document doc = searcher.doc(scoreDoc.doc);

      System.out.println(doc);

    }

//Polygon of the country. only several points. 
    System.out.println("-----------------------------------------------------------------");
    System.out.println("\nLatLonQuery  Polygon Query - Sri Lanka-------------------------------------");

    docs = searcher.search(LatLonPoint.newPolygonQuery("latlon", new Polygon(mapLat, mapLon)), 20);
    for (ScoreDoc scoreDoc : docs.scoreDocs
            ) {
      Document doc = searcher.doc(scoreDoc.doc);

      System.out.println(doc);

    }

// Query for a circle on. center is colombo. Radius is 0.01 radians from the center or the earth. Geo3D works with radians. 
    System.out.println("-----------------------------------------------------------------");
    System.out.println("\nLatLonQuery  Geo3D Circle - Colombo-------------------------------------");
      GeoCircle circle = GeoCircleFactory.makeGeoCircle(PlanetModel.WGS84, toRadians(6.9270790), toRadians( 79.8612430), 0.01);
      docs = searcher.search(Geo3DPoint.newShapeQuery("geo3d",circle), 10);
    for (ScoreDoc scoreDoc : docs.scoreDocs
            ) {
      Document doc = searcher.doc(scoreDoc.doc);

      System.out.println(doc);

    }
    System.out.println("-----------------------------------------------------------------");

  }

//Load Polygon for Sri Lanka from the file as a series of Lat Lon points 
  public void loadMap() throws FileNotFoundException {
    try (BufferedReader br = new BufferedReader(new FileReader("./data/Sri_Lanka"))) {
      String line = br.readLine();
      String[] rows = line.split(" ");
      mapLat = new double[rows.length];
      mapLon = new double[rows.length];

      int count = 0;
      for (String row : rows
              ) {
        String[] splitedRow = row.split(",");
        mapLat[count] = Double.parseDouble(splitedRow[1]);
        mapLon[count] = Double.parseDouble(splitedRow[0]);

        mapGeoPoints.add(new GeoPoint(PlanetModel.WGS84, mapLat[count] * Math.PI / 180, mapLon[count] * Math.PI / 180));
        count++;
      }


    } catch (Exception e) {
      System.out.println(e);
    }
  }


  private static double toRadians(double degrees) {
    //return degrees*(Math.PI/180)*(Math.PI/180);
    return Math.toRadians(degrees);
  }

}
