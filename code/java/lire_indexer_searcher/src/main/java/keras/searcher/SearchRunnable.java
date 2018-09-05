package keras.searcher;

import net.semanticmetadata.lire.searchers.ImageSearchHits;
import org.apache.lucene.index.IndexReader;

import java.awt.*;
import java.time.Duration;
import java.time.Instant;

public class SearchRunnable implements Runnable {
    private ImageSearchHits result;
    private KerasSearcher searcher;
    private IndexReader reader;
    private String imagePath;

    public SearchRunnable( KerasSearcher searcher,IndexReader reader, String imagePath){
        this.searcher = searcher;
        this.reader = reader;
        this.imagePath = imagePath;
    }
    @Override
    public void run() {
        this.result = searcher.search(this.imagePath,this.reader);

    }

    public ImageSearchHits getResult(){
        return this.result;
    }

}
