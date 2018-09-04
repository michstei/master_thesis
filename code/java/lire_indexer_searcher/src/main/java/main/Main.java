package main;

import keras.documentbuilder.KerasDocumentBuilder;
import keras.features.*;
import keras.features.quantized.*;
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
import org.apache.commons.io.FileUtils;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Vector;

public class Main {

    public static Class[] classes_double = {DenseNet121_Double.class,DenseNet169_Double.class,DenseNet201_Double.class,InceptionV3_Double.class,IncResNetV2_Double.class,ResNet50_Double.class,MobileNet_Double.class,VGG16_Double.class,VGG19_Double.class,Xception_Double.class};
    public static String[] classNames_double = {"DenseNet121_Double","DenseNet169_Double","DenseNet201_Double","InceptionV3_Double","IncResNetV2_Double","ResNet50_Double","MobileNet_Double","VGG16_Double","VGG19_Double","Xception_Double"};
    public static Class[] classes_float = {DenseNet121_Float.class,DenseNet169_Float.class,DenseNet201_Float.class,InceptionV3_Float.class,IncResNetV2_Float.class,ResNet50_Float.class,MobileNet_Float.class,VGG16_Float.class,VGG19_Float.class,Xception_Float.class};
    public static String[] classNames_float = {"DenseNet121_Float","DenseNet169_Float","DenseNet201_Float","InceptionV3_Float","IncResNetV2_Float","ResNet50_Float","MobileNet_Float","VGG16_Float","VGG19_Float","Xception_Float"};
    public static Class[] classes_long = {DenseNet121_Long.class,DenseNet169_Long.class,DenseNet201_Long.class,InceptionV3_Long.class,IncResNetV2_Long.class,ResNet50_Long.class,MobileNet_Long.class,VGG16_Long.class,VGG19_Long.class,Xception_Long.class};
    public static String[] classNames_long = {"DenseNet121_Long","DenseNet169_Long","DenseNet201_Long","InceptionV3_Long","IncResNetV2_Long","ResNet50_Long","MobileNet_Long","VGG16_Long","VGG19_Long","Xception_Long"};
    public static Class[] classes_int = {DenseNet121_Int.class,DenseNet169_Int.class,DenseNet201_Int.class,InceptionV3_Int.class,IncResNetV2_Int.class,ResNet50_Int.class, MobileNet_Int.class, VGG16_Int.class, VGG19_Int.class, Xception_Int.class};
    public static String[] classNames_int = {"DenseNet121_Int", "DenseNet169_Int", "DenseNet201_Int", "InceptionV3_Int", "IncResNetV2_Int", "ResNet50_Int", "MobileNet_Int", "VGG16_Int", "VGG19_Int", "Xception_Int"};
    public static Class[] classes_short = {DenseNet121_Short.class, DenseNet169_Short.class, DenseNet201_Short.class, InceptionV3_Short.class, IncResNetV2_Short.class, ResNet50_Short.class, MobileNet_Short.class, VGG16_Short.class, VGG19_Short.class, Xception_Short.class};
    public static String[] classNames_short = {"DenseNet121_Short", "DenseNet169_Short", "DenseNet201_Short", "InceptionV3_Short", "IncResNetV2_Short", "ResNet50_Short", "MobileNet_Short", "VGG16_Short", "VGG19_Short", "Xception_Short"};
    public static Class[] classes_byte = {DenseNet121_Byte.class, DenseNet169_Byte.class, DenseNet201_Byte.class, InceptionV3_Byte.class, IncResNetV2_Byte.class, ResNet50_Byte.class, MobileNet_Byte.class, VGG16_Byte.class, VGG19_Byte.class, Xception_Byte.class};
    public static String[] classNames_byte = {"DenseNet121_Byte", "DenseNet169_Byte", "DenseNet201_Byte", "InceptionV3_Byte", "IncResNetV2_Byte", "ResNet50_Byte", "MobileNet_Byte", "VGG16_Byte", "VGG19_Byte", "Xception_Byte"};

    public static void main(String[] args) throws Exception {
        boolean USE_METRIC_SPACES = false;

        String imageFolderPath = "/home/michael/master_thesis/data/Medico_2018_development_set/";
        String indexPath = USE_METRIC_SPACES?"/home/michael/master_thesis/data/indexMetricSpaces":"/home/michael/master_thesis/data/indexBitSampling";
        String outputFolderPath = "/home/michael/master_thesis/data/results/keras_imageNetWeights_noExtraPoolingLayers_longFeatureVectors/";
        String outputFilePathBase = USE_METRIC_SPACES ? outputFolderPath + "MetricSpacesResults_" : outputFolderPath + "BitSamplingResults_";
        String inFileTrain = "/home/michael/master_thesis/data/indexCreationFiles/inFileTrain.lst";
        String inFileTest = "/home/michael/master_thesis/data/indexCreationFiles/inFileTest.lst";

//        FilePrep prep = new FilePrep(imageFolderPath,5,inFileTrain,inFileTest);
//        prep.writeSetFiles();

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

        Vector<String> allCategories = new Vector<>(Arrays.asList("blurry-nothing", "colon-clear", "dyed-lifted-polyps", "dyed-resection-margins", "esophagitis", "instruments", "normal-cecum", "normal-pylorus", "normal-z-line", "out-of-patient", "polyps", "retroflex-rectum", "retroflex-stomach", "stool-inclusions", "stool-plenty", "ulcerative-colitis"));

        Class[] classes = classes_double;
        String[] classNames = classNames_double;
        if(classes.length > classNames.length) throw new Exception("More classes than classNames defined!");
        String[] outFiles = new String[classes.length];
        String[] csvFiles = new String[classes.length];
        for(int i = 0; i < classes.length; i++){
            outFiles[i] = "/home/michael/master_thesis/data/indexCreationFiles/out." + classNames[i] + ".dat";
            csvFiles[i] = "/home/michael/master_thesis/data/csv/quantized/" + classNames[i].toLowerCase() + "_long.csv";
        }

        index(USE_METRIC_SPACES, indexPath, inFileTrain, classes, outFiles, csvFiles, trainFiles);

        IndexReader[] readers = new IndexReader[classes.length];
        try {
            for( int i = 0; i < readers.length; i++) {
                readers[i] = DirectoryReader.open(FSDirectory.open(Paths.get(indexPath)));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        for(KerasFeature.DistanceFunction df : KerasFeature.DistanceFunction.values()) {
            DenseNet121_Double.USED_DISTANCE_FUN = df;
            DenseNet169_Double.USED_DISTANCE_FUN = df;
            DenseNet201_Double.USED_DISTANCE_FUN = df;
            InceptionV3_Double.USED_DISTANCE_FUN = df;
            IncResNetV2_Double.USED_DISTANCE_FUN = df;
            MobileNet_Double.USED_DISTANCE_FUN =   df;
            ResNet50_Double.USED_DISTANCE_FUN =    df;
            VGG16_Double.USED_DISTANCE_FUN =       df;
            VGG19_Double.USED_DISTANCE_FUN =       df;
            Xception_Double.USED_DISTANCE_FUN =    df;
            String outputFilePath = outputFilePathBase + df.name() + ".txt";
            PrintStream out = null;
            try {
                out = new PrintStream(new File(outputFilePath));
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println(df.name() + ":");
            int total = 0;
            int correct = 0;
            int incorrect = 0;
            int counter = 1;
            KerasSearcher[] searchers = new KerasSearcher[classes.length];

            setupSearchers(USE_METRIC_SPACES, classes, outFiles, readers, searchers);
            ImageSearchHits hits[] = new ImageSearchHits[classes.length];
            Thread[] threads = new Thread[classes.length];
            SearchRunnable[] runnables = new SearchRunnable[classes.length];
            for (String s : testFiles) {


                for (int i = 0; i < hits.length; i++) {
                    runnables[i] = new SearchRunnable(searchers[i], readers[i], s);
                    threads[i] = new Thread(runnables[i]);
                    threads[i].start();
                }

                for (int i = 0; i < hits.length; i++) {
                    try {
                        threads[i].join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    hits[i] = runnables[i].getResult();
                }


                LinkedHashMap<String, Double> preds = getResults(hits, readers, s, allCategories, classNames);
                String k = preds.entrySet().iterator().next().getKey();
                if (s.contains(k)) {
                    correct++;
                } else {
                    incorrect++;
                }
                total++;


                System.out.printf("\rprocessed file %4s of %d", (counter++) + "", testFiles.size());
            }
            System.out.println();

            out.println(String.format("total: %d\ncorrect: %d\nincorrect: %d\ncorrect Pcnt: %.2f%%\n", total, correct, incorrect, (correct / (float) total) * 100));
            System.out.println(String.format("total: %d\ncorrect: %d\nincorrect: %d\ncorrect Pcnt: %.2f%%\n", total, correct, incorrect, (correct / (float) total) * 100));
        }
    }

    private static void setupSearchers(boolean useMetricSpaces, Class[] classes, String[] outFiles, IndexReader[] readers, KerasSearcher[] searchers) {
        if (useMetricSpaces) //NOTE: MetricSpaces searching
        {

            try {
                for(int i = 0; i < classes.length; i++){
                    searchers[i] = new KerasMetricSpacesImageSearcher(10, new FileInputStream(outFiles[i]), 3, false, readers[i]);
                    ((KerasMetricSpacesImageSearcher)searchers[i]).setNumHashesUsedForQuery(20);
                }

            } catch (IllegalAccessException | InstantiationException | FileNotFoundException | ClassNotFoundException e) {
                e.printStackTrace();
            }

        }
        else//NOTE: BitSampling searching
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

    public static  LinkedHashMap<String, Double> getResults(ImageSearchHits[] hits, IndexReader[] readers, String fname,Vector<String> allCategories, String[] classNames) {
        Vector<String> hitsStrings = new Vector<>();
        StringBuilder builder = new StringBuilder();
        builder.append(fname + "\n");
        for(int j = 0; j < hits.length; j++){
            for (int i = 0; i < hits[j].length(); i++) {
                String filename = null;
                try {
                    filename = readers[j].document(hits[j].documentID(i)).getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0];

                } catch (IOException e) {
                    e.printStackTrace();
                }
                hitsStrings.add(filename);

                builder.append(classNames[j]);
                boolean hit = false;
                for(String c : allCategories){
                    if(fname.contains(c) && filename.contains(c)){
                        hit = true;
                        break;
                    }
                }
                builder.append(hit?" (true) : ":" (false) : ");
                builder.append(filename + "\n");

            }
        }
        try {
            FileUtils.writeStringToFile(new File("/home/michael/master_thesis/data/results/best_result_per_feature.txt"),builder.toString(), (Charset) null,true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ImageSearchHitClassifier classifier = new ImageSearchHitClassifier(allCategories, hitsStrings);
        return (LinkedHashMap<String, Double>) classifier.getPredictions();
    }
}






