package org.exist.xquery.modules.mpeg7.x3d.textures.SURFManager;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import javax.activation.MimetypesFileTypeMap;
import net.sf.javaml.clustering.Clusterer;
import net.sf.javaml.clustering.KMeans;
import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.DefaultDataset;
import net.sf.javaml.core.DenseInstance;
import net.sf.javaml.core.Instance;
import net.sf.javaml.distance.DistanceMeasure;
import net.sf.javaml.distance.EuclideanDistance;
import net.sf.javaml.tools.data.FileHandler;
import org.exist.xquery.modules.mpeg7.x3d.helpers.CommonUtils;
//import org.apache.log4j.Logger;

/**
 *
 * @author Markos | Patti Spala <pd.spala@gmail.com>
 */
public class BuildSURFVocabularies {

    //private static final Logger logger = Logger.getLogger(BuildSURFVocabularies.class);
    public static void main(String args[]) throws URISyntaxException {
        int vocabSize = 128;

        List<Path> fileList = ListDirectories.plainFileListing("/usr/local/exist2.2/webapp/WEB-INF/data/fs/db/3dData/x3d");
        Dataset entireDataset = new DefaultDataset();
        Dataset imageSURF;
        SURFOperations surfOperations = new SURFOperations();

        File forSizeEval;
        Double[] surfValues = new Double[64];
        double[] imageSurfValues = new double[64];

        System.out.println("Total files count: " + fileList.size());
        for (Path file : fileList) {
            try {
                //System.out.println(file.toString());
                forSizeEval = new File(file.toString());
                MimetypesFileTypeMap mtftp = new MimetypesFileTypeMap();
                mtftp.addMimeTypes("image png tif gif jpg jpeg bmp");
                String mimetype =mtftp.getContentType(forSizeEval);                                
                // Skip files that are not images and larger than 5MB for memory considerations
                if ((mimetype.equals("image")) && (forSizeEval.length() <= (5 * Math.pow(2, 20)))) {
                    imageSURF = surfOperations.extractFromFile(file.toString());
                    for (int i = 0; i < imageSURF.size(); i++) {
                        surfValues = imageSURF.get(i).values().toArray(surfValues);
                        for (int j = 0; j < surfValues.length; j++) {
                            imageSurfValues[j] = surfValues[j];
                        }
                        imageSURF.set(i, new DenseInstance(imageSurfValues));
                    }
                    entireDataset.addAll(imageSURF);
                }
            } catch (Exception ex) {
                System.out.println("Exception: " + ex);
                System.out.println("***************************************************************");
            }
        }
        //exportDataSet
        try {
            FileHandler.exportDataset(entireDataset, new File("/usr/local/exist2.2/VocabularyData/EntireDataset.iData"));
        } catch (IOException e) {
            System.out.println("FailedToStore");
            System.out.println(e);
        }
        //load the Complete DataSet
        try {
            entireDataset = FileHandler.loadDataset(new File("/usr/local/exist2.2/VocabularyData/EntireDataset.iData"));
        } catch (IOException ex) {
            System.out.println(ex);
            //logger.error(ex);
        }

        Instance[] vocabulary = organizeVocab(entireDataset, vocabSize);

        Dataset vocabToStore = new DefaultDataset();
        for (Instance vocabInstance : vocabulary) {
            vocabToStore.add(vocabInstance);
        }
        try {
            String fileName = "Vocab" + String.valueOf(vocabSize) + ".iVocab";
            URL link = new URL("http://54.72.206.163/exist/3dData/Vocabularies/SURF/" + fileName);
            CommonUtils.downloadFile(fileName, link);
            File vocabFile = new File(fileName);
            FileHandler.exportDataset(vocabToStore, vocabFile);
        } catch (IOException ex) {
            System.out.println(ex);
            //logger.error(ex);
        }

    }

    public static Instance[] organizeVocab(Dataset data, int numberOfClusters) {

        int subSampleSize = 200000;
        int numberOfIterations = 500;
        DistanceMeasure dm = new EuclideanDistance();
        int FeatureLength = data.get(0).values().size();

        //System.out.println("Total " + String.valueOf(data.size()) + " points of length " + String.valueOf(FeatureLength));
        Integer[] IndexesArray = new Integer[data.size()];
        for (int ii = 0; ii < data.size(); ii++) {
            IndexesArray[ii] = ii;
        }
        ArrayList<Integer> IndexesList = new ArrayList<Integer>(Arrays.asList(IndexesArray));
        java.util.Collections.shuffle(IndexesList);

        Dataset subData = new DefaultDataset();

        int indexesListSize = IndexesList.size();
        if (indexesListSize<subSampleSize) subSampleSize = indexesListSize;        
        for (int ii = 0; ii < subSampleSize; ii++) {
            subData.add(data.get(IndexesList.get(ii)));
            //subData.add(data.get(ii));
        }
        //System.out.println("Kept " + String.valueOf(subData.size()) + " points of length " + String.valueOf(FeatureLength));

        try {
            FileHandler.exportDataset(subData, new File("/usr/local/exist2.2/VocabularyData/subDataset.iData"));
        } catch (IOException ex) {
//            logger.error(ex);
            System.out.println(ex);
        }

        Clusterer km = new KMeans(numberOfClusters, numberOfIterations, dm);
        Dataset[] clusters = km.cluster(subData);

        //Calculate cluster centroids
        Instance[] centroids = new Instance[numberOfClusters];

        double[][] sumPosition = new double[numberOfClusters][FeatureLength];
        int[] countPosition = new int[numberOfClusters];
        // System.out.println(String.valueOf(clusters.length) + " clusters. Membership count:");
        for (int ClusterID = 0; ClusterID < numberOfClusters; ClusterID++) {
            //System.out.println(clusters[ClusterID].size());
            for (int i = 0; i < clusters[ClusterID].size(); i++) {
                Instance in = clusters[ClusterID].instance(i);
                for (int j = 0; j < FeatureLength; j++) {
                    sumPosition[ClusterID][j] += in.value(j);
                }
                countPosition[ClusterID]++;
            }
            if (countPosition[ClusterID] > 0) {
                double[] tmp = new double[FeatureLength];
                for (int j = 0; j < FeatureLength; j++) {
                    tmp[j] = (float) sumPosition[ClusterID][j] / countPosition[ClusterID];
                }
                centroids[ClusterID] = new DenseInstance(tmp);
            }
        }
        return centroids;
    }
}

class ListDirectories extends SimpleFileVisitor<Path> {

    List<Path> files = new LinkedList<Path>();

    @Override
    public FileVisitResult preVisitDirectory(Path dir,
            BasicFileAttributes attrs) {
        //System.out.println("Dir: " + dir.toString());
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
        files.add(file);
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException e) {
        return FileVisitResult.CONTINUE;
    }

    public static List<Path> plainFileListing(String PathStr) {

        Path search_directory_path = Paths.get(PathStr);
        //define the starting file tree
        ListDirectories traverser = new ListDirectories();
        //instantiate the walk
        try {
            Files.walkFileTree(search_directory_path, traverser);
            //start the walk
        } catch (IOException e) {
            System.err.println(e);
        }
        return traverser.files;
    }
}
