package keras;

import net.semanticmetadata.lire.imageanalysis.features.LireFeature;
import net.semanticmetadata.lire.utils.MetricsUtils;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import utils.KerasCSVReader;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

public class VGG16 implements KerasFeature{

    private final String featureName    = "VGG16";
    private final String fieldName      = "keras_vgg16";
    private double[] featureVector      = null;
    private String csvFilename          = null;

    public VGG16(){
    }

    public VGG16(String csvFilename){
        this.csvFilename = csvFilename;
    }
    @Override
    public void extract(String imageFilename) {
        if(csvFilename != null){
            // get featureVector from csv file
            KerasCSVReader reader = new KerasCSVReader(csvFilename,",");
            featureVector = reader.getValuesOfFile(imageFilename);
        }
        else{
            //get featureVector from python/keras model
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
        ByteBuffer buffer = ByteBuffer.allocate(featureVector.length * 8);
        for(double d : featureVector){
            buffer.putDouble(d);
        }
        return buffer.array();
    }

    @Override
    public void setByteArrayRepresentation(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        featureVector = new double[buffer.limit()/8];
        int i = 0;
        while(buffer.position() < buffer.limit()){
            featureVector[i++] = buffer.getDouble();
        }
    }

    @Override
    public void setByteArrayRepresentation(byte[] bytes, int i, int i1) {
        throw new NotImplementedException();
    }

    @Override
    public double getDistance(LireFeature lf) {
        if(!(lf instanceof KerasFeature)){
            throw new UnsupportedOperationException("Wrong descriptor!");
        }
        if(this.featureVector.length != lf.getFeatureVector().length){
            throw new UnsupportedOperationException("Lengths of featureVectors don't match");
        }
        return MetricsUtils.cosineCoefficient(this.featureVector,lf.getFeatureVector());
    }

    @Override
    public double[] getFeatureVector() {
        return this.featureVector;
    }
}