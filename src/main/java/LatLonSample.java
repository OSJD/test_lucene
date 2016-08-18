import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * Created by wso2123 on 8/18/16.
 */
public class LatLonSample {


    private IndexSearcher searcher;
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

            String[] fieldNames = new String[]{"Airport ID","Name", "City", "Country", "IATA/FAA","ICAO", "Latitude","Longitude","Altitude","Timezone", "DST", "Tz database time zone"};
            while ((line = br.readLine()) != null) {
                // process the line.
                String[] tags = line.split(",");
               // System.out.println(line);

                Document doc = new Document();
                for(int i=0; i<fieldNames.length; i++)
                    doc.add(new StoredField(fieldNames[i],tags[i]));

                //Add a latlon point to index
                try {
                    doc.add(new LatLonPoint("latlon", Double.parseDouble(tags[6]), Double.parseDouble(tags[7])));
                }
                catch (Exception e)
                {
                    System.out.println("Skipped: "+line);
                }
                writer.addDocument(doc);
            }
        }


        searcher = new IndexSearcher(DirectoryReader.open(writer));
    }

    public void search() throws IOException {
        System.out.println("\nLatLonQuery --------------------------------------");

        TopDocs docs = searcher.search(LatLonPoint.newDistanceQuery("latlon",  6.9270790, 79.8612430, 500*1000  ),10);
        for (ScoreDoc scoreDoc: docs.scoreDocs
                ) {
            Document doc = searcher.doc(scoreDoc.doc);

            System.out.println(doc);

        }

        System.out.println("-----------------------------------------------------------------");

    }

    public static void main(String[] args) throws IOException {
        LatLonSample sample  = new LatLonSample();
        sample.generateTheIndex();
        sample.search();
    }

}
