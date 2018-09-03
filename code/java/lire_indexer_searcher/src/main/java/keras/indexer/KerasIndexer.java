package keras.indexer;

import keras.documentbuilder.KerasDocumentBuilderImpl;
import keras.features.KerasFeature;
import net.semanticmetadata.lire.builders.GlobalDocumentBuilder;
import net.semanticmetadata.lire.imageanalysis.features.GlobalFeature;
import net.semanticmetadata.lire.indexers.hashing.MetricSpaces;
import net.semanticmetadata.lire.utils.LuceneUtils;
import org.apache.commons.io.FileUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;

import javax.imageio.ImageIO;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.*;

public class KerasIndexer {
    private KerasDocumentBuilderImpl documentBuilder = null;
    private IndexWriter indexWriter = null;
    private ArrayList<String> filesToindex = null;
    private String indexFolder = null;

    private GlobalDocumentBuilder.HashingMode hashingMode = null;
    private boolean hashingEnabled = false;
    private boolean useDocValues = false;

    public KerasIndexer(String indexFolder,
                        boolean iwCreate,
                        LuceneUtils.AnalyzerType analyzerType,
                        boolean hashingEnabled,
                        GlobalDocumentBuilder.HashingMode hashingMode,
                        boolean useDocValues,
                        String pathToFilesFolder) throws IOException {
        this.indexFolder = indexFolder;
        this.hashingEnabled = hashingEnabled;
        this.useDocValues = useDocValues;
        this.hashingMode = hashingMode;
        this.filesToindex = new ArrayList<>();
        listFilesFromDirectory(pathToFilesFolder,this.filesToindex);
        this.documentBuilder = new KerasDocumentBuilderImpl(this.hashingEnabled,this.hashingMode,this.useDocValues);
        this.indexWriter = LuceneUtils.createIndexWriter(this.indexFolder,iwCreate,analyzerType);

    }
    public KerasIndexer(String indexFolder,
                        boolean iwCreate,
                        LuceneUtils.AnalyzerType analyzerType,
                        boolean hashingEnabled,
                        GlobalDocumentBuilder.HashingMode hashingMode,
                        boolean useDocValues,
                        Vector<String> files) throws IOException {
        this.indexFolder = indexFolder;
        this.hashingEnabled = hashingEnabled;
        this.useDocValues = useDocValues;
        this.hashingMode = hashingMode;
        this.filesToindex = new ArrayList<>(files);
        this.documentBuilder = new KerasDocumentBuilderImpl(this.hashingEnabled,this.hashingMode,this.useDocValues);
        this.indexWriter = LuceneUtils.createIndexWriter(this.indexFolder,iwCreate,analyzerType);

    }


    public void addExtractor(Class<? extends GlobalFeature> featureClass){
        documentBuilder.addExtractor(featureClass);
    }

    public void addExtractor(Class<? extends KerasFeature> featureClass, String csvFile) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method m = featureClass.getMethod("setCsvFilename",String.class);
        m.invoke(null,csvFile);
        documentBuilder.addExtractor(featureClass);
    }
    public boolean addFileToIndex(String filePath){
        if(this.filesToindex != null){
            this.filesToindex.add(filePath);
            return true;
        }
        return false;
    }

    public boolean addFilesToIndex(List<String> filePaths){
        if(this.filesToindex != null){
            this.filesToindex.addAll(filePaths);
            return true;
        }
        return false;
    }

    public ArrayList<String> getFilesToindex(){
        return this.filesToindex;
    }

    public String getIndexFolder(){
        return this.indexFolder;
    }

    public MetricSpaces.Parameters  loadReferencePoints(String filePath) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        return MetricSpaces.loadReferencePoints(new FileInputStream(filePath));
    }
    public void indexReferencePoints(Class globalFeatureClass, int numberOfReferencePoints, int lenghtOfPostingList, File inFile, File outFile) throws IOException, IllegalAccessException, InstantiationException {
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

        GlobalFeature feature = (GlobalFeature)globalFeatureClass.newInstance();
        bw.write("# Created " + (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(new Date()) + " by " + MetricSpaces.class.getName() + " \n");
        bw.write(feature.getClass().getName() + "\n");
        bw.write(numberOfReferencePoints + "," + lenghtOfPostingList + "\n");
        System.out.print("Indexing ");
        int i = 0;
        Iterator iterator = lines.iterator();

        while(iterator.hasNext() && i < numberOfReferencePoints) {
            String file = (String)iterator.next();

            try {
                if(feature instanceof KerasFeature)
                    ((KerasFeature)feature).extract(file);
                else
                    feature.extract(ImageIO.read(new File(file)));
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

    public void index() throws IOException {
        String s ="|--------------------|";
        int valOld = 0;
        for(int index = 0; index < this.filesToindex.size(); index++){

            Document doc = documentBuilder.createDocument(this.filesToindex.get(index),this.filesToindex.get(index));
            indexWriter.addDocument(doc);
            double val = (index / (float)(this.filesToindex.size()-1) * 100);
            if(((int)val) != valOld && ((int)val) % 5 == 0){
                s = s.replaceFirst("-","#");
            }
            valOld = (int) val;
            System.out.printf("\r%s%05.2f%%",s,val);
        }
        System.out.println();
        LuceneUtils.closeWriter(indexWriter);
    }


    public void createInFile(String inFilePath) throws IOException {
        if(this.filesToindex != null ){
            FileUtils.writeLines(new File(inFilePath),this.filesToindex);
        }
    }
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

}
