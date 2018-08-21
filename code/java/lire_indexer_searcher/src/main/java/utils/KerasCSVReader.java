package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class KerasCSVReader {
    private String filepath;

    private String splitchar;
    private HashMap<String, ArrayList<Double>> values;
    public KerasCSVReader(String filepath,String splitchar){
        this.filepath = filepath;
        this.splitchar = splitchar;
        this.values = new HashMap<>();
        try {
            parseFile();
        } catch(Exception e){

        }
    }

    private void parseFile() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(new File(this.filepath)));
        String line = null;
        while((line = reader.readLine()) != null){
            String splits[] = line.split(this.splitchar);
            if(splits.length > 0){
                String filename = splits[0];
                ArrayList<Double> vals = new ArrayList<>();
                for(int i = 1; i < splits.length; i++){
                    vals.add(new Double(splits[i]));
                }
                this.values.put(filename,vals);
            }
        }
    }

    public HashMap<String,ArrayList<Double>> getValues(){
        return this.values;
    }

    public double[] getValuesOfFile(String filename){
        ArrayList<Double> list = this.values.get(filename);
        if(list == null) return null;
        double[] a = new double[list.size()];
        for (int  i = 0; i< list.size(); i++){
            a[i] = list.get(i).doubleValue();
        }
        return a;
    }
    public String getFilepath() {
        return filepath;
    }

    public String getSplitchar() {
        return splitchar;
    }

    public static void main(String[] args) {
        KerasCSVReader reader = new KerasCSVReader("/home/michael/master_thesis/code/python/csv_vgg16.csv",",");
        String imagepath = "/home/michael/master_thesis/data/Medico_2018_development_set/blurry-nothing/1.jpg";
        double[] values = reader.getValuesOfFile(imagepath);
        for(double d : values){
            System.out.print(d + ", ");
        }

    }
}
