package classifier;

import net.semanticmetadata.lire.builders.DocumentBuilder;
import net.semanticmetadata.lire.searchers.ImageSearchHits;
import org.apache.lucene.index.IndexReader;
import utils.Category;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

public class WeightedImageSearchHitClassifier {
    private Weights weights;
    private Vector<ImageSearchHits> imageSearchHits;
    private Vector<String> classNames;
    private Vector<IndexReader> indexReaders;
    private StringBuilder builder;
    public WeightedImageSearchHitClassifier(Weights weights, Vector<ImageSearchHits> imageSearchHits, Vector<String> classNames, Vector<IndexReader> indexReaders,StringBuilder builder){
        this.imageSearchHits = imageSearchHits;
        this.weights = weights;
        this.classNames = classNames;
        this.indexReaders = indexReaders;
        this.builder = builder;
    }

    public Vector<Prediction> getPredictions(){
        Vector<Prediction> predictions = new Vector<>();
        Vector<Vector<Prediction>> allPredictions = new Vector<>();
        for(int classIndex = 0; classIndex< this.imageSearchHits.size();classIndex++){
            ImageSearchHits hits = this.imageSearchHits.get(classIndex);
            String className = this.classNames.get(classIndex%classNames.size());
            IndexReader reader = this.indexReaders.get(classIndex);
            Vector<Prediction> predictionsOfClass = new Vector<>();
            builder.append(className );
            builder.append(":");
            builder.append("\n");
            for(int hitIndex = 0; hitIndex < hits.length(); hitIndex++){
                try {
                    double score = hits.score(hitIndex);
                    String filename = reader.document(hits.documentID(hitIndex)).getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0];
                    builder.append(score);
                    builder.append(":");
                    builder.append(filename);
                    builder.append("\n");
                    for (Category cat : Category.values()) {
                        if(filename.contains("/" + cat.getName() + "/")){
                            boolean found = false;
                            for(int i =0; i<predictionsOfClass.size();i++){
                                if(predictionsOfClass.get(i).category == cat){
                                    predictionsOfClass.get(i).score += 1.;
                                    found = true;
                                    break;
                                }
                            }
                            if(!found)
                                predictionsOfClass.add(new Prediction(cat,1.));
                            break;

                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            for(Prediction pred : predictionsOfClass){
                pred.score = pred.score * this.weights.getWeight(className,pred.category) / (double)hits.length();
            }
            allPredictions.add(predictionsOfClass);
        }
        for(Vector<Prediction> preds : allPredictions){
            for(Prediction p: preds){
                boolean found = false;
                for(Prediction prediction : predictions){

                    if(p.category == prediction.category){
                        prediction.score += p.score;
                        found = true;
                        break;
                    }
                }
                if(!found)
                    predictions.add(p);
            }
        }
        predictions.sort(Comparator.comparingDouble((Prediction o) -> o.score));
        Collections.reverse(predictions);
        return  predictions;

    }



    public class Prediction{
        public Category category;
        public double score;

        public Prediction(Category category, double score){
            this.category = category;
            this.score = score;
        }
    }
}
