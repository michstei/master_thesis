package keras.features;

import net.semanticmetadata.lire.imageanalysis.features.LireFeature;
import net.semanticmetadata.lire.utils.MetricsUtils;
import keras.utils.KerasCSVReader;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

public class DenseNet201 implements KerasFeature{

    private final String featureName    = "DenseNet201";
    private final String fieldName      = "DenseNet201";
    public static DistanceFunction USED_DISTANCE_FUN = DistanceFunction.DISTANCE_COSINE;
    private double[] featureVector      = null;
    private static String csvFilename          = null;
    private static KerasCSVReader reader = null;
    public static void setCsvFilename(String csvFilename) {
        DenseNet201.csvFilename = csvFilename;
        if(csvFilename != null) {
            // get featureVector from csv file
            reader = new KerasCSVReader(csvFilename, ",");
        }
    }

    public DenseNet201(){
    }
    public DenseNet201(String csvFilename){
        setCsvFilename(csvFilename);
    }

    @Override
    public void extract(String imageFilename) {
        featureVector = null;
        if(reader != null){
            // get featureVector from csv file
            featureVector = reader.getValuesOfFile(imageFilename);
        }
        if(featureVector == null){
            //TODO: get featureVector from somewhere else (python?)
        }
    }


    @Override
    public String getFeatureName() {
        return featureName;
    }

    @Override
    public String getFieldName() {
        return fieldName;
    }

    @Override
    public byte[] getByteArrayRepresentation() {
        ByteBuffer buffer = ByteBuffer.allocate(featureVector.length * Double.BYTES);
        for(double d : featureVector){
            buffer.putDouble(d);
        }
        return buffer.array();
    }

    @Override
    public void setByteArrayRepresentation(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        featureVector = new double[buffer.limit()/Double.BYTES];
        int i = 0;
        while(buffer.position() < buffer.limit()){
            featureVector[i++] = buffer.getDouble();
        }
    }

    @Override
    public void setByteArrayRepresentation(byte[] bytes, int offset, int length) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes,offset,length);
        featureVector = new double[buffer.limit()/Double.BYTES];
        int i = 0;
        while(buffer.position() < buffer.limit()){
            featureVector[i++] = buffer.getDouble();
        }
    }


    @Override
    public double getDistance(LireFeature lf) {
        if(!(lf instanceof KerasFeature)){
            throw new UnsupportedOperationException("Wrong descriptor!");
        }
        if(this.featureVector.length != lf.getFeatureVector().length){
            throw new UnsupportedOperationException("Lengths of featureVectors don't match");
        }
        return KerasFeature.getDistance(lf, USED_DISTANCE_FUN, this.featureVector);
    }

    @Override
    public double[] getFeatureVector() {
        return this.featureVector;
    }

    @Override
    public void extract(BufferedImage bufferedImage) {

    }
}
