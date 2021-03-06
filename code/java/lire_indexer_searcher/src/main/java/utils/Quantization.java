package utils;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;

/**
 * class to do the quantization of the generated features of the keras models
 */
public class Quantization {

    /**
     * datatypes to convert to
     */
    enum QuantizationType{
        QUANTIZATION_TYPE_DOUBLE,
        QUANTIZATION_TYPE_FLOAT,
        QUANTIZATION_TYPE_LONG,
        QUANTIZATION_TYPE_INT,
        QUANTIZATION_TYPE_SHORT,
        QUANTIZATION_TYPE_BYTE
    }

    /**
     * brings a double array in float range
     * @param values    values to quantize
     * @param min       minimum value of values
     * @param max       maximum value of values
     * @return          the quantized array
     */
    public static float[] quantizeDoubleToFloat(double[] values, double min, double max){
        float[] newValues = new float[values.length];
        for(int i = 0;i < values.length; i++){
            double val = (values[i] - min)/(max - min);
            val = (val * Float.MAX_VALUE);
            if(val < Float.MIN_VALUE) val = 0;
            if(val > Float.MAX_VALUE) val = Float.MAX_VALUE;
            newValues[i] = (float) val;
        }
        return newValues;
    }
    /**
     * brings a double array in long range
     * @param values    values to quantize
     * @param min       minimum value of values
     * @param max       maximum value of values
     * @return          the quantized array
     */
    public static long[] quantizeDoubleToLong(double[] values, double min, double max){
        long[] newValues = new long[values.length];
        for(int i = 0;i < values.length; i++){
            double val = (values[i] - min)/(max - min);
            val = (val * Long.MAX_VALUE);
            if(val < Long.MIN_VALUE) val = 0;
            if(val > Long.MAX_VALUE) val = Long.MAX_VALUE;
            newValues[i] = (int) val;
        }
        return newValues;
    }
    /**
     * brings a double array in int range
     * @param values    values to quantize
     * @param min       minimum value of values
     * @param max       maximum value of values
     * @return          the quantized array
     */
    public static int[] quantizeDoubleToInt(double[] values, double min, double max){
        int[] newValues = new int[values.length];
        for(int i = 0;i < values.length; i++){
            double val = (values[i] - min)/(max - min);
            val = (val * Integer.MAX_VALUE);
            if(val < Integer.MIN_VALUE) val = 0;
            if(val > Integer.MAX_VALUE) val = Integer.MAX_VALUE;
            newValues[i] = (int) val;
        }
        return newValues;
    }
    /**
     * brings a double array in short range
     * @param values    values to quantize
     * @param min       minimum value of values
     * @param max       maximum value of values
     * @return          the quantized array
     */
    public static short[] quantizeDoubleToShort(double[] values, double min, double max){
        short[] newValues = new short[values.length];
        for(int i = 0;i < values.length; i++){
            double val = (values[i] - min)/(max - min);
            val = (val * Short.MAX_VALUE);
            if(val < Short.MIN_VALUE) val = 0;
            if(val > Short.MAX_VALUE) val = Short.MAX_VALUE;
            newValues[i] = (short) val;
        }
        return newValues;
    }
    /**
     * brings a double array in byte range
     * @param values    values to quantize
     * @param min       minimum value of values
     * @param max       maximum value of values
     * @return          the quantized array
     */
    public static byte[] quantizeDoubleToByte(double[] values, double min, double max){
        byte[] newValues = new byte[values.length];
        for(int i = 0;i < values.length; i++){
            double val = (values[i] - min)/(max - min);
            val = (val * Byte.MAX_VALUE);
            if(val < Byte.MIN_VALUE) val = 0;
            if(val > Byte.MAX_VALUE) val = Byte.MAX_VALUE;
            newValues[i] = (byte) val;
        }
        return newValues;
    }

    /***
     * gets the minimum value of an array
     * @param values array
     * @return minimum of values
     */
    public static double getMin(double[] values){
        double min = Double.MAX_VALUE;

        for(double d : values){
            if(d < min){
                min = d;
            }
        }
        return 0;
    }

    /***
     * gets the maximum value of an array
     * @param values array
     * @return maximum of array
     */
    public static double getMax(double[] values){
        double max = Double.MIN_VALUE;
        for(double d : values){
            if(d > max){
                max = d;
            }
        }
        return max;
    }

    /***
     *
     * @param in
     * @return  in
     */
    public static double[] castToDoubleArray(double[] in){

        return in;
    }

    /***
     * casts  float[] to  double[]
     * @param in array to cast
     * @return  double[] version of in
     */
    public static double[] castToDoubleArray(float[] in){
        double[] out = new double[in.length];
        for(int i = 0; i < in.length; i++){
            out[i] = (double)in[i];
        }
        return out;
    }
    /***
     * casts  long[] to  double[]
     * @param in array to cast
     * @return  double[] version of in
     */
    public static double[] castToDoubleArray(long[] in){
        double[] out = new double[in.length];
        for(int i = 0; i < in.length; i++){
            out[i] = (double)in[i];
        }
        return out;
    }
    /***
     * casts  int[] to  double[]
     * @param in array to cast
     * @return  double[] version of in
     */
    public static double[] castToDoubleArray(int[] in){
        double[] out = new double[in.length];
        for(int i = 0; i < in.length; i++){
            out[i] = (double)in[i];
        }
        return out;
    }
    /***
     * casts  short[] to  double[]
     * @param in array to cast
     * @return  double[] version of in
     */
    public static double[] castToDoubleArray(short[] in){
        double[] out = new double[in.length];
        for(int i = 0; i < in.length; i++){
            out[i] = (double)in[i];
        }
        return out;
    }
    /***
     * casts  byte[] to  double[]
     * @param in array to cast
     * @return  double[] version of in
     */
    public static double[] castToDoubleArray(byte[] in){
        double[] out = new double[in.length];
        for(int i = 0; i < in.length; i++){
            out[i] = (double)in[i];
        }
        return out;
    }

    /***
     * takes a csv file with double values and generates a csv file with <code>type</code> values
     * @param reader    KerasCsvReader to quantize
     * @param outFile   path to the output csv file
     * @param type      Type of the new csv file
     * @throws IOException
     */
    public static void quantizeCSVFile(KerasCSVReader reader, String outFile, QuantizationType type) throws IOException {
        HashMap<String, double[]> values = reader.getValuesDouble();
        StringBuilder stringBuilder = new StringBuilder();
        for(String filename : values.keySet()){
            double[] array = values.get(filename);
            double min = getMin(array);
            double max = getMax(array);
            stringBuilder.append(filename.replace("/home/mst/master_thesis/data/Medico_2018_development_set", "/home/michael/master_thesis/data")).append(",");
            switch (type) {
                case QUANTIZATION_TYPE_DOUBLE:
                {
                    StringBuilder valuesString = new StringBuilder();
                    for (double f : array) {
                        String s = String.format("%.40f",f ).replaceAll("0+$", "");
                        valuesString.append(s).append(",");
                    }
                    valuesString = new StringBuilder(valuesString.substring(0, valuesString.length() - 1));
                    stringBuilder.append(valuesString);
                    break;
                }
                case QUANTIZATION_TYPE_FLOAT:
                {
                    float [] newValues = quantizeDoubleToFloat(array, min, max);
                    StringBuilder valuesString = new StringBuilder();
                    for (float f : newValues) {
                        String s = String.format("%.40f",f ).replaceAll("0+$", "");
                        valuesString.append(s).append(",");
                    }
                    valuesString = new StringBuilder(valuesString.substring(0, valuesString.length() - 1));
                    stringBuilder.append(valuesString);
                    break;
                }
                case QUANTIZATION_TYPE_LONG:
                {
                    long [] newValues = quantizeDoubleToLong(array, min, max);
                    StringBuilder valuesString = new StringBuilder();
                    for (long f : newValues) {
                        String s = f + "";
                        valuesString.append(s).append(",");
                    }
                    valuesString = new StringBuilder(valuesString.substring(0, valuesString.length() - 1));
                    stringBuilder.append(valuesString);
                    break;
                }
                case QUANTIZATION_TYPE_INT:
                {
                    int [] newValues = quantizeDoubleToInt(array, min, max);
                    StringBuilder valuesString = new StringBuilder();
                    for (int f : newValues) {
                        String s = f + "";
                        valuesString.append(s).append(",");
                    }
                    valuesString = new StringBuilder(valuesString.substring(0, valuesString.length() - 1));
                    stringBuilder.append(valuesString);
                    break;
                }
                case QUANTIZATION_TYPE_SHORT:
                {
                    short [] newValues = quantizeDoubleToShort(array, min, max);
                    StringBuilder valuesString = new StringBuilder();
                    for (short f : newValues) {
                        String s = f + "";
                        valuesString.append(s).append(",");
                    }
                    valuesString = new StringBuilder(valuesString.substring(0, valuesString.length() - 1));
                    stringBuilder.append(valuesString);
                    break;
                }
                case QUANTIZATION_TYPE_BYTE:
                {
                    byte [] newValues = quantizeDoubleToByte(array, min, max);
                    StringBuilder valuesString = new StringBuilder();
                    for (byte f : newValues) {
                        String s = f + "";
                        valuesString.append(s).append(",");
                    }
                    valuesString = new StringBuilder(valuesString.substring(0, valuesString.length() - 1));
                    stringBuilder.append(valuesString);
                    break;
                }
            }
            stringBuilder.append("\n");

        }
        FileUtils.writeStringToFile(new File(outFile), stringBuilder.toString(), (Charset) null);


    }



    public static void main(String[] args) {

        String inFile = "/home/michael/master_thesis/data/csv/";
        String outFileBase = "/home/michael/master_thesis/data/csv/";
        String[] filenames = new String[]{"mobilenet","resnet50","vgg16","vgg19","xception","densenet121","densenet169","densenet201"};
        try {
            for (String f : new String[]{"1024orLessTest/"}){
                System.out.println(f + ":");
                if(!(new File(outFileBase + f +"quantized/" ).exists())){
                    new File(outFileBase + f +"quantized/" ).mkdirs();
                }
                for (String s : filenames) {

                    KerasCSVReader reader = new KerasCSVReader(inFile + f + s + ".csv", ",");
//                    Quantization.quantizeCSVFile(reader, outFileBase + f +"quantized/" + s + "_double" + ".csv", QuantizationType.QUANTIZATION_TYPE_DOUBLE);
//                    System.out.println("File 1 processed");
//                    Quantization.quantizeCSVFile(reader, outFileBase + f +"quantized/" + s + "_float" + ".csv", QuantizationType.QUANTIZATION_TYPE_FLOAT);
//                    System.out.println("File 2 processed");
//                    Quantization.quantizeCSVFile(reader, outFileBase + f +"quantized/" + s + "_long" + ".csv", QuantizationType.QUANTIZATION_TYPE_LONG);
//                    System.out.println("File 3 processed");
                    Quantization.quantizeCSVFile(reader, outFileBase + f +"quantized/" + s + "_int" + ".csv", QuantizationType.QUANTIZATION_TYPE_INT);
                    System.out.println("File 4 processed");
//                    Quantization.quantizeCSVFile(reader, outFileBase + f +"quantized/" + s + "_short" + ".csv", QuantizationType.QUANTIZATION_TYPE_SHORT);
//                    System.out.println("File 5 processed");
//                    Quantization.quantizeCSVFile(reader, outFileBase + f +"quantized/" + s + "_byte" + ".csv", QuantizationType.QUANTIZATION_TYPE_BYTE);
//                    System.out.println("File 6 processed");
                    System.out.println(s + " processed");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
