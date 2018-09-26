package keras.features;

import keras.features.KerasFeature;
import utils.KerasCSVReader;
import utils.Quantization;
import net.semanticmetadata.lire.imageanalysis.features.LireFeature;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

public class ResNet50_Short implements KerasFeature{

    private final String featureName    = "ResNet50_Short";
    private final String fieldName      = "ResNet50_Short";
    public static DistanceFunction USED_DISTANCE_FUN = DistanceFunction.DISTANCEFUNCTION_COSINE;
    public ResNet50_Short(){
    }
    public ResNet50_Short(String csvFilename){
        setCsvFilename(csvFilename);
    }

    private short[] featureVector      = null;
    public static KerasCSVReader reader = null;

    public static void setCsvFilename(String csvFilename) {
        if(csvFilename != null && (reader != null && !csvFilename.equals(reader.getFilepath()))) {
            // get featureVector from csv file
            reader = new KerasCSVReader(csvFilename, ",");
        }
    }

    @Override
    public void extract(String imageFilename) {
        featureVector = null;
        if(reader != null){
            // get featureVector from csv file
            featureVector = reader.getValuesOfFileShort(imageFilename);
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
        ByteBuffer buffer = ByteBuffer.allocate(featureVector.length * Short.BYTES);
        for(short d : featureVector){
            buffer.putShort(d);
        }
        return buffer.array();
    }
    @Override
    public void setByteArrayRepresentation(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        featureVector = new short[buffer.limit()/Short.BYTES];
        int i = 0;
        while(buffer.position() < buffer.limit()){
            featureVector[i++] = buffer.getShort();
        }
    }

    @Override
    public void setByteArrayRepresentation(byte[] bytes, int offset, int length) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes,offset,length);

        featureVector = new short[buffer.limit()/Short.BYTES];
        int i = 0;
        while(buffer.position() < buffer.limit()){
            featureVector[i++] = buffer.getShort();
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
        return KerasFeature.getDistance(this.getFeatureVector(),lf.getFeatureVector(), USED_DISTANCE_FUN );
    }

    @Override
    public double[] getFeatureVector() {
        return Quantization.castToDoubleArray(this.featureVector);
    }

    @Override
    public void extract(BufferedImage bufferedImage) {

    }
}
