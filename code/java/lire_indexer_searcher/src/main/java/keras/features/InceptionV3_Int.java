package keras.features;

import utils.KerasCSVReader;
import utils.Quantization;
import net.semanticmetadata.lire.imageanalysis.features.LireFeature;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

public class InceptionV3_Int implements KerasFeature{

    public static DistanceFunction USED_DISTANCE_FUN = DistanceFunction.DISTANCEFUNCTION_COSINE;

    public InceptionV3_Int(){
    }

    public InceptionV3_Int(String csvFilename){
        setCsvFilename(csvFilename);
    }

    private int[] featureVector      = null;
    public static KerasCSVReader reader = null;

    public static void setCsvFilename(String csvFilename) {
        reader = KerasFeature.setCsvFilename(csvFilename,reader);
    }

    @Override
    public void extract(String imageFilename) {
        featureVector = null;
        if(reader != null){
            // get featureVector from csv file
            featureVector = reader.getValuesOfFileInt(imageFilename);
        }
        //TODO: get featureVector from somewhere else (python?)
    }


    @Override
    public String getFeatureName() {
        return "InceptionV3_Int";
    }

    @Override
    public String getFieldName() {
        return "InceptionV3_Int";
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
