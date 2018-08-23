package main;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Vector;

public class FIlePrep {
    public static void main(String[] args) {
        String folderPath = "/home/michael/master_thesis/data/Medico_2018_development_set/";
        Vector<String> allfiles = new Vector<>();
        Vector<String> trainfiles = new Vector<>();
        Vector<String> testfiles = new Vector<>();
        String inFileTrain = "/home/michael/master_thesis/data/indexCreationFiles/inFileTrain.lst";
        String inFileTest = "/home/michael/master_thesis/data/indexCreationFiles/inFileTest.lst";

        listFilesFromDirectory(folderPath,allfiles);
        divide(allfiles,trainfiles,testfiles,5);
        System.out.printf("all: %d, train: %d, test: %d\n",allfiles.size(),trainfiles.size(),testfiles.size());

        try {
            FileUtils.writeLines(new File(inFileTrain),trainfiles);
            FileUtils.writeLines(new File(inFileTest),testfiles);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static void divide(Vector<String> all,Vector<String> train, Vector<String> test,int p){
        for(int i = 0; i < all.size();i++){
            if(i % p==0){
                test.add(all.get(i));
            }
            else{
                train.add(all.get(i));
            }
        }
    }

    private static void listFilesFromDirectory(String directoryName, List<String> files) {
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
}
