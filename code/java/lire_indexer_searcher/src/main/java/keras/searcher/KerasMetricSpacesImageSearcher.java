package keras.searcher;


import keras.features.KerasFeature;
import net.semanticmetadata.lire.builders.GlobalDocumentBuilder;
import net.semanticmetadata.lire.imageanalysis.features.GlobalFeature;
import net.semanticmetadata.lire.indexers.hashing.MetricSpaces;
import net.semanticmetadata.lire.searchers.*;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.ClassicSimilarity;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.TreeSet;

public class KerasMetricSpacesImageSearcher implements KerasSearcher{
    private MetricSpaces.Parameters metricSpacesParameters;
    private int maxResultsHashBased = 1000;
    private int maximumHits;
    private String featureFieldName = null;
    private GlobalFeature feature = null;
    private String hashesFieldName = null;
    private int numHashesUsedForQuery = 25;
    private boolean useDocValues = false;
    private BinaryDocValues docValues = null;
    private IndexSearcher searcher = null;

    public KerasMetricSpacesImageSearcher(int maximumHits, File referencePointFile) throws IllegalAccessException, InstantiationException, ClassNotFoundException {
        this.maximumHits = maximumHits;

        try {
            this.metricSpacesParameters = MetricSpaces.loadReferencePoints(new FileInputStream(referencePointFile));
            this.feature = (GlobalFeature) this.metricSpacesParameters.featureClass.newInstance();
            this.featureFieldName = this.feature.getFieldName();
            this.hashesFieldName = this.featureFieldName + GlobalDocumentBuilder.HASH_FIELD_SUFFIX;
        } catch (IOException var4) {
            System.err.println("Error reading hash functions from default location.");
            var4.printStackTrace();
        }

    }

    public KerasMetricSpacesImageSearcher(int maximumHits, File referencePointFile, boolean useDocValues, IndexReader reader) throws IllegalAccessException, InstantiationException, ClassNotFoundException {
        this.maximumHits = maximumHits;
        this.useDocValues = useDocValues;

        try {
            this.metricSpacesParameters = MetricSpaces.loadReferencePoints(new FileInputStream(referencePointFile));
            this.feature = (KerasFeature)this.metricSpacesParameters.featureClass.newInstance();
            this.featureFieldName = this.feature.getFieldName();
            this.hashesFieldName = this.featureFieldName +  GlobalDocumentBuilder.HASH_FIELD_SUFFIX;
            if (useDocValues) {
                this.docValues = MultiDocValues.getBinaryValues(reader, this.featureFieldName);
                this.searcher = new IndexSearcher(reader);
            }
        } catch (IOException var6) {
            System.err.println("Error reading hash functions from default location.");
            var6.printStackTrace();
        }

    }

    public KerasMetricSpacesImageSearcher(int maximumHits, InputStream referencePoints, int numHashedResults) throws IllegalAccessException, InstantiationException, ClassNotFoundException {
        this.maximumHits = maximumHits;
        this.maxResultsHashBased = numHashedResults;

        try {
            this.metricSpacesParameters = MetricSpaces.loadReferencePoints(referencePoints);
            this.feature = (GlobalFeature)this.metricSpacesParameters.featureClass.newInstance();
            this.featureFieldName = this.feature.getFieldName();
            this.hashesFieldName = this.featureFieldName + "_hash";
        } catch (IOException var5) {
            System.err.println("Error reading hash functions from default location.");
            var5.printStackTrace();
        }

    }

    public KerasMetricSpacesImageSearcher(int maximumHits, InputStream referencePoints, int numHashedResults, boolean useDocValues, IndexReader reader) throws IllegalAccessException, InstantiationException, ClassNotFoundException {
        this.maximumHits = maximumHits;
        this.maxResultsHashBased = numHashedResults;
        this.useDocValues = useDocValues;

        try {
            this.metricSpacesParameters = MetricSpaces.loadReferencePoints(referencePoints);
            this.feature = (GlobalFeature)this.metricSpacesParameters.featureClass.newInstance();
            this.featureFieldName = this.feature.getFieldName();
            this.hashesFieldName = this.featureFieldName + GlobalDocumentBuilder.HASH_FIELD_SUFFIX;
            if (useDocValues) {
                this.docValues = MultiDocValues.getBinaryValues(reader, this.featureFieldName);
                this.searcher = new IndexSearcher(reader);
            }
        } catch (IOException var7) {
            System.err.println("Error reading hash functions from default location.");
            var7.printStackTrace();
        }

    }
    @Override
    public ImageSearchHits search(String image, IndexReader reader)  {
        try {
            GlobalFeature queryFeature = this.feature.getClass().newInstance();
            if(queryFeature instanceof KerasFeature) {
                ((KerasFeature)queryFeature).extract(image);
            }
            else{
                queryFeature.extract(ImageIO.read(new File(image)));
            }
            String query = MetricSpaces.generateBoostedQuery(queryFeature, this.numHashesUsedForQuery);
            return this.search(query, queryFeature, reader);
        } catch (Exception var5) {
            var5.printStackTrace();
            return null;
        }
    }

    public ImageSearchHits search(Document doc, IndexReader reader) throws IOException {
        GlobalFeature queryFeature;

        try {
            queryFeature = this.feature.getClass().newInstance();
        } catch (IllegalAccessException | InstantiationException var6) {
            var6.printStackTrace();
            return null;
        }

        if (this.useDocValues) {
            TopDocs topDocs = this.searcher.search(new TermQuery(new Term("ImageIdentifier", doc.get("ImageIdentifier"))), 1);
            if (topDocs.totalHits > 0) {
                int docID = topDocs.scoreDocs[0].doc;
                queryFeature.setByteArrayRepresentation(this.docValues.get(docID).bytes, this.docValues.get(docID).offset, this.docValues.get(docID).length);
                return this.search(MetricSpaces.generateBoostedQuery(queryFeature, this.numHashesUsedForQuery), queryFeature, this.searcher.getIndexReader());
            } else {
                return null;
            }
        } else {
            queryFeature.setByteArrayRepresentation(doc.getBinaryValue(this.featureFieldName).bytes, doc.getBinaryValue(this.featureFieldName).offset, doc.getBinaryValue(this.featureFieldName).length);
            return this.search(MetricSpaces.generateBoostedQuery(queryFeature, this.numHashesUsedForQuery), queryFeature, reader);
        }
    }

    private ImageSearchHits search(String hashes, GlobalFeature queryFeature, IndexReader reader) throws IOException {
        return this.useDocValues ? this.searchWithDocValues(hashes, queryFeature, reader) : this.searchWithField(hashes, queryFeature, reader);
    }

    private ImageSearchHits searchWithField(String hashes, GlobalFeature queryFeature, IndexReader reader) throws IOException {
        IndexSearcher searcher = new IndexSearcher(reader);
        searcher.setSimilarity(new KerasMetricSpacesImageSearcher.BaseSimilarity());
        QueryParser qp = new QueryParser(this.hashesFieldName, new WhitespaceAnalyzer());
        Query query = null;

        try {
            query = qp.parse(hashes);
        } catch (ParseException var14) {
            var14.printStackTrace();
        }

        if (query == null) {
            return null;
        } else {
            TopDocs docs = searcher.search(query, this.maxResultsHashBased);
            TreeSet<SimpleResult> resultScoreDocs = new TreeSet<>();
            double maxDistance = -1.0D;

            for(int i = 0; i < docs.scoreDocs.length; ++i) {
                this.feature.setByteArrayRepresentation(reader.document(docs.scoreDocs[i].doc).getBinaryValue(this.featureFieldName).bytes, reader.document(docs.scoreDocs[i].doc).getBinaryValue(this.featureFieldName).offset, reader.document(docs.scoreDocs[i].doc).getBinaryValue(this.featureFieldName).length);
                double tmpScore = queryFeature.getDistance(this.feature);

                assert tmpScore >= 0.0D;

                if (resultScoreDocs.size() < this.maximumHits) {
                    resultScoreDocs.add(new SimpleResult(tmpScore, docs.scoreDocs[i].doc));
                    maxDistance = Math.max(maxDistance, tmpScore);
                } else if (tmpScore < maxDistance) {
                    resultScoreDocs.remove(resultScoreDocs.last());
                    resultScoreDocs.add(new SimpleResult(tmpScore, docs.scoreDocs[i].doc));
                    maxDistance = resultScoreDocs.last().getDistance();
                }
            }

            assert resultScoreDocs.size() <= this.maximumHits;

            return new SimpleImageSearchHits(resultScoreDocs, maxDistance);
        }
    }

    private ImageSearchHits searchWithDocValues(String hashes, GlobalFeature queryFeature, IndexReader reader) throws IOException {
        IndexSearcher searcher = new IndexSearcher(reader);
        searcher.setSimilarity(new KerasMetricSpacesImageSearcher.BaseSimilarity());
        QueryParser qp = new QueryParser(this.hashesFieldName, new WhitespaceAnalyzer());
        Query query = null;

        try {
            query = qp.parse(hashes);
        } catch (ParseException var14) {
            var14.printStackTrace();
        }

        if (query == null) {
            return null;
        } else {
            TopDocs docs = searcher.search(query, this.maxResultsHashBased);
            TreeSet<SimpleResult> resultScoreDocs = new TreeSet<>();
            double maxDistance = -1.0D;

            for(int i = 0; i < docs.scoreDocs.length; ++i) {
                this.feature.setByteArrayRepresentation(this.docValues.get(docs.scoreDocs[i].doc).bytes, this.docValues.get(docs.scoreDocs[i].doc).offset, this.docValues.get(docs.scoreDocs[i].doc).length);
                double tmpScore = queryFeature.getDistance(this.feature);

                assert tmpScore >= 0.0D;

                if (resultScoreDocs.size() < this.maximumHits) {
                    resultScoreDocs.add(new SimpleResult(tmpScore, docs.scoreDocs[i].doc));
                    maxDistance = Math.max(maxDistance, tmpScore);
                } else if (tmpScore < maxDistance) {
                    resultScoreDocs.remove(resultScoreDocs.last());
                    resultScoreDocs.add(new SimpleResult(tmpScore, docs.scoreDocs[i].doc));
                    maxDistance = resultScoreDocs.last().getDistance();
                }
            }

            assert resultScoreDocs.size() <= this.maximumHits;

            return new SimpleImageSearchHits(resultScoreDocs, maxDistance);
        }
    }

    public ImageDuplicates findDuplicates(IndexReader reader) {
        throw new UnsupportedOperationException("not implemented.");
    }

    public int getNumHashesUsedForQuery() {
        return this.numHashesUsedForQuery;
    }

    public void setNumHashesUsedForQuery(int numHashesUsedForQuery) {
        this.numHashesUsedForQuery = numHashesUsedForQuery;
    }

    public int getNumberOfReferencePoints() {
        return this.metricSpacesParameters.numberOfReferencePoints;
    }

    public int getLengthOfPostingList() {
        return this.metricSpacesParameters.lengthOfPostingList;
    }

    class BaseSimilarity extends ClassicSimilarity {
        BaseSimilarity() {
        }

        public float tf(float freq) {
            return freq;
        }

        public float idf(long docFreq, long numDocs) {
            return 1.0F;
        }

        public float coord(int overlap, int maxOverlap) {
            return 1.0F;
        }

        public float queryNorm(float sumOfSquaredWeights) {
            return 1.0F;
        }

        public float sloppyFreq(int distance) {
            return 1.0F;
        }

        public float lengthNorm(FieldInvertState state) {
            return 1.0F;
        }
    }

}
