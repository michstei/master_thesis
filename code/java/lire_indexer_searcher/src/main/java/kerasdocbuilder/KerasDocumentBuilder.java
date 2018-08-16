package kerasdocbuilder;

import kerasfeatures.KerasFeature;
import net.semanticmetadata.lire.builders.DocumentBuilder;
import net.semanticmetadata.lire.imageanalysis.features.GlobalFeature;
import net.semanticmetadata.lire.indexers.hashing.BitSampling;
import net.semanticmetadata.lire.indexers.hashing.LocalitySensitiveHashing;
import net.semanticmetadata.lire.indexers.hashing.MetricSpaces;
import net.semanticmetadata.lire.indexers.parallel.ExtractorItem;
import net.semanticmetadata.lire.utils.ImageUtils;
import net.semanticmetadata.lire.utils.SerializationUtils;
import org.apache.lucene.document.*;
import org.apache.lucene.util.BytesRef;

import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class KerasDocumentBuilder implements DocumentBuilder{

    private boolean useDocValues = true;

    public enum HashingMode {BitSampling, LSH, MetricSpaces, None}

    private HashingMode hashingMode = HashingMode.BitSampling;
    private boolean hashingEnabled = false;

    private HashMap<ExtractorItem, String[]> extractorItems = new HashMap<ExtractorItem, String[]>(10);
    private boolean docsCreated = false;



    /**
     * Creates a GlobalDocumentBuilder with the specific hashing mode. Please note that you have to take care of the
     * initilization of the hashing subsystem yourself.
     *
     * @param hashing     true if you want hashing to be applied.
     * @param hashingMode the actual mode, eg. BitSampling or MetricSpaces.
     */
    public KerasDocumentBuilder(boolean hashing, HashingMode hashingMode) {
        this.hashingEnabled = hashing;
        this.hashingMode = hashingMode;
        if (hashingEnabled) testHashes();
    }







    /**
     * Can be used to add global extractors.
     *
     * @param kerasFeature
     */
    public void addExtractor(Class<? extends KerasFeature> kerasFeature) {
        addExtractor(new ExtractorItem(kerasFeature));
    }

    /**
     * Can be used to add global extractors.
     *
     * @param extractorItem
     */
    public void addExtractor(ExtractorItem extractorItem) {
        if (docsCreated)
            throw new UnsupportedOperationException("Cannot modify builder after documents have been created!");
        if (!extractorItem.isGlobal())
            throw new UnsupportedOperationException("ExtractorItem must contain GlobalFeature");

        String fieldName = extractorItem.getFieldName();
        extractorItems.put(extractorItem, new String[]{fieldName, fieldName + DocumentBuilder.HASH_FIELD_SUFFIX});
    }

    private static void testHashes() {
//        Let's try to read the hash functions right here and we don't have to care about it right now.
        try {
            BitSampling.readHashFunctions();
//            LocalitySensitiveHashing.readHashFunctions();
        } catch (Exception e) {
            System.err.println("Could not read BitSampling hashes from file when first creating a GlobalDocumentBuilder instance.");
            e.printStackTrace();
        }
    }

    /**
     * Images are resized so as not to exceed the {@link DocumentBuilder#MAX_IMAGE_DIMENSION}, after that
     * the feature is extracted using the given kerasFeature.
     *
     * @param image         is the image
     * @param kerasFeature selected global feature
     * @return the input kerasFeature
     */
    public KerasFeature extractKerasFeature(BufferedImage image, KerasFeature kerasFeature) {
        assert (image != null);
        // Scaling image is especially with the correlogram features very important!
        // All images are scaled to guarantee a certain upper limit for indexing.
        if (Math.max(image.getHeight(), image.getWidth()) > DocumentBuilder.MAX_IMAGE_DIMENSION) {
            image = ImageUtils.scaleImage(image, DocumentBuilder.MAX_IMAGE_DIMENSION);
        }

        kerasFeature.extract(image);
        return kerasFeature;
    }

    /**
     * Extracts the global feature and returns the Lucene Fields for the selected image.
     *
     * @param image         is the selected image.
     * @param extractorItem is the extractor to be used to extract the features.
     * @return Lucene Fields.
     */
    private Field[] getGlobalDescriptorFields(BufferedImage image, ExtractorItem extractorItem) {
        Field[] result;
//        if (hashingEnabled) result = new Field[2];
//        else result = new Field[1];
        Field hash = null;
        Field vector = null;

        KerasFeature kerasFeature = extractKerasFeature(image, (KerasFeature) extractorItem.getExtractorInstance());

        if (!useDocValues) {
            // TODO: Stored field is compressed and upon search decompression takes a lot of time (> 50% with a small index with 50k images). Find something else ...
            vector = new StoredField(extractorItems.get(extractorItem)[0], new BytesRef(kerasFeature.getByteArrayRepresentation()));
        } else {
            // Alternative: The DocValues field. It's extremely fast to read, but it's all in RAM most likely.
            vector = new BinaryDocValuesField(extractorItems.get(extractorItem)[0], new BytesRef(kerasFeature.getByteArrayRepresentation()));
        }


        // if BitSampling is an issue we add a field with the given hashFunctionsFileName and the suffix "hash":
        if (hashingEnabled) {
            // TODO: check eventually if there is a more compressed string version of the integers. i.e. the hex string
            if (kerasFeature.getFeatureVector().length <= 3100) {
                int[] hashes;
                if (hashingMode == HashingMode.BitSampling) {
                    hashes = BitSampling.generateHashes(kerasFeature.getFeatureVector());
                    hash = new TextField(extractorItems.get(extractorItem)[1], SerializationUtils.arrayToString(hashes), Field.Store.YES);
                } else if (hashingMode == HashingMode.LSH) {
                    hashes = LocalitySensitiveHashing.generateHashes(kerasFeature.getFeatureVector());
                    hash = new TextField(extractorItems.get(extractorItem)[1], SerializationUtils.arrayToString(hashes), Field.Store.YES);
                } else if (hashingMode == HashingMode.MetricSpaces) {
//                    if (MetricSpaces.supportsFeature(kerasFeature)) {
//                        // the name of the field is set at "addExtractor" time.
//                        hash = new TextField(extractorItems.get(extractorItem)[1], MetricSpaces.generateHashString(kerasFeature), Field.Store.YES);
//                    }
                }
            } else
                System.err.println("Could not create hashes, feature vector too long: " + kerasFeature.getFeatureVector().length + " (" + kerasFeature.getClass().getName() + ")");
        }
        if (hash != null) result = new Field[]{vector, hash};
        else result = new Field[]{vector};
        return result;
    }


    /**
     * @param image the image to analyze.
     * @return Lucene Fields.
     */
    @Override
    public Field[] createDescriptorFields(BufferedImage image) {
        docsCreated = true;
        LinkedList<Field> resultList = new LinkedList<Field>();
        Field[] fields;
        if (extractorItems.size() > 0) {
            for (Map.Entry<ExtractorItem, String[]> extractorItemEntry : extractorItems.entrySet()) {
                fields = getGlobalDescriptorFields(image, extractorItemEntry.getKey());

                Collections.addAll(resultList, fields);
            }
        }

        return resultList.toArray(new Field[resultList.size()]);
    }

    /**
     * @param image      the image to index. Cannot be NULL.
     * @param identifier an id for the image, for instance the filename or a URL. Can be NULL.
     * @return a Lucene Document.
     */
    @Override
    public Document createDocument(BufferedImage image, String identifier) {
        Document doc = new Document();

        if (identifier != null) {
            doc.add(new StringField(DocumentBuilder.FIELD_NAME_IDENTIFIER, identifier, Field.Store.YES));
        }

        Field[] fields = createDescriptorFields(image);
        for (Field field : fields) {
            doc.add(field);
        }

        return doc;
    }


}
