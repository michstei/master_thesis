package classifier;


import java.util.*;

import static java.util.stream.Collectors.toMap;

public class ImageSearchHitClassifier {
    private Vector<String> classes;
    private Vector<String> imageSearchHits;
    public ImageSearchHitClassifier(Vector<String> classes, Vector<String> imageSearchHits){
        this.classes = classes;
        this.imageSearchHits = imageSearchHits;
    }
    public Map<String,Double> getPredictions(){
        HashMap<String,Double> predictions = new HashMap<>();
        HashMap<String,Integer> counts = new HashMap<>();

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

        return  predictions.entrySet()
                .stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .collect(
                        toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,
                                LinkedHashMap::new));

    }
}
