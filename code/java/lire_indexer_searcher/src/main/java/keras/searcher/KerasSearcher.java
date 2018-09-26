package keras.searcher;

import net.semanticmetadata.lire.searchers.ImageSearchHits;
import org.apache.lucene.index.IndexReader;

public interface KerasSearcher {
    ImageSearchHits search(String image, IndexReader reader) ;
}
