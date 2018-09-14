package main;

import keras.documentbuilder.KerasDocumentBuilderImpl;
import keras.features.*;
import keras.features.quantized.*;
import keras.indexer.KerasIndexer;
import keras.searcher.KerasBitSamplingImageSearcher;
import keras.searcher.KerasMetricSpacesImageSearcher;
import keras.searcher.KerasSearcher;
import keras.searcher.SearchRunnable;
import net.semanticmetadata.lire.indexers.hashing.MetricSpaces;
import utils.FilePrep;
import utils.MedicoConfusionMatrix;
import classifier.ImageSearchHitClassifier;
import net.semanticmetadata.lire.builders.DocumentBuilder;
import net.semanticmetadata.lire.builders.GlobalDocumentBuilder;
import net.semanticmetadata.lire.imageanalysis.features.GlobalFeature;
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
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Vector;

/***
 * class to perform experiments for task
 */
public class Main {

    private static Class[]  all_classes_double =    {   DenseNet121_Double.class,  DenseNet169_Double.class,   DenseNet201_Double.class,   /*InceptionV3_Double.class,*/   /*IncResNetV2_Double.class,   */ResNet50_Double.class,  MobileNet_Double.class, VGG16_Double.class, VGG19_Double.class, Xception_Double.class   };
    private static String[] all_classNames_double = {   "DenseNet121_Double",      "DenseNet169_Double",       "DenseNet201_Double",       /*"InceptionV3_Double",*/       /*"IncResNetV2_Double",       */"ResNet50_Double",      "MobileNet_Double",     "VGG16_Double",     "VGG19_Double",     "Xception_Double"       };
    private static Class[]  all_classes_float =     {   DenseNet121_Float.class,   DenseNet169_Float.class,    DenseNet201_Float.class,    /*InceptionV3_Float.class,*/    /*IncResNetV2_Float.class,    */ResNet50_Float.class,   MobileNet_Float.class,  VGG16_Float.class,  VGG19_Float.class,  Xception_Float.class    };
    private static String[] all_classNames_float =  {   "DenseNet121_Float",       "DenseNet169_Float",        "DenseNet201_Float",        /*"InceptionV3_Float",*/        /*"IncResNetV2_Float",        */"ResNet50_Float",       "MobileNet_Float",      "VGG16_Float",      "VGG19_Float",      "Xception_Float"        };
    private static Class[]  all_classes_long =      {   DenseNet121_Long.class,    DenseNet169_Long.class,     DenseNet201_Long.class,     /*InceptionV3_Long.class,*/     /*IncResNetV2_Long.class,     */ResNet50_Long.class,    MobileNet_Long.class,   VGG16_Long.class,   VGG19_Long.class,   Xception_Long.class     };
    private static String[] all_classNames_long =   {   "DenseNet121_Long",        "DenseNet169_Long",         "DenseNet201_Long",         /*"InceptionV3_Long",*/         /*"IncResNetV2_Long",         */"ResNet50_Long",        "MobileNet_Long",       "VGG16_Long",       "VGG19_Long",       "Xception_Long"         };
    private static Class[]  all_classes_int =       {   DenseNet121_Int.class,     DenseNet169_Int.class,      DenseNet201_Int.class,      /*InceptionV3_Int.class,*/      /*IncResNetV2_Int.class,      */ResNet50_Int.class,     MobileNet_Int.class,    VGG16_Int.class,    VGG19_Int.class,    Xception_Int.class      };
    private static String[] all_classNames_int =    {   "DenseNet121_Int",         "DenseNet169_Int",          "DenseNet201_Int",          /*"InceptionV3_Int", */         /*"IncResNetV2_Int",          */"ResNet50_Int",         "MobileNet_Int",        "VGG16_Int",        "VGG19_Int",        "Xception_Int"          };
    private static Class[]  all_classes_short =     {   DenseNet121_Short.class,   DenseNet169_Short.class,    DenseNet201_Short.class,    /*InceptionV3_Short.class,*/    /*IncResNetV2_Short.class,    */ResNet50_Short.class,   MobileNet_Short.class,  VGG16_Short.class,  VGG19_Short.class,  Xception_Short.class    };
    private static String[] all_classNames_short =  {   "DenseNet121_Short",       "DenseNet169_Short",        "DenseNet201_Short",        /*"InceptionV3_Short", */       /*"IncResNetV2_Short",        */"ResNet50_Short",       "MobileNet_Short",      "VGG16_Short",      "VGG19_Short",      "Xception_Short"        };
    private static Class[]  all_classes_byte =      {   DenseNet121_Byte.class,    DenseNet169_Byte.class,     DenseNet201_Byte.class,     /*InceptionV3_Byte.class,*/     /*IncResNetV2_Byte.class,     */ResNet50_Byte.class,    MobileNet_Byte.class,   VGG16_Byte.class,   VGG19_Byte.class,   Xception_Byte.class     };
    private static String[] all_classNames_byte =   {   "DenseNet121_Byte",        "DenseNet169_Byte",         "DenseNet201_Byte",         /*"InceptionV3_Byte",*/         /*"IncResNetV2_Byte",         */"ResNet50_Byte",        "MobileNet_Byte",       "VGG16_Byte",       "VGG19_Byte",       "Xception_Byte"         };




    enum HashingMode{
        HASHING_MODE_METRIC_SPACES,
        HASHING_MODE_BITSAMPLING,


    }

    private static int maxHits = 3;
    private static String basePath = "/home/michael/master_thesis/data/";
    private static String inFileTrain = basePath + "indexCreationFiles/inFileTrain.lst";
    private static String inFileTest = basePath + "indexCreationFiles/inFileTest.lst";
    private static String imageFolderPath = basePath + "Medico_2018_development_set/";
    private static Vector<String> allCategories = new Vector<>(Arrays.asList("blurry-nothing", "colon-clear", "dyed-lifted-polyps", "dyed-resection-margins", "esophagitis", "instruments", "normal-cecum", "normal-pylorus", "normal-z-line", "out-of-patient", "polyps", "retroflex-rectum", "retroflex-stomach", "stool-inclusions", "stool-plenty", "ulcerative-colitis"));

    public static void main(String[] args) throws Exception {

        Instant startAll = Instant.now();
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
            String csvBasePath = basePath + "csv/lessLayers512/" ;
            String outFileBasePath = basePath + "indexCreationFiles/" + "lessLayers512/";
            String outputFolderPath =  basePath + "results/" + "lessLayers512/byte/";
            String[] indexPath = {basePath + "index/" + "lessLayers/" + "MetricSpaces/" , basePath + "index/" + "lessLayers/"+ "BitSampling/"};
            KerasDocumentBuilderImpl.maxDimensions = 512;

            Class[] classes =        all_classes_byte;
            String[] classNames = all_classNames_byte;

            if (!(new File(outputFolderPath).exists())) {
                new File(outputFolderPath).mkdirs();
            }
            String outputFilePathBase = outputFolderPath + "results_";






            String[] outFiles = new String[classes.length];
            String[] csvFiles = new String[classes.length];
            for (int i = 0; i < classes.length; i++) {
                if (!(new File(outFileBasePath).exists()))
                    new File(outFileBasePath).mkdirs();
                outFiles[i] = outFileBasePath + "out." + classNames[i] + ".dat";
                csvFiles[i] = csvBasePath + "quantized/" + classNames[i].toLowerCase() + ".csv";
            }


            index(HashingMode.HASHING_MODE_METRIC_SPACES, indexPath[0], inFileTrain, classes, outFiles, csvFiles, trainFiles);
            index(HashingMode.HASHING_MODE_BITSAMPLING, indexPath[1], inFileTrain, classes, outFiles, csvFiles, trainFiles);

            IndexReader[] readers = new IndexReader[classes.length * 2];
            try {
                for (int i = 0; i < classes.length; i++) {
                    readers[i] = DirectoryReader.open(FSDirectory.open(Paths.get(indexPath[0])));
                }
                for (int i = classes.length; i < readers.length; i++) {
                    readers[i] = DirectoryReader.open(FSDirectory.open(Paths.get(indexPath[1])));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            KerasSearcher[] searchers = new KerasSearcher[classes.length*2];

            setupSearchers(HashingMode.HASHING_MODE_METRIC_SPACES, classes, outFiles, readers, searchers,0, maxHits);
            setupSearchers(HashingMode.HASHING_MODE_BITSAMPLING, classes, outFiles, readers, searchers, classes.length,maxHits);
            ImageSearchHits hits[] = new ImageSearchHits[searchers.length];
            Thread[] threads = new Thread[searchers.length];
            SearchRunnable[] runnables = new SearchRunnable[searchers.length];

            for (KerasFeature.DistanceFunction df : KerasFeature.DistanceFunction.values()) {
                if (!(df == KerasFeature.DistanceFunction.DISTANCEFUNCTION_COSINE)) {
                    continue;
                }
                Instant startDf = Instant.now();
                for (Class c : classes) {
                    c.getDeclaredField("USED_DISTANCE_FUN").set(null, df);
                }

                String outputFilePath = outputFilePathBase + df.name() + ".txt";
                PrintStream out = null;
                try {

                    if (!(new File(outputFilePath).exists())) {
                        new File(outputFilePath).createNewFile();
                    }
                    out = new PrintStream(new File(outputFilePath));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println(df.name() + ":");

                int counter = 1;

                MedicoConfusionMatrix matrix = new MedicoConfusionMatrix();
                for (String s : testFiles) {
                    Instant st = Instant.now();

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


                    LinkedHashMap<String, Double> preds = getResults(hits, readers, s, allCategories, classNames, outputFolderPath + df.name() + "_");
                    String k = preds.entrySet().iterator().next().getKey();

                    MedicoConfusionMatrix.Category catGold = null, catPred = null;
                    for (MedicoConfusionMatrix.Category c : MedicoConfusionMatrix.Category.values()) {
                        if (s.contains("/"+c.getName()+"/")) {
                            catGold = c;
                        }
                        if (k.equals(c.getName())) {
                            catPred = c;
                        }
                    }

                    matrix.increaseValue(catGold, catPred);

                    Instant e = Instant.now();
                    System.out.printf("\rprocessed file %4s of %d in %s", (counter++) + "", testFiles.size(), Duration.between(st, e));
                }
                System.out.println();
                Instant endDf = Instant.now();
                out.println(String.format("total: %d\ncorrect: %d\nincorrect: %d\ncorrect Pcnt: %.2f%%\n", matrix.getTotal(), matrix.getCorrect(), matrix.getIncorrect(), matrix.getCorrectPcnt()));
                System.out.println(String.format("total: %d\ncorrect: %d\nincorrect: %d\ncorrect Pcnt: %.2f%%\n%s", matrix.getTotal(), matrix.getCorrect(), matrix.getIncorrect(), matrix.getCorrectPcnt(), Duration.between(startDf, endDf)));
                out.println(matrix.toString());
                matrix.printConfusionMatrix();
            }
            for (Class c : classes) {
                try {
                    c.getDeclaredField("reader").set(null, null);
                }
                catch(Exception e){//catch and ignore exception to support global features in classes list
                }
            }
            System.gc();



        Instant endAll = Instant.now();
        System.out.println(Duration.between(startAll, endAll));

    }
    /**
     * creates the searchers depending on HashingMode <code>m</code>
     * @param m HashingMode to be used
     * @param classes classes for which a searcher is to be created
     * @param outFiles  paths to outfiles (only for {@link Main.HashingMode#HASHING_MODE_METRIC_SPACES}
     * @param readers   array of indexreaders
     * @param searchers array for created searchers
     * @param maxHits   maximum hits that searchers should generate
     */
    private static void setupSearchers(HashingMode m, Class[] classes, String[] outFiles, IndexReader[] readers, KerasSearcher[] searchers,int searchersOffset, int maxHits) {
        if (  m == HashingMode.HASHING_MODE_METRIC_SPACES) //NOTE: MetricSpaces searching
        {

            try {
                for(int i = 0; i < classes.length; i++){
                    searchers[i + searchersOffset] = new KerasMetricSpacesImageSearcher(maxHits, new FileInputStream(outFiles[i]), maxHits, false, readers[i]);
                    ((KerasMetricSpacesImageSearcher)searchers[i]).setNumHashesUsedForQuery(20);
                }

            } catch (IllegalAccessException | InstantiationException | FileNotFoundException | ClassNotFoundException e) {
                e.printStackTrace();
            }

        }
        else//NOTE: BitSampling searching
        {

            for(int i = 0; i < classes.length; i++){
                try {
                    searchers[i + searchersOffset] = new KerasBitSamplingImageSearcher(maxHits, (GlobalFeature) classes[i].newInstance());
                } catch (InstantiationException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }


        }
    }
    /**
     * indexes the <code>filesToIndex</code> according to HashingMode <code>m</code>
     * @param m HashingMode to be used
     * @param indexPath path to folder for index
     * @param inFile    inFiles (list of filenames) (only for {@link Main.HashingMode#HASHING_MODE_METRIC_SPACES})
     * @param classes   classes that are used to create the index
     * @param outFiles  list of filepaths for the outfiles
     * @param csvFiles  list of CsvFiles with featurevectors for the files that are indexed
     * @param filesToIndex  filepaths to the files to index
     */
    private static void index(HashingMode m, String indexPath, String inFile, Class[] classes, String[] outFiles, String[] csvFiles, Vector<String> filesToIndex) {
        if(  m == HashingMode.HASHING_MODE_METRIC_SPACES)//NOTE: MetricSpaces indexing
        {
            try {
                KerasIndexer indexer = new KerasIndexer(indexPath, true, LuceneUtils.AnalyzerType.WhitespaceAnalyzer, true, GlobalDocumentBuilder.HashingMode.MetricSpaces, false, filesToIndex);
                for (int i = 0; i < classes.length; i++) {
                    if(KerasFeature.class.isAssignableFrom(classes[i])) {
                        indexer.addExtractor(classes[i], csvFiles[i]);
                    } else {
                        indexer.addExtractor(classes[i]);
                    }
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
    /**
     * retrieves the results from the InmagesearchHits <code>hits</code> and generates a Map with predictions
     * @param hits      search results from the searchers
     * @param readers   array of indexreaders
     * @param fname     name of the search file
     * @param allCategories list of categories
     * @param classNames    array of classnames
     * @param outFilePathBase   path to folder for output
     * @return map with predictions and scores
     */
    public static  LinkedHashMap<String, Double> getResults(ImageSearchHits[] hits, IndexReader[] readers, String fname,Vector<String> allCategories, String[] classNames, String outFilePathBase) {
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

                builder.append(classNames[j % classNames.length]);
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
            FileUtils.writeStringToFile(new File(outFilePathBase + "best_result_per_feature.txt"),builder.toString(), (Charset) null,true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ImageSearchHitClassifier classifier = new ImageSearchHitClassifier(allCategories, hitsStrings);
        return (LinkedHashMap<String, Double>) classifier.getPredictions();
    }
}






