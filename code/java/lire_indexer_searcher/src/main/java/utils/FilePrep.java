package utils;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Vector;

/**
 *  class to prepare the developmentset for training and testing (splitting it up)
 */
public class FilePrep {
    private String folderPath;

    private Vector<String> train = new Vector<>();

    private Vector<String> test = new Vector<>();
    private String inFileTrain;
    private String inFileTest;
    private int testPercent;
    public FilePrep(String folderPath, int testPercent, String trainFilePath, String testFilePath) {
        this.folderPath = folderPath;
        this.testPercent = testPercent;
        this.inFileTrain = trainFilePath;
        this.inFileTest = testFilePath;
        if(!(new File(new File(this.inFileTest).getParent()).exists())){
            new File(new File(this.inFileTest).getParent()).mkdirs();
        }
        if(!(new File(new File(this.inFileTrain).getParent()).exists())){
            new File(new File(this.inFileTrain).getParent()).mkdirs();
        }
        divide();
    }

    /***
     * divides the dataset into test and train set according to testPercent
     */
    private  void divide(){
        Vector<String> all = new Vector<>();
        listFilesFromDirectory(folderPath,all);

        for(MedicoConfusionMatrix.Category c : MedicoConfusionMatrix.Category.values()){
            test.addAll(getFromCategory(c,testPercent));
        }
        for(String s : all){
            if(!test.contains(s))
                train.add(s);
        }
        System.out.printf("Total Files: %d\nTraining Files: %d\nTesting Files: %d\n",all.size(),train.size(),test.size());
    }

    /***
     * adds one file from each category/folder to test
     * @param test      list of files used for testing NOT NULL
     * @param values    array of Categories from which files are chosen
     */
    private void addOneForEachCategory(Vector<String> test, MedicoConfusionMatrix.Category[] values) {
        for(MedicoConfusionMatrix.Category c : values){
            ArrayList<String> files = new ArrayList<>();
            listFilesFromDirectory(folderPath+c.getName() + "/",files);
            Random gen = new Random(System.nanoTime());
            test.add(files.get(gen.nextInt(files.size())));
        }
    }

    /***
     * returns pcnt files (min 1) from Category cat as Vector , randomly chosen
     * @param cat   Category from which files are chosen
     * @param pcnt  Percentage of files to choose
     * @return list of chosen file (min 1 file, max pcnt% files)
     */
    private Vector<String>  getFromCategory(MedicoConfusionMatrix.Category cat, int pcnt) {
        ArrayList<String> files = new ArrayList<>();
        listFilesFromDirectory(folderPath+cat.getName() + "/",files);
        int num = Math.max(1,(int) (files.size() * (pcnt/100.0) + 0.5));
        Random gen = new Random(System.nanoTime());
        Vector<String> test = new Vector<>();
        while (test.size()<num){
            String val = files.get(gen.nextInt(files.size()-1));
            if(!test.contains(val))
                test.add(val);

        }
        return test;
    }

    /***
     * gets all filepaths in given directory (recursivly) and adds them to list
     * @param directoryName path to directory
     * @param files list to which filepaths are added
     */
    private void listFilesFromDirectory(String directoryName, List<String> files) {
        File directory = new File(directoryName);

        // Get all the files from a directory.
        File[] fList = directory.listFiles();
        for (File file : fList) {
            if (file.isFile()) {
                files.add(file.getPath());
            } else if (file.isDirectory()) {
                listFilesFromDirectory(file.getPath(), files);
            }
        }
    }

    /***
     * writes the testset to file inFileTest
     */
    public void writeTestSetFile(){
        try {
            if(!(new File(inFileTest).exists())){
                new File(inFileTest).createNewFile();
            }
            FileUtils.writeLines(new File(inFileTest),test);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /***
     * writes the trainset to file inFileTrain
     */
    public void writeTrainSetFile(){
        try {
            if(!(new File(inFileTrain).exists())){
                new File(inFileTrain).createNewFile();
            }
            FileUtils.writeLines(new File(inFileTrain),train);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /***
     * writes the test and train sets to file
     */
    public void writeSetFiles(){
        writeTrainSetFile();
        writeTestSetFile();
    }
    public String getFolderPath() {
        return folderPath;
    }

    public Vector<String> getTrain() {
        return train;
    }

    public Vector<String> getTest() {
        return test;
    }

    public String getInFileTrain() {
        return inFileTrain;
    }

    public String getInFileTest() {
        return inFileTest;
    }

    public int getTestPercent() {
        return testPercent;
    }


    public static void main(String[] args) {
        String folderPath = "/home/michael/master_thesis/data/Medico_2018_development_set/";
        String inFileTrain = "/home/michael/master_thesis/data/indexCreationFiles/inFileTrain.lst";
        String inFileTest = "/home/michael/master_thesis/data/indexCreationFiles/inFileTest.lst";
        FilePrep prep = new FilePrep(folderPath,10,inFileTrain,inFileTest);
        prep.writeSetFiles();

    }
}
