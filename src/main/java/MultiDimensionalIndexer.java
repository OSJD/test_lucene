import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.spatial3d.Geo3DPoint;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;

import java.io.IOException;

/**
 * Created by wso2123 on 8/25/16.
 */
public class MultiDimensionalIndexer {

  private final Directory indexDir = new RAMDirectory();
  private IndexWriter writer;
  private static MultiDimensionalIndexer instance;


  private MultiDimensionalIndexer() throws IOException {

    //Initiate the writer
    writer = new IndexWriter(indexDir, new IndexWriterConfig(
            new WhitespaceAnalyzer()).setOpenMode(IndexWriterConfig.OpenMode.CREATE));
  }


  //Simple Singleton approach
  public static MultiDimensionalIndexer getInstance() throws IOException {
    if (instance == null)
      instance = new MultiDimensionalIndexer();

    return instance;
  }


  public IndexWriter getWriter() {
    return writer;
  }

  public void put(DataField... data) throws Exception {
    Document doc = new Document();
    for (DataField dataItem : data) {
      switch (dataItem.type) {

        case INT_POINT:
          int[] ints = (int[]) dataItem.value;
          doc.add(new IntPoint(dataItem.name,ints));
          break;

        case FLOAT_POINT:
          float[] floats = (float[])dataItem.value;
          doc.add(new FloatPoint(dataItem.name,floats));
          break;

        case DOUBLE_POINT_1D:
          double doubleValue = (double)dataItem.value;
          doc.add(new DoublePoint(dataItem.name,doubleValue));
          break;

        case DOUBLE_POINT:
          double[] doubles = (double[])dataItem.value;
          doc.add(new DoublePoint(dataItem.name,doubles));
          break;

        case LATLON_POINT:
          double[] latLon = (double[])dataItem.value;
          if(latLon.length!=2)
            throw new Exception("Invalid data entry for LatLon point");
          doc.add(new LatLonPoint(dataItem.name,latLon[0],latLon[1]));
          break;

        case GEO3D_POINT:
          double[] geo3d = (double[])dataItem.value;
          if(geo3d.length!=2)
            throw new Exception("Invalid data entry for LatLon point");
          doc.add(new Geo3DPoint(dataItem.name,geo3d[0],geo3d[1]));
          break;


        default:
          doc.add(new StoredField(dataItem.name,(String) dataItem.value));
          break;
      }
    }
    writer.addDocument(doc);

  }
}
