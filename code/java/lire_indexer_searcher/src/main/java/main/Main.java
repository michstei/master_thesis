package main;

import keras.documentbuilder.KerasDocumentBuilder;
import keras.documentbuilder.KerasDocumentBuilderImpl;
import keras.features.KerasFeature;
import keras.features.VGG16;
import keras.features.VGG19;
import keras.searcher.KerasMetricSpacesSearcher;
import net.semanticmetadata.lire.builders.DocumentBuilder;
import net.semanticmetadata.lire.builders.GlobalDocumentBuilder;
import net.semanticmetadata.lire.imageanalysis.features.global.CEDD;
import net.semanticmetadata.lire.indexers.hashing.MetricSpaces;
import net.semanticmetadata.lire.indexers.parallel.ParallelIndexer;
import net.semanticmetadata.lire.searchers.BitSamplingImageSearcher;
import net.semanticmetadata.lire.searchers.ImageSearchHits;
import net.semanticmetadata.lire.searchers.ImageSearcher;
import net.semanticmetadata.lire.searchers.MetricSpacesImageSearcher;
import net.semanticmetadata.lire.utils.LuceneUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.FSDirectory;

import javax.imageio.ImageIO;
import java.io.*;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

public class Main {
    private static String indexPath = "indexKeras";
    private static String folderPath = "/home/michael/master_thesis/data/Medico_2018_development_set/";
    private static Vector<String> files = new Vector<>();

    public static void listf(String directoryName, List<String> files) {
        File directory = new File(directoryName);

        // Get all the files from a directory.
        File[] fList = directory.listFiles();
        for (File file : fList) {
            if (file.isFile()) {
                files.add(file.getAbsolutePath());
            } else if (file.isDirectory()) {
                listf(file.getAbsolutePath(), files);
            }
        }
    }

    public static void main(String[] args) {
        String inFile = "inFile.lst";
        String outFile = "out.vgg16.dat";
        String outFile2 = "out.vgg19.dat";
//        if(false)
        {
            try {
                VGG16.setCsvFilename("/home/michael/master_thesis/data/csv/vgg16.csv");
                VGG19.setCsvFilename("/home/michael/master_thesis/data/csv/vgg19.csv");
                setupMetricSpaces(VGG16.class, 5000, 50, inFile, outFile);
                setupMetricSpaces(VGG19.class, 5000, 50, inFile, outFile2);
                MetricSpaces.loadReferencePoints(new FileInputStream(outFile));
                MetricSpaces.loadReferencePoints(new FileInputStream(outFile2));
                kerasIndex();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        files.add("colon-clear/1.jpg");
        files.add("dyed-lifted-polyps/1.jpg");
        files.add("instruments/1.jpg");
        files.add("out-of-patient/1.jpg");
        for(String s:files) {
            String filePath = folderPath + s;
            IndexReader reader = null;
            try {
                reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexPath)));
            } catch (IOException e) {
                e.printStackTrace();
            }
            KerasMetricSpacesSearcher searcher = null;
            try {
                searcher = new KerasMetricSpacesSearcher(10,new FileInputStream(outFile),10,true,reader);
//                searcher = new KerasMetricSpacesSearcher(10,new FileInputStream(outFile),10);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            searcher.setNumHashesUsedForQuery(20);
            ImageSearchHits hits = null;
            try {
                hits = searcher.search(filePath,reader);
            } catch (IOException e) {
                e.printStackTrace();
            }

            for (int i = 0; i < hits.length(); i++) {
                String filename = null;
                try {
                    filename = reader.document(hits.documentID(i)).getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0];
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println(hits.score(i) + ": \t" + filename);
            }
        }
//        ArrayList<String>fs = new ArrayList<>();
//        listf(folderPath,fs);
//        String inFile = "inFile.lst";
//        String outFile = "out.cedd.dat";
//        try{
//            FileUtils.writeLines(new File(inFile),fs);
//            setupMetricSpaces(inFile,outFile);
//            indexWithMetricSpaces(outFile);
//    //        index();
//            System.out.println("indexing done");
//            files.add("colon-clear/1.jpg");
//            files.add("dyed-lifted-polyps/1.jpg");
//            files.add("instruments/1.jpg");
//            files.add("out-of-patient/1.jpg");
//
//            for(String s:files) {
//                System.out.println(s);
////                search(folderPath + s);
//                searchWithMetricSpaces(folderPath + s,outFile);
//            }
//        } catch(Exception e){
//            e.printStackTrace();
//        }
    }

    private static void kerasIndex() throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {

        KerasDocumentBuilder docBuilder = new KerasDocumentBuilderImpl(true, GlobalDocumentBuilder.HashingMode.MetricSpaces,true);

        ((KerasDocumentBuilderImpl) docBuilder).addExtractor(VGG16.class);
        ((KerasDocumentBuilderImpl) docBuilder).addExtractor(VGG19.class);
        ArrayList<String> files = new ArrayList<>();
        listf( folderPath,files);
        try (IndexWriter iw = LuceneUtils.createIndexWriter(indexPath, true, LuceneUtils.AnalyzerType.WhitespaceAnalyzer)) {
            int counter = 0;
            double max = files.size();
            for(String s :files){
                System.out.println((counter++/max*100) + "%");
                Document doc = docBuilder.createDocument(s,s);
                iw.addDocument(doc);
            }
            LuceneUtils.closeWriter(iw);
        }catch(Exception e){
           e.printStackTrace();
        }

    }
    private static void index() {
        ParallelIndexer indexer = new ParallelIndexer(6,indexPath,folderPath, GlobalDocumentBuilder.HashingMode.BitSampling);
        indexer.addExtractor(CEDD.class);
        indexer.run();
    }
    private static void indexWithMetricSpaces(String outFile) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        MetricSpaces.loadReferencePoints(new FileInputStream(outFile));
        ParallelIndexer indexer = new ParallelIndexer(6,indexPath,folderPath, GlobalDocumentBuilder.HashingMode.MetricSpaces);
        indexer.addExtractor(CEDD.class);
        indexer.run();
    }
    private static void search(String filePath) throws IOException {
        IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexPath)));
        ImageSearcher searcher = new BitSamplingImageSearcher(5,new CEDD());

        ImageSearchHits hits = searcher.search(ImageIO.read(new File(filePath)),reader);

        for(int i = 0; i < hits.length(); i++){
            String filename = reader.document(hits.documentID(i)).getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0];
            System.out.println(hits.score(i) + ": \t" + filename);
        }
    }
    private static void searchWithMetricSpaces(String filePath,String outFile) throws IOException, IllegalAccessException, ClassNotFoundException, InstantiationException {
        IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexPath)));
        MetricSpacesImageSearcher searcher = new MetricSpacesImageSearcher(10,new FileInputStream(outFile),10);
        searcher.setNumHashesUsedForQuery(20);
        ImageSearchHits hits = searcher.search(ImageIO.read(new File(filePath)),reader);

        for(int i = 0; i < hits.length(); i++){
            String filename = reader.document(hits.documentID(i)).getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0];
            System.out.println(hits.score(i) + ": \t" + filename);
        }
    }

    private static void setupMetricSpaces(Class c,int numRefPoints,int postingListLength,String inFile,String outFile) throws IllegalAccessException, IOException, InstantiationException {
        indexReferencePointsKeras(c,numRefPoints,postingListLength,new File(inFile),new File(outFile));
    }

    public static void indexReferencePointsKeras(Class globalFeatureClass, int numberOfReferencePoints, int lenghtOfPostingList, File inFile, File outFile) throws IOException, IllegalAccessException, InstantiationException {
        BufferedReader br = new BufferedReader(new FileReader(inFile));
        BufferedWriter bw = new BufferedWriter(new FileWriter(outFile), 26214400);
        LinkedList<String> lines = new LinkedList();
        System.out.println("Reading input file.");

        String line;
        while((line = br.readLine()) != null) {
            if (!line.startsWith("#") && line.trim().length() > 1) {
                lines.add(line);
            }
        }

        br.close();
        System.out.printf("Read %,d lines from the input file. Now selecting reference points.\n", lines.size());
        Collections.shuffle(lines);
        KerasFeature feature = (KerasFeature)globalFeatureClass.newInstance();
        bw.write("# Created " + (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(new Date()) + " by " + MetricSpaces.class.getName() + " \n");
        bw.write(feature.getClass().getName() + "\n");
        bw.write(numberOfReferencePoints + "," + lenghtOfPostingList + "\n");
        System.out.print("Indexing ");
        int i = 0;
        Iterator iterator = lines.iterator();

        while(iterator.hasNext() && i < numberOfReferencePoints) {
            String file = (String)iterator.next();

            try {
                feature.extract(file);
                bw.write(Base64.getEncoder().encodeToString(feature.getByteArrayRepresentation()) + "\n");
                ++i;
                if (i % (numberOfReferencePoints >> 5) == 0) {
                    System.out.print('.');
                }
            } catch (Exception var14) {
                System.out.printf("Having problem \"%s\" with file %s\n", var14.getMessage(), file);
            }
        }

        System.out.println();
        bw.close();
    }
}
