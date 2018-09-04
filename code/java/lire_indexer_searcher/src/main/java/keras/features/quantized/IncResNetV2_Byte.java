package keras.features.quantized;

import keras.features.KerasFeature;
import keras.utils.KerasCSVReader;
import keras.utils.Quantization;
import net.semanticmetadata.lire.imageanalysis.features.LireFeature;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

public class IncResNetV2_Byte implements KerasFeature{

    private final String featureName    = "IncResNetV2_Byte";
    private final String fieldName      = "IncResNetV2_Byte";
    public static DistanceFunction USED_DISTANCE_FUN = DistanceFunction.DISTANCEFUNCTION_COSINE;
    public IncResNetV2_Byte(){
    }
    public IncResNetV2_Byte(String csvFilename){
        setCsvFilename(csvFilename);
    }

    private byte[] featureVector      = null;
    private static KerasCSVReader reader = null;

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
            featureVector = reader.getValuesOfFileByte(imageFilename);
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

        return featureVector;
    }

    @Override
    public void setByteArrayRepresentation(byte[] bytes) {
        featureVector = new byte[bytes.length];
        for(int i = 0; i < bytes.length; i++){
            featureVector[i] = bytes[i];
        }
    }

    @Override
    public void setByteArrayRepresentation(byte[] bytes, int offset, int length) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes,offset,length);

        featureVector = new byte[buffer.limit()];
        int i = 0;
        while(buffer.position() < buffer.limit()){
            featureVector[i++] = buffer.get();
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
