package keras.features;

import utils.KerasCSVReader;
import utils.Quantization;
import net.semanticmetadata.lire.imageanalysis.features.LireFeature;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;


public class VGG16_Byte implements KerasFeature{

    public static DistanceFunction USED_DISTANCE_FUN = DistanceFunction.DISTANCEFUNCTION_COSINE;
    public VGG16_Byte(){
    }

    public VGG16_Byte(String csvFilename){
        setCsvFilename(csvFilename);
    }

    private byte[] featureVector      = null;
    public static KerasCSVReader reader = null;

    public static void setCsvFilename(String csvFilename) {
        reader = KerasFeature.setCsvFilename(csvFilename,reader);
    }

    @Override
    public void extract(String imageFilename) {
        featureVector = null;
        if(reader != null){
            // get featureVector from csv file
            featureVector = reader.getValuesOfFileByte(imageFilename);
        }
        //TODO: get featureVector from somewhere else (python?)
    }


    @Override
    public String getFeatureName() {
        return "VGG16_Byte";
    }

    @Override
    public String getFieldName() {
        return "VGG16_Byte";
    }


    @Override
    public byte[] getByteArrayRepresentation() {

        return featureVector;
    }

    @Override
    public void setByteArrayRepresentation(byte[] bytes) {
        featureVector = new byte[bytes.length];
        System.arraycopy(bytes, 0, featureVector, 0, bytes.length);
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
