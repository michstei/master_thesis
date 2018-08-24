package main;

import keras.documentbuilder.KerasDocumentBuilder;
import keras.features.*;
import keras.indexer.KerasIndexer;
import keras.searcher.KerasBitSamplingImageSearcher;
import keras.searcher.KerasMetricSpacesImageSearcher;
import keras.searcher.KerasSearcher;
import keras.searcher.SearchRunnable;
import main.classifier.ImageSearchHitClassifier;
import net.semanticmetadata.lire.builders.DocumentBuilder;
import net.semanticmetadata.lire.builders.GlobalDocumentBuilder;
import net.semanticmetadata.lire.imageanalysis.features.GlobalFeature;
import net.semanticmetadata.lire.indexers.hashing.BitSampling;
import net.semanticmetadata.lire.searchers.ImageSearchHits;
import net.semanticmetadata.lire.utils.LuceneUtils;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Vector;

public class Main {

    public static void main(String[] args) {
        boolean USE_METRIC_SPACES = false;

        String folderPath = "/home/michael/master_thesis/data/Medico_2018_development_set/";
        String indexPath = USE_METRIC_SPACES?"/home/michael/master_thesis/data/indexMetricSpaces":"/home/michael/master_thesis/data/indexBitSampling";
        String outputFilePath = USE_METRIC_SPACES?"/home/michael/master_thesis/data/MetricSpacesResults.txt":"/home/michael/master_thesis/data/BitSamplingResults.txt";
        String inFileTrain = "/home/michael/master_thesis/data/indexCreationFiles/inFileTrain.lst";
        String inFileTest = "/home/michael/master_thesis/data/indexCreationFiles/inFileTest.lst";

        FilePrep prep = new FilePrep(folderPath,5,inFileTrain,inFileTest);
        prep.writeSetFiles();

        Vector<String> trainFiles = null;
        try {
            trainFiles = new Vector<>(Files.readAllLines(Paths.get(inFileTrain)));
        } catch (IOException e) {
            e.printStackTrace();
        }

        Vector<String> testFiles = new Vector<>();
        try {
            testFiles = new Vector<>(Files.readAllLines(Paths.get(inFileTest)));
        } catch (IOException e) {
            e.printStackTrace();
        }

        Vector<String> allClasses = new Vector(Arrays.asList(new String[]{"blurry-nothing", "colon-clear", "dyed-lifted-polyps", "dyed-resection-margins", "esophagitis", "instruments", "normal-cecum", "normal-pylorus", "normal-z-line", "out-of-patient", "polyps", "retroflex-rectum", "retroflex-stomach", "stool-inclusions", "stool-plenty", "ulcerative-colitis"}));
        Class[] classes = {DenseNet121.class, DenseNet169.class, DenseNet201.class,InceptionV3.class,IncResNetV2.class,MobileNet.class,VGG16.class,VGG19.class,Xception.class};
        String[] names = {"DenseNet121", "DenseNet169", "DenseNet201","InceptionV3","IncResNetV2","MobileNet","VGG16","VGG19","Xception"};
        String[] outFiles = new String[classes.length];
        String[] csvFiles = new String[classes.length];
        for(int i = 0; i < names.length; i++){
            outFiles[i] = "/home/michael/master_thesis/data/indexCreationFiles/out." + names[i] + ".dat";
            csvFiles[i] = "/home/michael/master_thesis/data/csv/" + names[i].toLowerCase() + ".csv";
        }

        index(USE_METRIC_SPACES, indexPath, inFileTrain, classes, outFiles, csvFiles, trainFiles);
        IndexReader reader = null;
        try {
            reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexPath)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        PrintStream out = null;
        try {
          out = new PrintStream(new File(outputFilePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
        int total = 0;
        int correct = 0;
        int incorrect = 0;
        StringBuilder outStringBuilder = new StringBuilder();
        int counter = 1;
        for (String s : testFiles) {
            ImageSearchHits hits[] = new ImageSearchHits[classes.length];

            KerasSearcher[] searchers = new KerasSearcher[classes.length];

            setupSearchers(USE_METRIC_SPACES, classes, outFiles, reader, searchers);

            Thread[] threads = new Thread[classes.length];
            SearchRunnable[] runnables = new SearchRunnable[classes.length];
            for(int i = 0; i< hits.length; i++){
                runnables[i] = new SearchRunnable(searchers[i],reader,s);
                threads[i] = new Thread(runnables[i]);
                threads[i].start();
            }

            for(int i = 0; i < hits.length; i++){
                try {
                    threads[i].join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                hits[i] = runnables[i].getResult();
            }


            LinkedHashMap<String, Double> preds = getResults(hits, reader,s,allClasses);
            outStringBuilder.append(s + System.lineSeparator());
            int count = 0;
            for (String k : preds.keySet()) {
                if (count == 0) {
                    if (s.contains(k)) {
                        correct++;
                    } else {
                        incorrect++;
                    }
                    total++;
                }
                if(count < 3) {
                    outStringBuilder.append(k + "\t" + preds.get(k) + System.lineSeparator());
                }
                else{
                    break;
                }
                count ++;
            }


            System.out.printf("processed file %d of %d",counter++, testFiles.size());
        }
        outStringBuilder.append(String.format("total: %d\ncorrect: %d\nincorrect: %d\ncorrect Pcnt: %.2f%%\n",total,correct,incorrect,(correct/(float)total)*100 ));
        out.println(outStringBuilder.toString());
        System.out.println(String.format("total: %d\ncorrect: %d\nincorrect: %d\ncorrect Pcnt: %.2f%%\n",total,correct,incorrect,(correct/(float)total)*100 ));
    }

    private static void setupSearchers(boolean useMetricSpaces, Class[] classes, String[] outFiles, IndexReader readers, KerasSearcher[] searchers) {
        if (useMetricSpaces) //NOTE: Metricspaces searching
        {

            try {
                for(int i = 0; i < classes.length; i++){
                    searchers[i] = new KerasMetricSpacesImageSearcher(10, new FileInputStream(outFiles[i]), 3, false, readers);
                    ((KerasMetricSpacesImageSearcher)searchers[i]).setNumHashesUsedForQuery(20);
                }

            } catch (IllegalAccessException | InstantiationException | FileNotFoundException | ClassNotFoundException e) {
                e.printStackTrace();
            }

        }
        else//NOTE: Bitsampling searching
        {
            try {
                if(!(new File(KerasDocumentBuilder.hashFilePath).exists())){
                    BitSampling.dimensions = KerasDocumentBuilder.maxDimensions;
                    BitSampling.generateHashFunctions(KerasDocumentBuilder.hashFilePath);
                }
                BitSampling.readHashFunctions(new FileInputStream(KerasDocumentBuilder.hashFilePath));
            } catch (IOException e) {
                e.printStackTrace();
            }
            for(int i = 0; i < classes.length; i++){
                try {
                    searchers[i] = new KerasBitSamplingImageSearcher(3, (GlobalFeature) classes[i].newInstance());
                } catch (InstantiationException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }


        }
    }

    private static void index(boolean useMetricSpaces, String indexPath, String inFile, Class[] classes, String[] outFiles, String[] csvFiles, Vector<String> filesToIndex) {
        if(useMetricSpaces)//NOTE: MetricSpaces indexing
        {
            try {
                KerasIndexer indexer = new KerasIndexer(indexPath, true, LuceneUtils.AnalyzerType.WhitespaceAnalyzer, true, GlobalDocumentBuilder.HashingMode.MetricSpaces, false, filesToIndex);
                for (int i = 0; i < classes.length; i++) {
                    indexer.addExtractor(classes[i], csvFiles[i]);
                    indexer.indexReferencePoints(classes[i], 500, 20, new File(inFile), new File(outFiles[i]));
                    indexer.loadReferencePoints(outFiles[i]);
                }
                indexer.index();

            } catch (IllegalAccessException | IOException | ClassNotFoundException | InstantiationException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
            }
        } else//NOTE: BitSampling indexing
        {
            try {
                KerasIndexer indexer = new KerasIndexer(indexPath, true, LuceneUtils.AnalyzerType.WhitespaceAnalyzer, true, GlobalDocumentBuilder.HashingMode.BitSampling, false, filesToIndex);
                for(int i = 0; i < classes.length; i++){
                    indexer.addExtractor(classes[i], csvFiles[i]);
                }

                indexer.index();

            } catch (IllegalAccessException | IOException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
    }

    public static  LinkedHashMap<String, Double> getResults(ImageSearchHits[] hits, IndexReader reader, String fname,Vector<String> allClasses) {
        Vector<String> hitsStrings = new Vector<>();
        for(int j = 0; j < hits.length; j++){
            for (int i = 0; i < hits[j].length(); i++) {
                String filename = null;
                try {
                    filename = reader.document(hits[j].documentID(i)).getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0];
                } catch (IOException e) {
                    e.printStackTrace();
                }
                hitsStrings.add(filename);


            }
        }
        ImageSearchHitClassifier classifier = new ImageSearchHitClassifier(allClasses
                , hitsStrings, fname);
        return (LinkedHashMap<String, Double>) classifier.getPredictions();
    }
}






