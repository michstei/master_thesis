package main;

import java.util.Arrays;

public class Quantization {

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

    public static void main(String[] args) {

        double [] d = {0.27959526,0.05277591,-0.30698302,0.5195378,0.044229206,1.4849992,0.2772316,0.035124235,0.57728857,0.1888771,0.6720748,0.844071};
        double min = Quantization.getMin(d);
        double max = Quantization.getMax(d);
        float [] f =  Quantization.quantizeDoubleToFloat(   d, min, max);
        long [] l =  Quantization.quantizeDoubleToLong(     d, min, max);
        int [] i =  Quantization.quantizeDoubleToInt(       d, min, max);
        short [] s =  Quantization.quantizeDoubleToShort(   d, min, max);
        byte [] b =  Quantization.quantizeDoubleToByte(     d, min, max);

        System.out.println("Double: " + Double.MAX_VALUE    + ": "+ Arrays.toString(d));
        System.out.println("Float : " + Float.MAX_VALUE     + ": "+ Arrays.toString(f));
        System.out.println("Long  : " + Long.MAX_VALUE      + ": "+ Arrays.toString(l));
        System.out.println("Int   : " + Integer.MAX_VALUE   + ": "+ Arrays.toString(i));
        System.out.println("Short : " + Short.MAX_VALUE     + ": "+ Arrays.toString(s));
        System.out.println("Byte  : " + Byte.MAX_VALUE      + ": "+ Arrays.toString(b));
    }
}
