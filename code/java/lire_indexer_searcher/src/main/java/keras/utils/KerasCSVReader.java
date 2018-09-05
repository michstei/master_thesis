package keras.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;

public class KerasCSVReader {
    private String filepath;

    private String splitchar;
    private HashMap<String, double[]> valuesDouble;
    private HashMap<String, float[]> valuesFloat;
    private HashMap<String, long[]> valuesLong;
    private HashMap<String, int[]> valuesInt;
    private HashMap<String, short[]> valuesShort;
    private HashMap<String, byte[]> valuesByte;


    public KerasCSVReader(String filepath,String splitchar){
        this.filepath = filepath;
        this.splitchar = splitchar;
        this.valuesDouble = new HashMap<>();
        this.valuesFloat = new HashMap<>();
        this.valuesLong = new HashMap<>();
        this.valuesInt = new HashMap<>();
        this.valuesShort = new HashMap<>();
        this.valuesByte = new HashMap<>();
        try {
            parseFile();
            System.out.println("initialized KerasCSVReader : " + this.filepath);
        } catch(Exception e){

        }
    }


    private void parseFile() throws IOException {
        ArrayList<String> lines = (ArrayList<String>) Files.readAllLines(Paths.get(this.filepath));
        DoubleConverterRunnable doubleConverter = new DoubleConverterRunnable (lines,this.getSplitchar());
        FloatConverterRunnable  floatConverter = new FloatConverterRunnable (lines,this.getSplitchar());
        LongConverterRunnable   longConverter = new LongConverterRunnable   (lines,this.getSplitchar());
        IntConverterRunnable    intConverter = new IntConverterRunnable     (lines,this.getSplitchar());
        ShortConverterRunnable  shortConverter = new ShortConverterRunnable (lines,this.getSplitchar());
        ByteConverterRunnable   byteConverter = new ByteConverterRunnable   (lines,this.getSplitchar());
        Thread[] t = new Thread[6];
        t[0] = new Thread(doubleConverter);
        t[1] = new Thread(floatConverter);
        t[2] = new Thread(longConverter);
        t[3] = new Thread(intConverter);
        t[4] = new Thread(shortConverter);
        t[5] = new Thread(byteConverter);
        for (Thread th : t){
            th.start();
        }
        try {
            for(Thread th : t) {
                th.join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        this.valuesDouble = doubleConverter.getResult();
        this.valuesFloat = floatConverter.getResult();
        this.valuesLong = longConverter.getResult();
        this.valuesInt = intConverter.getResult();
        this.valuesShort = shortConverter.getResult();
        this.valuesByte = byteConverter.getResult();

//        BufferedReader reader = new BufferedReader(new FileReader(new File(this.filepath)));
//        String line = null;
//        while((line = reader.readLine()) != null){
//            String splits[] = line.split(this.splitchar);

//            if(splits.length > 0){
//                String filename = splits[0];
//                ArrayList<Double> valsD = new ArrayList<>();
//                ArrayList<Float> valsF = new ArrayList<>();
//                ArrayList<Long> valsL = new ArrayList<>();
//                ArrayList<Integer> valsI = new ArrayList<>();
//                ArrayList<Short> valsS = new ArrayList<>();
//                ArrayList<Byte> valsB = new ArrayList<>();
//                for(int i = 1; i < splits.length; i++){
//                    String s = splits[i];
//                    if(splits[i].endsWith(".")){
//                        splits[i] = splits[i].substring(0,splits[i].length()-1);
//                    }
//                    try {
//                        valsD.add(new Double(splits[i]));
//                    }catch(Exception e){
//                        continue;
//                    }
//                    try{
//                        valsF.add(new Float(splits[i]));
//                    }catch(Exception e){
//                        continue;
//                    }
//                    try{
//                        valsL.add(new Long(splits[i]));
//                    }catch(Exception e){
//                        continue;
//                    }
//                    try{
//                        valsI.add(new Integer(splits[i]));
//                    }catch(Exception e){
//                        continue;
//                    }
//                    try{
//                        valsS.add(new Short(splits[i]));
//                    }catch(Exception e){
//                        continue;
//                    }
//                    try{
//                        valsB.add(new Byte(splits[i]));
//                    }catch(Exception e){
//                        continue;
//                    }
//
//                }
//                double[] arrD = new double[ valsD.size()];
//
//                for(int i = 0; i < valsD.size(); i++){
//                    arrD[i] = valsD.get(i);
//                }
//                this.valuesDouble.put(filename, arrD);
//                if(valsD.size() == valsF.size()) {
//                    float[] arrF = new float[valsF.size()];
//                    for (int i = 0; i < valsF.size(); i++) {
//                        arrF[i] = valsF.get(i);
//
//                    }
//                    this.valuesFloat.put(filename, arrF);
//                }
//                if(valsD.size() == valsL.size()) {
//                    long[] arrL =   new long[   valsL.size()];
//                    for(int i = 0; i < valsL.size(); i++){
//                        arrL[i] = valsL.get(i);
//
//                    }
//                    this.valuesLong.put(filename,   arrL);
//                }
//                if(valsD.size() == valsI.size()) {
//                    int[] arrI =    new int[    valsI.size()];
//                    for(int i = 0; i < valsI.size(); i++){
//                        arrI[i] = valsI.get(i);
//
//                    }
//                    this.valuesInt.put(filename,    arrI);
//                }
//                if(valsD.size() == valsS.size()) {
//                    short[] arrS =  new short[  valsS.size()];
//                    for(int i = 0; i < valsS.size(); i++){
//                        arrS[i] = valsS.get(i);
//
//                    }
//                    this.valuesShort.put(filename,  arrS);
//                }
//                if(valsD.size() == valsB.size()) {
//                    byte[] arrB =   new byte[   valsB.size()];
//                    for(int i = 0; i < valsB.size(); i++){
//                        arrB[i] = valsB.get(i);
//
//                    }
//                    this.valuesByte.put(filename,   arrB);
//                }
//
//            }
//        }
    }

    public HashMap<String,double[]> getValuesDouble(){
        return this.valuesDouble;
    }

    public double[] getValuesOfFileDouble(String filename){
        return this.valuesDouble.get(filename);
    }
    public float[] getValuesOfFileFloat(String filename){
        return this.valuesFloat.get(filename);
    }
    public long[] getValuesOfFileLong(String filename){
        return this.valuesLong.get(filename);
    }
    public int[] getValuesOfFileInt(String filename){
        return this.valuesInt.get(filename);
    }
    public short[] getValuesOfFileShort(String filename){
        return this.valuesShort.get(filename);
    }
    public byte[] getValuesOfFileByte(String filename){
        return this.valuesByte.get(filename);
    }
    public String getFilepath() {
        return filepath;
    }

    public String getSplitchar() {
        return splitchar;
    }


    class DoubleConverterRunnable implements Runnable{
        ArrayList<String> lines;
        private String splitChar;
        private HashMap<String, double[]>  result;
        public DoubleConverterRunnable(ArrayList<String> lines, String splitChar){
            this.lines = lines;
            this.result = new HashMap<>();
            this.splitChar = splitChar;
        }


        public HashMap<String, double[]> getResult(){

            return result;
        }

        @Override
        public void run() {

            String filename = null;
            for(String line : lines) {
                String[] splits = line.split(this.splitChar);
                filename = splits[0];
                double[] res = new double[splits.length - 1];
                for (int i = 1; i < splits.length; i++) {
                    String s = splits[i];
                    if (s.endsWith(".")) {
                        s = s.substring(0, s.length() - 1);
                    }
                    try {
                        res[i - 1] = new Double(s).doubleValue();
                    } catch (Exception e) {
                        res = null;
                        break;
                    }
                }
                if(res != null){
                    result.put(filename,res);
                }
            }

        }
    }

    class FloatConverterRunnable implements Runnable{
        ArrayList<String> lines;
        private String splitChar;
        private HashMap<String, float[]>  result;
        public FloatConverterRunnable(ArrayList<String> lines, String splitChar){
            this.lines = lines;
            this.result = new HashMap<>();
            this.splitChar = splitChar;
        }


        public HashMap<String, float[]> getResult(){

            return result;
        }

        @Override
        public void run() {

            String filename = null;
            for(String line : lines) {
                String[] splits = line.split(this.splitChar);
                filename = splits[0];
                float[] res = new float[splits.length - 1];
                for (int i = 1; i < splits.length; i++) {
                    String s = splits[i];
                    if (s.endsWith(".")) {
                        s = s.substring(0, s.length() - 1);
                    }
                    try {
                        res[i - 1] = new Float(s).floatValue();
                    } catch (Exception e) {
                        res = null;
                        break;
                    }
                }
                if(res != null){
                    result.put(filename,res);
                }
            }

        }
    }

    class LongConverterRunnable implements Runnable{
        ArrayList<String> lines;
        private String splitChar;
        private HashMap<String, long[]>  result;
        public LongConverterRunnable(ArrayList<String> lines, String splitChar){
            this.lines = lines;
            this.result = new HashMap<>();
            this.splitChar = splitChar;
        }


        public HashMap<String, long[]> getResult(){

            return result;
        }

        @Override
        public void run() {

            String filename = null;
            for(String line : lines) {
                String[] splits = line.split(this.splitChar);
                filename = splits[0];
                long[] res = new long[splits.length - 1];
                for (int i = 1; i < splits.length; i++) {
                    String s = splits[i];
                    if (s.endsWith(".")) {
                        s = s.substring(0, s.length() - 1);
                    }
                    try {
                        res[i - 1] = new Long(s).longValue();
                    } catch (Exception e) {
                        res = null;
                        break;
                    }
                }
                if(res != null){
                    result.put(filename,res);
                }
            }

        }
    }

    class IntConverterRunnable implements Runnable{
        ArrayList<String> lines;
        private String splitChar;
        private HashMap<String, int[]>  result;
        public IntConverterRunnable(ArrayList<String> lines, String splitChar){
            this.lines = lines;
            this.result = new HashMap<>();
            this.splitChar = splitChar;
        }


        public HashMap<String, int[]> getResult(){

            return result;
        }

        @Override
        public void run() {

            String filename = null;
            for(String line : lines) {
                String[] splits = line.split(this.splitChar);
                filename = splits[0];
                int[] res = new int[splits.length - 1];
                for (int i = 1; i < splits.length; i++) {
                    String s = splits[i];
                    if (s.endsWith(".")) {
                        s = s.substring(0, s.length() - 1);
                    }
                    try {
                        res[i - 1] = new Integer(s).intValue();
                    } catch (Exception e) {
                        res = null;
                        break;
                    }
                }
                if(res != null){
                    result.put(filename,res);
                }
            }

        }
    }

    class ShortConverterRunnable implements Runnable{
        ArrayList<String> lines;
        private String splitChar;
        private HashMap<String, short[]>  result;
        public ShortConverterRunnable(ArrayList<String> lines, String splitChar){
            this.lines = lines;
            this.result = new HashMap<>();
            this.splitChar = splitChar;
        }


        public HashMap<String, short[]> getResult(){

            return result;
        }

        @Override
        public void run() {

            String filename = null;
            for(String line : lines) {
                String[] splits = line.split(this.splitChar);
                filename = splits[0];
                short[] res = new short[splits.length - 1];
                for (int i = 1; i < splits.length; i++) {
                    String s = splits[i];
                    if (s.endsWith(".")) {
                        s = s.substring(0, s.length() - 1);
                    }
                    try {
                        res[i - 1] = new Short(s).shortValue();
                    } catch (Exception e) {
                        res = null;
                        break;
                    }
                }
                if(res != null){
                    result.put(filename,res);
                }
            }

        }
    }

    class ByteConverterRunnable implements Runnable{
        ArrayList<String> lines;
        private String splitChar;
        private HashMap<String, byte[]>  result;
        public ByteConverterRunnable(ArrayList<String> lines, String splitChar){
            this.lines = lines;
            this.result = new HashMap<>();
            this.splitChar = splitChar;
        }


        public HashMap<String, byte[]> getResult(){

            return result;
        }

        @Override
        public void run() {

            String filename = null;
            for(String line : lines) {
                String[] splits = line.split(this.splitChar);
                filename = splits[0];
                byte[] res = new byte[splits.length - 1];
                for (int i = 1; i < splits.length; i++) {
                    String s = splits[i];
                    if (s.endsWith(".")) {
                        s = s.substring(0, s.length() - 1);
                    }
                    try {
                        res[i - 1] = new Byte(s).byteValue();
                    } catch (Exception e) {
                        res = null;
                        break;
                    }
                }
                if(res != null){
                    result.put(filename,res);
                }
            }

        }
    }

    public static void main(String[] args) {
        KerasCSVReader reader = new KerasCSVReader("/home/michael/master_thesis/code/python/csv_vgg16.csv",",");
        String imagepath = "/home/michael/master_thesis/data/Medico_2018_development_set/blurry-nothing/1.jpg";
        double[] values = reader.getValuesOfFileDouble(imagepath);
        for(double d : values){
            System.out.print(d + ", ");
        }

    }
}
