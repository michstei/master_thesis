package keras;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import java.io.FileNotFoundException;

public interface KerasDocumentBuilder {

    /**
     * Creates the feature fields for a Lucene Document without creating the document itself.
     *
     * @param imagePath path to the image to analyze.
     * @return the fields resulting from the analysis.
     */
    Field[] createDescriptorFields(String imagePath);

    /**
     * Creates a new Lucene document from a BufferedImage. The identifier can be used like an id
     * (e.g. the file hashFunctionsFileName or the url of the image)
     *
     * @param imagePath path to the image to index. Cannot be NULL.
     * @param identifier an id for the image, for instance the filename or a URL. Can be NULL.
     * @return a Lucene Document containing the indexed image.
     */
    Document createDocument(String imagePath , String identifier) throws FileNotFoundException;

}
