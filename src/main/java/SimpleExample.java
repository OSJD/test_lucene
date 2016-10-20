import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.FloatPoint;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.LatLonPoint;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SimpleExample {

    private static final Directory indexDir = new RAMDirectory();
    private static IndexWriter writer;
    private static IndexSearcher searcher;

    private static final String NAME_TAG = "name";
    private static final String CITY_TAG = "city";
    private static final String LOCATION_TAG = "location";
    private static final String ALT_TAG = "alt";



    public static final double COLOMBO_LAT =  6.9270790;
    public static final double COLOMBO_LON =  79.8612430;
    public static final double COLOMBO_ALTITUDE =  22.0;



    public static void main(String[] args) throws IOException {

        // --------------------------------------------------------------------------------
        //Initiate the writer -------------------------------------------------------------
        writer = new IndexWriter(indexDir, new IndexWriterConfig(
                new WhitespaceAnalyzer()).setOpenMode(IndexWriterConfig.OpenMode.CREATE));



        //Index Data ----------------------------------------------------------------------
        //Sample Data 1 -------------------------------------------------------------------
        Document doc = new Document();
        doc.add(new StoredField(NAME_TAG ,"Colombo Ratmalana")); //Stored fields will be stored in the index and values can be retrieved from the index
        doc.add(new StoredField(CITY_TAG,"Colombo"));

        //Index data as a LatLon Point
        doc.add(new LatLonPoint(LOCATION_TAG, 6.821994,79.886208));

        //Index Altitude as a FloatPoint. An One-Dimensional Point
        doc.add(new FloatPoint(ALT_TAG,22));

        //Write the document into Index
        writer.addDocument(doc);


        //Sample Data 2 -------------------------------------------------------------------
        doc = new Document();
        doc.add(new StoredField(NAME_TAG ,"Bandaranaike Intl Colombo")); //Stored fields will be stored in the index and values can be retrieved from the index
        doc.add(new StoredField(CITY_TAG,"Colombo"));

        //Index data as a LatLon Point
        doc.add(new LatLonPoint(LOCATION_TAG, 7.180756,79.884117));

        //Index Altitude as a FloatPoint. An One-Dimensional Point
        doc.add(new FloatPoint(ALT_TAG,30));

        //Write the document into index
        writer.addDocument(doc);



        // ----------------------------------------------------------------------------------
        //Query for data --------------------------------------------------------------------
        // Create a index searcher using the particular IndexWriter
        searcher = new IndexSearcher(DirectoryReader.open(writer));

        // DPoints within 30 km from colombo city
        TopDocs docs = searcher.search(LatLonPoint.newDistanceQuery(LOCATION_TAG, COLOMBO_LAT,COLOMBO_LON, 30000), 5);
        System.out.println("Within 30km");
        printRecords(docs);



        // DPoints within 20 km from colombo city
        docs = searcher.search(LatLonPoint.newDistanceQuery(LOCATION_TAG, COLOMBO_LAT,COLOMBO_LON, 20000), 5);
        System.out.println("\nWithin 20km");
        printRecords(docs);



    }



    //This just convert the results to a String Array. Easy to print
    private static void printRecords(TopDocs docs) throws IOException {

        for (ScoreDoc scoreDoc : docs.scoreDocs
                ) {
            Document doc = searcher.doc(scoreDoc.doc);
            String str = "";
            for(IndexableField field:doc.getFields())
            {
                str+=field.name()+"="+field.stringValue()+"\t|\t";
            }

            System.out.println(str);
        }
    }


}
