package keras.features;

import utils.Quantization;
import net.semanticmetadata.lire.imageanalysis.features.LireFeature;
import utils.KerasCSVReader;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

public class IncResNetV2_Double implements KerasFeature {

    public static DistanceFunction USED_DISTANCE_FUN = DistanceFunction.DISTANCEFUNCTION_COSINE;
    public IncResNetV2_Double(){
    }
    public IncResNetV2_Double(String csvFilename){
        setCsvFilename(csvFilename);
    }

    private double[] featureVector      = null;
    public static KerasCSVReader reader = null;

    public static void setCsvFilename(String csvFilename) {
        reader = KerasFeature.setCsvFilename(csvFilename,reader);
    }
    //
    @Override
    public void extract(String imageFilename) {
        featureVector = null;
        if(reader != null){
            // get featureVector from csv file
            featureVector = reader.getValuesOfFileDouble(imageFilename);
        }
        //TODO: get featureVector from somewhere else (python?)
    }



    @Override
    public String getFeatureName() {
        return "IncResNetV2_Double";
    }

    @Override
    public String getFieldName() {
        return "IncResNetV2_Double";
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
