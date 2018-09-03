package keras.features;

import keras.utils.Quantization;
import net.semanticmetadata.lire.imageanalysis.features.LireFeature;
import keras.utils.KerasCSVReader;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

public class DenseNet169 implements KerasFeature{

    private final String featureName    = "DenseNet169";
    private final String fieldName      = "DenseNet169";
    public static DistanceFunction USED_DISTANCE_FUN = DistanceFunction.DISTANCEFUNCTION_COSINE;
    public DenseNet169(){
    }

    public DenseNet169(String csvFilename){
        setCsvFilename(csvFilename);
    }

    private long[] featureVector      = null;
    private static KerasCSVReader reader = null;

    public static void setCsvFilename(String csvFilename) {
        if(csvFilename != null) {
            // get featureVector from csv file
            reader = new KerasCSVReader(csvFilename, ",");
        }
    }
    //
//    @Override
//    public void extract(String imageFilename) {
//        featureVector = null;
//        if(reader != null){
//            // get featureVector from csv file
//            featureVector = reader.getValuesOfFileDouble(imageFilename);
//        }
//        if(featureVector == null){
//            //TODO: get featureVector from somewhere else (python?)
//        }
//    }
//    @Override
//    public void extract(String imageFilename) {
//        featureVector = null;
//        if(reader != null){
//            // get featureVector from csv file
//            featureVector = reader.getValuesOfFileFloat(imageFilename);
//        }
//        if(featureVector == null){
//            //TODO: get featureVector from somewhere else (python?)
//        }
//    }
    @Override
    public void extract(String imageFilename) {
        featureVector = null;
        if(reader != null){
            // get featureVector from csv file
            featureVector = reader.getValuesOfFileLong(imageFilename);
        }
        if(featureVector == null){
            //TODO: get featureVector from somewhere else (python?)
        }
    }
//    @Override
//    public void extract(String imageFilename) {
//        featureVector = null;
//        if(reader != null){
//            // get featureVector from csv file
//            featureVector = reader.getValuesOfFileInt(imageFilename);
//        }
//        if(featureVector == null){
//            //TODO: get featureVector from somewhere else (python?)
//        }
//    }
//    @Override
//    public void extract(String imageFilename) {
//        featureVector = null;
//        if(reader != null){
//            // get featureVector from csv file
//            featureVector = reader.getValuesOfFileShort(imageFilename);
//        }
//        if(featureVector == null){
//            //TODO: get featureVector from somewhere else (python?)
//        }
//    }
//    @Override
//    public void extract(String imageFilename) {
//        featureVector = null;
//        if(reader != null){
//            // get featureVector from csv file
//            featureVector = reader.getValuesOfFileByte(imageFilename);
//        }
//        if(featureVector == null){
//            //TODO: get featureVector from somewhere else (python?)
//        }
//    }


    @Override
    public String getFeatureName() {
        return featureName;
    }

    @Override
    public String getFieldName() {
        return fieldName;
    }

//    @Override
//    public byte[] getByteArrayRepresentation() {
//        ByteBuffer buffer = ByteBuffer.allocate(featureVector.length * Double.BYTES);
//        for(double d : featureVector){
//            buffer.putDouble(d);
//        }
//        return buffer.array();
//    }

//    @Override
//    public void setByteArrayRepresentation(byte[] bytes) {
//        ByteBuffer buffer = ByteBuffer.wrap(bytes);
//        featureVector = new double[buffer.limit()/Double.BYTES];
//        int i = 0;
//        while(buffer.position() < buffer.limit()){
//            featureVector[i++] = buffer.getDouble();
//        }
//    }
//
//    @Override
//    public void setByteArrayRepresentation(byte[] bytes, int offset, int length) {
//        ByteBuffer buffer = ByteBuffer.wrap(bytes,offset,length);
//
//        featureVector = new double[buffer.limit()/Double.BYTES];
//        int i = 0;
//        while(buffer.position() < buffer.limit()){
//            featureVector[i++] = buffer.getDouble();
//        }
//    }

//    @Override
//    public byte[] getByteArrayRepresentation() {
//        ByteBuffer buffer = ByteBuffer.allocate(featureVector.length * Float.BYTES);
//        for(float d : featureVector){
//            buffer.putFloat(d);
//        }
//        return buffer.array();
//    }
//
//
//    @Override
//    public void setByteArrayRepresentation(byte[] bytes) {
//        ByteBuffer buffer = ByteBuffer.wrap(bytes);
//        featureVector = new float[buffer.limit()/Float.BYTES];
//        int i = 0;
//        while(buffer.position() < buffer.limit()){
//            featureVector[i++] = buffer.getFloat();
//        }
//    }
//
//    @Override
//    public void setByteArrayRepresentation(byte[] bytes, int offset, int length) {
//        ByteBuffer buffer = ByteBuffer.wrap(bytes,offset,length);
//
//        featureVector = new float[buffer.limit()/Float.BYTES];
//        int i = 0;
//        while(buffer.position() < buffer.limit()){
//            featureVector[i++] = buffer.getFloat();
//        }
//    }

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

//    @Override
//    public byte[] getByteArrayRepresentation() {
//        ByteBuffer buffer = ByteBuffer.allocate(featureVector.length * Integer.BYTES);
//        for(int d : featureVector){
//            buffer.putInt(d);
//        }
//        return buffer.array();
//    }
//    @Override
//    public void setByteArrayRepresentation(byte[] bytes) {
//        ByteBuffer buffer = ByteBuffer.wrap(bytes);
//        featureVector = new int[buffer.limit()/Integer.BYTES];
//        int i = 0;
//        while(buffer.position() < buffer.limit()){
//            featureVector[i++] = buffer.getInt();
//        }
//    }
//
//    @Override
//    public void setByteArrayRepresentation(byte[] bytes, int offset, int length) {
//        ByteBuffer buffer = ByteBuffer.wrap(bytes,offset,length);
//
//        featureVector = new int[buffer.limit()/Integer.BYTES];
//        int i = 0;
//        while(buffer.position() < buffer.limit()){
//            featureVector[i++] = buffer.getInt();
//        }
//    }

//    @Override
//    public byte[] getByteArrayRepresentation() {
//        ByteBuffer buffer = ByteBuffer.allocate(featureVector.length * Short.BYTES);
//        for(short d : featureVector){
//            buffer.putShort(d);
//        }
//        return buffer.array();
//    }
//    @Override
//    public void setByteArrayRepresentation(byte[] bytes) {
//        ByteBuffer buffer = ByteBuffer.wrap(bytes);
//        featureVector = new short[buffer.limit()/Short.BYTES];
//        int i = 0;
//        while(buffer.position() < buffer.limit()){
//            featureVector[i++] = buffer.getShort();
//        }
//    }
//
//    @Override
//    public void setByteArrayRepresentation(byte[] bytes, int offset, int length) {
//        ByteBuffer buffer = ByteBuffer.wrap(bytes,offset,length);
//
//        featureVector = new short[buffer.limit()/Short.BYTES];
//        int i = 0;
//        while(buffer.position() < buffer.limit()){
//            featureVector[i++] = buffer.getShort();
//        }
//    }

//    @Override
//    public byte[] getByteArrayRepresentation() {
//
//        return featureVector;
//    }

//    @Override
//    public void setByteArrayRepresentation(byte[] bytes) {
//        featureVector = new byte[bytes.length];
//        for(int i = 0; i < bytes.length; i++){
//            featureVector[i] = bytes[i];
//        }
//    }
//
//    @Override
//    public void setByteArrayRepresentation(byte[] bytes, int offset, int length) {
//        ByteBuffer buffer = ByteBuffer.wrap(bytes,offset,length);
//
//        featureVector = new byte[buffer.limit()];
//        int i = 0;
//        while(buffer.position() < buffer.limit()){
//            featureVector[i++] = buffer.get();
//        }
//    }

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
