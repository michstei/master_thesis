package utils;

/**
 * class to represent a confusionmatrix for the medico dataset
 */
public class MedicoConfusionMatrix {

    /**
     * the matrix
     */
    int [][] confusionMatrix = new int[Category.values().length][Category.values().length];

    /**
     * default constructor
     */
    public MedicoConfusionMatrix(){

    }

    /**
     * increases value of cell [catPred][catGold] by 1
     * @param catGold   actual category of image
     * @param catPred   predicted category of image
     */
    public void increaseValue(Category catGold, Category catPred){
        if(catGold != null && catPred != null)
            confusionMatrix[catGold.ordinal()][catPred.ordinal()] = confusionMatrix[catGold.ordinal()][catPred.ordinal()] + 1;
    }

    /**
     * @return total amount of entries (sum of all values in array)
     */
    public int getTotal(){
        int sum = 0;
        for(int i = 0; i < confusionMatrix.length; i++){
            for(int j = 0; j < confusionMatrix[0].length;j++){
                sum += confusionMatrix[i][j];
            }
        }
        return sum;
    }

    /**
     * @return number of correctly predicted entries (sum of diagonal values)
     */
    public int getCorrect(){
        int correct = 0;
        for(int i = 0; i < confusionMatrix.length; i++){
            correct += confusionMatrix[i][i];
        }
        return correct;
    }

    /**
     * @return number of incorrect entries ( total - correct )
     */
    public int getIncorrect(){
        return getTotal() - getCorrect();
    }

    /**
     * @return percentage of correct entries
     */
    public double getCorrectPcnt(){
        return  ((getCorrect()/(double)getTotal())*100);
    }

    /**
     * prints the confusionmatrix to stdout
     */
    public void printConfusionMatrix(){
        System.out.println(this.toString());
    }

    /**
     * @return String representation of the Matrix
     */
    @Override
    public String toString(){
        StringBuilder b = new StringBuilder();
        b.append(String.format("%5s","A \\ P"));
        for(Category cat : Category.values()){
            b.append(String.format("%5s",cat.getShortName()));
        }
        b.append("\n");
        for(int i = 0; i < Category.values().length;i++){
            b.append(String.format("%5s",Category.values()[i].getShortName()));
            double sum = 0.0;
            for(int j = 0; j < Category.values().length;j++){
                sum += confusionMatrix[i][j];
                b.append(String.format("%5s", confusionMatrix[i][j] + ""));
            }
            b.append(String.format("\t%6.2f %%", (confusionMatrix[i][i]/sum)*100));
            b.append("\n");
        }
        return b.toString();
    }


    public static void main(String[] args) {
        MedicoConfusionMatrix m = new MedicoConfusionMatrix();
        m.printConfusionMatrix();
        m.increaseValue(Category.CATEGORY_POLYPS,Category.CATEGORY_POLYPS);
        m.increaseValue(Category.CATEGORY_POLYPS,Category.CATEGORY_POLYPS);
        m.increaseValue(Category.CATEGORY_POLYPS,Category.CATEGORY_DYED_LIFTED_POLYPS);
        m.increaseValue(Category.CATEGORY_BLURRY_NOTHING,Category.CATEGORY_POLYPS);
        m.increaseValue(Category.CATEGORY_ULCERATIVE_COLITIS,Category.CATEGORY_POLYPS);
        m.increaseValue(Category.CATEGORY_POLYPS,Category.CATEGORY_COLON_CLEAR);
        m.increaseValue(Category.CATEGORY_POLYPS,Category.CATEGORY_POLYPS);
        m.increaseValue(Category.CATEGORY_POLYPS,Category.CATEGORY_POLYPS);
        m.increaseValue(Category.CATEGORY_POLYPS,Category.CATEGORY_POLYPS);
        m.increaseValue(Category.CATEGORY_POLYPS,Category.CATEGORY_POLYPS);
        m.printConfusionMatrix();
        for(Category c:Category.values()){
            System.out.println(c.getName());
        }
    }

}
