package main;

import keras.features.*;
import keras.indexer.KerasIndexer;
import keras.searcher.KerasBitSamplingImageSearcher;
import keras.searcher.KerasMetricSpacesImageSearcher;
import keras.searcher.KerasSearcher;
import main.classifier.ImageSearchHitClassifier;
import net.semanticmetadata.lire.builders.DocumentBuilder;
import net.semanticmetadata.lire.builders.GlobalDocumentBuilder;
import net.semanticmetadata.lire.imageanalysis.features.GlobalFeature;
import net.semanticmetadata.lire.imageanalysis.features.global.CEDD;
import net.semanticmetadata.lire.searchers.ImageSearchHits;
import net.semanticmetadata.lire.utils.FileUtils;
import net.semanticmetadata.lire.utils.LuceneUtils;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Vector;

public class Main {
    private static String indexPath = "/home/michael/master_thesis/data/index";
    private static String folderPath = "/home/michael/master_thesis/data/Medico_2018_development_set/";
    private static Vector<String> files = new Vector<>();
    private static final boolean USE_METRIC_SPACES = true;

    public static void main(String[] args) {

        String inFile = "/home/michael/master_thesis/data/indexCreationFiles/inFileTrain.lst";
        String inFile2 = "/home/michael/master_thesis/data/indexCreationFiles/inFileTest.lst";
//        String outFile1 = "/home/michael/master_thesis/data/indexCreationFiles/out.vgg16.dat";
//        String outFile2 = "/home/michael/master_thesis/data/indexCreationFiles/out.vgg19.dat";
//        String outFile3 = "/home/michael/master_thesis/data/indexCreationFiles/out.cedd.dat";
        Class[] classes = {DenseNet121.class, DenseNet169.class, DenseNet201.class,InceptionV3.class,IncResNetV2.class,MobileNet.class,VGG16.class,VGG19.class,Xception.class};
        String[] names = {"DenseNet121", "DenseNet169", "DenseNet201","InceptionV3","IncResNetV2","MobileNet","VGG16","VGG19","Xception"};
        String[] outFiles = new String[classes.length];
        String[] csvFiles = new String[classes.length];
        for(int i = 0; i < names.length; i++){
            outFiles[i] = "/home/michael/master_thesis/data/indexCreationFiles/out." + names[i] + ".dat";
            csvFiles[i] = "/home/michael/master_thesis/data/csv/" + names[i].toLowerCase() + ".csv";
        }
        if(USE_METRIC_SPACES)//NOTE: MetricSpaces indexing
        {
            try {
                Vector<String> filesToIndex = new Vector<String>(Files.readAllLines(Paths.get(inFile)));
                KerasIndexer indexer = new KerasIndexer(indexPath, true, LuceneUtils.AnalyzerType.WhitespaceAnalyzer, true, GlobalDocumentBuilder.HashingMode.MetricSpaces, false, filesToIndex);
                for(int i = 0; i < classes.length; i++){
                    indexer.addExtractor(classes[i], csvFiles[i]);
                    indexer.indexReferencePoints(classes[i], 500, 20, new File(inFile), new File(outFiles[i]));
                    indexer.loadReferencePoints(outFiles[i]);
                }

//                indexer.addExtractor(VGG19.class, "/home/michael/master_thesis/data/csv/vgg19.csv");
//                indexer.addExtractor(CEDD.class);
//                indexer.indexReferencePoints(VGG19.class, 500, 20, new File(inFile), new File(outFile2));
//                indexer.indexReferencePoints(CEDD.class, 500, 20, new File(inFile), new File(outFile3));
//                indexer.loadReferencePoints(outFile2);
//                indexer.loadReferencePoints(outFile3);
                indexer.index();

            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        else//NOTE: BitSampling indexing
        {
            try {
                KerasIndexer indexer = new KerasIndexer(indexPath, true, LuceneUtils.AnalyzerType.WhitespaceAnalyzer, true, GlobalDocumentBuilder.HashingMode.BitSampling, false, folderPath);
                for(int i = 0; i < classes.length; i++){
                    indexer.addExtractor(classes[i], csvFiles[i]);
                }

                indexer.index();

            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        try {
            files = new Vector<String>(Files.readAllLines(Paths.get(inFile2)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        IndexReader reader = null;
        try {
            reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexPath)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        PrintStream out = null;
        try {
            new File("/home/michael/master_thesis/data/results.txt").createNewFile();
            out = new PrintStream(new File("/home/michael/master_thesis/data/results.txt"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (String s : files) {
            ImageSearchHits hits[] = new ImageSearchHits[classes.length];
//            ImageSearchHits hits2 = null;
//            ImageSearchHits hits3 = null;
            KerasSearcher[] searchers = new KerasSearcher[classes.length];
//            KerasSearcher searcher1 = null;
//            KerasSearcher searcher2 = null;
//            KerasSearcher searcher3 = null;
            if (USE_METRIC_SPACES) //NOTE: Metricspaces searching
            {

                try {
                    for(int i = 0; i < classes.length; i++){
                        searchers[i] = new KerasMetricSpacesImageSearcher(10, new FileInputStream(outFiles[i]), 3, false, reader);
                        ((KerasMetricSpacesImageSearcher)searchers[i]).setNumHashesUsedForQuery(20);
                    }
//                    searcher1 = new KerasMetricSpacesImageSearcher(10, new FileInputStream(outFile1), 3, false, reader);
//                    searcher2 = new KerasMetricSpacesImageSearcher(10, new FileInputStream(outFile2), 3, false, reader);
//                    searcher3 = new KerasMetricSpacesImageSearcher(10, new FileInputStream(outFile3), 3, false, reader);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
//                ((KerasMetricSpacesImageSearcher)searcher2).setNumHashesUsedForQuery(20);
//                ((KerasMetricSpacesImageSearcher)searcher3).setNumHashesUsedForQuery(20);
            }
            else//NOTE: Bitsampling searching
            {
                for(int i = 0; i < classes.length; i++){
                    try {
                        searchers[i] = new KerasBitSamplingImageSearcher(3, (GlobalFeature) classes[i].newInstance());
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
//                searcher1 = new KerasBitSamplingImageSearcher(3, new VGG16());
//                searcher2 = new KerasBitSamplingImageSearcher(3, new VGG19());
//                searcher3 = new KerasBitSamplingImageSearcher(3, new CEDD());

            }
            for(int i = 0; i< hits.length; i++){

                hits[i] = searchers[i].search(s, reader);
            }
//            hits2 = searcher2.search(s, reader);
//            hits3 = searcher3.search(s, reader);
            if(out != null){
                out.println(s);
            }
            System.out.println(s);
            for(int i = 0; i< hits.length; i++) {
                printResults(hits[i], reader, names[i], out,s);

            }
//            printResults(hits2,reader,"VGG19",out);
//            printResults(hits3,reader,"CEDD",out);

        }
    }

    public static void printResults(ImageSearchHits hits,IndexReader reader,String featurename,PrintStream out,String fname) {
        Vector<String> hitsStrings = new Vector<>();
        for (int i = 0; i < hits.length(); i++) {
            String filename = null;
            try {
                filename = reader.document(hits.documentID(i)).getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0];
            } catch (IOException e) {
                e.printStackTrace();
            }
            hitsStrings.add(filename);
            ImageSearchHitClassifier classifier = new ImageSearchHitClassifier(new Vector(Arrays.asList(new String[]{"blurry-nothing", "colon-clear", "dyed-lifted-polyps", "dyed-resection-margins", "esophagitis", "instruments", "normal-cecum", "normal-pylorus", "normal-z-line", "out-of-patient", "polyps", "retroflex-rectum", "retroflex-stomach", "stool-inclusions", "stool-plenty", "ulcerative-colitis"}))
                    , hitsStrings, fname);
            HashMap<String, Double> preds = classifier.getPredictions();

            System.out.println(fname);
            if (out != null) {
                out.println(fname);
            }
            for (String k : preds.keySet()) {
                System.out.println(k + preds.get(k));
                if (out != null) {
                    out.println(k + preds.get(k));
                }
            }
        }
    }
}






