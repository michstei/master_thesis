package keras.features.quantized;

import keras.features.KerasFeature;
import utils.KerasCSVReader;
import utils.Quantization;
import net.semanticmetadata.lire.imageanalysis.features.LireFeature;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

public class IncResNetV2_Int implements KerasFeature{

    private final String featureName    = "IncResNetV2_Int";
    private final String fieldName      = "IncResNetV2_Int";
    public static DistanceFunction USED_DISTANCE_FUN = DistanceFunction.DISTANCEFUNCTION_COSINE;
    public IncResNetV2_Int(){
    }
    public IncResNetV2_Int(String csvFilename){
        setCsvFilename(csvFilename);
    }

    private int[] featureVector      = null;
    public static KerasCSVReader reader = null;

    public static void setCsvFilename(String csvFilename) {
        if(csvFilename != null) {
            // get featureVector from csv file
            reader = new KerasCSVReader(csvFilename, ",");
        }
    }

    @Override
    public void extract(String imageFilename) {
        featureVector = null;
        if(reader != null){
            // get featureVector from csv file
            featureVector = reader.getValuesOfFileInt(imageFilename);
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
        ByteBuffer buffer = ByteBuffer.allocate(featureVector.length * Integer.BYTES);
        for(int d : featureVector){
            buffer.putInt(d);
        }
        return buffer.array();
    }
    @Override
    public void setByteArrayRepresentation(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        featureVector = new int[buffer.limit()/Integer.BYTES];
        int i = 0;
        while(buffer.position() < buffer.limit()){
            featureVector[i++] = buffer.getInt();
        }
    }

    @Override
    public void setByteArrayRepresentation(byte[] bytes, int offset, int length) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes,offset,length);

        featureVector = new int[buffer.limit()/Integer.BYTES];
        int i = 0;
        while(buffer.position() < buffer.limit()){
            featureVector[i++] = buffer.getInt();
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
