/**
 * Created by wso2123 on 8/4/16.
 */

import org.junit.Before;
import org.junit.Test;
import java.io.IOException;


public class LuceneTester {

    String path = "/home/wso2123/wso2/test_luc/";
    @Before
    public void TestIndex()
    {
        Indexer indexer = new Indexer();
        indexer.index(path+"data",path+"index",true);
    }

    @Test
    public void TestSearch() throws Exception {
        System.out.println("-------------------------\n Running the query");
        Searcher searcher = new Searcher();
        searcher.search(path+"index","contents","dependency");
    }

}
