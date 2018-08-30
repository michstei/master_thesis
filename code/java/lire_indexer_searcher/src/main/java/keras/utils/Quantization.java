package keras.utils;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Quantization {

    enum QuantizationType{
        QUANTIZATION_TYPE_DOUBLE,
        QUANTIZATION_TYPE_FLOAT,
        QUANTIZATION_TYPE_LONG,
        QUANTIZATION_TYPE_INT,
        QUANTIZATION_TYPE_SHORT,
        QUANTIZATION_TYPE_BYTE
    }

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

    public static double getMin(double[] values){
        double min = Double.MAX_VALUE;

        for(double d : values){
            if(d < min){
                min = d;
            }
        }
        return 0;
    }
    public static double getMax(double[] values){
        double max = Double.MIN_VALUE;
        for(double d : values){
            if(d > max){
                max = d;
            }
        }
        return max;
    }
    public static double[] castToDoubleArray(double[] in){

        return in;
    }
    public static double[] castToDoubleArray(float[] in){
        double[] out = new double[in.length];
        for(int i = 0; i < in.length; i++){
            out[i] = (double)in[i];
        }
        return out;
    }
    public static double[] castToDoubleArray(long[] in){
        double[] out = new double[in.length];
        for(int i = 0; i < in.length; i++){
            out[i] = (double)in[i];
        }
        return out;
    }
    public static double[] castToDoubleArray(int[] in){
        double[] out = new double[in.length];
        for(int i = 0; i < in.length; i++){
            out[i] = (double)in[i];
        }
        return out;
    }
    public static double[] castToDoubleArray(short[] in){
        double[] out = new double[in.length];
        for(int i = 0; i < in.length; i++){
            out[i] = (double)in[i];
        }
        return out;
    }
    public static double[] castToDoubleArray(byte[] in){
        double[] out = new double[in.length];
        for(int i = 0; i < in.length; i++){
            out[i] = (double)in[i];
        }
        return out;
    }

    public static void quantizeCSVFile(String inFile, String outFile, QuantizationType type) throws IOException {
        KerasCSVReader reader = new KerasCSVReader(inFile,",");
        HashMap<String, ArrayList<Double>> values = reader.getValues();
        StringBuilder stringBuilder = new StringBuilder();
        for(String filename : values.keySet()){
            ArrayList<Double> list = values.get(filename);
            double[] array = new double[list.size()];

            for(int i = 0; i < list.size(); i++ ){
                array[i] = list.get(i);
            }
            double min = getMin(array);
            double max = getMax(array);
            stringBuilder.append(filename + ",");
            switch (type) {
                case QUANTIZATION_TYPE_DOUBLE:
                {
                    double [] newValues = array;
                    String valuesString = "";
                    for (double f : newValues) {
                        String s = String.format("%.40f",f ).replaceAll("0+$", "");
                        valuesString += s + ",";
                    }
                    valuesString = valuesString.substring(0,valuesString.length()-1);
                    stringBuilder.append(valuesString);
                    break;
                }
                case QUANTIZATION_TYPE_FLOAT:
                {
                    float [] newValues = quantizeDoubleToFloat(array, min, max);
                    String valuesString = "";
                    for (float f : newValues) {
                        String s = String.format("%.40f",f ).replaceAll("0+$", "");
                        valuesString += s + ",";
                    }
                    valuesString = valuesString.substring(0,valuesString.length()-1);
                    stringBuilder.append(valuesString);
                    break;
                }
                case QUANTIZATION_TYPE_LONG:
                {
                    long [] newValues = quantizeDoubleToLong(array, min, max);
                    String valuesString = "";
                    for (long f : newValues) {
                        String s = f + "";
                        valuesString += s + ",";
                    }
                    valuesString = valuesString.substring(0,valuesString.length()-1);
                    stringBuilder.append(valuesString);
                    break;
                }
                case QUANTIZATION_TYPE_INT:
                {
                    int [] newValues = quantizeDoubleToInt(array, min, max);
                    String valuesString = "";
                    for (int f : newValues) {
                        String s = f + "";
                        valuesString += s + ",";
                    }
                    valuesString = valuesString.substring(0,valuesString.length()-1);
                    stringBuilder.append(valuesString);
                    break;
                }
                case QUANTIZATION_TYPE_SHORT:
                {
                    short [] newValues = quantizeDoubleToShort(array, min, max);
                    String valuesString = "";
                    for (short f : newValues) {
                        String s = f + "";
                        valuesString += s + ",";
                    }
                    valuesString = valuesString.substring(0,valuesString.length()-1);
                    stringBuilder.append(valuesString);
                    break;
                }
                case QUANTIZATION_TYPE_BYTE:
                {
                    byte [] newValues = quantizeDoubleToByte(array, min, max);
                    String valuesString = "";
                    for (byte f : newValues) {
                        String s = f + "";
                        valuesString += s + ",";
                    }
                    valuesString = valuesString.substring(0,valuesString.length()-1);
                    stringBuilder.append(valuesString);
                    break;
                }
            }
            stringBuilder.append("\n");

        }
        FileUtils.writeStringToFile(new File(outFile), stringBuilder.toString(), (Charset) null);


    }



    public static void main(String[] args) {

        String inFile = "/home/michael/master_thesis/data/csv/vgg16.csv";
        String outFileBase = "/home/michael/master_thesis/data/csv/quantized/";
        
        try {
            Quantization.quantizeCSVFile(inFile,outFileBase + "vgg16_" + "double"    + ".csv",QuantizationType.QUANTIZATION_TYPE_DOUBLE);
            Quantization.quantizeCSVFile(inFile,outFileBase + "vgg16_" + "float"     + ".csv",QuantizationType.QUANTIZATION_TYPE_FLOAT);
            Quantization.quantizeCSVFile(inFile,outFileBase + "vgg16_" + "long"      + ".csv",QuantizationType.QUANTIZATION_TYPE_LONG);
            Quantization.quantizeCSVFile(inFile,outFileBase + "vgg16_" + "int"       + ".csv",QuantizationType.QUANTIZATION_TYPE_INT);
            Quantization.quantizeCSVFile(inFile,outFileBase + "vgg16_" + "short"     + ".csv",QuantizationType.QUANTIZATION_TYPE_SHORT);
            Quantization.quantizeCSVFile(inFile,outFileBase + "vgg16_" + "byte"      + ".csv",QuantizationType.QUANTIZATION_TYPE_BYTE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
