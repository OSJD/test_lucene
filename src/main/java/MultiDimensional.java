import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.SloppyMath;

import java.io.IOException;

/**
 * Created by wso2123 on 8/18/16.
 */
public class MultiDimensional {

  private final Directory indexDir = new RAMDirectory();
  private IndexSearcher searcher;

  public static void main(String[] args) throws Exception {
    MultiDimensional exampleProject = new MultiDimensional();

    //Call above method to generate a sample index
    exampleProject.GenerateSampleIndex();

    //Simple Box Query
    exampleProject.boxQuery();


  }

  private void GenerateSampleIndex() throws IOException {

    IndexWriter writer = new IndexWriter(indexDir, new IndexWriterConfig(
            new WhitespaceAnalyzer()).setOpenMode(IndexWriterConfig.OpenMode.CREATE));

    //Insert Data
    Document doc = new Document();
    doc.add(new FloatPoint("pos", 0, 0));
    doc.add(new StoredField("id", 0));
    writer.addDocument(doc);

    doc = new Document();
    doc.add(new FloatPoint("pos", 0, 1));
    doc.add(new StoredField("id", 1));
    writer.addDocument(doc);

    doc = new Document();
    doc.add(new FloatPoint("pos", 1, 1));
    doc.add(new StoredField("id", 2));
    writer.addDocument(doc);

    doc = new Document();
    doc.add(new FloatPoint("pos", 0, 0));
    doc.add(new StoredField("id", 3));
    writer.addDocument(doc);

    // Open near-real-time searcher
    searcher = new IndexSearcher(DirectoryReader.open(writer));
    writer.close();
  }

  public void boxQuery() throws IOException {

    System.out.println("Multi dimensional box as a range query --------------------------------------");
//    TopDocs docs = searcher.search(DoublePoint.newRangeQuery("pos", new double[]{0, 12}, new double[]{13, 50}), 10);
    TopDocs docs = searcher.search(LatLonPoint.newDistanceQuery("pos",0, 0,9950000), 10);
    for (ScoreDoc scoreDoc : docs.scoreDocs
            ) {
      Document doc = searcher.doc(scoreDoc.doc);

      System.out.println(doc);

    }
    System.out.println("----------------------------------------------------------------------------");
    return;
  }

  private void printDocument(Document doc) {

    for (IndexableField f : doc.getFields()
            ) {
      System.out.print(f.name() + "=" + doc.get(f.name()) + " | ");
    }
    System.out.println();
  }


}
