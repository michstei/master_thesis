package main;

import net.semanticmetadata.lire.builders.DocumentBuilder;
import net.semanticmetadata.lire.builders.GlobalDocumentBuilder;
import net.semanticmetadata.lire.imageanalysis.features.global.CEDD;
import net.semanticmetadata.lire.indexers.hashing.MetricSpaces;
import net.semanticmetadata.lire.indexers.parallel.ParallelIndexer;
import net.semanticmetadata.lire.searchers.BitSamplingImageSearcher;
import net.semanticmetadata.lire.searchers.ImageSearchHits;
import net.semanticmetadata.lire.searchers.ImageSearcher;
import net.semanticmetadata.lire.searchers.MetricSpacesImageSearcher;
import org.apache.commons.io.FileUtils;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class Main {
    private static String indexPath = "index";
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

        ArrayList<String>fs = new ArrayList<>();
        listf(folderPath,fs);
        String inFile = "inFile.lst";
        String outFile = "out.cedd.dat";
        try{
            FileUtils.writeLines(new File(inFile),fs);
            setupMetricSpaces(inFile,outFile);
            indexWithMetricSpaces(outFile);
    //        index();
            System.out.println("indexing done");
            files.add("colon-clear/1.jpg");
            files.add("dyed-lifted-polyps/1.jpg");
            files.add("instruments/1.jpg");
            files.add("out-of-patient/1.jpg");

            for(String s:files) {
                System.out.println(s);
//                search(folderPath + s);
                searchWithMetricSpaces(folderPath + s,outFile);
            }
        } catch(Exception e){
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

    private static void setupMetricSpaces(String inFile,String outFile) throws IllegalAccessException, IOException, InstantiationException {
        MetricSpaces.indexReferencePoints(CEDD.class,5000,50,new File(inFile),new File(outFile));
    }
}
