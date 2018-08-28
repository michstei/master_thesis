package keras.documentbuilder;

import keras.features.KerasFeature;
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

import javax.imageio.ImageIO;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class KerasDocumentBuilderImpl implements KerasDocumentBuilder {

    private boolean useDocValues = false;
    public final int maxDimensions = 0;

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

    public KerasDocumentBuilderImpl(Class<? extends KerasFeature> globalFeatureClass) {
        addExtractor(globalFeatureClass);
    }

    public KerasDocumentBuilderImpl(Class<? extends KerasFeature> globalFeatureClass, boolean hashing) {
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
    public KerasDocumentBuilderImpl(Class<? extends KerasFeature> globalFeatureClass, boolean hashing, boolean useDocValues) {
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
     * @param kerasFeatureClass
     */
    public void addExtractor(Class<? extends GlobalFeature> kerasFeatureClass) {
        addExtractor(new ExtractorItem(kerasFeatureClass));
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
                if(new File(hashFilePath).exists())
                    new File(hashFilePath).delete();
                BitSampling.dimensions = KerasDocumentBuilder.maxDimensions;
                BitSampling.generateHashFunctions(hashFilePath);
                BitSampling.readHashFunctions(new FileInputStream(new File(hashFilePath)));
//            BitSampling.readHashFunctions();
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
     * Images are resized so as not to exceed the {@link DocumentBuilder#MAX_IMAGE_DIMENSION}, after that
     * the feature is extracted using the given globalFeature.
     *
     * @param imagePath         is the imagePath
     * @param kerasFeature selected global feature
     * @return the input globalFeature
     */
    public GlobalFeature extractGlobalFeature(String imagePath, GlobalFeature kerasFeature) throws IOException {
        assert (imagePath != null);

        kerasFeature.extract(ImageIO.read(new File(imagePath)));
        return kerasFeature;
    }
    /**
     * Extracts the global feature and returns the Lucene Fields for the selected image.
     *
     * @param imagePath         is the selected imagePath.
     * @param extractorItem           the features.
     * @return Lucene Fields.
     */
    private Field[] getGlobalDescriptorFields(String imagePath, ExtractorItem extractorItem) throws IOException {
        Field[] result;
//        if (hashingEnabled) result = new Field[2];
//        else result = new Field[1];
        Field hash = null;
        Field vector = null;

        GlobalFeature kerasFeature = extractorItem.getExtractorInstance() instanceof KerasFeature ? extractKerasFeature(imagePath, (KerasFeature) extractorItem.getExtractorInstance()) : extractGlobalFeature(imagePath,(GlobalFeature)extractorItem.getExtractorInstance());

        if (!useDocValues) {
            vector = new StoredField(extractorItems.get(extractorItem)[0], new BytesRef(kerasFeature.getByteArrayRepresentation()));
        } else {
            // Alternative: The DocValues field. It's extremely fast to read, but it's all in RAM most likely.
            vector = new BinaryDocValuesField(extractorItems.get(extractorItem)[0], new BytesRef(kerasFeature.getByteArrayRepresentation()));
        }


        // if BitSampling is an issue we add a field with the given hashFunctionsFileName and the suffix "hash":
        if (hashingEnabled) {
            if (kerasFeature.getFeatureVector().length <= 3100) {
                int[] hashes;
                if (hashingMode == GlobalDocumentBuilder.HashingMode.BitSampling) {


                    hashes = BitSampling.generateHashes(kerasFeature.getFeatureVector());
                    hash = new TextField(extractorItems.get(extractorItem)[1], SerializationUtils.arrayToString(hashes), Field.Store.YES);
                } else if (hashingMode == GlobalDocumentBuilder.HashingMode.LSH) {
                    hashes = LocalitySensitiveHashing.generateHashes(kerasFeature.getFeatureVector());
                    hash = new TextField(extractorItems.get(extractorItem)[1], SerializationUtils.arrayToString(hashes), Field.Store.YES);
                } else if (hashingMode == GlobalDocumentBuilder.HashingMode.MetricSpaces) {
                    if (MetricSpaces.supportsFeature(kerasFeature)) {
                        // the name of the field is set at "addExtractor" time.
                        hash = new TextField(extractorItems.get(extractorItem)[1], MetricSpaces.generateHashString(kerasFeature), Field.Store.YES);
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
    public Field[] createDescriptorFields(String imagePath) throws IOException {
        docsCreated = true;
        LinkedList<Field> resultList = new LinkedList<Field>();
        Field[] fields;
        if (extractorItems.size() > 0) {
            for (Map.Entry<ExtractorItem, String[]> extractorItemEntry : extractorItems.entrySet()) {
                fields = getGlobalDescriptorFields(imagePath, extractorItemEntry.getKey());

                Collections.addAll(resultList, fields);
            }
        }

        return resultList.toArray(new Field[resultList.size()]);
    }

    @Override
    public Document createDocument(String imagePath, String identifier) throws IOException {
        Document doc = new Document();

        if (identifier != null) {
            doc.add(new StringField(DocumentBuilder.FIELD_NAME_IDENTIFIER, identifier, Field.Store.YES));
        }

        Field[] fields = createDescriptorFields(imagePath);
        for (Field field : fields) {
            doc.add(field);
        }

        return doc;
    }
}
