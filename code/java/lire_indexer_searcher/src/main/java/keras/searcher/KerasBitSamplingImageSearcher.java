package keras.searcher;

import keras.documentbuilder.KerasDocumentBuilder;
import keras.features.KerasFeature;
import net.semanticmetadata.lire.builders.DocumentBuilder;
import net.semanticmetadata.lire.imageanalysis.features.GlobalFeature;
import net.semanticmetadata.lire.indexers.hashing.BitSampling;
import net.semanticmetadata.lire.searchers.*;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.FieldInvertState;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.util.BytesRef;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.TreeSet;

public class KerasBitSamplingImageSearcher implements KerasSearcher{
    private int maxResultsHashBased = 1000;
    private int maximumHits = 100;
    private String featureFieldName = null;
    private GlobalFeature feature = null;
    private String hashesFieldName = null;
    private boolean partialHashes = false;
    /**
     * Creates a new searcher for BitSampling based hashes.
     *
     * @param maximumHits      how many hits the searcher shall return.
     * @param featureFieldName the field hashFunctionsFileName of the feature.
     * @param hashesFieldName  the field hashFunctionsFileName of the hashes.
     * @param feature          an instance of the feature.
     */
    public KerasBitSamplingImageSearcher(int maximumHits, String featureFieldName, String hashesFieldName, GlobalFeature feature) {
        this.maximumHits = maximumHits;
        this.featureFieldName = featureFieldName;
        this.hashesFieldName = hashesFieldName;
        this.feature = feature;
        try {
            BitSampling.readHashFunctions(new FileInputStream(KerasDocumentBuilder.hashFilePath));
        } catch (IOException e) {
            System.err.println("Error reading hash functions from default location.");
            e.printStackTrace();
        }
    }

    /**
     * Creates a new searcher for BitSampling based hashes. The field names are inferred from the entries in //
     *
     * @param maximumHits how many hits the searcher shall return.
     * @param feature     an instance of the feature.
     */
    public KerasBitSamplingImageSearcher(int maximumHits, GlobalFeature feature) {
        this.maximumHits = maximumHits;
        this.featureFieldName = feature.getFieldName();
        this.hashesFieldName = featureFieldName + DocumentBuilder.HASH_FIELD_SUFFIX;
        this.feature = feature;
        try {
            if(!(new File(KerasDocumentBuilder.hashFilePath).exists())){
                BitSampling.dimensions = KerasDocumentBuilder.maxDimensions;
                BitSampling.generateHashFunctions(KerasDocumentBuilder.hashFilePath);
            }
            BitSampling.readHashFunctions(new FileInputStream(KerasDocumentBuilder.hashFilePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
//        try {
//            BitSampling.readHashFunctions(new FileInputStream(KerasDocumentBuilder.hashFilePath));
//        } catch (IOException e) {
//            System.err.println("Error reading hash functions from default location.");
//            e.printStackTrace();
//        }
    }

    /**
     * Creates a new searcher for BitSampling based hashes. The field names are inferred from the entries in //
     *
     * @param maximumHits how many hits the searcher shall return.
     * @param feature     an instance of the feature.
     * @param useFastSearch if true it only uses a random sample of hashes for the query and speeds up the search significantly.
     */
    public KerasBitSamplingImageSearcher(int maximumHits, GlobalFeature feature, boolean useFastSearch) {
        this.maximumHits = maximumHits;
        featureFieldName = feature.getFieldName();
        hashesFieldName = featureFieldName + DocumentBuilder.HASH_FIELD_SUFFIX;
        this.feature = feature;
        partialHashes = useFastSearch;
//        try {
//            BitSampling.readHashFunctions(new FileInputStream(KerasDocumentBuilder.hashFilePath));
//        } catch (IOException e) {
//            System.err.println("Error reading hash functions from default location.");
//            e.printStackTrace();
//        }
    }

    /**
     * Creates a new searcher for BitSampling based hashes. The field names are inferred from the entries in
     *
     * @param maximumHits      how many hits the searcher shall return.
     * @param feature          an instance of the feature.
     * @param numHashedResults the number of candidate results retrieved from the index before re-ranking.
     */
    public KerasBitSamplingImageSearcher(int maximumHits, GlobalFeature feature, int numHashedResults) {
        this.maximumHits = maximumHits;
        this.featureFieldName = feature.getFieldName();
        this.hashesFieldName = featureFieldName + DocumentBuilder.HASH_FIELD_SUFFIX;
        this.feature = feature;
        maxResultsHashBased = numHashedResults;
//        try {
//            BitSampling.readHashFunctions(new FileInputStream(KerasDocumentBuilder.hashFilePath));
//        } catch (IOException e) {
//            System.err.println("Error reading hash functions from default location.");
//            e.printStackTrace();
//        }
    }

    public KerasBitSamplingImageSearcher(int maximumHits, String featureFieldName, String hashesFieldName, GlobalFeature feature, int numHashedResults) {
        this.maximumHits = maximumHits;
        this.featureFieldName = featureFieldName;
        this.hashesFieldName = hashesFieldName;
        this.feature = feature;
        maxResultsHashBased = numHashedResults;
//        try {
//            BitSampling.readHashFunctions(new FileInputStream(KerasDocumentBuilder.hashFilePath));
//        } catch (IOException e) {
//            System.err.println("Error reading hash functions from default location.");
//            e.printStackTrace();
//        }
    }

    public KerasBitSamplingImageSearcher(int maximumHits, String featureFieldName, String hashesFieldName, GlobalFeature feature, InputStream hashes) {
        this.maximumHits = maximumHits;
        this.featureFieldName = featureFieldName;
        this.hashesFieldName = hashesFieldName;
        this.feature = feature;
//        try {
//            BitSampling.readHashFunctions(hashes);
//            hashes.close();
//        } catch (IOException e) {
//            System.err.println("Error reading has functions from given input stream.");
//            e.printStackTrace();
//        }
    }

    public KerasBitSamplingImageSearcher(int maximumHits, String featureFieldName, String hashesFieldName, GlobalFeature feature, InputStream hashes, int numHashedResults) {
        this.maximumHits = maximumHits;
        this.featureFieldName = featureFieldName;
        this.hashesFieldName = hashesFieldName;
        this.feature = feature;
        maxResultsHashBased = numHashedResults;
//        try {
//            BitSampling.readHashFunctions(hashes);
//            hashes.close();
//        } catch (IOException e) {
//            System.err.println("Error reading has functions from given input stream.");
//            e.printStackTrace();
//        }
    }
    @Override
    public ImageSearchHits search(String image, IndexReader reader) {
        try {
            GlobalFeature queryFeature = feature.getClass().newInstance();
            if(queryFeature instanceof KerasFeature){

                ((KerasFeature)queryFeature).extract(image);
            }
            else{
                queryFeature.extract(ImageIO.read(new File(image)));
            }
            BitSampling.dimensions = queryFeature.getFeatureVector().length;
            int[] ints = BitSampling.generateHashes(queryFeature.getFeatureVector());

            String[] hashes = new String[ints.length];
            for (int i = 0; i < ints.length; i++) {
                hashes[i] = Integer.toString(ints[i]);
            }
            return search(hashes, queryFeature, reader);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public ImageSearchHits search(Document doc, IndexReader reader) throws IOException {
        try {
            GlobalFeature queryFeature = feature.getClass().newInstance();
            queryFeature.setByteArrayRepresentation(doc.getBinaryValue(featureFieldName).bytes,
                    doc.getBinaryValue(featureFieldName).offset,
                    doc.getBinaryValue(featureFieldName).length);
            return search(doc.getValues(hashesFieldName)[0].split(" "), queryFeature, reader);
//            return search(doc.getValuesDouble(hashesFieldName + "_q")[0].split(" "), queryFeature, reader);  // just for debug if a query feature is stored in the index.
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private ImageSearchHits search(String[] hashes, GlobalFeature queryFeature, IndexReader reader) throws IOException {
        // first search by text:
        IndexSearcher searcher = new IndexSearcher(reader);
        searcher.setSimilarity(new KerasBitSamplingImageSearcher.BaseSimilarity());
        BooleanQuery query = null;
        BooleanQuery.Builder builder = new BooleanQuery.Builder();
        for (int i = 0; i < hashes.length; i++) {
            // be aware that the hashFunctionsFileName of the field must match the one you put the hashes in before.
            if (partialHashes) {
                if (Math.random() < 0.5) {
                    builder.add(new BooleanClause(new TermQuery(new Term(hashesFieldName, hashes[i] + "")), BooleanClause.Occur.SHOULD));
                }
            } else {
                builder.add(new BooleanClause(new TermQuery(new Term(hashesFieldName, hashes[i] + "")), BooleanClause.Occur.SHOULD));
            }
        }
        query = builder.build();
        TopDocs docs = searcher.search(query, maxResultsHashBased);
//        System.out.println(docs.totalHits);
        // then re-rank
        TreeSet<SimpleResult> resultScoreDocs = new TreeSet<SimpleResult>();
        double maxDistance = -1d;
        double tmpScore;
        for (int i = 0; i < docs.scoreDocs.length; i++) {
            BytesRef binaryValue = reader.document(docs.scoreDocs[i].doc).getBinaryValue(featureFieldName);
            feature.setByteArrayRepresentation(binaryValue.bytes,
                    binaryValue.offset,
                    binaryValue.length);
            tmpScore = queryFeature.getDistance(feature);
            assert (tmpScore >= 0);
            if (resultScoreDocs.size() < maximumHits) {
                resultScoreDocs.add(new SimpleResult(tmpScore, docs.scoreDocs[i].doc));
                maxDistance = Math.max(maxDistance, tmpScore);
            } else if (tmpScore < maxDistance) {
                // if it is nearer to the sample than at least one of the current set:
                // remove the last one ...
                resultScoreDocs.remove(resultScoreDocs.last());
                // add the new one ...
                resultScoreDocs.add(new SimpleResult(tmpScore, docs.scoreDocs[i].doc));
                // and set our new distance border ...
                maxDistance = resultScoreDocs.last().getDistance();
            }
        }
        assert (resultScoreDocs.size() <= maximumHits);
        return new SimpleImageSearchHits(resultScoreDocs, maxDistance);
    }

    public ImageDuplicates findDuplicates(IndexReader reader) throws IOException {
        throw new UnsupportedOperationException("not implemented.");
    }

    class BaseSimilarity extends ClassicSimilarity {
        public float tf(float freq) {
            return freq;
        }

        public float idf(long docFreq, long numDocs) {
            return 1;
        }

        public float coord(int overlap, int maxOverlap) {
            return 1;
        }

        public float queryNorm(float sumOfSquaredWeights) {
            return 1;
        }

        public float sloppyFreq(int distance) {
            return 1;
        }

        public float lengthNorm(FieldInvertState state) {
            return 1;
        }
    }
}
