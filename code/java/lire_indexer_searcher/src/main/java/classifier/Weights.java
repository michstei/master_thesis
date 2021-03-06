package classifier;

import utils.Category;

import java.util.Vector;

public class Weights {
    private float [] [] weights;
    private Vector<String> classNames;
    private Vector<Category> categories;

    public Weights(Vector<String> classNames, Vector<Category> categories){
        this.classNames = new Vector<>(classNames);
        this.categories = new Vector<>(categories);
        this.weights = new float[this.classNames.size()][this.categories.size()];
    }

    public void setWeight(String className, Category category, float weight){
        this.weights[classNames.indexOf(className)][categories.indexOf(category)] = weight;
    }

    public void setWeightsForClass(String className,Vector<Float> weights){
        if(weights.size() != categories.size())
            throw new IllegalArgumentException("weights.size() != categories.size()");
        for(int i = 0; i < categories.size();i++)
            this.weights[classNames.indexOf(className)][i] = weights.get(i);
    }

    public float getWeight(String className, Category category){
        return this.weights[classNames.indexOf(className)][categories.indexOf(category)];
    }

    public boolean checkWeightSums(){
        boolean correct = true;
        for(int catIndex = 0; catIndex < this.weights.length;catIndex++){
            float sum = 0.f;
            for(int classIndex = 0; classIndex < this.weights.length;classIndex++){
                sum += this.weights[classIndex][catIndex];
            }
            if(Math.abs(1.0-sum)>0.001){
                correct = false;
                break;
            }
        }
        return correct;
    }
}
