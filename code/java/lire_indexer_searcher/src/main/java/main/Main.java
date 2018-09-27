package main;

import classifier.WeightedImageSearchHitClassifier;
import classifier.Weights;
import keras.documentbuilder.KerasDocumentBuilderImpl;
import keras.features.*;
import keras.indexer.KerasIndexer;
import keras.searcher.KerasBitSamplingImageSearcher;
import keras.searcher.KerasMetricSpacesImageSearcher;
import keras.searcher.KerasSearcher;
import keras.searcher.SearchRunnable;
import org.apache.commons.io.FileUtils;
import utils.Category;
import utils.FilePrep;
import utils.MedicoConfusionMatrix;
import classifier.ImageSearchHitClassifier;
import net.semanticmetadata.lire.builders.DocumentBuilder;
import net.semanticmetadata.lire.builders.GlobalDocumentBuilder;
import net.semanticmetadata.lire.imageanalysis.features.GlobalFeature;
import net.semanticmetadata.lire.searchers.ImageSearchHits;
import net.semanticmetadata.lire.utils.LuceneUtils;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

/***
 * class to perform experiments for task
 */
@SuppressWarnings("FieldCanBeLocal")
public class Main {

    private static Class[]  all_classes_double =    {   DenseNet121_Double.class,  DenseNet169_Double.class,   DenseNet201_Double.class,   ResNet50_Double.class,  MobileNet_Double.class, VGG16_Double.class, VGG19_Double.class, Xception_Double.class   };
    private static Class[]  all_classes_float =     {   DenseNet121_Float.class,   DenseNet169_Float.class,    DenseNet201_Float.class,    ResNet50_Float.class,   MobileNet_Float.class,  VGG16_Float.class,  VGG19_Float.class,  Xception_Float.class    };
    private static Class[]  all_classes_long =      {   DenseNet121_Long.class,    DenseNet169_Long.class,     DenseNet201_Long.class,      ResNet50_Long.class,    MobileNet_Long.class,   VGG16_Long.class,   VGG19_Long.class,   Xception_Long.class     };
    private static Class[]  all_classes_int =       {   DenseNet121_Int.class,     DenseNet169_Int.class,      DenseNet201_Int.class,      ResNet50_Int.class,     MobileNet_Int.class,    VGG16_Int.class,    VGG19_Int.class,    Xception_Int.class      };
    private static Class[]  all_classes_short =     {   DenseNet121_Short.class,   DenseNet169_Short.class,    DenseNet201_Short.class,    ResNet50_Short.class,   MobileNet_Short.class,  VGG16_Short.class,  VGG19_Short.class,  Xception_Short.class    };
    private static Class[]  all_classes_byte =      {   DenseNet121_Byte.class,    DenseNet169_Byte.class,     DenseNet201_Byte.class,     ResNet50_Byte.class,    MobileNet_Byte.class,   VGG16_Byte.class,   VGG19_Byte.class,   Xception_Byte.class     };

//    private static Class<? extends GlobalFeature>[] globalFeatures = new Class[]{AutoColorCorrelogram.class, CEDD.class, ACCID.class, ColorLayout.class, EdgeHistogram.class, FCTH.class,  Gabor.class, JCD.class, LuminanceLayout.class,PHOG.class, ScalableColor.class, Tamura.class};
private static Class<? extends GlobalFeature>[] globalFeatures = new Class[]{};
//    private static Class<? extends GlobalFeature>[] globalFeatures = new Class[]{ACCID.class, ColorLayout.class};




    enum HashingMode{
        HASHING_MODE_METRIC_SPACES,
        HASHING_MODE_BITSAMPLING,


    }

    private static int maxHits = 3;
    private static String basePath = "/home/michael/master_thesis/data/";
    private static String inFileTrain = basePath + "indexCreationFiles/inFileTrain.lst";
    private static String inFileTest = basePath + "indexCreationFiles/inFileTest.lst";
    private static String imageFolderPath = basePath + "Medico_2018_development_set/";
    private static String imageFolderPathTest = basePath + "Medico_2018_test_set/";
    private static boolean useBitSampling = true;
    private static boolean useMetricSpaces = false;
    private static boolean skipIndexing = false;
    private static boolean generateInFiles = true;
    private static int numRuns = 40;
    private static boolean testset = true;

    public static void main(String[] args) throws Exception {


        Instant startAll = Instant.now();
        MedicoConfusionMatrix globalMatrix = new MedicoConfusionMatrix();
        if(testset) numRuns = 1;
        for(int runIdx = 1; runIdx <= numRuns; runIdx++) {
            System.out.println("run " + runIdx);
            if (generateInFiles) {
                if(testset){
                    Vector<String> train = new Vector<>(), test = new Vector<>();
                    FilePrep.listFilesFromDirectory(imageFolderPath,train);
                    FilePrep.listFilesFromDirectory(imageFolderPathTest,test);
                    FilePrep.writeFile(inFileTrain,train);
                    FilePrep.writeFile(inFileTest,test);
                }
                else {
                    FilePrep prep = new FilePrep(imageFolderPath, 5, inFileTrain, inFileTest);
                    prep.writeSetFiles();
                }
            }
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

            String folder = testset?"testset/":"kgcw/" + runIdx + "/";
            String csvBasePath = basePath + "csv/1024orLess/";
            String csvBasePathTest = basePath + "csv/1024orLessTest/";
            String outFileBasePath = basePath + "indexCreationFiles/" + folder;
            String outputFolderPath = basePath + "results/" + folder;
            String[] indexPath = {basePath + "index/" + folder + "MetricSpaces/", basePath + "index/" + folder + "BitSampling/"};
            KerasDocumentBuilderImpl.maxDimensions = 1024;

            Class[] classes = new Class[all_classes_int.length + globalFeatures.length];
            String[] classNames = new String[all_classes_int.length + globalFeatures.length];
//
            for (int i = 0; i < all_classes_int.length; i++) {
                classes[i] = all_classes_int[i];
                classNames[i] = all_classes_int[i].getName().replace("keras.features.", "");
            }
            for (int i = all_classes_int.length; i < classes.length; i++) {
                classes[i] = globalFeatures[i - all_classes_int.length];
                classNames[i] = globalFeatures[i - all_classes_int.length].getName().replace("net.semanticmetadata.lire.imageanalysis.features.global.", "");
            }
            Weights weights = null;
            try {
                weights = setupWeights(new Vector<>(Arrays.asList(classNames)));
            }catch (Exception e){
                e.printStackTrace();
                return;
            }
//        int featureIndex = 11; //stopped at FCTH - index 5
//        Class[] classes = new Class[]{globalFeatures[featureIndex]};
//        String[] classNames = new String[]{globalFeatures[featureIndex].getName().replace("net.semanticmetadata.lire.imageanalysis.features.global.","")};
            if (!(new File(outputFolderPath).exists())) {
                new File(outputFolderPath).mkdirs();
            }
            new File(outputFolderPath).mkdirs();
            String outputFilePathBase = outputFolderPath + "results_";


            String[] outFiles = new String[classes.length];
            String[] csvFiles = new String[classes.length];
            String[] csvFilesTest = new String[classes.length];
            for (int i = 0; i < classes.length; i++) {
                if (!(new File(outFileBasePath).exists()))
                    new File(outFileBasePath).mkdirs();
                outFiles[i] = outFileBasePath + "out." + classNames[i] + ".dat";
                csvFiles[i] = csvBasePath + "quantized/" + classNames[i].toLowerCase() + ".csv";
                csvFilesTest[i] = csvBasePathTest + "quantized/" + classNames[i].toLowerCase() + ".csv";
            }


            Instant startIndex = Instant.now();
            if (!skipIndexing || generateInFiles) {
                if (useMetricSpaces)
                    index(HashingMode.HASHING_MODE_METRIC_SPACES, indexPath[0], inFileTrain, classes, outFiles, csvFiles, trainFiles);
                if (useBitSampling)
                    index(HashingMode.HASHING_MODE_BITSAMPLING, indexPath[1], inFileTrain, classes, outFiles, csvFiles, trainFiles);

            } else {
                for (int i = 0; i < classes.length; i++) {
                    if (KerasFeature.class.isAssignableFrom(classes[i])) {
                        Method m = classes[i].getMethod("setCsvFilename", String.class);
                        m.invoke(null, csvFiles[i]);
                    }
                }
            }
            Instant endIndex = Instant.now();
            System.out.println("Indexing took: " + Duration.between(startIndex, endIndex));
            IndexReader[] readers = new IndexReader[classes.length * ((useMetricSpaces && useBitSampling) ? 2 : 1)];
            try {
                if (useMetricSpaces)
                    for (int i = 0; i < classes.length; i++) {
                        readers[i] = DirectoryReader.open(FSDirectory.open(Paths.get(indexPath[0])));
                    }
                if (useBitSampling)
                    for (int i = useMetricSpaces ? classes.length : 0; i < readers.length; i++) {
                        readers[i] = DirectoryReader.open(FSDirectory.open(Paths.get(indexPath[1])));
                    }
            } catch (IOException e) {
                e.printStackTrace();
            }
            KerasSearcher[] searchers = new KerasSearcher[classes.length * ((useMetricSpaces && useBitSampling) ? 2 : 1)];
//        for(int numResults = 1; numResults <= 10; numResults++) {

            for (int i = 0; i < classes.length; i++) {
                if (KerasFeature.class.isAssignableFrom(classes[i])) {
                    Method m = classes[i].getMethod("setCsvFilename", String.class);
                    m.invoke(null, csvFilesTest[i]);
                }
            }

            int numResults = 9;
            if (useMetricSpaces)
                setupSearchers(HashingMode.HASHING_MODE_METRIC_SPACES, classes, outFiles, readers, searchers, 0, numResults);
            if (useBitSampling)
                setupSearchers(HashingMode.HASHING_MODE_BITSAMPLING, classes, outFiles, readers, searchers, useMetricSpaces ? classes.length : 0, numResults);

            ImageSearchHits hits[] = new ImageSearchHits[searchers.length];
            Thread[] threads = new Thread[searchers.length];
            SearchRunnable[] runnables = new SearchRunnable[searchers.length];

            KerasFeature.DistanceFunction df = KerasFeature.DistanceFunction.DISTANCEFUNCTION_COSINE;

            Instant startDf = Instant.now();
            for (Class c : classes) {
                try {
                    c.getDeclaredField("USED_DISTANCE_FUN").set(null, df);
                } catch (Exception e) {//catch and ignore exception to support global features in classes list
                }
            }

            String outputFilePath = outputFilePathBase +  "run.txt";
            PrintStream out = null;
            try {

                if (!(new File(outputFilePath).exists())) {
                    new File(outputFilePath).createNewFile();
                }
                out = new PrintStream(new File(outputFilePath));
            } catch (IOException e) {
                e.printStackTrace();
            }

            int counter = 1;

            StringBuilder output = new StringBuilder();

            MedicoConfusionMatrix matrix = new MedicoConfusionMatrix();
            StringBuilder scores = new StringBuilder();
            StringBuilder findings = new StringBuilder();
            for (String testFile : testFiles) {
                Instant st = Instant.now();

                for (int i = 0; i < hits.length; i++) {
                    runnables[i] = new SearchRunnable(searchers[i], readers[i], testFile);
                    threads[i] = new Thread(runnables[i]);
                    threads[i].start();
                }

                for (int i = 0; i < hits.length; i++) {
                    try {
                        threads[i].join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
//                    hits[i] = runnables[i].getResult();
                }
                for (int i = 0; i < hits.length; i++) {
                    hits[i] = runnables[i].getResult();
                }
                //Weights weights, Vector<ImageSearchHits> imageSearchHits, Vector<String> classNames, Vector<IndexReader> indexReaders
                WeightedImageSearchHitClassifier classifier = new WeightedImageSearchHitClassifier(weights,
                        new Vector<>(Arrays.asList(hits)),
                        new Vector<>(Arrays.asList(classNames)),
                        new Vector<>(Arrays.asList(readers)),
                        findings

                );
                Vector<WeightedImageSearchHitClassifier.Prediction> predictions = classifier.getPredictions();
                output.append(testFile.substring(testFile.lastIndexOf('/') + 1));
                output.append(',');
                output.append(predictions.get(0).category.getName());
                output.append(',');
                output.append(String.format("%.3f",predictions.get(0).score));
                output.append('\n');
                scores.append(testFile).append(":\n");
                for (WeightedImageSearchHitClassifier.Prediction p : predictions) {
                    scores.append(p.score).append(" : ").append(p.category.getName()).append("\n");
                }
                if(testset) {
                    findings.append(testFile);
                    findings.append(":");
                    findings.append("\n");
                    Category catGold = null, catPred = null;
                    for (Category c : Category.values()) {
                        if (testFile.contains("/" + c.getName() + "/")) {
                            catGold = c;
                        }
                        if (predictions.get(0).category == c) {
                            catPred = c;
                        }
                    }
                    matrix.increaseValue(catGold, catPred);
                    globalMatrix.increaseValue(catGold, catPred);
                }
                Instant e = Instant.now();
                System.out.printf("\rprocessed file %4s of %d in %s / total %s", (counter++) + "", testFiles.size(), Duration.between(st, e), Duration.between(startDf, e));
            }
            FileUtils.writeStringToFile(new File(outputFilePathBase + "run_output.txt"), output.toString(), (String) null);
            FileUtils.writeStringToFile(new File(outputFilePathBase + "scores_per_class.txt"), scores.toString(), (String) null);
            Instant endDf = Instant.now();

            if(!testset) {
                FileUtils.writeStringToFile(new File(outputFilePathBase + "findings_per_class.txt"), findings.toString(), (String) null);
                System.out.println();
                out.println(String.format("total: %d\ncorrect: %d\nincorrect: %d\ncorrect Pcnt: %.2f%%\n", matrix.getTotal(), matrix.getCorrect(), matrix.getIncorrect(), matrix.getCorrectPcnt()));
                System.out.println(String.format("total: %d\ncorrect: %d\nincorrect: %d\ncorrect Pcnt: %.2f%%\n%s", matrix.getTotal(), matrix.getCorrect(), matrix.getIncorrect(), matrix.getCorrectPcnt(), Duration.between(startDf, endDf)));
                out.println(matrix.toString());
                matrix.printConfusionMatrix();
            }
//        }
//            for (Class c : classes) {
//                try {
//                    c.getDeclaredField("reader").set(null, null);
//                } catch (Exception e) {//catch and ignore exception to support global features in classes list
//                }
//            }
            System.gc();

        }
        Instant endAll = Instant.now();
        if(!testset) {
            System.out.println("TOTAL:");
            System.out.println(String.format("total: %d\ncorrect: %d\nincorrect: %d\ncorrect Pcnt: %.2f%%\n%s", globalMatrix.getTotal(), globalMatrix.getCorrect(), globalMatrix.getIncorrect(), globalMatrix.getCorrectPcnt(), Duration.between(startAll, endAll)));
            globalMatrix.printConfusionMatrix();
        }
        System.out.println("finished in " + Duration.between(startAll,endAll));
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
                    if(KerasFeature.class.isAssignableFrom(classes[i])) {
                        indexer.addExtractor(classes[i], csvFiles[i]);
                    } else {
                        indexer.addExtractor(classes[i]);
                    }
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
     * @param allCategories list of categories
     * @return map with predictions and scores
     */
    public static  LinkedHashMap<String, Double> getResults(ImageSearchHits[] hits, IndexReader[] readers,Vector<String> allCategories, String[] classNames, StringBuilder b) {
        Vector<String> hitsStrings = new Vector<>();
        for(int j = 0; j < hits.length; j++){
            b.append(classNames[j]).append(":\n");
            for (int i = 0; i < hits[j].length(); i++) {
                String filename = null;
                double score = 0;
                try {
                    score = hits[j].score(i);
                    filename = readers[j].document(hits[j].documentID(i)).getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0];

                } catch (IOException e) {
                    e.printStackTrace();
                }
                b.append(score).append(":").append(filename).append("\n");
                hitsStrings.add(filename);


            }
        }
        ImageSearchHitClassifier classifier = new ImageSearchHitClassifier(allCategories, hitsStrings);
        return (LinkedHashMap<String, Double>) classifier.getPredictions();
    }
    private static Weights setupWeights(Vector<String>classNames) throws Exception {
        Weights w = new Weights(classNames, new Vector<>(Arrays.asList(Category.values())));
        /*
        1  CATEGORY_BLURRY_NOTHING,
        2  CATEGORY_COLON_CLEAR,
        3  CATEGORY_DYED_LIFTED_POLYPS,
        4  CATEGORY_DYED_RESECTION_MARGINS ,
        5  CATEGORY_ESOPHAGITIS,
        6  CATEGORY_INSTRUMENTS,
        7  CATEGORY_NORMAL_CECUM,
        8  CATEGORY_NORMAL_PYLORUS,
        9  CATEGORY_NORMAL_Z_LINE,
        10 CATEGORY_OUT_OF_PATIENT,
        11 CATEGORY_POLYPS,
        12 CATEGORY_RETROFLEX_RECTUM,
        13 CATEGORY_RETROFLEX_STOMACH,
        14 CATEGORY_STOOL_INCLUSIONS,
        15 CATEGORY_STOOL_PLENTY,
        16 CATEGORY_ULCERATIVE_COLITIS;

         */
        int index = 0;
        Vector<Float> weights0,weights1,weights2,weights3,weights4,weights5,weights6,weights7,weights8,weights9;
        //                                    1        2        3       4      5       6        7        8        9      10      11     12       13       14       15       16
        weights0 = new Vector<>(Arrays.asList(0.125f,  0.125f,  0.16f,  0.05f, 0.3f,   0.125f,  0.01f,   0.2f,    0.1f,  0.125f, 0.2f,  0.125f,  0.18f,   0.125f,  0.125f,  0.1f));//DenseNet121_Int
        weights1 = new Vector<>(Arrays.asList(0.125f,  0.125f,  0.35f,  0.1f,  0.1f,   0.125f,  0.23f,   0.15f,   0.23f, 0.125f, 0.2f,  0.15f,   0.125f,  0.125f,  0.125f,  0.01f));//DenseNet169_Int
        weights2 = new Vector<>(Arrays.asList(0.125f,  0.125f,  0.01f,  0.19f, 0.2f,   0.125f,  0.15f,   0.25f,   0.1f,  0.125f, 0.16f, 0.15f,   0.226f,  0.125f,  0.125f,  0.17f));//DenseNet201_Int
        weights3 = new Vector<>(Arrays.asList(0.125f,  0.125f,  0.35f,  0.19f, 0.25f,  0.125f,  0.15f,   0.2f,    0.01f, 0.125f, 0.31f, 0.15f,   0.183f,  0.125f,  0.125f,  0.17f));//ResNet50_Int
        weights4 = new Vector<>(Arrays.asList(0.125f,  0.125f,  0.01f,  0.1f,  0.12f,  0.125f,  0.125f,  0.05f,   0.01f, 0.125f, 0.1f,  0.275f,  0.216f,  0.125f,  0.125f,  0.29f));//MobileNet_Int
        weights5 = new Vector<>(Arrays.asList(0.125f,  0.125f,  0.1f,   0.01f, 0.01f,  0.125f,  0.125f,  0.05f,   0.15f, 0.125f, 0.01f, 0.05f,   0.05f,   0.125f,  0.125f,  0.16f));//VGG16_Int
        weights6 = new Vector<>(Arrays.asList(0.125f,  0.125f,  0.01f,  0.01f, 0.01f,  0.125f,  0.2f,    0.05f,   0.25f, 0.125f, 0.01f, 0.05f,   0.01f,   0.125f,  0.125f,  0.1f));//VGG19_Int
        weights7 = new Vector<>(Arrays.asList(0.125f,  0.125f,  0.01f,  0.35f, 0.01f,  0.125f,  0.01f,   0.05f,   0.15f, 0.125f, 0.01f, 0.05f,   0.01f,   0.125f,  0.125f,  0.00f));//Xception_Int
//        weights8 = new Vector<>(Arrays.asList(0.1f,  0.1f,    0.15f, 0.05f, 0.05f,  0.1f,    0.1f,    0.1f,    0.05f, 0.1f,   0.01f, 0.1f,    0.1f,    0.1f,    0.1f,    0.04f));//ACCID
//        weights9 = new Vector<>(Arrays.asList(0.1f,  0.1f,    0.01f, 0.05f, 0.04f,  0.1f,    0.1f,    0.1f,    0.06f, 0.1f,   0.01f, 0.1f,    0.1f,    0.1f,    0.1f,    0.01f));//ColorLayout
        w.setWeightsForClass(classNames.get(index++),weights0);//DenseNet121_Int
        w.setWeightsForClass(classNames.get(index++),weights1);//DenseNet169_Int
        w.setWeightsForClass(classNames.get(index++),weights2);//DenseNet201_Int
        w.setWeightsForClass(classNames.get(index++),weights3);//ResNet50_Int
        w.setWeightsForClass(classNames.get(index++),weights4);//MobileNet_Int
        w.setWeightsForClass(classNames.get(index++),weights5);//VGG16_Int
        w.setWeightsForClass(classNames.get(index++),weights6);//VGG19_Int
        w.setWeightsForClass(classNames.get(index++),weights7);//Xception_Int
//        w.setWeightsForClass(classNames.get(index++),weights8);//ACCID
//        w.setWeightsForClass(classNames.get(index++),weights9);//ColorLayout
        if(!w.checkWeightSums()){
            throw new Exception("Weights don't sum up to 1.0!!!");
        }
        return w;
    }
}












