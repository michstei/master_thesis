package keras.features;

import utils.KerasCSVReader;
import utils.Quantization;
import net.semanticmetadata.lire.imageanalysis.features.LireFeature;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;


public class VGG16_Long implements KerasFeature{

    public static DistanceFunction USED_DISTANCE_FUN = DistanceFunction.DISTANCEFUNCTION_COSINE;
    public VGG16_Long(){
    }

    public VGG16_Long(String csvFilename){
        setCsvFilename(csvFilename);
    }

    private long[] featureVector      = null;
    public static KerasCSVReader reader = null;

    public static void setCsvFilename(String csvFilename) {
        reader = KerasFeature.setCsvFilename(csvFilename,reader);
    }

    @Override
    public void extract(String imageFilename) {
        featureVector = null;
        if(reader != null){
            // get featureVector from csv file
            featureVector = reader.getValuesOfFileLong(imageFilename);
        }
        //TODO: get featureVector from somewhere else (python?)
    }



    @Override
    public String getFeatureName() {
        return "VGG16_Long";
    }

    @Override
    public String getFieldName() {
        return "VGG16_Long";
    }



    @Override
    public byte[] getByteArrayRepresentation() {
        ByteBuffer buffer = ByteBuffer.allocate(featureVector.length * Long.BYTES);
        for(long d : featureVector){
            buffer.putLong(d);
        }
        return buffer.array();
    }

    @Override
    public void setByteArrayRepresentation(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        featureVector = new long[buffer.limit()/Long.BYTES];
        int i = 0;
        while(buffer.position() < buffer.limit()){
            featureVector[i++] = buffer.getLong();
        }
    }

    @Override
    public void setByteArrayRepresentation(byte[] bytes, int offset, int length) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes,offset,length);

        featureVector = new long[buffer.limit()/Long.BYTES];
        int i = 0;
        while(buffer.position() < buffer.limit()){
            featureVector[i++] = buffer.getLong();
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
