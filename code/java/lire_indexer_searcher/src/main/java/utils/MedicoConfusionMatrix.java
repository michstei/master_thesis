package utils;

/**
 * class to represent a confusionmatrix for the medico dataset
 */
public class MedicoConfusionMatrix {
    /**
     * Categories of the medicodataset
     */
    public enum Category {
        CATEGORY_BLURRY_NOTHING,
        CATEGORY_COLON_CLEAR,
        CATEGORY_DYED_LIFTED_POLYPS,
        CATEGORY_DYED_RESECTION_MARGINS ,
        CATEGORY_ESOPHAGITIS,
        CATEGORY_INSTRUMENTS,
        CATEGORY_NORMAL_CECUM,
        CATEGORY_NORMAL_PYLORUS,
        CATEGORY_NORMAL_Z_LINE,
        CATEGORY_OUT_OF_PATIENT,
        CATEGORY_POLYPS,
        CATEGORY_RETROFLEX_RECTUM,
        CATEGORY_RETROFLEX_STOMACH,
        CATEGORY_STOOL_INCLUSIONS,
        CATEGORY_STOOL_PLENTY,
        CATEGORY_ULCERATIVE_COLITIS;

        /**
         * @return short version of the category
         */
        public String getShortName(){
            switch(this){

                case CATEGORY_BLURRY_NOTHING:
                    return "BLN";
                case CATEGORY_COLON_CLEAR:
                    return "COC";
                case CATEGORY_DYED_LIFTED_POLYPS:
                    return "DLP";
                case CATEGORY_DYED_RESECTION_MARGINS:
                    return "DRM";
                case CATEGORY_ESOPHAGITIS:
                    return "ESO";
                case CATEGORY_INSTRUMENTS:
                    return "INS";
                case CATEGORY_NORMAL_CECUM:
                    return "NOC";
                case CATEGORY_NORMAL_PYLORUS:
                    return "NOP";
                case CATEGORY_NORMAL_Z_LINE:
                    return "NZL";
                case CATEGORY_OUT_OF_PATIENT:
                    return "OOP";
                case CATEGORY_POLYPS:
                    return "POL";
                case CATEGORY_RETROFLEX_RECTUM:
                    return "RER";
                case CATEGORY_RETROFLEX_STOMACH:
                    return "RES";
                case CATEGORY_STOOL_INCLUSIONS:
                    return "STI";
                case CATEGORY_STOOL_PLENTY:
                    return "STP";
                case CATEGORY_ULCERATIVE_COLITIS:
                    return "ULC";
                default:
                    return "";
            }
        }

        /**
         * @return aktual name of the category in the dataset (foldername)
         */
        public String getName(){
            return this.name().replaceAll("CATEGORY_","").replaceAll("_","-").toLowerCase();
        }
    }

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
            confusionMatrix[catPred.ordinal()][catGold.ordinal()] = confusionMatrix[catPred.ordinal()][catGold.ordinal()] + 1;
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
        System.out.printf("%5s","P \\ G");
        for(Category cat : Category.values()){
            System.out.printf("%5s",cat.getShortName());
        }
        System.out.println();
        for(int i = 0; i < Category.values().length;i++){
            System.out.printf("%5s",Category.values()[i].getShortName());
            for(int j = 0; j < Category.values().length;j++){
                System.out.printf("%5s", confusionMatrix[i][j] + "");
            }
            System.out.println();
        }
    }

    /**
     * @return String representation of the Matrix (same as {@link MedicoConfusionMatrix#printConfusionMatrix()}  )
     */
    @Override
    public String toString(){
        StringBuilder b = new StringBuilder();
        b.append(String.format("%5s","P \\ G"));
        for(Category cat : Category.values()){
            b.append(String.format("%5s",cat.getShortName()));
        }
        b.append("\n");
        for(int i = 0; i < Category.values().length;i++){
            b.append(String.format("%5s",Category.values()[i].getShortName()));
            for(int j = 0; j < Category.values().length;j++){
                b.append(String.format("%5s", confusionMatrix[i][j] + ""));
            }
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
