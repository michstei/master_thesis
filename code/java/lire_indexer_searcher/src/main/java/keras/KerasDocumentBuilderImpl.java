package keras;

import net.semanticmetadata.lire.builders.DocumentBuilder;
import net.semanticmetadata.lire.builders.GlobalDocumentBuilder;
import net.semanticmetadata.lire.imageanalysis.features.GlobalFeature;
import net.semanticmetadata.lire.indexers.hashing.BitSampling;
import net.semanticmetadata.lire.indexers.hashing.LocalitySensitiveHashing;
import net.semanticmetadata.lire.indexers.hashing.MetricSpaces;
import net.semanticmetadata.lire.indexers.parallel.ExtractorItem;
import net.semanticmetadata.lire.utils.SerializationUtils;
import org.apache.lucene.document.*;
import org.apache.lucene.util.BytesRef;

import java.io.FileNotFoundException;
import java.util.HashMap;

public class KerasDocumentBuilderImpl implements KerasDocumentBuilder {

    private boolean useDocValues = false;

    public enum HashingMode {BitSampling, LSH, MetricSpaces, None}

    private GlobalDocumentBuilder.HashingMode hashingMode = GlobalDocumentBuilder.HashingMode.BitSampling;
    private boolean hashingEnabled = false;

    private HashMap<ExtractorItem, String[]> extractorItems = new HashMap<ExtractorItem, String[]>(10);
    private boolean docsCreated = false;

    public KerasDocumentBuilderImpl() {
    }

    public KerasDocumentBuilderImpl(boolean hashing) {
        this.hashingEnabled = hashing;
        if (hashingEnabled) testHashes();
    }

    public KerasDocumentBuilderImpl(boolean hashing, boolean useDocValues) {
        this.hashingEnabled = hashing;
        if (hashingEnabled) testHashes();
        this.useDocValues = useDocValues;
    }

    /**
     * Creates a GlobalDocumentBuilder with the specific hashing mode. Please note that you have to take care of the
     * initilization of the hashing subsystem yourself.
     *
     * @param hashing     true if you want hashing to be applied.
     * @param hashingMode the actual mode, eg. BitSampling or MetricSpaces.
     */
    public KerasDocumentBuilderImpl(boolean hashing, GlobalDocumentBuilder.HashingMode hashingMode) {
        this.hashingEnabled = hashing;
        this.hashingMode = hashingMode;
        if (hashingEnabled) testHashes();
    }


    /**
     * Creates a GlobalDocumentBuilder with the specific hashing mode. Please note that you have to take care of the
     * initilization of the hashing subsystem yourself. Optionally use DocValues instead of TextField implementations
     * for storing the feature vector. Note that this cannot be read by ordinary linear searchers, but must be
     * implemented in a different way.
     *
     * @param hashing      true if you want hashing to be applied.
     * @param hashingMode  the actual mode, eg. BitSampling or MetricSpaces.
     * @param useDocValues set to true if you want to use DocValues instead of Lucene fields.
     */
    public KerasDocumentBuilderImpl(boolean hashing, GlobalDocumentBuilder.HashingMode hashingMode, boolean useDocValues) {
        this.hashingEnabled = hashing;
        this.hashingMode = hashingMode;
        this.useDocValues = useDocValues;
        if (hashingEnabled) testHashes();
    }

    public KerasDocumentBuilderImpl(Class<? extends GlobalFeature> globalFeatureClass) {
        addExtractor(globalFeatureClass);
    }

    public KerasDocumentBuilderImpl(Class<? extends GlobalFeature> globalFeatureClass, boolean hashing) {
        addExtractor(globalFeatureClass);
        this.hashingEnabled = hashing;
        if (hashingEnabled) testHashes();
    }


    /**
     * Use DocValues instead of TextField implementations for storing the feature vector. Note that this cannot be
     * read by ordinary linear searchers, but must be implmented in a different way.
     *
     * @param globalFeatureClass
     * @param hashing            set to true if hashing should be performed.
     * @param useDocValues       set to true if you want to use DocValues instead of Lucene fields.
     */
    public KerasDocumentBuilderImpl(Class<? extends GlobalFeature> globalFeatureClass, boolean hashing, boolean useDocValues) {
        addExtractor(globalFeatureClass);
        this.useDocValues = useDocValues;
        this.hashingEnabled = hashing;
        if (hashingEnabled) testHashes();
    }

    public KerasDocumentBuilderImpl(ExtractorItem extractorItem) {
        addExtractor(extractorItem);
    }


    /**
     * Can be used to add global extractors.
     *
     * @param globalFeatureClass
     */
    public void addExtractor(Class<? extends GlobalFeature> globalFeatureClass) {
        addExtractor(new ExtractorItem(globalFeatureClass));
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
     * the feature is extracted using the given globalFeature.
     *
     * @param imagePath         is the imagePath
     * @param kerasFeature selected global feature
     * @return the input globalFeature
     */
    public KerasFeature extractKerasFeature(String imagePath, KerasFeature kerasFeature) {
        assert (imagePath != null);

        kerasFeature.extract(imagePath);
        return kerasFeature;
    }

    /**
     * Extracts the global feature and returns the Lucene Fields for the selected image.
     *
     * @param imagePath         is the selected imagePath.
     * @param feature           the features.
     * @return Lucene Fields.
     */
    private Field[] getGlobalDescriptorFields(String imagePath, KerasFeature feature) {
        Field[] result;
//        if (hashingEnabled) result = new Field[2];
//        else result = new Field[1];
        Field hash = null;
        Field vector = null;

        KerasFeature kerasFeature = extractKerasFeature(imagePath, feature);

        if (!useDocValues) {
            // TODO: Stored field is compressed and upon search decompression takes a lot of time (> 50% with a small index with 50k images). Find something else ...
            vector = new StoredField(feature.getFieldName(), new BytesRef(kerasFeature.getByteArrayRepresentation()));
        } else {
            // Alternative: The DocValues field. It's extremely fast to read, but it's all in RAM most likely.
            vector = new BinaryDocValuesField(feature.getFieldName(), new BytesRef(kerasFeature.getByteArrayRepresentation()));
        }


        // if BitSampling is an issue we add a field with the given hashFunctionsFileName and the suffix "hash":
        if (hashingEnabled) {
            // TODO: check eventually if there is a more compressed string version of the integers. i.e. the hex string
            if (kerasFeature.getFeatureVector().length <= 3100) {
                int[] hashes;
                if (hashingMode == GlobalDocumentBuilder.HashingMode.BitSampling) {
                    hashes = BitSampling.generateHashes(kerasFeature.getFeatureVector());
                    hash = new TextField(kerasFeature.getFieldName()+DocumentBuilder.HASH_FIELD_SUFFIX, SerializationUtils.arrayToString(hashes), Field.Store.YES);
                } else if (hashingMode == GlobalDocumentBuilder.HashingMode.LSH) {
                    hashes = LocalitySensitiveHashing.generateHashes(kerasFeature.getFeatureVector());
                    hash = new TextField(kerasFeature.getFieldName()+DocumentBuilder.HASH_FIELD_SUFFIX, SerializationUtils.arrayToString(hashes), Field.Store.YES);
                } else if (hashingMode == GlobalDocumentBuilder.HashingMode.MetricSpaces) {
                    if (MetricSpaces.supportsFeature(kerasFeature)) {
                        // the name of the field is set at "addExtractor" time.
                        hash = new TextField(kerasFeature.getFieldName()+DocumentBuilder.HASH_FIELD_SUFFIX, MetricSpaces.generateHashString(kerasFeature), Field.Store.YES);
                    }
                }
            } else
                System.err.println("Could not create hashes, feature vector too long: " + kerasFeature.getFeatureVector().length + " (" + kerasFeature.getClass().getName() + ")");
        }
        if (hash != null) result = new Field[]{vector, hash};
        else result = new Field[]{vector};
        return result;
    }

    @Override
    public Field[] createDescriptorFields(String imagePath) {
        return new Field[0];
    }

    @Override
    public Document createDocument(String imagePath, String identifier) throws FileNotFoundException {
        return null;
    }
}
