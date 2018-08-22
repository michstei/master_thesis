package main;

import keras.features.VGG16;
import keras.features.VGG19;
import keras.indexer.KerasIndexer;
import keras.searcher.KerasMetricSpacesSearcher;
import net.semanticmetadata.lire.builders.DocumentBuilder;
import net.semanticmetadata.lire.builders.GlobalDocumentBuilder;
import net.semanticmetadata.lire.imageanalysis.features.global.CEDD;
import net.semanticmetadata.lire.searchers.ImageSearchHits;
import net.semanticmetadata.lire.utils.LuceneUtils;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Paths;
import java.util.Vector;

public class Main {
    private static String indexPath = "/home/michael/master_thesis/data/index";
    private static String folderPath = "/home/michael/master_thesis/data/Medico_2018_development_set/";
    private static Vector<String> files = new Vector<>();



    public static void main(String[] args) {
        String inFile = "/home/michael/master_thesis/data/indexCreationFiles/inFile.lst";
        String outFile = "/home/michael/master_thesis/data/indexCreationFiles/out.vgg16.dat";
        String outFile2 = "/home/michael/master_thesis/data/indexCreationFiles/out.vgg19.dat";
        String outFile3 = "/home/michael/master_thesis/data/indexCreationFiles/out.cedd.dat";
//        if(false)
        {
            try {
                KerasIndexer indexer = new KerasIndexer(indexPath,true, LuceneUtils.AnalyzerType.WhitespaceAnalyzer,true, GlobalDocumentBuilder.HashingMode.MetricSpaces,true,folderPath);
                indexer.createInFile(inFile);
                indexer.addExtractor(VGG16.class,"/home/michael/master_thesis/data/csv/vgg16.csv");
                indexer.addExtractor(VGG19.class,"/home/michael/master_thesis/data/csv/vgg19.csv");
                indexer.addExtractor(CEDD.class);
                indexer.indexReferencePoints(VGG16.class,500,20,new File(inFile),new File(outFile));
                indexer.indexReferencePoints(VGG19.class,500,20,new File(inFile),new File(outFile2));
                indexer.indexReferencePoints(CEDD.class,500,20,new File(inFile),new File(outFile3));
                indexer.loadReferencePoints(outFile);
                indexer.loadReferencePoints(outFile2);
                indexer.loadReferencePoints(outFile3);
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
            KerasMetricSpacesSearcher searcher2 = null;
            KerasMetricSpacesSearcher searcher3 = null;
            try {
                searcher = new KerasMetricSpacesSearcher(10,new FileInputStream(outFile),3,true,reader);
                searcher2 = new KerasMetricSpacesSearcher(10,new FileInputStream(outFile2),3,true,reader);
                searcher3 = new KerasMetricSpacesSearcher(10,new FileInputStream(outFile3),3,true,reader);
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
            searcher2.setNumHashesUsedForQuery(20);
            searcher3.setNumHashesUsedForQuery(20);

            ImageSearchHits hits = null;
            ImageSearchHits hits2 = null;
            ImageSearchHits hits3 = null;
            try {
                hits = searcher.search(filePath,reader);
                hits2 = searcher2.search(filePath,reader);
                hits3 = searcher3.search(filePath,reader);
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println();
            System.out.println("##### VGG16 #####");
            for (int i = 0; i < hits.length(); i++) {
                String filename = null;
                try {
                    filename = reader.document(hits.documentID(i)).getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0];
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println(hits.score(i) + ": \t" + filename);
            }
            System.out.println("##### VGG19 #####");
            for (int i = 0; i < hits2.length(); i++) {
                String filename = null;
                try {
                    filename = reader.document(hits2.documentID(i)).getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0];
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println(hits2.score(i) + ": \t" + filename);
            }
            System.out.println("##### CEDD #####");
            for (int i = 0; i < hits3.length(); i++) {
                String filename = null;
                try {
                    filename = reader.document(hits3.documentID(i)).getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0];
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println(hits3.score(i) + ": \t" + filename);
            }
        }

    }




}
