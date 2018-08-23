package main.classifier;


import java.util.HashMap;
import java.util.Vector;

public class ImageSearchHitClassifier {
    private Vector<String> classes;
    private Vector<String> imageSearchHits;
    private String filename;
    public ImageSearchHitClassifier(Vector<String> classes, Vector<String> imageSearchHits, String filename){
        this.classes = classes;
        this.imageSearchHits = imageSearchHits;
        this.filename = filename;
    }
    public HashMap<String,Double> getPredictions(){
        HashMap<String,Double> predictions = new HashMap<>();
        HashMap<String,Integer> counts = new HashMap<>();
        for(String key : classes){
            counts.put(key,0);
        }
        for(String s : imageSearchHits){
            for(String c : classes){
                if(s.contains(c)){
                    int val = counts.getOrDefault(c,0);
                    counts.put(c,val + 1);
                    break;
                }
            }
        }

        for(String key : counts.keySet()){
            predictions.put(key,  (counts.get(key)/(double)imageSearchHits.size()));
        }

        return (HashMap<String, Double>) predictions.entrySet().stream()
                .sorted(HashMap.Entry.comparingByValue());
    }
}
