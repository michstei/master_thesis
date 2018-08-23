package keras.searcher;

import net.semanticmetadata.lire.searchers.ImageSearchHits;
import org.apache.lucene.index.IndexReader;

import java.io.IOException;

public interface KerasSearcher {
    public ImageSearchHits search(String image, IndexReader reader) ;
}
